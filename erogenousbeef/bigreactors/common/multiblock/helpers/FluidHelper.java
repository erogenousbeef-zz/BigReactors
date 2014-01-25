package erogenousbeef.bigreactors.common.multiblock.helpers;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

public abstract class FluidHelper {

	private FluidStack[] fluids;
	private int capacity;
	
	private int ticksSinceLastUpdate;
	private static final int minimumTicksBetweenUpdates = 60;
	private static final int minimumDevianceForUpdate = 50; // at least 50mB difference before we send a fueling update to the client

	int[] fluidLevelAtLastUpdate;

	private static final int FORCE_UPDATE = -1000;
	private int numberOfFluids;
	
	public FluidHelper() {
		fluids = new FluidStack[numberOfFluids];
		fluidLevelAtLastUpdate = new int[numberOfFluids];

		for(int i = 0; i < numberOfFluids; i++) {
			fluids[i] = null;
			fluidLevelAtLastUpdate[i] = FORCE_UPDATE;
		}
		
		capacity = 0;
	}

	public abstract int getNumberOfFluidTanks();
	protected abstract String[] getNBTTankNames();
	
	public boolean shouldSendFuelingUpdate() {
		ticksSinceLastUpdate++;
		if(minimumTicksBetweenUpdates < ticksSinceLastUpdate) {
			int dev = 0;
			boolean shouldUpdate = false;
			for(int i = 0; i < numberOfFluids && !shouldUpdate; i++) {
				
				if(fluids[i] == null && fluidLevelAtLastUpdate[i] > 0) {
					shouldUpdate = true;
				}
				else if(fluids[i] != null) {
					if(fluidLevelAtLastUpdate[i] == FORCE_UPDATE) {
						shouldUpdate = true;
					}
					else {
						dev += Math.abs(fluids[i].amount - fluidLevelAtLastUpdate[i]);
					}
				}
				// else, both levels are zero, no-op
				
				if(dev >= minimumDevianceForUpdate) {
					shouldUpdate = true;
				}
			}
			
			if(shouldUpdate) {
				resetLastSeenFluidLevels();
			}
			
			ticksSinceLastUpdate = 0;
			return shouldUpdate;
		}
		
		return false;
	}
	
	public int getCapacity() { return capacity; }
	
	public void setCapacity(int newCapacity) {
		int oldCapacity = capacity;
		capacity = newCapacity;

		if(oldCapacity > capacity) {
			clampContentsToCapacity();
		}
	}
	
	protected void merge(FluidHelper other) {
		capacity += other.capacity;
		for(int i = 0; i < fluids.length; i++) {
			if(other.fluids[i] != null ){
				if(fluids[i] == null) {
					fluids[i] = other.fluids[i];
				}
				else {
					if(fluids[i].isFluidEqual(other.fluids[i])) {
						// If fluids match, absorb the other stack
						fluids[i].amount += other.fluids[i].amount;
					}
					else if(fluids[i].amount < other.fluids[i].amount) {
						// If fluids do not match, take the bigger stack
						fluids[i] = other.fluids[i];
					}
				}
			}
		}
	}
	
	public boolean isFull() {
		return getTotalAmount() >= getCapacity();
	}
	
	public boolean isEmpty() {
		return getTotalAmount() <= 0;
	}
	
	/**
	 * @return Total amount of stuff contained, fuel + waste.
	 */
	public int getTotalAmount() {
		int amt = 0;
		for(int i = 0; i < fluids.length; i++) {
			amt += getFluidAmount(i);
		}
		return amt;
	}
	
	protected NBTTagCompound writeToNBT(NBTTagCompound destination) {
		String[] tankNames = getNBTTankNames();
		
		if(tankNames.length != fluids.length) { throw new IllegalArgumentException("getNBTTankNames must return the same number of strings as there are fluid stacks"); }

		FluidStack stack;
		for(int i = 0; i < tankNames.length; i++) {
			stack = fluids[i];
			if(stack != null) {
				destination.setCompoundTag(tankNames[i], stack.writeToNBT(new NBTTagCompound()));
			}
		}
		
		return destination;
	}
	
	protected void readFromNBT(NBTTagCompound data) {
		String[] tankNames = getNBTTankNames();
		
		if(tankNames.length != fluids.length) { throw new IllegalArgumentException("getNBTTankNames must return the same number of strings as there are fluid stacks"); }

		for(int i = 0; i < tankNames.length; i++) {
			if(data.hasKey(tankNames[i])) {
				fluids[i] = FluidStack.loadFluidStackFromNBT(data.getCompoundTag(tankNames[i]));
				fluidLevelAtLastUpdate[i] = fluids[i].amount;
			}
		}
	}
	
	////// FLUID HELPERS //////
	protected void setFluid(int idx, FluidStack newFluid) {
		fluids[idx] = newFluid;
	}
	
	protected int getFluidAmount(int idx) {
		if(fluids[idx] == null) { return 0; }
		else { return fluids[idx].amount; }
		
	}
	
	protected Fluid getFluidType(int idx) {
		if(fluids[idx] == null) { return null; }
		else { return fluids[idx].getFluid(); }
	}
	
	protected boolean canAddToStack(int idx, FluidStack incoming) {
		if(idx < 0 || idx >= fluids.length) { return false; }
		else if(fluids[idx] == null) { return true; }
		return fluids[idx].isFluidEqual(incoming);
	}
	
	protected int addFluidToStack(int idx, int fluidAmount) {
		if(fluids[idx].getFluid() == null) {
			throw new IllegalArgumentException("Cannot add fluid with only an integer when tank is empty!");
		}
		
		int amtToAdd = Math.min(fluidAmount, getCapacity() - getTotalAmount());
		
		fluids[idx].amount += amtToAdd;
		return Math.max(0, fluidAmount - amtToAdd);
		
	}
	
	protected FluidStack addFluidToStack(int idx, FluidStack incoming) {
		if(!canAddToStack(idx, incoming)) { return incoming; }

		int amtToAdd = Math.min(incoming.amount, getCapacity() - getTotalAmount());

		if(amtToAdd <= 0) { 
			return incoming;
		}
		else if(amtToAdd >= incoming.amount) {
			if(fluids[idx] == null) {
				fluids[idx] = incoming;
			}
			else {
				fluids[idx].amount += incoming.amount;
			}
			return null;
		}
		else {
			if(fluids[idx] == null) {
				fluids[idx] = incoming.copy();
				fluids[idx].amount = 0;
			}

			fluids[idx].amount += amtToAdd;
			incoming.amount -= amtToAdd;
			return incoming;
		}
	}
	
	/**
	 * Drain some fluid from a given stack
	 * @param idx Index of the stack (FUEL or WASTE)
	 * @param amount Nominal amount to drain
	 * @return Amount actually drained
	 */
	protected int drainFluidFromStack(int idx, Fluid fluid, int amount) {
		if(fluids[idx] == null) { return 0; }
		
		if(fluids[idx].getFluid().getID() != fluid.getID()) { return 0; }

		return drainFluidFromStack(idx, amount);
	}
	
	/**
	 * Drain fluid from a given stack, without caring what type it is.
	 * @param idx Index of the stack
	 * @param amount Amount to drain
	 * @return
	 */
	protected int drainFluidFromStack(int idx, int amount) {
		if(fluids[idx] == null) { return 0; }

		if(fluids[idx].amount <= amount) {
			amount = fluids[idx].amount;
			fluids[idx] = null;
		}
		else {
			fluids[idx].amount -= amount;
		}
		return amount;
	}
	
	protected void clampContentsToCapacity() {
		if(getTotalAmount() > capacity) {
			int diff = getTotalAmount() - capacity;
			
			// Reduce stuff in the tanks. Start with waste, to be nice to players.
			for(int i = fluids.length - 1; i >= 0 && diff > 0; i--) {
				if(fluids[i] != null) {
					if(diff > fluids[i].amount) {
						diff -= fluids[i].amount;
						fluids[i] = null;
					}
					else {
						fluids[i].amount -= diff;
						diff = 0;
					}
				}
			}
		}
		// Else: nothing to do, no need to clamp
	}
	
	protected void resetLastSeenFluidLevels() {
		for(int i = 0; i < numberOfFluids; i++) {
			if(fluids[i] == null) {
				fluidLevelAtLastUpdate[i] = 0;
			}
			else {
				fluidLevelAtLastUpdate[i] = fluids[i].amount;
			}
		}
	}
}

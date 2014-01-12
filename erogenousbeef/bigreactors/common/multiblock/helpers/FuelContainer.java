package erogenousbeef.bigreactors.common.multiblock.helpers;

import cpw.mods.fml.common.FMLLog;
import erogenousbeef.bigreactors.common.BRRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

/**
 * Class to help with fuel/waste tracking in reactors.
 * For now, 
 * @author ErogenousBeef
 *
 */
public class FuelContainer {
	private static final int FUEL = 0;
	private static final int WASTE = 1;
	
	// TODO: Expand this so it can accept multiple types of fuel and waste
	private FluidStack[] fluids;
	private int capacity;
	
	public FuelContainer() {
		fluids = new FluidStack[2];
		capacity = 1000;
	}
	
	public static boolean isAcceptedFuel(Fluid fuelType) {
		return BRRegistry.getDataForFuel(fuelType) != null;
	}
	
	public static boolean isAcceptedWaste(Fluid wasteType) {
		return BRRegistry.getDataForWaste(wasteType) != null;
	}
	
	public int getCapacity() { return capacity; }
	
	public void setCapacity(int newCapacity) {
		int oldCapacity = capacity;
		capacity = newCapacity;

		if(oldCapacity > capacity) {
			clampContentsToCapacity();
		}
	}
	
	public int getFuelAmount() {
		if(fluids[FUEL] == null) { return 0; }
		else { return fluids[FUEL].amount; }
	}
	
	public int getWasteAmount() {
		if(fluids[WASTE] == null) { return 0; }
		else { return fluids[WASTE].amount; }
	}
	
	/**
	 * @return Total amount of stuff contained, fuel + waste.
	 */
	public int getTotalAmount() { return getFuelAmount() + getWasteAmount(); }
	
	/**
	 * Is it possible to add this fuel to the container, at all? Does not account for the container being full.
	 * @param incoming The fuel to add.
	 * @return True if the fuel can be added to the container.
	 */
	public boolean canAddFuel(FluidStack incoming) {
		return canAddToStack(FUEL, incoming);
	}

	/**
	 * Is it possible to add this Waste to the container, at all? Does not account for the container being full.
	 * @param incoming The waste to add.
	 * @return True if the waste can be added to the container.
	 */
	public boolean canAddWaste(FluidStack incoming) {
		return canAddToStack(WASTE, incoming);
	}
	
	/**
	 * Add some fuel to the current pile, if possible.
	 * @return An ItemStack containing the remaining un-accepted fuel, or null if the stack was completely used.
	 */
	public FluidStack addFuel(FluidStack incoming) {
		if(incoming == null) { return null; }
		return addFluidToStack(FUEL, incoming);
	}
	
	/**
	 * Add some waste to the current pile, if possible.
	 * @return An ItemStack containing the remaining un-accepted waste, or null if the stack was completely used.
	 */
	public FluidStack addWaste(FluidStack incoming) {
		if(incoming == null) { return null; }
		
		return addFluidToStack(WASTE, incoming);
	}
	
	public int drainFuel(int amount) {
		return drainFluidFromStack(FUEL, amount);
	}
	
	public int drainFuel(Fluid fuel, int amount) {
		return drainFluidFromStack(FUEL, fuel, amount);
	}
	
	public int drainWaste(int amount) {
		return drainFluidFromStack(WASTE, amount);
	}
	
	public int drainWaste(Fluid waste, int amount) {
		return drainFluidFromStack(WASTE, waste, amount);
	}
	
	public Fluid getFuelType() {
		return getFluidType(FUEL);
	}
	
	public Fluid getWasteType() {
		return getFluidType(WASTE);
	}
	
	private Fluid getFluidType(int idx) {
		if(fluids[idx] == null) { return null; }
		else { return fluids[idx].getFluid(); }
	}
	
	public boolean isFull() {
		return getTotalAmount() >= getCapacity();
	}
	
	public boolean isEmpty() {
		return getTotalAmount() <= 0;
	}
	
	public NBTTagCompound writeToNBT(NBTTagCompound destination) {
		if(fluids[FUEL] != null) {
			destination.setCompoundTag("fuel", fluids[FUEL].writeToNBT(new NBTTagCompound()));
		}
		
		if(fluids[WASTE] != null) {
			destination.setCompoundTag("waste", fluids[WASTE].writeToNBT(new NBTTagCompound()));
		}
		
		destination.setInteger("capacity", capacity);
		return destination;
	}
	
	public void readFromNBT(NBTTagCompound data) {
		if(data.hasKey("fuel")) {
			fluids[FUEL] = FluidStack.loadFluidStackFromNBT(data.getCompoundTag("fuel"));
		}
		else {
			fluids[FUEL] = null;			
		}
		
		if(data.hasKey("waste")) {
			fluids[WASTE] = FluidStack.loadFluidStackFromNBT(data.getCompoundTag("waste"));
		}
		else {
			fluids[WASTE] = null;
		}
		
		if(data.hasKey("capacity")) {
			capacity = data.getInteger("capacity");
		}
		else {
			setCapacity(1000);
		}
	}
	
	public void emptyFuel() {
		fluids[FUEL] = null;
	}
	
	public void emptyWaste() {
		fluids[WASTE] = null;
	}
	
	public void setFuel(FluidStack newFuel) {
		fluids[FUEL] = newFuel;
	}
	
	public void setWaste(FluidStack newWaste) {
		fluids[WASTE] = newWaste;
	}
	
	public void merge(FuelContainer other) {
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
		
		clampContentsToCapacity();
	}

	////// PRIVATE HELPERS //////
	
	private boolean canAddToStack(int idx, FluidStack incoming) {
		if(idx < 0 || idx >= fluids.length) { return false; }
		else if(fluids[idx] == null) { return true; }
		return fluids[idx].isFluidEqual(incoming);
	}
	
	private FluidStack addFluidToStack(int idx, FluidStack incoming) {
		if(!canAddToStack(idx, incoming)) { return incoming; }

		int amtToAdd = Math.min(incoming.amount, getCapacity() - getTotalAmount());

		if(amtToAdd <= 0) { 
			return incoming;
		}
		else if(amtToAdd >= incoming.amount) {
			fluids[idx] = incoming;
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
	private int drainFluidFromStack(int idx, Fluid fluid, int amount) {
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
	private int drainFluidFromStack(int idx, int amount) {
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
	
	public void clampContentsToCapacity() {
		if(getTotalAmount() > capacity) {
			int diff = getTotalAmount() - capacity;
			
			// Reduce stuff in the tanks. Start with waste, to be nice to players.
			for(int i = WASTE; i >= 0 && diff > 0; i--) {
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
}

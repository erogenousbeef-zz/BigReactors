package erogenousbeef.bigreactors.common.multiblock.helpers;

import cpw.mods.fml.common.FMLLog;
import erogenousbeef.bigreactors.api.IReactorFuel;
import erogenousbeef.bigreactors.common.BRRegistry;
import erogenousbeef.bigreactors.common.BigReactors;
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
	
	private int ticksSinceLastUpdate;
	private static final int minimumTicksBetweenUpdates = 60;
	private static final int minimumDevianceForUpdate = 50; // at least 50mB difference before we send a fueling update to the client

	int[] fluidLevelAtLastUpdate;

	private static final int NumberOfFluids = 2;
	private static final int FORCE_UPDATE = -1000;
	
	private float radiationFuelUsage;
	
	public FuelContainer() {
		fluids = new FluidStack[NumberOfFluids];
		fluidLevelAtLastUpdate = new int[NumberOfFluids];

		for(int i = 0; i < NumberOfFluids; i++) {
			fluids[i] = null;
			fluidLevelAtLastUpdate[i] = FORCE_UPDATE;
		}

		capacity = 1000;
		radiationFuelUsage = 0f;
	}
	
	public boolean shouldSendFuelingUpdate() {
		ticksSinceLastUpdate++;
		if(minimumTicksBetweenUpdates < ticksSinceLastUpdate) {
			int dev = 0;
			boolean shouldUpdate = false;
			for(int i = 0; i < NumberOfFluids && !shouldUpdate; i++) {
				
				if(fluids[i] == null && fluidLevelAtLastUpdate[i] > 0) {
					shouldUpdate = true;
				}
				else if(fluids[i] != null) {
					if(fluidLevelAtLastUpdate[i] == FORCE_UPDATE) {
						shouldUpdate = true;
					}
					else {
						int tmp = Math.abs(fluids[i].amount - fluidLevelAtLastUpdate[i]);
						dev += tmp;
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
	
	private void addWaste(int wasteAmt) {
		if(this.getWasteType() == null) {
			FMLLog.warning("System is using addWaste(int) when there's no waste present, defaulting to cyanite");
			addFluidToStack(WASTE, new FluidStack(BigReactors.fluidCyanite, wasteAmt));
		}
		else {
			addFluidToStack(WASTE, wasteAmt);
		}
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
		
		destination.setFloat("fuelUsage", radiationFuelUsage);
		return destination;
	}
	
	public void readFromNBT(NBTTagCompound data) {
		if(data.hasKey("fuel")) {
			fluids[FUEL] = FluidStack.loadFluidStackFromNBT(data.getCompoundTag("fuel"));
			fluidLevelAtLastUpdate[FUEL] = fluids[FUEL].amount;
		}
		else {
			fluids[FUEL] = null;			
			fluidLevelAtLastUpdate[FUEL] = 0;
		}
		
		if(data.hasKey("waste")) {
			fluids[WASTE] = FluidStack.loadFluidStackFromNBT(data.getCompoundTag("waste"));
			fluidLevelAtLastUpdate[WASTE] = fluids[WASTE].amount;
		}
		else {
			fluids[WASTE] = null;
			fluidLevelAtLastUpdate[WASTE] = 0;
		}
		
		if(data.hasKey("fuelUsage")) {
			radiationFuelUsage = data.getFloat("fuelUsage");
		}
		else {
			radiationFuelUsage = 0f;
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
		radiationFuelUsage = Math.max(radiationFuelUsage, other.radiationFuelUsage);
		
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
	
	public void onRadiationUsesFuel(float fuelUsed) {
		if(Float.isInfinite(fuelUsed) || Float.isNaN(fuelUsed)) { return; }
		
		radiationFuelUsage += fuelUsed;
		
		if(radiationFuelUsage < 1f) {
			return;
		}

		int fuelToConvert = Math.min(getFuelAmount(), (int)radiationFuelUsage);

		if(fuelToConvert <= 0) { return; }
		
		radiationFuelUsage = Math.max(0f, radiationFuelUsage - fuelToConvert);

		Fluid fuelType = getFuelType();
		if(fuelType != null) {
			this.drainFuel(fuelToConvert);
			
			if(getWasteType() != null) {
				// If there's already waste, just keep on producing the same type.
				// TODO: Allow fuel miscegenation?
				this.addWaste(fuelToConvert);
			}
			else {
				// Create waste type from registry
				IReactorFuel fuelData = BRRegistry.getDataForFuel(fuelType);
				Fluid wasteFluid = null;
				if(fuelData != null) {
					wasteFluid = fuelData.getProductFluid();
				}
				
				if(wasteFluid == null) {
					FMLLog.warning("Unable to locate waste for fuel type " + fuelType.getName() + "; using cyanite instead");
					wasteFluid = BigReactors.fluidCyanite;
				}
				
				this.addWaste(new FluidStack(wasteFluid, fuelToConvert));
			}
		}
		else {
			FMLLog.warning("Attempting to use %d fuel and there's no fuel in the tank", fuelToConvert);
		}
	}

	////// PRIVATE HELPERS //////
	
	private boolean canAddToStack(int idx, FluidStack incoming) {
		if(idx < 0 || idx >= fluids.length) { return false; }
		else if(fluids[idx] == null) { return true; }
		return fluids[idx].isFluidEqual(incoming);
	}
	
	private int addFluidToStack(int idx, int fluidAmount) {
		if(fluids[idx].getFluid() == null) {
			throw new IllegalArgumentException("Cannot add fluid with only an integer when tank is empty!");
		}
		
		int amtToAdd = Math.min(fluidAmount, getCapacity() - getTotalAmount());
		
		fluids[idx].amount += amtToAdd;
		return Math.max(0, fluidAmount - amtToAdd);
		
	}
	
	private FluidStack addFluidToStack(int idx, FluidStack incoming) {
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
	
	private void resetLastSeenFluidLevels() {
		for(int i = 0; i < NumberOfFluids; i++) {
			if(fluids[i] == null) {
				fluidLevelAtLastUpdate[i] = 0;
			}
			else {
				fluidLevelAtLastUpdate[i] = fluids[i].amount;
			}
		}
	}

	public float getFuelReactivity() {
		// TODO: Fetch this from the fuel itself
		return 1.1f;
	}
}

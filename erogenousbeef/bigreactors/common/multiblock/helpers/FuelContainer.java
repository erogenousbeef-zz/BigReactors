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
public class FuelContainer extends FluidHelper {
	private static final int FUEL = 0;
	private static final int WASTE = 1;
	
	private static final String[] tankNames = { "fuel", "waste" };
	
	private float radiationFuelUsage;
	
	public FuelContainer() {
		super(false);
		radiationFuelUsage = 0f;
	}
	
	public static boolean isAcceptedFuel(Fluid fuelType) {
		return BRRegistry.getDataForFuel(fuelType) != null;
	}
	
	public static boolean isAcceptedWaste(Fluid wasteType) {
		return BRRegistry.getDataForWaste(wasteType) != null;
	}
	
	
	public int getFuelAmount() {
		return getFluidAmount(FUEL);
	}
	
	public int getWasteAmount() {
		return getFluidAmount(WASTE);
	}
	
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
	
	public NBTTagCompound writeToNBT(NBTTagCompound destination) {
		super.writeToNBT(destination);
		
		destination.setFloat("fuelUsage", radiationFuelUsage);
		return destination;
	}
	
	public void readFromNBT(NBTTagCompound data) {
		super.readFromNBT(data);
		
		if(data.hasKey("fuelUsage")) {
			radiationFuelUsage = data.getFloat("fuelUsage");
		}
	}
	
	public void emptyFuel() {
		setFluid(FUEL, null);
	}
	
	public void emptyWaste() {
		setFluid(WASTE, null);
	}
	
	public void setFuel(FluidStack newFuel) {
		setFluid(FUEL, newFuel);
	}
	
	public void setWaste(FluidStack newWaste) {
		setFluid(WASTE, newWaste);
	}
	
	public void merge(FuelContainer other) {
		radiationFuelUsage = Math.max(radiationFuelUsage, other.radiationFuelUsage);
		
		super.merge(other);
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

	public float getFuelReactivity() {
		// TODO: Fetch this from the fuel itself
		return 1.1f;
	}

	@Override
	public int getNumberOfFluidTanks() {
		return 2;
	}

	@Override
	protected String[] getNBTTankNames() {
		return tankNames;
	}
}

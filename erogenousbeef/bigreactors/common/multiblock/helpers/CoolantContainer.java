package erogenousbeef.bigreactors.common.multiblock.helpers;

import cpw.mods.fml.common.FMLLog;
import erogenousbeef.bigreactors.common.BigReactors;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

public class CoolantContainer extends FluidHelper {

	public static final int HOT = 0;
	public static final int COLD = 1;

	private static final String[] tankNames = { "hot", "cold" };
	
	public CoolantContainer() {
		super(true);
	}

	@Override
	public int getNumberOfFluidTanks() {
		return 2;
	}

	@Override
	protected String[] getNBTTankNames() {
		return tankNames;
	}
	
	public boolean isAcceptedCoolant(Fluid fluid) {
		if(fluid == null) { return false; }
		
		// TODO: Lookup
		return fluid.getID() == FluidRegistry.WATER.getID();
	}

	public int addCoolant(FluidStack incoming) {
		if(incoming == null) { return 0; }
		return fill(COLD, incoming, true);
	}
	
	public int drainCoolant(int amount) {
		return drainFluidFromStack(COLD, amount);
	}
	
	public Fluid getCoolantType() {
		return getFluidType(COLD);
	}
	
	public int getCoolantAmount() {
		return getFluidAmount(COLD);
	}
	
	public Fluid getVaporType() {
		return getFluidType(HOT);
	}
	
	public int getVaporAmount() {
		return getFluidAmount(HOT);
	}
	
	public NBTTagCompound writeToNBT(NBTTagCompound destination) {
		super.writeToNBT(destination);

		return destination;
	}
	
	public void readFromNBT(NBTTagCompound data) {
		super.readFromNBT(data);
	}
	
	public void emptyCoolant() {
		setFluid(COLD, null);
	}
	
	public void emptyVapor() {
		setFluid(HOT, null);
	}
	
	public void setCoolant(FluidStack newFuel) {
		setFluid(COLD, newFuel);
	}
	
	public void setVapor(FluidStack newWaste) {
		setFluid(HOT, newWaste);
	}
	
	public void merge(CoolantContainer other) {
		super.merge(other);
	}
	
	public float getCoolantTemperature(float reactorTemperature) {
		if(getFluidAmount(COLD) <= 0 || getCoolantType() == null) { return reactorTemperature; }

		return Math.min(reactorTemperature, getBoilingPoint(getCoolantType()));
	}
	
	/**
	 * Attempt to transfer some heat (in RF) into the coolant system.
	 * This method assumes you've already checked the coolant temperature, above,
	 * and scaled the energy absorbed into the coolant based on surface area.
	 * 
	 * @param rfAbsorbed RF to transfer into the coolant system.
	 * @return RF remaining after absorption.
	 */
	public float onAbsorbHeat(float rfAbsorbed) {
		if(getFluidAmount(COLD) <= 0 || rfAbsorbed <= 0) { return rfAbsorbed; }

		Fluid coolantType = getCoolantType();
		int coolantAmt = getFluidAmount(COLD);
		int vaporAmt = getFluidAmount(HOT);

		float heatOfVaporization = getHeatOfVaporization(coolantType);
		
		int mbVaporized = Math.min(coolantAmt, (int)(rfAbsorbed / heatOfVaporization));

		// Cap by the available space in the vapor chamber
		mbVaporized = Math.min(mbVaporized, getRemainingSpaceForFluid(HOT));
		
		// We don't do partial vaporization. Just return all the heat.
		if(mbVaporized < 1) { return rfAbsorbed; }

		// Make sure we either have an empty vapor chamber or the vapor types match
		Fluid newVaporType = getVaporizedCoolantFluid(coolantType);
		if(newVaporType == null) {
			FMLLog.warning("Coolant in tank (%s) has no registered vapor type!", coolantType.getName());
			return rfAbsorbed;
		}
		
		Fluid existingVaporType = getVaporType();
		if(existingVaporType != null && getVaporType().getID() != existingVaporType.getID()) {
			// Can't vaporize anything with incompatible vapor in the vapor tank
			return rfAbsorbed;
		}
		
		// Vaporize! -- POINT OF NO RETURN
		this.drainCoolant(mbVaporized);
		
		if(existingVaporType != null) {
			addFluidToStack(HOT, mbVaporized);
		}
		else {
			fill(HOT, new FluidStack(newVaporType, mbVaporized), true);
		}
		
		// Calculate how much we actually absorbed via vaporization
		float energyConsumed = (float)mbVaporized * heatOfVaporization;
		
		// And return energy remaining after absorption
		return Math.max(0f, rfAbsorbed - energyConsumed);
	}
	
	private float getBoilingPoint(Fluid fluid) {
		if(fluid == null) { throw new IllegalArgumentException("Cannot pass a null fluid to getBoilingPoint"); } // just in case

		// TODO: Lookup
		return 100f;
	}
	
	/**
	 * Returns the amount of heat (in RF) needed to convert 1mB of liquid into 1mB of vapor. 
	 * @return
	 */
	private float getHeatOfVaporization(Fluid fluid) {
		if(fluid == null) { throw new IllegalArgumentException("Cannot pass a null fluid to getHeatOfVaporization"); } // just in case

		// TODO: Make a registry for this, do a lookup
		return 4f; // TE converts 1mB steam into 2 RF of work in a steam turbine, so we assume it's 50% efficient.
	}
	
	private Fluid getVaporizedCoolantFluid(Fluid fluid) {
		if(fluid == null) { throw new IllegalArgumentException("Cannot pass a null fluid to getVaporizedCoolantFluid"); } // just in case

		return BigReactors.fluidSteam;
	}

	@Override
	protected boolean isFluidValidForStack(int stackIdx, Fluid fluid) {
		switch(stackIdx) {
		case COLD:
			return isAcceptedCoolant(fluid);
		case HOT:
			return true;
		default:
			return false;
		}
	}
}

package erogenousbeef.bigreactors.common.multiblock.helpers;

import erogenousbeef.bigreactors.common.BigReactors;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

public class CoolantContainer extends FluidHelper {

	private static final int HOT = 0;
	private static final int COLD = 1;

	private static final String[] tankNames = { "hot", "cold" };
	
	public CoolantContainer() {
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

	protected Fluid getVaporForCoolant(Fluid fluid) {
		// TODO: Lookup
		return BigReactors.fluidSteam;
	}
	
	public boolean canAddCoolant(FluidStack incoming) {
		return canAddToStack(COLD, incoming);
	}
	
	public FluidStack addCoolant(FluidStack incoming) {
		if(incoming == null) { return null; }
		return addFluidToStack(COLD, incoming);
	}
	
	public int drainCoolant(int amount) {
		return drainFluidFromStack(COLD, amount);
	}
	
	public int drainCoolant(Fluid coolant, int amount) {
		return drainFluidFromStack(COLD, coolant, amount);
	}

	public int drainVapor(int amount) {
		return drainFluidFromStack(HOT, amount);
	}
	
	public int drainVapor(Fluid vapor, int amount) {
		return drainFluidFromStack(HOT, vapor, amount);
	}
	
	public Fluid getCoolantType() {
		return getFluidType(COLD);
	}
	
	public Fluid getVaporType() {
		return getFluidType(HOT);
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
	
	/**
	 * Attempt to transfer some heat (in RF) into the coolant system.
	 * @param rfAbsorbed RF to transfer into the coolant system.
	 * @return RF remaining after absorption.
	 */
	public float onAbsorbHeat(float rfAbsorbed) {
		// TODO: Absorb as much heat as possible
		return rfAbsorbed;
	}
}

package erogenousbeef.bigreactors.common.multiblock.helpers;

import erogenousbeef.bigreactors.api.data.FluidStateData;
import erogenousbeef.bigreactors.api.registry.FluidStates;
import erogenousbeef.bigreactors.common.BRLog;
import erogenousbeef.bigreactors.utils.StaticUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

public class CondenserContainer extends FluidHelper {

	public static final int HOT = 0;
	public static final int COLD = 1;
	
	private final static String[] TANK_NAMES = { "hot", "cold" };
	private final static int NUM_TANKS = 2;
	
	private int m_CondensedLastTick = 0; // Not persisted: reporting only
	private float m_RfTransferCoefficient = 0f; // Not persisted: Set on assembly
	
	public CondenserContainer() {
		super(true);
		m_CondensedLastTick = 0;
	}
	
	public void setRfTransferCoefficient(float coeff) {
		m_RfTransferCoefficient = Math.max(0f, coeff);
	}
	
	public int getFluidCondensedLastTick() {
		return m_CondensedLastTick;
	}
	
	public void setFluidCondensedLastTick(int fluidAmt) {
		m_CondensedLastTick = fluidAmt;
	}
	
	@Override
	public int getNumberOfFluidTanks() {
		return NUM_TANKS;
	}

	@Override
	protected String[] getNBTTankNames() {
		return TANK_NAMES;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound destination) {
		super.writeToNBT(destination);
		return destination;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound data) {
		super.readFromNBT(data);
	}
	
	public void merge(CondenserContainer other) {
		super.merge(other);
	}
	
	/**
	 * Performs condensation logic.
	 * 
	 * Condenses fluid from the HOT chamber to the COLD chamber, dependent
	 * on how much RF can be absorbed by an RF buffer.
	 * 
	 * Will modify the ThermalHelper argument by adding RF.
	 * 
	 * @param helper A ThermalHelper denoting the conditions of the thermal buffer receiving heat energy from condensation.
	 * @return The amount of RF transferred into the thermal buffer.
	 */
	public float condense(ThermalHelper helper) {
		int hotFluidAmt = getFluidAmount(HOT);
		int condensableFluid = Math.min(getCapacity() - getFluidAmount(COLD), hotFluidAmt);
		if(condensableFluid <= 0) { return 0f; } // Cold tank is full or hot tank is empty
		
		Fluid hotFluid = getFluidType(HOT);
		if(hotFluid == null) { return 0f; }
		
		FluidStateData fsd = FluidStates.get(hotFluid);
		if(fsd == null) {
			BRLog.warning("Unknown fluid for condensing: %s", hotFluid.getName());
			return 0f;
		}

		if(!StaticUtils.Fluids.areFluidsEqual(fsd.getGas(), hotFluid)) {
			BRLog.warning("Fluid data not available for condensing %s", hotFluid.getName());
			return 0f;
		}
		
		Fluid coldFluid = getFluidType(COLD);
		if(coldFluid != null || !StaticUtils.Fluids.areFluidsEqual(fsd.getLiquid(), coldFluid)) {
			// Cannot condense - cold fluid tank cannot accept this gas's condensate
			return 0f;
		}
		
		// OK, we now know what to condense this stuff into, and how much we can condense.
		float hotFluidTemp = fsd.getBoilingPoint();
		float deltaTemp = hotFluidTemp - helper.getTemp();
		if(deltaTemp <= 0f) { return 0f; } // Cannot transfer into a thermal buffer hotter than we are

		// Calculate optimals, as if we were dealing with floating-point fluids
		float maximumTransferRfT = deltaTemp * m_RfTransferCoefficient;
		float availableRfT  = fsd.getCondensationRf(condensableFluid); 
		float transferRfT = Math.min(availableRfT, maximumTransferRfT);
		
		// Finally, clamp by the amount of fluid we'll actually condense, since we have discrete fluids
		// TODO: Handle expansion coefficients.
		int unitsToCondense = (int)(transferRfT / fsd.getHeatOfVaporization());
		transferRfT = fsd.getCondensationRf(unitsToCondense);
		
		if(unitsToCondense <= 0) { return 0f; }
		
		// POINT OF NO RETURN: Transfer heat, convert fluid
		drain(HOT, unitsToCondense, true);
		fill(COLD, new FluidStack(coldFluid, unitsToCondense), true);
		helper.setRf(helper.getRf() + transferRfT);
		m_CondensedLastTick = unitsToCondense;
		return transferRfT;
	}
	
	@Override
	protected boolean isFluidValidForStack(int stackIdx, Fluid fluid) {
		if(stackIdx == HOT) {
			FluidStateData fsd = FluidStates.get(fluid);
			return fsd != null && StaticUtils.Fluids.areFluidsEqual(fluid, fsd.getGas());
		}
		else if(stackIdx == COLD) {
			return true;
		}
		else {
			return false;
		}
	}

}

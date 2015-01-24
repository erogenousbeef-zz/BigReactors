package erogenousbeef.bigreactors.common.multiblock.helpers;

import erogenousbeef.bigreactors.api.registry.SteamRegistry;
import erogenousbeef.bigreactors.common.BRLog;
import erogenousbeef.bigreactors.common.data.StandardFluids;
import erogenousbeef.bigreactors.utils.StaticUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

public class SteamEvapContainer extends FluidHelper {

	public static final int STEAM = 0;
	public static final int WATER = 1;

	protected static final String[] TANK_NAMES = {"steam", "water"}; 

	protected int m_VaporRemainder;
	protected int m_SteamMode;
	protected float m_RfTransferCoefficient;
	protected boolean m_Dirty;
	protected int m_SteamProducedLastTick;
	
	public SteamEvapContainer() {
		super(true);
		m_VaporRemainder = 0;
		m_SteamMode = 0;
		m_RfTransferCoefficient = 0f;
		m_Dirty = false;
		m_SteamProducedLastTick = 0;
	}
	
	public void setRfTransferCoefficient(float coeff) {
		m_RfTransferCoefficient = Math.max(0f, coeff);
	}

	@Override
	public int getNumberOfFluidTanks() {
		return TANK_NAMES.length;
	}

	@Override
	protected String[] getNBTTankNames() {
		return TANK_NAMES;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound destination) {
		super.writeToNBT(destination);
		destination.setInteger("remainder", m_VaporRemainder);
		destination.setInteger("mode", m_SteamMode);
		return destination;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound data) {
		super.readFromNBT(data);
		if(data.hasKey("remainder")) {
			m_VaporRemainder = data.getInteger("remainder");
		}
		
		if(data.hasKey("mode")) {
			setMode(data.getInteger("mode"), false);
		}
	}

	@Override
	protected boolean isFluidValidForStack(int stackIdx, Fluid fluid) {
		if(stackIdx == WATER) {
			return "water".equals(fluid.getName());
		}
		else if(stackIdx == STEAM) {
			return fluid.getName().startsWith("steam");
		}
		return false;
	}
	
	public void setSteamMode(int newMode) {
		setMode(newMode, true);
	}
	
	protected void setMode(int newMode, boolean setDirty) {
		m_SteamMode = newMode;

		if(setDirty) {
			m_Dirty = true;
		}
	}
	
	public boolean shouldSave() {
		return m_Dirty;
	}
	
	public void onSave() {
		m_Dirty = false;
	}
	
	public void merge(SteamEvapContainer other) {
		super.merge(other);
		m_SteamMode = Math.max(m_SteamMode, other.m_SteamMode);
		m_VaporRemainder = Math.max(m_VaporRemainder, m_VaporRemainder);
		m_Dirty = true;
	}
	
	public int getSteamProducedLastTick() { return m_SteamProducedLastTick; }
	public void setSteamProducedLastTick(int produced) { m_SteamProducedLastTick = produced; }
	
	/**
	 * Attempt to evaporate water into a given steam type. If no full units (1mB) of steam
	 * can be created at the current temperature, then no steam will be produced this tick.
	 * 
	 * @param helper ThermalHelper representing the thermal state of the heat exchanger
	 * @return The amount of RF transferred. ThermalHelper will also be modified.
	 */
	public float evaporate(ThermalHelper helper) {
		// Do we have water, and space to boil it into?
		int waterAmt = getFluidAmount(WATER);
		int spaceAvailable = getCapacity() - getFluidAmount(STEAM);
		waterAmt = Math.min(waterAmt, spaceAvailable);
		if(waterAmt <= 0 || spaceAvailable <= 0) { return 0f; }
		
		float deltaTemp = helper.getTemp() - StandardFluids.BoilingPoint_Water;

		// If we're not above the boiling point of water, ignore.
		if(deltaTemp <= 0f) { return 0f; }
		
		Fluid steamFluid = getFluidType(STEAM);
		if(steamFluid == null) {
			steamFluid = SteamRegistry.get(m_SteamMode);
		}
		
		// How many steam units can we possibly create, given space & water restrictions?
		// Add in m_VaporRemainder when dealing with water, to carry over any additional units that could
		// be created from prior vaporizations.
		int potentialSteamUnits = Math.min(waterAmt * StandardFluids.Expansion_Water + m_VaporRemainder, spaceAvailable);
		if(potentialSteamUnits <= 0) { return 0f; }

		// Now figure out how much RF we should transfer over
		float rfPerSteamUnit = SteamRegistry.getBaseRfAmountForFluid(steamFluid);
		float rfAbsorption = rfPerSteamUnit * potentialSteamUnits; // First, calculate maximum potential
		rfAbsorption = Math.min(deltaTemp * m_RfTransferCoefficient, rfAbsorption); // Cap based on maximum transfer rate
		rfAbsorption = Math.min(rfAbsorption, helper.getRf());
		
		if(rfAbsorption <= 0f) { return 0f; }

		// How much steam do we need to create to carry off the absorbed rf?
		int steamUnitsToCreate = (int)(rfAbsorption / rfPerSteamUnit);
		if(steamUnitsToCreate <= 0) { return 0f; } // Can't create any steam this time around, wait until conditions improve.
		
		int waterUnitsToUse;
		if(m_VaporRemainder >= steamUnitsToCreate) {
			waterUnitsToUse = 0;
		}
		else {
			// Always use at least one unit of water, if we exhaust the remainder
			waterUnitsToUse = Math.max(1, (int) Math.ceil((steamUnitsToCreate - m_VaporRemainder) / StandardFluids.Expansion_Water));
		}

		// Cap steam created based on the amount of water + remainder available, if need be
		if(waterUnitsToUse > waterAmt) {
			waterUnitsToUse = waterAmt;
			steamUnitsToCreate = m_VaporRemainder + (waterUnitsToUse * StandardFluids.Expansion_Water);
		}
		
		// Do the actual drain/fill
		drain(WATER, waterUnitsToUse, true);
		m_SteamProducedLastTick = fill(STEAM, new FluidStack(steamFluid, steamUnitsToCreate), true);
	
		// Slurp RF out of the thermal buffer
		rfAbsorption = steamUnitsToCreate * rfPerSteamUnit; // Recalculate RF absorbed based on steam actually created
		helper.setRf(helper.getRf() - rfAbsorption);
		
		// Now figure out how much remainder steam we have from the water vaporized
		m_VaporRemainder += waterUnitsToUse * StandardFluids.Expansion_Water;
		m_VaporRemainder -= steamUnitsToCreate;
		
		if(m_VaporRemainder < 0) {
			// OOPS?!
			BRLog.error("m_VaporRemainder is less than zero! This is a math error! (value: %d)", m_VaporRemainder);
			m_VaporRemainder = 0;
		} 
		
		return rfAbsorption;
	}
}

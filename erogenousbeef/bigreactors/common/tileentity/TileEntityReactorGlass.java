package erogenousbeef.bigreactors.common.tileentity;

import erogenousbeef.bigreactors.api.HeatPulse;
import erogenousbeef.bigreactors.api.IHeatEntity;
import erogenousbeef.bigreactors.api.IRadiationModerator;
import erogenousbeef.bigreactors.api.IRadiationPulse;
import erogenousbeef.bigreactors.common.multiblock.MultiblockReactor;
import erogenousbeef.core.multiblock.MultiblockControllerBase;
import erogenousbeef.core.multiblock.MultiblockTileEntityBase;

public class TileEntityReactorGlass extends MultiblockTileEntityBase implements IRadiationModerator, IHeatEntity {

	@Override
	public MultiblockControllerBase getNewMultiblockControllerObject() {
		return new MultiblockReactor(this.worldObj);
	}

	@Override
	public boolean isGoodForFrame() {
		return false;
	}

	@Override
	public boolean isGoodForSides() {
		return true;
	}

	@Override
	public boolean isGoodForTop() {
		return true;
	}

	@Override
	public boolean isGoodForBottom() {
		return true;
	}

	@Override
	public boolean isGoodForInterior() {
		return false;
	}

	@Override
	public void onMachineAssembled() {
	}

	@Override
	public void onMachineBroken() {
	}

	@Override
	public void onMachineActivated() {
	}

	@Override
	public void onMachineDeactivated() {
	}

	@Override
	public void receiveRadiationPulse(IRadiationPulse radiation) {
		double newHeat = radiation.getSlowRadiation() * 0.25;
		
		// Convert 15% of newly-gained heat to energy (thermocouple or something)
		radiation.addPower(newHeat*0.15);
		newHeat *= 0.85 * 0.5;
		radiation.changeHeat(newHeat);
		
		// Slow radiation is all lost now
		radiation.setSlowRadiation(0);
		
		// And zero out the TTL so evaluation force-stops
		radiation.setTimeToLive(0);
	}

	@Override
	public double getHeat() {
		if(!this.isConnected()) { return 0; }
		return ((MultiblockReactor)getMultiblockController()).getHeat();
	}

	@Override
	public double getThermalConductivity() {
		// Using iron so there's no disadvantage to reactor glass.
		return IHeatEntity.conductivityIron;
	}

	@Override
	public double onAbsorbHeat(IHeatEntity source, HeatPulse pulse, int faces, int contactArea) {
		double deltaTemp = source.getHeat() - getHeat();
		// If the source is cooler than the reactor, then do nothing
		if(deltaTemp <= 0.0) {
			return 0.0;
		}

		double heatToAbsorb = deltaTemp * getThermalConductivity() * (1.0/(double)faces) * contactArea;

		pulse.powerProduced += heatToAbsorb * 0.15;
		pulse.heatChange += heatToAbsorb * 0.85 * 0.5;

		return heatToAbsorb;
	}

	@Override
	public HeatPulse onRadiateHeat(double ambientHeat) {
		// Ignore, glass doesn't re-radiate heat
		return null;
	}
}

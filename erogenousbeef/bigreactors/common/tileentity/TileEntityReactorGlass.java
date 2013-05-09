package erogenousbeef.bigreactors.common.tileentity;

import erogenousbeef.bigreactors.api.IRadiationModerator;
import erogenousbeef.bigreactors.api.IRadiationPacket;
import erogenousbeef.bigreactors.common.multiblock.MultiblockReactor;
import erogenousbeef.core.multiblock.MultiblockControllerBase;
import erogenousbeef.core.multiblock.MultiblockTileEntityBase;

public class TileEntityReactorGlass extends MultiblockTileEntityBase implements IRadiationModerator {

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
	public void receivePulse(IRadiationPacket radiation) {
		if(this.isConnected()) {
			double newCasingHeat = radiation.getSlowRadiation();
			radiation.setSlowRadiation(0);
			radiation.setFastRadiation(0);
			
			// This is a bad assumption.
			// TODO: Fixme when I make a second multiblock machine.
			((MultiblockReactor)getMultiblockController()).addLatentHeat(newCasingHeat);
		}
	}
}

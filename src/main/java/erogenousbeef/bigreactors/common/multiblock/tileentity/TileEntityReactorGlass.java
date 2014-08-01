package erogenousbeef.bigreactors.common.multiblock.tileentity;

import erogenousbeef.core.multiblock.MultiblockValidationException;

public class TileEntityReactorGlass extends TileEntityReactorPartBase {

	@Override
	public void isGoodForFrame()  throws MultiblockValidationException {
		throw new MultiblockValidationException(String.format("%d, %d, %d - Reactor glass may only be used on the exterior faces, not as part of a reactor's frame or interior", xCoord, yCoord, zCoord));
	}

	@Override
	public void isGoodForSides() throws MultiblockValidationException {
	}

	@Override
	public void isGoodForTop() throws MultiblockValidationException {
	}

	@Override
	public void isGoodForBottom() throws MultiblockValidationException {
	}

	@Override
	public void isGoodForInterior() throws MultiblockValidationException {
		throw new MultiblockValidationException(String.format("%d, %d, %d - Reactor glass may only be used on the exterior faces, not as part of a reactor's frame or interior", xCoord, yCoord, zCoord));
	}

	@Override
	public void onMachineActivated() {
	}

	@Override
	public void onMachineDeactivated() {
	}
}

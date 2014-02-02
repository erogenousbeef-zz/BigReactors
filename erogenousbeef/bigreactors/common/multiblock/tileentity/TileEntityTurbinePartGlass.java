package erogenousbeef.bigreactors.common.multiblock.tileentity;

import erogenousbeef.core.multiblock.MultiblockValidationException;

public class TileEntityTurbinePartGlass extends TileEntityTurbinePartBase {

	public TileEntityTurbinePartGlass() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void isGoodForFrame() throws MultiblockValidationException {
		throw new MultiblockValidationException(String.format("%s, %s, %s - Glass cannot be used as part of a turbine's frame", xCoord, yCoord, zCoord));
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
		throw new MultiblockValidationException(String.format("%s, %s, %s - Glass can only be used as part of a turbine's exterior", xCoord, yCoord, zCoord));
	}
}

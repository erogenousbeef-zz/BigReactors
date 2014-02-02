package erogenousbeef.bigreactors.common.multiblock.tileentity;

import erogenousbeef.bigreactors.common.multiblock.block.BlockTurbineRotorPart;
import erogenousbeef.core.multiblock.MultiblockValidationException;

public class TileEntityTurbineRotorPart extends TileEntityTurbinePartBase {

	public TileEntityTurbineRotorPart() {
	}

	@Override
	public void isGoodForFrame() throws MultiblockValidationException {
		throw new MultiblockValidationException("Rotor parts may only be placed in the turbine interior");
	}

	@Override
	public void isGoodForSides() throws MultiblockValidationException {
		throw new MultiblockValidationException("Rotor parts may only be placed in the turbine interior");
	}

	@Override
	public void isGoodForTop() throws MultiblockValidationException {
		throw new MultiblockValidationException("Rotor parts may only be placed in the turbine interior");
	}

	@Override
	public void isGoodForBottom() throws MultiblockValidationException {
		throw new MultiblockValidationException("Rotor parts may only be placed in the turbine interior");
	}

	@Override
	public void isGoodForInterior() throws MultiblockValidationException {
	}

	public boolean isRotorShaft() {
		return BlockTurbineRotorPart.isRotorShaft(getBlockMetadata());
	}

	public boolean isRotorBlade() {
		return BlockTurbineRotorPart.isRotorBlade(getBlockMetadata());
	}
}

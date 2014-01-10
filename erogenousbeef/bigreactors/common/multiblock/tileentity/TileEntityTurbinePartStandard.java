package erogenousbeef.bigreactors.common.multiblock.tileentity;

import erogenousbeef.bigreactors.common.multiblock.block.BlockTurbinePart;
import erogenousbeef.core.multiblock.MultiblockValidationException;

public class TileEntityTurbinePartStandard extends TileEntityTurbinePartBase {

	public TileEntityTurbinePartStandard() {
		super();
	}

	public TileEntityTurbinePartStandard(int metadata) {
		super(metadata);
	}

	@Override
	public void isGoodForFrame() throws MultiblockValidationException {
		if(getMetadata() != BlockTurbinePart.METADATA_HOUSING) {
			throw new MultiblockValidationException(String.format("%d, %d, %d - only turbine housing may be used as part of the turbine's frame", xCoord, yCoord, zCoord));
		}
	}

	@Override
	public void isGoodForSides() {
	}

	@Override
	public void isGoodForTop() {
	}

	@Override
	public void isGoodForBottom() {
	}

	@Override
	public void isGoodForInterior() throws MultiblockValidationException {
		if(getMetadata() != BlockTurbinePart.METADATA_HOUSING) {
			throw new MultiblockValidationException(String.format("%d, %d, %d - this part is not valid for the interior of a turbine", xCoord, yCoord, zCoord));
		}
	}	
}

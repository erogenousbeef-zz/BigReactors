package erogenousbeef.core.multiblock.rectangular;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import erogenousbeef.core.common.CoordTriplet;
import erogenousbeef.core.multiblock.MultiblockControllerBase;
import erogenousbeef.core.multiblock.MultiblockValidationException;

public abstract class RectangularMultiblockControllerBase extends
		MultiblockControllerBase {

	protected RectangularMultiblockControllerBase(World world) {
		super(world);
	}

	/**
	 * @return True if the machine is "whole" and should be assembled. False otherwise.
	 */
	protected void isMachineWhole() throws MultiblockValidationException {
		if(connectedParts.size() < getMinimumNumberOfBlocksForAssembledMachine()) {
			throw new MultiblockValidationException("Machine is too small.");
		}
		
		CoordTriplet maximumCoord = getMaximumCoord();
		CoordTriplet minimumCoord = getMinimumCoord();
		
		// Quickly check for exceeded dimensions
		int deltaX = maximumCoord.x - minimumCoord.x + 1;
		int deltaY = maximumCoord.y - minimumCoord.y + 1;
		int deltaZ = maximumCoord.z - minimumCoord.z + 1;
		
		int maxX = getMaximumXSize();
		int maxY = getMaximumYSize();
		int maxZ = getMaximumZSize();
		int minX = getMinimumXSize();
		int minY = getMinimumYSize();
		int minZ = getMinimumZSize();
		
		if(maxX > 0 && deltaX > maxX) { throw new MultiblockValidationException(String.format("Machine is too large, it may be at most %d blocks in the X dimension", maxX)); }
		if(maxY > 0 && deltaY > maxY) { throw new MultiblockValidationException(String.format("Machine is too large, it may be at most %d blocks in the Y dimension", maxY)); }
		if(maxZ > 0 && deltaZ > maxZ) { throw new MultiblockValidationException(String.format("Machine is too large, it may be at most %d blocks in the Z dimension", maxZ)); }
		if(deltaX < minX) { throw new MultiblockValidationException(String.format("Machine is too small, it must be at least %d blocks in the X dimension", minX)); }
		if(deltaY < minY) { throw new MultiblockValidationException(String.format("Machine is too small, it must be at least %d blocks in the Y dimension", minY)); }
		if(deltaZ < minZ) { throw new MultiblockValidationException(String.format("Machine is too small, it must be at least %d blocks in the Z dimension", minZ)); }

		// Now we run a simple check on each block within that volume.
		// Any block deviating = NO DEAL SIR
		TileEntity te;
		RectangularMultiblockTileEntityBase part;
		for(int x = minimumCoord.x; x <= maximumCoord.x; x++) {
			for(int y = minimumCoord.y; y <= maximumCoord.y; y++) {
				for(int z = minimumCoord.z; z <= maximumCoord.z; z++) {
					// Okay, figure out what sort of block this should be.
					
					te = this.worldObj.getTileEntity(x, y, z);
					if(te instanceof RectangularMultiblockTileEntityBase) {
						part = (RectangularMultiblockTileEntityBase)te;
					}
					else {
						// This is permitted so that we can incorporate certain non-multiblock parts inside interiors
						part = null;
					}
					
					// Validate block type against both part-level and material-level validators.
					int extremes = 0;
					if(x == minimumCoord.x) { extremes++; }
					if(y == minimumCoord.y) { extremes++; }
					if(z == minimumCoord.z) { extremes++; }
					
					if(x == maximumCoord.x) { extremes++; }
					if(y == maximumCoord.y) { extremes++; }
					if(z == maximumCoord.z) { extremes++; }
					
					if(extremes >= 2) {
						if(part != null) {
							part.isGoodForFrame();
						}
						else {
							isBlockGoodForFrame(this.worldObj, x, y, z);
						}
					}
					else if(extremes == 1) {
						if(y == maximumCoord.y) {
							if(part != null) {
								part.isGoodForTop();
							}
							else {
								isBlockGoodForTop(this.worldObj, x, y, z);
							}
						}
						else if(y == minimumCoord.y) {
							if(part != null) {
								part.isGoodForBottom();
							}
							else {
								isBlockGoodForBottom(this.worldObj, x, y, z);
							}
						}
						else {
							// Side
							if(part != null) {
								part.isGoodForSides();
							}
							else {
								isBlockGoodForSides(this.worldObj, x, y, z);
							}
						}
					}
					else {
						if(part != null) {
							part.isGoodForInterior();
						}
						else {
							isBlockGoodForInterior(this.worldObj, x, y, z);
						}
					}
				}
			}
		}
	}	
	
}

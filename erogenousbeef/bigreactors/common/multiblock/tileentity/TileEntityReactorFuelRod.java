package erogenousbeef.bigreactors.common.multiblock.tileentity;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.IFluidBlock;
import erogenousbeef.bigreactors.api.HeatPulse;
import erogenousbeef.bigreactors.api.IHeatEntity;
import erogenousbeef.bigreactors.api.IRadiationModerator;
import erogenousbeef.bigreactors.api.IRadiationPulse;
import erogenousbeef.bigreactors.common.multiblock.MultiblockReactor;
import erogenousbeef.bigreactors.utils.StaticUtils;
import erogenousbeef.core.multiblock.IMultiblockPart;
import erogenousbeef.core.multiblock.MultiblockControllerBase;
import erogenousbeef.core.multiblock.MultiblockTileEntityBase;
import erogenousbeef.core.multiblock.MultiblockValidationException;
import erogenousbeef.core.multiblock.rectangular.IMultiblockPartRectangular;
import erogenousbeef.core.multiblock.rectangular.RectangularMultiblockTileEntityBase;

public class TileEntityReactorFuelRod extends TileEntityReactorPartBase implements IRadiationModerator, IHeatEntity {

	public TileEntityReactorFuelRod() {
		super();
	}
	
	// IRadiationModerator
	@Override
	public void receiveRadiationPulse(IRadiationPulse radiation) {
		/*
		if(this.isAssembled) {
			TileEntity te = this.worldObj.getBlockTileEntity(xCoord, controlRodY, zCoord);
			if(te != null && te instanceof IRadiationModerator) {
				((IRadiationModerator)te).receiveRadiationPulse(radiation);
			}
		}
		*/
	}

	// IHeatEntity
	@Override
	public float getThermalConductivity() {
		return IHeatEntity.conductivityCopper;
	}

	// RectangularMultiblockTileEntityBase
	@Override
	public void isGoodForFrame() throws MultiblockValidationException {
		throw new MultiblockValidationException(String.format("%d, %d, %d - fuel rods may only be placed in the reactor interior", xCoord, yCoord, zCoord));
	}

	@Override
	public void isGoodForSides() throws MultiblockValidationException {
		throw new MultiblockValidationException(String.format("%d, %d, %d - fuel rods may only be placed in the reactor interior", xCoord, yCoord, zCoord));
	}

	@Override
	public void isGoodForTop() throws MultiblockValidationException {
		throw new MultiblockValidationException(String.format("%d, %d, %d - fuel rods may only be placed in the reactor interior", xCoord, yCoord, zCoord));
	}

	@Override
	public void isGoodForBottom() throws MultiblockValidationException {
		throw new MultiblockValidationException(String.format("%d, %d, %d - fuel rods may only be placed in the reactor interior", xCoord, yCoord, zCoord));
	}

	@Override
	public void isGoodForInterior() throws MultiblockValidationException {
		// Check above and below. Above must be fuel rod or control rod.
		TileEntity entityAbove = this.worldObj.getBlockTileEntity(xCoord, yCoord+1, zCoord);
		if(!(entityAbove instanceof TileEntityReactorFuelRod || entityAbove instanceof TileEntityReactorControlRod)) {
			throw new MultiblockValidationException(String.format("Fuel rod at %d, %d, %d must be part of a vertical column that reaches the entire height of the reactor, with a control rod on top.", xCoord, yCoord, zCoord));
		}

		// Below must be fuel rod or the base of the reactor.
		TileEntity entityBelow = this.worldObj.getBlockTileEntity(xCoord, yCoord-1, zCoord);
		if(entityBelow instanceof TileEntityReactorFuelRod) {
			return;
		}
		else if(entityBelow instanceof IMultiblockPartRectangular) {
			((IMultiblockPartRectangular)entityBelow).isGoodForBottom();
			return;
		}
		
		throw new MultiblockValidationException(String.format("Fuel rod at %d, %d, %d must be part of a vertical column that reaches the entire height of the reactor, with a control rod on top.", xCoord, yCoord, zCoord));
	}

	@Override
	public void onMachineActivated() {
	}

	@Override
	public void onMachineDeactivated() {
	}

	// Reactor information retrieval methods
	
	/**
	 * Returns the rate of heat transfer from this block to the reactor environment, based on this block's surrounding blocks.
	 * Note that this method queries the world, so use it sparingly.
	 * 
	 * @return Heat transfer rate from fuel rod to reactor environment, in Centigrade per tick.
	 */
	public float getHeatTransferRate() {
		float heatTransferRate = 0f;

		TileEntity te;
		for(ForgeDirection dir: StaticUtils.CardinalDirections) {
			te = worldObj.getBlockTileEntity(xCoord + dir.offsetX, yCoord + dir.offsetY, zCoord + dir.offsetZ);
			if(te instanceof TileEntityReactorFuelRod) {
				// We don't transfer to other fuel rods, due to heat pooling.
				continue;
			}
			else if(te instanceof IHeatEntity) {
				heatTransferRate += ((IHeatEntity)te).getThermalConductivity();
			}
			else if(worldObj.isAirBlock(xCoord + dir.offsetX, yCoord + dir.offsetY, zCoord + dir.offsetZ)) {
				heatTransferRate += IHeatEntity.conductivityAir;
			}
			else {
				
				int blockID;
				blockID = worldObj.getBlockId(xCoord + dir.offsetX, yCoord + dir.offsetY, zCoord + dir.offsetZ);
				heatTransferRate += getConductivityFromBlockID(blockID);
			}
		}

		return heatTransferRate;
	}
	
	private float getConductivityFromBlockID(int blockID) {
		if(blockID == Block.blockIron.blockID) {
			return IHeatEntity.conductivityIron;
		}
		else if(blockID == Block.blockGold.blockID) {
			return IHeatEntity.conductivityGold;
		}
		else if(blockID == Block.blockDiamond.blockID) {
			return IHeatEntity.conductivityDiamond;
		}
		else if(blockID == Block.blockEmerald.blockID) {
			return IHeatEntity.conductivityEmerald;
		}
		else {
			Block b = Block.blocksList[blockID];
			if(b instanceof IFluidBlock) {
				Fluid fluid = ((IFluidBlock)b).getFluid();
				if(fluid != null) {
					return getConductivityForFluid(fluid.getName());
				}
				else {
					return IHeatEntity.conductivityWater;
				}
			}
			else {
				// Screw it, just assume it's air.
				return IHeatEntity.conductivityAir;
			}
		}
	}
	
	private float getConductivityForFluid(String fluidName) {
		if(fluidName.equals("water")) {
			return IHeatEntity.conductivityWater;
		}
		else if(fluidName.equals("ender")) {
			return IHeatEntity.conductivityGold;
		}
		else if(fluidName.equals("redstone")) {
			return IHeatEntity.conductivityEmerald;
		}
		else if(fluidName.equals("cryotheum")) {
			return IHeatEntity.conductivityGold;
		}
		else if(fluidName.equals("pyrotheum")) {
			return IHeatEntity.conductivityGlass;
		}
		else if(fluidName.equals("glowstone")) {
			return IHeatEntity.conductivityStone;
		}
		
		return IHeatEntity.conductivityWater;
	}
}

package erogenousbeef.bigreactors.common.multiblock.tileentity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.InventoryPlayer;
import erogenousbeef.bigreactors.common.multiblock.block.BlockExchangerPart;
import erogenousbeef.core.multiblock.MultiblockControllerBase;
import erogenousbeef.core.multiblock.MultiblockValidationException;

public class TileEntityExchangerPart extends TileEntityExchangerPartBase {

	public TileEntityExchangerPart() {
		super();
	}

	@Override
	public void isGoodForFrame() throws MultiblockValidationException {
		int metadata = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
		if(BlockExchangerPart.isCasing(metadata)) {
			throw new MultiblockValidationException("Only casing may be used in a heat exchanger's frame");
		}
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
		throw new MultiblockValidationException(String.format("%d, %d, %d - Block is not valid for the interior of a heat exchanger", xCoord, yCoord, zCoord));
	}
	
	@Override
	public void onMachineActivated() {
		super.onMachineActivated();
		if(worldObj.isRemote) {
			int metadata = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
			if(BlockExchangerPart.isController(metadata)) {
				// Re-render on client
				worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			}
		}
	}
	
	@Override
	public void onMachineBroken() {
		super.onMachineBroken();
		if(worldObj.isRemote) {
			int metadata = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
			if(BlockExchangerPart.isController(metadata)) {
				worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			}
		}
	}
	
	@Override
	public Object getContainer(InventoryPlayer inventoryPlayer) {
		if(!this.isConnected()) {
			return null;
		}
	
		int metadata = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
		if(BlockExchangerPart.isController(metadata)) {
			// TODO
			return null;
		}
		
		return null;
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public Object getGuiElement(InventoryPlayer inventoryPlayer) {
		if(!this.isConnected()) {
			return null;
		}
		
		int metadata = worldObj.getBlockMetadata(this.xCoord, this.yCoord, this.zCoord);
		if(BlockExchangerPart.isController(metadata)) {
			// TODO
			return null;
		}

		return null;
	}
}

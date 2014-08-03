package erogenousbeef.bigreactors.common.multiblock.tileentity;

import net.minecraft.block.Block;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import erogenousbeef.bigreactors.client.gui.GuiReactorStatus;
import erogenousbeef.bigreactors.common.BRLog;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.multiblock.block.BlockReactorPart;
import erogenousbeef.bigreactors.gui.container.ContainerReactorController;
import erogenousbeef.core.common.CoordTriplet;
import erogenousbeef.core.multiblock.MultiblockControllerBase;
import erogenousbeef.core.multiblock.MultiblockValidationException;

public class TileEntityReactorPart extends TileEntityReactorPartBase {

	public TileEntityReactorPart() {
		super();
	}

	@Override
	public void isGoodForFrame() throws MultiblockValidationException {
		int metadata = this.worldObj.getBlockMetadata(this.xCoord, this.yCoord, this.zCoord);
		if(BlockReactorPart.isCasing(metadata)) { return; }
		
		throw new MultiblockValidationException(String.format("%d, %d, %d - Only casing may be used as part of a reactor's frame", xCoord, yCoord, zCoord));
	}

	@Override
	public void isGoodForSides() throws MultiblockValidationException {
		// All parts are valid for sides, by default
	}

	@Override
	public void isGoodForTop() throws MultiblockValidationException {
		// All parts are valid for the top, by default
	}

	@Override
	public void isGoodForBottom() throws MultiblockValidationException {
		// All parts are valid for the bottom, by default
	}

	@Override
	public void isGoodForInterior() throws MultiblockValidationException {
		throw new MultiblockValidationException(String.format("%d, %d, %d - This reactor part may not be placed in the reactor's interior", xCoord, yCoord, zCoord));
	}

	@Override
	public void onMachineAssembled(MultiblockControllerBase multiblockController) {
		super.onMachineAssembled(multiblockController);
	}

	@Override
	public void onMachineBroken() {
		super.onMachineBroken();
	}

	@Override
	public void onMachineActivated() {
		// Re-render controllers on client
		if(this.worldObj.isRemote) {
			if(getBlockType() == BigReactors.blockReactorPart) {
				int metadata = this.getBlockMetadata();
				if(BlockReactorPart.isController(metadata)) {
					worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
				}
			}
		}
	}

	@Override
	public void onMachineDeactivated() {
		// Re-render controllers on client
		if(this.worldObj.isRemote) {
			if(getBlockType() == BigReactors.blockReactorPart) {
				int metadata = this.getBlockMetadata();
				if(BlockReactorPart.isController(metadata)) {
					worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
				}
			}
		}
	}

	// IMultiblockGuiHandler
	/**
	 * @return The Container object for use by the GUI. Null if there isn't any.
	 */
	@Override
	public Object getContainer(InventoryPlayer inventoryPlayer) {
		if(!this.isConnected()) {
			return null;
		}
		
		int metadata = this.worldObj.getBlockMetadata(this.xCoord, this.yCoord, this.zCoord);		
		if(BlockReactorPart.isController(metadata)) {
			return new ContainerReactorController(this, inventoryPlayer.player);
		}
		return null;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public Object getGuiElement(InventoryPlayer inventoryPlayer) {
		if(!this.isConnected()) {
			return null;
		}
		
		int metadata = this.worldObj.getBlockMetadata(this.xCoord, this.yCoord, this.zCoord);
		if(BlockReactorPart.isController(metadata)) {
			return new GuiReactorStatus(new ContainerReactorController(this, inventoryPlayer.player), this);
		}
		return null;
	}
}

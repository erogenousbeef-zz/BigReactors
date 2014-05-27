package erogenousbeef.bigreactors.common.multiblock.tileentity;

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
		int metadata = this.worldObj.getBlockMetadata(this.xCoord, this.yCoord, this.zCoord);
		if(BlockReactorPart.isCasing(metadata)) { return; }

		throw new MultiblockValidationException(String.format("%d, %d, %d - This part may not be placed on a reactor's top face", xCoord, yCoord, zCoord));
	}

	@Override
	public void isGoodForBottom() throws MultiblockValidationException {
		int metadata = this.worldObj.getBlockMetadata(this.xCoord, this.yCoord, this.zCoord);
		if(BlockReactorPart.isCasing(metadata)) { return; }

		throw new MultiblockValidationException(String.format("%d, %d, %d - This part may not be placed on a reactor's bottom face", xCoord, yCoord, zCoord));
	}

	@Override
	public void isGoodForInterior() throws MultiblockValidationException {
		throw new MultiblockValidationException(String.format("%d, %d, %d - This reactor part may not be placed in the reactor's interior", xCoord, yCoord, zCoord));
	}

	@Override
	public void onMachineAssembled(MultiblockControllerBase multiblockController) {
		super.onMachineAssembled(multiblockController);

		if(this.worldObj.isRemote) { return; }
		if(multiblockController == null) {
			throw new IllegalArgumentException("Being assembled into a null controller. This should never happen. Please report this stacktrace to http://github.com/ErogenousBeef/BigReactors/");
		}

		// Autoheal, for issue #65.
		if(this.getMultiblockController() == null) {
			BRLog.warning("Reactor part at (%d, %d, %d) is being assembled without being attached to a reactor. Attempting to auto-heal. Fully destroying and re-building this reactor is recommended if errors persist.", xCoord, yCoord, zCoord);
			this.onAttached(multiblockController);
		}
		
		if(getBlockType() == BigReactors.blockReactorPart) {
			int metadata = this.getBlockMetadata();
			if(BlockReactorPart.isCasing(metadata)) {
				this.setCasingMetadataBasedOnWorldPosition();
			}
			else if(BlockReactorPart.isController(metadata)) {
				// This is called during world loading as well, so controllers can start active.
				if(!this.getReactorController().isActive()) {
					this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, BlockReactorPart.CONTROLLER_IDLE, 2);				
				}
				else {
					this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, BlockReactorPart.CONTROLLER_ACTIVE, 2);				
				}
			}
		}
	}

	@Override
	public void onMachineBroken() {
		super.onMachineBroken();

		if(this.worldObj.isRemote) { return; }
		
		if(getBlockType() == BigReactors.blockReactorPart) {
			int metadata = this.getBlockMetadata();
			if(BlockReactorPart.isCasing(metadata)) {
				this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, BlockReactorPart.CASING_METADATA_BASE, 2);
			}
			else if(BlockReactorPart.isController(metadata)) {
				this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, BlockReactorPart.CONTROLLER_METADATA_BASE, 2);
			}
		}
	}

	@Override
	public void onMachineActivated() {
		if(this.worldObj.isRemote) { return; }
		
		if(getBlockType() == BigReactors.blockReactorPart) {
			int metadata = this.getBlockMetadata();
			if(BlockReactorPart.isController(metadata)) {
				this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, BlockReactorPart.CONTROLLER_ACTIVE, 2);
			}
		}
	}

	@Override
	public void onMachineDeactivated() {
		if(this.worldObj.isRemote) { return; }

		if(getBlockType() == BigReactors.blockReactorPart) {
			int metadata = this.getBlockMetadata();
			if(BlockReactorPart.isController(metadata)) {
				this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, BlockReactorPart.CONTROLLER_IDLE, 2);
			}
		}
	}

	private void setCasingMetadataBasedOnWorldPosition() {
		MultiblockControllerBase controller = this.getMultiblockController();
		assert(controller != null);
		CoordTriplet minCoord = controller.getMinimumCoord();
		CoordTriplet maxCoord = controller.getMaximumCoord();
		
		int extremes = 0;
		boolean xExtreme, yExtreme, zExtreme;
		xExtreme = yExtreme = zExtreme = false;

		if(xCoord == minCoord.x) { extremes++; xExtreme = true; }
		if(yCoord == minCoord.y) { extremes++; yExtreme = true; }
		if(zCoord == minCoord.z) { extremes++; zExtreme = true; }
		
		if(xCoord == maxCoord.x) { extremes++; xExtreme = true; }
		if(yCoord == maxCoord.y) { extremes++; yExtreme = true; }
		if(zCoord == maxCoord.z) { extremes++; zExtreme = true; }
		
		if(extremes == 3) {
			// Corner
			this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, BlockReactorPart.CASING_CORNER, 2);
		}
		else if(extremes == 2) {
			if(!xExtreme) {
				// Y/Z - must be east/west
				this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, BlockReactorPart.CASING_EASTWEST, 2);
			}
			else if(!zExtreme) {
				// X/Y - must be north-south
				this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, BlockReactorPart.CASING_NORTHSOUTH, 2);
			}
			else {
				// Not a y-extreme, must be vertical
				this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, BlockReactorPart.CASING_VERTICAL, 2);
			}						
		}
		else if(extremes == 1) {
			this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, BlockReactorPart.CASING_CENTER, 2);
		}
		else {
			// This shouldn't happen.
			this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, BlockReactorPart.CASING_METADATA_BASE, 2);
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

	// Only refresh if we're switching functionality
	// Warning: dragonz!
	@Override
    public boolean shouldRefresh(int oldID, int newID, int oldMeta, int newMeta, World world, int x, int y, int z)
    {
		if(oldID != newID) {
			return true;
		}
		if(BlockReactorPart.isCasing(oldMeta) && BlockReactorPart.isCasing(newMeta)) {
			return false;
		}
		if(BlockReactorPart.isAccessPort(oldMeta) && BlockReactorPart.isAccessPort(newMeta)) {
			return false;
		}
		if(BlockReactorPart.isController(oldMeta) && BlockReactorPart.isController(newMeta)) {
			return false;
		}
		if(BlockReactorPart.isPowerTap(oldMeta) && BlockReactorPart.isPowerTap(newMeta)) {
			return false;
		}
		if(BlockReactorPart.isRedNetPort(oldMeta) && BlockReactorPart.isPowerTap(newMeta)) {
			return false;
		}
		return true;
    }
}

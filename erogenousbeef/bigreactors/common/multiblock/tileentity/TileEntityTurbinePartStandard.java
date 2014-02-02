package erogenousbeef.bigreactors.common.multiblock.tileentity;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import erogenousbeef.bigreactors.client.gui.GuiTurbineController;
import erogenousbeef.bigreactors.common.multiblock.block.BlockTurbinePart;
import erogenousbeef.bigreactors.gui.container.ContainerSlotless;
import erogenousbeef.core.multiblock.MultiblockValidationException;

public class TileEntityTurbinePartStandard extends TileEntityTurbinePartBase {

	public TileEntityTurbinePartStandard() {
		super();
	}

	@Override
	public void isGoodForFrame() throws MultiblockValidationException {
		if(getBlockMetadata() != BlockTurbinePart.METADATA_HOUSING) {
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
		if(getBlockMetadata() != BlockTurbinePart.METADATA_HOUSING) {
			throw new MultiblockValidationException(String.format("%d, %d, %d - this part is not valid for the interior of a turbine", xCoord, yCoord, zCoord));
		}
	}
	
	@Override
	public Object getContainer(InventoryPlayer inventoryPlayer) {
		if(!this.isConnected()) {
			return null;
		}
		
		if(getBlockMetadata() == BlockTurbinePart.METADATA_CONTROLLER) {
			return (Object)(new ContainerSlotless(getTurbine(), inventoryPlayer.player));
		}
		
		return null;
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public Object getGuiElement(InventoryPlayer inventoryPlayer) {
		if(!this.isConnected()) {
			return null;
		}

		if(getBlockMetadata() == BlockTurbinePart.METADATA_CONTROLLER) {
			return new GuiTurbineController((Container)getContainer(inventoryPlayer), this);
		}
		return null;
	}

	@Override
	public void onMachineActivated() {
		// Re-render controller as active state has changed
		if(worldObj.isRemote && getBlockMetadata() == BlockTurbinePart.METADATA_CONTROLLER) {
			this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		}
	}

	@Override
	public void onMachineDeactivated() {
		// Re-render controller as active state has changed
		if(worldObj.isRemote && getBlockMetadata() == BlockTurbinePart.METADATA_CONTROLLER) {
			this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		}
	}	
}

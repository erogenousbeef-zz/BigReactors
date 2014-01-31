package erogenousbeef.bigreactors.common.multiblock.tileentity;

import java.io.DataInputStream;
import java.io.IOException;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraftforge.common.ForgeDirection;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import erogenousbeef.bigreactors.client.gui.GuiTurbineController;
import erogenousbeef.bigreactors.common.multiblock.MultiblockTurbine;
import erogenousbeef.bigreactors.common.multiblock.block.BlockTurbinePart;
import erogenousbeef.bigreactors.common.multiblock.interfaces.IMultiblockGuiHandler;
import erogenousbeef.bigreactors.common.multiblock.interfaces.IMultiblockNetworkHandler;
import erogenousbeef.bigreactors.gui.container.ContainerSlotless;
import erogenousbeef.bigreactors.net.PacketWrapper;
import erogenousbeef.bigreactors.net.Packets;
import erogenousbeef.core.common.CoordTriplet;
import erogenousbeef.core.multiblock.MultiblockControllerBase;
import erogenousbeef.core.multiblock.MultiblockTileEntityBase;
import erogenousbeef.core.multiblock.rectangular.RectangularMultiblockTileEntityBase;

public abstract class TileEntityTurbinePartBase extends RectangularMultiblockTileEntityBase implements IMultiblockGuiHandler, IMultiblockNetworkHandler {

	protected int _metadata;
	
	public TileEntityTurbinePartBase() {
		_metadata = -1;
	}
	
	public TileEntityTurbinePartBase(int metadata) {
		this();
		_metadata = metadata;
	}

	protected int getMetadata() {
		if(_metadata < 0) {
			_metadata = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
		}
		
		return _metadata;
	}
	
	@Override
	public MultiblockControllerBase createNewMultiblock() {
		return new MultiblockTurbine(worldObj);
	}
	
	@Override
	public Class<? extends MultiblockControllerBase> getMultiblockControllerType() {
		return MultiblockTurbine.class;
	}

	@Override
	public void onMachineAssembled(MultiblockControllerBase controller) {
		super.onMachineAssembled(controller);
		
		// Re-render this block on the client
		if(worldObj.isRemote) {
			this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		}
	}

	@Override
	public void onMachineBroken() {
		super.onMachineBroken();
		
		// Re-render this block on the client
		if(worldObj.isRemote) {
			this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		}
	}

	@Override
	public void onMachineActivated() {
		// Re-render controller as active state has changed
		if(worldObj.isRemote && getMetadata() == BlockTurbinePart.METADATA_CONTROLLER) {
			this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		}
	}

	@Override
	public void onMachineDeactivated() {
		// Re-render controller as active state has changed
		if(worldObj.isRemote && getMetadata() == BlockTurbinePart.METADATA_CONTROLLER) {
			this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		}
	}
	
	///// Network communication - IMultiblockNetworkHandler

	@Override
	public void onNetworkPacket(int packetType, DataInputStream data) throws IOException {
		if(!this.isConnected()) {
			return;
		}

		getTurbine().onNetworkPacket(packetType, data);
	}

	/// GUI Support - IMultiblockGuiHandler
	/**
	 * @return The Container object for use by the GUI. Null if there isn't any.
	 */
	@Override
	public Object getContainer(InventoryPlayer inventoryPlayer) {
		if(!this.isConnected()) {
			return null;
		}
		
		if(getMetadata() == BlockTurbinePart.METADATA_CONTROLLER) {
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

		if(getMetadata() == BlockTurbinePart.METADATA_CONTROLLER) {
			return new GuiTurbineController((Container)getContainer(inventoryPlayer), this);
		}
		return null;
	}

	public MultiblockTurbine getTurbine() {
		return (MultiblockTurbine)getMultiblockController();
	}
}

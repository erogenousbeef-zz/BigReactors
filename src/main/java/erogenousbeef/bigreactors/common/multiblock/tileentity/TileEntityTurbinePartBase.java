package erogenousbeef.bigreactors.common.multiblock.tileentity;

import io.netty.buffer.ByteBuf;

import java.io.IOException;

import net.minecraft.entity.player.InventoryPlayer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import erogenousbeef.bigreactors.common.BRLog;
import erogenousbeef.bigreactors.common.interfaces.IActivateable;
import erogenousbeef.bigreactors.common.multiblock.MultiblockTurbine;
import erogenousbeef.bigreactors.common.multiblock.interfaces.IMultiblockGuiHandler;
import erogenousbeef.core.common.CoordTriplet;
import erogenousbeef.core.multiblock.MultiblockControllerBase;
import erogenousbeef.core.multiblock.rectangular.RectangularMultiblockTileEntityBase;

public abstract class TileEntityTurbinePartBase extends RectangularMultiblockTileEntityBase implements IMultiblockGuiHandler, IActivateable {

	public TileEntityTurbinePartBase() {
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
	}

	@Override
	public void onMachineDeactivated() {
	}

	/// GUI Support - IMultiblockGuiHandler
	/**
	 * @return The Container object for use by the GUI. Null if there isn't any.
	 */
	@Override
	public Object getContainer(InventoryPlayer inventoryPlayer) {
		return null;
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public Object getGuiElement(InventoryPlayer inventoryPlayer) {
		return null;
	}

	public MultiblockTurbine getTurbine() {
		return (MultiblockTurbine)getMultiblockController();
	}
	
	// IActivateable
	// IActivateable
	@Override
	public CoordTriplet getReferenceCoord() {
		if(isConnected()) {
			return getMultiblockController().getReferenceCoord();
		}
		else {
			return new CoordTriplet(xCoord, yCoord, zCoord);
		}
	}
	
	@Override
	public boolean getActive() {
		if(isConnected()) {
			return getTurbine().getActive();
		}
		else {
			return false;
		}
	}
	
	@Override
	public void setActive(boolean active) {
		if(isConnected()) {
			getTurbine().setActive(active);
		}
		else {
			BRLog.error("Received a setActive command at %d, %d, %d, but not connected to a multiblock controller!", xCoord, yCoord, zCoord);
		}
	}
}

package erogenousbeef.bigreactors.common.multiblock.tileentity;

import net.minecraft.entity.player.InventoryPlayer;
import erogenousbeef.bigreactors.common.BRLog;
import erogenousbeef.bigreactors.common.interfaces.IBeefDebuggableTile;
import erogenousbeef.bigreactors.common.multiblock.MultiblockHeatExchanger;
import erogenousbeef.bigreactors.common.multiblock.MultiblockReactor;
import erogenousbeef.bigreactors.common.multiblock.interfaces.IActivateable;
import erogenousbeef.bigreactors.common.multiblock.interfaces.IMultiblockGuiHandler;
import erogenousbeef.core.common.CoordTriplet;
import erogenousbeef.core.multiblock.MultiblockControllerBase;
import erogenousbeef.core.multiblock.MultiblockValidationException;
import erogenousbeef.core.multiblock.rectangular.RectangularMultiblockTileEntityBase;

public abstract class TileEntityExchangerPartBase extends
		RectangularMultiblockTileEntityBase implements IMultiblockGuiHandler,
		IActivateable, IBeefDebuggableTile {

	protected TileEntityExchangerPartBase() {}

	@Override
	public boolean canUpdate() { return false; }
	
	public MultiblockHeatExchanger getExchangerController() {
		return (MultiblockHeatExchanger)getMultiblockController();
	}
	
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
			return getExchangerController().getActive();
		}
		else {
			return false;
		}
	}
	
	@Override
	public void setActive(boolean active) {
		if(isConnected()) {
			getExchangerController().setActive(active);
		}
		else {
			BRLog.error("Received a setActive command at %d, %d, %d, but not connected to a multiblock controller!", xCoord, yCoord, zCoord);
		}
	}

	@Override
	public Object getContainer(InventoryPlayer inventoryPlayer) {
		return null;
	}

	@Override
	public Object getGuiElement(InventoryPlayer inventoryPlayer) {
		return null;
	}

	@Override
	public void onMachineActivated() {
	}

	@Override
	public void onMachineDeactivated() {
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
	public MultiblockControllerBase createNewMultiblock() {
		return new MultiblockHeatExchanger(worldObj);
	}

	@Override
	public Class<? extends MultiblockControllerBase> getMultiblockControllerType() {
		return MultiblockHeatExchanger.class;
	}

	@Override
	public String getDebugInfo() {
		MultiblockHeatExchanger ex = getExchangerController();
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().toString()).append("\n");
		if(ex == null) {
			sb.append("Not attached to controller!");
			return sb.toString();
		}
		sb.append(ex.getDebugInfo());
		return sb.toString();
	}	
}

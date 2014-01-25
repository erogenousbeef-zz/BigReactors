package erogenousbeef.bigreactors.common.multiblock.tileentity;

import java.io.DataInputStream;
import java.io.IOException;

import net.minecraft.entity.player.InventoryPlayer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import erogenousbeef.bigreactors.api.IHeatEntity;
import erogenousbeef.bigreactors.api.IRadiationModerator;
import erogenousbeef.bigreactors.api.RadiationData;
import erogenousbeef.bigreactors.api.RadiationPacket;
import erogenousbeef.bigreactors.common.multiblock.MultiblockReactor;
import erogenousbeef.bigreactors.common.multiblock.interfaces.IMultiblockGuiHandler;
import erogenousbeef.bigreactors.common.multiblock.interfaces.IMultiblockNetworkHandler;
import erogenousbeef.bigreactors.net.Packets;
import erogenousbeef.core.multiblock.MultiblockControllerBase;
import erogenousbeef.core.multiblock.rectangular.RectangularMultiblockTileEntityBase;

public abstract class TileEntityReactorPartBase extends
		RectangularMultiblockTileEntityBase implements IMultiblockNetworkHandler, IMultiblockGuiHandler, IHeatEntity, IRadiationModerator {

	public TileEntityReactorPartBase() {
	}

	public MultiblockReactor getReactorController() { return (MultiblockReactor)this.getMultiblockController(); }
	
	@Override
	public boolean canUpdate() { return false; }

	@Override
	public MultiblockControllerBase createNewMultiblock() {
		return new MultiblockReactor(this.worldObj);
	}
	
	@Override
	public Class<? extends MultiblockControllerBase> getMultiblockControllerType() { return MultiblockReactor.class; }

	// IMultiblockNetworkHandler
	@Override
	public void onNetworkPacket(int packetType, DataInputStream data) throws IOException {
		if(!this.isConnected()) {
			return;
		}

		/// Client->Server packets
		
		if(packetType == Packets.MultiblockControllerButton) {
			String buttonName = data.readUTF();
			boolean newValue = data.readBoolean();
			
			if(buttonName.equals("activate")) {
				getReactorController().setActive(newValue);
			}
			else if(buttonName.equals("ejectWaste")) {
				getReactorController().ejectWaste();
			}
		}
		
		if(packetType == Packets.ReactorWasteEjectionSettingUpdate) {
			getReactorController().changeWasteEjection();
		}
		
		/// Server->Client packets
		
		if(packetType == Packets.ReactorControllerFullUpdate) {
			getReactorController().receiveReactorUpdate(data);
		}
	}
	
	// IMultiblockGuiHandler
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
	
	// IHeatEntity
	@Override
	public float getHeat() {
		if(!this.isConnected()) { return 0f; }
		return getReactorController().getReactorHeat();
	}

	@Override
	public float getThermalConductivity() {
		return IHeatEntity.conductivityIron;
	}

	// IRadiationModerator
	@Override
	public void moderateRadiation(RadiationData data, RadiationPacket radiation) {
		// Discard all remaining radiation, sorry bucko
		radiation.intensity = 0f;
	}
}

package erogenousbeef.bigreactors.common.multiblock.tileentity;

import io.netty.buffer.ByteBuf;

import java.io.DataInputStream;
import java.io.IOException;

import net.minecraft.entity.player.InventoryPlayer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import erogenousbeef.bigreactors.api.IHeatEntity;
import erogenousbeef.bigreactors.api.IRadiationModerator;
import erogenousbeef.bigreactors.common.data.RadiationData;
import erogenousbeef.bigreactors.common.data.RadiationPacket;
import erogenousbeef.bigreactors.common.multiblock.MultiblockReactor;
import erogenousbeef.bigreactors.common.multiblock.MultiblockReactor.WasteEjectionSetting;
import erogenousbeef.bigreactors.common.multiblock.interfaces.IMultiblockGuiHandler;
import erogenousbeef.bigreactors.common.multiblock.interfaces.IMultiblockNetworkHandler;
import erogenousbeef.bigreactors.net.Packets;
import erogenousbeef.core.common.CoordTriplet;
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
	public void onNetworkPacket(int packetType, ByteBuf data) {

		if(!this.isConnected()) {
			return;
		}

		if(packetType == Packets.ReactorControllerFullUpdate) {
			getReactorController().receiveReactorUpdate(data);
		}
		/// Client->Server packets
		
		if(packetType == Packets.MultiblockActivateButton) {
			boolean newValue = data.readBoolean();
			getReactorController().setActive(newValue);
		}
		
		if(packetType == Packets.ReactorWasteEjectionSettingUpdate) {
			int newSetting = data.readInt();
			getReactorController().setWasteEjection(WasteEjectionSetting.values()[newSetting]);
		}
		
		if(packetType == Packets.ReactorEjectButton) {
			boolean isFuelButton = data.readBoolean();
			boolean dumpAll = data.readBoolean();
			
			CoordTriplet destination = null;
			if(data.readBoolean())
			{
				destination = new CoordTriplet(data.readInt(), data.readInt(), data.readInt());
			}
			
			if(isFuelButton) {
				getReactorController().ejectFuel(dumpAll, destination);
			}
			else {
				getReactorController().ejectWaste(dumpAll, destination);
			}
		}
		
		/// Server->Client packets
		
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
		return getReactorController().getFuelHeat();
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

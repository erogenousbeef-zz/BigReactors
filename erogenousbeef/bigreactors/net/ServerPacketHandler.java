package erogenousbeef.bigreactors.net;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorControlRod;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorRedNetPort;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorRedstonePort;
import erogenousbeef.bigreactors.common.multiblock.interfaces.IMultiblockNetworkHandler;
import erogenousbeef.bigreactors.gui.IBeefGuiEntity;

public class ServerPacketHandler implements IPacketHandler {

	@Override
	public void onPacketData(INetworkManager manager,
			Packet250CustomPayload packet, Player player) {
		
		DataInputStream data = new DataInputStream(new ByteArrayInputStream(packet.data));
		int packetType = PacketWrapper.readPacketID(data);
		
		int x, y, z;
		switch(packetType) {
		case Packets.MultiblockControllerButton:
		case Packets.AccessPortButton:
		case Packets.ReactorWasteEjectionSettingUpdate:
			try {
				x = data.readInt();
				y = data.readInt();
				z = data.readInt();
				TileEntity te = ((EntityPlayer)player).worldObj.getBlockTileEntity(x, y, z);
				if(te instanceof IMultiblockNetworkHandler) {
					((IMultiblockNetworkHandler)te).onNetworkPacket(packetType, data);
				}
				else {
					throw new IOException("Invalid TileEntity for receipt of ReactorControllerButton packet");
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
			
			break;
		case Packets.BeefGuiButtonPress:
			try {
				x = data.readInt();
				y = data.readInt();
				z = data.readInt();
				TileEntity te = ((EntityPlayer)player).worldObj.getBlockTileEntity(x, y, z);
				if(te != null & te instanceof IBeefGuiEntity) {
					String buttonName = data.readUTF();
					((IBeefGuiEntity)te).onReceiveGuiButtonPress(buttonName, data);
				}
				else {
					throw new IOException("Invalid TileEntity for receipt of ReactorControllerButton packet");
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
			
			break;
		case Packets.ControlRodSetName:
			try {
				x = data.readInt();
				y = data.readInt();
				z = data.readInt();
				TileEntity te = ((EntityPlayer)player).worldObj.getBlockTileEntity(x, y, z);
				if(te instanceof TileEntityReactorControlRod) {
					String newName = data.readUTF();
					((TileEntityReactorControlRod)te).setName(newName);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
			
		case Packets.RedNetSetData:
			try {
				x = data.readInt();
				y = data.readInt();
				z = data.readInt();
				
				TileEntity te = ((EntityPlayer)player).worldObj.getBlockTileEntity(x, y, z);
				if(te instanceof TileEntityReactorRedNetPort) {
					((TileEntityReactorRedNetPort)te).decodeSettings(data, true);
				}
				else {
					throw new IOException("Invalid TileEntity for receipt of RedNetSetData packet");
				}
			} catch(IOException e) {
				e.printStackTrace();
			}
			break;
		case Packets.RedstoneSetData:
			try {
				x = data.readInt();
				y = data.readInt();
				z = data.readInt();
				
				TileEntity te = ((EntityPlayer)player).worldObj.getBlockTileEntity(x, y, z);
				if(te instanceof TileEntityReactorRedstonePort) {
					int newCircuit = data.readInt();
					int newLevel = data.readInt();
					boolean newGt = data.readBoolean();
					boolean pulse = data.readBoolean();
					((TileEntityReactorRedstonePort)te).onReceiveUpdatePacket(newCircuit, newLevel, newGt, pulse);
				}
			} catch(IOException e) {
				e.printStackTrace();
			}
			break;
			
		}
	}
}

package erogenousbeef.bigreactors.net;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;
import erogenousbeef.bigreactors.common.tileentity.TileEntityReactorPart;
import erogenousbeef.bigreactors.common.tileentity.base.TileEntityBeefBase;

public class ClientPacketHandler implements IPacketHandler {

	@Override
	public void onPacketData(INetworkManager manager,
			Packet250CustomPayload packet, Player player) {

		DataInputStream data = new DataInputStream(new ByteArrayInputStream(packet.data));
		int packetType = PacketWrapper.readPacketID(data);
		
		int x, y, z;
		switch(packetType) {
		case Packets.ReactorControllerFullUpdate: {
				try {
					x = data.readInt();
					y = data.readInt();
					z = data.readInt();
					TileEntity te = ((EntityPlayer)player).worldObj.getBlockTileEntity(x, y, z);
					if(te != null & te instanceof TileEntityReactorPart) {
						((TileEntityReactorPart)te).onNetworkPacket(packetType, data);
					}
					else {
						throw new IOException("Invalid TileEntity for receipt of TickUpdate packet");
					}
	
				} catch (IOException e) {
					e.printStackTrace();
					// TODO: Crash all the things.
				}			
			}
		break;
		case Packets.SmallMachineUIUpdate: {
				try {
					x = data.readInt();
					y = data.readInt();
					z = data.readInt();
					
					TileEntity te = ((EntityPlayer)player).worldObj.getBlockTileEntity(x, y, z);
					if(te != null && te instanceof TileEntityBeefBase) {
						NBTTagCompound tagCompound = Packet.readNBTTagCompound(data);
						((TileEntityBeefBase)te).onReceiveUpdate(tagCompound);
					}
					else {
						System.out.println(String.format("Error coords: %d, %d, %d", x, y, z));
						throw new IOException("Invalid TileEntity for receipt of TileEntityBeefBase packet");
					}
				} catch(IOException e) {
					e.printStackTrace();
					// TODO: Crash all the things.
				}
			}
		break;
		}
	}
}

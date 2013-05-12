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
import erogenousbeef.bigreactors.common.tileentity.TileEntityReactorPart;

public class ClientPacketHandler implements IPacketHandler {

	@Override
	public void onPacketData(INetworkManager manager,
			Packet250CustomPayload packet, Player player) {

		DataInputStream data = new DataInputStream(new ByteArrayInputStream(packet.data));
		int packetType = PacketWrapper.readPacketID(data);
		
		switch(packetType) {
		case Packets.ReactorControllerFullUpdate:
			int x, y, z;
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
			
			break;
		}
		
		
	}

}

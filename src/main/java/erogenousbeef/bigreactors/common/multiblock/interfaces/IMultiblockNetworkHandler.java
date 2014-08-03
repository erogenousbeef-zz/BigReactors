package erogenousbeef.bigreactors.common.multiblock.interfaces;

import io.netty.buffer.ByteBuf;

import java.io.DataInputStream;
import java.io.IOException;

import erogenousbeef.bigreactors.net.message.MultiblockMessage;

public interface IMultiblockNetworkHandler {

	/**
	 * Called when this block receives a network packet of any type.
	 * This method is always called within a try/catch block.
	 * Throw an IOException if there's trouble.
	 * 
	 * @see erogenousbeef.bigreactors.net.message.MultiblockMessage
	 * @param packetType The type of the packet received, as defined in the Packets class.
	 * @param data The DataInputStream with the data
	 * @throws IOException if there's trouble handling a packet
	 */
	public void onNetworkPacket(MultiblockMessage.Type packetType, ByteBuf data) throws IOException;
}

package erogenousbeef.bigreactors.common.multiblock.interfaces;

import java.io.DataInputStream;
import java.io.IOException;

public interface IMultiblockNetworkHandler {

	/**
	 * Called when this block receives a network packet of any type.
	 * This method is always called within a try/catch block.
	 * Throw an IOException if there's trouble.
	 * 
	 * @see erogenousbeef.bigreactors.net.Packets
	 * @param packetType The type of the packet received, as defined in the Packets class.
	 * @param data The DataInputStream with the data
	 * @throws An IOException if there's trouble handling a packet
	 */
	public void onNetworkPacket(int packetType, DataInputStream data) throws IOException;
	
}

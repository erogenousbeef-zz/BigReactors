package erogenousbeef.bigreactors.common.tileentity.base;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.net.PacketWrapper;
import erogenousbeef.bigreactors.net.Packets;

import universalelectricity.prefab.network.PacketManager;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet132TileEntityData;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;

public abstract class TileEntityBeefBase extends TileEntity {
	private Set<EntityPlayer> updatePlayers;
	private int ticksSinceLastUpdate;
	private static final int ticksBetweenUpdates = 3;

	public TileEntityBeefBase() {
		super();
		
		ticksSinceLastUpdate = 0;
		updatePlayers = new HashSet<EntityPlayer>();
	}
	
	public abstract GuiScreen getGUI(EntityPlayer player);
	
	public abstract Container getContainer(EntityPlayer player);

	// Network Communication
	@Override
	public Packet getDescriptionPacket()
	{
		NBTTagCompound tagCompound = new NBTTagCompound();
		this.writeToNBT(tagCompound);
		
		return new Packet132TileEntityData(xCoord, yCoord, zCoord, 0, tagCompound);
	}

	@Override
	public void onDataPacket(INetworkManager network, Packet132TileEntityData packet) {
		this.readFromNBT(packet.customParam1);
	}
	
	@Override
	public void updateEntity() {
		super.updateEntity();
		
		if(!this.worldObj.isRemote && this.updatePlayers.size() > 0) {
			ticksSinceLastUpdate++;
			if(ticksSinceLastUpdate >= ticksBetweenUpdates) {
				sendUpdatePacket();
				ticksSinceLastUpdate = 0;
			}
		}
	}

	
	// Player updates
	public void beginUpdatingPlayer(EntityPlayer player) {
		updatePlayers.add(player);
		sendUpdatePacketToClient(player);
	}
	
	public void stopUpdatingPlayer(EntityPlayer player) {
		updatePlayers.remove(player);
	}
	
	protected Packet getUpdatePacket() {
		// TODO: Corify
		NBTTagCompound childData = new NBTTagCompound();
		onSendUpdate(childData);
		
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream data = new DataOutputStream(bytes);
		try
		{
			data.write(Packets.SmallMachineUIUpdate);
			data.writeInt(this.xCoord);
			data.writeInt(this.yCoord);
			data.writeInt(this.zCoord);

			// Taken from Packet.java
            byte[] abyte = CompressedStreamTools.compress(childData);
            data.writeShort((short)abyte.length);
            data.write(abyte);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		Packet250CustomPayload newPacket = new Packet250CustomPayload();
		newPacket.channel = BigReactors.CHANNEL;
		newPacket.data = bytes.toByteArray();
		newPacket.length = newPacket.data.length;
		
		return newPacket;
	}
	
	private void sendUpdatePacketToClient(EntityPlayer recipient) {
		if(this.worldObj.isRemote) { return; }

		PacketDispatcher.sendPacketToPlayer(getUpdatePacket(), (Player)recipient);
		
	}
	
	private void sendUpdatePacket() {
		if(this.worldObj.isRemote) { return; }
		if(this.updatePlayers.size() <= 0) { return; }
		
		Packet data = getUpdatePacket();

		for(EntityPlayer player : updatePlayers) {
			PacketDispatcher.sendPacketToPlayer(data, (Player)player);
		}
	}
	
	/**
	 * Fill this NBT Tag Compound with your custom entity data.
	 * @param updateTag The tag to which your data should be written
	 */
	protected void onSendUpdate(NBTTagCompound updateTag) {}
	
	/**
	 * Read your custom update data from this NBT Tag Compound.
	 * @param updateTag The tag which should contain your data.
	 */
	public void onReceiveUpdate(NBTTagCompound updateTag) {}
}

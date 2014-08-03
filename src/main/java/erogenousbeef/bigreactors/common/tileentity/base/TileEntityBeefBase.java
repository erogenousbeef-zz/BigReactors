package erogenousbeef.bigreactors.common.tileentity.base;

import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import erogenousbeef.bigreactors.gui.IBeefGuiEntity;
import erogenousbeef.bigreactors.net.CommonPacketHandler;
import erogenousbeef.bigreactors.net.message.DeviceUpdateRotationMessage;
import erogenousbeef.bigreactors.net.message.DeviceUpdateMessage;

public abstract class TileEntityBeefBase extends TileEntity implements IBeefGuiEntity {
	private Set<EntityPlayer> updatePlayers;
	private int ticksSinceLastUpdate;
	private static final int ticksBetweenUpdates = 3;

	// A rotation matrix which assumes that the normalized forward direction is NORTH
	// 2 = Forward, 3 = Rear, 4 = Left, 5 = Right
	private static final int[][] ROTATION_MATRIX = {
		{0, 1, 2, 3, 5, 4}, // DOWN
		{0, 1, 2, 3, 5, 4}, // UP
		{0, 1, 2, 3, 5, 4}, // NORTH
		{0, 1, 3, 2, 4, 5}, // SOUTH
		{0, 1, 4, 5, 2, 3}, // WEST
		{0, 1, 5, 4, 3, 2}, // EAST
	};

	// Rotation
	ForgeDirection forwardFace;
	
	public TileEntityBeefBase() {
		super();
		
		forwardFace = ForgeDirection.NORTH;

		ticksSinceLastUpdate = 0;
		updatePlayers = new HashSet<EntityPlayer>();
	}

	// Rotation
	public ForgeDirection getFacingDirection() {
		return forwardFace;
	}
	
	public void rotateTowards(ForgeDirection newDirection) {
		if(forwardFace == newDirection) { return; }

		forwardFace = newDirection;
		if(!worldObj.isRemote) {
            CommonPacketHandler.INSTANCE.sendToAllAround(new DeviceUpdateRotationMessage(xCoord, yCoord, zCoord, newDirection.ordinal()), new NetworkRegistry.TargetPoint(worldObj.provider.dimensionId, xCoord, yCoord, zCoord, 50));
		}
	}
	
	public int getRotatedSide(int side) {
		if(side == 0 || side == 1) { return side; }
		
		return ROTATION_MATRIX[forwardFace.ordinal()][side];
	}

	// Save/Load
	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		
		// Rotation
		int rotation = tag.getInteger("rotation");
		forwardFace = ForgeDirection.getOrientation(rotation);
	}
	
	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		
		// Rotation
		tag.setInteger("rotation", forwardFace.ordinal());
	}
	
	// Network Communication
	@Override
	public Packet getDescriptionPacket()
	{
		NBTTagCompound tagCompound = new NBTTagCompound();
		this.writeToNBT(tagCompound);
		
		return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, tagCompound);
	}

	@Override
	public void onDataPacket(NetworkManager network, S35PacketUpdateTileEntity packet) {
		this.readFromNBT(packet.func_148857_g());
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

	
	// Player updates via IBeefGuiEntity
	@Override
	public void beginUpdatingPlayer(EntityPlayer player) {
		updatePlayers.add(player);
		sendUpdatePacketToClient(player);
	}

	@Override
	public void stopUpdatingPlayer(EntityPlayer player) {
		updatePlayers.remove(player);
	}
	
	protected IMessage getUpdatePacket() {
		NBTTagCompound childData = new NBTTagCompound();
		onSendUpdate(childData);
		
		return new DeviceUpdateMessage(xCoord, yCoord, zCoord, childData);
	}
	
	private void sendUpdatePacketToClient(EntityPlayer recipient) {
		if(this.worldObj.isRemote) { return; }

        CommonPacketHandler.INSTANCE.sendTo(getUpdatePacket(), (EntityPlayerMP)recipient);
		
	}
	
	private void sendUpdatePacket() {
		if(this.worldObj.isRemote) { return; }
		if(this.updatePlayers.size() <= 0) { return; }

		for(EntityPlayer player : updatePlayers) {
            CommonPacketHandler.INSTANCE.sendTo(getUpdatePacket(), (EntityPlayerMP)player);
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

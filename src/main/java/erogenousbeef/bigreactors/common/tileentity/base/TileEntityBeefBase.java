package erogenousbeef.bigreactors.common.tileentity.base;

import java.util.HashSet;
import java.util.Set;

import cofh.api.tileentity.IReconfigurableFacing;
import cofh.core.block.TileCoFHBase;
import cofh.lib.util.helpers.BlockHelper;

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
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.block.BlockBRDevice;
import erogenousbeef.bigreactors.common.interfaces.IBeefReconfigurableSides;
import erogenousbeef.bigreactors.gui.IBeefGuiEntity;
import erogenousbeef.bigreactors.net.CommonPacketHandler;
import erogenousbeef.bigreactors.net.message.DeviceUpdateExposureMessage;
import erogenousbeef.bigreactors.net.message.DeviceUpdateMessage;
import erogenousbeef.bigreactors.net.message.DeviceUpdateRotationMessage;

public abstract class TileEntityBeefBase extends TileCoFHBase implements IBeefGuiEntity, IBeefReconfigurableSides, IReconfigurableFacing {
	private Set<EntityPlayer> updatePlayers;
	private int ticksSinceLastUpdate;
	private static final int ticksBetweenUpdates = 3;

	protected static final int SIDE_UNEXPOSED = -1;
	protected static final int[] kEmptyIntArray = new int[0];

	protected int facing;	// Tile rotation
	int[] exposures; // Inventory/Fluid tank exposure

	public TileEntityBeefBase() {
		super();

		facing = ForgeDirection.NORTH.ordinal();

		exposures = new int[6];
		for(int i = 0; i < exposures.length; i++) {
			exposures[i] = SIDE_UNEXPOSED;
		}

		ticksSinceLastUpdate = 0;
		updatePlayers = new HashSet<EntityPlayer>();
	}

	// IReconfigurableFacing
	@Override
	public int getFacing() { return facing; }

	@Override
	public boolean setFacing(int newFacing) {
		if(facing == newFacing) { return false; }

		if(!allowYAxisFacing() && (newFacing == ForgeDirection.UP.ordinal() || newFacing == ForgeDirection.DOWN.ordinal())) {
			return false;
		}
		
		facing = newFacing;
		if(!worldObj.isRemote) {
            CommonPacketHandler.INSTANCE.sendToAllAround(new DeviceUpdateRotationMessage(xCoord, yCoord, zCoord, facing), new NetworkRegistry.TargetPoint(worldObj.provider.dimensionId, xCoord, yCoord, zCoord, 50));
            this.markChunkDirty();
		}

		this.callNeighborBlockChange();
		return true;
	}
	
	public int getRotatedSide(int side) {
		return BlockHelper.ICON_ROTATION_MAP[facing][side];
	}
	
	@Override
	public boolean rotateBlock() {
		return setFacing(BlockHelper.SIDE_LEFT[facing]);
	}
	
	@Override
	public boolean allowYAxisFacing() { return false; }

	// Save/Load
	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		
		// Rotation
		if(tag.hasKey("facing")) {
			facing = Math.max(0, Math.min(5, tag.getInteger("facing")));
		}
		else {
			facing = 2;
		}

		// Exposure settings
		if(tag.hasKey("exposures")) {
			int[] tagExposures = tag.getIntArray("exposures");
			assert(tagExposures.length == exposures.length);
			System.arraycopy(tagExposures, 0, exposures, 0, exposures.length);
		}
	}
	
	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		
		tag.setInteger("facing", facing);
		tag.setIntArray("exposures", exposures);
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

	// Return true if this machine is active.
	public abstract boolean isActive();
	
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
	
	// Side Exposure Helpers
	@Override
	public boolean setSide(int side, int config) {
		int rotatedSide = this.getRotatedSide(side);

		int numConfig = getNumConfig(side);
		if(config >= numConfig || config < -1) { config = SIDE_UNEXPOSED; }

		exposures[rotatedSide] = config;
		sendExposureUpdate();
		return true;
	}
	
	/**
	 * Autocorrecting getter for checking exposures without having to do the rotation yerself.
	 * @param worldSide The world side whose exposure you wish to get.
	 * @return The current exposure setting for the world side.
	 */
	protected int getExposure(int worldSide) {
		return exposures[getRotatedSide(worldSide)];
	}
	
	/**
	 * Used when sending updates from server to client; batch-updates all exposures.
	 * @param newExposures The new set of inventory exposures.
	 */
	public void setSides(int[] newExposures) {
		assert(newExposures.length == exposures.length);
		System.arraycopy(newExposures, 0, exposures, 0, newExposures.length);
		sendExposureUpdate(); // On client, should just notify neighbors
	}
	
	@Override
	public boolean incrSide(int side) {
		return changeSide(side, 1);
	}
	
	@Override
	public boolean decrSide(int side) {
		return changeSide(side, -1);
	}
	
	private boolean changeSide(int side, int amount) {
		int rotatedSide = this.getRotatedSide(side);
		
		int numConfig = getNumConfig(side);
		if(numConfig <= 0) { return false; }
		
		int newConfig = exposures[rotatedSide] + amount;
		if(newConfig >= numConfig) { newConfig = SIDE_UNEXPOSED; }

		return setSide(side, newConfig);
	}
	
	@Override
	public boolean resetSides() {
		boolean changed = false;
		
		for(int i = 0; i < exposures.length; i++) {
			if(exposures[i] != SIDE_UNEXPOSED) {
				changed = true;
				exposures[i] = SIDE_UNEXPOSED;
			}
		}
		
		if(changed) {
			sendExposureUpdate();
		}
		
		return true;
	}
	
	private void sendExposureUpdate() {
		if(!this.worldObj.isRemote) {
			// Send unrotated, as the rotation will be re-applied on the client
            CommonPacketHandler.INSTANCE.sendToAllAround(new DeviceUpdateExposureMessage(xCoord, yCoord, zCoord, exposures), new NetworkRegistry.TargetPoint(worldObj.provider.dimensionId, xCoord, yCoord, zCoord, 50));
            this.markChunkDirty();
		}
		else {
			// Re-render block on client
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		}

		this.callNeighborTileChange();
		this.callNeighborBlockChange();
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

	// Weird shit from TileCoFHBase
	public String getName() {
		return this.getBlockType().getUnlocalizedName();
	}
	
	public int getType() {
		return getBlockMetadata();
	}
}

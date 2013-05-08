package erogenousbeef.bigreactors.common.tileentity;

import java.io.DataInputStream;

import com.google.common.io.ByteArrayDataInput;

import erogenousbeef.bigreactors.api.IRadiationModerator;
import erogenousbeef.bigreactors.api.IRadiationPacket;
import erogenousbeef.bigreactors.client.gui.GuiReactorStatus;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.block.BlockReactorPart;
import erogenousbeef.bigreactors.common.multiblock.MultiblockReactor;
import erogenousbeef.bigreactors.gui.container.ContainerReactorController;
import erogenousbeef.bigreactors.net.PacketWrapper;
import erogenousbeef.bigreactors.net.Packets;
import erogenousbeef.core.common.CoordTriplet;
import erogenousbeef.core.multiblock.IMultiblockPart;
import erogenousbeef.core.multiblock.MultiblockControllerBase;
import erogenousbeef.core.multiblock.MultiblockTileEntityBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class TileEntityReactorPart extends MultiblockTileEntityBase implements IRadiationModerator, IMultiblockPart {

	public TileEntityReactorPart() {
		super();
	}

	public MultiblockReactor getReactorController() { return (MultiblockReactor)this.getMultiblockController(); }
	
	// Oh god this is a terrible hack
	@Override
	public void updateContainingBlockInfo() {
		super.updateContainingBlockInfo();
		
		if(this.worldObj.isRemote && !isConnected()) {
			// onBlockAdded is not normally called on clients
			onBlockAdded(this.worldObj, this.xCoord, this.yCoord, this.zCoord);
		}
	}
	
	@Override
	public boolean canUpdate() { return false; }

	@Override
	public void receivePulse(IRadiationPacket radiation) {
		if(this.isConnected()) {
			double newCasingHeat = radiation.getSlowRadiation();
			radiation.setSlowRadiation(0);
			radiation.setFastRadiation(0);
			
			getReactorController().addLatentHeat(newCasingHeat);
		}
	}

	@Override
	public boolean isGoodForFrame() {
		int metadata = this.worldObj.getBlockMetadata(this.xCoord, this.yCoord, this.zCoord);
		if(BlockReactorPart.isCasing(metadata)) { return true; }
		else { return false; }
	}

	@Override
	public boolean isGoodForSides() {
		int metadata = this.worldObj.getBlockMetadata(this.xCoord, this.yCoord, this.zCoord);
		if(BlockReactorPart.isControlRod(metadata)) { return false; }
		else { return true; }
	}

	@Override
	public boolean isGoodForTop() {
		int metadata = this.worldObj.getBlockMetadata(this.xCoord, this.yCoord, this.zCoord);
		if(BlockReactorPart.isCasing(metadata) || BlockReactorPart.isControlRod(metadata)) {
			return true;
		}
		return false;
	}

	@Override
	public boolean isGoodForBottom() {
		int metadata = this.worldObj.getBlockMetadata(this.xCoord, this.yCoord, this.zCoord);
		if(BlockReactorPart.isCasing(metadata)) { return true; }
		else { return false; }
	}

	@Override
	public boolean isGoodForInterior() {
		return false;
	}

	@Override
	public void onMachineAssembled() {
		if(this.worldObj.isRemote) { return; }

		int metadata = this.getBlockMetadata();
		if(BlockReactorPart.isCasing(metadata)) {
			this.setCasingMetadataBasedOnWorldPosition();
		}
		else if(BlockReactorPart.isControlRod(metadata)) {
			// Control rods start inserted.
			this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, BlockReactorPart.CONTROLROD_METADATA_BASE, 2);
		}
		else if(BlockReactorPart.isController(metadata)) {
			// Controllers start idle
			this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, BlockReactorPart.CONTROLLER_IDLE, 2);
		}
	}

	@Override
	public void onMachineBroken() {
		if(this.worldObj.isRemote) { return; }
		
		int metadata = this.getBlockMetadata();
		if(BlockReactorPart.isCasing(metadata)) {
			this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, BlockReactorPart.CASING_METADATA_BASE, 2);
		}
		else if(BlockReactorPart.isControlRod(metadata)) {
			this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, BlockReactorPart.CONTROLROD_METADATA_BASE, 2);
		}
		else if(BlockReactorPart.isController(metadata)) {
			// Controllers start idle
			this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, BlockReactorPart.CONTROLLER_METADATA_BASE, 2);
		}
	}

	@Override
	public void onMachineActivated() {
		if(this.worldObj.isRemote) { return; }
		
		int metadata = this.getBlockMetadata();
		if(BlockReactorPart.isController(metadata)) {
			this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, BlockReactorPart.CONTROLLER_ACTIVE, 2);
		}
		
	}

	@Override
	public void onMachineDeactivated() {
		if(this.worldObj.isRemote) { return; }

		int metadata = this.getBlockMetadata();
		if(BlockReactorPart.isController(metadata)) {
			this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, BlockReactorPart.CONTROLLER_IDLE, 2);
		}
	}

	// IPacketReceiver
	
	// Networking

	@Override
	protected void formatDescriptionPacket(NBTTagCompound packetData) {
		super.formatDescriptionPacket(packetData);
		// TODO: this.
	}
	
	@Override
	protected void decodeDescriptionPacket(NBTTagCompound packetData) {
		super.decodeDescriptionPacket(packetData);
		// TODO: This.
	}

	// NBT - Save/Load
	/**
	 * Reads a tile entity from NBT.
	 */
	@Override
	public void readFromNBT(NBTTagCompound par1NBTTagCompound)
	{
		super.readFromNBT(par1NBTTagCompound);
	}

	/**
	 * Writes a tile entity to NBT.
	 */
	@Override
	public void writeToNBT(NBTTagCompound par1NBTTagCompound)
	{
		super.writeToNBT(par1NBTTagCompound);
	}

	///// Network communication

	// TODO: Fix this. Communication with the controller should...
	public void onNetworkPacket(int packetType, DataInputStream data) {
		if(!this.isConnected()) {
			// TODO: Log this.
			return;
		}
		
		/// Client->Server packets
		
		if(packetType == Packets.ReactorControllerButton) {
			Class decodeAs[] = { String.class, Boolean.class };
			Object[] decodedData = PacketWrapper.readPacketData(data, decodeAs);
			String buttonName = (String) decodedData[0];
			boolean newValue = (Boolean) decodedData[1];
			
			if(buttonName.equals("activate")) {
				getReactorController().setActive(newValue);
			}
		}
		
		/// Server->Client packets
		
		if(packetType == Packets.ReactorControllerFullUpdate) {
			Class decodeAs[] = { Boolean.class, Double.class };
			Object[] decodedData = PacketWrapper.readPacketData(data, decodeAs);
			boolean active = (Boolean) decodedData[0];
			double heat = (Double) decodedData[1];
			
			getReactorController().setActive(active);
			getReactorController().setHeat(heat);
		}
		
		if(packetType == Packets.ReactorControllerTickUpdate) {
			Class decodeAs[] = { Double.class };
			Object[] decodedData = PacketWrapper.readPacketData(data, decodeAs);
			double heat = (Double) decodedData[0];
			getReactorController().setHeat(heat);
		}
	}

	@Override
	public MultiblockControllerBase getNewMultiblockControllerObject() {
		return new MultiblockReactor(this.worldObj);
	}
	
	private void setCasingMetadataBasedOnWorldPosition() {
		CoordTriplet minCoord = this.getMultiblockController().getMinimumCoord();
		CoordTriplet maxCoord = this.getMultiblockController().getMaximumCoord();
		
		int extremes = 0;
		boolean xExtreme, yExtreme, zExtreme;
		xExtreme = yExtreme = zExtreme = false;

		TileEntity te;
		if(xCoord == minCoord.x) { extremes++; xExtreme = true; }
		if(yCoord == minCoord.y) { extremes++; yExtreme = true; }
		if(zCoord == minCoord.z) { extremes++; zExtreme = true; }
		
		if(xCoord == maxCoord.x) { extremes++; xExtreme = true; }
		if(yCoord == maxCoord.y) { extremes++; yExtreme = true; }
		if(zCoord == maxCoord.z) { extremes++; zExtreme = true; }
		
		if(extremes == 3) {
			// Corner
			this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, BlockReactorPart.CASING_CORNER, 2);
		}
		else if(extremes == 2) {
			if(!xExtreme) {
				// Y/Z - must be east/west
				this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, BlockReactorPart.CASING_EASTWEST, 2);
			}
			else if(!zExtreme) {
				// X/Y - must be north-south
				this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, BlockReactorPart.CASING_NORTHSOUTH, 2);
			}
			else {
				// Not a y-extreme, must be vertical
				this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, BlockReactorPart.CASING_VERTICAL, 2);
			}						
		}
		else if(extremes == 1) {
			this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, BlockReactorPart.CASING_CENTER, 2);
		}
		else {
			// This shouldn't happen.
			this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, BlockReactorPart.CASING_METADATA_BASE, 2);
		}		
	}

	/**
	 * @return The Container object for use by the GUI. Null if there isn't any.
	 */
	public Object getContainer() {
		if(!this.isConnected()) {
			return null;
		}
		
		int metadata = this.worldObj.getBlockMetadata(this.xCoord, this.yCoord, this.zCoord);
		if(BlockReactorPart.isController(metadata)) {
			return new ContainerReactorController(this);
		}
		return null;
	}

	public Object getGuiElement() {
		if(!this.isConnected()) {
			System.out.println("getGuiElement - null (no connection)");
			return null;
		}
		
		int metadata = this.worldObj.getBlockMetadata(this.xCoord, this.yCoord, this.zCoord);
		if(BlockReactorPart.isController(metadata)) {
			return new GuiReactorStatus(new ContainerReactorController(this), this);
		}
		return null;
	}
}

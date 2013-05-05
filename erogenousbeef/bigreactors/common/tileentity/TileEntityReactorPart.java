package erogenousbeef.bigreactors.common.tileentity;

import java.io.DataInputStream;

import com.google.common.io.ByteArrayDataInput;

import erogenousbeef.bigreactors.api.IRadiationModerator;
import erogenousbeef.bigreactors.api.IRadiationPacket;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.block.BlockReactorPart;
import erogenousbeef.bigreactors.common.multiblock.MultiblockReactor;
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
		if(BlockReactorPart.isCasing(this.blockMetadata)) {
			this.setCasingMetadataBasedOnWorldPosition();
		}
		else if(BlockReactorPart.isControlRod(blockMetadata)) {
			// Control rods start inserted.
			this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, BlockReactorPart.CONTROLROD_METADATA_BASE, 2);
		}
		else if(BlockReactorPart.isController(blockMetadata)) {
			// Controllers start idle
			this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, BlockReactorPart.CONTROLLER_IDLE, 2);
		}
	}

	@Override
	public void onMachineBroken() {
		if(BlockReactorPart.isCasing(this.blockMetadata)) {
			this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, BlockReactorPart.CASING_METADATA_BASE, 2);
		}
		else if(BlockReactorPart.isControlRod(blockMetadata)) {
			this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, BlockReactorPart.CONTROLROD_METADATA_BASE, 2);
		}
		else if(BlockReactorPart.isController(blockMetadata)) {
			// Controllers start idle
			this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, BlockReactorPart.CONTROLLER_METADATA_BASE, 2);
		}
	}

	@Override
	public void onMachineActivated() {
		if(BlockReactorPart.isController(blockMetadata)) {
			this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, BlockReactorPart.CONTROLLER_ACTIVE, 2);
		}
		
	}

	@Override
	public void onMachineDeactivated() {
		if(BlockReactorPart.isController(blockMetadata)) {
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
		
		if(packetType == Packets.ReactorControllerButton) {
			if(!this.isMultiblockSaveDelegate()) {
				// TODO: log this.
				return;
			}

			Class decodeAs[] = { String.class, Boolean.class };
			Object[] decodedData = PacketWrapper.readPacketData(data, decodeAs);
			String buttonName = (String) decodedData[0];
			boolean newValue = (Boolean) decodedData[1];
			
			if(buttonName == "activate") {
				getReactorController().setActive(newValue);
			}
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
}

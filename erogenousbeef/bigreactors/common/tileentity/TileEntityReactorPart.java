package erogenousbeef.bigreactors.common.tileentity;

import java.io.DataInputStream;

import com.google.common.io.ByteArrayDataInput;

import erogenousbeef.bigreactors.api.HeatPulse;
import erogenousbeef.bigreactors.api.IHeatEntity;
import erogenousbeef.bigreactors.api.IRadiationModerator;
import erogenousbeef.bigreactors.api.IRadiationPulse;
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
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

public class TileEntityReactorPart extends MultiblockTileEntityBase implements IRadiationModerator, IMultiblockPart, IHeatEntity {

	public TileEntityReactorPart() {
		super();
	}

	public MultiblockReactor getReactorController() { return (MultiblockReactor)this.getMultiblockController(); }
	
	@Override
	public boolean canUpdate() { return false; }

	@Override
	public void receiveRadiationPulse(IRadiationPulse radiation) {
		double newHeat = radiation.getSlowRadiation() * 0.75;
		
		// Convert 10% of newly-gained heat to energy (thermocouple or something)
		radiation.addPower((int)(newHeat*0.1));
		newHeat *= 0.9;
		radiation.changeHeat(newHeat);
		
		// Slow radiation is all lost now
		radiation.setSlowRadiation(0);
		
		// And zero out the TTL so evaluation force-stops
		radiation.setTimeToLive(0);
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
			Class decodeAs[] = { Boolean.class, Double.class, Integer.class };
			Object[] decodedData = PacketWrapper.readPacketData(data, decodeAs);
			boolean active = (Boolean) decodedData[0];
			double heat = (Double) decodedData[1];
			int storedEnergy = (Integer) decodedData[2];

			getReactorController().setActive(active);
			getReactorController().setHeat(heat);
			getReactorController().setStoredEnergy(storedEnergy);
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
	public Object getContainer(InventoryPlayer inventoryPlayer) {
		if(!this.isConnected()) {
			return null;
		}
		
		int metadata = this.worldObj.getBlockMetadata(this.xCoord, this.yCoord, this.zCoord);
		if(BlockReactorPart.isController(metadata)) {
			return new ContainerReactorController(this, inventoryPlayer.player);
		}
		return null;
	}

	public Object getGuiElement(InventoryPlayer inventoryPlayer) {
		if(!this.isConnected()) {
			System.out.println("getGuiElement - null (no connection)");
			return null;
		}
		
		int metadata = this.worldObj.getBlockMetadata(this.xCoord, this.yCoord, this.zCoord);
		if(BlockReactorPart.isController(metadata)) {
			return new GuiReactorStatus(new ContainerReactorController(this, inventoryPlayer.player), this);
		}
		return null;
	}

	// Only refresh if we're switching functionality
	// Warning: dragonz!
	@Override
    public boolean shouldRefresh(int oldID, int newID, int oldMeta, int newMeta, World world, int x, int y, int z)
    {
		if(oldID != newID) {
			return true;
		}
		if(BlockReactorPart.isCasing(oldMeta) && BlockReactorPart.isCasing(newMeta)) {
			return false;
		}
		if(BlockReactorPart.isAccessPort(oldMeta) && BlockReactorPart.isAccessPort(newMeta)) {
			return false;
		}
		if(BlockReactorPart.isController(oldMeta) && BlockReactorPart.isController(newMeta)) {
			return false;
		}
		if(BlockReactorPart.isControlRod(oldMeta) && BlockReactorPart.isControlRod(newMeta)) {
			return false;
		}
		if(BlockReactorPart.isPowerTap(oldMeta) && BlockReactorPart.isPowerTap(newMeta)) {
			return false;
		}
		return true;
    }

	public void addHeat(int heatProduced) {
		if(isConnected()) {
			getReactorController().addLatentHeat(heatProduced);
		}
	}

	// IHeatEntity
	
	@Override
	public double getHeat() {
		if(!this.isConnected()) { return 0; }
		return getReactorController().getHeat();
	}

	@Override
	public double onAbsorbHeat(IHeatEntity source, HeatPulse pulse, int faces) {
		double deltaTemp = source.getHeat() - getHeat();
		// If the source is cooler than the reactor, then do nothing
		if(deltaTemp <= 0.0) {
			return 0.0;
		}

		double heatToAbsorb = deltaTemp * 0.05 * getThermalConductivity() * (1.0/(double)faces);

		pulse.powerProduced += (int)(heatToAbsorb*0.1);
		pulse.heatChange += heatToAbsorb * 0.9;

		return heatToAbsorb;
	}

	@Override
	public HeatPulse onRadiateHeat(double ambientHeat) {
		// Ignore. Casing does not re-radiate heat on its own.
		return null;
	}

	@Override
	public double getThermalConductivity() {
		return IHeatEntity.conductivityIron;
	}
}

package erogenousbeef.bigreactors.common.tileentity;

import java.io.DataInputStream;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import erogenousbeef.bigreactors.api.HeatPulse;
import erogenousbeef.bigreactors.api.IHeatEntity;
import erogenousbeef.bigreactors.api.IRadiationModerator;
import erogenousbeef.bigreactors.api.IRadiationPulse;
import erogenousbeef.bigreactors.client.gui.GuiReactorStatus;
import erogenousbeef.bigreactors.common.block.BlockReactorPart;
import erogenousbeef.bigreactors.common.multiblock.MultiblockReactor;
import erogenousbeef.bigreactors.gui.container.ContainerReactorController;
import erogenousbeef.bigreactors.net.PacketWrapper;
import erogenousbeef.bigreactors.net.Packets;
import erogenousbeef.core.common.CoordTriplet;
import erogenousbeef.core.multiblock.IMultiblockPart;
import erogenousbeef.core.multiblock.MultiblockControllerBase;
import erogenousbeef.core.multiblock.MultiblockTileEntityBase;

public class TileEntityReactorPart extends MultiblockTileEntityBase implements IRadiationModerator, IMultiblockPart, IHeatEntity {

	public TileEntityReactorPart() {
		super();
	}

	public MultiblockReactor getReactorController() { return (MultiblockReactor)this.getMultiblockController(); }
	
	@Override
	public boolean canUpdate() { return false; }

	@Override
	public boolean isGoodForFrame() {
		int metadata = this.worldObj.getBlockMetadata(this.xCoord, this.yCoord, this.zCoord);
		if(BlockReactorPart.isCasing(metadata)) { return true; }
		else { return false; }
	}

	@Override
	public boolean isGoodForSides() {
		return true;
	}

	@Override
	public boolean isGoodForTop() {
		int metadata = this.worldObj.getBlockMetadata(this.xCoord, this.yCoord, this.zCoord);
		if(BlockReactorPart.isCasing(metadata)) {
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
	public void onMachineAssembled(MultiblockControllerBase multiblockController) {
		if(this.worldObj.isRemote) { return; }
		if(multiblockController == null) {
			throw new IllegalArgumentException("Being assembled into a null controller. This should never happen. Please report this stacktrace to http://github.com/ErogenousBeef/BigReactors/");
		}

		// Autoheal, for issue #65.
		if(this.getMultiblockController() == null) {
			FMLLog.warning("Reactor part at (%d, %d, %d) is being assembled without being attached to a reactor. Attempting to auto-heal. Fully destroying and re-building this reactor is recommended if errors persist.", xCoord, yCoord, zCoord);
			this.onAttached(multiblockController);
		}
		
		int metadata = this.getBlockMetadata();
		if(BlockReactorPart.isCasing(metadata)) {
			this.setCasingMetadataBasedOnWorldPosition();
		}
		else if(BlockReactorPart.isController(metadata)) {
			// This is called during world loading as well, so controllers can start active.
			if(!this.getReactorController().isActive()) {
				this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, BlockReactorPart.CONTROLLER_IDLE, 2);				
			}
			else {
				this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, BlockReactorPart.CONTROLLER_ACTIVE, 2);				
			}
		}
	}

	@Override
	public void onMachineBroken() {
		if(this.worldObj.isRemote) { return; }
		
		int metadata = this.getBlockMetadata();
		if(BlockReactorPart.isCasing(metadata)) {
			this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, BlockReactorPart.CASING_METADATA_BASE, 2);
		}
		else if(BlockReactorPart.isController(metadata)) {
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
	protected void encodeDescriptionPacket(NBTTagCompound packetData) {
		super.encodeDescriptionPacket(packetData);
	}
	
	@Override
	protected void decodeDescriptionPacket(NBTTagCompound packetData) {
		super.decodeDescriptionPacket(packetData);
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
			else if(buttonName.equals("ejectWaste")) {
				getReactorController().ejectWaste();
			}
		}
		
		if(packetType == Packets.ReactorWasteEjectionSettingUpdate) {
			getReactorController().changeWasteEjection();
		}
		
		/// Server->Client packets
		
		if(packetType == Packets.ReactorControllerFullUpdate) {
			Class decodeAs[] = { Boolean.class, Float.class, Float.class, Float.class, Integer.class};
			Object[] decodedData = PacketWrapper.readPacketData(data, decodeAs);
			boolean active = (Boolean) decodedData[0];
			float heat = (Float) decodedData[1];
			float storedEnergy = (Float) decodedData[2];
			float energyGeneratedLastTick = (Float) decodedData[3];
			int fuelConsumedLastTick = (Integer) decodedData[4];

			MultiblockReactor reactor = getReactorController();
			reactor.setActive(active);
			reactor.setHeat(heat);
			reactor.setStoredEnergy(storedEnergy);
			reactor.setEnergyGeneratedLastTick(energyGeneratedLastTick);
			reactor.setFuelConsumedLastTick(fuelConsumedLastTick);
		}		
	}

	@Override
	public MultiblockControllerBase createNewMultiblock() {
		return new MultiblockReactor(this.worldObj);
	}
	
	@Override
	public Class<? extends MultiblockControllerBase> getMultiblockControllerType() { return MultiblockReactor.class; }
	
	private void setCasingMetadataBasedOnWorldPosition() {
		MultiblockControllerBase controller = this.getMultiblockController();
		assert(controller != null);
		CoordTriplet minCoord = controller.getMinimumCoord();
		CoordTriplet maxCoord = controller.getMaximumCoord();
		
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

	@SideOnly(Side.CLIENT)
	public Object getGuiElement(InventoryPlayer inventoryPlayer) {
		if(!this.isConnected()) {
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
		if(BlockReactorPart.isPowerTap(oldMeta) && BlockReactorPart.isPowerTap(newMeta)) {
			return false;
		}
		if(BlockReactorPart.isRedNetPort(oldMeta) && BlockReactorPart.isPowerTap(newMeta)) {
			return false;
		}
		return true;
    }

	// IRadiationModerator
	@Override
	public void receiveRadiationPulse(IRadiationPulse radiation) {
		float freePower = radiation.getSlowRadiation() * 0.25f;
		
		// Convert 25% of incident radiation to power, for balance reasons.
		radiation.addPower(freePower);
		
		// Slow radiation is all lost now
		radiation.setSlowRadiation(0);
		
		// And zero out the TTL so evaluation force-stops
		radiation.setTimeToLive(0);
	}
	
	// IHeatEntity
	@Override
	public float getHeat() {
		if(!this.isConnected()) { return 0f; }
		return getReactorController().getHeat();
	}

	@Override
	public float onAbsorbHeat(IHeatEntity source, HeatPulse pulse, int faces, int contactArea) {
		float deltaTemp = source.getHeat() - getHeat();
		// If the source is cooler than the reactor, then do nothing
		if(deltaTemp <= 0.0f) {
			return 0.0f;
		}

		float heatToAbsorb = deltaTemp * getThermalConductivity() * (1.0f/(float)faces) * contactArea;

		pulse.heatChange += heatToAbsorb;

		return heatToAbsorb;
	}

	@Override
	public HeatPulse onRadiateHeat(float ambientHeat) {
		// Ignore. Casing does not re-radiate heat on its own.
		return null;
	}

	@Override
	public float getThermalConductivity() {
		return IHeatEntity.conductivityIron;
	}
}

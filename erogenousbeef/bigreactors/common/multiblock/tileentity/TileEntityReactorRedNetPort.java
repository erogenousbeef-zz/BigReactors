package erogenousbeef.bigreactors.common.multiblock.tileentity;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;

import net.minecraft.block.Block;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import powercrystals.minefactoryreloaded.api.rednet.IConnectableRedNet;
import powercrystals.minefactoryreloaded.api.rednet.IRedNetNetworkContainer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import erogenousbeef.bigreactors.client.gui.GuiReactorRedNetPort;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.multiblock.MultiblockReactor;
import erogenousbeef.bigreactors.common.multiblock.block.BlockReactorPart;
import erogenousbeef.bigreactors.common.multiblock.interfaces.ITickableMultiblockPart;
import erogenousbeef.bigreactors.gui.container.ContainerBasic;
import erogenousbeef.core.common.CoordTriplet;
import erogenousbeef.core.multiblock.MultiblockControllerBase;

public class TileEntityReactorRedNetPort extends TileEntityReactorPart implements ITickableMultiblockPart {

	public enum CircuitType {
		DISABLED,
		inputActive, 				// Input: reactor on/off
		inputSetControlRod, 		// Input: control rod insertion (0-100)
		inputEjectWaste,			// Input: eject waste from the reactor

		outputFuelTemperature,		// Output: Temperature of the reactor fuel
		outputCasingTemperature,	// Output: Temperature of the reactor casing
		outputFuelMix, 		// Output: Fuel mix, % of contents that is fuel (0-100, 100 = 100% fuel)
		outputFuelAmount, 	// Output: Fuel amount in a control rod, raw value, (0-4*height)
		outputWasteAmount, 	// Output: Waste amount in a control rod, raw value, (0-4*height)
		outputEnergyAmount // Output: Energy in the reactor's buffer, percentile (0-100, 100 = 100% full)
	}

	protected final static int minInputEnumValue = CircuitType.inputActive.ordinal();
	protected final static int maxInputEnumValue = CircuitType.inputEjectWaste.ordinal();
	protected final static int minOutputEnumValue = CircuitType.outputFuelTemperature.ordinal();
	protected final static int maxOutputEnumValue = CircuitType.outputEnergyAmount.ordinal();

	protected CircuitType[] channelCircuitTypes;
	protected CoordTriplet[] coordMappings;
	protected boolean[] inputActivatesOnPulse;
	protected int[] oldValue;

	public final static int numChannels = 16;
	
	IRedNetNetworkContainer redNetwork;
	IConnectableRedNet redNetConnectable;

	int ticksSinceLastUpdate;
	
	public TileEntityReactorRedNetPort() {
		super();
		
		channelCircuitTypes = new CircuitType[numChannels];
		coordMappings = new CoordTriplet[numChannels];
		inputActivatesOnPulse = new boolean[numChannels];
		oldValue = new int[numChannels];

		for(int i = 0; i < numChannels; i++) {
			channelCircuitTypes[i] = CircuitType.DISABLED;
			coordMappings[i] = null;
			inputActivatesOnPulse[i] = false;
			oldValue[i] = 0;
		}
		
		redNetwork = null;
		redNetConnectable = null;

		ticksSinceLastUpdate = 0;
	}
	
	// IMultiblockPart
	@Override
	public void onAttached(MultiblockControllerBase newController) {
		super.onAttached(newController);

		if(this.worldObj.isRemote) { return; } 
		
		checkForConnections(this.worldObj, xCoord, yCoord, zCoord);
	}
	
	@Override
	public void onMachineAssembled(MultiblockControllerBase multiblockController) {
		super.onMachineAssembled(multiblockController);

		if(this.worldObj.isRemote) { return; } 
		
		checkForConnections(this.worldObj, xCoord, yCoord, zCoord);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object getGuiElement(InventoryPlayer inventoryPlayer) {
		return new GuiReactorRedNetPort(new ContainerBasic(), this);
	}
	
	@Override
	public Object getContainer(InventoryPlayer inventoryPlayer) {
		return new ContainerBasic();
	}

	@Override
	public void writeToNBT(NBTTagCompound par1NBTTagCompound)
	{
		super.writeToNBT(par1NBTTagCompound);
		encodeSettings(par1NBTTagCompound);
	}
	
	@Override
	protected void encodeDescriptionPacket(NBTTagCompound packetData) {
		super.encodeDescriptionPacket(packetData);
		encodeSettings(packetData);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound par1NBTTagCompound)
	{
		super.readFromNBT(par1NBTTagCompound);
		decodeSettings(par1NBTTagCompound);
	}
	
	@Override
	protected void decodeDescriptionPacket(NBTTagCompound packetData) {
		super.decodeDescriptionPacket(packetData);
		decodeSettings(packetData);
	}

	// RedNet API
	public int[] getOutputValues() {
		int[] outputs = new int[numChannels];
		for(int i = 0; i < numChannels; i++) {
			outputs[i] = getValueForChannel(i);
		}
		
		return outputs;
	}
	
	public int getValueForChannel(int channel) {
		if(channel < 0 || channel >= numChannels) { return 0; }
		
		if(!this.isConnected()) { return 0; }
		
		TileEntity te = null;
		
		switch(channelCircuitTypes[channel]) {
		case outputFuelTemperature:
			return (int)Math.floor(getReactorController().getFuelHeat());
		case outputCasingTemperature:
			return (int)Math.floor(getReactorController().getReactorHeat());
		case outputFuelMix:
			MultiblockReactor controller = getReactorController();
			return (int)Math.floor(((float)controller.getFuelAmount() / (float)controller.getCapacity())*100.0f);
		case outputFuelAmount:
			return getReactorController().getFuelAmount();
		case outputWasteAmount:
			return getReactorController().getWasteAmount();
		case outputEnergyAmount:
			int energyStored, energyTotal;
			MultiblockReactor reactor = this.getReactorController();
			if(reactor != null) {
				return reactor.getEnergyStoredPercentage();
			}
			return 0;
		default:
			return 0;
		}
	}
	
	public void onInputValuesChanged(int[] newValues) {
		for(int i = 0; i < newValues.length; i++) {
			onInputValueChanged(i, newValues[i]);
		}
	}
	
	public void onInputValueChanged(int channel, int newValue) {
		if(channel < 0 || channel >= numChannels) { return; }
		CircuitType type = channelCircuitTypes[channel];
		if(!isInput(type)) { return; }
		if(!this.isConnected()) { return; }
		
		if(newValue == oldValue[channel]) { return; }
		boolean isPulse = (oldValue[channel] == 0 && newValue != 0);
		
		MultiblockReactor reactor = null;
		switch(type) {
		case inputActive:
			reactor = getReactorController();
			if(inputActivatesOnPulse[channel]) {
				if(isPulse) {
					reactor.setActive(!reactor.isActive());
				}
			}
			else {
				boolean newActive = newValue != 0;
				if(newActive != reactor.isActive()) {
					reactor.setActive(newActive);
				}
			}
			break;
		case inputSetControlRod:
			// This doesn't make sense for pulsing
			if(coordMappings[channel] != null) {
				setControlRodInsertion(channel, coordMappings[channel], newValue);
			}
			else {
				reactor = getReactorController();
				reactor.setAllControlRodInsertionValues(newValue);
			}
			break;
		case inputEjectWaste:
			// This only makes sense for pulsing
			if(isPulse) {
				reactor = getReactorController();
				reactor.ejectWaste(false);
			}
		default:
			break;
		}
		
		oldValue[channel] = newValue;
	}
	
	// Public RedNet helpers for GUI & updates
	public void onNeighborBlockChange(World world, int x, int y, int z, int neighborBlockID) {
		checkForConnections(world, x, y, z);
	}
	
	/**
	 * Updates the connected RedNet network, if there is one.
	 * Will only send one update per N ticks, where N is a configurable setting.
	 */
	public void onMultiblockServerTick() {
		if(!this.isConnected()) { return; }

		ticksSinceLastUpdate++;
		if(ticksSinceLastUpdate < BigReactors.ticksPerRedstoneUpdate) { return; }

		ForgeDirection out = getOutwardsDir();
		
		if(redNetwork != null) {
				redNetwork.updateNetwork(worldObj, xCoord+out.offsetX, yCoord+out.offsetY, zCoord+out.offsetZ);
		}
		
		if(redNetConnectable != null) {
			redNetConnectable.onInputsChanged(worldObj, xCoord+out.offsetX, yCoord+out.offsetY, zCoord+out.offsetZ, out.getOpposite(), getOutputValues());
		}

		ticksSinceLastUpdate = 0;
	}

	public CircuitType getChannelCircuitType(int channel) {
		if(channel < 0 || channel >= numChannels) { return CircuitType.DISABLED; }
		return channelCircuitTypes[channel];
	}

	public CoordTriplet getMappedCoord(int channel) {
		return this.coordMappings[channel];
	}
	
	public boolean isInputActivatedOnPulse(int channel) {
		return this.inputActivatesOnPulse[channel];
	}

	// RedNet helper methods
	protected void clearChannel(int channel) {
		channelCircuitTypes[channel] = CircuitType.DISABLED;
		coordMappings[channel] = null;
		inputActivatesOnPulse[channel] = false;
		oldValue[channel] = 0;
	}

	protected TileEntity getMappedTileEntity(int channel) {
		if(channel < 0 || channel >= numChannels) { return null; }
		if(coordMappings[channel] == null) { return null; }
		
		CoordTriplet coord = coordMappings[channel];
		
		if(coord == null) { return null; }
		if(!this.worldObj.checkChunksExist(coord.x, coord.y, coord.z, coord.x, coord.y, coord.z)) {
			return null;
		}
		
		return this.worldObj.getBlockTileEntity(coord.x, coord.y, coord.z);
	}
	
	protected void setControlRodInsertion(int channel, CoordTriplet coordTriplet, int newValue) {
		if(!this.isConnected()) { return; }
		if(!this.worldObj.checkChunksExist(coordTriplet.x, coordTriplet.y, coordTriplet.z,
											coordTriplet.x, coordTriplet.y, coordTriplet.z)) {
			return;
		}
		
		TileEntity te = this.worldObj.getBlockTileEntity(coordTriplet.x, coordTriplet.y, coordTriplet.z);
		if(te instanceof TileEntityReactorControlRod) {
			((TileEntityReactorControlRod)te).setControlRodInsertion((short)newValue);
		}
		else {
			clearChannel(channel);
		}
	}
	
	protected NBTTagCompound encodeSetting(int channel) {
		NBTTagCompound entry = new NBTTagCompound();
		
		entry.setInteger("channel", channel);
		entry.setInteger("setting", this.channelCircuitTypes[channel].ordinal());
		if(isInput(this.channelCircuitTypes[channel]) && canBeToggledBetweenPulseAndNormal(this.channelCircuitTypes[channel])) {
			entry.setBoolean("pulse", this.inputActivatesOnPulse[channel]);
		}
		if( circuitTypeHasSubSetting(this.channelCircuitTypes[channel]) ) {
			CoordTriplet coord = this.coordMappings[channel];
			if(coord != null) {
				entry.setInteger("x", coord.x);
				entry.setInteger("y", coord.y);
				entry.setInteger("z", coord.z);
			}
		}
		
		return entry;
	}

	protected void decodeSetting(NBTTagCompound settingTag) {
		int channel = settingTag.getInteger("channel");
		int settingIdx = settingTag.getInteger("setting");
		
		clearChannel(channel);
		
		channelCircuitTypes[channel] = CircuitType.values()[settingIdx];
		
		if(isInput(this.channelCircuitTypes[channel]) && canBeToggledBetweenPulseAndNormal(this.channelCircuitTypes[channel])) {
			inputActivatesOnPulse[channel] = settingTag.getBoolean("pulse");
		}

		if( circuitTypeHasSubSetting(channelCircuitTypes[channel]) ) {
			if(settingTag.hasKey("x")) {
				int x, y, z;
				x = settingTag.getInteger("x");
				y = settingTag.getInteger("y");
				z = settingTag.getInteger("z");
				coordMappings[channel] = new CoordTriplet(x, y, z);
			}
		}
	}

	// Decodes setting changes from an update packet
	public void decodeSettings(DataInputStream data, boolean doValidation) throws IOException {
		int channel;
		for(;;) {
			try {
				channel = data.readInt();
				CircuitType newSetting = CircuitType.values()[ data.readInt() ];
				clearChannel(channel);

				channelCircuitTypes[channel] = newSetting;

				if(isInput(channelCircuitTypes[channel]) && canBeToggledBetweenPulseAndNormal(channelCircuitTypes[channel])) {
					inputActivatesOnPulse[channel] = data.readBoolean();
				}
				
				if(circuitTypeHasSubSetting(newSetting)) {
					boolean hasSubSettingData = data.readBoolean();
					CoordTriplet coord = null;
					if(hasSubSettingData) {
						coord = new CoordTriplet( data.readInt(), data.readInt(), data.readInt() );					
					}
					
					if(doValidation) {
						if(coord != null) {
							TileEntity te = worldObj.getBlockTileEntity(coord.x, coord.y, coord.z);
							if(!(te instanceof TileEntityReactorControlRod)) {
								throw new IOException("Invalid TileEntity for RedNet Port settings at " + coord.toString());
							}
						}
					}
					
					coordMappings[channel] = coord;
				}
			}
			catch(EOFException e) {
				// Expected, halt execution
				// And send the update to all nearby clients
				worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
				return;
			}
		}
	}
	
	// Helpers
	protected void encodeSettings(NBTTagCompound destination) {
		NBTTagList tagArray = new NBTTagList();
		
		for(int i = 0; i < numChannels; i++) {
			tagArray.appendTag(encodeSetting(i));
		}
		
		destination.setTag("redNetConfig", tagArray);
	}
	
	protected void decodeSettings(NBTTagCompound source) {
		NBTTagList tagArray = source.getTagList("redNetConfig");
		for(int i = 0; i < tagArray.tagCount(); i++) {
			decodeSetting( (NBTTagCompound)tagArray.tagAt(i) );
		}
	}
	
	/**
	 * Check for a world connection, if we're assembled.
	 * @param world
	 * @param x
	 * @param y
	 * @param z
	 */
	protected void checkForConnections(World world, int x, int y, int z) {
		ForgeDirection out = getOutwardsDir();

		if(out == ForgeDirection.UNKNOWN) {
			redNetwork = null;
			redNetConnectable = null;
		}
		else {
			// Check for rednet connections nearby
			redNetwork = null;
			redNetConnectable = null;

			Block b = Block.blocksList[worldObj.getBlockId(x + out.offsetX, y + out.offsetY, z + out.offsetZ)];
			if(!(b instanceof BlockReactorPart)) {
				if(b instanceof IRedNetNetworkContainer) {
					redNetwork = (IRedNetNetworkContainer)b;
				}
				else if(b instanceof IConnectableRedNet) {
					redNetConnectable = (IConnectableRedNet)b; 
				}
			}
		}
	}

	// Static Helpers
	public static boolean circuitTypeHasSubSetting(TileEntityReactorRedNetPort.CircuitType circuitType) {
		return circuitType == CircuitType.inputSetControlRod;
	}

	public static boolean canBeToggledBetweenPulseAndNormal(CircuitType circuitType) {
		return circuitType == CircuitType.inputActive;
	}

	public static boolean isInput(CircuitType type) { return type.ordinal() >= minInputEnumValue && type.ordinal() <= maxInputEnumValue; }
	public static boolean isOutput(CircuitType type) { return type.ordinal() >= minOutputEnumValue && type.ordinal() <= maxOutputEnumValue; }
}

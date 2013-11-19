package erogenousbeef.bigreactors.common.tileentity;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import erogenousbeef.bigreactors.client.gui.GuiReactorRedNetPort;
import erogenousbeef.bigreactors.common.multiblock.MultiblockReactor;
import erogenousbeef.bigreactors.gui.container.ContainerReactorRedNetPort;
import erogenousbeef.core.common.CoordTriplet;

public class TileEntityReactorRedNetPort extends TileEntityReactorPart {

	public enum CircuitType {
		DISABLED,
		inputActive, 				// Input: reactor on/off
		inputSetControlRod, 		// Input: control rod insertion (0-100)

		outputTemperature,				// Output: Temperature of the reactor
		outputFuelMix, 		// Output: Fuel mix, % of contents that is fuel (0-100, 100 = 100% fuel)
		outputFuelAmount, 	// Output: Fuel amount in a control rod, raw value, (0-4*height)
		outputWasteAmount 	// Output: Waste amount in a control rod, raw value, (0-4*height)
		
	}

	protected final static int minInputEnumValue = CircuitType.inputActive.ordinal();
	protected final static int maxInputEnumValue = CircuitType.inputSetControlRod.ordinal();
	protected final static int minOutputEnumValue = CircuitType.outputTemperature.ordinal();
	protected final static int maxOutputEnumValue = CircuitType.outputWasteAmount.ordinal();

	protected static boolean isInput(CircuitType type) { return type.ordinal() >= minInputEnumValue && type.ordinal() <= maxInputEnumValue; }
	protected static boolean isOutput(CircuitType type) { return type.ordinal() >= minOutputEnumValue && type.ordinal() <= maxOutputEnumValue; }
	
	protected CircuitType[] channelCircuitTypes;
	protected CoordTriplet[] coordMappings;
	public final static int numChannels = 16;
	
	public TileEntityReactorRedNetPort() {
		super();
		
		channelCircuitTypes = new CircuitType[numChannels];
		coordMappings = new CoordTriplet[numChannels];

		for(int i = 0; i < numChannels; i++) {
			channelCircuitTypes[i] = CircuitType.DISABLED;
			coordMappings[i] = null;
		}
	
	}
	
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
		case outputTemperature:
			te = getMappedTileEntity(channel);
			if(te instanceof TileEntityReactorControlRod) {
				return (int)Math.floor(((TileEntityReactorControlRod)te).getHeat());
			}
			else {
				return (int)Math.floor(getReactorController().getHeat());
			}
		case outputFuelMix:
			te = getMappedTileEntity(channel);
			if(te instanceof TileEntityReactorControlRod) {
				TileEntityReactorControlRod cr = (TileEntityReactorControlRod)te;
				if(cr.getTotalContainedAmount() <= 0) { return 0; }
				else { 
					return (int)Math.floor(((float)cr.getFuelAmount() / (float)cr.getTotalContainedAmount())*100.0f);
				}
			}
			else {
				clearChannel(channel);
				return 0;
			}
		case outputFuelAmount:
			te = getMappedTileEntity(channel);
			if(te instanceof TileEntityReactorControlRod) {
				return ((TileEntityReactorControlRod)te).getFuelAmount();
			}
			else {
				clearChannel(channel);
				return 0;
			}
		case outputWasteAmount:
			te = getMappedTileEntity(channel);
			if(te instanceof TileEntityReactorControlRod) {
				return ((TileEntityReactorControlRod)te).getWasteAmount();
			}
			else {
				clearChannel(channel);
				return 0;
			}
		default:
			return 0;
		}
	}
	
	private void clearChannel(int channel) {
		channelCircuitTypes[channel] = CircuitType.DISABLED;
		coordMappings[channel] = null;
	}

	public TileEntity getMappedTileEntity(int channel) {
		if(channel < 0 || channel >= numChannels) { return null; }
		if(coordMappings[channel] == null) { return null; }
		
		CoordTriplet coord = coordMappings[channel];
		
		if(coord == null) { return null; }
		if(!this.worldObj.checkChunksExist(coord.x, coord.y, coord.z, coord.x, coord.y, coord.z)) {
			return null;
		}
		
		return this.worldObj.getBlockTileEntity(coord.x, coord.y, coord.z);
	}
	
	public void setMappedTileEntity(int channel, TileEntity te) {
		if(channel < 0 || channel >= numChannels) { return; }
		
		CircuitType circuitType = channelCircuitTypes[channel]; 
		if(circuitType == CircuitType.outputTemperature ||
				circuitType == CircuitType.outputFuelMix ||
				circuitType == CircuitType.outputFuelAmount ||
				circuitType == CircuitType.outputWasteAmount ||
				circuitType == CircuitType.inputSetControlRod) {
			if(te instanceof TileEntityReactorControlRod) {
				coordMappings[channel] = ((TileEntityReactorControlRod) te).getWorldLocation();
			}
		}
	}
	
	public void setChannelCircuitType(int channel, CircuitType type) {
		if(channel < 0 || channel >= numChannels) { return; }
		channelCircuitTypes[channel] = type;
	}
	
	public CircuitType getChannelCircuitType(int channel) {
		if(channel < 0 || channel >= numChannels) { return CircuitType.DISABLED; }
		return channelCircuitTypes[channel];
	}
	
	public void onInputValuesChanged(int[] newValues) {
		for(int i = 0; i < newValues.length; i++) {
			onInputValueChanged(i, newValues[i]);
		}
	}
	
	protected void onInputValueChanged(int channel, int newValue) {
		if(channel < 0 || channel >= numChannels) { return; }
		CircuitType type = channelCircuitTypes[channel];
		if(!isInput(type)) { return; }
		if(!this.isConnected()) { return; }
		
		MultiblockReactor reactor = null;
		switch(type) {
		case inputActive:
			reactor = getReactorController();
			boolean newActive = newValue != 0;
			if(newActive != reactor.isActive()) {
				reactor.setActive(newActive);
			}
			break;
		case inputSetControlRod:
			if(coordMappings[channel] != null) {
				setControlRodInsertion(channel, coordMappings[channel], newValue);
			}
			else {
				reactor = getReactorController();
				reactor.setAllControlRodInsertionValues(newValue);
			}
			break;
		default:
			break;
		}
	}

	private void setControlRodInsertion(int channel, CoordTriplet coordTriplet, int newValue) {
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
	
	public void setMappedCoord(int channel, CoordTriplet mappedCoord) {
		this.coordMappings[channel] = mappedCoord;
	}
	
	public CoordTriplet getMappedCoord(int channel) {
		return this.coordMappings[channel];
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public Object getGuiElement(InventoryPlayer inventoryPlayer) {
		return new GuiReactorRedNetPort(new ContainerReactorRedNetPort(), this);
	}
	
	@Override
	public Object getContainer(InventoryPlayer inventoryPlayer) {
		return new ContainerReactorRedNetPort();
	}
	
	@Override
	protected void formatDescriptionPacket(NBTTagCompound packetData) {
		super.formatDescriptionPacket(packetData);
		
		NBTTagList tagArray = new NBTTagList();
		
		for(int i = 0; i < numChannels; i++) {
			tagArray.appendTag(encodeSetting(i));
		}
		
		packetData.setTag("redNetConfig", tagArray);
	}
	
	@Override
	protected void decodeDescriptionPacket(NBTTagCompound packetData) {
		super.decodeDescriptionPacket(packetData);
		
		NBTTagList tagArray = packetData.getTagList("redNetConfig");
		for(int i = 0; i < tagArray.tagCount(); i++) {
			decodeSetting( (NBTTagCompound)tagArray.tagAt(i) );
		}
	}
	
	protected NBTTagCompound encodeSetting(int channel) {
		NBTTagCompound entry = new NBTTagCompound();
		
		entry.setInteger("channel", channel);
		entry.setInteger("setting", this.channelCircuitTypes[channel].ordinal());
		if( circuitTypeHasSubSetting(this.channelCircuitTypes[channel]) ) {
			CoordTriplet coord = this.coordMappings[channel];
			entry.setInteger("x", coord.x);
			entry.setInteger("y", coord.y);
			entry.setInteger("z", coord.z);
		}
		
		return entry;
	}

	protected void decodeSetting(NBTTagCompound settingTag) {
		int channel = settingTag.getInteger("channel");
		int settingIdx = settingTag.getInteger("setting");
		
		channelCircuitTypes[channel] = CircuitType.values()[settingIdx];
		if( circuitTypeHasSubSetting(channelCircuitTypes[channel]) ) {
			int x, y, z;
			x = settingTag.getInteger("x");
			y = settingTag.getInteger("y");
			z = settingTag.getInteger("z");
			coordMappings[channel] = new CoordTriplet(x, y, z);
		} else {
			coordMappings[channel] = null;
		}
	}
	
	public static boolean circuitTypeHasSubSetting(TileEntityReactorRedNetPort.CircuitType circuitType) {
		switch(circuitType) {
			case inputSetControlRod:
			case outputTemperature:
			case outputFuelMix:
			case outputFuelAmount:
			case outputWasteAmount:
				return true;
			default:
				return false;
		}
	}
	
	public static boolean circuitTypeRequiresSubSetting(TileEntityReactorRedNetPort.CircuitType circuitType) {
		switch(circuitType) {
			case outputFuelMix:
			case outputFuelAmount:
			case outputWasteAmount:
				return true;
			default:
				return false;
		}
	}

	// Decodes setting changes from an update packet
	public void decodeSettings(DataInputStream data, boolean doValidation) throws IOException {
		int channel;
		for(;;) {
			try {
				channel = data.readInt();
				CircuitType newSetting = CircuitType.values()[ data.readInt() ];

				channelCircuitTypes[channel] = newSetting;

				if(circuitTypeHasSubSetting(newSetting)) {
					boolean hasSubSettingData = data.readBoolean();
					CoordTriplet coord = null;
					if(hasSubSettingData) {
						coord = new CoordTriplet( data.readInt(), data.readInt(), data.readInt() );					
					}
					
					if(doValidation) {
						if(circuitTypeRequiresSubSetting(newSetting) && coord == null) {
							throw new IOException("Invalid setting for RedNet Port settings - no tile entity coords included when setting " + newSetting.toString());
						}
						
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
}

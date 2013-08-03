package erogenousbeef.bigreactors.common.tileentity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.InventoryPlayer;
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
		inputSetAllControlRods,		// Input: control rod insertion for all rods (0-100)

		outputTemperature,				// Output: Temperature of the reactor
		outputControlRodTemperature, 	// Output: Temperature of a single control rod
		outputControlRodFuelMix, 		// Output: Fuel mix, % of contents that is fuel (0-100, 100 = 100% fuel)
		outputControlRodFuelAmount, 	// Output: Fuel amount in a control rod, raw value, (0-4*height)
		outputControlRodWasteAmount 	// Output: Waste amount in a control rod, raw value, (0-4*height)
		
	}

	protected final static int minInputEnumValue = CircuitType.inputActive.ordinal();
	protected final static int maxInputEnumValue = CircuitType.inputSetAllControlRods.ordinal();
	protected final static int minOutputEnumValue = CircuitType.outputTemperature.ordinal();
	protected final static int maxOutputEnumValue = CircuitType.outputControlRodWasteAmount.ordinal();

	protected static boolean isInput(CircuitType type) { return type.ordinal() >= minInputEnumValue && type.ordinal() <= maxInputEnumValue; }
	protected static boolean isOutput(CircuitType type) { return type.ordinal() >= minOutputEnumValue && type.ordinal() <= maxOutputEnumValue; }
	
	protected CircuitType[] channelCircuitTypes;
	protected CoordTriplet[] coordMappings;
	protected final static int numChannels = 16;
	
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
			return (int)Math.floor(getReactorController().getHeat());
		case outputControlRodTemperature:
			te = getMappedTileEntity(channel);
			if(te instanceof TileEntityReactorControlRod) {
				return (int)Math.floor(((TileEntityReactorControlRod)te).getHeat());
			}
			else {
				return 0;
			}
		case outputControlRodFuelMix:
			te = getMappedTileEntity(channel);
			if(te instanceof TileEntityReactorControlRod) {
				TileEntityReactorControlRod cr = (TileEntityReactorControlRod)te;
				if(cr.getTotalContainedAmount() <= 0) { return 0; }
				else { 
					return (int)Math.floor(((float)cr.getFuelAmount() / (float)cr.getTotalContainedAmount())*100.0f);
				}
			}
			else {
				return 0;
			}
		case outputControlRodFuelAmount:
			te = getMappedTileEntity(channel);
			if(te instanceof TileEntityReactorControlRod) {
				return ((TileEntityReactorControlRod)te).getFuelAmount();
			}
			else {
				return 0;
			}
		case outputControlRodWasteAmount:
			te = getMappedTileEntity(channel);
			if(te instanceof TileEntityReactorControlRod) {
				return ((TileEntityReactorControlRod)te).getWasteAmount();
			}
			else {
				return 0;
			}
		default:
			return 0;
		}
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
		if(circuitType == CircuitType.outputControlRodTemperature ||
				circuitType == CircuitType.outputControlRodFuelMix ||
				circuitType == CircuitType.outputControlRodFuelAmount ||
				circuitType == CircuitType.outputControlRodWasteAmount ||
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
			setControlRodInsertion(coordMappings[channel], newValue);
			break;
		case inputSetAllControlRods:
			reactor = getReactorController();
			reactor.setAllControlRodInsertionValues(newValue);
			break;
		default:
			break;
		}
		
	}

	private void setControlRodInsertion(CoordTriplet coordTriplet, int newValue) {
		if(!this.isConnected()) { return; }
		if(!this.worldObj.checkChunksExist(coordTriplet.x, coordTriplet.y, coordTriplet.z,
											coordTriplet.x, coordTriplet.y, coordTriplet.z)) {
			return;
		}
		
		TileEntity te = this.worldObj.getBlockTileEntity(coordTriplet.x, coordTriplet.y, coordTriplet.z);
		if(te instanceof TileEntityReactorControlRod) {
			((TileEntityReactorControlRod)te).setControlRodInsertion((short)newValue);
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
	
	public static boolean circuitTypeHasSubSetting(TileEntityReactorRedNetPort.CircuitType circuitType) {
		switch(circuitType) {
			case inputSetControlRod:
			case outputControlRodTemperature:
			case outputControlRodFuelMix:
			case outputControlRodFuelAmount:
			case outputControlRodWasteAmount:
				return true;
			default:
				return false;
		}
	}
}

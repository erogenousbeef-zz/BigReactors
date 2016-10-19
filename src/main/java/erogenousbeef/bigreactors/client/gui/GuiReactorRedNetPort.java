package erogenousbeef.bigreactors.client.gui;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.multiblock.block.BlockReactorPart;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorControlRod;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorRedNetPort;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorRedNetPort.CircuitType;
import erogenousbeef.bigreactors.gui.IBeefGuiControl;
import erogenousbeef.bigreactors.gui.controls.BeefGuiLabel;
import erogenousbeef.bigreactors.gui.controls.BeefGuiRedNetChannelSelector;
import erogenousbeef.bigreactors.gui.controls.grab.BeefGuiGrabSource;
import erogenousbeef.bigreactors.gui.controls.grab.RedNetConfigGrabTarget;
import erogenousbeef.bigreactors.gui.controls.grab.RedNetConfigGrabbable;
import erogenousbeef.bigreactors.net.CommonPacketHandler;
import erogenousbeef.bigreactors.net.helpers.RedNetChange;
import erogenousbeef.bigreactors.net.message.ReactorRedNetPortChangeMessage;
import erogenousbeef.core.common.CoordTriplet;

public class GuiReactorRedNetPort extends BeefGuiBase {

	protected static final int numChannels = 16;

	TileEntityReactorRedNetPort port;
	BeefGuiLabel titleString;
	BeefGuiLabel settingsString;

	BeefGuiLabel subSettingString;
	BeefGuiLabel subSettingValueString;

	private GuiButton commitBtn;

	private GuiButton subSettingForwardBtn;
	private GuiButton subSettingBackBtn;

	private ResourceLocation _guiBackground;

	protected static final String[] channelLabelStrings = new String[] {
			"White", "Orange", "Magenta", "LightBlue", "Yellow", "Lime", "Pink", "Gray",
			"LightGray", "Cyan", "Purple", "Blue", "Brown", "Green", "Red", "Black"
	};

	public static final String[] grabbableTooltips = {
		"Input: Toggle reactor on/off",
		"Input: Change control rod insertion",
		"Input: Eject Waste",
		"Output: Fuel Temp (C)",
		"Output: Casing Temp (C)",
		"Output: Fuel mix (% fuel, 0-100)",
		"Output: Fuel amount",
		"Output: Waste amount",
		"Output: Energy amount (%)"
	};

	BeefGuiRedNetChannelSelector[] channelSelectors = new BeefGuiRedNetChannelSelector[numChannels];
	RedNetConfigGrabTarget[] grabTargets = new RedNetConfigGrabTarget[numChannels];
	private CoordTriplet[] subSettingCoords = new CoordTriplet[numChannels];
	private boolean[] pulseActivated = new boolean[numChannels];

	private int selectedChannel = 0;

	public GuiReactorRedNetPort(Container container, TileEntityReactorRedNetPort redNetPort) {
		super(container);
		port = redNetPort;

		xSize = 255;
		ySize = 214;

		_guiBackground = new ResourceLocation(BigReactors.GUI_DIRECTORY + "RedNetPort.png");
	}

	@Override
	public ResourceLocation getGuiBackground() {
		return _guiBackground;
	}

	@Override
	public void initGui() {
		super.initGui();

		int leftX = guiLeft + 4;
		int topY = guiTop + 4;

		titleString = new BeefGuiLabel(this, "Reactor RedNet Port", leftX+2, topY+2);
		settingsString = new BeefGuiLabel(this, "Settings", leftX+154, topY+2);
		subSettingString = new BeefGuiLabel(this, "", leftX+154, topY+80);
		subSettingValueString = new BeefGuiLabel(this, "", leftX+154, topY+94);

		topY += titleString.getHeight() + 8;

		selectedChannel = 0;
		for(int i = 0; i < channelLabelStrings.length; i+=2) {
			channelSelectors[i] = new BeefGuiRedNetChannelSelector(this, channelLabelStrings[i], i, leftX, topY, 60, 20);
			grabTargets[i] = new RedNetConfigGrabTarget(this, leftX + 42, topY+2, port, i);

			if(i == 0) {
				channelSelectors[i].setSelected(true);
			}

			leftX += 74;

			channelSelectors[i + 1] = new BeefGuiRedNetChannelSelector(this, channelLabelStrings[i+1], i+1, leftX, topY, 60, 20);
			grabTargets[i + 1] = new RedNetConfigGrabTarget(this, leftX + 42, topY+2, port, i + 1);
			topY += 24;
			leftX = guiLeft + 4;

			registerControl(channelSelectors[i]);
			registerControl(channelSelectors[i+1]);

			registerControl(grabTargets[i]);
			registerControl(grabTargets[i+1]);
		}

		TileEntityReactorRedNetPort.CircuitType[] circuitTypes = TileEntityReactorRedNetPort.CircuitType.values();
		BlockReactorPart reactorPartBlock = BigReactors.blockReactorPart;
		RedNetConfigGrabbable[] grabbables = new RedNetConfigGrabbable[circuitTypes.length - 1];
		topY = guiTop + 21;
		leftX = guiLeft + 156;
		for(int i = 1; i < circuitTypes.length; i++) {
			grabbables[i-1] = new RedNetConfigGrabbable(grabbableTooltips[i-1], reactorPartBlock.getRedNetConfigIcon(circuitTypes[i]), circuitTypes[i]);
			BeefGuiGrabSource source = new BeefGuiGrabSource(this, leftX, topY, grabbables[i - 1]);
			registerControl(source);
			leftX += 20;
			if(leftX >= guiLeft + 230) {
				leftX = guiLeft + 156;
				topY += 20;
			}
		}

		registerControl(titleString);
		registerControl(settingsString);
		registerControl(subSettingString);
		registerControl(subSettingValueString);

		commitBtn = new GuiButton(0, guiLeft + 190, guiTop + 190, 56, 20, "Commit");
		commitBtn.enabled = false;

		this.subSettingForwardBtn 	= new GuiButton(1, guiLeft + 178, guiTop + 114, 20, 20, ">");
		this.subSettingBackBtn 		= new GuiButton(2, guiLeft + 154, guiTop + 114, 20, 20, "<");
		this.subSettingForwardBtn.visible = false;
		this.subSettingBackBtn.visible = false;

		this.buttonList.add(commitBtn);
		this.buttonList.add(subSettingForwardBtn);
		this.buttonList.add(subSettingBackBtn);

		// Populate all the channels with existing settings
		TileEntityReactorRedNetPort.CircuitType currentCircuitType;
		for(int i = 0; i < TileEntityReactorRedNetPort.numChannels; i++) {
			currentCircuitType = port.getChannelCircuitType(i);
			pulseActivated[i] = port.isInputActivatedOnPulse(i);
			if(currentCircuitType == TileEntityReactorRedNetPort.CircuitType.DISABLED) {
				grabTargets[i].setSlotContents(null);
			}
			else {
				grabTargets[i].setSlotContents( grabbables[currentCircuitType.ordinal() - 1 ]);
			}
		}

		for(int i = 0; i < subSettingCoords.length; i++) {
			subSettingCoords[i] = port.getMappedCoord(i);
		}

		updateSubSettingValueText();
	}

	@Override
	public void updateScreen() {
		super.updateScreen();

		boolean hasChanges = false;
		boolean invalidSetting = false;
		for(RedNetConfigGrabTarget target : grabTargets) {
			if(target.hasChanged()) {
				hasChanges = true;
			}
		}

		// See if any subsettings changed
		for(int i = 0; i < subSettingCoords.length; i++) {
			if(hasSubSettingChanged(i)) {
				hasChanges = true;
				break;
			}
		}

		for(int i = 0; i < numChannels; i++) {
			if(port.isInputActivatedOnPulse(i) != pulseActivated[i]) {
				hasChanges = true;
				break;
			}
		}

		commitBtn.enabled = hasChanges && !invalidSetting;
	}

	protected boolean hasSubSettingChanged(int idx) {
		if(subSettingCoords[idx] == null) {
			if(port.getMappedCoord(idx) != null)
			{
				return true;
			}
		}
		else if(port.getMappedCoord(idx) == null) {
			return true;
		}
		else if(!subSettingCoords[idx].equals(port.getMappedCoord(idx))) {
			return true;
		}
		return false;
	}

	protected boolean hasSettingChanged(int idx) {
		return grabTargets[idx].hasChanged() || hasSubSettingChanged(idx) || (port.isInputActivatedOnPulse(idx) != pulseActivated[idx]);
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if(button.id == 0) {
			RedNetChange[] packetData = getUpdatePacketData();

			if(packetData == null) { return; }
            CommonPacketHandler.INSTANCE.sendToServer(new ReactorRedNetPortChangeMessage(port, packetData));
        }

		if(button.id == 1 || button.id == 2) {
			changeSubSetting(button.id == 1);
		}
	}

	private RedNetChange[] getUpdatePacketData() {
		List<RedNetChange> packetData = new LinkedList<RedNetChange>();

		for(int i = 0; i < TileEntityReactorRedNetPort.numChannels; i++) {
			if(hasSettingChanged(i)) {
				CircuitType circuitType = grabTargets[i].getCircuitType();
				RedNetChange change = new RedNetChange(i, circuitType, pulseActivated[i], subSettingCoords[i]);
				packetData.add(change);
			}
		}

		if(packetData.size() < 1) { return null; }

		RedNetChange[] changes = new RedNetChange[packetData.size()];
		changes = packetData.toArray(changes);
		return changes;
	}

	@Override
	public void onControlClicked(IBeefGuiControl clickedControl) {
		if(clickedControl instanceof BeefGuiRedNetChannelSelector) {
			// Set all selectors to unselected, except the one we clicked
			// Also change the subsetting selectors, in case those are visible
			for(IBeefGuiControl control : controls) {
				if(control instanceof BeefGuiRedNetChannelSelector) {
					BeefGuiRedNetChannelSelector selector = (BeefGuiRedNetChannelSelector)control;

					boolean wasSelected = selector.isSelected();
					selector.setSelected(control == clickedControl);
					if(control == clickedControl) {
						this.selectedChannel = selector.getChannel();
					}

					onChannelChanged(selector.getChannel());
				}
			}
		}
	}

	public void onChannelChanged(int changedChannel) {
		if(this.selectedChannel != changedChannel) { return; }

		CircuitType currentCircuitType = grabTargets[selectedChannel].getCircuitType();

		if(CircuitType.hasCoordinate(currentCircuitType)) {
			subSettingString.setLabelText("Control Rod: ");
			subSettingForwardBtn.visible = true;
			subSettingBackBtn.visible = true;
		}
		else if(TileEntityReactorRedNetPort.isInput(currentCircuitType) && CircuitType.canBeToggledBetweenPulseAndNormal(currentCircuitType)) {
			subSettingString.setLabelText("Activates On:");
			subSettingForwardBtn.visible = true;
			subSettingBackBtn.visible = true;
		}
		else if(currentCircuitType == CircuitType.inputEjectWaste) {
			subSettingString.setLabelText("Activates On:");
			subSettingForwardBtn.visible = false;
			subSettingBackBtn.visible = false;
		}
		else {
			subSettingString.setLabelText("");
			subSettingForwardBtn.visible = false;
			subSettingBackBtn.visible = false;
		}

		updateSubSettingValueText();
	}

	private String getControlRodLabelFromLocation(CircuitType circuitType, CoordTriplet location) {
		if(location == null) {
			return "-- ALL --";
		}
		else {
			TileEntity te = port.getWorldObj().getTileEntity(location.x, location.y, location.z);
			if( te instanceof TileEntityReactorControlRod ) {
				TileEntityReactorControlRod rod = (TileEntityReactorControlRod)te;
				if( rod.getName().equals("")) {
					return location.toString();
				}
				else {
					return rod.getName();
				}
			}
			else {
				return "INVALID: " + location.toString();
			}
		}
	}

	private void changeSubSetting(boolean forward) {
		CircuitType circuitType = grabTargets[selectedChannel].getCircuitType();

		if( CircuitType.hasCoordinate(circuitType) ) {
			// Select a new control rod
			CoordTriplet[] controlRodLocations = port.getReactorController().getControlRodLocations();
			int newIdx = 0;
			// Locate current idx; will be -1 if not found, which is expected.
			int oldIdx = Arrays.asList(controlRodLocations).indexOf( subSettingCoords[selectedChannel] );
			if(forward) {
				newIdx = oldIdx + 1;
			}
			else {
				if(oldIdx == -1) {
					newIdx = controlRodLocations.length - 1;
				}
				else {
					newIdx = oldIdx - 1;
				}
			}

			if(newIdx < 0 || newIdx >= controlRodLocations.length) {
				subSettingCoords[selectedChannel] = null;
			}
			else {
				subSettingCoords[selectedChannel] = controlRodLocations[newIdx];
			}
		}
		else if( CircuitType.canBeToggledBetweenPulseAndNormal(circuitType) ) {
			pulseActivated[selectedChannel] = !pulseActivated[selectedChannel];
		}

		updateSubSettingValueText();
	}

	private void updateSubSettingValueText() {
		subSettingValueString.setLabelTooltip("");


		CircuitType circuitType = grabTargets[selectedChannel].getCircuitType();

		if( CircuitType.hasCoordinate(circuitType) ) {
			subSettingValueString.setLabelText( getControlRodLabelFromLocation(circuitType, subSettingCoords[selectedChannel]) );
		}
		else if(TileEntityReactorRedNetPort.isInput(circuitType) && CircuitType.canBeToggledBetweenPulseAndNormal(circuitType)) {
			subSettingValueString.setLabelText(pulseActivated[selectedChannel]?"Pulse":"Level");
		}
		else if(circuitType == CircuitType.inputEjectWaste) {
			subSettingValueString.setLabelText("Pulse");
		}
		else {
			subSettingValueString.setLabelText("");
		}
	}
}

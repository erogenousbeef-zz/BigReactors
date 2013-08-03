package erogenousbeef.bigreactors.client.gui;

import java.util.Arrays;

import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.block.BlockReactorPart;
import erogenousbeef.bigreactors.common.tileentity.TileEntityReactorControlRod;
import erogenousbeef.bigreactors.common.tileentity.TileEntityReactorRedNetPort;
import erogenousbeef.bigreactors.gui.IBeefGuiControl;
import erogenousbeef.bigreactors.gui.controls.BeefGuiLabel;
import erogenousbeef.bigreactors.gui.controls.BeefGuiListBox;
import erogenousbeef.bigreactors.gui.controls.BeefGuiRedNetChannelSelector;
import erogenousbeef.bigreactors.gui.controls.grab.BeefGuiGrabSource;
import erogenousbeef.bigreactors.gui.controls.grab.BeefGuiGrabTarget;
import erogenousbeef.bigreactors.gui.controls.grab.RedNetConfigGrabTarget;
import erogenousbeef.bigreactors.gui.controls.grab.RedNetConfigGrabbable;
import erogenousbeef.core.common.CoordTriplet;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.inventory.Container;

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
	
	protected static final String[] channelLabelStrings = new String[] {
			"White", "Orange", "Magenta", "LightBlue", "Yellow", "Lime", "Pink", "Gray",
			"LightGray", "Cyan", "Purple", "Blue", "Brown", "Green", "Red", "Black"
	};
	
	protected static final String[] grabbableTooltips = {
		"Input: Toggle reactor on/off",
		"Input: Change control rod insertion",
		"Input: Change all control rod insertions",
		"Output: Reactor temperature (C)",
		"Output: Control rod temperature (C)",
		"Output: Control rod fuel mix (% fuel, 0-100)",
		"Output: Control rod fuel amount",
		"Output: Control rod waste amount"
	};
	
	BeefGuiRedNetChannelSelector[] channelSelectors = new BeefGuiRedNetChannelSelector[numChannels];
	RedNetConfigGrabTarget[] grabTargets = new RedNetConfigGrabTarget[numChannels];
	private CoordTriplet[] subSettingCoords = new CoordTriplet[numChannels];
	
	private int selectedChannel = 0;
	
	public GuiReactorRedNetPort(Container container, TileEntityReactorRedNetPort redNetPort) {
		super(container);
		port = redNetPort;
		
		xSize = 255;
		ySize = 214;
		
		for(int i = 0; i < subSettingCoords.length; i++) {
			subSettingCoords[i] = redNetPort.getMappedCoord(i);
		}
	}

	@Override
	public String getGuiBackground() {
		// TODO: Add slots to background
		return BigReactors.GUI_DIRECTORY + "RedNetPort.png";
	}
	
	@Override
	public void initGui() {
		super.initGui();

		int leftX = 4;
		int topY = 4;
		
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
			leftX = 4;
			
			registerControl(channelSelectors[i]);
			registerControl(channelSelectors[i+1]);
			
			registerControl(grabTargets[i]);
			registerControl(grabTargets[i+1]);
		}
		
		TileEntityReactorRedNetPort.CircuitType[] circuitTypes = TileEntityReactorRedNetPort.CircuitType.values();
		BlockReactorPart reactorPartBlock = (BlockReactorPart)BigReactors.blockReactorPart;
		RedNetConfigGrabbable[] grabbables = new RedNetConfigGrabbable[circuitTypes.length - 1];
		topY = 21;
		leftX = 156;
		for(int i = 1; i < circuitTypes.length; i++) {
			grabbables[i-1] = new RedNetConfigGrabbable(grabbableTooltips[i-1], reactorPartBlock.getRedNetConfigIcon(circuitTypes[i]), circuitTypes[i]);
			BeefGuiGrabSource source = new BeefGuiGrabSource(this, leftX, topY, grabbables[i - 1]);			
			registerControl(source);
			leftX += 20;
			if(leftX >= 230) {
				leftX = 156;
				topY += 20;
			}
		}

		registerControl(titleString);
		registerControl(settingsString);
		registerControl(subSettingString);
		registerControl(subSettingValueString);
		
		// TODO: Hide subSetting strings initially
		
		commitBtn = new GuiButton(0, guiLeft + 190, guiTop + 190, 56, 20, "Commit");
		commitBtn.enabled = false;
		
		// TODO: Hide these initially
		this.subSettingForwardBtn 	= new GuiButton(1, guiLeft + 178, guiTop + 114, 20, 20, ">");
		this.subSettingBackBtn 		= new GuiButton(2, guiLeft + 154, guiTop + 114, 20, 20, "<");
		this.subSettingForwardBtn.drawButton = false;
		this.subSettingBackBtn.drawButton = false;
		
		this.buttonList.add(commitBtn);
		this.buttonList.add(subSettingForwardBtn);
		this.buttonList.add(subSettingBackBtn);

		// TODO: Populate all the channels with existing settings
	}
	
	@Override
	public void updateScreen() {
		super.updateScreen();
		
		boolean hasChanges = false;
		for(RedNetConfigGrabTarget target : grabTargets) {
			if(target.hasChanged()) {
				hasChanges = true;
				break;
			}
		}
		
		// See if any subsettings changed
		for(int i = 0; i < subSettingCoords.length; i++) {
			if(subSettingCoords[i] == null) {
				if(port.getMappedCoord(i) != null)
				{
					hasChanges = true;
					break;
				}
			}
			else if(!subSettingCoords[i].equals(port.getMappedCoord(i)))
			{
				hasChanges = true;
				break;
			}
		}
		
		commitBtn.enabled = hasChanges;
	}
	
	@Override
	protected void actionPerformed(GuiButton button) {
		if(button.id == 0) {
			// TODO: Send update packet
			System.out.println("TODO: Send update packet");
		}
		
		if(button.id == 1 || button.id == 2) {
			changeSelectedCoord(button.id == 1);
		}
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

		CoordTriplet[] controlRodLocations = port.getReactorController().getControlRodLocations();
		
		if(TileEntityReactorRedNetPort.circuitTypeHasSubSetting(grabTargets[selectedChannel].getCircuitType())) {
			subSettingString.setLabelText("Control Rod: ");
			subSettingValueString.setLabelText( getControlRodLabelFromLocation(subSettingCoords[selectedChannel]) );
			subSettingForwardBtn.drawButton = true;
			subSettingBackBtn.drawButton = true;
		}
		else {
			subSettingString.setLabelText("");
			subSettingValueString.setLabelText("");
			subSettingForwardBtn.drawButton = false;
			subSettingBackBtn.drawButton = false;
		}
	}
	
	private String getControlRodLabelFromLocation(CoordTriplet location) {
		if(location == null) {
			return "-- NONE --";
		}
		
		TileEntityReactorControlRod controlRod = (TileEntityReactorControlRod)port.worldObj.getBlockTileEntity(location.x, location.y,  location.z);
		if(controlRod == null) {
			throw new IllegalArgumentException("No control rod found at coords " + location.toString());
		}
		
		return controlRod.getName();
	}
	
	private void changeSelectedCoord(boolean forward) {
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
}

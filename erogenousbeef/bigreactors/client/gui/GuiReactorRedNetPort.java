package erogenousbeef.bigreactors.client.gui;

import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.block.BlockReactorPart;
import erogenousbeef.bigreactors.common.tileentity.TileEntityReactorRedNetPort;
import erogenousbeef.bigreactors.gui.IBeefGuiControl;
import erogenousbeef.bigreactors.gui.controls.BeefGuiLabel;
import erogenousbeef.bigreactors.gui.controls.BeefGuiListBox;
import erogenousbeef.bigreactors.gui.controls.BeefGuiRedNetChannelSelector;
import erogenousbeef.bigreactors.gui.controls.grab.BeefGuiGrabSource;
import erogenousbeef.bigreactors.gui.controls.grab.BeefGuiGrabTarget;
import erogenousbeef.bigreactors.gui.controls.grab.RedNetConfigGrabTarget;
import erogenousbeef.bigreactors.gui.controls.grab.RedNetConfigGrabbable;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.inventory.Container;

public class GuiReactorRedNetPort extends BeefGuiBase {

	protected static final int numChannels = 16;
	
	TileEntityReactorRedNetPort port;
	BeefGuiLabel titleString;
	BeefGuiLabel settingsString;
	
	private GuiButton commitBtn;
	
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
	
	public GuiReactorRedNetPort(Container container, TileEntityReactorRedNetPort redNetPort) {
		super(container);
		port = redNetPort;
		
		xSize = 255;
		ySize = 214;
		
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
		settingsString = new BeefGuiLabel(this, "Settings", leftX+164, topY+2);
		topY += titleString.getHeight() + 8;
		
		for(int i = 0; i < channelLabelStrings.length; i+=2) {
			channelSelectors[i] = new BeefGuiRedNetChannelSelector(this, channelLabelStrings[i], i, leftX, topY, 60, 20);
			grabTargets[i] = new RedNetConfigGrabTarget(this, leftX + 42, topY+2, port, i);
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
		leftX = 166;
		for(int i = 1; i < circuitTypes.length; i++) {
			grabbables[i-1] = new RedNetConfigGrabbable(grabbableTooltips[i-1], reactorPartBlock.getRedNetConfigIcon(circuitTypes[i]), circuitTypes[i]);
			BeefGuiGrabSource source = new BeefGuiGrabSource(this, leftX, topY, grabbables[i - 1]);			
			registerControl(source);
			leftX += 20;
			if(leftX >= 230) {
				leftX = 166;
				topY += 20;
			}
		}

		// TODO: Populate all the channels with existing settings
		
		registerControl(titleString);
		registerControl(settingsString);
		
		commitBtn = new GuiButton(0, guiLeft + 190, guiTop + 190, 56, 20, "Commit");
		commitBtn.enabled = false;
		
		this.buttonList.add(commitBtn);
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
		
		commitBtn.enabled = hasChanges;
	}
	
	@Override
	protected void actionPerformed(GuiButton button) {
		if(button.id == 0) {
			// TODO: Send update packet
			System.out.println("TODO: Send update packet");
		}
	}
	
	@Override
	public void onControlClicked(IBeefGuiControl clickedControl) {
		if(clickedControl instanceof BeefGuiRedNetChannelSelector) {
			// Set all selectors to unselected, except the one we clicked
			System.out.println("Clicked control: " + ((BeefGuiRedNetChannelSelector)clickedControl).getTooltip());
			for(IBeefGuiControl control : controls) {
				if(control instanceof BeefGuiRedNetChannelSelector) {
					((BeefGuiRedNetChannelSelector)control).setSelected(control == clickedControl);
				}
			}
			
			// TODO: Re-init the sub-choices selector
		}
	}

}

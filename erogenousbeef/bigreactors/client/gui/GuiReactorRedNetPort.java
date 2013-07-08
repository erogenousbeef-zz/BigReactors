package erogenousbeef.bigreactors.client.gui;

import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.block.BlockReactorPart;
import erogenousbeef.bigreactors.common.tileentity.TileEntityReactorRedNetPort;
import erogenousbeef.bigreactors.gui.controls.BeefGuiLabel;
import erogenousbeef.bigreactors.gui.controls.BeefGuiListBox;
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
	BeefGuiLabel[] channelLabels = new BeefGuiLabel[numChannels];
	
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
	
	RedNetConfigGrabTarget[] grabTargets = new RedNetConfigGrabTarget[numChannels];
	
	public GuiReactorRedNetPort(Container container, TileEntityReactorRedNetPort redNetPort) {
		super(container);
		port = redNetPort;
		
		//xSize = 241;
		ySize = 220;
		
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
		
		titleString = new BeefGuiLabel(this, "Reactor RedNet Port", leftX, topY);
		topY += titleString.getHeight() + 8;
		
		int halfLength = channelLabelStrings.length / 2;
		for(int i = 0; i < channelLabelStrings.length / 2; i++) {
			channelLabels[i] = new BeefGuiLabel(this, channelLabelStrings[i], leftX, topY+2);
			grabTargets[i] = new RedNetConfigGrabTarget(this, leftX + 60, topY, port, i);
			leftX += 82;
			
			channelLabels[i + halfLength] = new BeefGuiLabel(this, channelLabelStrings[i + halfLength], leftX, topY+2);
			grabTargets[i + halfLength] = new RedNetConfigGrabTarget(this, leftX + 60, topY, port, i + halfLength);
			topY += 22;
			leftX = 4;
			
			registerControl(channelLabels[i]);
			registerControl(channelLabels[i+halfLength]);
			
			registerControl(grabTargets[i]);
			registerControl(grabTargets[i+halfLength]);
		}
		
		TileEntityReactorRedNetPort.CircuitType[] circuitTypes = TileEntityReactorRedNetPort.CircuitType.values();
		BlockReactorPart reactorPartBlock = (BlockReactorPart)BigReactors.blockReactorPart;
		RedNetConfigGrabbable[] grabbables = new RedNetConfigGrabbable[circuitTypes.length - 1];
		for(int i = 1; i < circuitTypes.length; i++) {
			grabbables[i-1] = new RedNetConfigGrabbable(grabbableTooltips[i-1], reactorPartBlock.getRedNetConfigIcon(circuitTypes[i]), circuitTypes[i]);
			BeefGuiGrabSource source = new BeefGuiGrabSource(this, leftX, topY, grabbables[i - 1]);			
			registerControl(source);
			leftX += 18;
		}

		// TODO: Populate all the channels with existing settings
		
		registerControl(titleString);
		
		commitBtn = new GuiButton(0, guiLeft + width - 60, guiTop + height - 24, 56, 20, "Commit");
		commitBtn.enabled = false;
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

}

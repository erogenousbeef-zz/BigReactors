package erogenousbeef.bigreactors.client.gui;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.block.BlockReactorPart;
import erogenousbeef.bigreactors.common.tileentity.TileEntityReactorRedstonePort;
import erogenousbeef.bigreactors.common.tileentity.TileEntityReactorRedNetPort.CircuitType;
import erogenousbeef.bigreactors.gui.controls.BeefGuiLabel;
import erogenousbeef.bigreactors.gui.controls.GuiSelectableButton;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;

public class GuiReactorRedstonePort extends BeefGuiBase {

	private ResourceLocation _guiBackground;
	private TileEntityReactorRedstonePort port;

	BeefGuiLabel titleString;
	BeefGuiLabel settingString;

	private GuiButton commitBtn;
	private GuiButton resetBtn;
	
	// Subsetting display
	BeefGuiLabel subInputPulseOrSteady;
	private GuiButton subInputPulse;
	private GuiButton subInputSteady;
	private GuiButton subInputRodInOrOut;
	private GuiTextField subInputRodSetting; // Also used as the pulse activation setting
	private GuiTextField subInputRodSettingOff;
	
	BeefGuiLabel subOutputValueLabel;
	private GuiButton subOutputGreaterOrLessThan;
	private GuiTextField subOutputLevel;
	// End Subsetting display
	
	private Map<CircuitType, GuiSelectableButton> btnMap;
	private int outputLevel;
	private boolean greaterThan;
	
	private static final int MINIMUM_SETTING_SELECTOR_ID = 10;
	
	public GuiReactorRedstonePort(Container container, TileEntityReactorRedstonePort tileentity) {
		super(container);
		_guiBackground = new ResourceLocation(BigReactors.GUI_DIRECTORY + "BasicBackground.png");
		port = tileentity;
		
		btnMap = new HashMap<CircuitType, GuiSelectableButton>();
		outputLevel = tileentity.getOutputLevel();
		greaterThan = tileentity.getGreaterThan();
	}

	@Override
	public ResourceLocation getGuiBackground() {
		return _guiBackground;
	}

	private void registerCircuitButton(CircuitType ct, GuiSelectableButton btn) {
		this.btnMap.put(ct, btn);
		registerControl(btn);
	}
	
	@Override
	public void initGui() {
		super.initGui();

		int leftX = guiLeft + 4;
		int topY = guiTop + 6;
		
		titleString = new BeefGuiLabel(this, "Reactor Redstone Port", leftX+2, topY);
		topY += titleString.getHeight() + 4;
		
		settingString = new BeefGuiLabel(this, "Pick a setting", leftX, topY);
		topY += settingString.getHeight() + 4;
		
		// Setting picker
		BlockReactorPart reactorPartBlock = (BlockReactorPart)BigReactors.blockReactorPart;
		int buttonOrdinal = MINIMUM_SETTING_SELECTOR_ID;
		leftX = guiLeft + 16;
		CircuitType currentCircuitType = port.getCircuitType();
		for(CircuitType ct : CircuitType.values()) {
			if(ct == CircuitType.DISABLED) { continue; }
			GuiSelectableButton newBtn = new GuiSelectableButton(buttonOrdinal++, leftX, topY, reactorPartBlock.getRedNetConfigIcon(ct), 0xFF00FF00, this);
			newBtn.displayString = GuiReactorRedNetPort.grabbableTooltips[ct.ordinal()-1];

			if(ct == currentCircuitType) {
				newBtn.setSelected(true);
			}

			leftX += 28;
			if(leftX > guiLeft + 130) {
				topY += 28;
				leftX = guiLeft + 16;
			}
			
			registerCircuitButton(ct, newBtn);
		}
		
		leftX = guiLeft + 4;
		// TODO: Subsetting display
		
		// Bottom buttons
		commitBtn = new GuiButton(0, guiLeft + 116, guiTop + 142, 56, 20, "Commit");
		commitBtn.enabled = false;

		resetBtn  = new GuiButton(1, guiLeft + 4, guiTop + 142, 56, 20, "Reset");
		
		registerControl(titleString);
		registerControl(settingString);

		registerControl(commitBtn);
		registerControl(resetBtn);
	}
	
	@Override
	public void updateScreen() {
		super.updateScreen();
	}
	
	@Override
	protected void actionPerformed(GuiButton clickedButton) {
		if(clickedButton.id == 0) {
			// TODO: SUMBIT
		}
		else if(clickedButton.id == 1) {
			this.outputLevel = port.getOutputLevel();
			this.greaterThan = port.getGreaterThan();
			for(Entry<CircuitType, GuiSelectableButton> pair : btnMap.entrySet()) {
				pair.getValue().setSelected(pair.getKey() == port.getCircuitType());
			}
			// TODO: Update subsettings
		}
		else if(clickedButton.id >= MINIMUM_SETTING_SELECTOR_ID) {
			for(Entry<CircuitType, GuiSelectableButton> pair : btnMap.entrySet()) {
				GuiSelectableButton btn = pair.getValue();
				btn.setSelected(btn.id == clickedButton.id);
			}
			// TODO: Update subsettings
		}
	}
}

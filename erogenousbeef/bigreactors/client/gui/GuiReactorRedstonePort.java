package erogenousbeef.bigreactors.client.gui;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.lwjgl.input.Keyboard;

import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.block.BlockReactorPart;
import erogenousbeef.bigreactors.common.tileentity.TileEntityReactorRedNetPort;
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
	private BeefGuiLabel subSettingLabel;
	private GuiButton subInputButton;
	private GuiButton subInputButton2;
	private BeefGuiLabel subInputRodSettingLabel;
	private GuiTextField subInputRodSetting; // Also used as the pulse activation setting
	private BeefGuiLabel subInputRodSettingPctLabel;
	private BeefGuiLabel subInputRodSettingOffLabel;
	private GuiTextField subInputRodSettingOff;
	private BeefGuiLabel subInputRodSettingOffPctLabel;
	
	BeefGuiLabel subOutputValueLabel;
	private GuiTextField subOutputValue;
	// End Subsetting display
	
	private Map<CircuitType, GuiSelectableButton> btnMap;
	private int outputLevel;
	private boolean greaterThan;
	private boolean retract; // insert of insert
	
	private static final int MINIMUM_SETTING_SELECTOR_ID = 10;
	
	public GuiReactorRedstonePort(Container container, TileEntityReactorRedstonePort tileentity) {
		super(container);
		_guiBackground = new ResourceLocation(BigReactors.GUI_DIRECTORY + "RedstonePort.png");
		port = tileentity;
		
		ySize = 204;
		
		btnMap = new HashMap<CircuitType, GuiSelectableButton>();
		outputLevel = tileentity.getOutputLevel();
		greaterThan = tileentity.getGreaterThan();
		
		if(outputLevel < 0) {
			this.retract = true; 
		}
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

		int leftX = guiLeft + 6;
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
		
		topY += 32;
		leftX = guiLeft + 6;

		// TODO: Subsetting display
		subSettingLabel = new BeefGuiLabel(this, "Settings", leftX, topY);
		topY += subSettingLabel.getHeight() + 4;

		subInputButton = new GuiButton(2, leftX, topY, 100, 20, "Activate on Pulse");
		subInputButton2 = new GuiButton(3, leftX + xSize - 46, topY, 36, 20, "In/Out");
		topY += 24;
		
		subInputRodSettingLabel = new BeefGuiLabel(this, "While On", leftX, topY);
		subInputRodSettingOffLabel = new BeefGuiLabel(this, "While Off", leftX + xSize/2, topY);
		
		subOutputValue = new GuiTextField(this.fontRenderer, leftX, topY, 60, 12);
		subOutputValue.setCanLoseFocus(true);
		subOutputValue.setMaxStringLength(7);
		subOutputValue.setText("");
		subOutputValue.setEnabled(true);

		subOutputValueLabel = new BeefGuiLabel(this, "C", leftX + 62, topY + 2);
		
		topY += subInputRodSettingLabel.getHeight() + 2;
		
		subInputRodSetting = new GuiTextField(this.fontRenderer, leftX, topY, 32, 12);
		subInputRodSetting.setCanLoseFocus(true);
		subInputRodSetting.setMaxStringLength(3);
		subInputRodSetting.setText("");
		subInputRodSetting.setEnabled(true);

		subInputRodSettingPctLabel = new BeefGuiLabel(this, "%", leftX + 34, topY + 2);

		subInputRodSettingOff = new GuiTextField(this.fontRenderer, leftX + xSize/2, topY, 32, 12);
		subInputRodSettingOff.setCanLoseFocus(true);
		subInputRodSettingOff.setMaxStringLength(3);
		subInputRodSettingOff.setText("");
		subInputRodSettingOff.setEnabled(true);
		subInputRodSettingOffPctLabel = new BeefGuiLabel(this, "%", leftX + xSize/2 + 34, topY + 2);

		topY += 24;
		
		// Bottom buttons
		commitBtn = new GuiButton(0, guiLeft + xSize - 60, guiTop + ySize - 24, 56, 20, "Commit");
		commitBtn.enabled = false;

		resetBtn  = new GuiButton(1, guiLeft + 4, guiTop + ySize - 24, 56, 20, "Reset");
		
		registerControl(titleString);
		registerControl(settingString);
		registerControl(subSettingLabel);
		registerControl(subInputButton);
		registerControl(subInputButton2);
		registerControl(subInputRodSettingLabel);
		registerControl(subInputRodSettingOffLabel);
		registerControl(subInputRodSetting);
		registerControl(subInputRodSettingOff);
		registerControl(subInputRodSettingPctLabel);
		registerControl(subInputRodSettingOffPctLabel);
		registerControl(subOutputValue);
		registerControl(subOutputValueLabel);

		registerControl(commitBtn);
		registerControl(resetBtn);
		
		updateSubSettings();
	}
	
	@Override
	public void updateScreen() {
		super.updateScreen();
		
		updateSubSettings();
	}
	
	private void updateSubSettings() {
		CircuitType selectedSetting = getUserSelectedCircuitType();
		
		this.subSettingLabel.setLabelText(getLabelFromSelectedSubSetting(selectedSetting));
		updateSubSettingInputButton(selectedSetting);
		updateSubSettingTextFields(selectedSetting);
	}

	private String getLabelFromSelectedSubSetting(CircuitType selectedSetting) {
		switch(selectedSetting) {
			case inputActive: 			return "Input - Enable/Disable";
			case inputEjectWaste: 		return "Input - Eject Waste";
			case inputSetControlRod: 	return "Input - Control Rod Insertion";
			case outputFuelAmount: 		return "Output - Fuel Amount";
			case outputWasteAmount: 	return "Output - Waste Amount";
			case outputFuelMix: 		return "Output - Fuel Enrichment %";
			case outputTemperature: 	return "Output - Temperature (C)";
		default:
			return "";
		}
	}
	
	private void updateSubSettingInputButton(CircuitType selectedSetting) {
		subInputButton.drawButton = true;
		subInputButton2.drawButton = false;
		switch(selectedSetting) {
		case inputActive:
			subInputButton.enabled = true;
			if(this.greaterThan) {
				subInputButton.displayString = "Toggle on Pulse";
			}
			else {
				subInputButton.displayString = "Follow Input Signal";
			}
			break;
		case inputSetControlRod:
			subInputButton.enabled = true;
			if(this.greaterThan) {
				subInputButton2.drawButton = true;
				if(this.retract) {
					subInputButton.displayString = "Retract on Pulse";
				}
				else {
					subInputButton.displayString = "Insert on Pulse";
				}
			}
			else {
				subInputButton.displayString = "Set From Signal";
			}
			break;
		case inputEjectWaste:
			subInputButton.enabled = false;
			subInputButton.displayString = "Eject on Pulse";
			break;
		case outputTemperature:
		case outputFuelMix:
		case outputFuelAmount:
		case outputWasteAmount:
			subInputButton.enabled = true;
			if(this.greaterThan) {
				subInputButton.displayString = "Active While Above";
			}
			else {
				subInputButton.displayString = "Active While Below";
			}
			break;
		default:
			subInputButton.drawButton = false;
		}
	}

	private void updateSubSettingTextFields(CircuitType selectedSetting) {
		subOutputValueLabel.setLabelText("");
		subInputRodSettingLabel.setLabelText("");
		subInputRodSettingPctLabel.setLabelText("");
		subInputRodSettingOffLabel.setLabelText("");
		subInputRodSettingOffPctLabel.setLabelText("");
		subOutputValueLabel.setLabelTooltip("");
		
		subInputRodSetting.setVisible(false);
		subInputRodSettingOff.setVisible(false);
		subOutputValue.setVisible(false);
		
		switch(selectedSetting) {
			case outputTemperature:
				subOutputValueLabel.setLabelText("C");
				subOutputValueLabel.setLabelTooltip("Degrees centigrade");
				subOutputValue.setVisible(true);
				break;
			case outputFuelMix:
				subOutputValueLabel.setLabelText("%");
				subOutputValueLabel.setLabelTooltip("% of reactor contents which are fuels, 0 if empty");
				subOutputValue.setVisible(true);
				break;
			case outputFuelAmount:
				subOutputValueLabel.setLabelText("mB");
				subOutputValueLabel.setLabelTooltip("Milli-buckets");
				subOutputValue.setVisible(true);
				break;
			case outputWasteAmount:
				subOutputValueLabel.setLabelText("mB");
				subOutputValueLabel.setLabelTooltip("Milli-buckets");
				subOutputValue.setVisible(true);
				break;
			case inputSetControlRod:
				if(this.greaterThan) {
					if(this.retract) {
						subInputRodSettingLabel.setLabelText("Insert by");
					}
					else {
						subInputRodSettingLabel.setLabelText("Retract by");
					}
				}
				else {
					subInputRodSettingLabel.setLabelText("While On");
					subInputRodSettingOffLabel.setLabelText("While Off");
					subInputRodSettingOffPctLabel.setLabelText("%");
					subInputRodSettingOff.setVisible(true);
				}
				subInputRodSetting.setVisible(true);
				subInputRodSettingPctLabel.setLabelText("%");
				break;
			default:
				break;
		
		}
	}
	
	@Override
	protected void actionPerformed(GuiButton clickedButton) {
		if(clickedButton.id == 0) {
			// TODO: SUMBIT
		}
		else if(clickedButton.id == 1) {
			for(Entry<CircuitType, GuiSelectableButton> pair : btnMap.entrySet()) {
				pair.getValue().setSelected(pair.getKey() == port.getCircuitType());
			}
			
			setSubSettingsToDefaults(port.getCircuitType());
		}
		else if(clickedButton.id == 2) {
			this.greaterThan = !this.greaterThan;
		}
		else if(clickedButton.id == 3) {
			this.retract = !this.retract;
		}
		else if(clickedButton.id >= MINIMUM_SETTING_SELECTOR_ID && clickedButton.id < MINIMUM_SETTING_SELECTOR_ID + btnMap.size()) {
			CircuitType ct = CircuitType.DISABLED;
			for(Entry<CircuitType, GuiSelectableButton> pair : btnMap.entrySet()) {
				GuiSelectableButton btn = pair.getValue();
				btn.setSelected(btn.id == clickedButton.id);

				if(btn.isSelected()) {
					ct = pair.getKey();
				}
			}
			
			setSubSettingsToDefaults(ct);
		}
	}
	
	private void setSubSettingsToDefaults(CircuitType selectedType) {
		if(port.getCircuitType() == selectedType) {
			// RESTORE ALL THE DEFAULTS
			this.outputLevel = port.getOutputLevel();
			this.greaterThan = port.getGreaterThan();
			if(this.outputLevel < 0) {
				this.retract = true;
			}
		}
		else {
			this.greaterThan = true;
			this.retract = false;
			
			// This should reset outputLevel from stored values
			if(TileEntityReactorRedNetPort.isInput(selectedType)) {
				this.validateInputValues();
			}
			else {
				this.validateOutputValues();
			}
		}
		
		this.subInputRodSetting.setFocused(false);
		this.subInputRodSettingOff.setFocused(false);
		this.subOutputValue.setFocused(false);
	}

	// Allow 0-9 (regular or numpad), backspace, delete, left/right arrows.
	private boolean isKeyValidForValueInput(int keyCode) {
		if(keyCode >= Keyboard.KEY_1 && keyCode <= Keyboard.KEY_0) { return true; }
		switch(keyCode) {
			case Keyboard.KEY_NUMPAD0:
			case Keyboard.KEY_NUMPAD1:
			case Keyboard.KEY_NUMPAD2:
			case Keyboard.KEY_NUMPAD3:
			case Keyboard.KEY_NUMPAD4:
			case Keyboard.KEY_NUMPAD5:
			case Keyboard.KEY_NUMPAD6:
			case Keyboard.KEY_NUMPAD7:
			case Keyboard.KEY_NUMPAD8:
			case Keyboard.KEY_NUMPAD9:
			case Keyboard.KEY_DELETE:
			case Keyboard.KEY_BACK:
			case Keyboard.KEY_LEFT:
			case Keyboard.KEY_RIGHT:
				return true;
			default:
				return false;
		}
	}
	
	@Override
	protected void keyTyped(char inputChar, int keyCode) {
		boolean isAnyTextboxFocused = this.subInputRodSetting.isFocused() ||
										this.subInputRodSettingOff.isFocused() ||
										this.subOutputValue.isFocused();
		
        if (keyCode == Keyboard.KEY_ESCAPE ||
        		(!isAnyTextboxFocused && keyCode == this.mc.gameSettings.keyBindInventory.keyCode)) {
            this.mc.thePlayer.closeScreen();
        }

        // Allow arrow keys, 0-9, and delete
        if(isKeyValidForValueInput(keyCode)) {
            if(this.subInputRodSetting.isFocused()) {
            	this.subInputRodSetting.textboxKeyTyped(inputChar,  keyCode);
                validateInputValues();
            }
            if(this.subInputRodSettingOff.isFocused()) {
            	this.subInputRodSettingOff.textboxKeyTyped(inputChar,  keyCode);
                validateInputValues();
            }
            if(this.subOutputValue.isFocused()) {
            	this.subOutputValue.textboxKeyTyped(inputChar,  keyCode);
            	validateOutputValues();
            }
            
        }
		
		if(keyCode == Keyboard.KEY_TAB) {
			/// ffffffuuuuuuuck tabbing
			if(this.subOutputValue.isFocused()) {
				this.subOutputValue.setFocused(false);
			}
			else if(this.subOutputValue.getVisible()) {
				this.subOutputValue.setFocused(true);
			}
			
			if(this.subInputRodSettingOff.getVisible()) {
				if(this.subInputRodSetting.isFocused()) {
					this.subInputRodSetting.setFocused(false);
					this.subInputRodSettingOff.setFocused(true);
				}
				else if(this.subInputRodSettingOff.isFocused()) {
					this.subInputRodSettingOff.setFocused(false);
				}
				else {
					this.subInputRodSetting.setFocused(true);
				}
			}
			else if(this.subInputRodSettingOff.getVisible()) {
				if(this.subInputRodSetting.isFocused()) {
					this.subInputRodSetting.setFocused(false);
				}
				else {
					this.subInputRodSetting.setFocused(true);
				}
			}
			// Else, nothing is visible, nothing is focused, screw you.
		}
		
		if(keyCode == Keyboard.KEY_RETURN && isAnyTextboxFocused) {
			this.subInputRodSetting.setFocused(false);
			this.subInputRodSettingOff.setFocused(false);
			this.subOutputValue.setFocused(false);
		}
	}

	private void validateInputValues() {
		String in1 = this.subInputRodSetting.getText();
		int val1;
		if(in1.isEmpty()) {
			val1 = 0;
		}
		else {
			val1 = Integer.valueOf(in1);
			if(val1 < 0) { val1 = 0; }
			else if(val1 > 100) { val1 = 100; }
		}
		this.subInputRodSetting.setText(Integer.toString(val1));
		
		if(this.subInputRodSettingOff.getVisible()) {
			int val2;
			String in2 = this.subInputRodSettingOff.getText();
			if(in2.isEmpty()) {
				val2 = 0;
			}
			else {
				val2 = Integer.valueOf(in2);
				if(val2 < 0) { val2 = 0; }
				else if(val2 > 100) { val2 = 100; }
			}
			// pack into high-order bits
			this.outputLevel = (val2 >> 8) & 0xFF00;

			this.subInputRodSettingOff.setText(Integer.toString(val2));
		}
		else {
			// Preserve high-order bits
			this.outputLevel = this.outputLevel & 0xFF00;
		}

		// Pack in low-order bits
		this.outputLevel |= val1 & 0xFF;
	}
	
	private void validateOutputValues() {
		CircuitType selectedType = getUserSelectedCircuitType();
		int maxVal = Integer.MAX_VALUE;
		if(selectedType == CircuitType.outputFuelMix) {
			// Percentile
			maxVal = 100;
		}
	
		String in1 = this.subOutputValue.getText();
		int val1;
		if(in1.isEmpty()) {
			val1 = 0;
		}
		else {
			val1 = Integer.valueOf(in1);
			if(val1 < 0) { val1 = 0; }
			else if(val1 > maxVal) { val1 = maxVal; }
		}
		this.subOutputValue.setText(Integer.toString(val1));

		this.outputLevel = val1;
	}
	
	private CircuitType getUserSelectedCircuitType() {
		for(Entry<CircuitType, GuiSelectableButton> pair : btnMap.entrySet()) {
			if(pair.getValue().isSelected()) {
				return pair.getKey();
			}
		}

		return CircuitType.DISABLED;
	}
}

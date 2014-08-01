package erogenousbeef.bigreactors.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.inventory.Container;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import erogenousbeef.bigreactors.client.ClientProxy;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.multiblock.MultiblockReactor;
import erogenousbeef.bigreactors.common.multiblock.MultiblockReactor.WasteEjectionSetting;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorPart;
import erogenousbeef.bigreactors.gui.BeefGuiIconManager;
import erogenousbeef.bigreactors.gui.controls.BeefGuiFluidBar;
import erogenousbeef.bigreactors.gui.controls.BeefGuiFuelMixBar;
import erogenousbeef.bigreactors.gui.controls.BeefGuiHeatBar;
import erogenousbeef.bigreactors.gui.controls.BeefGuiIcon;
import erogenousbeef.bigreactors.gui.controls.BeefGuiLabel;
import erogenousbeef.bigreactors.gui.controls.BeefGuiPowerBar;
import erogenousbeef.bigreactors.gui.controls.GuiIconButton;
import erogenousbeef.bigreactors.net.CommonPacketHandler;
import erogenousbeef.bigreactors.net.Packets;
import erogenousbeef.bigreactors.net.message.MultiblockNetworkHandlerMessageServer;
import erogenousbeef.bigreactors.utils.StaticUtils;
import erogenousbeef.core.common.CoordTriplet;

public class GuiReactorStatus extends BeefGuiBase {

	private GuiIconButton btnReactorOn;
	private GuiIconButton btnReactorOff;
	private GuiIconButton btnWasteAutoEject;
	private GuiIconButton btnWasteManual;
	
	private GuiIconButton btnWasteEject;
	
	private TileEntityReactorPart part;
	private MultiblockReactor reactor;
	
	private BeefGuiLabel titleString;
	private BeefGuiLabel statusString;
	
	private BeefGuiIcon  heatIcon;
	private BeefGuiLabel heatString;
	private BeefGuiIcon outputIcon;
	private BeefGuiLabel outputString;
	private BeefGuiIcon fuelConsumedIcon;
	private BeefGuiLabel fuelConsumedString;
	private BeefGuiIcon reactivityIcon;
	private BeefGuiLabel reactivityString;

	private BeefGuiPowerBar powerBar;
	private BeefGuiHeatBar coreHeatBar;
	private BeefGuiHeatBar caseHeatBar;
	private BeefGuiFuelMixBar fuelMixBar;
	
	private BeefGuiIcon coolantIcon;
	private BeefGuiFluidBar coolantBar;
	private BeefGuiIcon hotFluidIcon;
	private BeefGuiFluidBar hotFluidBar;
	
	public GuiReactorStatus(Container container, TileEntityReactorPart tileEntityReactorPart) {
		super(container);
		
		ySize = 186;
		
		this.part = tileEntityReactorPart;
		this.reactor = part.getReactorController();
	}
	
	// Add controls, etc.
	@Override
	public void initGui() {
		super.initGui();
		
		int xCenter = guiLeft + this.xSize / 2;
		int yCenter = this.ySize / 2;
		
		btnReactorOn = new GuiIconButton(0, guiLeft + 4, guiTop + 164, 18, 18, ClientProxy.GuiIcons.getIcon("On_off"));
		btnReactorOff = new GuiIconButton(1, guiLeft + 22, guiTop + 164, 18, 18, ClientProxy.GuiIcons.getIcon("Off_off"));
		
		btnReactorOn.setTooltip(new String[] { EnumChatFormatting.AQUA + "Activate Reactor" });
		btnReactorOff.setTooltip(new String[] { EnumChatFormatting.AQUA + "Deactivate Reactor", "Residual heat will still", "generate power/consume coolant,", "until the reactor cools." });
		
		btnWasteAutoEject = new GuiIconButton(2, guiLeft + 4, guiTop + 144, 18, 18, ClientProxy.GuiIcons.getIcon("wasteEject_off"));
		btnWasteManual = new GuiIconButton(4, guiLeft + 22, guiTop + 144, 18, 18, ClientProxy.GuiIcons.getIcon("wasteManual_off"));
		btnWasteEject = new GuiIconButton(5, guiLeft + 50, guiTop + 144, 18, 18, ClientProxy.GuiIcons.getIcon("wasteEject"));

		btnWasteEject.visible = false;

		btnWasteAutoEject.setTooltip(new String[] { EnumChatFormatting.AQUA + "Auto-Eject Waste", "Waste in the core will be ejected", "as soon as possible" });
		btnWasteManual.setTooltip(new String[] { EnumChatFormatting.AQUA + "Do Not Auto-Eject Waste", EnumChatFormatting.LIGHT_PURPLE + "Waste must be manually ejected.", "", "Ejection can be done from this", "screen, or via rednet,", "redstone or computer port signals."});
		btnWasteEject.setTooltip(new String[] { EnumChatFormatting.AQUA + "Eject Waste Now", "Ejects waste from the core", "into access ports.", "Each 1000mB waste = 1 ingot", "", "SHIFT: Dump excess waste, if any"});
		
		registerControl(btnReactorOn);
		registerControl(btnReactorOff);
		registerControl(btnWasteAutoEject);
		registerControl(btnWasteManual);
		registerControl(btnWasteEject);
		
		int leftX = guiLeft + 4;
		int topY = guiTop + 4;
		
		titleString = new BeefGuiLabel(this, "Reactor Control", leftX, topY);
		topY += titleString.getHeight() + 4;
		
		heatIcon = new BeefGuiIcon(this, leftX - 2, topY, 16, 16, ClientProxy.GuiIcons.getIcon("temperature"), new String[] { EnumChatFormatting.AQUA + "Core Temperature", "", "Temperature inside the reactor core.", "Higher temperatures increase fuel burnup." });
		heatString = new BeefGuiLabel(this, "", leftX + 22, topY + 4);
		topY += heatIcon.getHeight() + 5;
		
		outputIcon = new BeefGuiIcon(this, leftX + 1, topY);
		outputString = new BeefGuiLabel(this, "", leftX + 22, topY + 4);
		topY += outputIcon.getHeight() + 5;
		
		fuelConsumedIcon = new BeefGuiIcon(this, leftX + 1, topY, 16, 16, ClientProxy.GuiIcons.getIcon("fuelUsageRate"), new String[] { EnumChatFormatting.AQUA + "Fuel Burnup Rate", "", "The rate at which fuel is", "fissioned into waste in the core."});
		fuelConsumedString = new BeefGuiLabel(this, "", leftX + 22, topY + 4);
		topY += fuelConsumedIcon.getHeight() + 5;

		reactivityIcon = new BeefGuiIcon(this, leftX, topY, 16, 16, ClientProxy.GuiIcons.getIcon("reactivity"), new String[] { EnumChatFormatting.AQUA + "Fuel Reactivity", "", "How heavily irradiated the core is.", "Higher levels of radiation", "reduce fuel burnup."});
		reactivityString = new BeefGuiLabel(this, "", leftX + 22, topY + 4);
		topY += reactivityIcon.getHeight() + 6;

		statusString = new BeefGuiLabel(this, "", leftX+1, topY);
		topY += statusString.getHeight() + 4;
		
		
		powerBar = new BeefGuiPowerBar(this, guiLeft + 152, guiTop + 22, this.reactor);
		coreHeatBar = new BeefGuiHeatBar(this, guiLeft + 130, guiTop + 22, EnumChatFormatting.AQUA + "Core Heat", new String[] { "Heat of the reactor's fuel.", "High heat raises fuel usage.", "", "Core heat is transferred to", "the casing. Transfer rate", "is based on the design of", "the reactor's interior."});
		caseHeatBar = new BeefGuiHeatBar(this, guiLeft + 108, guiTop + 22, EnumChatFormatting.AQUA + "Casing Heat", new String[] { "Heat of the reactor's casing.", "High heat raises energy output", "and coolant conversion."});
		fuelMixBar = new BeefGuiFuelMixBar(this, guiLeft + 86, guiTop + 22, this.reactor);

		coolantIcon = new BeefGuiIcon(this, guiLeft + 132, guiTop + 91, 16, 16, ClientProxy.GuiIcons.getIcon("coolantIn"), new String[] { EnumChatFormatting.AQUA + "Coolant Fluid Tank", "", "Casing heat will superheat", "coolant in this tank." });
		coolantBar = new BeefGuiFluidBar(this, guiLeft + 131, guiTop + 108, this.reactor, MultiblockReactor.FLUID_COOLANT);
		
		hotFluidIcon = new BeefGuiIcon(this, guiLeft + 154, guiTop + 91, 16, 16, ClientProxy.GuiIcons.getIcon("hotFluidOut"), new String[] { EnumChatFormatting.AQUA + "Hot Fluid Tank", "", "Superheated coolant", "will pump into this tank,", "and must be piped out", "via coolant ports" });
		hotFluidBar = new BeefGuiFluidBar(this, guiLeft + 153, guiTop + 108, this.reactor, MultiblockReactor.FLUID_SUPERHEATED);
		
		registerControl(titleString);
		registerControl(statusString);
		registerControl(heatIcon);
		registerControl(heatString);
		registerControl(outputIcon);
		registerControl(outputString);
		registerControl(fuelConsumedIcon);
		registerControl(fuelConsumedString);
		registerControl(reactivityIcon);
		registerControl(reactivityString);
		registerControl(powerBar);
		registerControl(coreHeatBar);
		registerControl(caseHeatBar);
		registerControl(fuelMixBar);
		registerControl(coolantBar);
		registerControl(hotFluidBar);
		registerControl(coolantIcon);
		registerControl(hotFluidIcon);
		
		updateIcons();
	}

	@Override
	public ResourceLocation getGuiBackground() {
		return new ResourceLocation(BigReactors.GUI_DIRECTORY + "ReactorController.png");
	}

	@Override
	public void updateScreen() {
		super.updateScreen();
		
		updateIcons();
		
		if(reactor.isActive()) {
			statusString.setLabelText("Status: " + EnumChatFormatting.DARK_GREEN + "Online");
		}
		else {
			statusString.setLabelText("Status: " + EnumChatFormatting.DARK_RED + "Offline");
		}
		
		outputString.setLabelText(getFormattedOutputString());
		if(reactor.isPassivelyCooled()) {
			outputString.setLabelTooltip(String.format("%.2f flux per tick", reactor.getEnergyGeneratedLastTick()));
		}
		else {
			outputString.setLabelTooltip(String.format("%.0f millibuckets per tick", reactor.getEnergyGeneratedLastTick()));
		}

		heatString.setLabelText(Integer.toString((int)reactor.getFuelHeat()) + " C");
		coreHeatBar.setHeat(reactor.getFuelHeat());
		caseHeatBar.setHeat(reactor.getReactorHeat());

		float fuelConsumption = reactor.getFuelConsumedLastTick();
		fuelConsumedString.setLabelText(StaticUtils.Strings.formatMillibuckets(fuelConsumption) + "/t");
		fuelConsumedString.setLabelTooltip(getFuelConsumptionTooltip(fuelConsumption));

		reactivityString.setLabelText(String.format("%2.0f%%", reactor.getFuelFertility() * 100f));
	}
	
	@Override
	protected void actionPerformed(GuiButton button) {
		CoordTriplet saveDelegate = reactor.getReferenceCoord();
		if(button.id == 0 || button.id == 1) {
			boolean newSetting = button.id == 0;
			if(newSetting != reactor.isActive()) {
                CommonPacketHandler.INSTANCE.sendToServer(new MultiblockNetworkHandlerMessageServer(Packets.MultiblockActivateButton, saveDelegate.x, saveDelegate.y, saveDelegate.z, newSetting));
			}
		}
		else if(button.id >= 2 && button.id <= 4) {
			WasteEjectionSetting newEjectionSetting;
			switch(button.id) {
			case 4:
				newEjectionSetting = WasteEjectionSetting.kManual;
				break;
			default:
				newEjectionSetting = WasteEjectionSetting.kAutomatic;
				break;
			}
			
			if(reactor.getWasteEjection() != newEjectionSetting) {
                CommonPacketHandler.INSTANCE.sendToServer(new MultiblockNetworkHandlerMessageServer(Packets.ReactorWasteEjectionSettingUpdate, saveDelegate.x, saveDelegate.y, saveDelegate.z, newEjectionSetting.ordinal()));
			}
		}
		else if(button.id == 5) {
            CommonPacketHandler.INSTANCE.sendToServer(new MultiblockNetworkHandlerMessageServer(Packets.ReactorEjectButton, saveDelegate.x, saveDelegate.y, saveDelegate.z, false, isShiftKeyDown(), false));
		}
	}
	
	protected void updateIcons() {
		if(reactor.isActive()) {
			btnReactorOn.setIcon(ClientProxy.GuiIcons.getIcon(BeefGuiIconManager.ON_ON));
			btnReactorOff.setIcon(ClientProxy.GuiIcons.getIcon(BeefGuiIconManager.OFF_OFF));
		}
		else {
			btnReactorOn.setIcon(ClientProxy.GuiIcons.getIcon(BeefGuiIconManager.ON_OFF));
			btnReactorOff.setIcon(ClientProxy.GuiIcons.getIcon(BeefGuiIconManager.OFF_ON));
		}
		
		if(reactor.isPassivelyCooled()) {
			outputIcon.setIcon(ClientProxy.GuiIcons.getIcon("energyOutput"));
			outputIcon.setTooltip(passivelyCooledTooltip);
			
			coolantIcon.visible = false;
			coolantBar.visible = false;
			hotFluidIcon.visible = false;
			hotFluidBar.visible = false;
		}
		else {
			outputIcon.setIcon(ClientProxy.GuiIcons.getIcon("hotFluidOut"));
			outputIcon.setTooltip(activelyCooledTooltip);
			
			coolantIcon.visible = true;
			coolantBar.visible = true;
			hotFluidIcon.visible = true;
			hotFluidBar.visible = true;
		}

		
		switch(reactor.getWasteEjection()) {
		case kAutomatic:
			btnWasteAutoEject.setIcon(ClientProxy.GuiIcons.getIcon(BeefGuiIconManager.WASTE_EJECT_ON));
			btnWasteManual.setIcon(ClientProxy.GuiIcons.getIcon(BeefGuiIconManager.WASTE_MANUAL_OFF));
			btnWasteEject.visible = false;
			break;
		case kManual:
		default:
			btnWasteAutoEject.setIcon(ClientProxy.GuiIcons.getIcon(BeefGuiIconManager.WASTE_EJECT_OFF));
			btnWasteManual.setIcon(ClientProxy.GuiIcons.getIcon(BeefGuiIconManager.WASTE_MANUAL_ON));
			btnWasteEject.visible = true;
			break;
		}
	}
	
	private static final String[] passivelyCooledTooltip = new String[] {
		EnumChatFormatting.AQUA + "Energy Output",
		"",
		"This reactor is passively cooled",
		"and generates energy directly from",
		"the heat of its core."
	};
	
	private static final String[] activelyCooledTooltip = new String[] {
		EnumChatFormatting.AQUA + "Hot Fluid Output",
		"",
		"This reactor is actively cooled",
		"by a fluid, such as water, which",
		"is superheated by the core."
	};

	private String getFormattedOutputString() {
		float number = reactor.getEnergyGeneratedLastTick(); // Also doubles as fluid vaporized last tick

		if(reactor.isPassivelyCooled()) {
			return StaticUtils.Strings.formatRF(number) + "/t";
		}
		else {
			return StaticUtils.Strings.formatMillibuckets(number) + "/t";			
		}
	}
	
	private String getFuelConsumptionTooltip(float fuelConsumption) {
		if(fuelConsumption <= 0.000001f) { return "0 millibuckets per tick"; }
		
		int exp = (int)Math.log10(fuelConsumption);
		
		int decimalPlaces = 0;
		if(exp < 1) {
			decimalPlaces = Math.abs(exp) + 2;
			return String.format("%." + Integer.toString(decimalPlaces) + "f millibuckets per tick", fuelConsumption);
		}
		else {
			return String.format("%.0f millibuckets per tick", fuelConsumption);
		}
	}
}

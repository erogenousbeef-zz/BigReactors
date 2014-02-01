package erogenousbeef.bigreactors.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.common.network.PacketDispatcher;
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
import erogenousbeef.bigreactors.net.PacketWrapper;
import erogenousbeef.bigreactors.net.Packets;
import erogenousbeef.bigreactors.utils.FloatAverager;
import erogenousbeef.core.common.CoordTriplet;
import erogenousbeef.bigreactors.gui.GuiConstants;

public class GuiReactorStatus extends BeefGuiBase {

	private GuiIconButton btnReactorOn;
	private GuiIconButton btnReactorOff;
	private GuiIconButton btnWasteAutoEject;
	private GuiIconButton btnWasteReplaceOnly;
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
	
	private FloatAverager averagedHeat;
	private FloatAverager averagedRfOutput;
	private FloatAverager averagedFuelConsumption;
	
	public GuiReactorStatus(Container container, TileEntityReactorPart tileEntityReactorPart) {
		super(container);
		
		ySize = 186;
		
		this.part = tileEntityReactorPart;
		this.reactor = part.getReactorController();
		
		this.averagedHeat = new FloatAverager(30);
		this.averagedRfOutput = new FloatAverager(30); // About 1.5 seconds
		this.averagedFuelConsumption = new FloatAverager(30);
	}
	
	// Add controls, etc.
	@Override
	public void initGui() {
		super.initGui();
		
		int xCenter = guiLeft + this.xSize / 2;
		int yCenter = this.ySize / 2;
		
		btnReactorOn = new GuiIconButton(0, guiLeft + 4, 190, 18, 18, ClientProxy.GuiIcons.getIcon("On_off"));
		btnReactorOff = new GuiIconButton(1, guiLeft + 22, 190, 18, 18, ClientProxy.GuiIcons.getIcon("Off_off"));
		
		btnReactorOn.setTooltip(new String[] { GuiConstants.LITECYAN_TEXT + "Activate Reactor" });
		btnReactorOff.setTooltip(new String[] { GuiConstants.LITECYAN_TEXT + "Deactivate Reactor", "Residual heat will still", "generate power/consume coolant,", "until the reactor cools." });
		
		btnWasteAutoEject = new GuiIconButton(2, guiLeft + 4, 170, 18, 18, ClientProxy.GuiIcons.getIcon("wasteEject_off"));
		btnWasteReplaceOnly = new GuiIconButton(3, guiLeft + 22, 170, 18, 18, ClientProxy.GuiIcons.getIcon("wasteReplace_off"));
		btnWasteManual = new GuiIconButton(4, guiLeft + 40, 170, 18, 18, ClientProxy.GuiIcons.getIcon("Off_off"));
		btnWasteEject = new GuiIconButton(5, guiLeft + 80, 170, 18, 18, ClientProxy.GuiIcons.getIcon("wasteEject"));

		btnWasteEject.drawButton = false;

		btnWasteAutoEject.setTooltip(new String[] { GuiConstants.LITECYAN_TEXT + "Auto-Eject Waste", "Waste in the core will be ejected", "as soon as possible" });
		btnWasteReplaceOnly.setTooltip(new String[] { GuiConstants.LITECYAN_TEXT + "Replace Waste", "Waste in the core will be ejected", "only when it can be replaced", "with fresh fuel" });
		btnWasteManual.setTooltip(new String[] { GuiConstants.LITECYAN_TEXT + "Do Not Auto-Eject Waste", GuiConstants.VIOLET_TEXT + "Waste must be manually ejected.", "", "Ejection can be done from this", "screen, or via rednet,", "redstone or computer port signals."});
		btnWasteEject.setTooltip(new String[] { GuiConstants.LITECYAN_TEXT + "Eject Waste Now", "Ejects waste from the core", "into access ports.", "Each 1000mB waste = 1 ingot", "", "SHIFT: Dump excess waste, if any"});
		
		registerControl(btnReactorOn);
		registerControl(btnReactorOff);
		registerControl(btnWasteAutoEject);
		registerControl(btnWasteReplaceOnly);
		registerControl(btnWasteManual);
		registerControl(btnWasteEject);
		
		int leftX = guiLeft + 4;
		int topY = guiTop + 4;
		
		titleString = new BeefGuiLabel(this, "Reactor Control", leftX, topY);
		topY += titleString.getHeight() + 4;
		
		heatIcon = new BeefGuiIcon(this, leftX - 2, topY, ClientProxy.GuiIcons.getIcon("temperature"), new String[] { GuiConstants.LITECYAN_TEXT + "Core Temperature", "", "Temperature inside the reactor core.", "Higher temperatures increase fuel burnup." });
		heatString = new BeefGuiLabel(this, "", leftX + 22, topY + 4);
		topY += heatIcon.getHeight() + 5;
		
		outputIcon = new BeefGuiIcon(this, leftX + 1, topY);
		outputString = new BeefGuiLabel(this, "", leftX + 22, topY + 4);
		topY += outputIcon.getHeight() + 5;
		
		fuelConsumedIcon = new BeefGuiIcon(this, leftX + 1, topY, ClientProxy.GuiIcons.getIcon("fuelUsageRate"), new String[] { GuiConstants.LITECYAN_TEXT + "Fuel Burnup Rate", "", "The rate at which fuel is", "fissioned into waste in the core."});
		fuelConsumedString = new BeefGuiLabel(this, "", leftX + 22, topY + 4);
		topY += fuelConsumedIcon.getHeight() + 5;

		reactivityIcon = new BeefGuiIcon(this, leftX, topY, ClientProxy.GuiIcons.getIcon("reactivity"), new String[] { GuiConstants.LITECYAN_TEXT + "Fuel Reactivity", "", "How heavily irradiated the core is.", "Higher levels of radiation", "reduce fuel burnup."});
		reactivityString = new BeefGuiLabel(this, "", leftX + 22, topY + 4);
		topY += reactivityIcon.getHeight() + 6;

		statusString = new BeefGuiLabel(this, "", leftX+1, topY);
		topY += statusString.getHeight() + 4;
		
		
		powerBar = new BeefGuiPowerBar(this, guiLeft + 152, guiTop + 22, this.reactor);
		coreHeatBar = new BeefGuiHeatBar(this, guiLeft + 130, guiTop + 22, GuiConstants.LITECYAN_TEXT + "Core Heat", new String[] { "Heat of the reactor's fuel.", "High heat raises fuel usage.", "", "Core heat is transferred to", "the casing. Transfer rate", "is based on the design of", "the reactor's interior."});
		caseHeatBar = new BeefGuiHeatBar(this, guiLeft + 108, guiTop + 22, GuiConstants.LITECYAN_TEXT + "Casing Heat", new String[] { "Heat of the reactor's casing.", "High heat raises energy output", "and coolant conversion."});
		fuelMixBar = new BeefGuiFuelMixBar(this, guiLeft + 86, guiTop + 22, this.reactor);

		coolantIcon = new BeefGuiIcon(this, guiLeft + 132, guiTop + 91, ClientProxy.GuiIcons.getIcon("coolantIn"), new String[] { GuiConstants.LITECYAN_TEXT + "Coolant Fluid Tank", "", "Casing heat will superheat", "coolant in this tank." });
		coolantBar = new BeefGuiFluidBar(this, guiLeft + 131, guiTop + 108, this.reactor, MultiblockReactor.FLUID_COOLANT);
		
		hotFluidIcon = new BeefGuiIcon(this, guiLeft + 154, guiTop + 91, ClientProxy.GuiIcons.getIcon("hotFluidOut"), new String[] { GuiConstants.LITECYAN_TEXT + "Hot Fluid Tank", "", "Superheated coolant", "will pump into this tank,", "and must be piped out", "via coolant ports" });
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
		
		averagedHeat.setAll(reactor.getReactorHeat());
		averagedRfOutput.setAll(reactor.getEnergyGeneratedLastTick());
		averagedFuelConsumption.setAll(reactor.getFuelConsumedLastTick());
		
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
			statusString.setLabelText("Status: " + GuiConstants.DARKGREEN_TEXT + "Online");
		}
		else {
			statusString.setLabelText("Status: " + GuiConstants.DARKRED_TEXT + "Offline");
		}
		
		// Grab averaged values
		averagedRfOutput.add(reactor.getEnergyGeneratedLastTick());
		averagedHeat.add(reactor.getFuelHeat());
		averagedFuelConsumption.add(reactor.getFuelConsumedLastTick());
		
		float averagedOutput = averagedRfOutput.average();
		if(averagedOutput >= 100f) {
			outputString.setLabelText(String.format("%1.0f RF/t", averagedRfOutput.average()));			
		}
		else {
			outputString.setLabelText(String.format("%1.1f RF/t", averagedRfOutput.average()));			
		}

		heatString.setLabelText(Integer.toString((int)averagedHeat.average()) + " C");
		coreHeatBar.setHeat(reactor.getFuelHeat());
		caseHeatBar.setHeat(reactor.getReactorHeat());
		
		float averagedConsumption = averagedFuelConsumption.average();
		if(averagedConsumption < 0.1f) {
			fuelConsumedString.setLabelText(String.format("%1.3f mB/t", averagedFuelConsumption.average()));
		}
		else if(averagedConsumption < 1f) {
			fuelConsumedString.setLabelText(String.format("%1.2f mB/t", averagedFuelConsumption.average()));
		}
		else if(averagedConsumption < 10f) {
			fuelConsumedString.setLabelText(String.format("%1.1f mB/t", averagedFuelConsumption.average()));
		}
		else {
			fuelConsumedString.setLabelText(String.format("%1.0f mB/t", averagedFuelConsumption.average()));
		}
		
		reactivityString.setLabelText(String.format("%2.0f%%", reactor.getFuelFertility() * 100f));
	}
	
	@Override
	protected void actionPerformed(GuiButton button) {
		CoordTriplet saveDelegate = reactor.getReferenceCoord();
		if(button.id == 0 || button.id == 1) {
			boolean newSetting = button.id == 0;
			if(newSetting != reactor.isActive()) {
				PacketDispatcher.sendPacketToServer(PacketWrapper.createPacket(BigReactors.CHANNEL, Packets.MultiblockActivateButton,
						new Object[] { saveDelegate.x, saveDelegate.y, saveDelegate.z, newSetting }));
			}
		}
		else if(button.id >= 2 && button.id <= 4) {
			WasteEjectionSetting newEjectionSetting;
			switch(button.id) {
			case 3:
				newEjectionSetting = WasteEjectionSetting.kAutomaticOnlyIfCanReplace;
				break;
			case 4:
				newEjectionSetting = WasteEjectionSetting.kManual;
				break;
			default:
				newEjectionSetting = WasteEjectionSetting.kAutomatic;
				break;
			}
			
			if(reactor.getWasteEjection() != newEjectionSetting) {
				PacketDispatcher.sendPacketToServer(PacketWrapper.createPacket(BigReactors.CHANNEL, Packets.ReactorWasteEjectionSettingUpdate, 
						new Object[] { saveDelegate.x, saveDelegate.y, saveDelegate.z, newEjectionSetting.ordinal() } ));
			}
		}
		else if(button.id == 5) {
			PacketDispatcher.sendPacketToServer(PacketWrapper.createPacket(BigReactors.CHANNEL, Packets.ReactorEjectButton,
						new Object[] { saveDelegate.x, saveDelegate.y, saveDelegate.z, true, isShiftKeyDown() }));
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

		btnWasteEject.drawButton = false;
		
		switch(reactor.getWasteEjection()) {
		case kAutomatic:
			btnWasteAutoEject.setIcon(ClientProxy.GuiIcons.getIcon(BeefGuiIconManager.WASTE_EJECT_ON));
			btnWasteReplaceOnly.setIcon(ClientProxy.GuiIcons.getIcon(BeefGuiIconManager.WASTE_REPLACE_OFF));
			btnWasteManual.setIcon(ClientProxy.GuiIcons.getIcon(BeefGuiIconManager.OFF_OFF));
			break;
		case kManual:
			btnWasteAutoEject.setIcon(ClientProxy.GuiIcons.getIcon(BeefGuiIconManager.WASTE_EJECT_OFF));
			btnWasteReplaceOnly.setIcon(ClientProxy.GuiIcons.getIcon(BeefGuiIconManager.WASTE_REPLACE_OFF));
			btnWasteManual.setIcon(ClientProxy.GuiIcons.getIcon(BeefGuiIconManager.OFF_ON));
			btnWasteEject.drawButton = true;
			break;
		default:
			btnWasteAutoEject.setIcon(ClientProxy.GuiIcons.getIcon(BeefGuiIconManager.WASTE_EJECT_OFF));
			btnWasteReplaceOnly.setIcon(ClientProxy.GuiIcons.getIcon(BeefGuiIconManager.WASTE_REPLACE_ON));
			btnWasteManual.setIcon(ClientProxy.GuiIcons.getIcon(BeefGuiIconManager.OFF_OFF));
			break;
		}
	}
	
	private static final String[] passivelyCooledTooltip = new String[] {
		GuiConstants.LITECYAN_TEXT + "Energy Output",
		"",
		"This reactor is passively cooled",
		"and generates energy directly from",
		"the heat of its core."
	};
	
	private static final String[] activelyCooledTooltip = new String[] {
		GuiConstants.LITECYAN_TEXT + "Hot Fluid Output",
		"",
		"This reactor is actively cooled",
		"by a fluid, such as water, which",
		"is superheated by the core."
	};
}

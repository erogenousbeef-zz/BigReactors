package erogenousbeef.bigreactors.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.common.network.PacketDispatcher;
import erogenousbeef.bigreactors.client.ClientProxy;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.multiblock.MultiblockTurbine;
import erogenousbeef.bigreactors.common.multiblock.MultiblockTurbine.VentStatus;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityTurbinePartBase;
import erogenousbeef.bigreactors.gui.BeefGuiIconManager;
import erogenousbeef.bigreactors.gui.GuiConstants;
import erogenousbeef.bigreactors.gui.controls.BeefGuiFluidBar;
import erogenousbeef.bigreactors.gui.controls.BeefGuiIcon;
import erogenousbeef.bigreactors.gui.controls.BeefGuiLabel;
import erogenousbeef.bigreactors.gui.controls.BeefGuiPowerBar;
import erogenousbeef.bigreactors.gui.controls.BeefGuiRpmBar;
import erogenousbeef.bigreactors.gui.controls.GuiIconButton;
import erogenousbeef.bigreactors.net.PacketWrapper;
import erogenousbeef.bigreactors.net.Packets;
import erogenousbeef.core.common.CoordTriplet;

public class GuiTurbineController extends BeefGuiBase {

	TileEntityTurbinePartBase part;
	MultiblockTurbine turbine;
	
	private BeefGuiLabel titleString;
	private BeefGuiLabel statusString;
	
	private BeefGuiIcon speedIcon;
	private BeefGuiLabel speedString;
	
	private BeefGuiIcon energyGeneratedIcon;
	private BeefGuiLabel energyGeneratedString;
	
	private BeefGuiIcon rotorEfficiencyIcon;
	private BeefGuiLabel rotorEfficiencyString;

	private BeefGuiIcon powerIcon;
	private BeefGuiPowerBar powerBar;
	private BeefGuiIcon steamIcon;
	private BeefGuiFluidBar steamBar;
	private BeefGuiIcon waterIcon;
	private BeefGuiFluidBar waterBar;
	
	private BeefGuiIcon rpmIcon;
	private BeefGuiRpmBar rpmBar;

	private BeefGuiLabel governorString;
	private GuiIconButton btnGovernorUp;
	private GuiIconButton btnGovernorDown;
	
	private GuiIconButton btnActivate;
	private GuiIconButton btnDeactivate;
	
	private GuiIconButton btnVentAll;
	private GuiIconButton btnVentOverflow;
	private GuiIconButton btnVentNone;
	
	public GuiTurbineController(Container container, TileEntityTurbinePartBase part) {
		super(container);
		
		this.part = part;
		turbine = part.getTurbine();
	}

	@Override
	public ResourceLocation getGuiBackground() {
		return new ResourceLocation(BigReactors.GUI_DIRECTORY + "TurbineController.png");
	}
	
	// Add controls, etc.
	@Override
	public void initGui() {
		super.initGui();
		
		int leftX = guiLeft + 4;
		int topY = guiTop + 4;
		
		titleString = new BeefGuiLabel(this, "Turbine Control", leftX, topY);
		topY += titleString.getHeight() + 4;
		
		speedIcon = new BeefGuiIcon(this, leftX + 1, topY, ClientProxy.GuiIcons.getIcon("rpm"), new String[] { GuiConstants.LITECYAN_TEXT + "Rotor Speed", "", "Speed of the rotor in", "revolutions per minute.", "", "Rotors perform best at 900", "or 1800 RPM.", "", "Speeds over 2000PM are overspeed", "and may cause a turbine to", "fail catastrophically." });
		speedString = new BeefGuiLabel(this, "", leftX + 22, topY + 4);
		topY += speedIcon.getHeight() + 4;

		energyGeneratedIcon = new BeefGuiIcon(this, leftX+1, topY, ClientProxy.GuiIcons.getIcon("energyOutput"), new String[] { GuiConstants.LITECYAN_TEXT + "Energy Output", "", "Turbines generate energy via", "metal induction coils placed", "around a spinning rotor.", "More, or higher-quality, coils", "generate energy faster."});
		energyGeneratedString = new BeefGuiLabel(this, "", leftX + 22, topY + 4);
		topY += energyGeneratedIcon.getHeight() + 4;
		
		rotorEfficiencyIcon = new BeefGuiIcon(this, leftX + 1, topY, ClientProxy.GuiIcons.getIcon("rotorEfficiency"), new String[] { GuiConstants.LITECYAN_TEXT + "Rotor Efficiency", "", "Rotor blades can only fully", "capture energy from 15mB of", "fluid per blade.", "", "Efficiency drops if the flow", "of input fluid rises past", "capacity."});
		rotorEfficiencyString = new BeefGuiLabel(this, "", leftX + 22, topY + 4);
		topY += rotorEfficiencyIcon.getHeight() + 4;

		statusString = new BeefGuiLabel(this, "", leftX, topY);
		topY += statusString.getHeight() + 4;
		
		powerIcon = new BeefGuiIcon(this, guiLeft + 153, guiTop + 4, ClientProxy.GuiIcons.getIcon("energyStored"), new String[] { GuiConstants.LITECYAN_TEXT + "Energy Storage" });
		powerBar = new BeefGuiPowerBar(this, guiLeft + 152, guiTop + 22, this.turbine);
		
		steamIcon = new BeefGuiIcon(this, guiLeft + 113, guiTop + 4, ClientProxy.GuiIcons.getIcon("hotFluidIn"), new String[] { GuiConstants.LITECYAN_TEXT + "Intake Fluid Tank" });
		steamBar = new BeefGuiFluidBar(this, guiLeft + 112, guiTop + 22, turbine, MultiblockTurbine.TANK_INPUT);

		waterIcon = new BeefGuiIcon(this, guiLeft + 133, guiTop + 4, ClientProxy.GuiIcons.getIcon("coolantOut"), new String[] { GuiConstants.LITECYAN_TEXT + "Exhaust Fluid Tank" });
		waterBar = new BeefGuiFluidBar(this, guiLeft + 132, guiTop + 22, turbine, MultiblockTurbine.TANK_OUTPUT);

		rpmIcon = new BeefGuiIcon(this, guiLeft + 93, guiTop + 4, ClientProxy.GuiIcons.getIcon("rpm"), new String[] { GuiConstants.LITECYAN_TEXT + "Rotor Speed" });
		rpmBar = new BeefGuiRpmBar(this, guiLeft + 92, guiTop + 22, turbine, "Rotor Speed", new String[] {"Rotors perform best at", "900 or 1800 RPM.", "", "Rotors kept overspeed for too", "long may fail.", "", "Catastrophically."});
	
		governorString = new BeefGuiLabel(this, "", guiLeft + 4, guiTop + 110);
		btnGovernorUp   = new GuiIconButton(2, guiLeft + 110, guiTop + 104, 18, 18, ClientProxy.GuiIcons.getIcon("upArrow"),   new String[] { GuiConstants.LITECYAN_TEXT + "Increase Max Flow Rate", "", "Higher flow rates will increase", "rotor speed.", "", "SHIFT: +10 mB", "CTRL: +100mB", "CTRL+SHIFT: +1000mB"});
		btnGovernorDown = new GuiIconButton(3, guiLeft + 128, guiTop + 104, 18, 18, ClientProxy.GuiIcons.getIcon("downArrow"), new String[] { GuiConstants.LITECYAN_TEXT + "Decrease Max Flow Rate", "", "Lower flow rates will decrease", "rotor speed.",  "", "SHIFT: -10 mB", "CTRL: -100mB", "CTRL+SHIFT: -1000mB"});

		btnActivate = new GuiIconButton(0, guiLeft + 4, guiTop + 144, 18, 18, ClientProxy.GuiIcons.getIcon("On_off"), new String[] { GuiConstants.LITECYAN_TEXT + "Activate Turbine", "", "Enables flow of intake fluid to rotor.", "Fluid flow will spin up the rotor." });
		btnDeactivate = new GuiIconButton(1, guiLeft + 24, guiTop + 144, 18, 18, ClientProxy.GuiIcons.getIcon("Off_off"), new String[] { GuiConstants.LITECYAN_TEXT + "Deactivate Turbine", "", "Disables flow of intake fluid to rotor.", "The rotor will spin down." });
		
		btnVentAll = new GuiIconButton(4, guiLeft + 4, guiTop + 124, 18, 18, ClientProxy.GuiIcons.getIcon("ventAllOff"), new String[] { GuiConstants.LITECYAN_TEXT + "Vent: All Exhaust", "", "Dump all exhaust fluids.", "The exhaust fluid tank", "will not fill."});
		btnVentOverflow = new GuiIconButton(5, guiLeft + 24, guiTop + 124, 18, 18, ClientProxy.GuiIcons.getIcon("ventOverflowOff"), new String[] { GuiConstants.LITECYAN_TEXT + "Vent: Overflow Only", "", "Dump excess exhaust fluids.", "Excess fluids will be lost", "if exhaust fluid tank is full."});
		btnVentNone = new GuiIconButton(6, guiLeft + 44, guiTop + 124, 18, 18, ClientProxy.GuiIcons.getIcon("ventNoneOff"), new String[] { GuiConstants.LITECYAN_TEXT + "Vent: Closed", "", "Preserve all exhaust fluids.", "Turbine will slow or halt", "fluid intake if exhaust", "fluid tank is full."});
		
		registerControl(titleString);
		registerControl(statusString);
		registerControl(speedIcon);
		registerControl(speedString);
		registerControl(energyGeneratedIcon);
		registerControl(energyGeneratedString);
		registerControl(rotorEfficiencyIcon);
		registerControl(rotorEfficiencyString);
		registerControl(powerBar);
		registerControl(steamBar);
		registerControl(waterBar);
		registerControl(powerIcon);
		registerControl(steamIcon);
		registerControl(waterIcon);
		registerControl(rpmIcon);
		registerControl(rpmBar);
		registerControl(governorString);
		registerControl(btnGovernorUp);
		registerControl(btnGovernorDown);
		registerControl(btnActivate);
		registerControl(btnDeactivate);
		registerControl(btnVentAll);
		registerControl(btnVentOverflow);
		registerControl(btnVentNone);

		updateStrings();
		updateTooltips();
	}

	private void updateStrings() {
		if(turbine.isActive()) {
			statusString.setLabelText("Status: " + GuiConstants.DARKGREEN_TEXT + "Active");
			btnActivate.setIcon(ClientProxy.GuiIcons.getIcon(BeefGuiIconManager.ON_ON));
			btnDeactivate.setIcon(ClientProxy.GuiIcons.getIcon(BeefGuiIconManager.OFF_OFF));
		}
		else {
			statusString.setLabelText("Status: " + GuiConstants.DARKRED_TEXT + "Inactive");
			btnActivate.setIcon(ClientProxy.GuiIcons.getIcon(BeefGuiIconManager.ON_OFF));
			btnDeactivate.setIcon(ClientProxy.GuiIcons.getIcon(BeefGuiIconManager.OFF_ON));
		}
		
		speedString.setLabelText(String.format("%.1f RPM", turbine.getRotorSpeed()));
		energyGeneratedString.setLabelText(String.format("%.0f RF/t", turbine.getEnergyGeneratedLastTick()));
		governorString.setLabelText(String.format("Max Flow: %d mB/t", turbine.getMaxIntakeRate()));
		
		if(turbine.isActive()) {
			if(turbine.getRotorEfficiencyLastTick() < 1f) {
				rotorEfficiencyString.setLabelText(String.format("%.1f%%", turbine.getRotorEfficiencyLastTick() * 100f));
			}
			else {
				rotorEfficiencyString.setLabelText("100%");
			}

			int numBlades = turbine.getNumRotorBlades();
			int fluidLastTick = turbine.getFluidConsumedLastTick();
			int neededBlades = fluidLastTick / MultiblockTurbine.inputFluidPerBlade;
			
			rotorEfficiencyString.setLabelTooltip(String.format("%d / %d blades", numBlades, neededBlades));
		}
		else {
			rotorEfficiencyString.setLabelText("Unknown");
		}
		
		switch(turbine.getVentSetting()) {
		case DoNotVent:
			btnVentNone.setIcon(ClientProxy.GuiIcons.getIcon(BeefGuiIconManager.VENT_NONE_ON));
			btnVentOverflow.setIcon(ClientProxy.GuiIcons.getIcon(BeefGuiIconManager.VENT_OVERFLOW_OFF));
			btnVentAll.setIcon(ClientProxy.GuiIcons.getIcon(BeefGuiIconManager.VENT_ALL_OFF));
			break;
		case VentOverflow:
			btnVentNone.setIcon(ClientProxy.GuiIcons.getIcon(BeefGuiIconManager.VENT_NONE_OFF));
			btnVentOverflow.setIcon(ClientProxy.GuiIcons.getIcon(BeefGuiIconManager.VENT_OVERFLOW_ON));
			btnVentAll.setIcon(ClientProxy.GuiIcons.getIcon(BeefGuiIconManager.VENT_ALL_OFF));
			break;
		default:
			// Vent all
			btnVentNone.setIcon(ClientProxy.GuiIcons.getIcon(BeefGuiIconManager.VENT_NONE_OFF));
			btnVentOverflow.setIcon(ClientProxy.GuiIcons.getIcon(BeefGuiIconManager.VENT_OVERFLOW_OFF));
			btnVentAll.setIcon(ClientProxy.GuiIcons.getIcon(BeefGuiIconManager.VENT_ALL_ON));
		}
	}

	@Override
	public void updateScreen() {
		super.updateScreen();
		
		updateStrings();
	}
	
	protected void updateTooltips() {
		
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		CoordTriplet saveDelegate = turbine.getReferenceCoord();

		if(button.id == 0 || button.id == 1) {
			boolean setActive = button.id == 0;
			if(setActive != turbine.isActive()) {
				PacketDispatcher.sendPacketToServer(PacketWrapper.createPacket(BigReactors.CHANNEL, Packets.MultiblockActivateButton,
						new Object[] { saveDelegate.x, saveDelegate.y, saveDelegate.z, setActive }));
			}
		}
		
		if(button.id == 2 || button.id == 3) {
			int exponent = 0;

			if(isShiftKeyDown()) {
				exponent += 1;
			}
			if(isCtrlKeyDown()) {
				exponent += 2;
			}

			int newMax = (int) Math.round(Math.pow(10, exponent));

			if(button.id == 3) { newMax *= -1; }
			
			newMax = Math.max(0, Math.min(turbine.getMaxIntakeRateMax(), turbine.getMaxIntakeRate() + newMax));

			if(newMax != turbine.getMaxIntakeRate()) {
				PacketDispatcher.sendPacketToServer(PacketWrapper.createPacket(BigReactors.CHANNEL, Packets.MultiblockTurbineGovernorUpdate,
						new Object[] { saveDelegate.x, saveDelegate.y, saveDelegate.z, newMax }));
			}
		}
		
		if(button.id >= 4 && button.id <= 6) {
			VentStatus newStatus;
			switch(button.id) {
			case 5:
				newStatus = VentStatus.VentOverflow;
				break;
			case 6:
				newStatus = VentStatus.DoNotVent;
				break;
			default:
				newStatus = VentStatus.VentAll;
				break;
			}
			
			if(newStatus != turbine.getVentSetting()) {
				PacketDispatcher.sendPacketToServer(PacketWrapper.createPacket(BigReactors.CHANNEL, Packets.MultiblockTurbineVentUpdate,
						new Object[] { saveDelegate.x, saveDelegate.y, saveDelegate.z, newStatus.ordinal() }));
			}
		}
	}
}

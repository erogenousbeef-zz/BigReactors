package erogenousbeef.bigreactors.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.common.network.PacketDispatcher;
import erogenousbeef.bigreactors.client.ClientProxy;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.multiblock.MultiblockTurbine;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityTurbinePartBase;
import erogenousbeef.bigreactors.common.tileentity.TileEntityDebugTurbine;
import erogenousbeef.bigreactors.gui.GuiConstants;
import erogenousbeef.bigreactors.gui.controls.BeefGuiFluidBar;
import erogenousbeef.bigreactors.gui.controls.BeefGuiIcon;
import erogenousbeef.bigreactors.gui.controls.BeefGuiLabel;
import erogenousbeef.bigreactors.gui.controls.BeefGuiPowerBar;
import erogenousbeef.bigreactors.gui.controls.BeefGuiRpmBar;
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

	private BeefGuiIcon powerIcon;
	private BeefGuiPowerBar powerBar;
	private BeefGuiIcon steamIcon;
	private BeefGuiFluidBar steamBar;
	private BeefGuiIcon waterIcon;
	private BeefGuiFluidBar waterBar;
	
	private BeefGuiIcon rpmIcon;
	private BeefGuiRpmBar rpmBar;

	private GuiButton toggleActive;
	
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

		statusString = new BeefGuiLabel(this, "", leftX, topY);
		topY += statusString.getHeight() + 4;
		
		powerIcon = new BeefGuiIcon(this, guiLeft + 153, guiTop + 4, ClientProxy.GuiIcons.getIcon("energyStored"), new String[] { GuiConstants.LITECYAN_TEXT + "Energy Storage" });
		powerBar = new BeefGuiPowerBar(this, guiLeft + 152, guiTop + 22, this.turbine);
		
		steamIcon = new BeefGuiIcon(this, guiLeft + 113, guiTop + 4, ClientProxy.GuiIcons.getIcon("hotFluidIn"), new String[] { GuiConstants.LITECYAN_TEXT + "Hot Fluid Tank" });
		steamBar = new BeefGuiFluidBar(this, guiLeft + 112, guiTop + 22, turbine, MultiblockTurbine.TANK_INPUT);

		waterIcon = new BeefGuiIcon(this, guiLeft + 133, guiTop + 4, ClientProxy.GuiIcons.getIcon("coolantOut"), new String[] { GuiConstants.LITECYAN_TEXT + "Cold Fluid Tank" });
		waterBar = new BeefGuiFluidBar(this, guiLeft + 132, guiTop + 22, turbine, MultiblockTurbine.TANK_OUTPUT);

		rpmIcon = new BeefGuiIcon(this, guiLeft + 93, guiTop + 4, ClientProxy.GuiIcons.getIcon("rpm"), new String[] { GuiConstants.LITECYAN_TEXT + "Rotor Speed" });
		rpmBar = new BeefGuiRpmBar(this, guiLeft + 92, guiTop + 22, turbine, "Rotor Speed", new String[] {"Rotors perform best at", "900 or 1800 RPM.", "", "Rotors kept overspeed for too", "long may fail.", "", "Catastrophically."});
	
		toggleActive = new GuiButton(1, guiLeft + 4, guiTop + 124, 70, 20, "Activate");
		
		registerControl(titleString);
		registerControl(statusString);
		registerControl(speedIcon);
		registerControl(speedString);
		registerControl(energyGeneratedIcon);
		registerControl(energyGeneratedString);
		registerControl(powerBar);
		registerControl(steamBar);
		registerControl(waterBar);
		registerControl(powerIcon);
		registerControl(steamIcon);
		registerControl(waterIcon);
		registerControl(rpmIcon);
		registerControl(rpmBar);
		registerControl(toggleActive);

		updateStrings();
		updateTooltips();
	}

	private void updateStrings() {
		if(turbine.isActive()) {
			statusString.setLabelText("Status: " + GuiConstants.DARKGREEN_TEXT + "Active");
			toggleActive.displayString = "Deactivate";
		}
		else {
			statusString.setLabelText("Status: " + GuiConstants.DARKRED_TEXT + "Inactive");
			toggleActive.displayString = "Activate";
		}
		
		speedString.setLabelText(String.format("%.1f RPM", turbine.getRotorSpeed()));
		energyGeneratedString.setLabelText(String.format("%.0f RF/t", turbine.getEnergyGeneratedLastTick()));
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
		if(button.id == 1) {
			CoordTriplet saveDelegate = turbine.getReferenceCoord();
			boolean newValue = !turbine.isActive();
			PacketDispatcher.sendPacketToServer(PacketWrapper.createPacket(BigReactors.CHANNEL, Packets.MultiblockControllerButton,
						new Object[] { saveDelegate.x, saveDelegate.y, saveDelegate.z, "activate", newValue }));
		}
	}
}

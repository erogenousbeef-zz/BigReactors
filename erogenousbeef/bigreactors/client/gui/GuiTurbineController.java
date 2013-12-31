package erogenousbeef.bigreactors.client.gui;

import cpw.mods.fml.common.network.PacketDispatcher;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.multiblock.MultiblockTurbine;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityTurbinePart;
import erogenousbeef.bigreactors.common.tileentity.TileEntityDebugTurbine;
import erogenousbeef.bigreactors.gui.controls.BeefGuiFluidBar;
import erogenousbeef.bigreactors.gui.controls.BeefGuiLabel;
import erogenousbeef.bigreactors.gui.controls.BeefGuiPowerBar;
import erogenousbeef.bigreactors.net.PacketWrapper;
import erogenousbeef.bigreactors.net.Packets;
import erogenousbeef.core.common.CoordTriplet;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;

public class GuiTurbineController extends BeefGuiBase {

	TileEntityTurbinePart part;
	MultiblockTurbine turbine;
	
	private BeefGuiLabel titleString;
	private BeefGuiLabel statusString;
	private BeefGuiLabel speedString;
	private BeefGuiLabel energyGeneratedString;

	private BeefGuiPowerBar powerBar;
	private BeefGuiFluidBar steamBar;
	private BeefGuiFluidBar waterBar;

	private GuiButton toggleActive;
	
	public GuiTurbineController(Container container, TileEntityTurbinePart part) {
		super(container);
		
		this.part = part;
		turbine = part.getTurbine();
	}

	@Override
	public ResourceLocation getGuiBackground() {
		// TODO FIXME
		return new ResourceLocation(BigReactors.GUI_DIRECTORY + "ReactorController.png");
	}
	
	// Add controls, etc.
	@Override
	public void initGui() {
		super.initGui();
		
		int leftX = guiLeft + 4;
		int topY = guiTop + 4;
		
		titleString = new BeefGuiLabel(this, "Turbine Control", leftX, topY);
		topY += titleString.getHeight() + 4;
		
		statusString = new BeefGuiLabel(this, "", leftX, topY);
		topY += statusString.getHeight() + 4;

		speedString = new BeefGuiLabel(this, "", leftX, topY);
		topY += speedString.getHeight() + 4;

		energyGeneratedString = new BeefGuiLabel(this, "", leftX, topY);
		topY += energyGeneratedString.getHeight() + 4;

		powerBar = new BeefGuiPowerBar(this, guiLeft + 152, guiTop + 22, this.turbine);
		steamBar = new BeefGuiFluidBar(this, guiLeft + 110, guiTop + 22, turbine, TileEntityDebugTurbine.TANK_STEAM);
		waterBar = new BeefGuiFluidBar(this, guiLeft + 132, guiTop + 22, turbine, TileEntityDebugTurbine.TANK_WATER);
	
		toggleActive = new GuiButton(1, guiLeft + 4, guiTop + 124, 70, 20, "Activate");
		
		registerControl(titleString);
		registerControl(statusString);
		registerControl(speedString);
		registerControl(energyGeneratedString);
		registerControl(powerBar);
		registerControl(steamBar);
		registerControl(waterBar);
		registerControl(toggleActive);

		updateStrings();
	}

	private void updateStrings() {
		if(turbine.isActive()) {
			statusString.setLabelText("Status: Active");
			toggleActive.displayString = "Deactivate";
		}
		else {
			statusString.setLabelText("Status: Inactive");
			toggleActive.displayString = "Activate";
		}
		
		speedString.setLabelText(String.format("Rotor Speed: %.1f RPM", 0f)); // TODO
		energyGeneratedString.setLabelText(String.format("Energy Output: %.0f RF/t", 0f)); // TODO
	}

	@Override
	public void updateScreen() {
		super.updateScreen();
		
		updateStrings();
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		CoordTriplet saveDelegate = turbine.getReferenceCoord();
		if(button.id == 1) {
			boolean newValue = !turbine.isActive();
			PacketDispatcher.sendPacketToServer(PacketWrapper.createPacket(BigReactors.CHANNEL, Packets.TurbineControllerButton,
						new Object[] { saveDelegate.x, saveDelegate.y, saveDelegate.z, "activate", newValue }));
		}
	}
}

package erogenousbeef.bigreactors.client.gui;

import cpw.mods.fml.common.network.PacketDispatcher;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.multiblock.MultiblockReactor;
import erogenousbeef.bigreactors.common.multiblock.MultiblockReactor.WasteEjectionSetting;
import erogenousbeef.bigreactors.common.tileentity.TileEntityReactorPart;
import erogenousbeef.bigreactors.gui.controls.BeefGuiLabel;
import erogenousbeef.bigreactors.gui.controls.BeefGuiPowerBar;
import erogenousbeef.bigreactors.net.PacketWrapper;
import erogenousbeef.bigreactors.net.Packets;
import erogenousbeef.core.common.CoordTriplet;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;

public class GuiReactorStatus extends BeefGuiBase {

	private GuiButton _toggleReactorOn;
	private GuiButton _reactorWastePolicy;
	private GuiButton _ejectWaste;
	
	private TileEntityReactorPart part;
	private MultiblockReactor reactor;
	
	private BeefGuiLabel titleString;
	private BeefGuiLabel statusString;
	private BeefGuiLabel heatString;
	private BeefGuiLabel fuelRodsString;
	private BeefGuiLabel energyGeneratedString;
	private BeefGuiPowerBar powerBar;
	
	public GuiReactorStatus(Container container, TileEntityReactorPart tileEntityReactorPart) {
		super(container);
		
		this.part = tileEntityReactorPart;
		this.reactor = part.getReactorController();
	}
	
	// Add controls, etc.
	@Override
	public void initGui() {
		super.initGui();
		
		int xCenter = guiLeft + this.xSize / 2;
		int yCenter = this.ySize / 2;
		
		_toggleReactorOn = new GuiButton(1, xCenter - (this.xSize/2) + 4, yCenter + (this.height/2) - 24, 70, 20, getReactorToggleText() );
		_reactorWastePolicy = new GuiButton(2, xCenter - (this.xSize/2) + 4, yCenter + (this.height / 2) - 46, 114, 20, getReactorWastePolicyText(reactor.getWasteEjection()));
		_ejectWaste = new GuiButton(3, xCenter - (this.xSize/2) + 122, yCenter + (this.height / 2) - 46, 50, 20, "Eject");

		this.buttonList.add(_toggleReactorOn);
		this.buttonList.add(_reactorWastePolicy);
		this.buttonList.add(_ejectWaste);

		int leftX = 4;
		int topY = 4;
		
		titleString = new BeefGuiLabel(this, "Reactor Control", leftX, topY);
		topY += titleString.getHeight() + 4;
		
		statusString = new BeefGuiLabel(this, "Status: -- updating --", leftX, topY);
		topY += statusString.getHeight() + 4;
		
		heatString = new BeefGuiLabel(this, "Heat: -- updating --", leftX, topY);
		topY += heatString.getHeight() + 4;
		
		fuelRodsString = new BeefGuiLabel(this, "Active Fuel Rods: -- updating --", leftX, topY);
		topY += fuelRodsString.getHeight() + 4;

		energyGeneratedString = new BeefGuiLabel(this, "Power Output: -- updating --", leftX, topY);
		topY += fuelRodsString.getHeight() + 4;
		
		powerBar = new BeefGuiPowerBar(this, guiLeft + 152, guiTop + 22, this.reactor);
		
		registerControl(titleString);
		registerControl(statusString);
		registerControl(heatString);
		registerControl(fuelRodsString);
		registerControl(energyGeneratedString);
		registerControl(powerBar);
	}

	private String getReactorWastePolicyText(WasteEjectionSetting setting) {
		switch(setting) {
		case kAutomatic:
			return "Waste: Auto-Eject";
		case kAutomaticOnlyIfCanReplace:
			return "Waste: Auto-Replace";
		case kManual:
		default:
			return "Waste: Manual";
		}
	}

	@Override
	public ResourceLocation getGuiBackground() {
		return new ResourceLocation(BigReactors.GUI_DIRECTORY + "ReactorController.png");
	}

	@Override
	public void updateScreen() {
		super.updateScreen();
		
		_toggleReactorOn.displayString = getReactorToggleText();
		
		if(reactor.isActive()) {
			statusString.setLabelText("Status: Online");
		}
		else {
			statusString.setLabelText("Status: Offline");
		}
		
		heatString.setLabelText("Heat: " + Integer.toString((int)reactor.getHeat()) + " degrees C");
		fuelRodsString.setLabelText("Active Fuel Rods: " + Integer.toString(reactor.getFuelColumnCount()));
		
		MultiblockReactor.WasteEjectionSetting wasteSetting = reactor.getWasteEjection();
		_reactorWastePolicy.displayString = getReactorWastePolicyText(wasteSetting);
		
		_ejectWaste.enabled = (wasteSetting == MultiblockReactor.WasteEjectionSetting.kManual);
		
		energyGeneratedString.setLabelText(String.format("Power Output: %1.1f MJ", reactor.getEnergyGeneratedLastTick()));
	}
	
	@Override
	protected void actionPerformed(GuiButton button) {
		CoordTriplet saveDelegate = reactor.getReferenceCoord();
		if(button.id == 1) {
			boolean newValue = !reactor.isActive();
			PacketDispatcher.sendPacketToServer(PacketWrapper.createPacket(BigReactors.CHANNEL, Packets.ReactorControllerButton,
						new Object[] { saveDelegate.x, saveDelegate.y, saveDelegate.z, "activate", newValue }));
		}
		else if(button.id == 2) {
			PacketDispatcher.sendPacketToServer(PacketWrapper.createPacket(BigReactors.CHANNEL, Packets.ReactorWasteEjectionSettingUpdate, 
						new Object[] { saveDelegate.x, saveDelegate.y, saveDelegate.z } ));
		}
		else if(button.id == 3) {
			// Boolean value is ignored here.
			PacketDispatcher.sendPacketToServer(PacketWrapper.createPacket(BigReactors.CHANNEL, Packets.ReactorControllerButton,
						new Object[] { saveDelegate.x, saveDelegate.y, saveDelegate.z, "ejectWaste", false }));
		}
	}
	
	private String getReactorToggleText() {
		if(reactor.isActive()) {
			return "Shutdown";
		}
		else {
			return "Activate";
		}
	}
}

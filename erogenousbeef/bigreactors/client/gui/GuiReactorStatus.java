package erogenousbeef.bigreactors.client.gui;

import cpw.mods.fml.common.network.PacketDispatcher;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.multiblock.MultiblockReactor;
import erogenousbeef.bigreactors.common.tileentity.TileEntityReactorPart;
import erogenousbeef.bigreactors.gui.controls.BeefGuiLabel;
import erogenousbeef.bigreactors.net.PacketWrapper;
import erogenousbeef.bigreactors.net.Packets;
import erogenousbeef.core.common.CoordTriplet;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.inventory.Container;

public class GuiReactorStatus extends BeefGuiBase {

	private GuiButton _toggleReactorOn;
	
	private TileEntityReactorPart part;
	private MultiblockReactor reactor;
	
	private BeefGuiLabel titleString;
	private BeefGuiLabel statusString;
	private BeefGuiLabel heatString;
	private BeefGuiLabel fuelRodsString;
	private BeefGuiLabel powerStoredString;
	
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
		this.buttonList.add(_toggleReactorOn);

		int leftX = 4;
		int topY = 4;
		
		titleString = new BeefGuiLabel(this, "Reactor Control", leftX, topY);
		topY += titleString.getHeight() + 4;
		
		statusString = new BeefGuiLabel(this, "Status: -- updating --", leftX, topY);
		topY += statusString.getHeight() + 4;
		
		heatString = new BeefGuiLabel(this, "Heat: -- updating --", leftX, topY);
		topY += heatString.getHeight() + 4;
		
		fuelRodsString = new BeefGuiLabel(this, "Active Fuel Columns: -- updating --", leftX, topY);
		topY += fuelRodsString.getHeight() + 4;
		
		powerStoredString = new BeefGuiLabel(this, "Power Exported: -- updating --", leftX, topY);
		topY += powerStoredString.getHeight() + 4;
		
		registerControl(titleString);
		registerControl(statusString);
		registerControl(heatString);
		registerControl(fuelRodsString);
		registerControl(powerStoredString);
	}

	@Override
	protected String getGuiBackground() {
		return BigReactors.GUI_DIRECTORY + "ReactorController.png";
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
		powerStoredString.setLabelText(String.format("Power Stored: %.3f units", reactor.getStoredEnergy()));
	}
	
	@Override
	protected void actionPerformed(GuiButton button) {
		if(button.id == 1) {
			boolean newValue = !reactor.isActive();
			
			CoordTriplet saveDelegate = reactor.getDelegateLocation();
			PacketDispatcher.sendPacketToServer(PacketWrapper.createPacket(BigReactors.CHANNEL, Packets.ReactorControllerButton,
						new Object[] { saveDelegate.x, saveDelegate.y, saveDelegate.z, "activate", newValue }));
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

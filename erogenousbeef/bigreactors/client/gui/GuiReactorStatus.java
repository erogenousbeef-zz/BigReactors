package erogenousbeef.bigreactors.client.gui;

import cpw.mods.fml.common.network.PacketDispatcher;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.multiblock.MultiblockReactor;
import erogenousbeef.bigreactors.gui.controls.BeefGuiLabel;
import erogenousbeef.bigreactors.net.PacketWrapper;
import erogenousbeef.bigreactors.net.Packets;
import erogenousbeef.core.common.CoordTriplet;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public class GuiReactorStatus extends BeefGuiBase {

	private GuiButton _toggleReactorOn;
	
	private MultiblockReactor reactor;
	
	private BeefGuiLabel titleString;
	private BeefGuiLabel statusString;
	private BeefGuiLabel heatString;
	private BeefGuiLabel fuelRodsString;
	private BeefGuiLabel powerUsageString;
	
	public GuiReactorStatus(MultiblockReactor reactor) {
		super();
		
		this.reactor = reactor;
	}
	
	// Add controls, etc.
	@Override
	public void initGui() {
		super.initGui();
		
		int xCenter = (this.width  - this.xSize ) / 2;
		int yCenter = (this.height - this.ySize ) / 2; 
		
		_toggleReactorOn = new GuiButton(1, xCenter + (this.xSize/2) - 24, 4, 20, 20, getReactorToggleText() );
		
		int leftX = 4;
		int topY = 4;
		
		titleString = new BeefGuiLabel("Reactor Control", leftX, topY, this.fontRenderer);
		topY += titleString.getHeight() + 4;
		
		statusString = new BeefGuiLabel("Status: Online", leftX, topY, this.fontRenderer);
		topY += statusString.getHeight() + 4;
		
		heatString = new BeefGuiLabel("Heat: 0", leftX, topY, this.fontRenderer);
		topY += heatString.getHeight() + 4;
		
		fuelRodsString = new BeefGuiLabel("Active Fuel Columns: 0", leftX, topY, this.fontRenderer);
		topY += fuelRodsString.getHeight() + 4;
		
		powerUsageString = new BeefGuiLabel("Power Exported: 0mj", leftX, topY, this.fontRenderer);
		topY += powerUsageString.getHeight() + 4;
		
		registerControl(titleString);
		registerControl(statusString);
		registerControl(heatString);
		registerControl(fuelRodsString);
		registerControl(powerUsageString);
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
			statusString.setLabelText("Status: Online", this.fontRenderer);
		}
		else {
			statusString.setLabelText("Status: Offline", this.fontRenderer);
		}
		
		heatString.setLabelText("Heat: " + Integer.toString((int)reactor.getHeat()), this.fontRenderer);
		fuelRodsString.setLabelText("Active Fuel Rods: " + Integer.toString(reactor.getActiveFuelRodCount()), this.fontRenderer);
		powerUsageString.setLabelText("Power Exported: 0mj", this.fontRenderer);
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

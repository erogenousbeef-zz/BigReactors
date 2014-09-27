package erogenousbeef.bigreactors.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.inventory.Container;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import erogenousbeef.bigreactors.client.ClientProxy;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorAccessPort;
import erogenousbeef.bigreactors.gui.BeefGuiIconManager;
import erogenousbeef.bigreactors.gui.controls.BeefGuiLabel;
import erogenousbeef.bigreactors.gui.controls.GuiIconButton;
import erogenousbeef.bigreactors.net.CommonPacketHandler;
import erogenousbeef.bigreactors.net.message.ReactorAccessPortChangeDirectionMessage;
import erogenousbeef.bigreactors.net.message.multiblock.ReactorCommandEjectToPortMessage;

public class GuiReactorAccessPort extends BeefGuiBase {
	private TileEntityReactorAccessPort _port;
	
	protected BeefGuiLabel inventoryLabel;
	
	protected GuiIconButton ejectFuel;
	protected GuiIconButton ejectWaste;
	
	protected GuiIconButton btnInlet;
	protected GuiIconButton btnOutlet;
	
	public GuiReactorAccessPort(Container container, TileEntityReactorAccessPort accessPort) {
		super(container);
		
		_port = accessPort;
	}
	
	@Override
	public void initGui() {
		super.initGui();

		ejectFuel = new GuiIconButton(2, guiLeft + xSize - 97, guiTop + 53, 18, 18, ClientProxy.GuiIcons.getIcon("fuelEject"), new String[] { EnumChatFormatting.AQUA + "Eject Fuel", "", "Ejects fuel contained in the", "reactor, placing ingots in the", "reactor's access ports.", "", "SHIFT: Dump excess fuel."});
		ejectWaste = new GuiIconButton(3, guiLeft + xSize - 77, guiTop + 53, 18, 18, ClientProxy.GuiIcons.getIcon("wasteEject"), new String[] { EnumChatFormatting.AQUA + "Eject Waste", "", "Ejects waste contained in the", "reactor, placing ingots in the", "reactor's access ports.", "", "SHIFT: Dump excess waste."});
		
		btnInlet = new GuiIconButton(0, guiLeft + xSize - 47, guiTop + 53, 18, 18, ClientProxy.GuiIcons.getIcon("inletOn"), new String[] { EnumChatFormatting.AQUA + "Inlet Mode", "", "Sets the access port to", "inlet mode.", "", "Port WILL accept", "items from pipes/ducts.", "Port WILL NOT eject", "items to pipes/ducts."});
		btnOutlet = new GuiIconButton(1, guiLeft + xSize - 27, guiTop + 53, 18, 18, ClientProxy.GuiIcons.getIcon("outletOn"), new String[] { EnumChatFormatting.AQUA + "Outlet Mode", "", "Sets the access port to", "outlet mode.", "", "Port WILL NOT accept", "items from pipes/ducts.", "Port WILL eject", "ingots to pipes/ducts."});
		
		inventoryLabel = new BeefGuiLabel(this, "Inventory", guiLeft + 8, guiTop + 64);
		
		registerControl(ejectFuel);
		registerControl(ejectWaste);
		registerControl(btnOutlet);
		registerControl(btnInlet);
		registerControl(inventoryLabel);
		
		updateIcons();
	}

	@Override
	public ResourceLocation getGuiBackground() {
		return new ResourceLocation(BigReactors.GUI_DIRECTORY + "ReactorAccessPort.png");
	}

	@Override
	public void updateScreen() {
		super.updateScreen();

		updateIcons();
	}
	
	protected void updateIcons() {
		if(_port.isInlet()) {
			btnInlet.setIcon(ClientProxy.GuiIcons.getIcon(BeefGuiIconManager.INLET_ON));
			btnOutlet.setIcon(ClientProxy.GuiIcons.getIcon(BeefGuiIconManager.OUTLET_OFF));
		}
		else {
			btnInlet.setIcon(ClientProxy.GuiIcons.getIcon(BeefGuiIconManager.INLET_OFF));
			btnOutlet.setIcon(ClientProxy.GuiIcons.getIcon(BeefGuiIconManager.OUTLET_ON));
		}
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float gameTicks) {
		super.drawScreen(mouseX, mouseY, gameTicks);
	}
	
	@Override
	protected void actionPerformed(GuiButton button) {
		if(button.id == 0 || button.id == 1) {
            CommonPacketHandler.INSTANCE.sendToServer(new ReactorAccessPortChangeDirectionMessage(_port, button.id == btnInlet.id));
		}
		
		else if(button.id == 2 || button.id == 3) {
			boolean ejectFuel = button.id == 2;
            CommonPacketHandler.INSTANCE.sendToServer(new ReactorCommandEjectToPortMessage(_port, ejectFuel, isShiftKeyDown()));
		}
	}
}

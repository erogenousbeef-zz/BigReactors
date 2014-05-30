package erogenousbeef.bigreactors.client.gui;

import welfare93.bigreactors.packet.MainPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;
import erogenousbeef.bigreactors.client.ClientProxy;
import erogenousbeef.bigreactors.common.BRLoader;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.multiblock.block.BlockReactorPart;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorAccessPort;
import erogenousbeef.bigreactors.gui.BeefGuiIconManager;
import erogenousbeef.bigreactors.gui.GuiConstants;
import erogenousbeef.bigreactors.gui.controls.BeefGuiLabel;
import erogenousbeef.bigreactors.gui.controls.GuiIconButton;
import erogenousbeef.bigreactors.net.Packets;

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
		
		int xCenter = guiLeft + this.xSize / 2;
		int yCenter = this.ySize / 2;

		int metadata = _port.getWorldObj().getBlockMetadata(_port.xCoord, _port.yCoord, _port.zCoord);
		
		ejectFuel = new GuiIconButton(2, guiLeft + xSize - 97, guiTop + 53, 18, 18, ClientProxy.GuiIcons.getIcon("fuelEject"), new String[] { GuiConstants.LITECYAN_TEXT + "Eject Fuel", "", "Ejects fuel contained in the", "reactor, placing ingots in the", "reactor's access ports.", "", "SHIFT: Dump excess fuel."});
		ejectWaste = new GuiIconButton(3, guiLeft + xSize - 77, guiTop + 53, 18, 18, ClientProxy.GuiIcons.getIcon("wasteEject"), new String[] { GuiConstants.LITECYAN_TEXT + "Eject Waste", "", "Ejects waste contained in the", "reactor, placing ingots in the", "reactor's access ports.", "", "SHIFT: Dump excess waste."});
		
		btnInlet = new GuiIconButton(0, guiLeft + xSize - 47, guiTop + 53, 18, 18, ClientProxy.GuiIcons.getIcon("inletOn"), new String[] { GuiConstants.LITECYAN_TEXT + "Inlet Mode", "", "Sets the access port to", "inlet mode.", "", "Port WILL accept", "items from pipes/ducts.", "Port WILL NOT eject", "items to pipes/ducts."});
		btnOutlet = new GuiIconButton(1, guiLeft + xSize - 27, guiTop + 53, 18, 18, ClientProxy.GuiIcons.getIcon("outletOn"), new String[] { GuiConstants.LITECYAN_TEXT + "Outlet Mode", "", "Sets the access port to", "outlet mode.", "", "Port WILL NOT accept", "items from pipes/ducts.", "Port WILL eject", "ingots to pipes/ducts."});
		
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
		int metadata = _port.getWorldObj().getBlockMetadata(_port.xCoord, _port.yCoord, _port.zCoord);
		if(metadata == BlockReactorPart.ACCESSPORT_INLET) {
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
			int metadata = _port.getWorldObj().getBlockMetadata(_port.xCoord, _port.yCoord, _port.zCoord);
			int newMetadata = button.id == 0 ? BlockReactorPart.ACCESSPORT_INLET : BlockReactorPart.ACCESSPORT_OUTLET;
			
			if(newMetadata != metadata) {
				ByteBuf a=Unpooled.buffer();
				a.writeByte(newMetadata);
				BRLoader.packethandler.sendToServer(new MainPacket(Packets.AccessPortButton,_port.xCoord, _port.yCoord, _port.zCoord,a));
			}
		}
		
		else if(button.id == 2 || button.id == 3) {
			boolean fuel = button.id == 2;
			ByteBuf a=Unpooled.buffer();
			a.writeBoolean(fuel);
			a.writeBoolean(isShiftKeyDown());
			a.writeBoolean(true);
			a.writeInt(_port.xCoord);
			a.writeInt(_port.yCoord);
			a.writeInt(_port.zCoord);
			BRLoader.packethandler.sendToServer(new MainPacket(Packets.ReactorEjectButton,_port.xCoord, _port.yCoord, _port.zCoord,a));
		}
	}
	
	private String getStringFromMetadata(int metadata) {
		if(metadata == BlockReactorPart.ACCESSPORT_INLET) {
			return "Dir: IN";
		}
		else {
			return "Dir: OUT";
		}
	}
}

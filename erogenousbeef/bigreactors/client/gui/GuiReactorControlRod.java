package erogenousbeef.bigreactors.client.gui;

import cpw.mods.fml.common.network.PacketDispatcher;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.tileentity.TileEntityReactorControlRod;
import erogenousbeef.bigreactors.gui.controls.BeefGuiLabel;
import erogenousbeef.bigreactors.net.PacketWrapper;
import erogenousbeef.bigreactors.net.Packets;

public class GuiReactorControlRod extends BeefGuiBase {

	TileEntityReactorControlRod entity;
	
	BeefGuiLabel titleString;
	BeefGuiLabel heatString;
	BeefGuiLabel fuelString;
	BeefGuiLabel wasteString;
	BeefGuiLabel rodStatus;

	GuiButton rodInsertBtn;
	GuiButton rodRetractBtn;
	
	public GuiReactorControlRod(Container c, TileEntityReactorControlRod controlRod) {
		super(c);
		
		entity = controlRod;
	}
	
	@Override
	public ResourceLocation getGuiBackground() {
		// TODO: Real gui?
		return new ResourceLocation (BigReactors.GUI_DIRECTORY + "ReactorController.png");
	}

	@Override
	public void initGui() {
		super.initGui();

		int leftX = 4;
		int topY = 4;
		
		titleString = new BeefGuiLabel(this, "Reactor Control Rod", leftX, topY);
		topY += titleString.getHeight() + 8;
		
		heatString = new BeefGuiLabel(this, "Heat: ??? C", leftX, topY);
		topY += heatString.getHeight() + 8;
		
		fuelString = new BeefGuiLabel(this, "Fuel: ???? (??%)", leftX, topY);
		topY += fuelString.getHeight() + 8;

		wasteString = new BeefGuiLabel(this, "Waste: ???? (??%)", leftX, topY);
		topY += wasteString.getHeight() + 8;

		rodStatus = new BeefGuiLabel(this, "Control Rod: ???", leftX, topY);
		
		int btnLeftX = leftX + rodStatus.getWidth() + 16;
		rodRetractBtn = new GuiButton(0, guiLeft + btnLeftX, guiTop + topY - 6, 20, 20, "-");
		btnLeftX += 22;
		rodInsertBtn = new GuiButton(1, guiLeft + btnLeftX, guiTop + topY - 6, 20, 20, "+");
		
		topY += rodStatus.getHeight() + 8;
		
		registerControl(titleString);
		registerControl(heatString);
		registerControl(fuelString);
		registerControl(wasteString);
		registerControl(rodStatus);
		
		buttonList.add(rodRetractBtn);
		buttonList.add(rodInsertBtn);
	}
	
	@Override
	public void updateScreen() {
		super.updateScreen();
		
		heatString.setLabelText(String.format("Heat: %2.2f C", entity.getHeat()));
		fuelString.setLabelText(String.format("Fuel: %d (%2.1f%%)", entity.getFuelAmount(), ((float)entity.getFuelAmount() / (float)entity.getTotalContainedAmount())*100f));
		wasteString.setLabelText(String.format("Waste: %d (%2.1f%%)", entity.getWasteAmount(), ((float)entity.getWasteAmount() / (float)entity.getTotalContainedAmount())*100f));
		rodStatus.setLabelText(String.format("Control Rod: %2d%%", entity.getControlRodInsertion()));
		if(entity.isAssembled()) {
			rodInsertBtn.enabled = true;
			rodRetractBtn.enabled = true;
		}
		else {
			rodInsertBtn.enabled = false;
			rodRetractBtn.enabled = false;
		}
	}
	
	@Override
	protected void actionPerformed(GuiButton button) {
		String btnCmd;
		
		switch(button.id) {
		case 0:
			btnCmd = "rodRetract";
			break;
		case 1:
		default:
			btnCmd = "rodInsert";
			break;
		}
		
		PacketDispatcher.sendPacketToServer(PacketWrapper.createPacket(BigReactors.CHANNEL, Packets.BeefGuiButtonPress,
				new Object[] { entity.xCoord, entity.yCoord, entity.zCoord, btnCmd }));
	}
}

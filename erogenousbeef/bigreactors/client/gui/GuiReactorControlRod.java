package erogenousbeef.bigreactors.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.common.network.PacketDispatcher;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.multiblock.MultiblockReactor;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorControlRod;
import erogenousbeef.bigreactors.gui.controls.BeefGuiLabel;
import erogenousbeef.bigreactors.net.PacketWrapper;
import erogenousbeef.bigreactors.net.Packets;
import erogenousbeef.core.multiblock.MultiblockControllerBase;

public class GuiReactorControlRod extends BeefGuiBase {

	TileEntityReactorControlRod entity;
	
	BeefGuiLabel titleString;
	BeefGuiLabel heatString;
	BeefGuiLabel rodStatus;
	BeefGuiLabel rodNameLabel;

	GuiButton rodInsertBtn;
	GuiButton rodRetractBtn;
	GuiButton setNameBtn;

    private GuiTextField rodName;

	
	public GuiReactorControlRod(Container c, TileEntityReactorControlRod controlRod) {
		super(c);
		
		entity = controlRod;
	}
	
	@Override
	public ResourceLocation getGuiBackground() {
		// TODO: Real gui?
		return new ResourceLocation (BigReactors.GUI_DIRECTORY + "BasicBackground.png");
	}

	@Override
	public void initGui() {
		super.initGui();

		int leftX = guiLeft + 4;
		int topY = guiTop + 4;
		
		Keyboard.enableRepeatEvents(true);
		
		titleString = new BeefGuiLabel(this, "Reactor Control Rod", leftX, topY);
		topY += titleString.getHeight() + 8;
		
		rodNameLabel = new BeefGuiLabel(this, "Name:", leftX, topY + 6);
		
		rodName = new GuiTextField(fontRenderer, leftX + 4 + rodNameLabel.getWidth(), topY, 100, 20);
		rodName.setCanLoseFocus(true);
		rodName.setMaxStringLength(32);
		rodName.setText(entity.getName());
		rodName.setEnabled(true);
		
		setNameBtn = new GuiButton(2, guiLeft + 140, guiTop + topY, 30, 20, "Set");
		setNameBtn.enabled = false;
		topY += 28;
		
		heatString = new BeefGuiLabel(this, "Heat", leftX, topY);
		topY += heatString.getHeight() + 8;
		
		rodStatus = new BeefGuiLabel(this, "Control Rods:", leftX, topY);
		
		int btnLeftX = leftX + rodStatus.getWidth() + 16;
		rodRetractBtn = new GuiButton(0, btnLeftX, topY - 6, 20, 20, "-");
		btnLeftX += 22;
		rodInsertBtn = new GuiButton(1, btnLeftX, topY - 6, 20, 20, "+");
		
		topY += rodStatus.getHeight() + 8;
		
		registerControl(titleString);
		registerControl(heatString);
		registerControl(rodStatus);
		registerControl(rodNameLabel);
		
		registerControl(rodName);
		
		buttonList.add(rodRetractBtn);
		buttonList.add(rodInsertBtn);
		buttonList.add(setNameBtn);
		
		updateStrings();
	}
	
	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
		Keyboard.enableRepeatEvents(false);
	}
	
	@Override
	public void updateScreen() {
		super.updateScreen();
		updateStrings();
	}
	
	protected void updateStrings() {
		MultiblockControllerBase controller = entity.getMultiblockController();
		if(controller instanceof MultiblockReactor) {
			MultiblockReactor reactor = (MultiblockReactor)controller;
			heatString.setLabelText(String.format("Heat: %1.1f C", reactor.getFuelHeat()));
		}
		else {
			heatString.setLabelText(String.format("Heat: Unknown"));
		}

		rodStatus.setLabelText(String.format("Control Rod: %2d%%", entity.getControlRodInsertion()));
		if(entity.isConnected()) {
			rodInsertBtn.enabled = true;
			rodRetractBtn.enabled = true;
		}
		else {
			rodInsertBtn.enabled = false;
			rodRetractBtn.enabled = false;
		}
		
		this.setNameBtn.enabled = !entity.getName().equals(this.rodName.getText());
	}
	
	@Override
	protected void actionPerformed(GuiButton button) {
		String btnCmd;
		
		switch(button.id) {
		case 0:
			btnCmd = "rodRetract";
			break;
		case 2:
			PacketDispatcher.sendPacketToServer(PacketWrapper.createPacket(BigReactors.CHANNEL, Packets.ControlRodSetName,
					new Object[] { entity.xCoord, entity.yCoord, entity.zCoord, this.rodName.getText() }));
			this.rodName.setFocused(false);
			return;
		case 1:
		default:
			btnCmd = "rodInsert";
			break;
		}
		
		PacketDispatcher.sendPacketToServer(PacketWrapper.createPacket(BigReactors.CHANNEL, Packets.BeefGuiButtonPress,
				new Object[] { entity.xCoord, entity.yCoord, entity.zCoord, btnCmd }));
	}
	
	@Override
	protected void keyTyped(char inputChar, int keyCode) {
        if (keyCode == Keyboard.KEY_ESCAPE ||
        		(!this.rodName.isFocused() && keyCode == this.mc.gameSettings.keyBindInventory.keyCode)) {
            this.mc.thePlayer.closeScreen();
        }

		this.rodName.textboxKeyTyped(inputChar, keyCode);
		
		if(keyCode == Keyboard.KEY_TAB) {
			// Tab
			if(this.rodName.isFocused()) {
				this.rodName.setFocused(false);
			}
			else {
				this.rodName.setFocused(true);
			}
		}
		
		if(keyCode == Keyboard.KEY_RETURN) {
			// Return/enter
			this.actionPerformed((GuiButton)this.buttonList.get(2));
		}
	}
	
}

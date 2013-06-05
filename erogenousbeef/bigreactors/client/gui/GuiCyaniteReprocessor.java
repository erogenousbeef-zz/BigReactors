package erogenousbeef.bigreactors.client.gui;

import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.inventory.Container;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.LiquidContainerRegistry;
import net.minecraftforge.liquids.LiquidStack;
import cpw.mods.fml.common.network.PacketDispatcher;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.block.BlockReactorPart;
import erogenousbeef.bigreactors.common.tileentity.TileEntityCyaniteReprocessor;
import erogenousbeef.bigreactors.common.tileentity.TileEntityReactorAccessPort;
import erogenousbeef.bigreactors.common.tileentity.base.TileEntityInventory;
import erogenousbeef.bigreactors.gui.controls.BeefGuiLabel;
import erogenousbeef.bigreactors.gui.controls.BeefGuiLiquidBar;
import erogenousbeef.bigreactors.gui.controls.BeefGuiPowerBar;
import erogenousbeef.bigreactors.gui.controls.BeefGuiProgressArrow;
import erogenousbeef.bigreactors.gui.controls.BeefGuiProgressBarVertical;
import erogenousbeef.bigreactors.gui.controls.GuiImageButton;
import erogenousbeef.bigreactors.net.PacketWrapper;
import erogenousbeef.bigreactors.net.Packets;

public class GuiCyaniteReprocessor extends BeefGuiBase {

	private GuiButton _togglePort;
	private TileEntityCyaniteReprocessor _entity;

	private BeefGuiLabel titleString;
	private BeefGuiLabel progressString;
	
	private GuiImageButton leftInvExposureButton;
	private GuiImageButton rightInvExposureButton;
	private GuiImageButton topInvExposureButton;
	private GuiImageButton bottomInvExposureButton;
	private GuiImageButton rearInvExposureButton;
	
	private BeefGuiPowerBar powerBar;
	private BeefGuiLiquidBar liquidBar;
	private BeefGuiProgressArrow progressArrow;
	
	public GuiCyaniteReprocessor(Container container, TileEntityCyaniteReprocessor entity) {
		super(container);
		
		_entity = entity;
		xSize = 241;
		ySize = 175;
	}
	
	@Override
	public void initGui() {
		super.initGui();

		int leftX = 8;
		int topY = 6;
		
		titleString = new BeefGuiLabel(this, _entity.getInvName(), leftX, topY);
		topY += titleString.getHeight() + 8;
		
		progressString = new BeefGuiLabel(this, "Progress: ???", leftX + titleString.getWidth() + 8, 4);
		
		liquidBar = new BeefGuiLiquidBar(this, guiLeft + 8, guiTop + 16, _entity, 0);
		powerBar = new BeefGuiPowerBar(this, guiLeft + 148, guiTop + 16, _entity);
		progressArrow = new BeefGuiProgressArrow(this, guiLeft + 76, guiTop + 41, 0, 178, _entity);
		
		registerControl(titleString);
		registerControl(progressString);
		registerControl(powerBar);
		registerControl(liquidBar);
		registerControl(progressArrow);
		
		leftInvExposureButton = new GuiImageButton(ForgeDirection.WEST.ordinal(), guiLeft + 179, guiTop + 25, 20, 20, "");
		buttonList.add(leftInvExposureButton);

		rightInvExposureButton = new GuiImageButton(ForgeDirection.EAST.ordinal(), guiLeft + 219, guiTop + 25, 20, 20, "");
		buttonList.add(rightInvExposureButton);

		topInvExposureButton = new GuiImageButton(ForgeDirection.UP.ordinal(), guiLeft + 199, guiTop + 5, 20, 20, "");
		buttonList.add(topInvExposureButton);
		
		bottomInvExposureButton = new GuiImageButton(ForgeDirection.DOWN.ordinal(), guiLeft + 199, guiTop + 45, 20, 20, "");
		buttonList.add(bottomInvExposureButton);

		rearInvExposureButton = new GuiImageButton(ForgeDirection.SOUTH.ordinal(), guiLeft + 219, guiTop + 45, 20, 20, "");
		buttonList.add(rearInvExposureButton);
	
	}

	@Override
	public String getGuiBackground() {
		return BigReactors.GUI_DIRECTORY + "CyaniteReprocessor.png";
	}

	@Override
	public void updateScreen() {
		super.updateScreen();

		// TODO: REMOVEME
		if(_entity.isActive()) {
			progressString.setLabelText(String.format("Progress: %2.1f", _entity.getCycleCompletion() * 100f));
		}
		else {
			progressString.setLabelText(String.format("Progress: Inactive"));
		}

		// Exposure buttons
		int exposed = _entity.getExposedSlotFromReferenceSide(ForgeDirection.EAST.ordinal());
		int liquidExposed = _entity.getExposedTankFromReferenceSide(ForgeDirection.EAST);
		if(exposed != TileEntityInventory.INVENTORY_UNEXPOSED) {
			rightInvExposureButton.displayString = getTextureForExposedInventory(exposed);
		}
		else {
			rightInvExposureButton.displayString = getTextureForExposedLiquidInventory(liquidExposed);
		}

		exposed = _entity.getExposedSlotFromReferenceSide(ForgeDirection.WEST.ordinal());
		liquidExposed = _entity.getExposedTankFromReferenceSide(ForgeDirection.WEST);
		if(exposed != TileEntityInventory.INVENTORY_UNEXPOSED) {
			leftInvExposureButton.displayString = getTextureForExposedInventory(exposed);
		}
		else {
			leftInvExposureButton.displayString = getTextureForExposedLiquidInventory(liquidExposed);
		}
		
		exposed = _entity.getExposedSlotFromReferenceSide(ForgeDirection.UP.ordinal());
		liquidExposed = _entity.getExposedTankFromReferenceSide(ForgeDirection.UP);
		if(exposed != TileEntityInventory.INVENTORY_UNEXPOSED) {
			topInvExposureButton.displayString = getTextureForExposedInventory(exposed);
		}
		else {
			topInvExposureButton.displayString = getTextureForExposedLiquidInventory(liquidExposed);
		}

		exposed = _entity.getExposedSlotFromReferenceSide(ForgeDirection.DOWN.ordinal());
		liquidExposed = _entity.getExposedTankFromReferenceSide(ForgeDirection.DOWN);
		if(exposed != TileEntityInventory.INVENTORY_UNEXPOSED) {
			bottomInvExposureButton.displayString = getTextureForExposedInventory(exposed);
		}
		else {
			bottomInvExposureButton.displayString = getTextureForExposedLiquidInventory(liquidExposed);
		}

		exposed = _entity.getExposedSlotFromReferenceSide(ForgeDirection.SOUTH.ordinal());
		liquidExposed = _entity.getExposedTankFromReferenceSide(ForgeDirection.SOUTH);
		if(exposed != TileEntityInventory.INVENTORY_UNEXPOSED) {
			rearInvExposureButton.displayString = getTextureForExposedInventory(exposed);
		}
		else {
			rearInvExposureButton.displayString = getTextureForExposedLiquidInventory(liquidExposed);
		}
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float gameTicks) {
		super.drawScreen(mouseX, mouseY, gameTicks);
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if(button.id >= 0 && button.id < 6) {
			PacketDispatcher.sendPacketToServer(PacketWrapper.createPacket(BigReactors.CHANNEL, Packets.BeefGuiButtonPress,
						new Object[] { _entity.xCoord, _entity.yCoord, _entity.zCoord, "changeInvSide", button.id }));
			return;
		}
	}

	protected String getTextureForExposedInventory(int inv) {
		if(inv == 0) {
			return BigReactors.GUI_DIRECTORY + "redSquare.png";
		}
		else if(inv == 1) {
			return BigReactors.GUI_DIRECTORY + "greenSquare.png";
		}
		
		return "";
	}
	
	protected String getTextureForExposedLiquidInventory(int tank) {
		if(tank == 0) {
			return BigReactors.GUI_DIRECTORY + "blueSquare.png";
		}
		
		return "";
	}
}

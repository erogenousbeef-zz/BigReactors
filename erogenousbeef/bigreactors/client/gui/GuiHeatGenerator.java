package erogenousbeef.bigreactors.client.gui;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.network.PacketDispatcher;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.tileentity.TileEntityHeatGenerator;
import erogenousbeef.bigreactors.common.tileentity.base.TileEntityInventory;
import erogenousbeef.bigreactors.gui.controls.BeefGuiFluidBar;
import erogenousbeef.bigreactors.gui.controls.BeefGuiLabel;
import erogenousbeef.bigreactors.gui.controls.BeefGuiPowerBar;
import erogenousbeef.bigreactors.gui.controls.BeefGuiProgressArrow;
import erogenousbeef.bigreactors.gui.controls.GuiImageButton;
import erogenousbeef.bigreactors.net.PacketWrapper;
import erogenousbeef.bigreactors.net.Packets;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeDirection;

public class GuiHeatGenerator extends BeefGuiBase {

	private TileEntityHeatGenerator _entity;

	private BeefGuiLabel titleString;
	private BeefGuiLabel tempString;
	
	private GuiImageButton leftInvExposureButton;
	private GuiImageButton rightInvExposureButton;
	private GuiImageButton topInvExposureButton;
	private GuiImageButton bottomInvExposureButton;
	private GuiImageButton rearInvExposureButton;
	
	private GuiButton toggleActive;
	
	private BeefGuiFluidBar steamBar;
	private BeefGuiFluidBar fluidBar;
	private BeefGuiProgressArrow progressArrow;
	
	public GuiHeatGenerator(Container container, TileEntityHeatGenerator entity) {
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
		
		titleString = new BeefGuiLabel(this, "Water-Steam Thingus", leftX, topY);
		topY += titleString.getHeight() + 8;
		
		tempString = new BeefGuiLabel(this, "Temp: ???", leftX+22, topY);
		topY += tempString.getHeight() + 4;
		
		fluidBar = new BeefGuiFluidBar(this, guiLeft + 8, guiTop + 16, _entity, 0);
		steamBar = new BeefGuiFluidBar(this, guiLeft + 148, guiTop + 16, _entity, 1);
		progressArrow = new BeefGuiProgressArrow(this, guiLeft + 76, guiTop + 41, 0, 178, _entity);

		registerControl(titleString);
		registerControl(tempString);
		registerControl(fluidBar);
		registerControl(steamBar);
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
	
		toggleActive = new GuiButton(10, guiLeft + 58, guiTop + 70, 60, 20, "On/Off");
		buttonList.add(toggleActive);
	}

	@Override
	public ResourceLocation getGuiBackground() {
		return new ResourceLocation(BigReactors.GUI_DIRECTORY + "CyaniteReprocessor.png");
	}

	@Override
	public void updateScreen() {
		super.updateScreen();

		tempString.setLabelText(String.format("Temperature: %.1fC", _entity.internalTemperature-273f));

		// Exposure buttons
		int exposed = _entity.getExposedSlotFromReferenceSide(ForgeDirection.EAST.ordinal());
		int fluidExposed = _entity.getExposedTankFromReferenceSide(ForgeDirection.EAST);
		if(exposed != TileEntityInventory.INVENTORY_UNEXPOSED) {
			rightInvExposureButton.displayString = getTextureForExposedInventory(exposed);
		}
		else {
			rightInvExposureButton.displayString = getTextureForExposedFluidInventory(fluidExposed);
		}

		exposed = _entity.getExposedSlotFromReferenceSide(ForgeDirection.WEST.ordinal());
		fluidExposed = _entity.getExposedTankFromReferenceSide(ForgeDirection.WEST);
		if(exposed != TileEntityInventory.INVENTORY_UNEXPOSED) {
			leftInvExposureButton.displayString = getTextureForExposedInventory(exposed);
		}
		else {
			leftInvExposureButton.displayString = getTextureForExposedFluidInventory(fluidExposed);
		}
		
		exposed = _entity.getExposedSlotFromReferenceSide(ForgeDirection.UP.ordinal());
		fluidExposed = _entity.getExposedTankFromReferenceSide(ForgeDirection.UP);
		if(exposed != TileEntityInventory.INVENTORY_UNEXPOSED) {
			topInvExposureButton.displayString = getTextureForExposedInventory(exposed);
		}
		else {
			topInvExposureButton.displayString = getTextureForExposedFluidInventory(fluidExposed);
		}

		exposed = _entity.getExposedSlotFromReferenceSide(ForgeDirection.DOWN.ordinal());
		fluidExposed = _entity.getExposedTankFromReferenceSide(ForgeDirection.DOWN);
		if(exposed != TileEntityInventory.INVENTORY_UNEXPOSED) {
			bottomInvExposureButton.displayString = getTextureForExposedInventory(exposed);
		}
		else {
			bottomInvExposureButton.displayString = getTextureForExposedFluidInventory(fluidExposed);
		}

		exposed = _entity.getExposedSlotFromReferenceSide(ForgeDirection.SOUTH.ordinal());
		fluidExposed = _entity.getExposedTankFromReferenceSide(ForgeDirection.SOUTH);
		if(exposed != TileEntityInventory.INVENTORY_UNEXPOSED) {
			rearInvExposureButton.displayString = getTextureForExposedInventory(exposed);
		}
		else {
			rearInvExposureButton.displayString = getTextureForExposedFluidInventory(fluidExposed);
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
		else if(button.id == 10) {
			PacketDispatcher.sendPacketToServer(PacketWrapper.createPacket(BigReactors.CHANNEL, Packets.BeefGuiButtonPress,
					new Object[] { _entity.xCoord, _entity.yCoord, _entity.zCoord, "active" }));
		}
	}

	protected String getTextureForExposedInventory(int inv) {
		return "";
	}
	
	protected String getTextureForExposedFluidInventory(int tank) {
		if(tank == 0) {
			return BigReactors.GUI_DIRECTORY + "blueSquare.png";
		}
		else if(tank == 1) {
			return BigReactors.GUI_DIRECTORY + "whiteSquare.png";
		}
		
		return "";
	}

}

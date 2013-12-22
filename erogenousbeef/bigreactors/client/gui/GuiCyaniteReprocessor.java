package erogenousbeef.bigreactors.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeDirection;
import cpw.mods.fml.common.network.PacketDispatcher;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.block.BlockBRSmallMachine;
import erogenousbeef.bigreactors.common.tileentity.TileEntityCyaniteReprocessor;
import erogenousbeef.bigreactors.gui.controls.BeefGuiFluidBar;
import erogenousbeef.bigreactors.gui.controls.BeefGuiLabel;
import erogenousbeef.bigreactors.gui.controls.BeefGuiPowerBar;
import erogenousbeef.bigreactors.gui.controls.BeefGuiProgressArrow;
import erogenousbeef.bigreactors.gui.controls.GuiIconButton;
import erogenousbeef.bigreactors.net.PacketWrapper;
import erogenousbeef.bigreactors.net.Packets;

public class GuiCyaniteReprocessor extends BeefGuiBase {

	private GuiButton _togglePort;
	private TileEntityCyaniteReprocessor _entity;

	private BeefGuiLabel titleString;
	
	private static final int EXPOSURE_BUTTON_ID_BASE = 100;
	private GuiIconButton[] exposureButtons;
	
	private GuiIconButton leftInvExposureButton;
	private GuiIconButton rightInvExposureButton;
	private GuiIconButton topInvExposureButton;
	private GuiIconButton bottomInvExposureButton;
	private GuiIconButton rearInvExposureButton;
	
	private BeefGuiPowerBar powerBar;
	private BeefGuiFluidBar fluidBar;
	private BeefGuiProgressArrow progressArrow;
	
	public GuiCyaniteReprocessor(Container container, TileEntityCyaniteReprocessor entity) {
		super(container);
		
		_entity = entity;
		xSize = 245;
		ySize = 175;
	}
	
	private void createInventoryExposureButton(ForgeDirection dir, int x, int y) {
		if(exposureButtons[dir.ordinal()] != null) { throw new IllegalArgumentException("Direction already exposed"); }

		GuiIconButton newBtn = new GuiIconButton(EXPOSURE_BUTTON_ID_BASE + dir.ordinal(), x, y, 20, 20, null);
		buttonList.add(newBtn);
		exposureButtons[dir.ordinal()] = newBtn;
	}
	
	@Override
	public void initGui() {
		super.initGui();

		int leftX = guiLeft + 8;
		int topY = guiTop + 6;
		
		titleString = new BeefGuiLabel(this, _entity.getInvName(), leftX, topY);
		topY += titleString.getHeight() + 8;
		
		fluidBar = new BeefGuiFluidBar(this, guiLeft + 8, guiTop + 16, _entity, 0);
		powerBar = new BeefGuiPowerBar(this, guiLeft + 148, guiTop + 16, _entity);
		progressArrow = new BeefGuiProgressArrow(this, guiLeft + 76, guiTop + 41, 0, 178, _entity);
		
		registerControl(titleString);
		registerControl(powerBar);
		registerControl(fluidBar);
		registerControl(progressArrow);


		// Do this here to make the GUI resize-proof
		exposureButtons = new GuiIconButton[6];
		for(int i = 0; i < 6; i++) {
			exposureButtons[i] = null;
		}

		createInventoryExposureButton(ForgeDirection.WEST, guiLeft + 180, guiTop + 25);
		createInventoryExposureButton(ForgeDirection.EAST, guiLeft + 222, guiTop + 25);
		createInventoryExposureButton(ForgeDirection.NORTH, guiLeft + 201, guiTop + 25);
		createInventoryExposureButton(ForgeDirection.UP, guiLeft + 201, guiTop + 4);
		createInventoryExposureButton(ForgeDirection.DOWN, guiLeft + 201, guiTop + 46);
		createInventoryExposureButton(ForgeDirection.SOUTH, guiLeft + 222, guiTop + 46);
		
		exposureButtons[ForgeDirection.NORTH.ordinal()].setIcon(BigReactors.blockSmallMachine.getIcon(4, BlockBRSmallMachine.META_CYANITE_REPROCESSOR));
		exposureButtons[ForgeDirection.NORTH.ordinal()].enabled = false;
		updateInventoryExposures();
	}

	@Override
	public ResourceLocation getGuiBackground() {
		return new ResourceLocation(BigReactors.GUI_DIRECTORY + "CyaniteReprocessor.png");
	}

	@Override
	public void updateScreen() {
		super.updateScreen();
		updateInventoryExposures();
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float gameTicks) {
		super.drawScreen(mouseX, mouseY, gameTicks);
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if(button.id >= EXPOSURE_BUTTON_ID_BASE && button.id < EXPOSURE_BUTTON_ID_BASE + 6) {
			PacketDispatcher.sendPacketToServer(PacketWrapper.createPacket(BigReactors.CHANNEL, Packets.BeefGuiButtonPress,
						new Object[] { _entity.xCoord, _entity.yCoord, _entity.zCoord, "changeInvSide", button.id - EXPOSURE_BUTTON_ID_BASE }));
			return;
		}
	}

	protected void updateInventoryExposures() {
		BlockBRSmallMachine smallMachineBlock = (BlockBRSmallMachine)BigReactors.blockSmallMachine;
		for(ForgeDirection dir : ForgeDirection.values()) {
			if(dir == ForgeDirection.UNKNOWN || dir == ForgeDirection.NORTH) { continue; }
			int rotatedSide = _entity.getRotatedSide(dir.ordinal());
			
			exposureButtons[dir.ordinal()].setIcon( smallMachineBlock.getIconFromTileEntity(_entity, BlockBRSmallMachine.META_CYANITE_REPROCESSOR, rotatedSide) );
		}
	}

}

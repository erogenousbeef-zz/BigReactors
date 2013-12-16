package erogenousbeef.bigreactors.client.gui;

import cpw.mods.fml.common.network.PacketDispatcher;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.block.BlockBRSmallMachine;
import erogenousbeef.bigreactors.common.tileentity.base.TileEntityBeefBase;
import erogenousbeef.bigreactors.gui.controls.GuiIconButton;
import erogenousbeef.bigreactors.net.PacketWrapper;
import erogenousbeef.bigreactors.net.Packets;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeDirection;

public abstract class BeefGuiSmallMachineBase extends BeefGuiBase {

	protected static final int EXPOSURE_BUTTON_ID_BASE = 100;
	private GuiIconButton[] exposureButtons;
	
	private GuiIconButton leftInvExposureButton;
	private GuiIconButton rightInvExposureButton;
	private GuiIconButton topInvExposureButton;
	private GuiIconButton bottomInvExposureButton;
	private GuiIconButton rearInvExposureButton;
	
	TileEntityBeefBase _entity;
	
	public BeefGuiSmallMachineBase(Container container, TileEntityBeefBase tileEntity) {
		super(container);
		_entity = tileEntity;
	}

	protected abstract int getBlockMetadata();
	
	private void createInventoryExposureButton(ForgeDirection dir, int x, int y) {
		if(exposureButtons[dir.ordinal()] != null) { throw new IllegalArgumentException("Direction already exposed"); }

		GuiIconButton newBtn = new GuiIconButton(EXPOSURE_BUTTON_ID_BASE + dir.ordinal(), x, y, 20, 20, null);
		buttonList.add(newBtn);
		exposureButtons[dir.ordinal()] = newBtn;
	}
	
	/**
	 * Create GUI inventory exposure button grid
	 * @param minLeft The leftmost coordinate for the buttons, including guiLeft
	 * @param minTop The topmost coordinate for the buttons, including guiTop
	 */
	protected void createInventoryExposureButtons(int minLeft, int minTop) {
		// Do this here to make the GUI resize-proof
		exposureButtons = new GuiIconButton[6];
		for(int i = 0; i < 6; i++) {
			exposureButtons[i] = null;
		}

		createInventoryExposureButton(ForgeDirection.WEST, minLeft, minTop + 21);
		createInventoryExposureButton(ForgeDirection.EAST, minLeft + 42, minTop + 21);
		createInventoryExposureButton(ForgeDirection.NORTH, minLeft + 21, minTop + 21);
		createInventoryExposureButton(ForgeDirection.UP, minLeft + 21, minTop);
		createInventoryExposureButton(ForgeDirection.DOWN, minLeft + 21, minTop + 42);
		createInventoryExposureButton(ForgeDirection.SOUTH, minLeft + 42, minTop + 42);

		exposureButtons[ForgeDirection.NORTH.ordinal()].setIcon(BigReactors.blockSmallMachine.getIcon(4, getBlockMetadata()));
		exposureButtons[ForgeDirection.NORTH.ordinal()].enabled = false;
	}
	
	@Override
	public void updateScreen() {
		super.updateScreen();
		updateInventoryExposures();
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		super.actionPerformed(button);
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

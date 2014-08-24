package erogenousbeef.bigreactors.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.inventory.Container;
import cofh.lib.util.helpers.BlockHelper;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.block.BlockBRDevice;
import erogenousbeef.bigreactors.common.tileentity.base.TileEntityBeefBase;
import erogenousbeef.bigreactors.gui.controls.GuiIconButton;
import erogenousbeef.bigreactors.net.CommonPacketHandler;
import erogenousbeef.bigreactors.net.message.DeviceChangeExposureMessage;

public abstract class BeefGuiDeviceBase extends BeefGuiBase {

	protected static final int EXPOSURE_BUTTON_ID_BASE = 100;
	private GuiIconButton[] exposureButtons;
	
	TileEntityBeefBase _entity;
	
	public BeefGuiDeviceBase(Container container, TileEntityBeefBase tileEntity) {
		super(container);
		_entity = tileEntity;
	}

	/**
	 * Used to set the icon for the front face of the machine on the exposure button panel. No other uses.
	 * @return The metadata of the machine whose icon should show up in the center of the exposure buttons.
	 */
	protected abstract int getBlockMetadata();
	
	private void createInventoryExposureButton(int side, int x, int y) {
		if(exposureButtons[side] != null) { throw new IllegalArgumentException("Direction already exposed"); }

		GuiIconButton newBtn = new GuiIconButton(EXPOSURE_BUTTON_ID_BASE + side, x, y, 20, 20, null);
		buttonList.add(newBtn);
		exposureButtons[side] = newBtn;
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

		int facing = _entity.getFacing();		
		createInventoryExposureButton(BlockHelper.SIDE_LEFT[facing], minLeft, minTop + 21);
		createInventoryExposureButton(BlockHelper.SIDE_RIGHT[facing], minLeft + 42, minTop + 21);
		createInventoryExposureButton(facing, minLeft + 21, minTop + 21);
		createInventoryExposureButton(BlockHelper.SIDE_ABOVE[facing], minLeft + 21, minTop);
		createInventoryExposureButton(BlockHelper.SIDE_BELOW[facing], minLeft + 21, minTop + 42);
		createInventoryExposureButton(BlockHelper.SIDE_OPPOSITE[facing], minLeft + 42, minTop + 42);

		exposureButtons[facing].setIcon(BigReactors.blockDevice.getIcon(4, getBlockMetadata()));
		exposureButtons[facing].enabled = false;
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
			// TODO: Figure out how to detect rightclicks
            CommonPacketHandler.INSTANCE.sendToServer(new DeviceChangeExposureMessage(_entity.xCoord, _entity.yCoord, _entity.zCoord, button.id - EXPOSURE_BUTTON_ID_BASE, true));
		}
	}
	
	protected void updateInventoryExposures() {
		int facing = _entity.getFacing();
		BlockBRDevice deviceBlock = (BlockBRDevice)BigReactors.blockDevice;
		for(int side = 0; side < 6; side++) {
			if(side == facing) { continue; }
			exposureButtons[side].setIcon( deviceBlock.getIconFromTileEntity(_entity, BlockBRDevice.META_CYANITE_REPROCESSOR, side) );
		}
	}
}

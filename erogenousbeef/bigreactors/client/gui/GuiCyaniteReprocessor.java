package erogenousbeef.bigreactors.client.gui;

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
import erogenousbeef.bigreactors.gui.controls.GuiImageButton;
import erogenousbeef.bigreactors.net.PacketWrapper;
import erogenousbeef.bigreactors.net.Packets;

public class GuiCyaniteReprocessor extends BeefGuiBase {

	private GuiButton _togglePort;
	private TileEntityCyaniteReprocessor _entity;

	private BeefGuiLabel titleString;
	private BeefGuiLabel powerStoredString;
	private BeefGuiLabel waterStoredString;
	private BeefGuiLabel progressString;
	
	private GuiImageButton leftInvExposureButton;
	private GuiImageButton rightInvExposureButton;
	private GuiImageButton topInvExposureButton;
	private GuiImageButton bottomInvExposureButton;
	private GuiImageButton rearInvExposureButton;
	
	public GuiCyaniteReprocessor(Container container, TileEntityCyaniteReprocessor entity) {
		super(container);
		
		_entity = entity;
		xSize = 241;
	}
	
	@Override
	public void initGui() {
		super.initGui();

		int leftX = 4;
		int topY = 4;
		
		titleString = new BeefGuiLabel(this, _entity.getInvName(), leftX, topY);
		topY += titleString.getHeight() + 8;
		
		waterStoredString = new BeefGuiLabel(this, "Liquid: ???", leftX+7, topY);
		topY += waterStoredString.getHeight() + 8;

		powerStoredString = new BeefGuiLabel(this, "Power: ???", leftX, topY);
		topY += powerStoredString.getHeight() + 4;
		
		progressString = new BeefGuiLabel(this, "Progress: ???", leftX + titleString.getWidth() + 8, 4);
		
		registerControl(titleString);
		registerControl(powerStoredString);
		registerControl(waterStoredString);
		registerControl(progressString);
		
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
	protected String getGuiBackground() {
		return BigReactors.GUI_DIRECTORY + "CyaniteReprocessor.png";
	}

	@Override
	public void updateScreen() {
		super.updateScreen();

		powerStoredString.setLabelText(String.format("NRG: %d MJ", _entity.getEnergyStored()));
		
		LiquidStack waterStack = _entity.drain(0, _entity.getTankSize(0), false);
		
		
		float waterAmt = 0f;
		if(waterStack != null) {
			waterAmt = (float)waterStack.amount / (float)LiquidContainerRegistry.BUCKET_VOLUME;
		}
		waterStoredString.setLabelText(String.format("Liquid: %1.1f", waterAmt));
		
		if(_entity.isActive()) {
			progressString.setLabelText(String.format("Progress: %2.1f", _entity.getCycleCompletion() * 100f));
		}
		else {
			progressString.setLabelText(String.format("Progress: Inactive"));
		}

		int exposed = _entity.getExposedSlotFromReferenceSide(ForgeDirection.EAST.ordinal());
		rightInvExposureButton.displayString = getTextureForExposedInventory(exposed);

		exposed = _entity.getExposedSlotFromReferenceSide(ForgeDirection.WEST.ordinal());
		leftInvExposureButton.displayString = getTextureForExposedInventory(exposed);
		
		exposed = _entity.getExposedSlotFromReferenceSide(ForgeDirection.UP.ordinal());
		topInvExposureButton.displayString = getTextureForExposedInventory(exposed);

		exposed = _entity.getExposedSlotFromReferenceSide(ForgeDirection.DOWN.ordinal());
		bottomInvExposureButton.displayString = getTextureForExposedInventory(exposed);

		exposed = _entity.getExposedSlotFromReferenceSide(ForgeDirection.SOUTH.ordinal());
		rearInvExposureButton.displayString = getTextureForExposedInventory(exposed);
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float gameTicks) {
		super.drawScreen(mouseX, mouseY, gameTicks);
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		System.out.println(String.format("actionPerformed: %d", button.id));
		if(button.id >= 0 && button.id < 6) {
			System.out.println("Sending packet to server");
			PacketDispatcher.sendPacketToServer(PacketWrapper.createPacket(BigReactors.CHANNEL, Packets.SmallMachineButton,
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
	
}

package erogenousbeef.bigreactors.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.block.BlockBRSmallMachine;
import erogenousbeef.bigreactors.common.tileentity.TileEntityCyaniteReprocessor;
import erogenousbeef.bigreactors.gui.controls.BeefGuiFluidBar;
import erogenousbeef.bigreactors.gui.controls.BeefGuiLabel;
import erogenousbeef.bigreactors.gui.controls.BeefGuiPowerBar;
import erogenousbeef.bigreactors.gui.controls.BeefGuiProgressArrow;

public class GuiCyaniteReprocessor extends BeefGuiSmallMachineBase {

	private GuiButton _togglePort;
	private TileEntityCyaniteReprocessor _entity;

	private BeefGuiLabel titleString;
	
	private BeefGuiPowerBar powerBar;
	private BeefGuiFluidBar fluidBar;
	private BeefGuiProgressArrow progressArrow;
	
	public GuiCyaniteReprocessor(Container container, TileEntityCyaniteReprocessor entity) {
		super(container, entity);
		
		_entity = entity;
		xSize = 245;
		ySize = 175;
	}
	
	@Override
	public void initGui() {
		super.initGui();

		int leftX = guiLeft + 8;
		int topY = guiTop + 6;
		
		titleString = new BeefGuiLabel(this, _entity.getInventoryName(), leftX, topY);
		topY += titleString.getHeight() + 8;
		
		fluidBar = new BeefGuiFluidBar(this, guiLeft + 8, guiTop + 16, _entity, 0);
		powerBar = new BeefGuiPowerBar(this, guiLeft + 148, guiTop + 16, _entity);
		progressArrow = new BeefGuiProgressArrow(this, guiLeft + 76, guiTop + 41, 0, 178, _entity);
		
		registerControl(titleString);
		registerControl(powerBar);
		registerControl(fluidBar);
		registerControl(progressArrow);

		createInventoryExposureButtons(guiLeft + 180, guiTop + 4);
	}

	@Override
	public ResourceLocation getGuiBackground() {
		return new ResourceLocation(BigReactors.GUI_DIRECTORY + "CyaniteReprocessor.png");
	}

	@Override
	public void updateScreen() {
		super.updateScreen();
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float gameTicks) {
		super.drawScreen(mouseX, mouseY, gameTicks);
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		super.actionPerformed(button);
	}

	@Override
	protected int getBlockMetadata() {
		return BlockBRSmallMachine.META_CYANITE_REPROCESSOR;
	}
}

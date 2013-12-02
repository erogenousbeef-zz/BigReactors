package erogenousbeef.bigreactors.client.gui;

import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.tileentity.TileEntityReactorRedstonePort;
import erogenousbeef.bigreactors.gui.controls.BeefGuiLabel;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;

public class GuiReactorRedstonePort extends BeefGuiBase {

	private ResourceLocation _guiBackground;
	private TileEntityReactorRedstonePort port;

	BeefGuiLabel titleString;

	private GuiButton commitBtn;
	private GuiButton resetBtn;
	
	public GuiReactorRedstonePort(Container container, TileEntityReactorRedstonePort tileentity) {
		super(container);
		_guiBackground = new ResourceLocation(BigReactors.GUI_DIRECTORY + "BasicBackground.png");
		port = tileentity;
	}

	@Override
	public ResourceLocation getGuiBackground() {
		return _guiBackground;
	}

	@Override
	public void initGui() {
		super.initGui();

		int leftX = guiLeft + 4;
		int topY = guiTop + 6;
		
		titleString = new BeefGuiLabel(this, "Reactor Redstone Port", leftX+2, topY);
		topY += titleString.getHeight() + 4;
		
		commitBtn = new GuiButton(0, guiLeft + 116, guiTop + 142, 56, 20, "Commit");
		commitBtn.enabled = false;

		resetBtn  = new GuiButton(1, guiLeft + 4, guiTop + 142, 56, 20, "Reset");

		registerControl(titleString);

		this.buttonList.add(commitBtn);
		this.buttonList.add(resetBtn);
	}
	
	@Override
	public void updateScreen() {
		super.updateScreen();
	}
	
	@Override
	protected void actionPerformed(GuiButton button) {
		if(button.id == 0) {
			// TODO: SUMBIT
		}
		else if(button.id == 1) {
			// TODO: RESET
		}
	}
}

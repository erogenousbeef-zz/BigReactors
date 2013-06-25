package erogenousbeef.bigreactors.gui.controls.grab;

import erogenousbeef.bigreactors.client.gui.BeefGuiBase;
import erogenousbeef.bigreactors.gui.BeefGuiControlBase;
import erogenousbeef.bigreactors.gui.IBeefTooltipControl;
import net.minecraft.util.Icon;

/**
 * A class for objects which can be clicked
 */
public class BeefGuiGrabSource extends BeefGuiControlBase implements IBeefTooltipControl {

	protected static final int defaultHoverColor = 0x33ffffff; // 20% alpha white
	
	protected IBeefGuiGrabbable grabbable;
	protected int hoverColor;
	
	public BeefGuiGrabSource(BeefGuiBase container, int x, int y, IBeefGuiGrabbable grabbable) {
		super(container, x, y, 16, 16);
		this.grabbable = grabbable;
		hoverColor = defaultHoverColor;
	}

	@Override
	public void drawForeground(int mouseX, int mouseY) {
		if(this.grabbable != null) {
			this.guiContainer.drawTexturedModelRectFromIcon(x, y, grabbable.getIcon(), width, height);
		}
		
		if(this.isMouseOver(mouseX, mouseY)) {
			this.drawRect(this.x, this.y, this.x+this.width, this.y+this.height, hoverColor);
		}
	}
	
	public IBeefGuiGrabbable getGrabbable() { return grabbable; }

	@Override
	public void drawBackground(int mouseX, int mouseY) {
	}
	
	@Override
	public void onMouseClicked(int mouseX, int mouseY, int buttonIndex) {
		if(buttonIndex == 0 && isMouseOver(mouseX, mouseY)) {
			this.guiContainer.setGrabbedItem(grabbable);
		}
	}

	@Override
	public String getTooltip() {
		return this.grabbable.getName();
	}
}

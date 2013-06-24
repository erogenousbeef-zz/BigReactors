package erogenousbeef.bigreactors.gui;

import erogenousbeef.bigreactors.client.gui.BeefGuiBase;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;

public abstract class BeefGuiControlBase implements IBeefGuiControl {

	protected BeefGuiBase guiContainer;
	protected int x, y;
	protected int width, height;
	
	protected BeefGuiControlBase(BeefGuiBase container, int x, int y, int width, int height) {
		this.guiContainer = container;
		this.x = x;
		this.y = y;
		this.height = height;
		this.width = width;
	}

	/**
	 * Check if the mouse is over this control.
	 * @param mouseX Screen-relative mouse X coordinate.
	 * @param mouseY Screen-relative mouse Y coordinate.
	 * @return True if the mouse is over this control, false otherwise.
	 */
	public boolean isMouseOver(int mouseX, int mouseY) {
		if(mouseX < x || mouseX > x+width || mouseY < y || mouseY > y+height) { return false; }
		return true;
	}
	
	public int getWidth() { return width; }
	public int getHeight() { return height; }
	
	
	/**
	 * Handle mouse clicks. Called for all clicks, not just ones inside the control.
	 * @param mouseX Screen-relative mouse X coordinate.
	 * @param mouseY Screen-relative mouse Y coordinate.
	 * @param mouseButton Button being pressed. 0 = left, 1 = right, 2 = middle.
	 */
	public void onMouseClicked(int mouseX, int mouseY, int mouseButton) {}
}

package erogenousbeef.bigreactors.gui;

import net.minecraft.client.gui.FontRenderer;

public interface IBeefGuiControl {
	/**
	 * Draw control background. Has window-relative coordinates.
	 * @param mouseX Window-relative X position of the mouse cursor.
	 * @param mouseY Window-relative Y position of the mouse cursor.
	 */
	public void drawBackground(int mouseX, int mouseY);
	
	/**
	 * Draw control foreground. Has window-relative coordinates.
	 * @param mouseX Window-relative X position of the mouse cursor.
	 * @param mouseY Window-relative Y position of the mouse cursor.
	 */
	public void drawForeground(int mouseX, int mouseY);
}

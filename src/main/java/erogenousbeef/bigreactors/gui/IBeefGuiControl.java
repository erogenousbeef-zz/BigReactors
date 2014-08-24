package erogenousbeef.bigreactors.gui;

import net.minecraft.client.renderer.texture.TextureManager;

public interface IBeefGuiControl {
	/**
	 * Draw control background. Has window-relative coordinates.
	 * @param mouseX Window-relative X position of the mouse cursor.
	 * @param mouseY Window-relative Y position of the mouse cursor.
	 */
	public void drawBackground(TextureManager renderEngine, int mouseX, int mouseY);
	
	/**
	 * Draw control foreground. Has window-relative coordinates.
	 * @param mouseX Window-relative X position of the mouse cursor.
	 * @param mouseY Window-relative Y position of the mouse cursor.
	 */
	public void drawForeground(TextureManager renderEngine, int mouseX, int mouseY);

	/**
	 * Handle mouse clicks. Called for all clicks, not just ones inside the control.
	 * @param mouseX Screen-relative mouse X coordinate.
	 * @param mouseY Screen-relative mouse Y coordinate.
	 * @param mouseButton Button being pressed. 0 = left, 1 = right, 2 = middle.
	 */
	public void onMouseClicked(int mouseX, int mouseY, int mouseButton);
}

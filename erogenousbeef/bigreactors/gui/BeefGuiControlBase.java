package erogenousbeef.bigreactors.gui;

import org.lwjgl.opengl.GL11;

import erogenousbeef.bigreactors.client.gui.BeefGuiBase;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.Tessellator;
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
	
	// Static Helpers
	protected static void drawRect(int xMin, int yMin, int xMax, int yMax, int color)
	{
		int temp;

		if (xMax < xMin) {
			temp = xMin;
			xMin = xMax;
			xMax = temp;
		}

		if (yMax < yMin) {
			temp = yMin;
			yMin = yMax;
			yMax = temp;
		}

		float a = (float)(color >> 24 & 255) / 255.0F;
		float r = (float)(color >> 16 & 255) / 255.0F;
		float g = (float)(color >> 8 & 255) / 255.0F;
		float b = (float)(color & 255) / 255.0F;
		Tessellator tessellator = Tessellator.instance;
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glColor4f(r, g, b, a);
		tessellator.startDrawingQuads();
		tessellator.addVertex(xMin, yMax, 0.0D);
		tessellator.addVertex(xMax, yMax, 0.0D);
		tessellator.addVertex(xMax, yMin, 0.0D);
		tessellator.addVertex(xMin, yMin, 0.0D);
		tessellator.draw();
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_BLEND);
	}	
}

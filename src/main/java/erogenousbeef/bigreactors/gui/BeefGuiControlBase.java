package erogenousbeef.bigreactors.gui;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;

import org.lwjgl.opengl.GL11;

import erogenousbeef.bigreactors.client.gui.BeefGuiBase;

public abstract class BeefGuiControlBase implements IBeefGuiControl {

	protected BeefGuiBase guiContainer;
	protected int absoluteX, absoluteY; // Screen-relative X/Y (for backgrounds)
	protected int relativeX, relativeY; // GUI-relative X/Y (for foregrounds)
	protected int width, height;
	
	public boolean visible;
	
	// We use absolute coords to match other Minecraft controls.
	protected BeefGuiControlBase(BeefGuiBase container, int absoluteX, int absoluteY, int width, int height) {
		this.guiContainer = container;
		this.absoluteX = absoluteX;
		this.absoluteY = absoluteY;
		this.relativeX = absoluteX - container.getGuiLeft();
		this.relativeY = absoluteY - container.getGuiTop();
		this.height = height;
		this.width = width;
		visible = true;
	}

	/**
	 * Check if the mouse is over this control.
	 * @param mouseX Screen-relative mouse X coordinate.
	 * @param mouseY Screen-relative mouse Y coordinate.
	 * @return True if the mouse is over this control, false otherwise.
	 */
	public boolean isMouseOver(int mouseX, int mouseY) {
		if(mouseX < absoluteX || mouseX > absoluteX+width || mouseY < absoluteY || mouseY > absoluteY+height) { return false; }
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
		float a = (float)(color >> 24 & 255) / 255.0F;
		float r = (float)(color >> 16 & 255) / 255.0F;
		float g = (float)(color >> 8 & 255) / 255.0F;
		float b = (float)(color & 255) / 255.0F;
		drawRect(xMin, yMin, xMax, yMax, r, g, b, a);
	}
	
	protected static void drawRect(int xMin, int yMin, int xMax, int yMax, float r, float g, float b, float a)
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
	
    protected static void drawTexturedModelRectFromIcon(int x, int y, IIcon icon, int width, int height)
    {
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV((double)(x + 0), (double)(y + height), 0.0D, (double)icon.getMinU(), (double)icon.getMaxV());
        tessellator.addVertexWithUV((double)(x + width), (double)(y + height), 0.0D, (double)icon.getMaxU(), (double)icon.getMaxV());
        tessellator.addVertexWithUV((double)(x + width), (double)(y + 0), 0.0D, (double)icon.getMaxU(), (double)icon.getMinV());
        tessellator.addVertexWithUV((double)(x + 0), (double)(y + 0), 0.0D, (double)icon.getMinU(), (double)icon.getMinV());
        tessellator.draw();
    }
	
    public boolean isVisible() { return visible; }
}

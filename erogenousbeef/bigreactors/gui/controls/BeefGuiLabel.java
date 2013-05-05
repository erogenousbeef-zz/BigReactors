package erogenousbeef.bigreactors.gui.controls;

import erogenousbeef.bigreactors.gui.IBeefGuiControl;
import erogenousbeef.bigreactors.gui.IBeefTooltipControl;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderEngine;

public class BeefGuiLabel implements IBeefGuiControl, IBeefTooltipControl {

	protected int x, y;
	protected int xSize, ySize;
	protected int xMax;	// If >0, wrap text at this pixel level. Does not split words. 
	protected String labelText;
	protected String labelTooltip;
	protected boolean dropShadow;
	protected int color;
	
	public int getWidth() { return xSize; }
	public int getHeight() { return ySize; }
	
	public String getLabelText() { return labelText; }
	public String getLabelTooltip() { return labelTooltip; }

	public void setLabelText(String newText, FontRenderer renderer) { 
		if(newText == labelText) { return; }
		labelText = newText;
		recalculateSize(renderer);		
	}

	public void setLabelTooltip(String newTooltip) { labelTooltip = newTooltip; }
	public void setDropShadow(boolean shadow) { dropShadow = shadow; }
	public void setColor(int color) { this.color = color; }
	
	// If set to 0 or less, disables wrapping.
	public void setWordWrapLength(int pixels) { xMax = pixels; }
	
	public BeefGuiLabel(String text, int x, int y, FontRenderer renderer) {
		this.labelText = text;
		this.x = x;
		this.y = y;
		
		recalculateSize(renderer);
	}

	private void recalculateSize(FontRenderer renderer) {
		if(xMax > 0) {
			xSize = renderer.splitStringWidth(labelText, xMax);
			int totalWidth = renderer.getStringWidth(labelText);
			ySize = renderer.FONT_HEIGHT * Math.max(1, (totalWidth / xSize));		
		}
		else {
			xSize = renderer.getStringWidth(labelText);
			ySize = renderer.FONT_HEIGHT;
		}
	}
	
	public boolean isMouseOver(int mouseX, int mouseY) {
		if(mouseX < x || mouseX > x+xSize || mouseY < y || mouseY > y+ySize) { return false; }
		return true;
	}
	
	public void render(RenderEngine renderEngine, FontRenderer renderer) {
		if(xMax > 0) {
			renderer.drawSplitString(labelText, x, y, color, xMax);
		}
		else {
			renderer.drawString(labelText, x, y, color, dropShadow);
		}
	}
	
	public void renderTooltip(RenderEngine renderEngine, FontRenderer fontRenderer, int mouseX, int mouseY) {
		fontRenderer.drawString(labelTooltip, mouseX, mouseY, 0, false);
	}
}

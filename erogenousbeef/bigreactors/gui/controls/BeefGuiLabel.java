package erogenousbeef.bigreactors.gui.controls;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import erogenousbeef.bigreactors.client.gui.BeefGuiBase;
import erogenousbeef.bigreactors.gui.BeefGuiControlBase;
import erogenousbeef.bigreactors.gui.IBeefGuiControl;
import erogenousbeef.bigreactors.gui.IBeefTooltipControl;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderEngine;
import net.minecraft.inventory.Container;

@SideOnly(Side.CLIENT)
public class BeefGuiLabel extends BeefGuiControlBase implements IBeefTooltipControl {

	protected int xMax;	// If >0, wrap text at this pixel level. Does not split words. 
	protected String labelText;
	protected String labelTooltip;
	protected boolean dropShadow;
	protected int color;
	
	public String getLabelText() { return labelText; }
	public String getLabelTooltip() { return labelTooltip; }

	public void setLabelText(String newText) { 
		if(newText == labelText) { return; }
		labelText = newText;
		recalculateSize();
	}

	public void setLabelTooltip(String newTooltip) { labelTooltip = newTooltip; }
	public void setDropShadow(boolean shadow) { dropShadow = shadow; }
	public void setColor(int color) { this.color = color; }
	
	// If set to 0 or less, disables wrapping.
	public void setWordWrapLength(int pixels) { xMax = pixels; }
	
	public BeefGuiLabel(BeefGuiBase container, String text, int x, int y) {
		super(container, x, y, 0, 0);
		this.labelText = text;
		recalculateSize();
	}

	private void recalculateSize() {
		FontRenderer fontRenderer = guiContainer.getFontRenderer();
		if(xMax > 0) {
			this.width = fontRenderer.splitStringWidth(labelText, xMax);
			int totalWidth = fontRenderer.getStringWidth(labelText);
			this.height = fontRenderer.FONT_HEIGHT * Math.max(1, (totalWidth / width));		
		}
		else {
			this.width = fontRenderer.getStringWidth(labelText);
			this.height = fontRenderer.FONT_HEIGHT;
		}
	}
	
	@Override
	public void drawBackground(int mouseX, int mouseY) {
	}
	
	@Override
	public void drawForeground(int mouseX, int mouseY) {
		FontRenderer fontRenderer = guiContainer.getFontRenderer();
		if(xMax > 0) {
			fontRenderer.drawSplitString(labelText, x, y, color, xMax);
		}
		else {
			fontRenderer.drawString(labelText, x, y, color, dropShadow);
		}
	}
	@Override
	public String getTooltip() {
		return labelTooltip;
	}
}

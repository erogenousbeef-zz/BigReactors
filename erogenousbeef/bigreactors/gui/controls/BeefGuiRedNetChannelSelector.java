package erogenousbeef.bigreactors.gui.controls;

import net.minecraft.client.renderer.RenderEngine;
import net.minecraft.entity.passive.EntitySheep;
import erogenousbeef.bigreactors.client.gui.BeefGuiBase;
import erogenousbeef.bigreactors.gui.BeefGuiControlBase;
import erogenousbeef.bigreactors.gui.IBeefGuiControl;
import erogenousbeef.bigreactors.gui.IBeefTooltipControl;

public class BeefGuiRedNetChannelSelector extends BeefGuiControlBase implements IBeefGuiControl,
		IBeefTooltipControl {

	String caption;
	int colorIdx;
	boolean selected;
	
	protected int barHeight = 4;
	
	/**
	 * 
	 * @param container
	 * @param caption
	 * @param colorIdx Index in the EntitySheep color table. Always 100% opacity.
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public BeefGuiRedNetChannelSelector(BeefGuiBase container, String caption, int colorIdx, int x, int y, int width, int height) {
		super(container, x, y, width, height);
		this.caption = caption;
		this.colorIdx = colorIdx;
		this.selected = false;
	}
	
	@Override
	public boolean isMouseOver(int mouseX, int mouseY) {
		// Only mouse over when the color or line are moused over.
		if(mouseX < x+height && mouseX >= x) {
			return mouseY >= y && mouseY <= y+height;
		}
		else if(mouseX < x+width-height && mouseX >= x+height) {
			int barTop = y + height/2 - (barHeight/2-1);
			return mouseY >= barTop && mouseY < barTop+barHeight;
		}
		return false;
	}

	@Override
	public String getTooltip() {
		return caption;
	}

	@Override
	public void drawBackground(RenderEngine renderEngine, int mouseX, int mouseY) {
		int guiLeft = guiContainer.getGuiLeft();
		int guiTop = guiContainer.getGuiTop();
		int relativeX = x + guiLeft;
		int relativeY = y + guiTop;
		
		int barTop = relativeY + height/2 - (barHeight/2-1);
		int borderColor = 0xff000000;
		if(this.selected) {
			borderColor = 0xff22dd22; // bright green?
		}
		this.drawRect(relativeX, relativeY, relativeX+height, relativeY+height, borderColor);
		this.drawRect(relativeX+width-height, relativeY, relativeX+width, relativeY+height, borderColor);
		this.drawRect(relativeX+height, barTop, relativeX+width-height, barTop+barHeight, borderColor);
		
		float[] color = EntitySheep.fleeceColorTable[this.colorIdx];
		
		this.drawRect(relativeX+1, relativeY+1, relativeX+height-1, relativeY+height-1, color[0], color[1], color[2], 1.0f);
		this.drawRect(relativeX+width-height+1, relativeY+1, relativeX+width-1, relativeY+height-1, 0xff777777);
	}

	@Override
	public void drawForeground(RenderEngine renderEngine, int mouseX, int mouseY) {
	}

	@Override
	public void onMouseClicked(int mouseX, int mouseY, int mouseButton) {
		if(mouseButton == 0 && isMouseOver(mouseX, mouseY)) {
			guiContainer.onControlClicked(this);
		}
	}
	
	public void setSelected(boolean selected) {
		this.selected = selected;
	}

}

package erogenousbeef.bigreactors.gui.controls;

import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.passive.EntitySheep;
import erogenousbeef.bigreactors.client.gui.BeefGuiBase;
import erogenousbeef.bigreactors.gui.BeefGuiControlBase;
import erogenousbeef.bigreactors.gui.IBeefGuiControl;
import erogenousbeef.bigreactors.gui.IBeefTooltipControl;

public class BeefGuiRedNetChannelSelector extends BeefGuiControlBase implements IBeefGuiControl,
		IBeefTooltipControl {

	String caption;
	int channelIdx;
	boolean selected;
	
	protected int barHeight = 4;
	
	/**
	 * 
	 * @param container
	 * @param caption
	 * @param channelIdx Index of the channel, also the color index in the EntitySheep color table. (Always 100% opacity.)
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public BeefGuiRedNetChannelSelector(BeefGuiBase container, String caption, int colorIdx, int x, int y, int width, int height) {
		super(container, x, y, width, height);
		this.caption = caption;
		this.channelIdx = colorIdx;
		this.selected = false;
	}
	
	@Override
	public boolean isMouseOver(int mouseX, int mouseY) {
		// Only mouse over when the color or line are moused over.
		if(mouseX < absoluteX+width && mouseX >= absoluteX) {
			return mouseY >= absoluteY && mouseY <= absoluteY+height;
		}
		else if(mouseX < absoluteX+width-height && mouseX >= absoluteX+height) {
			int barTop = absoluteY + height/2 - (barHeight/2-1);
			return mouseY >= barTop && mouseY < barTop+barHeight;
		}
		return false;
	}

	@Override
	public String getTooltip() {
		return caption;
	}

	@Override
	public void drawBackground(TextureManager renderEngine, int mouseX, int mouseY) {
		int barTop = absoluteY + height/2 - (barHeight/2-1);
		int borderColor = 0xff000000;
		if(this.selected) {
			borderColor = 0xff22dd22; // bright green?
		}
		this.drawRect(absoluteX, absoluteY, absoluteX+height, absoluteY+height, borderColor);
		this.drawRect(absoluteX+width-height, absoluteY, absoluteX+width, absoluteY+height, borderColor);
		this.drawRect(absoluteX+height, barTop, absoluteX+width-height, barTop+barHeight, borderColor);
		
		float[] color = EntitySheep.fleeceColorTable[this.channelIdx];
		
		this.drawRect(absoluteX+1, absoluteY+1, absoluteX+height-1, absoluteY+height-1, color[0], color[1], color[2], 1.0f);
		this.drawRect(absoluteX+width-height+1, absoluteY+1, absoluteX+width-1, absoluteY+height-1, 0xff777777);
	}

	@Override
	public void drawForeground(TextureManager renderEngine, int mouseX, int mouseY) {
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

	public boolean isSelected() {
		return selected;
	}

	public int getChannel() {
		return channelIdx;
	}
	
}

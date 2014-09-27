package erogenousbeef.bigreactors.gui.controls;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.IIcon;

import org.lwjgl.opengl.GL11;

import erogenousbeef.bigreactors.client.gui.BeefGuiBase;
import erogenousbeef.bigreactors.gui.IBeefTooltipControl;

public class GuiSelectableButton extends GuiButton implements IBeefTooltipControl {

	private boolean selected;
	private int selectedColor;
	private IIcon icon;
	
	private BeefGuiBase window;

	public GuiSelectableButton(int id, int x, int y, IIcon icon, int selectedColor, BeefGuiBase containingWindow) {
		super(id, x, y, 24, 24, "");
		selected = false;
		this.icon = icon;
		this.selectedColor = selectedColor;
		window = containingWindow;
	}
	
	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	
	public boolean isSelected() { return this.selected; }

	@Override
	public void drawButton(Minecraft minecraft, int par2, int par3) {
        if (this.visible)
        {
            minecraft.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
            this.field_146123_n = par2 >= this.xPosition && par3 >= this.yPosition && par2 < this.xPosition + this.width && par3 < this.yPosition + this.height;
            
            int k = this.getHoverState(this.field_146123_n);
            int borderColor = this.selected ? this.selectedColor : 0xFF000000;
            int bgColor = 0xFF565656; // disabled
            if(k == 1) {
            	bgColor = 0xFF999999; // enabled
            }
            else if(k == 2) {
            	bgColor = 0xFF9999CC; // hovered
            	borderColor = this.selected ? this.selectedColor : 0xFF5555AA;
            }

        	this.drawRect(this.xPosition, this.yPosition, this.xPosition+this.width, this.yPosition+this.height, borderColor);
        	this.drawRect(this.xPosition+1, this.yPosition+1, this.xPosition+this.width-1, this.yPosition+this.height-1, bgColor);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.drawTexturedModelRectFromIcon(this.xPosition+1, this.yPosition+1, this.icon, this.width-2, this.height-2);
            
            this.mouseDragged(minecraft, par2, par3);
        }
	}

	@Override
	public boolean isMouseOver(int mouseX, int mouseY) {
		if(mouseX < xPosition || mouseX > xPosition+width || mouseY < yPosition || mouseY > yPosition+height) { return false; }
		return true;
	}

	@Override
	public String[] getTooltip() {
		return new String[] { this.displayString };
	}

	@Override
	public boolean isVisible() {
		return visible;
	}
}

package erogenousbeef.bigreactors.gui.controls;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.Icon;

import org.lwjgl.opengl.GL11;

import erogenousbeef.bigreactors.gui.IBeefTooltipControl;

public class GuiIconButton extends GuiButton implements IBeefTooltipControl {

	protected Icon icon;
	
	protected String[] tooltip;
	
	public GuiIconButton(int buttonId, int x, int y, int width, int height) {
		super(buttonId, x, y, width, height, "");
		icon = null;
		tooltip = null;
	}

	public GuiIconButton(int buttonId, int x, int y, int width, int height, Icon icon) {
		this(buttonId, x, y, width, height);
		this.icon = icon;
		tooltip = null;
	}

	public GuiIconButton(int buttonId, int x, int y, int width, int height, Icon icon, String[] tooltip) {
		this(buttonId, x, y, width, height, icon);
		this.tooltip = tooltip;
	}
	
	public void setIcon(Icon icon) {
		this.icon = icon;
	}

    /**
     * Draws this button to the screen.
     */
    public void drawButton(Minecraft par1Minecraft, int par2, int par3)
    {
        if (this.drawButton)
        {
            FontRenderer fontrenderer = par1Minecraft.fontRenderer;
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

            // Draw the border
            this.field_82253_i = par2 >= this.xPosition && par3 >= this.yPosition && par2 < this.xPosition + this.width && par3 < this.yPosition + this.height;
            int k = this.getHoverState(this.field_82253_i);
            int borderColor = k == 2 ? 0xFF5555AA : 0xFF000000;
        	this.drawRect(this.xPosition, this.yPosition, this.xPosition+this.width, this.yPosition+this.height, borderColor);
            
            this.mouseDragged(par1Minecraft, par2, par3);

            // Draw the icon
            if(this.icon != null) {
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                par1Minecraft.renderEngine.bindTexture(TextureMap.locationBlocksTexture);
            	this.drawTexturedModelRectFromIcon(this.xPosition + 1, this.yPosition + 1, this.icon, this.width-2, this.height-2);
            }
        }
    }

	@Override
	public boolean isMouseOver(int mouseX, int mouseY) {
		if(mouseX < xPosition || mouseX > xPosition+width || mouseY < yPosition || mouseY > yPosition+height) { return false; }
		return true;
	}

	public void setTooltip(String[] tooltip) {
		this.tooltip = tooltip;  
	}
	
	@Override
	public String[] getTooltip() {
		if(this.drawButton) {
			return tooltip;
		}
		else {
			return null;
		}
	}

	@Override
	public boolean isVisible() {
		return drawButton;
	}
}

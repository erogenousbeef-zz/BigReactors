package erogenousbeef.bigreactors.gui.controls;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.Icon;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

public class GuiIconButton extends GuiButton {

	protected Icon icon;
	
	public GuiIconButton(int buttonId, int x, int y, int width, int height, Icon icon) {
		super(buttonId, x, y, width, height, "");
		this.icon = icon;
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
}

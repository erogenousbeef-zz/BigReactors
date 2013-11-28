package erogenousbeef.bigreactors.gui.controls;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

public class GuiImageButton extends GuiButton {

	protected static Map<String, ResourceLocation> _cache = new HashMap<String, ResourceLocation>();
	
	public GuiImageButton(int buttonId, int x, int y, int width, int height,
			String imagePath) {
		super(buttonId, x, y, width, height, imagePath);
	}

    /**
     * Draws this button to the screen.
     */
    public void drawButton(Minecraft par1Minecraft, int par2, int par3)
    {
        if (this.drawButton)
        {
            FontRenderer fontrenderer = par1Minecraft.fontRenderer;
            par1Minecraft.renderEngine.bindTexture(buttonTextures);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.field_82253_i = par2 >= this.xPosition && par3 >= this.yPosition && par2 < this.xPosition + this.width && par3 < this.yPosition + this.height;
            int k = this.getHoverState(this.field_82253_i);
            this.drawTexturedModalRect(this.xPosition, this.yPosition, 0, 46 + k * 20, this.width / 2, this.height);
            this.drawTexturedModalRect(this.xPosition + this.width / 2, this.yPosition, 200 - this.width / 2, 46 + k * 20, this.width / 2, this.height);
            this.mouseDragged(par1Minecraft, par2, par3);

            // Draw the displayString as an image, if set
        	// TODO: FIX THIS - for some reason it doesn't draw the whole image :(
            if(!this.displayString.equals("")) {
            	ResourceLocation resourceLoc;
            	if(_cache.containsKey(this.displayString)) {
            		resourceLoc = _cache.get(this.displayString);
            	}
            	else {
            		resourceLoc = new ResourceLocation(displayString);
            		_cache.put(displayString, resourceLoc);
            	}

                par1Minecraft.renderEngine.bindTexture(resourceLoc);
                this.drawTexturedModalRect(this.xPosition + 2 , this.yPosition + 2, 0, 0, this.width - 4, this.height - 4);
            }
        }
    }
	
	
}

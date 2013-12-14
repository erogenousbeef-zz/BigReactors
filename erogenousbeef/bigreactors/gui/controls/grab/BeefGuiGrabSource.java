package erogenousbeef.bigreactors.gui.controls.grab;

import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;

import org.lwjgl.opengl.GL11;

import erogenousbeef.bigreactors.client.gui.BeefGuiBase;
import erogenousbeef.bigreactors.gui.BeefGuiControlBase;
import erogenousbeef.bigreactors.gui.IBeefTooltipControl;

/**
 * A class for objects which can be clicked
 */
public class BeefGuiGrabSource extends BeefGuiControlBase implements IBeefTooltipControl {

	protected static final int defaultHoverColor = 0x33ffffff; // 20% alpha white
	
	protected IBeefGuiGrabbable grabbable;
	protected int hoverColor;
	
	public BeefGuiGrabSource(BeefGuiBase container, int x, int y, IBeefGuiGrabbable grabbable) {
		super(container, x, y, 16, 16);
		this.grabbable = grabbable;
		hoverColor = defaultHoverColor;
	}

	@Override
	public void drawForeground(TextureManager renderEngine, int mouseX, int mouseY) {
		if(this.grabbable != null) {
			renderEngine.bindTexture( TextureMap.locationBlocksTexture );
			GL11.glColor4f(1f, 1f, 1f, 1f);
			this.guiContainer.drawTexturedModelRectFromIcon(relativeX, relativeY, grabbable.getIcon(), width, height);
		}
		else {
			this.drawRect(this.relativeX, this.relativeY, this.relativeX+this.width, this.relativeY+this.height, 0x66ff0000); // Red error spot			
		}
		
		if(this.isMouseOver(mouseX, mouseY)) {
			this.drawRect(this.relativeX, this.relativeY, this.relativeX+this.width, this.relativeY+this.height, hoverColor);
		}
	}
	
	public IBeefGuiGrabbable getGrabbable() { return grabbable; }

	@Override
	public void drawBackground(TextureManager renderEngine, int mouseX, int mouseY) {
		// Draw Border
		this.drawRect(absoluteX-1, absoluteY-1, absoluteX+width+1, absoluteY+height+1, 0xff222222);
		
		// Draw Background
		this.drawRect(absoluteX, absoluteY, absoluteX+width, absoluteY+height, 0xff777777);
	}
	
	@Override
	public void onMouseClicked(int mouseX, int mouseY, int buttonIndex) {
		if(buttonIndex == 0 && isMouseOver(mouseX, mouseY)) {
			this.guiContainer.setGrabbedItem(grabbable);
		}
	}

	@Override
	public String[] getTooltip() {
		return new String[] { this.grabbable.getName() };
	}
}

package erogenousbeef.bigreactors.client.gui;

import java.util.LinkedList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import erogenousbeef.bigreactors.gui.IBeefGuiControl;
import erogenousbeef.bigreactors.gui.IBeefTooltipControl;

import net.minecraft.client.gui.GuiScreen;

public abstract class BeefGuiBase extends GuiScreen {

	protected List<IBeefGuiControl> controls;
	protected List<IBeefTooltipControl> controlsWithTooltips;
	
	// Set these!
	protected int xSize = 200;
	protected int ySize = 200;
	
	public BeefGuiBase() {
		super();
		
		controls = new LinkedList<IBeefGuiControl>();
		controlsWithTooltips = new LinkedList<IBeefTooltipControl>();
	}
	
	// Add controls, etc.
	@Override
	public void initGui() {
		super.initGui();
	}

	public void registerControl(IBeefGuiControl newControl) {
		controls.add(newControl);
		
		if(newControl instanceof IBeefTooltipControl) {
			controlsWithTooltips.add((IBeefTooltipControl) newControl);
		}
	}
	
	@Override
    public void drawScreen(int mouseX, int mouseY, float gameTicks) {
    	super.drawScreen(mouseX, mouseY, gameTicks);
    	
    	drawBackground(mouseX, mouseY, gameTicks);
    	drawForeground(mouseX, mouseY, gameTicks);
    }
	
	protected void drawBackground(int mouseX, int mouseY, float gameTicks) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.renderEngine.bindTexture(getGuiBackground());
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;
		this.drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
	}
	
	// Override to draw your custom controls
	protected void drawForeground(int mouseX, int mouseY, float gameTicks) {
		for(IBeefGuiControl c : controls) {
			c.render(this.mc.renderEngine, this.fontRenderer);
		}

		for(IBeefTooltipControl tc: controlsWithTooltips) {
			if(tc.isMouseOver(mouseX,  mouseY)) {
				tc.renderTooltip(this.mc.renderEngine, this.fontRenderer, mouseX, mouseY);
				break;
			}
		}
	}
	
	protected abstract String getGuiBackground();
}

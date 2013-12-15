package erogenousbeef.bigreactors.gui.controls;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import erogenousbeef.bigreactors.client.gui.BeefGuiBase;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.gui.BeefGuiControlBase;

public abstract class BeefGuiVerticalProgressBar extends BeefGuiControlBase {
	private final static int controlWidth = 20;
	private final static int controlHeight = 64;

	protected ResourceLocation controlResource;

	private double backgroundLeftU = 0;
	private double backgroundRightU = 0.32;
	
	private double gradationLeftU = 0.77;
	private double gradationRightU = 1;
	
	protected float barAbsoluteMaxHeight;
	
	public BeefGuiVerticalProgressBar(BeefGuiBase container, int x, int y) {
		super(container, x, y, controlWidth, controlHeight);
		
		controlResource = new ResourceLocation(BigReactors.GUI_DIRECTORY + getBackgroundTexture());
		
		backgroundLeftU = getBackgroundLeftU();
		backgroundRightU = getBackgroundRightU();
		gradationLeftU = getGradationLeftU();
		gradationRightU = getGradationRightU();
		
		barAbsoluteMaxHeight = this.height - 1;
		
	}
	
	protected boolean drawGradationMarks() { return false; }
	protected String getBackgroundTexture() { return "controls/FluidTank.png"; }

	protected abstract float getProgress();
	protected abstract void drawProgressBar(Tessellator tessellator, TextureManager renderEngine, int barMinX, int barMaxX, int barMinY, int barMaxY, int zLevel);
	
	protected double getBackgroundLeftU() { return 0; }
	protected double getBackgroundRightU() { return 0.32; }
	protected double getGradationLeftU() { return 0.77; }
	protected double getGradationRightU() { return 1; } 
	
	@Override
	public void drawBackground(TextureManager renderEngine, int mouseX, int mouseY) {
		// Draw the background
		GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		renderEngine.bindTexture(controlResource);
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		tessellator.addVertexWithUV(this.absoluteX, this.absoluteY + this.height, backgroundLeftU, 0, 1.0);
		tessellator.addVertexWithUV(this.absoluteX + this.width, this.absoluteY + this.height, 0, backgroundRightU, 1.0);
		tessellator.addVertexWithUV(this.absoluteX + this.width, this.absoluteY, 0, backgroundRightU, 0);
		tessellator.addVertexWithUV(this.absoluteX, this.absoluteY, 0, backgroundLeftU, 0);
		tessellator.draw();
		
		float progress = getProgress();
		// Draw the bar itself, on top of the background
		if(progress > 0.0) {
			int barHeight = Math.max(1, (int)Math.floor(progress * barAbsoluteMaxHeight));
			int barMinX = this.absoluteX + 1;
			int barMaxX = this.absoluteX + this.width - 4;
			int barMinY = this.absoluteY + this.height - barHeight;
			int barMaxY = this.absoluteY + this.height - 1;
			
			this.drawProgressBar(tessellator, renderEngine, barMinX, barMaxX, barMinY, barMaxY, 1);
		}
		
		if(drawGradationMarks()) {
			GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
			renderEngine.bindTexture(controlResource);
			tessellator.startDrawingQuads();
			tessellator.addVertexWithUV(this.absoluteX, this.absoluteY + this.height, 2, gradationLeftU, 1.0);
			tessellator.addVertexWithUV(this.absoluteX + this.width - 4, this.absoluteY + this.height, 2, gradationRightU, 1.0);
			tessellator.addVertexWithUV(this.absoluteX + this.width - 4, this.absoluteY, 2, gradationRightU, 0);
			tessellator.addVertexWithUV(this.absoluteX, this.absoluteY, 2, gradationLeftU, 0);
			tessellator.draw();
		}
	}

	@Override
	public void drawForeground(TextureManager renderEngine, int mouseX, int mouseY) {
		
	}
}

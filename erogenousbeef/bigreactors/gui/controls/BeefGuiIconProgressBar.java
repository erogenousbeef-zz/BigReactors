package erogenousbeef.bigreactors.gui.controls;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import erogenousbeef.bigreactors.client.gui.BeefGuiBase;

public abstract class BeefGuiIconProgressBar extends BeefGuiVerticalProgressBar {

	public BeefGuiIconProgressBar(BeefGuiBase container, int x, int y) {
		super(container, x, y);
		
	}
	
	protected abstract IIcon getProgressBarIcon();
	protected abstract ResourceLocation getResourceLocation();
	
	@Override
	protected void drawProgressBar(Tessellator tessellator, TextureManager renderEngine, int barMinX, int barMaxX, int barMinY, int barMaxY, int zLevel) {
		Icon progressBarIcon = getProgressBarIcon();
		if(progressBarIcon == null) {
			return;
		}

		double minU = progressBarIcon.getMinU();
		double minV = progressBarIcon.getMinV();
		double maxU = progressBarIcon.getMaxU();
		double maxV = progressBarIcon.getMaxV();
		
		renderEngine.bindTexture(getResourceLocation());
		
		// Draw the bar in 16-pixel slices from the bottom up.
		for(int slicedBarY = barMaxY; slicedBarY > 0; slicedBarY -= 16) {
			int slicedBarHeight = (int)Math.min(slicedBarY - barMinY, 16.0f);
			tessellator.startDrawingQuads();
			tessellator.addVertexWithUV(barMinX, slicedBarY, zLevel, minU, minV + (maxV - minV) * slicedBarHeight / 16.0f);
			tessellator.addVertexWithUV(barMaxX, slicedBarY, zLevel, maxU, minV + (maxV - minV) * slicedBarHeight / 16.0f);
			tessellator.addVertexWithUV(barMaxX, slicedBarY - slicedBarHeight, zLevel, maxU, minV);
			tessellator.addVertexWithUV(barMinX, slicedBarY - slicedBarHeight, zLevel, minU, minV);
			tessellator.draw();
		}
	}	
}

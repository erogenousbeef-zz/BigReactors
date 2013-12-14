package erogenousbeef.bigreactors.gui.controls;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraftforge.common.ForgeDirection;
import cofh.api.energy.IEnergyHandler;
import erogenousbeef.bigreactors.client.gui.BeefGuiBase;
import erogenousbeef.bigreactors.gui.IBeefTooltipControl;

public class BeefGuiPowerBar extends BeefGuiVerticalProgressBar implements
		IBeefTooltipControl {

	IEnergyHandler _entity;
	
	protected double barLeftU = 0.5;
	protected double barRightU = 1;
	
	public BeefGuiPowerBar(BeefGuiBase container, int x, int y, IEnergyHandler entity) {
		super(container, x, y);
		_entity = entity;
	}

	@Override
	protected double getBackgroundLeftU() { return 0; }
	
	@Override
	protected double getBackgroundRightU() { return 0.499; }
	
	
	@Override
	protected String getBackgroundTexture() { return "controls/Energy.png"; }
	
	@Override
	protected float getProgress() {
		return (float)_entity.getEnergyStored(ForgeDirection.UNKNOWN) / (float)_entity.getMaxEnergyStored(ForgeDirection.UNKNOWN);
	}

	@Override
	public String getTooltip() {
		return String.format("%d / %d RF", _entity.getEnergyStored(ForgeDirection.UNKNOWN), _entity.getMaxEnergyStored(ForgeDirection.UNKNOWN));
	}

	@Override
	protected void drawProgressBar(Tessellator tessellator,
			TextureManager renderEngine, int barMinX, int barMaxX, int barMinY,
			int barMaxY, int zLevel) {

		double barHeight = (getProgress() * (this.height-2)) + 2;
		double barMaxV = 1;
		double barMinV = 1 - Math.min(1, Math.max(0, barHeight / this.height));
		
		renderEngine.bindTexture(controlResource);
		tessellator.startDrawingQuads();
		tessellator.addVertexWithUV(this.absoluteX, this.absoluteY + this.height, zLevel, barLeftU, barMaxV);
		tessellator.addVertexWithUV(this.absoluteX + this.width, this.absoluteY + this.height, zLevel, barRightU, barMaxV);
		tessellator.addVertexWithUV(this.absoluteX + this.width, this.absoluteY + this.height - barHeight, zLevel, barRightU, barMinV);
		tessellator.addVertexWithUV(this.absoluteX, this.absoluteY + this.height - barHeight, zLevel, barLeftU, barMinV);
		tessellator.draw();
	}
}

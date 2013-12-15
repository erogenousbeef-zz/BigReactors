package erogenousbeef.bigreactors.gui.controls;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import erogenousbeef.bigreactors.client.gui.BeefGuiBase;
import erogenousbeef.bigreactors.common.interfaces.IReactorFuelInfo;
import erogenousbeef.bigreactors.gui.IBeefTooltipControl;

public class BeefGuiFuelMixBar extends BeefGuiVerticalProgressBar implements
		IBeefTooltipControl {

	IReactorFuelInfo entity;

	protected double fuelLeftU = 0.25;
	protected double fuelRightU = 0.4999;
	protected double wasteLeftU = 0.5;
	protected double wasteRightU = 0.7499;
	
	public BeefGuiFuelMixBar(BeefGuiBase container, int x, int y, IReactorFuelInfo entity) {
		super(container, x, y);
		this.entity = entity;
	}

	@Override
	protected double getBackgroundLeftU() { return 0; }
	
	@Override
	protected double getBackgroundRightU() { return 0.2499; }

	@Override
	protected String getBackgroundTexture() { return "controls/FuelMixBar.png"; }

	private final static double maxV = 63.0/64.0;
	private final static double minV = 1.0/64.0;

	@Override
	protected void drawProgressBar(Tessellator tessellator,
			TextureManager renderEngine, int barMinX, int barMaxX, int barMinY,
			int barMaxY, int zLevel) {

		int barMaxHeight = this.height - 1;
		int barHeight = Math.max(1, Math.round(getProgress() * barMaxHeight));

		double fullness = (double)(entity.getFuelAmount() + entity.getWasteAmount()) / (double)entity.getCapacity();
		double fuelProportion = (double)entity.getFuelAmount() / (double)(entity.getFuelAmount() + entity.getWasteAmount());
		double wasteProportion = (double)entity.getWasteAmount() / (double)(entity.getFuelAmount() + entity.getWasteAmount());

		renderEngine.bindTexture(controlResource);
		if(fuelProportion > 0) {
			double fuelMinV = 1.0 - fullness*maxV;
			double fuelMaxV = maxV;
			tessellator.startDrawingQuads();
			tessellator.addVertexWithUV(this.absoluteX, this.absoluteY + this.height - 1, zLevel, fuelLeftU, fuelMaxV);
			tessellator.addVertexWithUV(this.absoluteX + this.width, this.absoluteY + this.height - 1, zLevel, fuelRightU, fuelMaxV);
			tessellator.addVertexWithUV(this.absoluteX + this.width, this.absoluteY + this.height - barHeight, zLevel, fuelRightU, fuelMinV);
			tessellator.addVertexWithUV(this.absoluteX, this.absoluteY + this.height - barHeight, zLevel, fuelLeftU, fuelMinV);
			tessellator.draw();
		}
		
		if(wasteProportion > 0) {
			double wasteMinV = 1.0 - fullness * wasteProportion * maxV;
			double wasteMaxV = maxV;
			double wasteHeight = Math.round(barHeight * wasteProportion);
			
			if(wasteHeight > 0) {
				double wasteTop = this.absoluteY + this.height - 1 - wasteHeight;
				
				tessellator.startDrawingQuads();
				tessellator.addVertexWithUV(this.absoluteX, this.absoluteY + this.height - 1, zLevel+1, wasteLeftU, wasteMaxV);
				tessellator.addVertexWithUV(this.absoluteX + this.width, this.absoluteY + this.height - 1, zLevel+1, wasteRightU, wasteMaxV);
				tessellator.addVertexWithUV(this.absoluteX + this.width, wasteTop, zLevel+1, wasteRightU, wasteMinV);
				tessellator.addVertexWithUV(this.absoluteX, wasteTop, zLevel+1, wasteLeftU, wasteMinV);
				tessellator.draw();
			}
		}
	}

	@Override
	public String[] getTooltip() {
		float fullness = getProgress() * 100f;
		float richness;
		if(entity.getFuelAmount() + entity.getWasteAmount() == 0) {
			richness = 0f;
		}
		else {
			richness = ((float)entity.getFuelAmount() / (float)(entity.getFuelAmount() + entity.getWasteAmount())) * 100f;
		}
		return new String[] {
				"Reactor Fuel Rods",
				String.format(" %2.1f%% full", fullness),
				String.format(" %2.1f%% enriched", richness)
		};
	}

	@Override
	protected float getProgress() {
		return (float)(entity.getFuelAmount() + entity.getWasteAmount()) / (float)entity.getCapacity();
	}	
}

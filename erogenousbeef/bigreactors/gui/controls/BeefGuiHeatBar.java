package erogenousbeef.bigreactors.gui.controls;

import erogenousbeef.bigreactors.api.IHeatEntity;
import erogenousbeef.bigreactors.client.gui.BeefGuiBase;
import erogenousbeef.bigreactors.gui.IBeefTooltipControl;

public class BeefGuiHeatBar extends BeefGuiTextureProgressBar implements
		IBeefTooltipControl {

	private static final float heatMax = 2000f;
	IHeatEntity entity;
	
	public BeefGuiHeatBar(BeefGuiBase container, int x, int y, IHeatEntity entity) {
		super(container, x, y);
		this.entity = entity;
	}

	@Override
	protected String getBackgroundTexture() { return "controls/HeatBar.png"; }
	
	@Override
	protected float getProgress() {
		return Math.min(1, Math.max(0, entity.getHeat() / heatMax));
	}

	@Override
	public String[] getTooltip() {
		return new String[] { "Reactor Heat", String.format("%d degrees C", (int)entity.getHeat()) };
	}
}

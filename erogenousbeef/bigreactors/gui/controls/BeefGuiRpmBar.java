package erogenousbeef.bigreactors.gui.controls;

import erogenousbeef.bigreactors.client.gui.BeefGuiBase;
import erogenousbeef.bigreactors.common.multiblock.MultiblockTurbine;
import erogenousbeef.bigreactors.gui.GuiConstants;
import erogenousbeef.bigreactors.gui.IBeefTooltipControl;

public class BeefGuiRpmBar extends BeefGuiTextureProgressBar implements
		IBeefTooltipControl {

	MultiblockTurbine turbine;
	String[] tooltip;
	
	public BeefGuiRpmBar(BeefGuiBase container, int x, int y, MultiblockTurbine turbine, String tooltipTitle, String[] extraTooltip) {
		super(container, x, y);
		tooltip = null;
		this.turbine = turbine;
		
		if(extraTooltip == null || extraTooltip.length <= 0) {
			tooltip = new String[2];
		}
		else {
			tooltip = new String[3 + extraTooltip.length];
			tooltip[2] = "";
			for(int i = 0; i < extraTooltip.length; i++) {
				tooltip[i+3] = extraTooltip[i];
			}
		}
		tooltip[0] = GuiConstants.LITECYAN_TEXT + tooltipTitle;
		tooltip[1] = "";
	}

	@Override
	protected String getBackgroundTexture() { return "controls/RpmBar.png"; }
	
	@Override
	public String[] getTooltip() {
		if(turbine != null) {
			tooltip[1] = String.format("  %.0f RPM", turbine.getRotorSpeed());
		}
		else {
			tooltip[1] = "  0 RPM";
		}

		return tooltip;
	}

	@Override
	protected float getProgress() {
		if(turbine == null) { return 0f; }
		
		return turbine.getRotorSpeed() / turbine.getMaxRotorSpeed();
	}

}

package erogenousbeef.bigreactors.gui;

import net.minecraft.client.gui.FontRenderer;

public interface IBeefTooltipControl {
	boolean isMouseOver(int mouseX, int mouseY);
	String getTooltip();
}

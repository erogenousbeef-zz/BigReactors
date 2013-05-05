package erogenousbeef.bigreactors.gui;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderEngine;

public interface IBeefTooltipControl {
	boolean isMouseOver(int mouseX, int mouseY);
	void renderTooltip(RenderEngine renderer, FontRenderer fontRenderer, int mouseX, int mouseY);
}

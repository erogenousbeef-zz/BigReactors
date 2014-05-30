package erogenousbeef.bigreactors.gui;

import net.minecraft.client.gui.FontRenderer;

public interface IBeefListBoxEntry {

	public int getHeight();
	
	public void draw(FontRenderer fontRenderer, int x, int y, int backgroundColor, int foregroundColor);
	
}

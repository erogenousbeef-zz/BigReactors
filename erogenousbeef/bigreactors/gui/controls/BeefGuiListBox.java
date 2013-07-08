package erogenousbeef.bigreactors.gui.controls;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.RenderEngine;
import net.minecraft.client.renderer.Tessellator;

import erogenousbeef.bigreactors.client.gui.BeefGuiBase;
import erogenousbeef.bigreactors.gui.BeefGuiControlBase;
import erogenousbeef.bigreactors.gui.IBeefListBoxEntry;

public class BeefGuiListBox extends BeefGuiControlBase {

	protected int borderColor = 0xffaaaaaa;
	protected int backgroundColor = 0xff000000;
	protected int selectedLineColor = 0xff000000;
	protected int textColor = 0xffcdcdcd;
	protected int selectedTextColor = 0xffffffff;
	
	private List<IBeefListBoxEntry> entries;
	
	private int displayTop;
	private int selectedEntryIdx;
	
	private static final int NO_ENTRY = -1;
	private static final int margin = 2;
	
	public BeefGuiListBox(BeefGuiBase container, int x, int y, int width,
			int height) {
		super(container, x, y, width, height);
		
		entries = new ArrayList<IBeefListBoxEntry>();
		displayTop = 0;
		selectedEntryIdx = NO_ENTRY;
	}

	public void add(IBeefListBoxEntry entry) {
		entries.add(entry);
	}
	
	public void remove(IBeefListBoxEntry entry) {
		entries.remove(entry);
	}
	
	
	
	@Override
	public void drawBackground(RenderEngine renderEngine, int mouseX, int mouseY) {
		drawRect(x, y, x+width, y+height, borderColor);
		drawRect(x+margin, y+margin, x+width-margin*2, y+height-margin*2, backgroundColor);
	}

	@Override
	public void drawForeground(RenderEngine renderEngine, int mouseX, int mouseY) {
		int drawnY = 0;
		IBeefListBoxEntry entry;
		for(int i = displayTop; i < entries.size(); i++) {
			entry = entries.get(i);
			if(entry.getHeight() + drawnY > this.height) { break; }
			else {
				if(this.selectedEntryIdx == i) {
					entry.draw(this.guiContainer.getFontRenderer(), x+margin, y+margin+drawnY, selectedLineColor, selectedTextColor);
				}
				else {
					entry.draw(this.guiContainer.getFontRenderer(), x+margin, y+margin+drawnY, backgroundColor, textColor);
				}
			}
			if(drawnY >= this.height) { break; }
		}
	}

	@Override
	public void onMouseClicked(int mouseX, int mouseY, int mouseButton) {
		int checkedY = 0;
		for(int i = displayTop; i < entries.size(); i++) {
			int entryHeight = entries.get(i).getHeight();
			if(checkedY + entryHeight > (this.height-margin*2)) {
				break;
			}
			
			if(this.y + margin + checkedY <= mouseY && this.y + margin + entryHeight + checkedY >= mouseY) {
				setSelectedIndex(i);
				onEntryClicked(entries.get(i));
				break;
			}
			
			checkedY += entryHeight;
		}
	}
	
	protected void setSelectedIndex(int newSelectedIdx) {
		if(newSelectedIdx < 0 || newSelectedIdx >= this.entries.size()) { return; }
		if(this.selectedEntryIdx == newSelectedIdx) { return; }
		this.selectedEntryIdx = newSelectedIdx;
		this.guiContainer.onListBoxSelectionChanged(this, entries.get(this.selectedEntryIdx));
	}
	
	public void onEntryClicked(IBeefListBoxEntry entry) {
		this.guiContainer.onListBoxEntryClicked(this, entry);
	}
	
}

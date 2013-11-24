package erogenousbeef.bigreactors.gui.controls;

import net.minecraft.client.gui.FontRenderer;
import erogenousbeef.bigreactors.common.tileentity.TileEntityReactorRedNetPort;
import erogenousbeef.bigreactors.gui.IBeefListBoxEntry;

public class BeefGuiRedNetConfigListEntry implements IBeefListBoxEntry {

	private String displayName;
	private TileEntityReactorRedNetPort.CircuitType circuitType;
	
	public BeefGuiRedNetConfigListEntry(TileEntityReactorRedNetPort.CircuitType circuitType, String displayName) {
		this.circuitType = circuitType;
		this.displayName = displayName;
	}
	
	@Override
	public int getHeight() {
		return 20;
	}

	@Override
	public void draw(FontRenderer fontRenderer, int x, int y, int backgroundColor, int foregroundColor) {
		// TODO: Draw background color bit
		fontRenderer.drawString(this.displayName, x, y, foregroundColor);
	}
	
	public TileEntityReactorRedNetPort.CircuitType getCircuitType() { return circuitType; }
}

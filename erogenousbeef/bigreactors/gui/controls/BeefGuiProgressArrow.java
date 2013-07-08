package erogenousbeef.bigreactors.gui.controls;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderEngine;
import erogenousbeef.bigreactors.client.gui.BeefGuiBase;
import erogenousbeef.bigreactors.common.tileentity.base.TileEntityPoweredInventory;
import erogenousbeef.bigreactors.gui.BeefGuiControlBase;

public class BeefGuiProgressArrow extends BeefGuiControlBase {

	TileEntityPoweredInventory entity;
	private int arrowU;
	private int arrowV;
	
	public BeefGuiProgressArrow(BeefGuiBase container, int x, int y, int arrowU, int arrowV, TileEntityPoweredInventory entity) {
		super(container, x, y, 25, 16);
		this.arrowU = arrowU;
		this.arrowV = arrowV;
		this.entity = entity;
	}

	@Override
	public void drawBackground(RenderEngine renderEngine, int mouseX, int mouseY) {
		if(entity.getCycleCompletion() > 0.0) {
			int progressWidth = (int)(entity.getCycleCompletion() * (float)(this.width-1));
			renderEngine.bindTexture(this.guiContainer.getGuiBackground());
			guiContainer.drawTexturedModalRect(this.x, this.y, arrowU, arrowV, 1+progressWidth, this.height);
		}
	}

	@Override
	public void drawForeground(RenderEngine renderEngine, int mouseX, int mouseY) {
	}

}

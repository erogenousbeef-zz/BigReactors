package erogenousbeef.bigreactors.gui.controls;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import erogenousbeef.bigreactors.client.gui.BeefGuiBase;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.gui.BeefGuiControlBase;
import erogenousbeef.bigreactors.gui.IBeefTooltipControl;

public class BeefGuiInsertionProgressBar extends BeefGuiControlBase implements IBeefTooltipControl {

	public final static int controlWidth = 20;
	public final static int controlHeight = 64;

	protected ResourceLocation controlResource;

	private double backgroundLeftU = 0;
	private double backgroundRightU = 0.5;
	
	private double rodLeftU = 0.51;
	private double rodRightU = 1;
	
	protected float barAbsoluteMaxHeight;
	protected float insertion = 0f;

	protected String[] tooltip = {
			EnumChatFormatting.AQUA + "Control Rod",
			"",
			"Insertion: XX%"
	};
	
	public BeefGuiInsertionProgressBar(BeefGuiBase container, int x, int y) {
		super(container, x, y, controlWidth, controlHeight);
		
		controlResource = new ResourceLocation(BigReactors.GUI_DIRECTORY + getBackgroundTexture());
		barAbsoluteMaxHeight = this.height - 1;
	}
	
	public void setInsertion(float insertion) { this.insertion = Math.min(1f, Math.max(0f, insertion)); }
	
	protected String getBackgroundTexture() { return "controls/ControlRod.png"; }
	
	@Override
	public void drawBackground(TextureManager renderEngine, int mouseX, int mouseY) {
		if(!this.visible) { return; }

		// Draw the background
		GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		renderEngine.bindTexture(controlResource);
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		tessellator.addVertexWithUV(this.absoluteX, this.absoluteY + this.height, 0, backgroundLeftU, 1.0);
		tessellator.addVertexWithUV(this.absoluteX + this.width, this.absoluteY + this.height, 0, backgroundRightU, 1.0);
		tessellator.addVertexWithUV(this.absoluteX + this.width, this.absoluteY, 0, backgroundRightU, 0);
		tessellator.addVertexWithUV(this.absoluteX, this.absoluteY, 0, backgroundLeftU, 0);
		tessellator.draw();
		
		// Draw the rod itself, on top of the background
		if(insertion > 0f) {
			int barHeight = Math.max(1, (int)Math.floor(insertion * barAbsoluteMaxHeight));
			int rodMaxY = this.absoluteY + barHeight;
			
			float rodTopV = 1f - insertion; // TODO
			
			tessellator.startDrawingQuads();
			tessellator.addVertexWithUV(this.absoluteX, rodMaxY, 2, rodLeftU, 1f);
			tessellator.addVertexWithUV(this.absoluteX + this.width, rodMaxY, 2, rodRightU, 1f);
			tessellator.addVertexWithUV(this.absoluteX + this.width, this.absoluteY, 2, rodRightU, rodTopV);
			tessellator.addVertexWithUV(this.absoluteX, this.absoluteY, 2, rodLeftU, rodTopV);
			tessellator.draw();
		}
	}

	@Override
	public void drawForeground(TextureManager renderEngine, int mouseX,
			int mouseY) {
	}

	@Override
	public String[] getTooltip() {
		tooltip[2] = String.format("Insertion: %.0f%%", this.insertion*100f);
		return tooltip;
	}

}

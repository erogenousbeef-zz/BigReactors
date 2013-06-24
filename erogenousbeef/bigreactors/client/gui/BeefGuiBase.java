package erogenousbeef.bigreactors.client.gui;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import erogenousbeef.bigreactors.gui.IBeefGuiControl;
import erogenousbeef.bigreactors.gui.IBeefTooltipControl;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;

@SideOnly(Side.CLIENT)
public abstract class BeefGuiBase extends GuiContainer {

	protected List<IBeefGuiControl> controls;
	protected List<IBeefTooltipControl> controlsWithTooltips;
	protected List<GuiTextField> textFields;
	
	public BeefGuiBase(Container container) {
		super(container);
		
		controls = new ArrayList<IBeefGuiControl>();
		controlsWithTooltips = new ArrayList<IBeefTooltipControl>();
		textFields = new ArrayList<GuiTextField>();
	}

	public void registerControl(GuiTextField newTextField) {
		textFields.add(newTextField);
	}
	
	public void registerControl(IBeefGuiControl newControl) {
		controls.add(newControl);
		
		if(newControl instanceof IBeefTooltipControl) {
			controlsWithTooltips.add((IBeefTooltipControl) newControl);
		}
	}

	public FontRenderer getFontRenderer() { return this.fontRenderer; }
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float gameTicks, int mouseX, int mouseY) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.renderEngine.bindTexture(getGuiBackground());
		this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		
		int relativeX, relativeY;
		relativeX = mouseX - this.guiLeft;
		relativeY = mouseY - this.guiTop;
		for(IBeefGuiControl c : controls) {
			c.drawBackground(relativeX, relativeY);
		}
		
		for(GuiTextField field : textFields) {
			field.drawTextBox();
		}
	}
	
	// Override to draw your custom controls
	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		int relativeX, relativeY;
		relativeX = mouseX - this.guiLeft;
		relativeY = mouseY - this.guiTop;
		for(IBeefGuiControl c : controls) {
			c.drawForeground(relativeX, relativeY);
		}

		for(IBeefTooltipControl tc: controlsWithTooltips) {
			if(tc.isMouseOver(mouseX,  mouseY)) {
				String tooltip = tc.getTooltip();
				if(tooltip != null && !tooltip.equals("")) {
					drawCreativeTabHoveringText(tooltip, relativeX, relativeY);
					break;
				}
			}
		}
	}
	
	@Override
	protected void mouseClicked(int x, int y, int buttonIndex) {
		super.mouseClicked(x, y, buttonIndex);
		for(GuiTextField field : textFields) {
			field.mouseClicked(x, y, buttonIndex);
		}
		
		for(IBeefGuiControl c: controls) {
			c.onMouseClicked(x, y, buttonIndex);
		}
	}
	
	
	public abstract String getGuiBackground();
	
	public int getGuiLeft() { return guiLeft; }
	public int getGuiTop() { return guiTop; }
}

package erogenousbeef.bigreactors.client.gui;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import erogenousbeef.bigreactors.gui.IBeefGuiControl;
import erogenousbeef.bigreactors.gui.IBeefListBoxEntry;
import erogenousbeef.bigreactors.gui.IBeefTooltipControl;
import erogenousbeef.bigreactors.gui.controls.BeefGuiListBox;
import erogenousbeef.bigreactors.gui.controls.grab.BeefGuiGrabSource;
import erogenousbeef.bigreactors.gui.controls.grab.IBeefGuiGrabbable;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.util.Icon;

@SideOnly(Side.CLIENT)
public abstract class BeefGuiBase extends GuiContainer {

	protected List<IBeefGuiControl> controls;
	protected List<IBeefTooltipControl> controlsWithTooltips;
	protected List<GuiTextField> textFields;
	
	protected IBeefGuiGrabbable grabbedItem;
	
	public BeefGuiBase(Container container) {
		super(container);
		
		controls = new ArrayList<IBeefGuiControl>();
		controlsWithTooltips = new ArrayList<IBeefTooltipControl>();
		textFields = new ArrayList<GuiTextField>();
		
		grabbedItem = null;
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
			c.drawBackground(this.mc.renderEngine, relativeX, relativeY);
		}
		
		for(GuiTextField field : textFields) {
			field.drawTextBox();
		}
	}
	
	// Override to draw your custom controls
	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		int absoluteX, absoluteY;
		absoluteX = mouseX - this.guiLeft;
		absoluteY = mouseY - this.guiTop;
		for(IBeefGuiControl c : controls) {
			c.drawForeground(this.mc.renderEngine, absoluteX, absoluteY);
		}

		for(IBeefTooltipControl tc: controlsWithTooltips) {
			if(tc.isMouseOver(absoluteX,  absoluteY)) {
				String tooltip = tc.getTooltip();
				if(tooltip != null && !tooltip.equals("")) {
					drawCreativeTabHoveringText(tooltip, absoluteX, absoluteY);
					break;
				}
			}
		}
		
		if(this.grabbedItem != null) {
			// Render grabbed item next to mouse
            this.mc.renderEngine.bindTexture("/terrain.png");
            GL11.glColor4f(1f, 1f, 1f, 1f);
            this.drawTexturedModelRectFromIcon(absoluteX+1, absoluteY+1, this.grabbedItem.getIcon(), 16, 16);
		}
	}
	
	@Override
	protected void mouseClicked(int x, int y, int buttonIndex) {
		int absoluteX, absoluteY;
		absoluteX = x - this.guiLeft;
		absoluteY = y - this.guiTop;
		
		super.mouseClicked(x, y, buttonIndex);
		for(GuiTextField field : textFields) {
			field.mouseClicked(x, y, buttonIndex);
		}
		
		for(IBeefGuiControl c: controls) {
			c.onMouseClicked(absoluteX, absoluteY, buttonIndex);
		}
	}
	
	public abstract String getGuiBackground();
	
	public int getGuiLeft() { return guiLeft; }
	public int getGuiTop() { return guiTop; }
	
	public void onListBoxSelectionChanged(BeefGuiListBox listBox, IBeefListBoxEntry selectedEntry) {}
	public void onListBoxEntryClicked(BeefGuiListBox listBox, IBeefListBoxEntry clickedEntry) {}

	public void setGrabbedItem(IBeefGuiGrabbable grabbedSource) {
		this.grabbedItem = grabbedSource;
	}
	
	public IBeefGuiGrabbable getGrabbedItem() {
		return this.grabbedItem;
	}
	
	public void onControlClicked(IBeefGuiControl control) {}
}

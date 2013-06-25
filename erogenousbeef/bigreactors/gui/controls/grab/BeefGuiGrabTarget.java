package erogenousbeef.bigreactors.gui.controls.grab;

import net.minecraft.util.Icon;
import erogenousbeef.bigreactors.client.gui.BeefGuiBase;
import erogenousbeef.bigreactors.gui.BeefGuiControlBase;

public abstract class BeefGuiGrabTarget extends BeefGuiControlBase {

	protected static final int hoverColor = 0x33ffffff; // 20% opacity white
	protected static final int invalidHoverColor = 0x33ff9999; // 20% opacity red
	
	protected IBeefGuiGrabbable grabbable;
	
	
	protected BeefGuiGrabTarget(BeefGuiBase container, int x, int y) {
		super(container, x, y, 16, 16);
		grabbable = null;
	}

	@Override
	public void drawForeground(int mouseX, int mouseY) {
		if(grabbable != null) {
			this.guiContainer.drawTexturedModelRectFromIcon(x, y, grabbable.getIcon(), width, height);
		}
		
		if(this.isMouseOver(mouseX, mouseY)) {
			if(this.guiContainer.getGrabbedItem() != null && isAcceptedGrab(this.guiContainer.getGrabbedItem())) {
				this.drawRect(this.x, this.y, this.x+this.width, this.y+this.height, invalidHoverColor);
			}
			else {
				this.drawRect(this.x, this.y, this.x+this.width, this.y+this.height, hoverColor);
			}
		}
	}

	@Override
	public void drawBackground(int mouseX, int mouseY) {
	}
	
	@Override
	public void onMouseClicked(int mouseX, int mouseY, int buttonIndex) {
		if(isMouseOver(mouseX, mouseY)) {
			if(buttonIndex == 0) {
				setSlotContents(this.guiContainer.getGrabbedItem());
				this.guiContainer.setGrabbedItem(null);
			}
			else {
				clearSlot();
			}
		}
	}
	
	private void clearSlot() {
		this.grabbable = null;
		this.onSlotCleared();
	}

	public void setSlotContents(IBeefGuiGrabbable grabbedItem) {
		if(grabbedItem == null) {
			this.clearSlot();
		}
		else {
			if(this.grabbable != grabbedItem) {
				this.grabbable = grabbedItem;
				this.onSlotSet();
			}
		}
	}
	
	/**
	 * Called when the grab-object is cleared from this slot, 
	 * either by right-clicking or left-clicking with nothing selected.
	 */
	public abstract void onSlotCleared();
	
	/**
	 * Called when the grab-object is set or changed in this slot.
	 */
	public abstract void onSlotSet();

	/**
	 * Called when we're trying to determine if the grabbable item is
	 * compatible with this target. Return true if the user can "drop"
	 * the item here.
	 * Null is always accepted. You do not have to check for it.
	 * @param grabbedItem An IBeefGuiGrabbable that has been grabbed by the user.
	 * @return True if the user can "drop" the Grabbable here, false otherwise.
	 */
	public abstract boolean isAcceptedGrab(IBeefGuiGrabbable grabbedItem);	
}

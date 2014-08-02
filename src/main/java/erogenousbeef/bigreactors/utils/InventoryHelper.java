package erogenousbeef.bigreactors.utils;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

// Strongly based off Powercrystals' InventoryManager
public class InventoryHelper {
	private IInventory inventory;

	public InventoryHelper(IInventory inventory) {
		this.inventory = inventory;
	}

	protected boolean canAdd(ItemStack stack, int slot) {
		if(inventory == null) { return false; }
		return inventory.isItemValidForSlot(slot, stack);
	}

	protected boolean canRemove(ItemStack stack, int slot) {
		return inventory != null;
	}

	/**
	 * Add an item to a wrapped inventory
	 * 
	 * @param stack
	 *            Item stack to place into the wrapped inventory
	 * @return Stack representing the remaining items
	 */
	public ItemStack addItem(ItemStack stack) {
		if (stack == null) {
			return null;
		}

		int quantitytoadd = stack.stackSize;
		ItemStack remaining = stack.copy();
		int[] candidates = getSlots();
		
		if(candidates.length == 0) {
			return stack;
		}

		for (int candidateSlot : candidates) {
			int maxStackSize = Math.min(inventory.getInventoryStackLimit(),
					stack.getMaxStackSize());
			ItemStack s = inventory.getStackInSlot(candidateSlot);
			if (s == null) {
				ItemStack add = stack.copy();
				add.stackSize = Math.min(quantitytoadd, maxStackSize);

				if (canAdd(add, candidateSlot)) {
					quantitytoadd -= add.stackSize;
					inventory.setInventorySlotContents(candidateSlot, add);
					inventory.markDirty();
				}
			} else if (StaticUtils.Inventory.areStacksEqual(s, stack)) {
				ItemStack add = stack.copy();
				add.stackSize = Math.min(quantitytoadd, maxStackSize
						- s.stackSize);

				if (add.stackSize > 0 && canAdd(add, candidateSlot)) {
					s.stackSize += add.stackSize;
					quantitytoadd -= add.stackSize;
					inventory.setInventorySlotContents(candidateSlot, s);
					inventory.markDirty();
				}
			}
			if (quantitytoadd == 0) {
				break;
			}
		}

		remaining.stackSize = quantitytoadd;
		if (remaining.stackSize == 0) {
			return null;
		} else {
			return remaining;
		}
	}

	private final static int[] noSlots = new int[0];
	protected int[] getSlots() {
		if(inventory == null) { return noSlots; }
		int[] slots = new int[inventory.getSizeInventory()];
		for (int i = 0; i < slots.length; i++) {
			slots[i] = i;
		}
		return slots;
	}
}

package erogenousbeef.bigreactors.utils;

import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeDirection;

public class SidedInventoryHelper extends InventoryHelper {

	private ISidedInventory sidedInventory;
	private ForgeDirection side;
	
	public SidedInventoryHelper(ISidedInventory inventory, ForgeDirection side) {
		super(inventory);
		
		this.sidedInventory = inventory;
		this.side = side;
	}
	
	@Override
	protected boolean canAdd(ItemStack stack, int slot) {
		return sidedInventory.canInsertItem(slot, stack, this.side.ordinal());
	}
	
	@Override
	protected boolean canRemove(ItemStack stack, int slot) {
		return sidedInventory.canExtractItem(slot, stack, this.side.ordinal());
	}
	
	@Override
	public int[] getSlots() {
		return sidedInventory.getAccessibleSlotsFromSide(this.side.ordinal());
	}


}

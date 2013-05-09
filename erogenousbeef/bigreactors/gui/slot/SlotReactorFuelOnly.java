package erogenousbeef.bigreactors.gui.slot;

import erogenousbeef.bigreactors.common.BigReactors;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotReactorFuelOnly extends Slot {

	public SlotReactorFuelOnly(IInventory par1iInventory, int par2, int par3,
			int par4) {
		super(par1iInventory, par2, par3, par4);
	}

	@Override
	public boolean isItemValid(ItemStack stack) {
		return stack.itemID == BigReactors.ingotGeneric.itemID;
	}
}

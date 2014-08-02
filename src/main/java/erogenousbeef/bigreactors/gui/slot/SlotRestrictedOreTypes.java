package erogenousbeef.bigreactors.gui.slot;

import java.util.ArrayList;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class SlotRestrictedOreTypes extends Slot {

	protected String[] acceptedTypes;
	
	public SlotRestrictedOreTypes(IInventory par1iInventory, int par2, int par3,
			int par4, String[] acceptedOreDictionaryNames) {
		super(par1iInventory, par2, par3, par4);
		
		acceptedTypes = acceptedOreDictionaryNames.clone();
	}

	@Override
	public boolean isItemValid(ItemStack stack) {
		ArrayList<ItemStack> candidates;
		for(String acceptedType : acceptedTypes) {
			candidates = OreDictionary.getOres(acceptedType);
			for(ItemStack candidate : candidates) {
				if(stack.isItemEqual(candidate)) {
					return true;
				}
			}
		}

		return false;
	}
}

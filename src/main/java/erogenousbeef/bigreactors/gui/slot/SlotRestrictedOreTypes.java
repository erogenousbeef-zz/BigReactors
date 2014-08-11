package erogenousbeef.bigreactors.gui.slot;

import java.util.ArrayList;
import java.util.List;

import scala.actors.threadpool.Arrays;

import cofh.core.util.oredict.OreDictionaryArbiter;
import cofh.util.ItemHelper;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotRestrictedOreTypes extends Slot {

	protected List<String> acceptedTypes;
	
	public SlotRestrictedOreTypes(IInventory par1iInventory, int par2, int par3,
			int par4, String[] acceptedOreDictionaryNames) {
		super(par1iInventory, par2, par3, par4);
		
		acceptedTypes = new ArrayList<String>(Arrays.asList(acceptedOreDictionaryNames));
	}

	@Override
	public boolean isItemValid(ItemStack stack) {
		if(stack == null) { return false; }

		ArrayList<String> oreNames = OreDictionaryArbiter.getAllOreNames(stack);
		if(oreNames != null) {
			for(String oreName : oreNames) {
				if(acceptedTypes.contains(oreName)) {
					return true;
				}
			}
		}

		return false;
	}
}

package erogenousbeef.bigreactors.gui.slot;

import java.util.ArrayList;

import erogenousbeef.bigreactors.api.IReactorFuel;
import erogenousbeef.bigreactors.common.BRRegistry;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class SlotReactorInput extends Slot {

	boolean fuel = true;
	
	public SlotReactorInput(IInventory par1iInventory, int par2, int par3,
			int par4, boolean fuel) {
		super(par1iInventory, par2, par3, par4);
		this.fuel = fuel;
	}

	@Override
	public boolean isItemValid(ItemStack stack) {
		IReactorFuel data = BRRegistry.getDataForSolid(stack);
		if(data != null) {
			if(fuel)
				return data.isFuel();
			else
				return data.isWaste();
		}
		return false;
	}
}

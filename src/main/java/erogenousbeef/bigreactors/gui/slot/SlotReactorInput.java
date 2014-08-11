package erogenousbeef.bigreactors.gui.slot;

import java.util.ArrayList;

import cofh.core.util.oredict.OreDictionaryArbiter;
import cofh.lib.util.helpers.ItemHelper;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import erogenousbeef.bigreactors.api.registry.Reactants;
import erogenousbeef.bigreactors.common.BRLog;

public class SlotReactorInput extends Slot {

	boolean fuel = true;
	
	public SlotReactorInput(IInventory par1iInventory, int par2, int par3,
			int par4, boolean fuel) {
		super(par1iInventory, par2, par3, par4);
		this.fuel = fuel;
	}

	@Override
	public boolean isItemValid(ItemStack stack) {
		if(stack == null) { return false; }
		
		if(fuel) {
			return Reactants.isFuel(stack);
		}
		else {
			return Reactants.isWaste(stack);
		}
	}
}

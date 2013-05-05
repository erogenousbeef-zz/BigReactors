package erogenousbeef.bigreactors.api;

import net.minecraft.item.ItemStack;
import net.minecraftforge.liquids.LiquidStack;

public interface IReactorFuelLiquid {
	int getBlendColor();
	LiquidStack getReferenceLiquidStack();
	ItemStack getReferenceItemStack();
}

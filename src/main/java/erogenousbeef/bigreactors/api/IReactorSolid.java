package erogenousbeef.bigreactors.api;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

/**
 * A specialized interface that allows solid ItemStacks to be used as reactor fuels.
 * In order for this to work, you must select an equivalent Fluid fuel.
 * 
 * All solid fuels are "magically" converted into Fluids when they enter the
 * reactor system. 
 * @author Erogenous Beef
 */
public interface IReactorSolid {
	/**
	 * Get the solid itemstack to which this solid fuel maps.
	 */
	public ItemStack getReferenceItem();

	/**
	 * Get the fluid to which this solid fuel maps.
	 */
	public FluidStack getReferenceFluid();

	// In this case, both the reference item and reference fluid must match!
	/**
	 * @param otherFuel The IReactorSolid to compare.
	 * @return True if the other fuel's reference item and reference fluid both match
	 */
	public boolean isEqual(IReactorSolid otherFuel);

	/**
	 * @param otherItem An ItemStack to check for equality.
	 * @return True if the fuel's reference item is the same as the argument.
	 */
	public boolean isItemEqual(ItemStack otherItem);

	/**
	 * @param otherFluid A Fluid to check for equality.
	 * @return True if the fuel's reference fluid is the same as the argument.
	 */
	public boolean isFluidEqual(Fluid otherFluid);
}

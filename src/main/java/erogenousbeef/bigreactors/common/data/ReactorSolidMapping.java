package erogenousbeef.bigreactors.common.data;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public class ReactorSolidMapping {

	protected ItemStack referenceItem;
	protected FluidStack referenceFluid;
	
	public ReactorSolidMapping(ItemStack item, FluidStack fluid) {
		referenceItem = item.copy();
		referenceFluid = fluid;
	}

	public ItemStack getReferenceItem() {
		return referenceItem.copy();
	}

	public boolean isEqual(ReactorSolidMapping otherFuel) {
		return isFluidEqual(otherFuel.getReferenceFluid()) && isItemEqual(otherFuel.getReferenceItem());
	}

	public boolean isItemEqual(ItemStack otherItem) {
		return referenceItem.isItemEqual(otherItem);
	}
	
	@Override
	public boolean equals(Object other) {
		if(other instanceof ReactorSolidMapping) {
			return isEqual((ReactorSolidMapping)other);
		}
		else if(other instanceof FluidStack) {
			return isFluidEqual((FluidStack)other);
		}
		else if(other instanceof ItemStack) {
			return isItemEqual((ItemStack)other);
		}
		else {
			// Standard IReactorFuels cannot be equal, as they do not contain solid data
			return false;
		}
	}

	public FluidStack getReferenceFluid() {
		return referenceFluid;
	}

	public boolean isFluidEqual(FluidStack otherFluid) {
		return referenceFluid.isFluidEqual(otherFluid);
	}
}

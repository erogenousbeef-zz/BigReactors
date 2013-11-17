package erogenousbeef.bigreactors.common;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import erogenousbeef.bigreactors.api.IReactorFuel;
import erogenousbeef.bigreactors.api.IReactorSolid;

public class ReactorSolidMapping implements IReactorSolid {

	protected ItemStack referenceItem;
	protected Fluid referenceFluid;
	
	public ReactorSolidMapping(ItemStack item, Fluid fluid) {
		referenceItem = item.copy();
		referenceFluid = fluid;
	}

	@Override
	public ItemStack getReferenceItem() {
		return referenceItem.copy();
	}

	@Override
	public boolean isEqual(IReactorSolid otherFuel) {
		return isFluidEqual(otherFuel.getReferenceFluid()) && isItemEqual(otherFuel.getReferenceItem());
	}

	@Override
	public boolean isItemEqual(ItemStack otherItem) {
		return referenceItem.isItemEqual(otherItem);
	}
	
	@Override
	public boolean equals(Object other) {
		if(other instanceof IReactorSolid) {
			return isEqual((IReactorSolid)other);
		}
		else if(other instanceof Fluid) {
			return isFluidEqual((Fluid)other);
		}
		else if(other instanceof ItemStack) {
			return isItemEqual((ItemStack)other);
		}
		else {
			// Standard IReactorFuels cannot be equal, as they do not contain solid data
			return false;
		}
	}

	@Override
	public Fluid getReferenceFluid() {
		return referenceFluid;
	}

	@Override
	public boolean isFluidEqual(Fluid otherFluid) {
		return referenceFluid.getID() == otherFluid.getID();
	}
}

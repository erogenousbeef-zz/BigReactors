package erogenousbeef.bigreactors.common;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import erogenousbeef.bigreactors.api.IReactorFuel;

public class ReactorFuel implements IReactorFuel {
	protected ItemStack referenceItem;
	protected int color;
	
	public ReactorFuel(Item item, int color) {
		this.referenceItem = new ItemStack(item);
		this.color = color;
	}
	
	public ReactorFuel(ItemStack itemStack, int color) {
		this.referenceItem = itemStack;
		this.color = color;
	}
	
	@Override
	public boolean isFuelEqual(IReactorFuel otherFuel) {
		return referenceItem.isItemEqual(otherFuel.getReferenceItem());
	}

	@Override
	public boolean isFuelEqual(ItemStack item) {
		return item.isItemEqual(referenceItem);
	}

	@Override
	public ItemStack getReferenceItem() {
		return referenceItem;
	}

	@Override
	public int getFuelColor() {
		return color;
	}

	@Override
	public boolean equals(Object arg0) {
		if(arg0 instanceof IReactorFuel) {
			return this.referenceItem.isItemEqual(((IReactorFuel)arg0).getReferenceItem());
		}
		else if(arg0 instanceof ItemStack) {
			return this.referenceItem.isItemEqual((ItemStack)arg0);
		}

		return false;
	}
}

package erogenousbeef.bigreactors.common.item;

import erogenousbeef.bigreactors.common.BigReactors;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemBlockYelloriumFuelRod extends ItemBlock {
	public ItemBlockYelloriumFuelRod(int id)
	{
		super(id);
		this.setMaxDamage(0);
	}

	@Override
	public String getUnlocalizedName(ItemStack itemstack)
	{
		return getUnlocalizedName();
	}

	@Override
	public String getUnlocalizedName()
	{
		return BigReactors.blockYelloriumFuelRod.getUnlocalizedName() + ".0";
	}
}

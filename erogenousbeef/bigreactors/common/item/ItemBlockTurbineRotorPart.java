package erogenousbeef.bigreactors.common.item;

import erogenousbeef.bigreactors.common.BigReactors;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemBlockTurbineRotorPart extends ItemBlock {

	public ItemBlockTurbineRotorPart(int id) {
		super(id);
		this.setMaxDamage(0);
		this.setHasSubtypes(true);
	}

	@Override
	public int getMetadata(int damage)
	{
		return damage;
	}
	
	@Override
	public String getUnlocalizedName(ItemStack itemstack)
	{
		int damage = itemstack.getItemDamage();
		return BigReactors.blockTurbineRotorPart.getUnlocalizedName() + "." + damage;
	}

	@Override
	public String getUnlocalizedName()
	{
		return BigReactors.blockTurbineRotorPart.getUnlocalizedName() + ".0";
	}
}

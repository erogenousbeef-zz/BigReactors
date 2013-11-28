package erogenousbeef.bigreactors.common.item;

import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import erogenousbeef.bigreactors.common.BigReactors;

public class ItemBlockSmallMachine extends ItemBlock {
	public ItemBlockSmallMachine(int id)
	{
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
		return BigReactors.blockSmallMachine.getUnlocalizedName() + "." + damage;
	}

	@Override
	public String getUnlocalizedName()
	{
		return BigReactors.blockSmallMachine.getUnlocalizedName() + ".0";
	}	
}

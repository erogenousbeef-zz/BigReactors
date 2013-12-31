package erogenousbeef.bigreactors.common.item;

import erogenousbeef.bigreactors.common.BigReactors;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemBlockTurbinePart extends ItemBlock {

	public ItemBlockTurbinePart(int id) {
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
		return BigReactors.blockTurbinePart.getUnlocalizedName() + "." + damage;
	}

	@Override
	public String getUnlocalizedName()
	{
		return BigReactors.blockTurbinePart.getUnlocalizedName() + ".0";
	}
}

package erogenousbeef.bigreactors.common.item;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

public class ItemIngot extends ItemBase
{
	public static final String[] TYPES = { "ingotYellorium", "ingotDepletedYellorium" };

	public ItemIngot(int id)
	{
		super("ingot", id);
		this.setHasSubtypes(true);
		this.setMaxDamage(0);
	}

	@Override
	public int getMetadata(int damage)
	{
		return damage;
	}

	@Override
	public String getUnlocalizedName(ItemStack itemStack)
	{
		return "item." + TYPES[itemStack.getItemDamage()];
	}

	@Override
	public void getSubItems(int par1, CreativeTabs creativeTabs, List list)
	{
		for (int i = 0; i < TYPES.length; i++)
		{
			list.add(new ItemStack(this, 1, i));
		}
	}

	public static boolean isFuel(int itemDamage) {
		return itemDamage == 0;
	}

	public static boolean isWaste(int itemDamage) {
		return itemDamage == 1;
	}
}

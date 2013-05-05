package erogenousbeef.bigreactors.common;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

public class CreativeTabBR extends net.minecraft.creativetab.CreativeTabs {

	public CreativeTabBR(int par1, String par2Str)
	{
		super(par1, par2Str);
	}

	public ItemStack getIconItemStack()
	{
		return new ItemStack(BigReactors.blockYelloriteOre, 1, 0);
	}
}

package erogenousbeef.bigreactors.common.item;

import java.util.ArrayList;
import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import erogenousbeef.bigreactors.common.BigReactors;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;

public class ItemBase extends Item {
	protected final List<Icon> icons = new ArrayList<Icon>();

	public ItemBase(String name, int id)
	{
		super(id);
		this.setUnlocalizedName(name);
		this.setCreativeTab(BigReactors.TAB);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister iconRegister)
	{
		List<ItemStack> list = new ArrayList<ItemStack>();
		this.getSubItems(this.itemID, this.getCreativeTab(), list);

		if (list.size() > 1)
		{
			for (ItemStack itemStack : list)
			{
				this.icons.add(iconRegister.registerIcon(this.getUnlocalizedName(itemStack).replace("item.", BigReactors.TEXTURE_NAME_PREFIX)));
			}
		}
		else
		{
			this.itemIcon = iconRegister.registerIcon(this.getUnlocalizedName().replace("item.", BigReactors.TEXTURE_NAME_PREFIX));
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIconFromDamage(int damage)
	{
		if (this.icons.size() > damage && !this.isDamageable())
		{
			return icons.get(damage);
		}

		return super.getIconFromDamage(damage);
	}
}

package erogenousbeef.bigreactors.common.block;

import java.util.List;

import erogenousbeef.bigreactors.common.BigReactors;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;

public class BlockBROre extends Block {
	private Icon iconYellorite;

	public BlockBROre(int id)
	{
		super(id, Material.rock);
		this.setCreativeTab(BigReactors.TAB);
		this.setUnlocalizedName("brOre");
		this.setTextureName(BigReactors.TEXTURE_NAME_PREFIX + "oreYellorite");
		this.setHardness(2f);
	}

	@Override
	public Icon getIcon(int side, int metadata)
	{
		return this.iconYellorite;
	}

	@Override
	public void registerIcons(IconRegister par1IconRegister)
	{
		this.iconYellorite = par1IconRegister.registerIcon(BigReactors.TEXTURE_NAME_PREFIX + "oreYellorite");
	}

	@Override
	public int damageDropped(int metadata)
	{
		return metadata;
	}

	@Override
	public void getSubBlocks(int par1, CreativeTabs par2CreativeTabs, List par3List)
	{
		par3List.add(new ItemStack(par1, 1, 0));
	}
}

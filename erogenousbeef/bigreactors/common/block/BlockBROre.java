package erogenousbeef.bigreactors.common.block;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import erogenousbeef.bigreactors.common.BRLoader;
import erogenousbeef.bigreactors.common.BigReactors;

public class BlockBROre extends Block {
	private IIcon iconYellorite;

	//TODO: string id
	public BlockBROre()
	{
		super( Material.rock);
		this.setCreativeTab(BigReactors.TAB);
		this.setBlockName(BRLoader.MOD_ID+".brOre");
		this.setBlockTextureName(BigReactors.TEXTURE_NAME_PREFIX + "oreYellorite");
		this.setHardness(2f);
	}

	@Override
	public IIcon getIcon(int side, int metadata)
	{
		return this.iconYellorite;
	}

	@Override
	public void registerBlockIcons(IIconRegister par1IconRegister)
	{
		this.iconYellorite = par1IconRegister.registerIcon(BigReactors.TEXTURE_NAME_PREFIX + "oreYellorite");
	}

	@Override
	public int damageDropped(int metadata)
	{
		return metadata;
	}

	@Override
	public void getSubBlocks(Item par1, CreativeTabs par2CreativeTabs, List par3List)
	{
		par3List.add(new ItemStack(par1, 1, 0));
	}
}

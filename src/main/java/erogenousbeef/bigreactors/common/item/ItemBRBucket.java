package erogenousbeef.bigreactors.common.item;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import erogenousbeef.bigreactors.common.BigReactors;

public class ItemBRBucket extends ItemBucket {

	private Block _fluid;
	
	public ItemBRBucket(Block fluid) {
		super(fluid);
		setCreativeTab(BigReactors.TAB);
		_fluid = fluid;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerIcons(IIconRegister iconRegistry) {
		this.itemIcon = iconRegistry.registerIcon(BigReactors.TEXTURE_NAME_PREFIX + getUnlocalizedName());
	}

	@Override
	public boolean tryPlaceContainedLiquid(World world, int x, int y, int z) {
		if(_fluid == null) {
			return false;
		}
		else if(!world.isAirBlock(x, y, z) && world.getBlock(x, y, z).getMaterial().isSolid()) {
			return false;
		}
		else {
			world.setBlock(x, y, z, _fluid, 0, 3);
			return true;
		}
	}
	
	@Override
	public void getSubItems(Item item, CreativeTabs creativeTab, List subTypes) {
		subTypes.add(new ItemStack(item, 1, 0));
	}
}

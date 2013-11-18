package erogenousbeef.bigreactors.common.item;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import erogenousbeef.bigreactors.common.BigReactors;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemBRBucket extends ItemBucket {

	private int _fluidId;
	
	public ItemBRBucket(int id, int fluidId) {
		super(id, fluidId);
		setCreativeTab(BigReactors.TAB);
		_fluidId = fluidId;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerIcons(IconRegister iconRegistry) {
		this.itemIcon = iconRegistry.registerIcon(BigReactors.TEXTURE_NAME_PREFIX + getUnlocalizedName());
	}

	@Override
	public boolean tryPlaceContainedLiquid(World world, int x, int y, int z) {
		if(_fluidId <= 0) {
			return false;
		}
		else if(!world.isAirBlock(x, y, z) && world.getBlockMaterial(x, y, z).isSolid()) {
			return false;
		}
		else {
			world.setBlock(x, y, z, _fluidId, 0, 3);
			return true;
		}
	}
	
	@Override
	public void getSubItems(int itemId, CreativeTabs creativeTab, List subTypes) {
		subTypes.add(new ItemStack(itemId, 1, 0));
	}
}

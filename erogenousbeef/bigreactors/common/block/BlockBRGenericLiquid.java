package erogenousbeef.bigreactors.common.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import erogenousbeef.bigreactors.common.BigReactors;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.liquids.ILiquid;
import net.minecraftforge.liquids.LiquidStack;

public class BlockBRGenericLiquid extends Block implements ILiquid {

	private Icon _iconFlowing;
	private Icon _iconStill;
	
	public BlockBRGenericLiquid(int blockID, String unlocalizedName) {
		super(blockID, Material.water);
		

		setUnlocalizedName("liquid." + unlocalizedName + ".still");
		setHardness(100F);
		setLightOpacity(3);
	}

	@Override
	public int getLightValue(IBlockAccess world, int x, int y, int z) {
		if(blockID == BigReactors.liquidYelloriumStill.blockID) {
			return 12;
		}
		else if(blockID == BigReactors.liquidCyaniteStill.blockID) {
			return 4;
		}
		else if(blockID == BigReactors.liquidFuelColumnStill.blockID) {
			return 8;
		}
		else {
			return 0;
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerIcons(IconRegister iconRegistry) {
		_iconStill   = iconRegistry.registerIcon(BigReactors.TEXTURE_NAME_PREFIX + getUnlocalizedName());
		_iconFlowing = iconRegistry.registerIcon(BigReactors.TEXTURE_NAME_PREFIX + getUnlocalizedName().replace(".still", ".flowing"));
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public Icon getIcon(int side, int metadata) {
		return side <= 1 ? _iconStill : _iconFlowing;
	}
	
	// ILiquid
	@Override
	public int stillLiquidId() {
		return blockID;
	}

	@Override
	public boolean isMetaSensitive() {
		return false;
	}

	@Override
	public int stillLiquidMeta() {
		return 0;
	}
}

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
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;

public class BlockBRGenericFluid extends BlockFluidClassic {

	/*
	private Icon _iconFlowing;
	private Icon _iconStill;
	*/
	
	public BlockBRGenericFluid(int blockID, Fluid fluid, String unlocalizedName) {
		super(blockID, fluid, Material.water);

		setUnlocalizedName("fluid." + unlocalizedName + ".still");
	}

	// TODO: Remove if no longer needed
	/*
	@SideOnly(Side.CLIENT)
	@Override
	public void registerIcons(IconRegister iconRegistry) {
		_iconStill   = iconRegistry.registerIcon(BigReactors.TEXTURE_NAME_PREFIX + getUnlocalizedName());
		_iconFlowing = iconRegistry.registerIcon(BigReactors.TEXTURE_NAME_PREFIX + getUnlocalizedName().replace(".still", ".flowing"));
	}
	*/

	// TODO: Remove me if no longer needed
	/*
	@SideOnly(Side.CLIENT)
	@Override
	public Icon getIcon(int side, int metadata) {
		return side <= 1 ? _iconStill : _iconFlowing;
	}
	*/
}

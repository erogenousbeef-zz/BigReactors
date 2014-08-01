package erogenousbeef.bigreactors.common.block;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import erogenousbeef.bigreactors.common.BigReactors;

public class BlockBRGenericFluid extends BlockFluidClassic {

	private IIcon _iconFlowing;
	private IIcon _iconStill;
	
	public BlockBRGenericFluid(Fluid fluid, String unlocalizedName) {
		super(fluid, Material.water);

		setBlockName("fluid." + unlocalizedName + ".still");
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerBlockIcons(IIconRegister iconRegistry) {
		_iconStill   = iconRegistry.registerIcon(BigReactors.TEXTURE_NAME_PREFIX + getUnlocalizedName());
		_iconFlowing = iconRegistry.registerIcon(BigReactors.TEXTURE_NAME_PREFIX + getUnlocalizedName().replace(".still", ".flowing"));

		this.stack.getFluid().setIcons(_iconStill, _iconFlowing);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon(int side, int metadata) {
		return side <= 1 ? _iconStill : _iconFlowing;
	}
}

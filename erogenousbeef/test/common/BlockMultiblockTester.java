package erogenousbeef.test.common;

import erogenousbeef.core.multiblock.BlockMultiblockBase;
import erogenousbeef.core.multiblock.IMultiblockPart;
import erogenousbeef.test.TestMod;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

public class BlockMultiblockTester extends BlockMultiblockBase {

	public IIcon icon;
	//TODO: par1 == new id
	public BlockMultiblockTester(String par1,Material par2Material) {
		super(par1,par2Material);
		this.setCreativeTab(TestMod.TAB);
		this.setBlockName(BRLoader.MOD_ID+".mbTester");
		this.setHardness(1f);
	}

	@Override
	public IIcon getIcon(int side, int metadata) {
		return icon;
	}
	
	@Override
	public void registerBlockIcons(IIconRegister iconRegister) {
		this.icon = iconRegister.registerIcon(TestMod.TEXTURE_NAME_PREFIX + getUnlocalizedName());
	}
	
	@Override
	public TileEntity createNewTileEntity(World world) {
		return null;
	}
	
	@Override
	public TileEntity createTileEntity(World world, int metadata) {
		return new TileEntityMultiblockTester();
	}
	
/*	@Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9) {
		if(player.isSneaking()) {
			return false;
		}
		
		TileEntity te = world.getTileEntity(x, y, z);
		if(te != null && te instanceof TileEntityMultiblockTester) {
			((TileEntityMultiblockTester)te).changeColor();
			return true;
		}
		
		return false;
	}*/
	
	@Override
	public boolean renderAsNormalBlock() {
		return true;
	}
	
	@Override
	public boolean isOpaqueCube() {
		return true;
	}

	@Override
	public TileEntity createNewTileEntity(World var1, int var2) {
		return null;
	}
	
}

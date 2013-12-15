package erogenousbeef.bigreactors.common.block;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.tileentity.TileEntityReactorGlass;

public class BlockReactorGlass extends BlockContainer {

	public BlockReactorGlass(int id, Material material) {
		super(id, material);
		
		setStepSound(soundGlassFootstep);
		setHardness(1.0f);
		setUnlocalizedName("blockReactorGlass");
		this.setTextureName(BigReactors.TEXTURE_NAME_PREFIX + "blockReactorGlass");
		setCreativeTab(BigReactors.TAB);
	}
	
	@Override
	public TileEntity createNewTileEntity(World world) {
		return null;
	}
	
	@Override
	public TileEntity createTileEntity(World world, int metadata) {
		return new TileEntityReactorGlass();
	}
	
	@Override
	public Icon getIcon(int side, int metadata)
	{
		return blockIcon;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister par1IconRegister)
	{
		this.blockIcon = par1IconRegister.registerIcon(BigReactors.TEXTURE_NAME_PREFIX + getUnlocalizedName());
	}
	
	@Override
	public void onBlockAdded(World world, int x, int y, int z) {
		TileEntity te = world.getBlockTileEntity(x, y, z);
		if(te != null && te instanceof TileEntityReactorGlass) {
			TileEntityReactorGlass rp = (TileEntityReactorGlass)te;
			rp.onBlockAdded(world, x, y, z);
		}
	}
	
	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}
}

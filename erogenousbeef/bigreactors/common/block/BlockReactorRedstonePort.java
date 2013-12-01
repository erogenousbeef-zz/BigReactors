package erogenousbeef.bigreactors.common.block;

import java.util.Random;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import erogenousbeef.bigreactors.common.BRLoader;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.tileentity.TileEntityReactorRedstonePort;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

public class BlockReactorRedstonePort extends BlockContainer {

	protected Icon blockIconLit;
	
	public static final int META_REDSTONE_LIT = 1;
	public static final int META_REDSTONE_UNLIT = 0;
	
	protected final static int REDSTONE_VALUE_OFF = 0;  // corresponds to no power
	protected final static int REDSTONE_VALUE_ON  = 15; // corresponds to strong power
	
	public BlockReactorRedstonePort(int id, Material material) {
		super(id, material);
		
		setStepSound(soundMetalFootstep);
		setHardness(1.0f);
		setUnlocalizedName("blockReactorRedstonePort");
		this.setTextureName(BigReactors.TEXTURE_NAME_PREFIX + getUnlocalizedName());
		setCreativeTab(BigReactors.TAB);
	}
	
	@Override
	public TileEntity createNewTileEntity(World world) {
		return null;
	}
	
	@Override
	public TileEntity createTileEntity(World world, int metadata) {
		return new TileEntityReactorRedstonePort();
	}
	
	@Override
	public Icon getIcon(int side, int metadata)
	{
		if(side == 0 || side == 1) { return BigReactors.blockReactorPart.getIcon(side, BlockReactorPart.CASING_METADATA_BASE); }

		if(metadata == META_REDSTONE_LIT) { return blockIconLit; }
		else {
			return blockIcon;
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister par1IconRegister)
	{
		this.blockIcon = par1IconRegister.registerIcon(BigReactors.TEXTURE_NAME_PREFIX + getUnlocalizedName() + ".unlit");
		this.blockIconLit = par1IconRegister.registerIcon(BigReactors.TEXTURE_NAME_PREFIX + getUnlocalizedName() + ".lit");
	}
	
	
	@Override
	public int damageDropped(int metadata)
	{
		return META_REDSTONE_UNLIT;
	}

	@Override
	public void onBlockAdded(World world, int x, int y, int z) {
		TileEntity te = world.getBlockTileEntity(x, y, z);
		if(te instanceof TileEntityReactorRedstonePort) {
			TileEntityReactorRedstonePort rp = (TileEntityReactorRedstonePort)te;
			rp.onBlockAdded(world, x, y, z);
		}
	}
	
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9) {
		if(player.isSneaking()) {
			return false;
		}

		TileEntity te = world.getBlockTileEntity(x, y, z);
		if(te instanceof TileEntityReactorRedstonePort) {
			if(!((TileEntityReactorRedstonePort)te).isConnected()) { return false; }
			
			if(!world.isRemote)
				((TileEntityReactorRedstonePort)te).sendRedstoneUpdate();

			if(!world.isRemote) {
				player.openGui(BRLoader.instance, 0, world, x, y, z);
			}
			return true;
		}

		return false;
	}
	
    /**
     * A randomly called display update to be able to add particles or other items for display
     */
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(World world, int x, int y, int z, Random par5Random)
    {
    	TileEntity te = world.getBlockTileEntity(x, y, z);
        if (te instanceof TileEntityReactorRedstonePort)
        {
        	TileEntityReactorRedstonePort port = (TileEntityReactorRedstonePort)te;
        	if(port.isRedstoneActive()) {
                ForgeDirection out = port.getOutwardsDirection();
                double particleX, particleY, particleZ;
                particleY = y + 0.45D + par5Random.nextFloat() * 0.1D;

                if(out.offsetX > 0)
                	particleX = x + par5Random.nextFloat() * 0.1D + 1.1D;
                else
                	particleX = x + 0.45D + par5Random.nextFloat() * 0.1D;
                
                if(out.offsetZ > 0)
                	particleZ = z + par5Random.nextFloat() * 0.1D + 1.1D;
                else
                	particleZ = z + 0.45D + par5Random.nextFloat() * 0.1D;

                world.spawnParticle("reddust", particleX, particleY, particleZ, 0.0D, par5Random.nextFloat() * 0.1D, 0.0D);
        	}
        }
    }
	
	// Redstone API
	@Override
	public boolean canProvidePower() { return true; }
	
	@Override
	public int isProvidingStrongPower(IBlockAccess world, int x, int y, int z, int side) {
		if(side == 0 || side == 1) { return 0; }

		int md = world.getBlockMetadata(x, y, z);
		return (md == META_REDSTONE_LIT) ? REDSTONE_VALUE_ON : REDSTONE_VALUE_OFF;
	}
	
	@Override
	public int isProvidingWeakPower(IBlockAccess world, int x, int y, int z, int side) {
		if(side == 0 || side == 1) { return 0; }

		int md = world.getBlockMetadata(x, y, z);
		return (md == META_REDSTONE_LIT) ? REDSTONE_VALUE_ON : REDSTONE_VALUE_OFF;
	}
}

package erogenousbeef.bigreactors.common.multiblock.block;

import java.util.Random;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import powercrystals.minefactoryreloaded.api.rednet.IConnectableRedNet;
import powercrystals.minefactoryreloaded.api.rednet.RedNetConnectionType;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import erogenousbeef.bigreactors.common.BRLoader;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorRedstonePort;

public class BlockReactorRedstonePort extends BlockContainer implements IConnectableRedNet {

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
                ForgeDirection out = port.getOutwardsDir();
                
                if(out != ForgeDirection.UNKNOWN) {
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
    }
	
    @Override
	public void onNeighborBlockChange(World world, int x, int y, int z, int neighborBlockID) {
    	super.onNeighborBlockChange(world, x, y,z, neighborBlockID);

    	TileEntity te = world.getBlockTileEntity(x, y, z);
    	if(te instanceof TileEntityReactorRedstonePort) {
    		((TileEntityReactorRedstonePort)te).onNeighborBlockChange(x, y, z, neighborBlockID);
    	}
    }
    
	// Redstone API
	@Override
	public boolean canProvidePower() { return true; }
	
	@Override
	public int isProvidingStrongPower(IBlockAccess world, int x, int y, int z, int side) {
		return isProvidingWeakPower(world, x, y, z, side);
	}
	
	@Override
	public int isProvidingWeakPower(IBlockAccess world, int x, int y, int z, int side) {
		if(side == 0 || side == 1) { return REDSTONE_VALUE_OFF; }

		TileEntity te = world.getBlockTileEntity(x, y, z);
		if(te instanceof TileEntityReactorRedstonePort) {
			TileEntityReactorRedstonePort port = (TileEntityReactorRedstonePort)te;
			if(port.isOutput())
				return port.isRedstoneActive() ? REDSTONE_VALUE_ON : REDSTONE_VALUE_OFF;
			else
				return REDSTONE_VALUE_OFF;
		}
		
		return REDSTONE_VALUE_OFF;
	}

	// IConnectableRedNet - for pretty cable connections
	@Override
	public RedNetConnectionType getConnectionType(World world, int x, int y,
			int z, ForgeDirection side) {
		TileEntity te = world.getBlockTileEntity(x, y, z);
		if(te instanceof TileEntityReactorRedstonePort) {
			TileEntityReactorRedstonePort port = (TileEntityReactorRedstonePort)te;
			if(port.isConnected()) {
				return RedNetConnectionType.CableSingle;
			}
		}
		return RedNetConnectionType.None;
	}

	@Override
	public int[] getOutputValues(World world, int x, int y, int z,
			ForgeDirection side) {
		return null;
	}

	@Override
	public int getOutputValue(World world, int x, int y, int z,
			ForgeDirection side, int subnet) {
		return isProvidingWeakPower(world, x, y, z, side.ordinal());
	}

	@Override
	public void onInputsChanged(World world, int x, int y, int z,
			ForgeDirection side, int[] inputValues) {
		// Not used
	}

	@Override
	public void onInputChanged(World world, int x, int y, int z,
			ForgeDirection side, int inputValue) {
		TileEntity te = world.getBlockTileEntity(x, y, z);
		if(te instanceof TileEntityReactorRedstonePort) {
			TileEntityReactorRedstonePort port = (TileEntityReactorRedstonePort)te;
			port.onRedNetUpdate(inputValue);
		}
	}
}

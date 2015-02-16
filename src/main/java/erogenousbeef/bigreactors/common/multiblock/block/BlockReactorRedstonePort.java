package erogenousbeef.bigreactors.common.multiblock.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import powercrystals.minefactoryreloaded.api.rednet.IRedNetOmniNode;
import powercrystals.minefactoryreloaded.api.rednet.connectivity.RedNetConnectionType;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import erogenousbeef.bigreactors.common.BRLoader;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorRedstonePort;

@Optional.InterfaceList({
	@Optional.Interface(iface = "powercrystals.minefactoryreloaded.api.rednet.IRedNetOmniNode", modid = "MineFactoryReloaded")
})
public class BlockReactorRedstonePort extends BlockContainer implements IRedNetOmniNode {

	protected IIcon blockIconLit;

	public static final int META_REDSTONE_LIT = 1;
	public static final int META_REDSTONE_UNLIT = 0;

	protected final static int REDSTONE_VALUE_OFF = 0;  // corresponds to no power
	protected final static int REDSTONE_VALUE_ON  = 15; // corresponds to strong power

	public BlockReactorRedstonePort(Material material) {
		super(material);

		setStepSound(soundTypeMetal);
		setHardness(2.0f);
		setBlockName("blockReactorRedstonePort");
		this.setBlockTextureName(BigReactors.TEXTURE_NAME_PREFIX + getUnlocalizedName());
		setCreativeTab(BigReactors.TAB);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		return new TileEntityReactorRedstonePort();
	}

	@Override
	public IIcon getIcon(int side, int metadata)
	{
		if(side == 0 || side == 1) { return BigReactors.blockReactorPart.getIcon(side, BlockReactorPart.METADATA_CASING); }

		if(metadata == META_REDSTONE_LIT) { return blockIconLit; }
		else {
			return blockIcon;
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister par1IconRegister)
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

		TileEntity te = world.getTileEntity(x, y, z);
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
	@Override
	@SideOnly(Side.CLIENT)
    public void randomDisplayTick(World world, int x, int y, int z, Random par5Random)
    {
    	TileEntity te = world.getTileEntity(x, y, z);
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
	public void onNeighborBlockChange(World world, int x, int y, int z, Block neighborBlock) {
    	super.onNeighborBlockChange(world, x, y,z, neighborBlock);

    	TileEntity te = world.getTileEntity(x, y, z);
    	if(te instanceof TileEntityReactorRedstonePort) {
    		((TileEntityReactorRedstonePort)te).onNeighborBlockChange(x, y, z, neighborBlock);
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

		TileEntity te = world.getTileEntity(x, y, z);
		if(te instanceof TileEntityReactorRedstonePort) {
			TileEntityReactorRedstonePort port = (TileEntityReactorRedstonePort)te;
			if(port.isOutput())
				return port.isRedstoneActive() ? REDSTONE_VALUE_ON : REDSTONE_VALUE_OFF;
			else
				return REDSTONE_VALUE_OFF;
		}

		return REDSTONE_VALUE_OFF;
	}

	// IRedNetOmniNode - for pretty cable connections
	@Optional.Method(modid = "MineFactoryReloaded")
	@Override
	public RedNetConnectionType getConnectionType(World world, int x, int y,
			int z, ForgeDirection side) {
		TileEntity te = world.getTileEntity(x, y, z);
		if(te instanceof TileEntityReactorRedstonePort) {
			TileEntityReactorRedstonePort port = (TileEntityReactorRedstonePort)te;
			if(port.isConnected()) {
				return RedNetConnectionType.CableSingle;
			}
		}
		return RedNetConnectionType.None;
	}

	@Optional.Method(modid = "MineFactoryReloaded")
	@Override
	public int[] getOutputValues(World world, int x, int y, int z,
			ForgeDirection side) {
		return null;
	}

	@Optional.Method(modid = "MineFactoryReloaded")
	@Override
	public int getOutputValue(World world, int x, int y, int z,
			ForgeDirection side, int subnet) {
		return isProvidingWeakPower(world, x, y, z, side.ordinal());
	}

	@Optional.Method(modid = "MineFactoryReloaded")
	@Override
	public void onInputsChanged(World world, int x, int y, int z,
			ForgeDirection side, int[] inputValues) {
		// Not used
	}

	@Optional.Method(modid = "MineFactoryReloaded")
	@Override
	public void onInputChanged(World world, int x, int y, int z,
			ForgeDirection side, int inputValue) {
		TileEntity te = world.getTileEntity(x, y, z);
		if(te instanceof TileEntityReactorRedstonePort) {
			TileEntityReactorRedstonePort port = (TileEntityReactorRedstonePort)te;
			port.onRedNetUpdate(inputValue);
		}
	}

	@Override
    public boolean canCreatureSpawn(EnumCreatureType type, IBlockAccess world, int x, int y, int z)
    {
		return false;
    }
}

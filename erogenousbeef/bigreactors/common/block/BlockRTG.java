package erogenousbeef.bigreactors.common.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.tileentity.TileEntityRTG;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

public class BlockRTG extends BlockContainer {

	private Icon iconMachine;
	private Icon iconRTGOff;
	private Icon iconRTGOn;
	
	public BlockRTG(int id, Material material) {
		super(id, material);
		
		setStepSound(soundMetalFootstep);
		setHardness(1.0f);
		setUnlocalizedName("blockRadiothermalGen");
		this.setTextureName(BigReactors.TEXTURE_NAME_PREFIX + "blockRadiothermalGen");
		setCreativeTab(BigReactors.TAB);
	}
	
    /**
     * Called whenever the block is added into the world. Args: world, x, y, z
     */
    public void onBlockAdded(World world, int x, int y, int z)
    {
        super.onBlockAdded(world, x, y, z);
        this.setDefaultDirection(world, x, y, z);
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack stack) {
    	if(entity == null) { return; }

    	// Block starts out facing the player
    	int facing = MathHelper.floor_double((double)((entity.rotationYaw * 4F) / 360F) + 0.5D) & 3;
    	facing += 3;
    	world.setBlockMetadataWithNotify(x, y, z, facing, 2);
    }
    
    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entity, int side, float xOffset, float yOffset, float zOffset) {
    	if(entity.isSneaking()) { return false; }
    	else {
    		// For now, rotate. Later on, add wrench support and GUI
    		int metadata = world.getBlockMetadata(x, y, z);
    		metadata++;
    		if(metadata > 6) { metadata = 3; }
    		world.setBlockMetadataWithNotify(x, y, z, metadata, 2);
    		// world.markBlockForUpdate(x, y, z);
    		return true;
    	}
    }

    /**
     * set a blocks direction
     */
    private void setDefaultDirection(World world, int x, int y, int z)
    {
        if (!world.isRemote)
        {
            int southBlock = world.getBlockId(x, y, z - 1);
            int northBlock = world.getBlockId(x, y, z + 1);
            int eastBlock = world.getBlockId(x - 1, y, z);
            int westBlock = world.getBlockId(x + 1, y, z);
            byte newMetadata = 3;

            if (Block.opaqueCubeLookup[southBlock] && !Block.opaqueCubeLookup[northBlock])
            {
                newMetadata = 3;
            }

            if (Block.opaqueCubeLookup[northBlock] && !Block.opaqueCubeLookup[southBlock])
            {
                newMetadata = 2;
            }

            if (Block.opaqueCubeLookup[eastBlock] && !Block.opaqueCubeLookup[westBlock])
            {
                newMetadata = 5;
            }

            if (Block.opaqueCubeLookup[westBlock] && !Block.opaqueCubeLookup[eastBlock])
            {
                newMetadata = 4;
            }

            world.setBlockMetadataWithNotify(x, y, z, newMetadata, 2);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Icon getBlockTexture(IBlockAccess iblockaccess, int x, int y, int z, int side) {
    	int metadata = iblockaccess.getBlockMetadata(x, y, z);
    	if(side != metadata) {
    		return iconMachine;
    	}
    	else {
    		TileEntity te = iblockaccess.getBlockTileEntity(x, y, z);
    		if(te instanceof TileEntityRTG) {
    			if( ((TileEntityRTG)te).isActive() ) { return iconRTGOn; }
    			else { return iconRTGOff; }
    		}
    		else {
    			return iconRTGOff;
    		}
    	}
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public Icon getIcon(int side, int metadata) {
    	if(side != metadata) { return iconMachine; }
    	else {
			return iconRTGOff;
    	}
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IconRegister iconRegister)
    {
        this.iconMachine 	= iconRegister.registerIcon(BigReactors.TEXTURE_NAME_PREFIX + BigReactors.blockReactorPart.getUnlocalizedName());
        this.iconRTGOn 		= iconRegister.registerIcon(BigReactors.TEXTURE_NAME_PREFIX + getUnlocalizedName() + ".On");
        this.iconRTGOff 	= iconRegister.registerIcon(BigReactors.TEXTURE_NAME_PREFIX + getUnlocalizedName() + ".Off");
    }

	@Override
	public TileEntity createNewTileEntity(World world) {
		return null;
	}
	
	@Override
	public TileEntity createTileEntity(World world, int metadata) {
		return new TileEntityRTG();
	}
}

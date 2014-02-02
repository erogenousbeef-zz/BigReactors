package erogenousbeef.bigreactors.common.multiblock.block;

import java.util.List;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.Icon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorGlass;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityTurbinePartGlass;
import erogenousbeef.bigreactors.utils.StaticUtils;
import erogenousbeef.core.multiblock.IMultiblockPart;
import erogenousbeef.core.multiblock.MultiblockControllerBase;

public class BlockMultiblockGlass extends BlockContainer {

	public static final int METADATA_REACTOR = 0;
	public static final int METADATA_TURBINE = 1;
	
	private static String[] subBlocks = new String[] { "reactor", "turbine" };
	private Icon[][] icons = new Icon[subBlocks.length][16]; 
	private Icon transparentIcon;
	
	public BlockMultiblockGlass(int par1, Material par2Material) {
		super(par1, par2Material);
		
		setStepSound(soundGlassFootstep);
		setHardness(1.0f);
		setUnlocalizedName("multiblockGlass");
		this.setTextureName(BigReactors.TEXTURE_NAME_PREFIX + "multiblockGlass");
		setCreativeTab(BigReactors.TAB);
	}

	@Override
	public TileEntity createNewTileEntity(World world) {
		return null;
	}

	@Override
	public TileEntity createTileEntity(World world, int metadata) {
		switch(metadata) {
		case METADATA_REACTOR:
			return new TileEntityReactorGlass();
		case METADATA_TURBINE:
			return new TileEntityTurbinePartGlass();
		default:
			throw new IllegalArgumentException("Unrecognized metadata");
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister par1IconRegister)
	{
		this.transparentIcon = par1IconRegister.registerIcon(BigReactors.TEXTURE_NAME_PREFIX + getUnlocalizedName() + ".transparent");
		
		for(int metadata = 0; metadata < subBlocks.length; metadata++) {
			for(int i = 0; i < 16; ++i) {
				icons[metadata][i] = par1IconRegister.registerIcon(BigReactors.TEXTURE_NAME_PREFIX + getUnlocalizedName() + "." + subBlocks[metadata] + "." + Integer.toString(i));
			}
		}
	}

	@Override
    public Icon getBlockTexture(IBlockAccess blockAccess, int x, int y, int z, int side) {
		ForgeDirection[] dirsToCheck = StaticUtils.neighborsBySide[side];
		ForgeDirection dir;
		int myBlockId = blockAccess.getBlockId(x,y,z);
		int myBlockMetadata = blockAccess.getBlockMetadata(x, y, z);
		
		// First check if we have a block in front of us of the same type - if so, just be completely transparent on this side
		ForgeDirection out = ForgeDirection.getOrientation(side);
		if(blockAccess.getBlockId(x + out.offsetX, y+out.offsetY, z+out.offsetZ) == myBlockId &&
				blockAccess.getBlockMetadata(x + out.offsetX, y + out.offsetY, z + out.offsetZ) == myBlockMetadata) {
			return transparentIcon;
		}
		
		// Calculate icon index based on whether the blocks around this block match it
		// Icons use a naming pattern so that the bits correspond to:
		// 1 = Connected on top, 2 = connected on bottom, 4 = connected on left, 8 = connected on right
		int iconIdx = 0;
		for(int i = 0; i < dirsToCheck.length; i++) {
			dir = dirsToCheck[i];
			// Same blockID and metadata on this side?
			if(blockAccess.getBlockId(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ) == myBlockId &&
					blockAccess.getBlockMetadata(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ) == myBlockMetadata) {
				// Connected!
				iconIdx |= 1 << i;
			}
		}
		
		return icons[myBlockMetadata][iconIdx];
	}
	
	@Override
	public Icon getIcon(int side, int metadata) {
		return icons[metadata][0];
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}
	
	@Override
	public int damageDropped(int metadata)
	{
		return metadata;
	}

	public ItemStack getItemStack(String name) {
		int metadata = -1;
		for(int i = 0; i < subBlocks.length; i++) {
			if(subBlocks[i].equals(name)) {
				metadata = i;
				break;
			}
		}
		
		if(metadata < 0) {
			throw new IllegalArgumentException("Unable to find a block with the name " + name);
		}
		return new ItemStack(blockID, 1, metadata);
	}

	@Override
	public void getSubBlocks(int par1, CreativeTabs par2CreativeTabs, List par3List)
	{
		for(int i = 0; i < subBlocks.length; i++) {
			par3List.add(new ItemStack(blockID, 1, i));
		}
	}
	
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9) {
		if(player.isSneaking()) {
			return false;
		}

		// If the player's hands are empty and they rightclick on a multiblock, they get a 
		// multiblock-debugging message if the machine is not assembled.
		if(!world.isRemote && player.getCurrentEquippedItem() == null) {
			TileEntity te = world.getBlockTileEntity(x, y, z);
			if(te instanceof IMultiblockPart) {
				MultiblockControllerBase controller = ((IMultiblockPart)te).getMultiblockController();

				if(controller == null) {
					player.sendChatToPlayer(ChatMessageComponent.createFromText(String.format("SERIOUS ERROR - server part @ %d, %d, %d has no controller!", x, y, z)));
				}
				else {
					Exception e = controller.getLastValidationException();
					if(e != null) {
						player.sendChatToPlayer(ChatMessageComponent.createFromText(e.getMessage()));
						return true;
					}
				}
			}
		}
		
		return false;
	}
}

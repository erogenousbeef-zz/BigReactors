package erogenousbeef.bigreactors.common.multiblock.block;

import java.util.List;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import erogenousbeef.bigreactors.common.BRLoader;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.multiblock.MultiblockTurbine;
import erogenousbeef.bigreactors.common.multiblock.interfaces.INeighborUpdatableEntity;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityTurbineCreativeSteamGenerator;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityTurbinePart;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityTurbinePowerTap;
import erogenousbeef.core.common.CoordTriplet;
import erogenousbeef.core.multiblock.IMultiblockPart;
import erogenousbeef.core.multiblock.MultiblockControllerBase;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.Icon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

public class BlockTurbinePart extends BlockContainer {

	public static final int METADATA_HOUSING = 0;
	public static final int METADATA_CONTROLLER = 1;
	public static final int METADATA_POWERTAP = 2;
	public static final int METADATA_FLUIDPORT = 3;
	public static final int METADATA_BEARING = 4;
	public static final int METADATA_CREATIVE_GENERATOR = 5;
	
	private static final String[] _subBlocks = new String[] { "housing",
														"controller",
														"powerTap",
														"fluidPort",
														"bearing",
														"creativeSteamGenerator" };

	// Additional non-metadata-based icons
	private static final int SUBICON_NONE = -1;
	private static final int SUBICON_HOUSING_FRAME_VERTICAL = 0;
	private static final int SUBICON_HOUSING_FRAME_EASTWEST = 1;
	private static final int SUBICON_HOUSING_FRAME_NORTHSOUTH = 2;
	private static final int SUBICON_HOUSING_FACE = 3;
	private static final int SUBICON_HOUSING_CORNER = 4;
	private static final int SUBICON_CONTROLLER_IDLE = 5;
	private static final int SUBICON_CONTROLLER_ACTIVE = 6;
	private static final int SUBICON_POWERTAP_ACTIVE = 7;
	private static final int SUBICON_FLUIDPORT_OUTPUT = 8;

	private static final String[] _subIconNames = new String[] {
		"housing.vertical",
		"housing.eastwest",
		"housing.northsouth",
		"housing.face",
		"housing.corner",
		"controller.idle",
		"controller.active",
		"powerTap.connected",
		"fluidPort.outlet"
	};
	
	private Icon[] _icons = new Icon[_subBlocks.length];
	private Icon[] _subIcons = new Icon[_subIconNames.length];
	
	public BlockTurbinePart(int blockID, Material material) {
		super(blockID, material);

		setStepSound(soundMetalFootstep);
		setHardness(1.0f);
		setUnlocalizedName("blockTurbinePart");
		this.setTextureName(BigReactors.TEXTURE_NAME_PREFIX + "blockTurbinePart");
		setCreativeTab(BigReactors.TAB);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister par1IconRegister)
	{
		// Base icons
		for(int i = 0; i < _subBlocks.length; ++i) {
			_icons[i] = par1IconRegister.registerIcon(BigReactors.TEXTURE_NAME_PREFIX + getUnlocalizedName() + "." + _subBlocks[i]);
		}
		
		for(int i = 0; i < _subIcons.length; i++) {
			_subIcons[i] = par1IconRegister.registerIcon(BigReactors.TEXTURE_NAME_PREFIX + getUnlocalizedName() + "." + _subIconNames[i]);
		}
		
		this.blockIcon = _icons[0];
	}

	@Override
    public Icon getBlockTexture(IBlockAccess blockAccess, int x, int y, int z, int side) {
		TileEntity te = blockAccess.getBlockTileEntity(x, y, z);
		int metadata = blockAccess.getBlockMetadata(x,y,z);

		if(te instanceof TileEntityTurbinePart) {
			TileEntityTurbinePart part = (TileEntityTurbinePart)te;
			MultiblockTurbine turbine = part.getTurbine();
			
			if(!part.isConnected() || turbine == null || !turbine.isAssembled()) {
				return getIcon(side, metadata);
			}
			else {
				int subIcon = SUBICON_NONE;
				if(metadata == METADATA_HOUSING) {
					//FMLLog.info("getting subicon for housing @ %d, %d, %d on controller %d", x, y, z, turbine.hashCode());
					subIcon = getSubIconForHousing(x, y, z, turbine, side);
				}
				else if(part.getOutwardsDir().ordinal() == side) {
					// Only put the fancy icon on one side of the machine. Other parts will use the base.
					if(metadata == METADATA_CONTROLLER) {
						if(turbine.isActive()) {
							subIcon = SUBICON_CONTROLLER_ACTIVE;
						}
						else {
							subIcon = SUBICON_CONTROLLER_IDLE;
						}
					}
					else if(metadata == METADATA_POWERTAP) {
						if(te instanceof TileEntityTurbinePowerTap && ((TileEntityTurbinePowerTap)te).isAttachedToPowerNetwork()) {
							subIcon = SUBICON_POWERTAP_ACTIVE;
						}
					}
					else if(metadata == METADATA_FLUIDPORT) {
						// TODO
					}
				}
				else {
					// Assembled non-housing parts use the face texture so it's all smooth on the inside
					subIcon = SUBICON_HOUSING_FACE;
				}
				
				if(subIcon == SUBICON_NONE) {
					return getIcon(side, metadata);
				}
				else {
					return _subIcons[subIcon];
				}
			}
		}
		else {
			FMLLog.info("block @ %d, %d, %d is not a turbine part", x, y, z);
		}

		// Not a "proper" TE, so just pass through
		return getIcon(side, metadata);
	}
	
	private int getSubIconForHousing(int x, int y, int z, MultiblockTurbine turbine, int side) {
		CoordTriplet minCoord, maxCoord;
		minCoord = turbine.getMinimumCoord();
		maxCoord = turbine.getMaximumCoord();
		
		if(minCoord == null || maxCoord == null) {
			FMLLog.info("bailing out @ %d, %d %d - min/max are null", x, y, z);
			return SUBICON_NONE;
		}
		
		int extremes = 0;
		boolean xExtreme, yExtreme, zExtreme;
		xExtreme = yExtreme = zExtreme = false;

		if(x == minCoord.x) { extremes++; xExtreme = true; }
		if(y == minCoord.y) { extremes++; yExtreme = true; }
		if(z == minCoord.z) { extremes++; zExtreme = true; }
		
		if(x == maxCoord.x) { extremes++; xExtreme = true; }
		if(y == maxCoord.y) { extremes++; yExtreme = true; }
		if(z == maxCoord.z) { extremes++; zExtreme = true; }

		if(extremes >= 3) {
			return SUBICON_HOUSING_CORNER;
		}
		else if(extremes <= 0) {
			return SUBICON_NONE;
		}
		else if(extremes == 1) {
			return SUBICON_HOUSING_FACE;
		}
		else {
			// fun...
			if(!yExtreme) {
				// Vertical frame
				if(side == 0 || side == 1) { return SUBICON_NONE; }
				else { return SUBICON_HOUSING_FRAME_VERTICAL; }
			}
			else if(!xExtreme) {
				// East-west frame
				if(side == 4 || side == 5) { return SUBICON_NONE; }
				else { return SUBICON_HOUSING_FRAME_EASTWEST; }
			}
			else {
				// North-south frame
				if(side == 0 || side == 1) { return SUBICON_HOUSING_FRAME_NORTHSOUTH; }
				else if(side == 4 || side == 5) { return SUBICON_HOUSING_FRAME_EASTWEST; }
				else { return SUBICON_NONE; }
			}
		}
	}

	@Override
	public Icon getIcon(int side, int metadata) {
		return _icons[metadata];
	}

	@Override
	public TileEntity createNewTileEntity(World world) {
		// We use the metadata-driven version. Not this one.
		return null;
	}

	@Override
	public TileEntity createTileEntity(World world, int metadata) {
		if(metadata == METADATA_POWERTAP) {
			return new TileEntityTurbinePowerTap(metadata);
		}
		else if(metadata == METADATA_CREATIVE_GENERATOR) {
			return new TileEntityTurbineCreativeSteamGenerator(metadata);
		}
		else {
			return new TileEntityTurbinePart(metadata);
		}
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, int neighborBlockID) {
		TileEntity te = world.getBlockTileEntity(x, y, z);
		// Signal power taps when their neighbors change, etc.
		if(te instanceof INeighborUpdatableEntity) {
			((INeighborUpdatableEntity)te).onNeighborBlockChange(world, x, y, z, neighborBlockID);
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
						player.sendChatToPlayer(ChatMessageComponent.createFromText(e.getMessage() + " - controller " + Integer.toString(controller.hashCode())));
						return true;
					}
				}
			}
		}
		
		int metadata = world.getBlockMetadata(x, y, z);
		
		// Does this machine even have a GUI?
		if(metadata != METADATA_CONTROLLER) { return false; }

		// Check to see if machine is assembled
		TileEntity te = world.getBlockTileEntity(x, y, z);
		if(!(te instanceof IMultiblockPart)) {
			return false;
		}
		
		IMultiblockPart part = (IMultiblockPart)te;
		if(!part.isConnected() || !part.getMultiblockController().isAssembled()) {
			return false;
		}
		
		if(!world.isRemote) {
			player.openGui(BRLoader.instance, 0, world, x, y, z);
		}
		return true;
	}

	@Override
	public boolean isOpaqueCube()
	{
		return true;
	}

	@Override
	public boolean renderAsNormalBlock()
	{
		return true;
	}

	@Override
	public int damageDropped(int metadata)
	{
		return metadata;
	}
	
	public ItemStack getItemStack(String name) {
		int metadata = -1;
		for(int i = 0; i < _subBlocks.length; i++) {
			if(_subBlocks[i].equals(name)) {
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
		for(int i = 0; i < _subBlocks.length; i++) {
			par3List.add(new ItemStack(blockID, 1, i));
		}
	}
	
	@Override
	public void breakBlock(World world, int x, int y, int z, int blockId, int meta)
	{
		// Drop everything inside inventory blocks
		TileEntity te = world.getBlockTileEntity(x, y, z);
		if(te instanceof IInventory)
		{
			IInventory inventory = ((IInventory)te);
inv:		for(int i = 0; i < inventory.getSizeInventory(); i++)
			{
				ItemStack itemstack = inventory.getStackInSlot(i);
				if(itemstack == null)
				{
					continue;
				}
				float xOffset = world.rand.nextFloat() * 0.8F + 0.1F;
				float yOffset = world.rand.nextFloat() * 0.8F + 0.1F;
				float zOffset = world.rand.nextFloat() * 0.8F + 0.1F;
				do
				{
					if(itemstack.stackSize <= 0)
					{
						continue inv;
					}
					int amountToDrop = world.rand.nextInt(21) + 10;
					if(amountToDrop > itemstack.stackSize)
					{
						amountToDrop = itemstack.stackSize;
					}
					itemstack.stackSize -= amountToDrop;
					EntityItem entityitem = new EntityItem(world, (float)x + xOffset, (float)y + yOffset, (float)z + zOffset, new ItemStack(itemstack.itemID, amountToDrop, itemstack.getItemDamage()));
					if(itemstack.getTagCompound() != null)
					{
						entityitem.getEntityItem().setTagCompound(itemstack.getTagCompound());
					}
					float motionMultiplier = 0.05F;
					entityitem.motionX = (float)world.rand.nextGaussian() * motionMultiplier;
					entityitem.motionY = (float)world.rand.nextGaussian() * motionMultiplier + 0.2F;
					entityitem.motionZ = (float)world.rand.nextGaussian() * motionMultiplier;
					world.spawnEntityInWorld(entityitem);
				} while(true);
			}
		}

		super.breakBlock(world, x, y, z, blockId, meta);
	}
}

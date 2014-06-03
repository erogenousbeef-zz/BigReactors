package erogenousbeef.bigreactors.common.multiblock.block;

import java.util.List;
import java.util.Random;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.Icon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import erogenousbeef.bigreactors.common.BRLoader;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.multiblock.MultiblockTurbine;
import erogenousbeef.bigreactors.common.multiblock.interfaces.INeighborUpdatableEntity;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityTurbineComputerPort;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityTurbineFluidPort;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityTurbineFluidPort.FluidFlow;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityTurbinePartBase;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityTurbinePartStandard;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityTurbinePowerTap;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityTurbineRotorBearing;
import erogenousbeef.bigreactors.utils.StaticUtils;
import erogenousbeef.core.common.CoordTriplet;
import erogenousbeef.core.multiblock.IMultiblockPart;
import erogenousbeef.core.multiblock.MultiblockControllerBase;

public class BlockTurbinePart extends BlockContainer implements IPeripheralProvider {

	public static final int METADATA_HOUSING = 0;
	public static final int METADATA_CONTROLLER = 1;
	public static final int METADATA_POWERTAP = 2;
	public static final int METADATA_FLUIDPORT = 3;
	public static final int METADATA_BEARING = 4;
	public static final int METADATA_COMPUTERPORT = 5;
	
	private static final String[] _subBlocks = new String[] { "housing",
														"controller",
														"powerTap",
														"fluidPort",
														"bearing",
														"computerPort" };

	// Additional non-metadata-based icons
	private static final int SUBICON_NONE = -1;
	private static final int SUBICON_HOUSING_FRAME_TOP = 0;
	private static final int SUBICON_HOUSING_FRAME_BOTTOM = 1;
	private static final int SUBICON_HOUSING_FRAME_LEFT = 2;
	private static final int SUBICON_HOUSING_FRAME_RIGHT = 3;
	private static final int SUBICON_HOUSING_FACE = 4;
	private static final int SUBICON_HOUSING_CORNER = 5;
	private static final int SUBICON_CONTROLLER_IDLE = 6;
	private static final int SUBICON_CONTROLLER_ACTIVE = 7;
	private static final int SUBICON_POWERTAP_ACTIVE = 8;
	private static final int SUBICON_FLUIDPORT_OUTPUT = 9;

	private static final String[] _subIconNames = new String[] {
		"housing.edge.0",
		"housing.edge.1",
		"housing.edge.2",
		"housing.edge.3",
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
		setHardness(2.0f);
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

		if(te instanceof TileEntityTurbinePartBase) {
			TileEntityTurbinePartBase part = (TileEntityTurbinePartBase)te;
			MultiblockTurbine turbine = part.getTurbine();
			
			if(metadata == METADATA_FLUIDPORT) {
				if(te instanceof TileEntityTurbineFluidPort) {
					if(turbine == null || !turbine.isAssembled() || part.getOutwardsDir().ordinal() == side)
					{
						if(((TileEntityTurbineFluidPort)te).getFlowDirection() == FluidFlow.Out)
							return _subIcons[SUBICON_FLUIDPORT_OUTPUT];
						else
							return _icons[METADATA_FLUIDPORT];
					}
					else if(turbine.isAssembled() && part.getOutwardsDir().ordinal() != side)
						return _subIcons[SUBICON_HOUSING_FACE];
				}
				return getIcon(side, metadata);
			}
			else if(!part.isConnected() || turbine == null || !turbine.isAssembled()) {
				return getIcon(side, metadata);
			}
			else {
				int subIcon = SUBICON_NONE;
				if(metadata == METADATA_HOUSING) {
					subIcon = getSubIconForHousing(blockAccess, x, y, z, turbine, side);
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

		// Not a "proper" TE, so just pass through
		return getIcon(side, metadata);
	}
	
	private int getSubIconForHousing(IBlockAccess blockAccess, int x, int y, int z, MultiblockTurbine turbine, int side) {
		CoordTriplet minCoord, maxCoord;
		minCoord = turbine.getMinimumCoord();
		maxCoord = turbine.getMaximumCoord();
		
		if(minCoord == null || maxCoord == null) {
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
			ForgeDirection[] dirsToCheck = StaticUtils.neighborsBySide[side];
			ForgeDirection dir;

			int myBlockId = blockAccess.getBlockId(x,y,z);
			int iconIdx = -1;

			for(int i = 0; i < dirsToCheck.length; i++) {
				dir = dirsToCheck[i];
				
				int neighborBlockId = blockAccess.getBlockId(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ);
				// See if we're a turbine part
				if(neighborBlockId != myBlockId && neighborBlockId != BigReactors.blockMultiblockGlass.blockID
						&& (BigReactors.blockMultiblockCreativePart != null && neighborBlockId != BigReactors.blockMultiblockCreativePart.blockID)) {
					// One of these things is not like the others...
					iconIdx = i;
					break;
				}
			}
			
			return iconIdx + SUBICON_HOUSING_FRAME_TOP;
		}
	}

	@Override
	public Icon getIcon(int side, int metadata) {
		metadata = Math.max(0, Math.min(metadata, _subBlocks.length-1));
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
			return new TileEntityTurbinePowerTap();
		}
		else if(metadata == METADATA_FLUIDPORT) {
			return new TileEntityTurbineFluidPort();
		}
		else if(metadata == METADATA_BEARING) {
			// Does jack-all different except for store display lists on the client
			return new TileEntityTurbineRotorBearing();
		}
		else if(metadata == METADATA_COMPUTERPORT) {
			return new TileEntityTurbineComputerPort();
		}
		else {
			return new TileEntityTurbinePartStandard();
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
		
		int metadata = world.getBlockMetadata(x, y, z);
		
		if(metadata == METADATA_FLUIDPORT && (player.getCurrentEquippedItem() == null || StaticUtils.Inventory.isPlayerHoldingWrench(player))) {
			TileEntity te = world.getBlockTileEntity(x, y, z);
			if(te instanceof TileEntityTurbineFluidPort) {
				TileEntityTurbineFluidPort fluidPort = (TileEntityTurbineFluidPort)te; 
				FluidFlow flow = fluidPort.getFlowDirection();
				fluidPort.setFluidFlowDirection(flow == FluidFlow.In ? FluidFlow.Out : FluidFlow.In);
				return true;
			}
		}
		
		if(world.isRemote) {
			return true;
		}
		
		// If the player's hands are empty and they rightclick on a multiblock, they get a 
		// multiblock-debugging message if the machine is not assembled.
		if(player.getCurrentEquippedItem() == null) {
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
		
		player.openGui(BRLoader.instance, 0, world, x, y, z);
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
	
    /**
     * A randomly called display update to be able to add particles or other items for display
     */
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(World world, int x, int y, int z, Random par5Random)
    {
    	int metadata = world.getBlockMetadata(x, y, z);
    	if(metadata == METADATA_BEARING) {
        	TileEntity te = world.getBlockTileEntity(x, y, z);
    		if(te instanceof TileEntityTurbinePartStandard) {
    			// Rotor bearing found!
    			TileEntityTurbinePartStandard bearing = (TileEntityTurbinePartStandard)te;
    			MultiblockTurbine turbine = bearing.getTurbine();
    			if(turbine != null && turbine.isActive()) {
    				// Spawn particles!
    				int numParticles = Math.min(20, Math.max(1, turbine.getFluidConsumedLastTick() / 40));
    				ForgeDirection inwardsDir = bearing.getOutwardsDir().getOpposite();
    				CoordTriplet minCoord, maxCoord;
    				minCoord = turbine.getMinimumCoord();
    				maxCoord = turbine.getMaximumCoord();
    				minCoord.x++; minCoord.y++; minCoord.z++;
    				maxCoord.x--; maxCoord.y--; maxCoord.z--;
    				if(inwardsDir.offsetX != 0) {
    					minCoord.x = maxCoord.x = bearing.xCoord + inwardsDir.offsetX;
    				}
    				else if(inwardsDir.offsetY != 0) {
    					minCoord.y = maxCoord.y = bearing.yCoord + inwardsDir.offsetY;
    				}
    				else {
    					minCoord.z = maxCoord.z = bearing.zCoord + inwardsDir.offsetZ;
    				}
    				
                    double particleX, particleY, particleZ;
    				for(int i = 0; i < numParticles; i++) {
    					particleX = minCoord.x + par5Random.nextFloat() * (maxCoord.x - minCoord.x + 1);
    					particleY = minCoord.y + par5Random.nextFloat() * (maxCoord.y - minCoord.y + 1);
    					particleZ = minCoord.z + par5Random.nextFloat() * (maxCoord.z - minCoord.z + 1);
                        world.spawnParticle(BigReactors.isValentinesDay ? "heart" : "cloud", particleX, particleY, particleZ,
                        		par5Random.nextFloat() * inwardsDir.offsetX,
                        		par5Random.nextFloat() * inwardsDir.offsetY,
                        		par5Random.nextFloat() * inwardsDir.offsetZ);
    				}
    			}
    		}
    	}
    }
    
    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z)
    {
    	int metadata = world.getBlockMetadata(x, y, z);
    	if(metadata == METADATA_BEARING) {
        	TileEntity te = world.getBlockTileEntity(x, y, z);
        	if(te instanceof TileEntityTurbineRotorBearing) {
        		TileEntityTurbineRotorBearing bearing = (TileEntityTurbineRotorBearing)te;
        		if(bearing.isConnected() && bearing.getTurbine().isActive()) {
        			return bearing.getAABB();
        		}
        	}
    	}
    	
		return super.getCollisionBoundingBoxFromPool(world, x, y, z);
    }

	@Override
	public IPeripheral getPeripheral(World world, int x, int y, int z, int side) {
		TileEntity te = world.getBlockTileEntity(x, y, z);
		
		if(te instanceof TileEntityTurbineComputerPort)
			return (IPeripheral)te;
		
		return null;
	}
	
	@Override
    public boolean canCreatureSpawn(EnumCreatureType type, World world, int x, int y, int z)
    {
		return false;
    }
}

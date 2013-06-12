package erogenousbeef.bigreactors.common.block;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import erogenousbeef.bigreactors.common.BRLoader;
import erogenousbeef.bigreactors.common.BRUtilities;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.tileentity.TileEntityCyaniteReprocessor;
import erogenousbeef.bigreactors.common.tileentity.base.TileEntityBeefBase;
import erogenousbeef.bigreactors.common.tileentity.base.TileEntityInventory;
import erogenousbeef.bigreactors.common.tileentity.base.TileEntityPoweredInventory;
import erogenousbeef.bigreactors.common.tileentity.base.TileEntityPoweredInventoryLiquid;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.ITankContainer;
import net.minecraftforge.liquids.LiquidContainerRegistry;

public class BlockBRSmallMachine extends BlockContainer {

	private static String[] _subBlocks = new String[] { "cyaniteReprocessor" };
	private Icon[] _icons = new Icon[_subBlocks.length];
	private Icon[] _activeIcons = new Icon[_subBlocks.length];
	private Icon[] _inventorySideIcons = new Icon[3];
	private Icon[] _liquidSideIcons = new Icon[1];
	
	protected static Icon powerIcon = null; // find a better home for this.
	
	public BlockBRSmallMachine(int id, Material material) {
		super(id, material);
		setStepSound(soundMetalFootstep);
		setHardness(1.0f);
		setUnlocalizedName("blockBRSmallMachine");
		setCreativeTab(BigReactors.TAB);
	}
	
	public static Icon getPowerIcon() {
		return powerIcon;
	}

	@Override
	public Icon getBlockTexture(IBlockAccess blockAccess, int x, int y, int z, int side)
	{
		TileEntity te = blockAccess.getBlockTileEntity(x, y, z);
		if(te instanceof TileEntityBeefBase)
		{
			if(side == ((TileEntityBeefBase)te).getFacingDirection().ordinal()) {
				int metadata = blockAccess.getBlockMetadata(x, y, z);
				if(te instanceof TileEntityPoweredInventory) {
					if(((TileEntityPoweredInventory)te).isActive()) {
						return _activeIcons[metadata];
					}
				}
				return _icons[metadata];
			}
		}
		
		if(te instanceof TileEntityInventory) {
			int[] slots = ((TileEntityInventory)te).getAccessibleSlotsFromSide(side);
			if(slots != null && slots[0] != TileEntityInventory.INVENTORY_UNEXPOSED) {
				return _inventorySideIcons[slots[0]];
			}
		}
		
		if(te instanceof TileEntityPoweredInventoryLiquid) {
			if(((TileEntityPoweredInventoryLiquid)te).getTank(ForgeDirection.getOrientation(side), null) != null) {
				return _liquidSideIcons[0];
			}
		}

		return this.blockIcon;
	}	
	
	@Override
	public Icon getIcon(int side, int metadata)
	{
		// This is used when rendering in-inventory. 4 == front here.
		if(side == 4) {
			return _icons[metadata];
		}
		return this.blockIcon;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister par1IconRegister)
	{
		this.blockIcon = par1IconRegister.registerIcon(BigReactors.TEXTURE_NAME_PREFIX + getUnlocalizedName());
		
		for(int i = 0; i < _subBlocks.length; ++i) {
			_icons[i] = par1IconRegister.registerIcon(BigReactors.TEXTURE_NAME_PREFIX + getUnlocalizedName() + "." + _subBlocks[i]);
			_activeIcons[i] = par1IconRegister.registerIcon(BigReactors.TEXTURE_NAME_PREFIX + getUnlocalizedName() + "." + _subBlocks[i] + ".active");
		}
		
		_inventorySideIcons[0] = par1IconRegister.registerIcon(BigReactors.TEXTURE_NAME_PREFIX + getUnlocalizedName() + ".redPort");
		_inventorySideIcons[1] = par1IconRegister.registerIcon(BigReactors.TEXTURE_NAME_PREFIX + getUnlocalizedName() + ".greenPort");
		_inventorySideIcons[2] = par1IconRegister.registerIcon(BigReactors.TEXTURE_NAME_PREFIX + getUnlocalizedName() + ".bluePort");

		// TODO: Better icons for these
		_liquidSideIcons[0] = par1IconRegister.registerIcon(BigReactors.TEXTURE_NAME_PREFIX + getUnlocalizedName() + ".openPort");
		
		// Ugly hack, fix later.
		powerIcon = par1IconRegister.registerIcon(BigReactors.TEXTURE_NAME_PREFIX + "gui.power");
	}
	
	@Override
	public TileEntity createNewTileEntity(World world) {
		return null;
	}

	@Override
	public TileEntity createTileEntity(World world, int metadata) {
		return new TileEntityCyaniteReprocessor();
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

	public ItemStack getCyaniteReprocessorItemStack() {
		return new ItemStack(this.blockID, 1, 0);
	}
	
	@Override
	public void getSubBlocks(int par1, CreativeTabs par2CreativeTabs, List par3List)
	{
		par3List.add(this.getCyaniteReprocessorItemStack());
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float hitX, float hitY, float hitZ) {
		TileEntity te = world.getBlockTileEntity(x, y, z);
		if(te == null) { return false; }

		if(entityPlayer.isSneaking()) {
			// Empty-handed sneaking also works to rotate the machine, because it's easier on pre-wrench players.
			if(entityPlayer.inventory.getCurrentItem() == null && te instanceof TileEntityBeefBase) {
				ForgeDirection newFacing = getDirectionFacingEntity(entityPlayer);
				((TileEntityBeefBase)te).rotateTowards(newFacing);
				return true;
			}
			
			return false;
		}

		// WRENCH SUPPORT HAH.
		if(te instanceof TileEntityBeefBase && BRUtilities.isPlayerHoldingWrench(entityPlayer)) {
			ForgeDirection newFacing = getDirectionFacingEntity(entityPlayer);
			((TileEntityBeefBase)te).rotateTowards(newFacing);
			return true;
		}

		// Handle buckets
		if(te instanceof ITankContainer && LiquidContainerRegistry.isEmptyContainer(entityPlayer.inventory.getCurrentItem()))
		{
			if(BRUtilities.fillBucketFromTank((ITankContainer)te, entityPlayer))
			{
				return true;
			}
		}
		else if(te instanceof ITankContainer && LiquidContainerRegistry.isFilledContainer(entityPlayer.inventory.getCurrentItem()))
		{
			if(BRUtilities.fillTankFromBucket((ITankContainer)te, entityPlayer))
			{
				return true;
			}
		}
		
		// Show GUI
		if(te instanceof TileEntityBeefBase) {
			entityPlayer.openGui(BRLoader.instance, 0, world, x, y, z);
			return true;
		}
		
		return false;
	}
	
	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLiving entity, ItemStack stack) {
		if(entity == null) { return; }
		
		TileEntity te = world.getBlockTileEntity(x, y, z);
		
		// ???
		if(stack.getTagCompound() != null)
		{
			stack.getTagCompound().setInteger("x", x);
			stack.getTagCompound().setInteger("y", y);
			stack.getTagCompound().setInteger("z", z);
			te.readFromNBT(stack.getTagCompound());
		}
		
		if(te != null && te instanceof TileEntityBeefBase) {
			ForgeDirection newFacing = getDirectionFacingEntity(entity);
			((TileEntityBeefBase)te).rotateTowards(newFacing);
		}
	}
	
	protected ForgeDirection getDirectionFacingEntity(Entity entity) {
		int facingAngle = (MathHelper.floor_double((entity.rotationYaw * 4F) / 360F + 0.5D) & 3);
		switch(facingAngle) {
		case 1:
			return ForgeDirection.EAST;
		case 2:
			return ForgeDirection.SOUTH;
		case 3:
			return ForgeDirection.WEST;
		default:
			return ForgeDirection.NORTH;
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

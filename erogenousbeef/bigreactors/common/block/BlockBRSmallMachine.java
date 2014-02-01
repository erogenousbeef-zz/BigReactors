package erogenousbeef.bigreactors.common.block;

import java.util.List;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
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
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import erogenousbeef.bigreactors.common.BRLoader;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.tileentity.TileEntityCyaniteReprocessor;
import erogenousbeef.bigreactors.common.tileentity.base.TileEntityBeefBase;
import erogenousbeef.bigreactors.common.tileentity.base.TileEntityInventory;
import erogenousbeef.bigreactors.common.tileentity.base.TileEntityPoweredInventory;
import erogenousbeef.bigreactors.common.tileentity.base.TileEntityPoweredInventoryFluid;
import erogenousbeef.bigreactors.utils.StaticUtils;

public class BlockBRSmallMachine extends BlockContainer {

	public static final int META_CYANITE_REPROCESSOR = 0;
	
	public static final String[] _subBlocks = {
		"cyaniteReprocessor"
	};
	
	private Icon[] _icons = new Icon[_subBlocks.length];
	private Icon[] _activeIcons = new Icon[_subBlocks.length];
	private Icon[] _inventorySideIcons = new Icon[3];
	private Icon[] _fluidSideIcons = new Icon[2];
	
	public BlockBRSmallMachine(int id, Material material) {
		super(id, material);
		setStepSound(soundMetalFootstep);
		setHardness(1.0f);
		setUnlocalizedName("blockBRSmallMachine");
		setTextureName(BigReactors.TEXTURE_NAME_PREFIX + "blockBRSmallMachine");
		setCreativeTab(BigReactors.TAB);
	}
	
	public Icon getIconFromTileEntity(TileEntity te, int metadata, int side) {
		if(metadata < 0) { return blockIcon; }

		if(te instanceof TileEntityBeefBase)
		{
			if(side == ((TileEntityBeefBase)te).getFacingDirection().ordinal()) {
				if(te instanceof TileEntityPoweredInventory) {
					if(((TileEntityPoweredInventory)te).isActive()) {
						if(metadata >= _activeIcons.length) {
							return blockIcon;
						}
						else {
							return _activeIcons[metadata];
						}
					}
				}
				
				if(metadata >= _icons.length) {
					return blockIcon;
				}
				else {
					return _icons[metadata];
				}
			}
		}
		
		if(te instanceof TileEntityInventory) {
			int[] slots = ((TileEntityInventory)te).getAccessibleSlotsFromSide(side);
			if(slots != null && slots.length > 0 && slots[0] != TileEntityInventory.INVENTORY_UNEXPOSED) {
				return _inventorySideIcons[slots[0]];
			}
		}
		
		if(te instanceof TileEntityPoweredInventoryFluid) {
			// TODO: Fix.
			TileEntityPoweredInventoryFluid fluidTe = (TileEntityPoweredInventoryFluid)te;
			int tank = fluidTe.getExposedTankFromReferenceSide(ForgeDirection.getOrientation(fluidTe.getRotatedSide(side)));
			if(tank != -1) {
				return _fluidSideIcons[tank];
			}
		}

		return this.blockIcon;
	}
	
	@Override
	public Icon getBlockTexture(IBlockAccess blockAccess, int x, int y, int z, int side)
	{
		TileEntity te = blockAccess.getBlockTileEntity(x, y, z);
		int metadata = blockAccess.getBlockMetadata(x, y, z);
		return this.getIconFromTileEntity(te, metadata, side);
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
		
		// TODO: Better icons for these
		_inventorySideIcons[0] = par1IconRegister.registerIcon(BigReactors.TEXTURE_NAME_PREFIX + getUnlocalizedName() + ".redPort");
		_inventorySideIcons[1] = par1IconRegister.registerIcon(BigReactors.TEXTURE_NAME_PREFIX + getUnlocalizedName() + ".greenPort");
		_inventorySideIcons[2] = par1IconRegister.registerIcon(BigReactors.TEXTURE_NAME_PREFIX + getUnlocalizedName() + ".openPort");

		_fluidSideIcons[0] = par1IconRegister.registerIcon(BigReactors.TEXTURE_NAME_PREFIX + getUnlocalizedName() + ".bluePort");
		_fluidSideIcons[1] = par1IconRegister.registerIcon(BigReactors.TEXTURE_NAME_PREFIX + getUnlocalizedName() + ".fluidTank2");
	}
	
	@Override
	public TileEntity createNewTileEntity(World world) {
		return null;
	}

	@Override
	public TileEntity createTileEntity(World world, int metadata) {
		switch(metadata) {
		case META_CYANITE_REPROCESSOR:
			return new TileEntityCyaniteReprocessor();
		default:
			throw new IllegalArgumentException("Unknown metadata for tile entity");
		}
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
		return new ItemStack(this.blockID, 1, META_CYANITE_REPROCESSOR);
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
		if(te instanceof TileEntityBeefBase && StaticUtils.Inventory.isPlayerHoldingWrench(entityPlayer)) {
			ForgeDirection newFacing = getDirectionFacingEntity(entityPlayer);
			((TileEntityBeefBase)te).rotateTowards(newFacing);
			return true;
		}

		// Handle buckets
		if(te instanceof IFluidHandler && FluidContainerRegistry.isEmptyContainer(entityPlayer.inventory.getCurrentItem()))
		{
			IFluidHandler fluidHandler = (IFluidHandler)te;
			FluidTankInfo[] infoz = fluidHandler.getTankInfo(ForgeDirection.UNKNOWN);
			for(FluidTankInfo info : infoz) {
				if(StaticUtils.Fluids.fillContainerFromTank(world, fluidHandler, entityPlayer, info.fluid)) {
					return true;
				}
			}
		}
		else if(te instanceof IFluidHandler && FluidContainerRegistry.isFilledContainer(entityPlayer.inventory.getCurrentItem()))
		{
			if(StaticUtils.Fluids.fillTankWithContainer(world, (IFluidHandler)te, entityPlayer)) {
				return true;
			}
		}
		
		// Show GUI
		if(te instanceof TileEntityBeefBase) {
			if(!world.isRemote) {
				entityPlayer.openGui(BRLoader.instance, 0, world, x, y, z);
			}
			return true;
		}
		
		return false;
	}
	
	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack stack) {
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

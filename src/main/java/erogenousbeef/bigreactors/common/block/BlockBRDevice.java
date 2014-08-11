package erogenousbeef.bigreactors.common.block;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import cofh.api.tileentity.IReconfigurableFacing;
import cofh.core.block.BlockCoFHBase;
import cofh.lib.util.helpers.BlockHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import erogenousbeef.bigreactors.common.BRLoader;
import erogenousbeef.bigreactors.common.BRLog;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.interfaces.IBeefReconfigurableSides;
import erogenousbeef.bigreactors.common.interfaces.IWrenchable;
import erogenousbeef.bigreactors.common.tileentity.TileEntityCyaniteReprocessor;
import erogenousbeef.bigreactors.common.tileentity.base.TileEntityBeefBase;
import erogenousbeef.bigreactors.utils.StaticUtils;

public class BlockBRDevice extends BlockCoFHBase {

	public static final int META_CYANITE_REPROCESSOR = 0;
	
	public static final String[] _subBlocks = {
		"cyaniteReprocessor"
	};
	
	private IIcon[] _icons = new IIcon[_subBlocks.length];
	private IIcon[] _activeIcons = new IIcon[_subBlocks.length];
	
	public BlockBRDevice(Material material) {
		super(material);
		setStepSound(soundTypeMetal);
		setHardness(1.0f);
		setBlockName("blockBRDevice");
		setBlockTextureName(BigReactors.TEXTURE_NAME_PREFIX + "blockBRDevice");
		setCreativeTab(BigReactors.TAB);
	}
	
	public static final int SIDE_FRONT = ForgeDirection.NORTH.ordinal();

	private IIcon safeGetIcon(IIcon[] list, int idx, int x, int y, int z) {
		if(idx < 0 || idx >= list.length) {
			BRLog.info("Invalid metadata (%d) for block at %d, %d, %d!", idx, x, y, z);
			return blockIcon;
		}
		else {
			return list[idx];
		}
	}
	public IIcon getIconFromTileEntity(TileEntity te, int metadata, int side) {
		if(metadata < 0) { return blockIcon; }

		// Tracks the actual index of the current side, after rotation
		int front = -1;

		if(te instanceof IReconfigurableFacing) {
			IReconfigurableFacing teFacing = (IReconfigurableFacing)te;
			front = teFacing.getFacing();
		}
		
		if(side == front) {
			if(te instanceof TileEntityBeefBase) {
				TileEntityBeefBase beefTe = (TileEntityBeefBase)te;
				if(beefTe.isActive()) {
					return safeGetIcon(_activeIcons, metadata, te.xCoord, te.yCoord, te.zCoord);
				}
			}
			return safeGetIcon(_icons, metadata, te.xCoord, te.yCoord, te.zCoord);
		}
		
		if(te instanceof IBeefReconfigurableSides) {
			IBeefReconfigurableSides teSides = (IBeefReconfigurableSides)te;
			return teSides.getIconForSide(side);
		}

		return blockIcon;
	}
	
	@Override
	public IIcon getIcon(IBlockAccess blockAccess, int x, int y, int z, int side)
	{
		TileEntity te = blockAccess.getTileEntity(x, y, z);
		int metadata = blockAccess.getBlockMetadata(x, y, z);
		return this.getIconFromTileEntity(te, metadata, side);
	}
	
	@Override
	public IIcon getIcon(int side, int metadata)
	{
		// This is used when rendering in-inventory. 4 == front here.
		if(side == 4) {
			return _icons[metadata];
		}
		return this.blockIcon;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister par1IconRegister)
	{
		this.blockIcon = par1IconRegister.registerIcon(BigReactors.TEXTURE_NAME_PREFIX + getUnlocalizedName());
		
		for(int i = 0; i < _subBlocks.length; ++i) {
			_icons[i] = par1IconRegister.registerIcon(BigReactors.TEXTURE_NAME_PREFIX + getUnlocalizedName() + "." + _subBlocks[i]);
			_activeIcons[i] = par1IconRegister.registerIcon(BigReactors.TEXTURE_NAME_PREFIX + getUnlocalizedName() + "." + _subBlocks[i] + ".active");
		}
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		switch(metadata) {
		case META_CYANITE_REPROCESSOR:
			return new TileEntityCyaniteReprocessor();
		default:
			throw new IllegalArgumentException("Unknown metadata for tile entity");
		}
	}

	public ItemStack getCyaniteReprocessorItemStack() {
		return new ItemStack(this, 1, META_CYANITE_REPROCESSOR);
	}
	
	@Override
	public void getSubBlocks(Item par1, CreativeTabs par2CreativeTabs, List par3List)
	{
		par3List.add(this.getCyaniteReprocessorItemStack());
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float hitX, float hitY, float hitZ) {
		TileEntity te = world.getTileEntity(x, y, z);
		if(te == null) { return false; }

		if(entityPlayer.isSneaking()) {
			// Empty-handed sneaking also works to rotate the machine, because it's easier on pre-wrench players.
			if(entityPlayer.inventory.getCurrentItem() == null && te instanceof IReconfigurableFacing) {
				return ((IReconfigurableFacing)te).rotateBlock();
			}
			
			return false;
		}

		if(te instanceof IWrenchable && StaticUtils.Inventory.isPlayerHoldingWrench(entityPlayer)) {
			return ((IWrenchable)te).onWrench(entityPlayer, side);
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

	// IDismantleable
	@Override
	public ArrayList<ItemStack> dismantleBlock(EntityPlayer player, NBTTagCompound blockTag,
			World world, int x, int y, int z, boolean returnDrops, boolean unknown) {
		// TODO figure this out
		return null;
	}
	
	// IInitializer (unused)
	@Override
	public boolean initialize() {
		return false;
	}

	@Override
	public boolean postInit() {
		return false;
	}
}

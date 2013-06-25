package erogenousbeef.bigreactors.common.block;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import powercrystals.minefactoryreloaded.api.rednet.IConnectableRedNet;
import powercrystals.minefactoryreloaded.api.rednet.RedNetConnectionType;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import erogenousbeef.bigreactors.common.BRConfig;
import erogenousbeef.bigreactors.common.BRLoader;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.tileentity.TileEntityReactorAccessPort;
import erogenousbeef.bigreactors.common.tileentity.TileEntityReactorControlRod;
import erogenousbeef.bigreactors.common.tileentity.TileEntityReactorPart;
import erogenousbeef.bigreactors.common.tileentity.TileEntityReactorPowerTap;
import erogenousbeef.bigreactors.common.tileentity.TileEntityReactorRedNetPort;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

public class BlockReactorPart extends BlockContainer implements IConnectableRedNet {
	
	public static final int CASING_METADATA_BASE = 0;	// Requires 5 "block types" to do properly.
	public static final int CASING_CORNER = 1;
	public static final int CASING_CENTER = 2;
	public static final int CASING_VERTICAL = 3;
	public static final int CASING_EASTWEST = 4;
	public static final int CASING_NORTHSOUTH = 5;
	
	public static final int CONTROLLER_METADATA_BASE = 6; // Disabled, Idle, Active
	public static final int CONTROLLER_IDLE = 7;
	public static final int CONTROLLER_ACTIVE = 8;

	public static final int POWERTAP_METADATA_BASE 	 = 9; // Disconnected, Connected
	
	public static final int ACCESSPORT_INLET = 11;
	public static final int ACCESSPORT_OUTLET = 12;
	public static final int REDNETPORT = 13;
	
	private static String[] _subBlocks = new String[] { "casingDefault",
														"casingCorner",
														"casingCenter",
														"casingVertical",
														"casingEastWest",
														"casingNorthSouth",
														"controllerInactive",
														"controllerIdle",
														"controllerActive",
														"powerTapDisconnected",
														"powerTapConnected",
														"accessInlet",
														"accessOutlet",
														"redNetPort" };

	private Icon[] _icons = new Icon[_subBlocks.length];
	
	@SideOnly(Side.CLIENT)
	private Icon[] _redNetPortConfigIcons = new Icon[TileEntityReactorRedNetPort.CircuitType.values().length - 1];
	
	public static boolean isCasing(int metadata) { return metadata >= CASING_METADATA_BASE && metadata < CONTROLLER_METADATA_BASE; }
	public static boolean isController(int metadata) { return metadata >= CONTROLLER_METADATA_BASE && metadata < POWERTAP_METADATA_BASE; }
	public static boolean isPowerTap(int metadata) { return metadata >= POWERTAP_METADATA_BASE && metadata < ACCESSPORT_INLET; }
	public static boolean isAccessPort(int metadata) { return metadata >= ACCESSPORT_INLET && metadata < REDNETPORT; }
	public static boolean isRedNetPort(int metadata) { return metadata == REDNETPORT; }
	
	public BlockReactorPart(int id, Material material) {
		super(id, material);
		
		setStepSound(soundMetalFootstep);
		setHardness(1.0f);
		setUnlocalizedName("blockReactorPart");
		setCreativeTab(BigReactors.TAB);
	}

	@Override
	public Icon getIcon(int side, int metadata)
	{
		// Casing block
		switch(metadata) {
			case CASING_METADATA_BASE:
			case CASING_CORNER:
			case CASING_CENTER:
				return _icons[metadata];
			
			case CASING_VERTICAL:
				// Vertical block
				if(side == 0 || side == 1) {
					return _icons[CASING_METADATA_BASE];
				}
				else
				{
					return _icons[metadata];
				}
			case CASING_EASTWEST:
				// X-aligned block (e/w)
				if(side == 4 || side == 5) {
					return _icons[CASING_METADATA_BASE];
				}
				else {
					return _icons[metadata];
				}
			case CASING_NORTHSOUTH:
				// Z-aligned block (n/s)
				if(side == 2 || side == 3) {
					return _icons[CASING_METADATA_BASE];
				}
				else if(side == 4 || side == 5) {
					// I hate everything
					return _icons[CASING_EASTWEST];
				}
				else {
					return _icons[metadata];
				}
			
			default:
				if(side == 0 || side == 1) {
					return _icons[CASING_METADATA_BASE];
				}
				else {
					return _icons[metadata];
				}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister par1IconRegister)
	{
		this.blockIcon = par1IconRegister.registerIcon(BigReactors.TEXTURE_NAME_PREFIX + getUnlocalizedName());
		
		for(int i = 0; i < _subBlocks.length; ++i) {
			_icons[i] = par1IconRegister.registerIcon(BigReactors.TEXTURE_NAME_PREFIX + getUnlocalizedName() + "." + _subBlocks[i]);
		}
		
		// We do this to skip DISABLED
		TileEntityReactorRedNetPort.CircuitType[] circuitTypes = TileEntityReactorRedNetPort.CircuitType.values();
		for(int i = 1; i < circuitTypes.length; ++i) {
			_redNetPortConfigIcons[i - 1] = par1IconRegister.registerIcon(BigReactors.TEXTURE_NAME_PREFIX + "config.redNetPort." + circuitTypes[i].name());
		}
	}
	
	// We do this to skip DISABLED
	@SideOnly(Side.CLIENT)
	public Icon getRedNetConfigIcon(TileEntityReactorRedNetPort.CircuitType circuitType) {
		if(circuitType == TileEntityReactorRedNetPort.CircuitType.DISABLED) { return null; }
		else {
			return _redNetPortConfigIcons[circuitType.ordinal() - 1];
		}
	}

	@Override
	public TileEntity createNewTileEntity(World world) {
		// Uses the metadata-driven version for efficiency
		return null;
	}	
	
	@Override
	public TileEntity createTileEntity(World world, int metadata) {
		if(isPowerTap(metadata)) {
			return new TileEntityReactorPowerTap();
		}
		else if(isAccessPort(metadata)) {
			return new TileEntityReactorAccessPort();
		}
		else if(isRedNetPort(metadata)) {
			return new TileEntityReactorRedNetPort();
		}
		else {
			return new TileEntityReactorPart();
		}
	}
	
	@Override
	public void onBlockAdded(World world, int x, int y, int z) {
		TileEntity te = world.getBlockTileEntity(x, y, z);
		if(te != null && te instanceof TileEntityReactorPart) {
			TileEntityReactorPart rp = (TileEntityReactorPart)te;
			rp.onBlockAdded(world, x, y, z);
		}
	}
	
	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, int neighborBlockID) {
		TileEntity te = world.getBlockTileEntity(x, y, z);
		if(te != null && te instanceof TileEntityReactorPowerTap) {
			TileEntityReactorPowerTap tap = (TileEntityReactorPowerTap)te;
			tap.onNeighborBlockChange(world, x, y, z, neighborBlockID);
		}
		// TODO: Handle rednet connector?
	}
	
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9) {
		if(player.isSneaking()) {
			return false;
		}
		
		int metadata = world.getBlockMetadata(x, y, z);
		if(!isController(metadata) && !isAccessPort(metadata) && !isRedNetPort(metadata)) {
			return false;
		}
		
		// Machine isn't assembled yet...
		if(metadata == CONTROLLER_METADATA_BASE) { return false; }

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
		if (isCasing(metadata))
		{
			return CASING_METADATA_BASE;
		}
		else if (isController(metadata))
		{
			return CONTROLLER_METADATA_BASE;
		}
		else if(isPowerTap(metadata)) {
			return POWERTAP_METADATA_BASE;
		}
		else if(isAccessPort(metadata)) {
			return ACCESSPORT_INLET;
		}
		else if(isRedNetPort(metadata)) {
			return REDNETPORT;
		}
		else {
			return CASING_METADATA_BASE;
		}
	}
	
	public ItemStack getReactorCasingItemStack() {
		return new ItemStack(this.blockID, 1, CASING_METADATA_BASE);
	}
	
	public ItemStack getReactorControllerItemStack() {
		return new ItemStack(this.blockID, 1, CONTROLLER_METADATA_BASE);
	}
	
	public ItemStack getReactorPowerTapItemStack() {
		return new ItemStack(this.blockID, 1, POWERTAP_METADATA_BASE);
	}
	
	public ItemStack getAccessPortItemStack() {
		return new ItemStack(this.blockID, 1, ACCESSPORT_INLET);
	}
	
	public ItemStack getRedNetPortItemStack() {
		return new ItemStack(this.blockID, 1, REDNETPORT);
	}
	
	@Override
	public void getSubBlocks(int par1, CreativeTabs par2CreativeTabs, List par3List)
	{
		par3List.add(this.getReactorCasingItemStack());
		par3List.add(this.getReactorControllerItemStack());
		par3List.add(this.getReactorPowerTapItemStack());
		par3List.add(this.getAccessPortItemStack());
		par3List.add(this.getRedNetPortItemStack());
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
	
	// IConnectableRedNet

	@Override
	public RedNetConnectionType getConnectionType(World world, int x, int y,
			int z, ForgeDirection side) {
		TileEntity te = world.getBlockTileEntity(x, y, z);
		if(te instanceof TileEntityReactorRedNetPort) {
			return RedNetConnectionType.CableAll;
		}

		return RedNetConnectionType.None;
	}
	@Override
	public int[] getOutputValues(World world, int x, int y, int z,
			ForgeDirection side) {
		TileEntity te = world.getBlockTileEntity(x, y, z);
		if(te instanceof TileEntityReactorRedNetPort) {
			return ((TileEntityReactorRedNetPort)te).getOutputValues();
		}
		else {
			int[] values = new int[16];
			for(int i = 0; i < 16; i++) {
				values[i] = 0;
			}
			return values;
		}
	}
	
	// Never used. we're always in "all" mode.
	@Override
	public int getOutputValue(World world, int x, int y, int z,
			ForgeDirection side, int subnet) {
		return 0;
	}
	
	
	@Override
	public void onInputsChanged(World world, int x, int y, int z,
			ForgeDirection side, int[] inputValues) {
		TileEntity te = world.getBlockTileEntity(x, y, z);
		if(te instanceof TileEntityReactorRedNetPort) {
			// TODO: This.
			return;
		}
		
		return;
	}

	// Never used, we're always in "all" mode.
	@Override
	public void onInputChanged(World world, int x, int y, int z,
			ForgeDirection side, int inputValue) {
		System.err.println("[BlockReactorPart] - Unused function: onInputChanged with value " + Integer.toString(inputValue));
	}	
}

package erogenousbeef.bigreactors.common.block;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import erogenousbeef.bigreactors.common.BRConfig;
import erogenousbeef.bigreactors.common.BRLoader;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.tileentity.TileEntityReactorAccessPort;
import erogenousbeef.bigreactors.common.tileentity.TileEntityReactorPart;
import erogenousbeef.bigreactors.common.tileentity.TileEntityReactorPowerTap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.World;

public class BlockReactorPart extends BlockContainer {
	
	public static final int CASING_METADATA_BASE = 0;	// Requires 5 "block types" to do properly.
	public static final int CASING_CORNER = 1;
	public static final int CASING_CENTER = 2;
	public static final int CASING_VERTICAL = 3;
	public static final int CASING_EASTWEST = 4;
	public static final int CASING_NORTHSOUTH = 5;
	
	public static final int CONTROLLER_METADATA_BASE = 6; // Disabled, Idle, Active
	public static final int CONTROLLER_IDLE = 7;
	public static final int CONTROLLER_ACTIVE = 8;

	public static final int CONTROLROD_METADATA_BASE = 9; // Inserted, Retracted
	public static final int POWERTAP_METADATA_BASE 	 = 11; // Disconnected, Connected
	
	public static final int ACCESSPORT_INLET = 13;
	public static final int ACCESSPORT_OUTLET = 14;
	
	private static String[] _subBlocks = new String[] { "casingDefault",
														"casingCorner",
														"casingCenter",
														"casingVertical",
														"casingEastWest",
														"casingNorthSouth",
														"controllerInactive",
														"controllerIdle",
														"controllerActive",
														"controlRodInserted",
														"controlRodRetracted",
														"powerTapDisconnected",
														"powerTapConnected",
														"accessInlet",
														"accessOutlet"};
	private Icon[] _icons = new Icon[_subBlocks.length];
	
	public static boolean isCasing(int metadata) { return metadata >= CASING_METADATA_BASE && metadata < CONTROLLER_METADATA_BASE; }
	public static boolean isController(int metadata) { return metadata >= CONTROLLER_METADATA_BASE && metadata < CONTROLROD_METADATA_BASE; }
	public static boolean isControlRod(int metadata) { return metadata >= CONTROLROD_METADATA_BASE && metadata < POWERTAP_METADATA_BASE; }
	public static boolean isPowerTap(int metadata) { return metadata >= POWERTAP_METADATA_BASE && metadata < ACCESSPORT_INLET; }
	public static boolean isAccessPort(int metadata) { return metadata >= ACCESSPORT_INLET; }
	
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
			
			case CONTROLROD_METADATA_BASE:
			case CONTROLROD_METADATA_BASE+1:
				if(side == 1) {
					return _icons[metadata];
				}
				else {
					return _icons[CASING_METADATA_BASE];
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
		// TODO: Handle connections to power conduits and shit.
		TileEntity te = world.getBlockTileEntity(x, y, z);
		if(te != null && te instanceof TileEntityReactorPowerTap) {
			TileEntityReactorPowerTap tap = (TileEntityReactorPowerTap)te;
			tap.onNeighborBlockChange(world, x, y, z, neighborBlockID);
		}
	}
	
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9) {
		if(player.isSneaking()) {
			return false;
		}
		
		int metadata = world.getBlockMetadata(x, y, z);
		if(!isController(metadata) && !isAccessPort(metadata)) {
			return false;
		}
		
		// Machine isn't assembled yet...
		if(metadata == CONTROLLER_METADATA_BASE) { return false; }

		// TODO: Fixme. Causes an NPE.
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
		if (isCasing(metadata))
		{
			return CASING_METADATA_BASE;
		}
		else if (isController(metadata))
		{
			return CONTROLLER_METADATA_BASE;
		}
		else if(isControlRod(metadata))
		{
			return CONTROLROD_METADATA_BASE;
		}
		else if(isPowerTap(metadata)) {
			return POWERTAP_METADATA_BASE;
		}
		else if(isAccessPort(metadata)) {
			return ACCESSPORT_INLET;
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
	
	public ItemStack getReactorControlRodItemStack() {
		return new ItemStack(this.blockID, 1, CONTROLROD_METADATA_BASE);
	}

	public ItemStack getReactorPowerTapItemStack() {
		return new ItemStack(this.blockID, 1, POWERTAP_METADATA_BASE);
	}
	
	public ItemStack getAccessPortItemStack() {
		return new ItemStack(this.blockID, 1, ACCESSPORT_INLET);
	}
	
	@Override
	public void getSubBlocks(int par1, CreativeTabs par2CreativeTabs, List par3List)
	{
		par3List.add(this.getReactorCasingItemStack());
		par3List.add(this.getReactorControllerItemStack());
		par3List.add(this.getReactorControlRodItemStack());
		par3List.add(this.getReactorPowerTapItemStack());
		par3List.add(this.getAccessPortItemStack());
	}
}

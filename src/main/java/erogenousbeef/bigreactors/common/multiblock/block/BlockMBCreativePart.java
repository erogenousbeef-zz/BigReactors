package erogenousbeef.bigreactors.common.multiblock.block;

import java.util.List;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.multiblock.tileentity.creative.TileEntityReactorCreativeCoolantPort;
import erogenousbeef.bigreactors.common.multiblock.tileentity.creative.TileEntityTurbineCreativeSteamGenerator;
import erogenousbeef.bigreactors.utils.StaticUtils;
import erogenousbeef.core.multiblock.MultiblockControllerBase;
import erogenousbeef.core.multiblock.rectangular.RectangularMultiblockTileEntityBase;

public class BlockMBCreativePart extends BlockContainer {

	public static final int REACTOR_CREATIVE_COOLANT_PORT = 0;
	public static final int TURBINE_CREATIVE_FLUID_PORT = 1;
	
	private static String[] subBlocks = new String[] { "reactor.coolantPort", "turbine.fluidPort" };
	private static String[] subIconNames = new String[] { "reactor.coolantPort.outlet" };
	
	private static final int SUBICON_CREATIVE_COOLANT_OUTLET = 0;
	
	private IIcon[] icons = new IIcon[subBlocks.length];
	private IIcon[] subIcons = new IIcon[subIconNames.length];
	
	public BlockMBCreativePart(Material material) {
		super(material);

		setStepSound(soundTypeMetal);
		setHardness(1.0f);
		setBlockName("blockMBCreativePart");
		this.setBlockTextureName(BigReactors.TEXTURE_NAME_PREFIX + "blockMBCreativePart");
		setCreativeTab(BigReactors.TAB);
	}

	@Override
    public IIcon getIcon(IBlockAccess blockAccess, int x, int y, int z, int side) {
		int metadata = blockAccess.getBlockMetadata(x, y, z);
		TileEntity te = blockAccess.getTileEntity(x, y, z);
		
		if(te instanceof RectangularMultiblockTileEntityBase) {
			RectangularMultiblockTileEntityBase rte = (RectangularMultiblockTileEntityBase)te;
			MultiblockControllerBase controller = rte.getMultiblockController();
			if(controller != null && controller.isAssembled()) {
				if(rte.getOutwardsDir().ordinal() == side) {
					return getIconFromTileEntity(rte, metadata);
				}
			}
		}
		
		return getIcon(side, metadata);
	}
	
	@Override
	public IIcon getIcon(int side, int metadata) {
		if(side == 0 || side == 1) { return blockIcon; }
		metadata = metadata % icons.length;
		return icons[metadata];
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister par1IconRegister)
	{
		this.blockIcon = par1IconRegister.registerIcon(BigReactors.TEXTURE_NAME_PREFIX + getUnlocalizedName());
		
		for(int i = 0; i < subBlocks.length; ++i) {
			icons[i] = par1IconRegister.registerIcon(BigReactors.TEXTURE_NAME_PREFIX + getUnlocalizedName() + "." + subBlocks[i]);
		}
		
		for(int i = 0; i < subIconNames.length; ++i) {
			subIcons[i] = par1IconRegister.registerIcon(BigReactors.TEXTURE_NAME_PREFIX + getUnlocalizedName() + "." + subIconNames[i]);
		}
	}
	
	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		switch(metadata) {
			case REACTOR_CREATIVE_COOLANT_PORT:
				return new TileEntityReactorCreativeCoolantPort();
			case TURBINE_CREATIVE_FLUID_PORT:
				return new TileEntityTurbineCreativeSteamGenerator();
			default:
				return null;
		}
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9) {
		if(player.isSneaking()) {
			return false;
		}

		ItemStack currentEquippedItem = player.getCurrentEquippedItem();
		
		TileEntity te = world.getTileEntity(x, y, z);
		if(te instanceof TileEntityReactorCreativeCoolantPort) {
			TileEntityReactorCreativeCoolantPort cp = (TileEntityReactorCreativeCoolantPort)te;
			if(currentEquippedItem == null || StaticUtils.Inventory.isPlayerHoldingWrench(player)) {
				// Use wrench to change inlet/outlet state
				cp.setInlet(!cp.isInlet());
			}
			else {
				cp.forceAddWater();
			}
			return true;
		}
		
		return false;
	}
	
	@Override
	public int damageDropped(int metadata)
	{
		return metadata;
	}

	public ItemStack getReactorCoolantPort() {
		return new ItemStack(this, 1, REACTOR_CREATIVE_COOLANT_PORT);
	}
	
	public ItemStack getTurbineFluidPort() {
		return new ItemStack(this, 1, TURBINE_CREATIVE_FLUID_PORT);
	}

	@Override
	public void getSubBlocks(Item par1, CreativeTabs par2CreativeTabs, List par3List)
	{
		par3List.add(getReactorCoolantPort());
		par3List.add(getTurbineFluidPort());
	}

	private IIcon getIconFromTileEntity(RectangularMultiblockTileEntityBase rte, int metadata) {
		if(rte instanceof TileEntityReactorCreativeCoolantPort) {
			if(!((TileEntityReactorCreativeCoolantPort)rte).isInlet()) {
				return subIcons[SUBICON_CREATIVE_COOLANT_OUTLET];
			}
		}

		metadata = metadata % icons.length;
		return icons[metadata];
	}
	
	@Override
    public boolean canCreatureSpawn(EnumCreatureType type, IBlockAccess world, int x, int y, int z)
    {
		return false;
    }
}

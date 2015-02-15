package erogenousbeef.bigreactors.common.multiblock.block;

import java.util.List;

import cpw.mods.fml.common.Optional;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.multiblock.MultiblockHeatExchanger;
import erogenousbeef.bigreactors.common.multiblock.interfaces.INeighborUpdatableEntity;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityExchangerComputerPort;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityExchangerFluidPort;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityExchangerPart;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityExchangerPartBase;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorAccessPort;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorComputerPort;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorControlRod;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorCoolantPort;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorPart;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorPowerTap;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorRedNetPort;
import erogenousbeef.bigreactors.utils.StaticUtils;
import erogenousbeef.core.multiblock.MultiblockControllerBase;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

@Optional.InterfaceList({
	@Optional.Interface(iface = "dan200.computercraft.api.peripheral.IPeripheralProvider", modid = "ComputerCraft"),
})
public class BlockExchangerPart extends BlockContainer implements IPeripheralProvider {

	public final static int METADATA_CASING = 0;
	public final static int METADATA_CONTROLLER = 1;
	public final static int METADATA_FLUIDPORT = 2;
	public final static int METADATA_COMPUTERPORT = 3;
	
	private static final int CONTROLLER_OFF = 0;
	private static final int CONTROLLER_IDLE = 1;
	private static final int CONTROLLER_ACTIVE = 2;
	
	private static final int FLUIDPORT_PRIM_INLET = 0;
	private static final int FLUIDPORT_PRIM_OUTLET = 1;
	private static final int FLUIDPORT_SEC_INLET = 2;
	private static final int FLUIDPORT_SEC_OUTLET = 3;
	
	private final static String[] _subBlocks = {
			"casing",
			"controller",
			"fluidPort",
			"computerPort"
	};

	private static String[][] _states = new String[][] {
		{"default", "face", "corner", "eastwest", "northsouth", "vertical"}, // Casing
		{"off", "idle", "active"}, 		// Controller
		{"primaryInlet", "primaryOutlet", "secondaryInlet", "secondaryOutlet"},	// Fluid Port
		{"default"},					// Computer Port
	};
	
	public static final boolean isCasing(int metadata) { return metadata == METADATA_CASING; }
	public static final boolean isController(int metadata) { return metadata == METADATA_CONTROLLER; }
	public static final boolean isFluidPort(int metadata) { return metadata == METADATA_FLUIDPORT; }
	public static final boolean isComputerPort(int metadata) { return metadata == METADATA_COMPUTERPORT; }

	private IIcon[][] _icons = new IIcon[_states.length][];
	
	public BlockExchangerPart(Material material) {
		super(material);
		setStepSound(soundTypeMetal);
		setHardness(2.0f);
		setBlockName("blockExchangerPart");
		this.setBlockTextureName(BigReactors.TEXTURE_NAME_PREFIX + "blockExchangerPart");
		setCreativeTab(BigReactors.TAB);
	}

	@Override
    public IIcon getIcon(IBlockAccess blockAccess, int x, int y, int z, int side) {
		IIcon icon = null;
		int metadata = blockAccess.getBlockMetadata(x,y,z);
		
		switch(metadata) {
			case METADATA_CASING:
				icon = getCasingIcon(blockAccess, x, y, z, side);
				break;
			case METADATA_CONTROLLER:
				icon = getControllerIcon(blockAccess, x, y, z, side);
				break;
			case METADATA_FLUIDPORT:
				icon = getFluidPortIcon(blockAccess, x, y, z, side);
				break;
			case METADATA_COMPUTERPORT:
				icon = getFaceOrBlockIcon(blockAccess, x, y, z, side, metadata);
				break;
		}
		return icon != null ? icon : getIcon(side, metadata);
	}
	
	@Override
	public IIcon getIcon(int side, int metadata)
	{
		if(side > 1 && (metadata >= 0 && metadata < _icons.length)) {
			return _icons[metadata][0];
		}
		return blockIcon;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister par1IconRegister)
	{
		String prefix = BigReactors.TEXTURE_NAME_PREFIX + getUnlocalizedName() + ".";

		for(int metadata = 0; metadata < _states.length; ++metadata) {
			String[] blockStates = _states[metadata];
			_icons[metadata] = new IIcon[blockStates.length];

			for(int state = 0; state < blockStates.length; state++) {
				_icons[metadata][state] = par1IconRegister.registerIcon(prefix + _subBlocks[metadata] + "." + blockStates[state]);
			}
		}
		
		this.blockIcon = _icons[0][0];
	}	
	
	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		switch(metadata) {
			case METADATA_COMPUTERPORT:
				return new TileEntityExchangerComputerPort();
			case METADATA_FLUIDPORT:
				return new TileEntityExchangerFluidPort();
			default:
				return new TileEntityExchangerPart();
		}
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block neighborBlock) {
		TileEntity te = StaticUtils.TE.getTileEntityUnsafe(world, x, y, z);

		// Signal power taps when their neighbors change, etc.
		if(te instanceof INeighborUpdatableEntity) {
			((INeighborUpdatableEntity)te).onNeighborBlockChange(world, x, y, z, neighborBlock);
		}
	}

	@Override
	public void onNeighborChange(IBlockAccess world, int x, int y, int z, int neighborX, int neighborY, int neighborZ) {
		TileEntity te = StaticUtils.TE.getTileEntityUnsafe(world, x, y, z);

		// Signal power taps when their neighbors change, etc.
		if(te instanceof INeighborUpdatableEntity) {
			((INeighborUpdatableEntity)te).onNeighborTileChange(world, x, y, z, neighborX, neighborY, neighborZ);
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
	
	@Override
	public void getSubBlocks(Item par1, CreativeTabs par2CreativeTabs, List par3List)
	{
		for(int metadata = 0; metadata < _subBlocks.length; metadata++) {
			par3List.add(new ItemStack(this, 1, metadata));
		}
	}
	
	@Override
	public void breakBlock(World world, int x, int y, int z, Block block, int meta)
	{
		// Drop everything inside inventory blocks
		TileEntity te = world.getTileEntity(x, y, z);
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
					EntityItem entityitem = new EntityItem(world, (float)x + xOffset, (float)y + yOffset, (float)z + zOffset, new ItemStack(itemstack.getItem(), amountToDrop, itemstack.getItemDamage()));
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

		super.breakBlock(world, x, y, z, block, meta);
	}
	
	@Override
    public boolean canCreatureSpawn(EnumCreatureType type, IBlockAccess world, int x, int y, int z)
    {
		return false;
    }
	
	// IPeripheralProvider
	@Optional.Method(modid ="ComputerCraft")
	@Override
	public IPeripheral getPeripheral(World world, int x, int y, int z, int side) {
		TileEntity te = world.getTileEntity(x, y, z);
		
		if(te instanceof TileEntityExchangerComputerPort)
			return (IPeripheral)te;
		
		return null;
	}
	
	//// Icon selection code ////
	private IIcon getFluidPortIcon(IBlockAccess blockAccess, int x, int y, int z, int side) {
		TileEntity te = blockAccess.getTileEntity(x, y, z);
		if(te instanceof TileEntityExchangerFluidPort) {
			TileEntityExchangerFluidPort port = (TileEntityExchangerFluidPort)te;
			
			if(!isAssembled(port) || isOutwardsSide(port, side)) {
				switch(port.getPortDirection()) {
					case PrimaryInlet:
						return _icons[METADATA_FLUIDPORT][FLUIDPORT_PRIM_INLET];
					case PrimaryOutlet:
						return _icons[METADATA_FLUIDPORT][FLUIDPORT_PRIM_OUTLET];
					case SecondaryInlet:
						return _icons[METADATA_FLUIDPORT][FLUIDPORT_SEC_INLET];
					case SecondaryOutlet:
						return _icons[METADATA_FLUIDPORT][FLUIDPORT_SEC_OUTLET];
					default:
						return blockIcon;
				}
			}
		}
		return blockIcon;
	}
	
	private IIcon getControllerIcon(IBlockAccess blockAccess, int x, int y, int z, int side) {
		TileEntity te = blockAccess.getTileEntity(x, y, z);
		if(te instanceof TileEntityExchangerPartBase) {
			TileEntityExchangerPartBase part = (TileEntityExchangerPartBase)te;
			MultiblockHeatExchanger exchanger = part.getExchangerController();
			if(exchanger == null || !exchanger.isAssembled()) {
				return _icons[METADATA_CONTROLLER][CONTROLLER_OFF];
			}
			else if(exchanger.getActive()) {
				return _icons[METADATA_CONTROLLER][CONTROLLER_ACTIVE];
			}
			else {
				return _icons[METADATA_CONTROLLER][CONTROLLER_IDLE];
			}
		}
		return blockIcon;
	}
	
	private IIcon getFaceOrBlockIcon(IBlockAccess blockAccess, int x, int y, int z, int side, int metadata) {
		TileEntity te = blockAccess.getTileEntity(x, y, z);
		if(te instanceof TileEntityExchangerPartBase) {
			TileEntityExchangerPartBase part = (TileEntityExchangerPartBase)te;
			if(!isAssembled(part) || isOutwardsSide(part, side)) {
				return _icons[metadata][0];
			}
		}
		return blockIcon;
	}
	
	private IIcon getCasingIcon(IBlockAccess blockAccess, int x, int y, int z, int side) {
		// TODO
		return blockIcon;
	}

	private boolean isOutwardsSide(TileEntityExchangerPartBase part, int side) {
		ForgeDirection outDir = part.getOutwardsDir();
		return outDir.ordinal() == side;
	}
	
	private boolean isAssembled(TileEntityExchangerPartBase part) {
		MultiblockControllerBase exchanger = part.getMultiblockController();
		return exchanger != null && exchanger.isAssembled();
	}
	
	public ItemStack getItemStack(String name) {
		for(int i = 0; i < _subBlocks.length; i++) {
			if(_subBlocks[i].equals(name)) {
				return new ItemStack(this, 1, i);
			}
		}
		
		throw new IllegalArgumentException("Unable to find Heat Exchanger part with name " + name);
	}
}

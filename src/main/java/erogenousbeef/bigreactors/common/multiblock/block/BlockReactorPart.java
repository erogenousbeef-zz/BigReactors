package erogenousbeef.bigreactors.common.multiblock.block;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import powercrystals.minefactoryreloaded.api.rednet.IRedNetOmniNode;
import powercrystals.minefactoryreloaded.api.rednet.connectivity.RedNetConnectionType;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import erogenousbeef.bigreactors.common.BRLoader;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.multiblock.MultiblockReactor;
import erogenousbeef.bigreactors.common.multiblock.interfaces.INeighborUpdatableEntity;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorAccessPort;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorComputerPort;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorCoolantPort;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorPart;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorPowerTap;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorRedNetPort;
import erogenousbeef.bigreactors.utils.StaticUtils;
import erogenousbeef.core.common.CoordTriplet;
import erogenousbeef.core.multiblock.IMultiblockPart;
import erogenousbeef.core.multiblock.MultiblockControllerBase;
import erogenousbeef.core.multiblock.rectangular.PartPosition;

public class BlockReactorPart extends BlockContainer implements IRedNetOmniNode, IPeripheralProvider {
	
	public static final int METADATA_CASING = 0;
	public static final int METADATA_CONTROLLER = 1;
	public static final int METADATA_POWERTAP = 2;
	public static final int METADATA_ACCESSPORT = 3;
	public static final int METADATA_REDNETPORT = 4;
	public static final int METADATA_COMPUTERPORT = 5;
	public static final int METADATA_COOLANTPORT = 6;
	
	private static final int PORT_INLET = 0;
	private static final int PORT_OUTLET = 1;
	private static final int TAP_DISCONNECTED = 0;
	private static final int TAP_CONNECTED = 1;
	private static final int CONTROLLER_OFF = 0;
	private static final int CONTROLLER_IDLE = 1;
	private static final int CONTROLLER_ACTIVE = 2;
	
	private static String[] _subBlocks = new String[] { "casing",
														"controller",
														"powerTap",
														"accessPort",
														"redNetPort",
														"computerPort",
														"coolantPort" };
	

	private static String[][] _states = new String[][] {
		{"default", "face", "corner", "eastwest", "northsouth", "vertical"}, // Casing
		{"off", "idle", "active"}, 		// Controller
		{"disconnected", "connected"}, 	// Power Tap
		{"inlet", "outlet"}, 			// Access Port
		{"default"},					// RedNet Port
		{"default"},					// Computer Port
		{"inlet", "outlet"} 			// Coolant Port
	};
	private IIcon[][] _icons = new IIcon[_states.length][];
	private static final int NUM_ICONS = 18; // Number in the states dict + 1

	private IIcon[] _redNetPortConfigIcons = new IIcon[TileEntityReactorRedNetPort.CircuitType.values().length - 1];
	
	public static boolean isCasing(int metadata) { return metadata == METADATA_CASING; }
	public static boolean isController(int metadata) { return metadata == METADATA_CONTROLLER; }
	public static boolean isPowerTap(int metadata) { return metadata == METADATA_POWERTAP; }
	public static boolean isAccessPort(int metadata) { return metadata == METADATA_ACCESSPORT; }
	public static boolean isRedNetPort(int metadata) { return metadata == METADATA_REDNETPORT; }
	public static boolean isComputerPort(int metadata) { return metadata == METADATA_COMPUTERPORT; }
	public static boolean isCoolantPort(int metadata) { return metadata == METADATA_COOLANTPORT; }
	
	public BlockReactorPart(Material material) {
		super(material);
		
		setStepSound(soundTypeMetal);
		setHardness(2.0f);
		setBlockName("blockReactorPart");
		this.setBlockTextureName(BigReactors.TEXTURE_NAME_PREFIX + "blockReactorPart");
		setCreativeTab(BigReactors.TAB);
	}

	@Override
    public IIcon getIcon(IBlockAccess blockAccess, int x, int y, int z, int side) {
		IIcon icon = null;
		int metadata = blockAccess.getBlockMetadata(x,y,z);
		
		if(metadata != METADATA_CASING && (side == 0 || side == 1)) {
			return blockIcon;
		}
		
		switch(metadata) {
			case METADATA_CASING:
				icon = getCasingIcon(blockAccess, x, y, z, side);
				break;
			case METADATA_CONTROLLER:
				icon = getControllerIcon(blockAccess, x, y, z, side);
				break;
			case METADATA_POWERTAP:
				icon = getPowerTapIcon(blockAccess, x, y, z, side);
				break;
			case METADATA_ACCESSPORT:
				icon = getAccessPortIcon(blockAccess, x, y, z, side);
				break;
			case METADATA_COOLANTPORT:
				icon = getCoolantPortIcon(blockAccess, x, y, z, side);
				break;
			case METADATA_REDNETPORT:
			case METADATA_COMPUTERPORT:
				icon = getFaceOrBlockIcon(blockAccess, x, y, z, side, metadata);
				break;
		}

		return icon != null ? icon : getIcon(side, metadata);
	}

	@Override
	public IIcon getIcon(int side, int metadata)
	{
		if(side == 0 || side == 1) { return blockIcon; }
		else if(metadata >= 0 && metadata < _icons.length) {
			return _icons[metadata][0];
		}
		else {
			return blockIcon;
		}
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
		
		this.blockIcon = par1IconRegister.registerIcon(BigReactors.TEXTURE_NAME_PREFIX + getUnlocalizedName());
		
		// We do this to skip DISABLED
		TileEntityReactorRedNetPort.CircuitType[] circuitTypes = TileEntityReactorRedNetPort.CircuitType.values();
		String rednetPrefix = BigReactors.TEXTURE_NAME_PREFIX + "redNet/";

		for(int i = 1; i < circuitTypes.length; ++i) {
			_redNetPortConfigIcons[i - 1] = par1IconRegister.registerIcon(rednetPrefix + circuitTypes[i].name());
		}
	}

	// We do this to skip DISABLED
	@SideOnly(Side.CLIENT)
	public IIcon getRedNetConfigIcon(TileEntityReactorRedNetPort.CircuitType circuitType) {
		if(circuitType == TileEntityReactorRedNetPort.CircuitType.DISABLED) { return null; }
		else {
			return _redNetPortConfigIcons[circuitType.ordinal() - 1];
		}
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		if(isPowerTap(metadata)) {
			return new TileEntityReactorPowerTap();
		}
		else if(isAccessPort(metadata)) {
			return new TileEntityReactorAccessPort();
		}
		else if(isRedNetPort(metadata)) {
			return new TileEntityReactorRedNetPort();
		}
		else if(isComputerPort(metadata)) {
			return new TileEntityReactorComputerPort();
		}
		else if(isCoolantPort(metadata)) {
			return new TileEntityReactorCoolantPort();
		}
		else {
			return new TileEntityReactorPart();
		}
	}
	
	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block neighborBlock) {
		TileEntity te = world.getTileEntity(x, y, z);
		// Signal power taps when their neighbors change, etc.
		if(te instanceof INeighborUpdatableEntity) {
			((INeighborUpdatableEntity)te).onNeighborBlockChange(world, x, y, z, neighborBlock);
		}
	}

	@Override
	public void onNeighborChange(IBlockAccess world, int x, int y, int z, int neighborX, int neighborY, int neighborZ) {
		TileEntity te = world.getTileEntity(x, y, z);
		// Signal power taps when their neighbors change, etc.
		if(te instanceof INeighborUpdatableEntity) {
			((INeighborUpdatableEntity)te).onNeighborTileChange(world, x, y, z, neighborX, neighborY, neighborZ);
		}
	}


	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9) {
		if(player.isSneaking()) {
			return false;
		}

		int metadata = world.getBlockMetadata(x, y, z);
		TileEntity te = world.getTileEntity(x, y, z);

		MultiblockControllerBase controller = null;
		if(te instanceof IMultiblockPart) {
			controller = ((IMultiblockPart)te).getMultiblockController();
		}
		
		if(!isController(metadata) && !isAccessPort(metadata) && !isRedNetPort(metadata)) {
			// If the player's hands are empty and they rightclick on a multiblock, they get a 
			// multiblock-debugging message if the machine is not assembled.
			if(!world.isRemote) {
				ItemStack currentEquippedItem = player.getCurrentEquippedItem();
				
				if(StaticUtils.Inventory.isPlayerHoldingWrench(player) && isCoolantPort(metadata)) {
					if(te instanceof TileEntityReactorCoolantPort) {
						TileEntityReactorCoolantPort cp = (TileEntityReactorCoolantPort)te;
						cp.setInlet(!cp.isInlet());
						return true;
					}
				}
				else if(currentEquippedItem == null) {
					if(te instanceof IMultiblockPart) {
						if(controller != null) {
							Exception e = controller.getLastValidationException();
							if(e != null) {
								player.addChatMessage(new ChatComponentText(e.getMessage()));
								return true;
							}
						}
						else {
							player.addChatMessage(new ChatComponentText("Block is not connected to a reactor. This could be due to lag, or a bug. If the problem persists, try breaking and re-placing the block.")); //TODO Localize
							return true;
						}
					}
				}
			}
			return false;
		}
		else if(!world.isRemote && isAccessPort(metadata) && StaticUtils.Inventory.isPlayerHoldingWrench(player)) {
			if(te instanceof TileEntityReactorAccessPort) {
				TileEntityReactorAccessPort cp = (TileEntityReactorAccessPort)te;
				cp.setInlet(!cp.isInlet());
				return true;
			}
		}
		
		// Don't open the controller GUI if the reactor isn't assembled
		if(isController(metadata) && (controller == null || !controller.isAssembled())) { return false; }

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
	
	public ItemStack getReactorCasingItemStack() {
		return new ItemStack(this, 1, METADATA_CASING);
	}
	
	public ItemStack getReactorControllerItemStack() {
		return new ItemStack(this, 1, METADATA_CONTROLLER);
	}
	
	public ItemStack getReactorPowerTapItemStack() {
		return new ItemStack(this, 1, METADATA_POWERTAP);
	}
	
	public ItemStack getAccessPortItemStack() {
		return new ItemStack(this, 1, METADATA_ACCESSPORT);
	}
	
	public ItemStack getRedNetPortItemStack() {
		return new ItemStack(this, 1, METADATA_REDNETPORT);
	}
	
	public ItemStack getComputerPortItemStack() {
		return new ItemStack(this, 1, METADATA_COMPUTERPORT);
	}
	
	public ItemStack getCoolantPortItemStack() {
		return new ItemStack(this, 1, METADATA_COOLANTPORT);
	}
	
	@Override
	public void getSubBlocks(Item par1, CreativeTabs par2CreativeTabs, List par3List)
	{
		par3List.add(getReactorCasingItemStack());
		par3List.add(getReactorControllerItemStack());
		par3List.add(getReactorPowerTapItemStack());
		par3List.add(getAccessPortItemStack());
		par3List.add(getRedNetPortItemStack());
		par3List.add(getComputerPortItemStack());
		par3List.add(getCoolantPortItemStack());
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

	// IConnectableRedNet
	@Override
	public RedNetConnectionType getConnectionType(World world, int x, int y,
			int z, ForgeDirection side) {
		TileEntity te = world.getTileEntity(x, y, z);
		if(te instanceof TileEntityReactorRedNetPort) {
			return RedNetConnectionType.CableAll;
		}

		return RedNetConnectionType.None;
	}

	@Override
	public int[] getOutputValues(World world, int x, int y, int z,
			ForgeDirection side) {
		TileEntity te = world.getTileEntity(x, y, z);
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
		TileEntity te = world.getTileEntity(x, y, z);
		if(te instanceof TileEntityReactorRedNetPort) {
			return ((TileEntityReactorRedNetPort)te).getValueForChannel(subnet);
		}
		return 0;
	}
	
	
	@Override
	public void onInputsChanged(World world, int x, int y, int z,
			ForgeDirection side, int[] inputValues) {
		TileEntity te = world.getTileEntity(x, y, z);
		if(te instanceof TileEntityReactorRedNetPort) {
			((TileEntityReactorRedNetPort)te).onInputValuesChanged(inputValues);
		}
	}

	// Never used, we're always in "all" mode.
	@Override
	public void onInputChanged(World world, int x, int y, int z,
			ForgeDirection side, int inputValue) {
		return;
	}
	
	// IPeripheralProvider
	@Override
	public IPeripheral getPeripheral(World world, int x, int y, int z, int side) {
		TileEntity te = world.getTileEntity(x, y, z);
		
		if(te instanceof TileEntityReactorComputerPort)
			return (IPeripheral)te;
		
		return null;
	}
	
	//// UGLY UI CODE HERE ////
	private IIcon getCoolantPortIcon(IBlockAccess blockAccess, int x, int y,
			int z, int side) {
		TileEntity te = blockAccess.getTileEntity(x, y, z);
		if(te instanceof TileEntityReactorCoolantPort) {
			TileEntityReactorCoolantPort port = (TileEntityReactorCoolantPort)te;
			
			if(!isReactorAssembled(port) || isOutwardsSide(port, side)) {
				if(port.isInlet()) {
					return _icons[METADATA_COOLANTPORT][PORT_INLET];
				}
				else {
					return _icons[METADATA_COOLANTPORT][PORT_OUTLET];
				}
			}
		}
		return blockIcon;
	}

	private IIcon getAccessPortIcon(IBlockAccess blockAccess, int x, int y,
			int z, int side) {
		TileEntity te = blockAccess.getTileEntity(x, y, z);
		if(te instanceof TileEntityReactorAccessPort) {
			TileEntityReactorAccessPort port = (TileEntityReactorAccessPort)te;

			if(!isReactorAssembled(port) || isOutwardsSide(port, side)) {
				if(port.isInlet()) {
					return _icons[METADATA_ACCESSPORT][PORT_INLET];
				}
				else {
					return _icons[METADATA_ACCESSPORT][PORT_OUTLET];
				}
			}
		}
		return blockIcon;
	}

	private IIcon getPowerTapIcon(IBlockAccess blockAccess, int x, int y, int z, int side) {
		TileEntity te = blockAccess.getTileEntity(x, y, z);
		if(te instanceof TileEntityReactorPowerTap) {
			TileEntityReactorPowerTap tap = (TileEntityReactorPowerTap)te;
			
			if(!isReactorAssembled(tap) || isOutwardsSide(tap, side)) {
				if(tap.hasEnergyConnection()) {
					return _icons[METADATA_POWERTAP][TAP_CONNECTED];
				}
				else {
					return _icons[METADATA_POWERTAP][TAP_DISCONNECTED];
				}
			}
		}
		return blockIcon;
	}

	private IIcon getControllerIcon(IBlockAccess blockAccess, int x, int y,
			int z, int side) {
		TileEntity te = blockAccess.getTileEntity(x, y, z);
		if(te instanceof TileEntityReactorPart) {
			TileEntityReactorPart controller = (TileEntityReactorPart)te;
			MultiblockReactor reactor = controller.getReactorController();
			
			if(reactor == null || !reactor.isAssembled()) {
				return _icons[METADATA_CONTROLLER][CONTROLLER_OFF];
			}
			else if(!isOutwardsSide(controller, side)) {
				return blockIcon;
			}
			else if(reactor.getActive()) {
				return _icons[METADATA_CONTROLLER][CONTROLLER_ACTIVE];
			}
			else {
				return _icons[METADATA_CONTROLLER][CONTROLLER_IDLE];
			}
		}
		return blockIcon;
	}

	private static final int DEFAULT = 0;
	private static final int FACE = 1;
	private static final int CORNER = 2;
	private static final int EASTWEST = 3;
	private static final int NORTHSOUTH = 4;
	private static final int VERTICAL = 5;
	
	private IIcon getCasingIcon(IBlockAccess blockAccess, int x, int y, int z, int side) {
		TileEntity te = blockAccess.getTileEntity(x, y, z);
		if(te instanceof TileEntityReactorPart) {
			TileEntityReactorPart part = (TileEntityReactorPart)te;
			PartPosition position = part.getPartPosition();
			MultiblockReactor reactor = part.getReactorController();
			if(!reactor.isAssembled()) {
				return _icons[METADATA_CASING][DEFAULT];
			}
			
			switch(position) {
			case BottomFace:
			case TopFace:
			case EastFace:
			case WestFace:
			case NorthFace:
			case SouthFace:
				return _icons[METADATA_CASING][FACE];
			case FrameCorner:
				return _icons[METADATA_CASING][CORNER];
			case Frame:
				return getCasingEdgeIcon(part, reactor, side);
			case Interior:
			case Unknown:
			default:
				return _icons[METADATA_CASING][DEFAULT];
			}
		}
		return _icons[METADATA_CASING][DEFAULT];
	}
	
	private IIcon getCasingEdgeIcon(TileEntityReactorPart part, MultiblockReactor reactor, int side) {
		if(!reactor.isAssembled()) { return _icons[METADATA_CASING][DEFAULT]; }

		CoordTriplet minCoord = reactor.getMinimumCoord();
		CoordTriplet maxCoord = reactor.getMaximumCoord();

		boolean xExtreme, yExtreme, zExtreme;
		xExtreme = yExtreme = zExtreme = false;

		if(part.xCoord == minCoord.x || part.xCoord == maxCoord.x) { xExtreme = true; }
		if(part.yCoord == minCoord.y || part.yCoord == maxCoord.y) { yExtreme = true; }
		if(part.zCoord == minCoord.z || part.zCoord == maxCoord.z) { zExtreme = true; }
		
		int idx = DEFAULT;
		if(!xExtreme) {
			if(side < 4) { idx = EASTWEST; }
		}
		else if(!yExtreme) {
			if(side > 1) {
				idx = VERTICAL;
			}
		}
		else { // !zExtreme
			if(side < 2) {
				idx = NORTHSOUTH;
			}
			else if(side > 3) {
				idx = EASTWEST;
			}
		}
		return _icons[METADATA_CASING][idx];
	}
	
	private IIcon getFaceOrBlockIcon(IBlockAccess blockAccess, int x, int y, int z, int side, int metadata) {
		TileEntity te = blockAccess.getTileEntity(x, y, z);
		if(te instanceof TileEntityReactorPart) {
			TileEntityReactorPart part = (TileEntityReactorPart)te;
			if(!isReactorAssembled(part) || isOutwardsSide(part, side)) {
				return _icons[metadata][0];
			}
		}
		return this.blockIcon;
	}
	
	/**
	 * @param part The part whose sides we're checking
	 * @param side The side to compare to the part
	 * @return True if `side` is the outwards-facing face of `part`
	 */
	private boolean isOutwardsSide(TileEntityReactorPart part, int side) {
		ForgeDirection outDir = part.getOutwardsDir();
		return outDir.ordinal() == side;
	}
	
	private boolean isReactorAssembled(TileEntityReactorPart part) {
		MultiblockReactor reactor = part.getReactorController();
		return reactor != null && reactor.isAssembled();
	}
}

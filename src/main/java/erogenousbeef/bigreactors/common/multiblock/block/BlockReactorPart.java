package erogenousbeef.bigreactors.common.multiblock.block;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
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
//import powercrystals.minefactoryreloaded.api.rednet.IConnectableRedNet;
//import powercrystals.minefactoryreloaded.api.rednet.RedNetConnectionType;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import erogenousbeef.bigreactors.common.BRLoader;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.multiblock.interfaces.INeighborUpdatableEntity;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorAccessPort;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorComputerPort;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorCoolantPort;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorPart;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorPowerTap;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorRedNetPort;
import erogenousbeef.bigreactors.utils.StaticUtils;
import erogenousbeef.core.multiblock.IMultiblockPart;
import erogenousbeef.core.multiblock.MultiblockControllerBase;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
//implements IConnectableRedNet, IPeripheralProvider
public class BlockReactorPart extends BlockContainer implements IPeripheralProvider   {
	
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
	public static final int COMPUTERPORT = 14;
	public static final int COOLANTPORT = 15;
	
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
														"redNetPort",
														"computerPort",
														"coolantPort" };

	protected static final int SUBICON_COOLANT_OUTLET = 0;
	
	private static String[] _subIconNames = new String[] {
		"coolantPort.outlet"
	};
	
	private IIcon[] _icons = new IIcon[_subBlocks.length];
	private IIcon[] _redNetPortConfigIcons = new IIcon[TileEntityReactorRedNetPort.CircuitType.values().length - 1];
	
	private IIcon[] _subIcons = new IIcon[_subIconNames.length];
	
	public static boolean isCasing(int metadata) { return metadata >= CASING_METADATA_BASE && metadata < CONTROLLER_METADATA_BASE; }
	public static boolean isController(int metadata) { return metadata >= CONTROLLER_METADATA_BASE && metadata < POWERTAP_METADATA_BASE; }
	public static boolean isPowerTap(int metadata) { return metadata >= POWERTAP_METADATA_BASE && metadata < ACCESSPORT_INLET; }
	public static boolean isAccessPort(int metadata) { return metadata >= ACCESSPORT_INLET && metadata < REDNETPORT; }
	public static boolean isRedNetPort(int metadata) { return metadata == REDNETPORT; }
	public static boolean isComputerPort(int metadata) { return metadata == COMPUTERPORT; }
	public static boolean isCoolantPort(int metadata) { return metadata == COOLANTPORT; }
	
	public BlockReactorPart(Material material) {
		super(material);
		
		setStepSound(soundTypeMetal);
		setHardness(2.0f);
		setBlockName(BRLoader.MOD_ID+".blockReactorPart");
		this.setBlockTextureName(BigReactors.TEXTURE_NAME_PREFIX + "blockReactorPart");
		setCreativeTab(BigReactors.TAB);
	}
	@Override
    public IIcon getIcon(IBlockAccess blockAccess, int x, int y, int z, int side) {
		// TODO: Put all icon selection in here
		int metadata = blockAccess.getBlockMetadata(x,y,z);

		if(isCoolantPort(metadata)) {
			TileEntity te = blockAccess.getTileEntity(x, y, z);

			if(te instanceof TileEntityReactorCoolantPort) {
				TileEntityReactorCoolantPort cp = (TileEntityReactorCoolantPort)te;
				
				if(cp.isConnected() && cp.getReactorController().isAssembled()) {
					if(cp.getOutwardsDir().ordinal() == side) {
						if(cp.isInlet()) {
							return _icons[metadata];
						}
						else {
							return _subIcons[SUBICON_COOLANT_OUTLET];
						}
					}
					else {
						return _icons[CASING_METADATA_BASE];
					}
				}
				else {
					if(side == 0 || side == 1) {
						return _icons[CASING_METADATA_BASE];
					}
					else if(cp.isInlet()) {
						return _icons[metadata];
					}
					else {
						return _subIcons[SUBICON_COOLANT_OUTLET];
					}
				}
			}
		}
		
		return getIcon(side, metadata);
	}
	
	
	@Override
	public IIcon getIcon(int side, int metadata)
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
					metadata = Math.max(0, Math.min(15, metadata));
					return _icons[metadata];
				}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister par1IconRegister)
	{
		this.blockIcon = par1IconRegister.registerIcon(BigReactors.TEXTURE_NAME_PREFIX + getUnlocalizedName());
		
		for(int i = 0; i < _subBlocks.length; ++i) {
			_icons[i] = par1IconRegister.registerIcon(BigReactors.TEXTURE_NAME_PREFIX + getUnlocalizedName() + "." + _subBlocks[i]);
		}
		
		for(int i = 0; i < _subIconNames.length; ++i) {
			_subIcons[i] = par1IconRegister.registerIcon(BigReactors.TEXTURE_NAME_PREFIX + getUnlocalizedName() + "." + _subIconNames[i]);
		}
		
		// We do this to skip DISABLED
		TileEntityReactorRedNetPort.CircuitType[] circuitTypes = TileEntityReactorRedNetPort.CircuitType.values();
		for(int i = 1; i < circuitTypes.length; ++i) {
			_redNetPortConfigIcons[i - 1] = par1IconRegister.registerIcon(BigReactors.TEXTURE_NAME_PREFIX + "redNet/" + circuitTypes[i].name());
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
	public TileEntity createNewTileEntity(World world,int var) {
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
	public void onNeighborBlockChange(World world, int x, int y, int z, Block neighborBlockID) {
		TileEntity te = world.getTileEntity(x, y, z);
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
		if(!isController(metadata) && !isAccessPort(metadata) && !isRedNetPort(metadata)) {
			// If the player's hands are empty and they rightclick on a multiblock, they get a 
			// multiblock-debugging message if the machine is not assembled.
			if(!world.isRemote) {
				ItemStack currentEquippedItem = player.getCurrentEquippedItem();
				
				if(isCoolantPort(metadata) && (StaticUtils.Inventory.isPlayerHoldingWrench(player) || currentEquippedItem == null)) {
					// Use wrench to change inlet/outlet state
					TileEntity te = world.getTileEntity(x, y, z);
					if(te instanceof TileEntityReactorCoolantPort) {
						TileEntityReactorCoolantPort cp = (TileEntityReactorCoolantPort)te;
						cp.setInlet(!cp.isInlet());
						return true;
					}
				}
				else if(currentEquippedItem == null) {
					TileEntity te = world.getTileEntity(x, y, z);
					if(te instanceof IMultiblockPart) {
						MultiblockControllerBase controller = ((IMultiblockPart)te).getMultiblockController();
						if(controller != null) {
							Exception e = controller.getLastValidationException();
							if(e != null) {
								player.addChatComponentMessage(new ChatComponentText(e.getMessage()));
								return true;
							}
						}
						else {
							player.addChatComponentMessage(new ChatComponentText("Block is not connected to a reactor. This could be due to lag, or a bug. If the problem persists, try breaking and re-placing the block."));
							return true;
						}
					}
				}
			}
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
		else if(isComputerPort(metadata)) {
			return COMPUTERPORT;
		}
		else if(isCoolantPort(metadata)) {
			return COOLANTPORT;
		}
		else {
			return CASING_METADATA_BASE;
		}
	}
	
	public ItemStack getReactorCasingItemStack() {
		return new ItemStack(this, 1, CASING_METADATA_BASE);
	}
	
	public ItemStack getReactorControllerItemStack() {
		return new ItemStack(this, 1, CONTROLLER_METADATA_BASE);
	}
	
	public ItemStack getReactorPowerTapItemStack() {
		return new ItemStack(this, 1, POWERTAP_METADATA_BASE);
	}
	
	public ItemStack getAccessPortItemStack() {
		return new ItemStack(this, 1, ACCESSPORT_INLET);
	}
	
	public ItemStack getRedNetPortItemStack() {
		return new ItemStack(this, 1, REDNETPORT);
	}
	
	public ItemStack getComputerPortItemStack() {
		return new ItemStack(this, 1, COMPUTERPORT);
	}
	
	public ItemStack getCoolantPortItemStack() {
		return new ItemStack(this, 1, COOLANTPORT);
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
	public void breakBlock(World world, int x, int y, int z, Block blockId, int meta)
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

		super.breakBlock(world, x, y, z, blockId, meta);
	}
	/*
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
	}*/
	
	// IPeripheralProvider
	@Override
	public IPeripheral getPeripheral(World world, int x, int y, int z, int side) {
		TileEntity te = world.getTileEntity(x, y, z);
		
		if(te instanceof TileEntityReactorComputerPort)
			return (IPeripheral)te;
		
		return null;
	}
}

package erogenousbeef.bigreactors.common.tileentity.base;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import cofh.lib.util.helpers.BlockHelper;
import erogenousbeef.bigreactors.utils.AdjacentInventoryHelper;

public abstract class TileEntityInventory extends TileEntityBeefBase implements IInventory, ISidedInventory {
	
	// Inventory
	protected ItemStack[] _inventories;
	protected int[][] invSlotExposures;

	private AdjacentInventoryHelper[] adjacentInvs;
	
	protected static final int SLOT_NONE = TileEntityBeefBase.SIDE_UNEXPOSED;

	public TileEntityInventory() {
		super();
		_inventories = new ItemStack[getSizeInventory()];
		invSlotExposures = new int[getSizeInventory()][1];
		for(int i = 0; i < invSlotExposures.length; i++) {
			// Set up a cached array with all possible exposed inventory slots, so we don't have to alloc at runtime
			invSlotExposures[i][0] = i;
		}
		
		adjacentInvs = new AdjacentInventoryHelper[ForgeDirection.VALID_DIRECTIONS.length];
		for(ForgeDirection dir: ForgeDirection.VALID_DIRECTIONS) {
			adjacentInvs[dir.ordinal()] = new AdjacentInventoryHelper(dir);
		}

		resetAdjacentInventories();
	}

	@Override
	public void onNeighborBlockChange() {
		super.onNeighborBlockChange();
		
		checkAdjacentInventories();
	}
	
	@Override
	public void onNeighborTileChange(int x, int y, int z) {
		super.onNeighborTileChange(x, y, z);
		int side = BlockHelper.determineAdjacentSide(this, x, y, z);
		checkAdjacentInventory(ForgeDirection.getOrientation(side));
	}
	
	// TileEntity overrides
	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		
		// Inventories
		_inventories = new ItemStack[getSizeInventory()];
		if(tag.hasKey("Items")) {
			NBTTagList tagList = tag.getTagList("Items", 10);
			for(int i = 0; i < tagList.tagCount(); i++) {
				NBTTagCompound itemTag = (NBTTagCompound)tagList.getCompoundTagAt(i);
				int slot = itemTag.getByte("Slot") & 0xff;
				if(slot >= 0 && slot <= _inventories.length) {
					ItemStack itemStack = new ItemStack((Block)null,0,0);
					itemStack.readFromNBT(itemTag);
					_inventories[slot] = itemStack;
				}
			}
		}
	}
	
	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);

		// Inventories
		NBTTagList tagList = new NBTTagList();		
		for(int i = 0; i < _inventories.length; i++) {
			if((_inventories[i]) != null) {
				NBTTagCompound itemTag = new NBTTagCompound();
				itemTag.setByte("Slot", (byte)i);
				_inventories[i].writeToNBT(itemTag);
				tagList.appendTag(itemTag);
			}
		}
		
		if(tagList.tagCount() > 0) {
			tag.setTag("Items", tagList);
		}
	}
	
	// IInventory
	@Override
	public abstract int getSizeInventory();

	@Override
	public ItemStack getStackInSlot(int slot) {
		return _inventories[slot];
	}

	@Override
	public ItemStack decrStackSize(int slot, int amount) {
		if(_inventories[slot] != null)
		{
			if(_inventories[slot].stackSize <= amount)
			{
				ItemStack itemstack = _inventories[slot];
				_inventories[slot] = null;
				return itemstack;
			}
			ItemStack newStack = _inventories[slot].splitStack(amount);
			if(_inventories[slot].stackSize == 0)
			{
				_inventories[slot] = null;
			}
			return newStack;
		}
		else
		{
			return null;
		}
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slot) {
		return null;
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack itemstack) {
		_inventories[slot] = itemstack;
		if(itemstack != null && itemstack.stackSize > getInventoryStackLimit())
		{
			itemstack.stackSize = getInventoryStackLimit();
		}
	}

	@Override
	public abstract String getInventoryName();
	
	@Override
	public boolean hasCustomInventoryName() {
		return false;
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		if(worldObj.getTileEntity(xCoord, yCoord, zCoord) != this)
		{
			return false;
		}
		return entityplayer.getDistanceSq((double)xCoord + 0.5D, (double)yCoord + 0.5D, (double)zCoord + 0.5D) <= 64D;
	}

	@Override
	public void openInventory() {
	}

	@Override
	public void closeInventory() {
	}

	@Override
	public abstract boolean isItemValidForSlot(int slot, ItemStack itemstack);

	// ISidedInventory
	/**
	 * Get the exposed inventory slot from a given world side.
	 * Remember to translate this into a reference side!
	 * @param side The side being queried for exposure.
	 * @return The index of the exposed slot, -1 (SLOT_UNEXPOSED) if none.
	 */
	protected abstract int getExposedInventorySlotFromSide(int side);
	
	@Override
	public int[] getAccessibleSlotsFromSide(int side) {
		int exposedSlot = getExposedInventorySlotFromSide(side);
		if(exposedSlot >= 0 && exposedSlot < invSlotExposures.length) {
			return invSlotExposures[exposedSlot];
		}
		else {
			return kEmptyIntArray;
		}
	}

	@Override
	public boolean canInsertItem(int slot, ItemStack itemstack, int side) {
		return isItemValidForSlot(slot, itemstack);
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack itemstack, int side) {
		return isItemValidForSlot(slot, itemstack);
	}	

	// IItemDuctConnection
	public boolean canConduitConnect(ForgeDirection from) {
		return from != ForgeDirection.UNKNOWN;
	}

	/**
	 * This method distributes items from all exposed slots to linked inventories
	 * on their respective sides.
	 */
	protected void distributeItems()
	{
		for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			distributeSide(dir);
		}
	}
	
	protected void distributeItemsFromSlot(int slot) {
		if(slot == SLOT_NONE) { return; }
		for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			int sideSlot = getExposedInventorySlotFromSide(dir.ordinal());
			if(slot == sideSlot) {
				_inventories[slot] = distributeItemToSide(dir, _inventories[slot]);
			}
			
			if(_inventories[slot] == null) { break; }
		}
	}

	/**
	 * Distributes items from whichever slot is currently exposed on a given
	 * side to any adjacent pipes/ducts/inventories.
	 * @param dir The side whose exposed items you wish to distribute.
	 */
	protected void distributeSide(ForgeDirection dir) {
		int slot = getExposedInventorySlotFromSide(dir.ordinal());
		if(slot == SLOT_NONE) { return; }
		if(_inventories[slot] == null) { return; }
		
		_inventories[slot] = distributeItemToSide(dir, _inventories[slot]);
	}
	
	/**
	 * Distributes a given item stack to a given side.
	 * Note that this method does not check for exposures.
	 * @param dir Direction/side to which you wish to distribute items.
	 * @param itemstack An item stack to distribute.
	 * @return An itemstack containing the undistributed items, or null if all items were distributed.
	 */
	protected ItemStack distributeItemToSide(ForgeDirection dir, ItemStack itemstack) {
		return adjacentInvs[dir.ordinal()].distribute(itemstack);
	}
	
	// Adjacent Inventory Detection
	private void checkAdjacentInventories() {
		boolean changed = false;
		for(ForgeDirection dir: ForgeDirection.VALID_DIRECTIONS) {
			checkAdjacentInventory(dir);
		}
	}
	
	private void checkAdjacentInventory(ForgeDirection dir) {
		TileEntity te = worldObj.getTileEntity(xCoord+dir.offsetX, yCoord+dir.offsetY, zCoord+dir.offsetZ);
		if(adjacentInvs[dir.ordinal()].set(te)) {
			distributeSide(dir);
		}
	}
	
	private void resetAdjacentInventories() {
		for(int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
			adjacentInvs[i].set(null);
		}
	}
}

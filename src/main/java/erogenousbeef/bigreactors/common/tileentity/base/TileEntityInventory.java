package erogenousbeef.bigreactors.common.tileentity.base;

import io.netty.buffer.ByteBuf;

import java.io.IOException;

import scala.actors.threadpool.Arrays;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.api.transport.IPipeTile;
import cofh.api.transport.IItemDuct;
import cpw.mods.fml.common.network.NetworkRegistry;
import erogenousbeef.bigreactors.net.CommonPacketHandler;
import erogenousbeef.bigreactors.utils.InventoryHelper;
import erogenousbeef.bigreactors.utils.SidedInventoryHelper;
import erogenousbeef.bigreactors.utils.StaticUtils;
import erogenousbeef.bigreactors.utils.intermod.ModHelperBase;

public abstract class TileEntityInventory extends TileEntityBeefBase implements IInventory, ISidedInventory {
	
	// Inventory
	protected ItemStack[] _inventories;
	protected int[][] invSlotExposures;
	
	protected static final int SLOT_NONE = -1;
	
	public TileEntityInventory() {
		super();
		_inventories = new ItemStack[getSizeInventory()];
		invSlotExposures = new int[getSizeInventory()][1];
		for(int i = 0; i < invSlotExposures.length; i++) {
			// Set up a cached array with all possible exposed inventory slots, so we don't have to alloc at runtime
			invSlotExposures[i][0] = i;
		}
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
		if(exposedSlot > 0 && exposedSlot < invSlotExposures.length) {
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
	 * @param fromSlot The inventory slot into which this object would normally go.
	 * @param itemToDistribute An ItemStack to distribute to pipes
	 * @return Null if the stack was distributed, an ItemStack indicating the remainder otherwise.
	 */
	protected ItemStack distributeItemToPipes(int fromSlot, ItemStack itemToDistribute) {
		ForgeDirection[] dirsToCheck = { ForgeDirection.NORTH, ForgeDirection.SOUTH,
										ForgeDirection.EAST, ForgeDirection.WEST, ForgeDirection.UP, ForgeDirection.DOWN };

		for(ForgeDirection dir : dirsToCheck) {
			// Are we exposed on that side?
			if(itemToDistribute == null) { return null; }

			int[] accessibleSlots = getAccessibleSlotsFromSide(dir.ordinal());
			if(accessibleSlots == null || accessibleSlots.length < 1) { continue; }
			
			boolean allowed = false;
			for(int i = 0; i < accessibleSlots.length; i++) {
				if(accessibleSlots[i] == fromSlot) {
					allowed = true;
					break;
				}
			}
			
			if(!allowed) { continue; }
			
			TileEntity te = this.worldObj.getTileEntity(xCoord+dir.offsetX, yCoord+dir.offsetY, zCoord+dir.offsetZ);
			if(ModHelperBase.useCofh && te instanceof IItemDuct) {
				IItemDuct conduit = (IItemDuct)te;
				itemToDistribute = conduit.insertItem(dir.getOpposite(), itemToDistribute);
			}
			else if(ModHelperBase.useBuildcraftTransport && te instanceof IPipeTile) {
				IPipeTile pipe = (IPipeTile)te;
				if(pipe.isPipeConnected(dir.getOpposite())) {
					itemToDistribute.stackSize -= pipe.injectItem(itemToDistribute.copy(), true, dir.getOpposite());
					
					if(itemToDistribute.stackSize <= 0) {
						return null;
					}
				}
			}
			else if(te instanceof IInventory) {
				InventoryHelper helper;
				if(te instanceof ISidedInventory) {
					helper = new SidedInventoryHelper((ISidedInventory)te, dir.getOpposite());
				}
				else {
					IInventory inv = (IInventory)te;
					if(worldObj.getBlock(xCoord+dir.offsetX, yCoord+dir.offsetY, zCoord+dir.offsetZ) == Blocks.chest) {
						inv = StaticUtils.Inventory.checkForDoubleChest(worldObj, inv, xCoord+dir.offsetX, yCoord+dir.offsetY, zCoord+dir.offsetZ);
					}
					helper = new InventoryHelper(inv);
				}
				itemToDistribute = helper.addItem(itemToDistribute);
			}
		}
		
		return itemToDistribute;
	}

}

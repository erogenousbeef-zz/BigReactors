package erogenousbeef.bigreactors.common.tileentity.base;

import io.netty.buffer.ByteBuf;

import java.io.DataInputStream;
import java.io.IOException;

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
import erogenousbeef.bigreactors.net.message.SmallMachineInventoryExposureMessage;
import erogenousbeef.bigreactors.utils.InventoryHelper;
import erogenousbeef.bigreactors.utils.SidedInventoryHelper;
import erogenousbeef.bigreactors.utils.StaticUtils;

public abstract class TileEntityInventory extends TileEntityBeefBase implements IInventory, ISidedInventory {
	
	protected static final int[] kEmptyIntArray = new int[0];
	
	// Configurable Sides
	protected int[][] invExposures;
	public static final int INVENTORY_UNEXPOSED = -1;
	
	// Inventory
	protected ItemStack[] _inventories;
	
	public TileEntityInventory() {
		super();
		_inventories = new ItemStack[getSizeInventory()];
		invExposures = new int[6][1]; // 6 forge directions - we keep them in 1-length arrays because some stuff uses it that way
		
		resetInventoryExposures();
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
		
		resetInventoryExposures();
		if(tag.hasKey("invExposures")) {
			NBTTagList exposureList = tag.getTagList("invExposures", 10);
			for(int i = 0; i < exposureList.tagCount(); i++) {
				NBTTagCompound exposureTag = (NBTTagCompound) exposureList.getCompoundTagAt(i);
				int exposureIdx = exposureTag.getInteger("exposureIdx");
				invExposures[exposureIdx][0] = exposureTag.getInteger("direction");
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
		
		// Save inventory exposure orientations
		NBTTagList exposureTagList = new NBTTagList();
		for(int i = 0; i < 6; i++) {
			if(invExposures[i][0] == INVENTORY_UNEXPOSED) {
				continue;
			}
			NBTTagCompound exposureTag = new NBTTagCompound();
			exposureTag.setInteger("exposureIdx", i);
			exposureTag.setInteger("direction", invExposures[i][0]);
			exposureTagList.appendTag(exposureTag);
		}
		
		if(exposureTagList.tagCount() > 0) {
			tag.setTag("invExposures", exposureTagList);			
		}
	}
	
	// Inventory Exposures
	/**
	 * Set the exposed inventory slot on a given side.
	 * @param side Unrotated (world) side to set
	 * @param slot The inventory slot to expose, or -1 (INVENTORY_UNEXPOSED) if none.
	 */
	public void setExposedInventorySlot(int side, int slot) {
		if(side < 0 || side > 5) {
			return;
		}
		
		if(side == this.forwardFace.ordinal()) {
			return;
		}
		
		int rotatedSide = this.getRotatedSide(side);
		setExposedInventorySlotReference(rotatedSide, slot);
	}

	/**
	 * Set the exposed inventory slot on a given side, using the reference side index.
	 * Only use this if you know what you're doing.
	 * @param referenceSide Reference side. 2 = North, 3 = South, 4 = East, 5 = West
	 * @param slot The inventory slot to expose, or -1 (INVENTORY_UNEXPOSED) if none.
	 */
	public void setExposedInventorySlotReference(int referenceSide, int slot) {
		if(referenceSide < 0 || referenceSide >= invExposures.length) { return; }
		if(invExposures[referenceSide][0] == slot) {
			return;
		}

		invExposures[referenceSide][0] = slot;
		
		if(!this.worldObj.isRemote) {
			// Send unrotated, as the rotation will be re-applied on the client
            CommonPacketHandler.INSTANCE.sendToAllAround(new SmallMachineInventoryExposureMessage(xCoord, yCoord, zCoord, referenceSide, slot), new NetworkRegistry.TargetPoint(worldObj.provider.dimensionId, xCoord, yCoord, zCoord, 50));

            this.worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, this.worldObj.getBlock(this.xCoord, this.yCoord, this.zCoord));
		}
	}

	/**
	 * Get the exposed inventory slot from the REFERENCE side; i.e. the unrotated side, as if the machine faced north
	 * @param side Reference side. 2 = North, 3 = South, 4 = East, 5 = West
	 * @return The exposed inventory slot index on that side, or INVENTORY_UNEXPOSED if none.
	 */
	public int getExposedSlotFromReferenceSide(int side) {
		if(side < 0 || side > 5) { return INVENTORY_UNEXPOSED; }
		
		return invExposures[side][0];
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
	@Override
	public int[] getAccessibleSlotsFromSide(int side) {
		int rotatedSide = this.getRotatedSide(side);
		
		if(invExposures[rotatedSide][0] == INVENTORY_UNEXPOSED) { return kEmptyIntArray; }
		
		return invExposures[rotatedSide];
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
	
	// Networked GUI
	@Override
	public void onReceiveGuiButtonPress(String buttonName, ByteBuf dataStream) throws IOException {
		if(buttonName.equals("changeInvSide")) {
			int side = dataStream.readInt();
			iterateInventoryExposure(side);
		}
	}
	
	// Helpers
	protected void iterateInventoryExposure(int side) {
		int slot = invExposures[side][0];
		slot++;
		if(slot >= getSizeInventory()) {
			slot = INVENTORY_UNEXPOSED;
		}
		
		this.setExposedInventorySlotReference(side, slot);
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

			int rotatedSide = this.getRotatedSide(dir.ordinal());
			if(invExposures[rotatedSide][0] != fromSlot) { continue; }
			
			TileEntity te = this.worldObj.getTileEntity(xCoord+dir.offsetX, yCoord+dir.offsetY, zCoord+dir.offsetZ);
			if(te instanceof IItemDuct) {
				IItemDuct conduit = (IItemDuct)te;
				itemToDistribute = conduit.insertItem(dir.getOpposite(), itemToDistribute);
			}
			else if(te instanceof IPipeTile) {
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

	private void resetInventoryExposures() {
		for(int i = 0; i < 6; i++) {
			invExposures[i][0] = INVENTORY_UNEXPOSED;
		}
		
	}
}

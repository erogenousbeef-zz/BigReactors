package erogenousbeef.bigreactors.common.tileentity.base;

import buildcraft.api.transport.IPipeEntry;
import cpw.mods.fml.common.network.PacketDispatcher;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.block.BlockReactorPart;
import erogenousbeef.bigreactors.common.item.ItemIngot;
import erogenousbeef.bigreactors.net.PacketWrapper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet132TileEntityData;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

public abstract class TileEntityInventory extends TileEntityBeefBase implements IInventory, ISidedInventory {
	// Rotation
	ForgeDirection forwardFace;
	
	// Configurable Sides
	
	// Power
	
	// Inventory
	protected ItemStack[] _inventories;
	
	public TileEntityInventory() {
		super();
		forwardFace = ForgeDirection.NORTH;
		_inventories = new ItemStack[getSizeInventory()];
	}
	
	// Rotation
	public ForgeDirection getFacingDirection() {
		return forwardFace;
	}
	
	public void rotateTowards(ForgeDirection newDirection) {
		forwardFace = newDirection;
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		if(!worldObj.isRemote) {
			// TODO: Special packet for these updates
			//PacketDispatcher.sendPacketToAllAround(xCoord, yCoord, zCoord, worldObj.provider.dimensionId, 0, getUpdatePacket());
		}
	}
	
	public int getRotatedSide(int side) {
		if(side == 0 || side == 1) { return side; }
		
		return ForgeDirection.ROTATION_MATRIX[ForgeDirection.UP.ordinal()][side];
	}
	
	// TileEntity overrides
	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		
		int rotation = tag.getInteger("rotation");
		forwardFace = ForgeDirection.getOrientation(rotation);
		
		_inventories = new ItemStack[getSizeInventory()];
		if(tag.hasKey("Items")) {
			NBTTagList tagList = tag.getTagList("Items");
			for(int i = 0; i < tagList.tagCount(); i++) {
				NBTTagCompound itemTag = (NBTTagCompound)tagList.tagAt(i);
				int slot = itemTag.getByte("Slot") & 0xff;
				if(slot >= 0 && slot <= _inventories.length) {
					ItemStack itemStack = new ItemStack(0,0,0);
					itemStack.readFromNBT(itemTag);
					_inventories[slot] = itemStack;
				}
			}
		}
	}
	
	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		tag.setInteger("rotation", forwardFace.ordinal());
		
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
	public abstract String getInvName();
	
	@Override
	public boolean isInvNameLocalized() {
		return false;
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		if(worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) != this)
		{
			return false;
		}
		return entityplayer.getDistanceSq((double)xCoord + 0.5D, (double)yCoord + 0.5D, (double)zCoord + 0.5D) <= 64D;
	}

	@Override
	public void openChest() {
	}

	@Override
	public void closeChest() {
	}

	@Override
	public abstract boolean isStackValidForSlot(int slot, ItemStack itemstack);

	// ISidedInventory
	@Override
	public int[] getAccessibleSlotsFromSide(int side) {
		if(side == 0 || side == 1) { return null; }
		
		int[] allSlots = new int[getSizeInventory()];
		for(int i = 0; i < getSizeInventory(); i++) {
			allSlots[i] = i;
		}
		
		return allSlots;
	}

	@Override
	public boolean canInsertItem(int slot, ItemStack itemstack, int side) {
		if(side == 0 || side == 1) { return false; }
		
		return isStackValidForSlot(slot, itemstack);
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack itemstack, int side) {
		if(side == 0 || side == 1) { return false; }
		
		return isStackValidForSlot(slot, itemstack);
	}	
	
	// Helpers
	
	/**
	 * @param itemToDistribute An ItemStack to distribute to pipes
	 * @return Null if the stack was distributed, the same ItemStack otherwise.
	 */
	protected ItemStack distributeItemToPipes(ItemStack itemToDistribute) {
		if(itemToDistribute == null) { return null; }
		
		ForgeDirection[] dirsToCheck = { ForgeDirection.NORTH, ForgeDirection.SOUTH,
										ForgeDirection.EAST, ForgeDirection.WEST };

		for(ForgeDirection dir : dirsToCheck) {
			TileEntity te = this.worldObj.getBlockTileEntity(xCoord+dir.offsetX, yCoord+dir.offsetY, zCoord+dir.offsetZ);
			if(te != null && te instanceof IPipeEntry) {
				IPipeEntry pipe = (IPipeEntry)te;
				if(pipe.acceptItems()) {
					pipe.entityEntering(itemToDistribute.copy(), dir.getOpposite());
					return null;
				}
			}
		}
		
		return itemToDistribute;
	}	
}

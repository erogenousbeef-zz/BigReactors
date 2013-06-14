package erogenousbeef.bigreactors.common.tileentity.base;

import java.io.DataInputStream;
import java.io.IOException;

import buildcraft.api.transport.IPipeEntry;
import cpw.mods.fml.common.network.PacketDispatcher;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.block.BlockReactorPart;
import erogenousbeef.bigreactors.common.item.ItemIngot;
import erogenousbeef.bigreactors.net.PacketWrapper;
import erogenousbeef.bigreactors.net.Packets;
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
	
	// Configurable Sides
	protected int[] invExposures;
	public static final int INVENTORY_UNEXPOSED = -1;
	
	// Inventory
	protected ItemStack[] _inventories;
	
	public TileEntityInventory() {
		super();
		_inventories = new ItemStack[getSizeInventory()];
		
		resetInventoryExposures();
	}
	
	// TileEntity overrides
	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		
		// Inventories
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
		
		resetInventoryExposures();
		if(tag.hasKey("invExposures")) {
			NBTTagList exposureList = tag.getTagList("invExposures");
			for(int i = 0; i < exposureList.tagCount(); i++) {
				NBTTagCompound exposureTag = (NBTTagCompound) exposureList.tagAt(i);
				int exposureIdx = exposureTag.getInteger("exposureIdx");
				invExposures[exposureIdx] = exposureTag.getInteger("direction");
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
			if(invExposures[i] == INVENTORY_UNEXPOSED) {
				continue;
			}
			NBTTagCompound exposureTag = new NBTTagCompound();
			exposureTag.setInteger("exposureIdx", i);
			exposureTag.setInteger("direction", invExposures[i]);
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
	 * @param side Reference side. 2 = North, 3 = South, 4 = East, 5 = West
	 * @param slot The inventory slot to expose, or -1 (INVENTORY_UNEXPOSED) if none.
	 */
	public void setExposedInventorySlotReference(int referenceSide, int slot) {
		if(referenceSide < 0 || referenceSide >= invExposures.length) { return; }
		if(invExposures[referenceSide] == slot) {
			return;
		}

		invExposures[referenceSide] = slot;
		
		if(!this.worldObj.isRemote) {
			// Send unrotated, as the rotation will be re-applied on the client
			Packet updatePacket = PacketWrapper.createPacket(BigReactors.CHANNEL, Packets.SmallMachineInventoryExposureUpdate,
																new Object[] { xCoord, yCoord, zCoord, referenceSide, slot });
			PacketDispatcher.sendPacketToAllAround(xCoord, yCoord, zCoord, 50, worldObj.provider.dimensionId, updatePacket);
			
			this.worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, this.worldObj.getBlockId(this.xCoord, this.yCoord, this.zCoord));
		}
	}

	/**
	 * Get the exposed inventory slot from the REFERENCE side; i.e. the unrotated side, as if the machine faced north
	 * @param side Reference side. 2 = North, 3 = South, 4 = East, 5 = West
	 * @return The exposed inventory slot index on that side, or INVENTORY_UNEXPOSED if none.
	 */
	public int getExposedSlotFromReferenceSide(int side) {
		if(side < 0 || side > 5) { return INVENTORY_UNEXPOSED; }
		
		return invExposures[side];
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
		int rotatedSide = this.getRotatedSide(side);
		
		if(invExposures[rotatedSide] == INVENTORY_UNEXPOSED) { return new int[0]; }
		
		int[] slots = new int[1];
		slots[0] = invExposures[rotatedSide];
		return slots;
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
	
	// Networked GUI
	@Override
	public void onReceiveGuiButtonPress(String buttonName, DataInputStream dataStream) throws IOException {
		if(buttonName.equals("changeInvSide")) {
			int side = dataStream.readInt();
			iterateInventoryExposure(side);
		}
	}
	
	// Helpers
	protected void iterateInventoryExposure(int side) {
		int slot = invExposures[side];
		slot++;
		if(slot >= getSizeInventory()) {
			slot = INVENTORY_UNEXPOSED;
		}
		
		this.setExposedInventorySlotReference(side, slot);
	}
	
	
	/**
	 * @param fromSlot The inventory slot into which this object would normally go.
	 * @param itemToDistribute An ItemStack to distribute to pipes
	 * @return Null if the stack was distributed, the same ItemStack otherwise.
	 */
	protected ItemStack distributeItemToPipes(int fromSlot, ItemStack itemToDistribute) {
		if(itemToDistribute == null) { return null; }

		ForgeDirection[] dirsToCheck = { ForgeDirection.NORTH, ForgeDirection.SOUTH,
										ForgeDirection.EAST, ForgeDirection.WEST };

		for(ForgeDirection dir : dirsToCheck) {
			// Are we exposed on that side?
			int rotatedSide = this.getRotatedSide(dir.ordinal());
			if(invExposures[rotatedSide] != fromSlot) { continue; }
			
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
	
	private void resetInventoryExposures() {
		invExposures = new int[6]; // 6 forge directions
		for(int i = 0; i < 6; i++) {
			invExposures[i] = INVENTORY_UNEXPOSED;
		}
	}
}

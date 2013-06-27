package erogenousbeef.bigreactors.common.tileentity;

import java.io.DataInputStream;
import java.util.HashMap;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import buildcraft.api.transport.IPipeEntry;
import erogenousbeef.bigreactors.api.IReactorFuel;
import erogenousbeef.bigreactors.client.gui.GuiReactorAccessPort;
import erogenousbeef.bigreactors.common.BRRegistry;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.block.BlockReactorPart;
import erogenousbeef.bigreactors.common.item.ItemIngot;
import erogenousbeef.bigreactors.gui.container.ContainerReactorAccessPort;
import erogenousbeef.bigreactors.gui.container.ContainerReactorController;
import erogenousbeef.bigreactors.net.PacketWrapper;
import erogenousbeef.bigreactors.net.Packets;
import erogenousbeef.core.common.CoordTriplet;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

public class TileEntityReactorAccessPort extends TileEntityReactorPart implements IInventory, ISidedInventory {

	protected ItemStack[] _inventories;
	
	public static final int SLOT_INLET = 0;
	public static final int SLOT_OUTLET = 1;
	public static final int NUM_SLOTS = 2;
	
	public TileEntityReactorAccessPort() {
		super();
		
		_inventories = new ItemStack[getSizeInventory()];
	}

	// TileEntity overrides
	
	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
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
	public int getSizeInventory() {
		return NUM_SLOTS;
	}

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
	public String getInvName() {
		return "Access Port";
	}

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
	public boolean isStackValidForSlot(int slot, ItemStack itemstack) {
		if(itemstack == null) { return true; }

		if(slot == SLOT_INLET) {
			return BRRegistry.getDataForFuel(itemstack) != null;
		}
		else if(slot == SLOT_OUTLET) {
			return BRRegistry.getDataForWaste(itemstack) != null;
		}
		
		return false;
	}

	// ISidedInventory
	
	@Override
	public int[] getAccessibleSlotsFromSide(int side) {
		if(side == 0 || side == 1) { return null; }
		
		int metadata = this.getBlockMetadata();
		if(metadata == BlockReactorPart.ACCESSPORT_INLET) {
			return new int[] {SLOT_INLET};
		}
		else if(metadata == BlockReactorPart.ACCESSPORT_OUTLET) {
			return new int[] {SLOT_OUTLET};
		}
		return null;
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
	
	@Override
	public void onNetworkPacket(int packetType, DataInputStream data) {
		if(packetType == Packets.AccessPortButton) {
			Class[] decodeAs = { Byte.class };
			Object[] decodedData = PacketWrapper.readPacketData(data, decodeAs);
			byte newMetadata = (Byte)decodedData[0];
			
			if(newMetadata == BlockReactorPart.ACCESSPORT_INLET || newMetadata == BlockReactorPart.ACCESSPORT_OUTLET) {
				this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, newMetadata, 2);
			}
		}
	}
	
	@Override
	public Object getContainer(InventoryPlayer inventoryPlayer) {
		return new ContainerReactorAccessPort(this, inventoryPlayer);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public Object getGuiElement(InventoryPlayer inventoryPlayer) {
		return new GuiReactorAccessPort(new ContainerReactorAccessPort(this, inventoryPlayer), this);
	}
	
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
					pipe.entityEntering(itemToDistribute.copy(), dir);
					return null;
				}
			}
		}
		
		return itemToDistribute;
	}

	/**
	 * Called when new waste has been placed in the access port
	 */
	public void onWasteReceived() {
		if(BlockReactorPart.ACCESSPORT_OUTLET == this.worldObj.getBlockMetadata(xCoord, yCoord, zCoord)) {
			_inventories[SLOT_OUTLET] = distributeItemToPipes(_inventories[SLOT_OUTLET]);
		}
	}
}

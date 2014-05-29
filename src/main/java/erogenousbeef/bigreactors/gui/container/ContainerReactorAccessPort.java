package erogenousbeef.bigreactors.gui.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorAccessPort;
import erogenousbeef.bigreactors.gui.slot.SlotReactorInput;
import erogenousbeef.bigreactors.gui.slot.SlotRemoveOnly;

public class ContainerReactorAccessPort extends Container {

	protected TileEntityReactorAccessPort _port;
	
	public ContainerReactorAccessPort(TileEntityReactorAccessPort port, InventoryPlayer inv) {
		super();
		_port = port;
		addSlots();
		addPlayerInventory(inv);
	}

	protected void addSlots() {
		// Input Slot
		addSlotToContainer(new SlotReactorInput(_port, 0, 44, 18, true));
	
		// Output Slot
		addSlotToContainer(new SlotRemoveOnly(_port, 1, 116, 18));
	}
	
	protected int getPlayerInventoryVerticalOffset()
	{
		return 74;
	}

	protected void addPlayerInventory(InventoryPlayer inventoryPlayer)
	{
		for (int i = 0; i < 3; i++)
		{
			for (int j = 0; j < 9; j++)
			{
					addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9, 8 + j * 18, getPlayerInventoryVerticalOffset() + i * 18));
			}
		}

		for (int i = 0; i < 9; i++)
		{
			addSlotToContainer(new Slot(inventoryPlayer, i, 8 + i * 18, getPlayerInventoryVerticalOffset() + 58));
		}
	}	
	
	@Override
	public boolean canInteractWith(EntityPlayer player)
	{
		return _port.isUseableByPlayer(player);
	}
	
	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slot)
	{
		ItemStack stack = null;
		Slot slotObject = (Slot) inventorySlots.get(slot);
		int numSlots = _port.getSizeInventory();

		if(slotObject != null && slotObject.getHasStack())
		{
			ItemStack stackInSlot = slotObject.getStack();
			stack = stackInSlot.copy();

			if(slot < numSlots)
			{
				if(!mergeItemStack(stackInSlot, numSlots, inventorySlots.size(), true))
				{
					return null;
				}
			}
			else if(!mergeItemStack(stackInSlot, 0, numSlots, false))
			{
				return null;
			}

			if(stackInSlot.stackSize == 0)
			{
				slotObject.putStack(null);
			}
			else
			{
				slotObject.onSlotChanged();
			}

			if(stackInSlot.stackSize == stack.stackSize)
			{
				return null;
			}

			slotObject.onPickupFromSlot(player, stackInSlot);
		}

		return stack;
	}

	// Override this so we can check if stuff is valid for the slot
	// Stolen directly from powercrystals
	@Override
	protected boolean mergeItemStack(ItemStack stack, int slotStart, int slotRange, boolean reverse)
	{
		boolean successful = false;
		int slotIndex = slotStart;
		int maxStack = Math.min(stack.getMaxStackSize(), _port.getInventoryStackLimit());

		if(reverse)
		{
			slotIndex = slotRange - 1;
		}

		Slot slot;
		ItemStack existingStack;

		if(stack.isStackable())
		{
			while(stack.stackSize > 0 && (!reverse && slotIndex < slotRange || reverse && slotIndex >= slotStart))
			{
				slot = (Slot)this.inventorySlots.get(slotIndex);
				existingStack = slot.getStack();

				if(slot.isItemValid(stack) && existingStack != null && existingStack.getItem() == stack.getItem() && (!stack.getHasSubtypes() || stack.getItemDamage() == existingStack.getItemDamage()) && ItemStack.areItemStackTagsEqual(stack, existingStack))
				{
					int existingSize = existingStack.stackSize + stack.stackSize;

					if(existingSize <= maxStack)
					{
						stack.stackSize = 0;
						existingStack.stackSize = existingSize;
						slot.onSlotChanged();
						successful = true;
					}
					else if (existingStack.stackSize < maxStack)
					{
						stack.stackSize -= maxStack - existingStack.stackSize;
						existingStack.stackSize = maxStack;
						slot.onSlotChanged();
						successful = true;
					}
				}

				if(reverse)
				{
					--slotIndex;
				}
				else
				{
					++slotIndex;
				}
			}
		}

		if(stack.stackSize > 0)
		{
			if(reverse)
			{
				slotIndex = slotRange - 1;
			}
			else
			{
				slotIndex = slotStart;
			}

			while(!reverse && slotIndex < slotRange || reverse && slotIndex >= slotStart)
			{
				slot = (Slot)this.inventorySlots.get(slotIndex);
				existingStack = slot.getStack();

				if(slot.isItemValid(stack) && existingStack == null)
				{
					slot.putStack(stack.copy());
					slot.onSlotChanged();
					stack.stackSize = 0;
					successful = true;
					break;
				}

				if(reverse)
				{
					--slotIndex;
				}
				else
				{
					++slotIndex;
				}
			}
		}

		return successful;
	}
}

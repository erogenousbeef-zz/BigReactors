package erogenousbeef.bigreactors.gui.container;

import erogenousbeef.bigreactors.common.tileentity.TileEntityReactorAccessPort;
import erogenousbeef.bigreactors.gui.slot.SlotReactorFuelOnly;
import erogenousbeef.bigreactors.gui.slot.SlotRemoveOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

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
		addSlotToContainer(new SlotReactorFuelOnly(_port, 0, 44, 41));
	
		// Output Slot
		addSlotToContainer(new SlotRemoveOnly(_port, 1, 116, 41));
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
			else if(!mergeItemStack(stackInSlot, 0, 9, false))
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

}

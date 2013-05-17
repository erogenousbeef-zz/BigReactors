package erogenousbeef.bigreactors.gui.container;

import erogenousbeef.bigreactors.common.tileentity.TileEntityCyaniteReprocessor;
import erogenousbeef.bigreactors.common.tileentity.TileEntityReactorAccessPort;
import erogenousbeef.bigreactors.gui.slot.SlotReactorFuelOnly;
import erogenousbeef.bigreactors.gui.slot.SlotRemoveOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerCyaniteReprocessor extends Container {

	protected TileEntityCyaniteReprocessor _entity;
	
	public ContainerCyaniteReprocessor(TileEntityCyaniteReprocessor entity, EntityPlayer player) {
		super();
		_entity = entity;
		addSlots();
		addPlayerInventory(player.inventory);
		_entity.beginUpdatingPlayer(player);
	}

	protected void addSlots() {
		// Input Slot
		addSlotToContainer(new SlotReactorFuelOnly(_entity, 0, 44, 41));
	
		// Output Slot
		addSlotToContainer(new SlotRemoveOnly(_entity, 1, 116, 41));
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
		return _entity.isUseableByPlayer(player);
	}
	
	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slot)
	{
		ItemStack stack = null;
		Slot slotObject = (Slot) inventorySlots.get(slot);
		int numSlots = _entity.getSizeInventory();

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
	
	// Update subscription
	
	@Override
    public void onCraftGuiClosed(EntityPlayer player) {
		super.onCraftGuiClosed(player);
		
		_entity.stopUpdatingPlayer(player);
	}
	
}

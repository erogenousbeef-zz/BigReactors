package erogenousbeef.bigreactors.common;

import buildcraft.api.tools.IToolWrench;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.fluids.IFluidTank;

public class BRUtilities {
	/**
	 * Attempts to fill tank with the player's current item.
	 * @param	destinationTank			the tank the fluid is going into
	 * @param	entityPlayer	the player trying to fill the tank
	 * @return	True if fluid was transferred to the tank.
	 */
	public static boolean fillTankFromBucket(IFluidHandler destinationTank, EntityPlayer entityPlayer)
	{
		ItemStack currentItem = entityPlayer.inventory.getCurrentItem();
		FluidStack fluid = FluidContainerRegistry.getFluidForFilledItem(currentItem);
		if(fluid != null)
		{
			if(destinationTank.fill(ForgeDirection.UNKNOWN, fluid, false) == fluid.amount)
			{
				destinationTank.fill(ForgeDirection.UNKNOWN, fluid, true);
				if(!entityPlayer.capabilities.isCreativeMode)
				{
					entityPlayer.inventory.setInventorySlotContents(entityPlayer.inventory.currentItem, consumeItem(currentItem));					
				}
				return true;
			}
		}
		return false;
	}

	/**
	 * Attempts to drain tank into the player's current item.
	 * @param	te			the tank the fluid is coming from
	 * @param	entityPlayer	the player trying to take fluid from the tank
	 * @return	True if fluid was transferred from the tank.
	 */
	public static boolean fillBucketFromTank(IFluidHandler te, EntityPlayer entityPlayer)
	{
		ItemStack currentItem = entityPlayer.inventory.getCurrentItem();
		if(FluidContainerRegistry.isEmptyContainer(currentItem))
		{
			FluidTankInfo[] tankInfos = te.getTankInfo(ForgeDirection.UNKNOWN);
			for(FluidTankInfo tankInfo : tankInfos) {
				if(tankInfo.fluid.amount > 0) {
					ItemStack filledBucket = FluidContainerRegistry.fillFluidContainer(tankInfo.fluid, currentItem);
					if(FluidContainerRegistry.isFilledContainer((filledBucket))) {
						te.drain(ForgeDirection.UNKNOWN, tankInfo.fluid, true);
						if(entityPlayer.capabilities.isCreativeMode)
						{
							return true;
						}
						else if(currentItem.stackSize == 1)
						{
							entityPlayer.inventory.setInventorySlotContents(entityPlayer.inventory.currentItem, filledBucket);
							return true;
						}
						else if(entityPlayer.inventory.addItemStackToInventory(filledBucket))
						{
							currentItem.stackSize -= 1;
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	
	/**
	 * Consume a single item from a stack of items
	 * @param stack The stack from which to consume
	 * @return The remainder of the stack, or null if the stack was fully consumed.
	 */
	public static ItemStack consumeItem(ItemStack stack)
	{
		return consumeItem(stack, 1);
	}
	
	/**
	 * Consume some amount of items from a stack of items. Assumes you've already validated
	 * the consumption. If you try to consume more than the stack has, it will simply destroy
	 * the stack, as if you'd consumed all of it.
	 * @param stack The stack from which to consume
	 * @return The remainder of the stack, or null if the stack was fully consumed.
	 */
	public static ItemStack consumeItem(ItemStack stack, int amount)
	{
		if(stack.stackSize <= amount)
		{
			if(stack.getItem().hasContainerItem())
			{
				return stack.getItem().getContainerItemStack(stack);
			}
			else
			{
				return null;
			}
		}
		else
		{
			stack.stackSize -= amount;
			return stack;
		}	
	}
	
	/**
	 * Is this player holding a goddamn wrench?
	 * @return True if the player is holding a goddamn wrench. BC only, screw you.
	 */
	public static boolean isPlayerHoldingWrench(EntityPlayer player) {
		if(player.inventory.getCurrentItem() == null) { 
			return false;
		}
		Item currentItem = Item.itemsList[player.inventory.getCurrentItem().itemID];
		return currentItem instanceof IToolWrench;
	}
}

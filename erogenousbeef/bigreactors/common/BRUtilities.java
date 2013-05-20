package erogenousbeef.bigreactors.common;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.ILiquidTank;
import net.minecraftforge.liquids.ITankContainer;
import net.minecraftforge.liquids.LiquidContainerRegistry;
import net.minecraftforge.liquids.LiquidStack;

public class BRUtilities {
	/**
	 * Attempts to fill tank with the player's current item.
	 * @param	destinationTank			the tank the liquid is going into
	 * @param	entityPlayer	the player trying to fill the tank
	 * @return	True if liquid was transferred to the tank.
	 */
	public static boolean fillTankFromBucket(ITankContainer destinationTank, EntityPlayer entityPlayer)
	{
		ItemStack currentItem = entityPlayer.inventory.getCurrentItem();
		LiquidStack liquid = LiquidContainerRegistry.getLiquidForFilledItem(currentItem);
		if(liquid != null)
		{
			if(destinationTank.fill(ForgeDirection.UNKNOWN, liquid, false) == liquid.amount)
			{
				destinationTank.fill(ForgeDirection.UNKNOWN, liquid, true);
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
	 * @param	sourceTank			the tank the liquid is coming from
	 * @param	entityPlayer	the player trying to take liquid from the tank
	 * @return	True if liquid was transferred from the tank.
	 */
	public static boolean fillBucketFromTank(ITankContainer sourceTank, EntityPlayer entityPlayer)
	{
		ItemStack currentItem = entityPlayer.inventory.getCurrentItem();
		if(LiquidContainerRegistry.isEmptyContainer(currentItem))
		{
			for(ILiquidTank tank : sourceTank.getTanks(ForgeDirection.UNKNOWN))
			{
				LiquidStack tankLiquid = tank.getLiquid();
				ItemStack filledBucket = LiquidContainerRegistry.fillLiquidContainer(tankLiquid, currentItem);
				if(LiquidContainerRegistry.isFilledContainer(filledBucket))
				{
					LiquidStack bucketLiquid = LiquidContainerRegistry.getLiquidForFilledItem(filledBucket);
					if(entityPlayer.capabilities.isCreativeMode)
					{
						tank.drain(bucketLiquid.amount, true);
						return true;
					}
					else if(currentItem.stackSize == 1)
					{
						tank.drain(bucketLiquid.amount, true);
						entityPlayer.inventory.setInventorySlotContents(entityPlayer.inventory.currentItem, filledBucket);
						return true;
					}
					else if(entityPlayer.inventory.addItemStackToInventory(filledBucket))
					{
						tank.drain(bucketLiquid.amount, true);
						currentItem.stackSize -= 1;
						return true;
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
			return stack.splitStack(amount);
		}	
	}	
}

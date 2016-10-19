package erogenousbeef.bigreactors.utils;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;
import buildcraft.api.tools.IToolWrench;
import cofh.api.item.IToolHammer;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.utils.intermod.ModHelperBase;
import erogenousbeef.core.common.CoordTriplet;

public class StaticUtils {

	public static final ForgeDirection[] CardinalDirections = new ForgeDirection[] { ForgeDirection.NORTH, ForgeDirection.EAST, ForgeDirection.SOUTH, ForgeDirection.WEST };

	public static final ForgeDirection neighborsBySide[][] = new ForgeDirection[][] {
		{ForgeDirection.NORTH, ForgeDirection.SOUTH, ForgeDirection.WEST, ForgeDirection.EAST},
		{ForgeDirection.NORTH, ForgeDirection.SOUTH, ForgeDirection.WEST, ForgeDirection.EAST},
		{ForgeDirection.UP, ForgeDirection.DOWN, ForgeDirection.EAST, ForgeDirection.WEST},
		{ForgeDirection.UP, ForgeDirection.DOWN, ForgeDirection.WEST, ForgeDirection.EAST},
		{ForgeDirection.UP, ForgeDirection.DOWN, ForgeDirection.NORTH, ForgeDirection.SOUTH},
		{ForgeDirection.UP, ForgeDirection.DOWN, ForgeDirection.SOUTH, ForgeDirection.NORTH}
	};

	public static class Inventory {
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
			if(stack == null) { return null; }

			if(stack.stackSize <= amount)
			{
				if(stack.getItem().hasContainerItem(stack))
				{
					return stack.getItem().getContainerItem(stack);
				}
				else
				{
					return null;
				}
			}
			else
			{
				stack.splitStack(amount);
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
			Item currentItem = player.inventory.getCurrentItem().getItem();
			return (ModHelperBase.useCofh && currentItem instanceof IToolHammer) ||
					(ModHelperBase.useBuildcraftTools && currentItem instanceof IToolWrench);
		}

		/**
		 * Check to see if two stacks are equal. NBT-sensitive.
		 * Lifted from PowerCrystalsCore.
		 * @param s1 First stack to compare
		 * @param s2 Second stack to compare
		 * @return True if stacks are equal, false otherwise.
		 */
        public static boolean areStacksEqual(ItemStack s1, ItemStack s2)
        {
                return areStacksEqual(s1, s2, true);
        }

        /**
		 * Check to see if two stacks are equal. Optionally NBT-sensitive.
		 * Lifted from PowerCrystalsCore.
		 * @param s1 First stack to compare
		 * @param s2 Second stack to compare
         * @param nbtSensitive True if stacks' NBT tags should be checked for equality.
         * @return True if stacks are equal, false otherwise.
         */
        public static boolean areStacksEqual(ItemStack s1, ItemStack s2, boolean nbtSensitive)
        {
                if(s1 == null || s2 == null) return false;
                if(!s1.isItemEqual(s2)) return false;
                
                if(nbtSensitive)
                {
                        if(s1.getTagCompound() == null && s2.getTagCompound() == null) return true;
                        if(s1.getTagCompound() == null || s2.getTagCompound() == null) return false;
                        return s1.getTagCompound().equals(s2.getTagCompound());
                }
                
                return true;
        }

        private static final ForgeDirection[] chestDirections = new ForgeDirection[] { ForgeDirection.NORTH,
        																				ForgeDirection.SOUTH,
        																				ForgeDirection.EAST,
        																				ForgeDirection.WEST};

		public static IInventory checkForDoubleChest(World worldObj, IInventory te, int x, int y, int z) {
			for(ForgeDirection dir : chestDirections) {
				if(worldObj.getBlock(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ) == Blocks.chest) {
					TileEntity otherTe = worldObj.getTileEntity(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ);
					if(otherTe instanceof IInventory) {
						return new InventoryLargeChest("Large Chest", te, (IInventory)otherTe);
					}
				}
			}

			// Not a large chest, so just return the single chest.
			return te;
		}
	}

	public static class Fluids {
		/* Below stolen from COFHLib because COFHLib itself still relies on cofh.core */
		public static boolean fillTankWithContainer(World world, IFluidHandler handler, EntityPlayer player) {

	        ItemStack container = player.getCurrentEquippedItem();
	        FluidStack fluid = FluidContainerRegistry.getFluidForFilledItem(container);

	        if (fluid != null) {
	                if (handler.fill(ForgeDirection.UNKNOWN, fluid, false) == fluid.amount || player.capabilities.isCreativeMode) {
	                        if (world.isRemote) {
	                                return true;
	                        }
	                        handler.fill(ForgeDirection.UNKNOWN, fluid, true);

	                        if (!player.capabilities.isCreativeMode) {
	                                player.inventory.setInventorySlotContents(player.inventory.currentItem, Inventory.consumeItem(container));
	                        }
	                        return true;
	                }
	        }
	        return false;
		}

		public static boolean fillContainerFromTank(World world, IFluidHandler handler, EntityPlayer player, FluidStack tankFluid) {
			ItemStack container = player.getCurrentEquippedItem();

			if (FluidContainerRegistry.isEmptyContainer(container)) {
			        ItemStack returnStack = FluidContainerRegistry.fillFluidContainer(tankFluid, container);
			        FluidStack fluid = FluidContainerRegistry.getFluidForFilledItem(returnStack);

			        if (fluid == null || returnStack == null) {
			                return false;
			        }
			        if (!player.capabilities.isCreativeMode) {
			                if (container.stackSize == 1) {
			                        container = container.copy();
			                        player.inventory.setInventorySlotContents(player.inventory.currentItem, returnStack);
			                } else if (!player.inventory.addItemStackToInventory(returnStack)) {
			                        return false;
			                }
			                handler.drain(ForgeDirection.UNKNOWN, fluid.amount, true);
			                container.stackSize--;

			                if (container.stackSize <= 0) {
			                        container = null;
			                }
			        } else {
			                handler.drain(ForgeDirection.UNKNOWN, fluid.amount, true);
			        }
			        return true;
			}
			return false;
		}
	}

	public static class ExtraMath {
		/**
		 * Linear interpolate between two numbers.
		 * @param from
		 * @param to
		 * @param modifier
		 * @return
		 */
		public static float Lerp(float from, float to, float modifier)
		{
			modifier = Math.min(1f, Math.max(0f, modifier));
		    return from + modifier * (to - from);
		}

		/**
		 * Calculate the volume of the cube defined by two coordinates.
		 * @param minimum Minimum coordinate.
		 * @param maximum Maximum coordinate.
		 * @return The cube's volume, in blocks.
		 */
		public static int Volume(CoordTriplet minimum, CoordTriplet maximum) {
			if(minimum == null || maximum == null) { return 0; }

			int xsize = Math.abs(maximum.x - minimum.x) + 1;
			int ysize = Math.abs(maximum.y - minimum.y) + 1;
			int zsize = Math.abs(maximum.z - minimum.z) + 1;
			return xsize * ysize * zsize;
		}
	}

	public static class Energy {
		public static float RFPerCentigradePerUnitVolume = 10f;

		public static float getRFFromVolumeAndTemp(int volume, float temperature) {
			return temperature * volume * RFPerCentigradePerUnitVolume;
		}

		public static float getTempFromVolumeAndRF(int volume, float rf) {
			return rf / (volume * RFPerCentigradePerUnitVolume);
		}
	}

	public static class Strings {
		public static String[] sizePrefixes = {"", "Ki", "Me", "Gi", "Te", "Pe", "Ex", "Ze", "Yo", "Ho"};
		// Ho = Hojillion

		public static String formatRF(float number) {
			String prefix = "";
			if(number < 0f) {
				prefix = "-";
				number *= -1;
			}

			if(number <= 0.00001f) { return "0.00 RF"; }

			int power = (int)Math.floor(Math.log10(number));

			int decimalPoints = 2 - (power % 3);
			int letterIdx = Math.max(0, Math.min(sizePrefixes.length, power / 3));
			double divisor = Math.pow(1000f, letterIdx);

			if(divisor > 0) {
				return String.format("%s%." + Integer.toString(decimalPoints) + "f %sRF", prefix, number/divisor, sizePrefixes[letterIdx]);
			}
			else {
				return String.format("%s%." + Integer.toString(decimalPoints) + "f RF", prefix, number);
			}
		}

		public static String formatMillibuckets(float number) {
			String prefix = "";
			if(number < 0f) {
				prefix = "-";
				number *= -1;
			}

			if(number <= 0.00001f) { return "0.000 mB"; }
			int power = (int)Math.floor(Math.log10(number));
			if(power < 1) {
				return String.format("%.3f mB", number);
			}
			else if(power < 2) {
				return String.format("%.2f mB", number);
			}
			else if(power < 3) {
				return String.format("%.1f mB", number);
			}
			else if(power < 4) {
				return String.format("%.0f mB", number);
			}
			else {
				number /= 1000f; // Re-render into buckets
				if(power < 5) {
					return String.format("%.2f B", number);
				}
				else if(power < 6) {
					return String.format("%.1f B", number);
				}
				else {
					return String.format("%.0f B", number);
				}
			}
		}
	}

	// Mob = Mobile = Entity
	public static class Mob {
		/**
		 * @param entity The entity whose facing you wish to query.
		 * @return The ForgeDirection which entity is facing (north/south/east/west)
		 */
		protected ForgeDirection getFacingDirection(Entity entity) {
			int facingAngle = (MathHelper.floor_double((entity.rotationYaw * 4F) / 360F + 0.5D) & 3);
			switch(facingAngle) {
			case 1:
				return ForgeDirection.EAST;
			case 2:
				return ForgeDirection.SOUTH;
			case 3:
				return ForgeDirection.WEST;
			default:
				return ForgeDirection.NORTH;
			}
		}
	}

	public static class TE {
		public static TileEntity getTileEntityUnsafe(IBlockAccess iba, int x, int y, int z) {
			TileEntity te = null;

			if(iba instanceof World) {
				// We don't want to trigger tile entity loads in this method
				te = getTileEntityUnsafe((World)iba, x, y, z);
			}
			else {
				// Should never happen, generally
				te = iba.getTileEntity(x, y, z);
			}

			return te;
		}

		public static TileEntity getTileEntityUnsafe(World world, int x, int y, int z) {
			TileEntity te = null;

			Chunk chunk = world.getChunkFromBlockCoords(x, z);
			if(chunk != null) {
				te = chunk.getTileEntityUnsafe(x & 0x0F, y, z & 0x0F);
			}

			return te;
		}
	}

	public static class WorldGen {
		/**
		 * Check if a Big Reactors world generator should even bother to run
		 * in a given dimension.
		 * @param dimensionId The dimension being queried for WorldGen.
		 * @return True if world generation should proceed, false if it should be skipped altogether.
		 */
		public static boolean shouldGenerateInDimension(int dimensionId) {
			return dimensionId >= 0 || BigReactors.enableWorldGenInNegativeDimensions ||
					BigReactors.dimensionWhitelist.contains(dimensionId);
		}
	}
}

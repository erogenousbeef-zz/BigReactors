package erogenousbeef.bigreactors.utils;

import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.api.transport.IInjectable;
import buildcraft.api.transport.IPipeTile;
import cofh.api.transport.IItemDuct;
import erogenousbeef.bigreactors.utils.intermod.ModHelperBase;

/**
 * Wraps a given direction's inventory exposure. Listen for block/tile changes and set
 * whatever tile entity you find in that direction as this object's
 * tracked tile entity.
 * 
 * You can then use distribute() to safely try to distribute items.
 * @author Erogenous Beef
 */
public class AdjacentInventoryHelper {

	private ForgeDirection dir;
	private TileEntity entity;
	private IItemDuct duct;
	private IPipeTile pipe;
	private InventoryHelper inv;

	/**
	 * @param dir The direction away from the current tile entity which this wrapper represents.
	 */
	public AdjacentInventoryHelper(ForgeDirection dir) {
		this.dir = dir;
		entity = null;
		duct = null;
		pipe = null;
		inv = null;
	}

	/**
	 * Attempt to distribute an item to a cached inventory connection
	 * which is wrapped by this object.
	 * @param itemToDistribute An itemstack to distribute.
	 * @return An itemstack containing the remaining items, or null if all items were distributed.
	 */
	public ItemStack distribute(ItemStack itemToDistribute) {
		if(entity == null || itemToDistribute == null) {
			return itemToDistribute;
		}
		
		if(!hasConnection()) {
			return itemToDistribute;
		}

		if(ModHelperBase.useCofh && duct != null) {
			itemToDistribute = duct.insertItem(dir.getOpposite(), itemToDistribute);
		}
		else if(ModHelperBase.useBuildcraftTransport && pipe != null) {
			if(pipe.isPipeConnected(dir.getOpposite())) {
				itemToDistribute.stackSize -= pipe.injectItem(itemToDistribute.copy(), true, dir.getOpposite(), null);
				
				if(itemToDistribute.stackSize <= 0) {
					itemToDistribute = null;
				}
			}

		}
		else if(inv != null) {
			itemToDistribute = inv.addItem(itemToDistribute);
		}

		return itemToDistribute;
	}
	
	public boolean hasConnection() {
		return inv != null || pipe != null || duct != null;
	}
	
	/**
	 * @param te The new tile entity for this helper to cache.
	 * @return True if this helper's wrapped inventory changed, false otherwise.
	 */
	public boolean set(TileEntity te) {
		if(entity == te) { return false; }
		
		if(te == null) {
			duct = null;
			pipe = null;
			inv = null;
		}
		else if(ModHelperBase.useCofh && te instanceof IItemDuct) {
			setDuct((IItemDuct)te);
		}
		else if(ModHelperBase.useBuildcraftTransport && te instanceof IPipeTile) {
			setPipe((IPipeTile)te);
		}
		else if(te instanceof IInventory) {
			setInv(te);
		}
		
		entity = te;
		return true;
	}
	
	private void setDuct(IItemDuct duct) {
		this.duct = duct;
		this.pipe = null;
		this.inv = null;
	}
	
	private void setPipe(IPipeTile pipe) {
		this.pipe = pipe;
		this.duct = null;
		this.inv = null;
	}
	
	private void setInv(TileEntity te) {
		this.pipe = null;
		this.duct = null;
		this.inv = null;

		if(te instanceof ISidedInventory) {
			this.inv = new SidedInventoryHelper((ISidedInventory)te, dir.getOpposite());
		}
		else {
			IInventory inv = (IInventory)te;
			World world = te.getWorldObj();
			if(world.getBlock(te.xCoord, te.yCoord, te.zCoord) == Blocks.chest) {
				inv = StaticUtils.Inventory.checkForDoubleChest(world, inv, te.xCoord, te.yCoord, te.zCoord);
			}
			this.inv = new InventoryHelper(inv);
		}
	}
}

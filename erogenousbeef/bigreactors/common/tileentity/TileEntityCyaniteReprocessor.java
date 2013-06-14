package erogenousbeef.bigreactors.common.tileentity;

import java.util.ArrayList;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.liquids.LiquidContainerRegistry;
import net.minecraftforge.liquids.LiquidDictionary;
import net.minecraftforge.liquids.LiquidStack;
import net.minecraftforge.oredict.OreDictionary;
import erogenousbeef.bigreactors.client.gui.GuiCyaniteReprocessor;
import erogenousbeef.bigreactors.common.BRUtilities;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.block.BlockReactorPart;
import erogenousbeef.bigreactors.common.item.ItemIngot;
import erogenousbeef.bigreactors.common.tileentity.base.TileEntityPoweredInventoryLiquid;
import erogenousbeef.bigreactors.gui.container.ContainerCyaniteReprocessor;

public class TileEntityCyaniteReprocessor extends TileEntityPoweredInventoryLiquid {

	public static final int SLOT_INLET = 0;
	public static final int SLOT_OUTLET = 1;
	public static final int NUM_SLOTS = 2;
	
	protected static final int LIQUID_CONSUMED = LiquidContainerRegistry.BUCKET_VOLUME * 1;
	protected static final int INGOTS_CONSUMED = 2;
	
	public TileEntityCyaniteReprocessor() {
		super();
	}

	@Override
	public int getSizeInventory() {
		return NUM_SLOTS;
	}

	@Override
	public String getInvName() {
		return "Cyanite Reprocessor";
	}

	@Override
	public boolean isStackValidForSlot(int slot, ItemStack itemstack) {
		if(itemstack == null) { return true; }
		
		// TODO: Fix this to use the registry
		if(itemstack.itemID == BigReactors.ingotGeneric.itemID) {
			if(ItemIngot.isFuel(itemstack.getItemDamage()) && slot == SLOT_OUTLET) {
				return true;
			}
			else if(ItemIngot.isWaste(itemstack.getItemDamage()) && slot == SLOT_INLET) {
				return true;
			}
		}
		
		return false;
	}

	@Override
	public float getMaxEnergyStored() {
		return 1000f;
	}

	@Override
	public float getCycleEnergyCost() {
		return 100f;
	}

	@Override
	public int getCycleLength() {
		return 200; // 10 seconds / 20tps
	}

	@Override
	public boolean canBeginCycle() {
		LiquidStack liquid = drain(0, LIQUID_CONSUMED, false);
		if(liquid == null || liquid.amount < LIQUID_CONSUMED) {
			return false;
		}

		if(_inventories[SLOT_INLET] != null && _inventories[SLOT_INLET].stackSize >= INGOTS_CONSUMED) {
			if(_inventories[SLOT_OUTLET] != null && _inventories[SLOT_OUTLET].stackSize >= getInventoryStackLimit()) {
				return false;
			}
			return true;
		}

		return false;
	}

	@Override
	public void onPoweredCycleBegin() {
	}

	@Override
	public void onPoweredCycleEnd() {
		if(_inventories[SLOT_OUTLET] != null) {
			if(consumeInputs()) {
				_inventories[SLOT_OUTLET].stackSize += 1;
			}
		}
		else {
			// TODO: Make this query the input for the right type of output to create.
			ArrayList<ItemStack> candidates = OreDictionary.getOres("ingotPlutonium");
			if(candidates.isEmpty()) {
				// WTF?
				return;
			}
			
			if(consumeInputs()) {
				_inventories[SLOT_OUTLET] = candidates.get(0).copy();
				_inventories[SLOT_OUTLET].stackSize = 1;
			}
		}
		
		_inventories[SLOT_OUTLET] = distributeItemToPipes(SLOT_OUTLET, _inventories[SLOT_OUTLET]);
	}
	
	private boolean consumeInputs() {
		_inventories[SLOT_INLET] = BRUtilities.consumeItem(_inventories[SLOT_INLET], INGOTS_CONSUMED);
		drain(0, LIQUID_CONSUMED, true);
		
		return true;
	}

	@Override
	public int getNumTanks() {
		return 1;
	}

	@Override
	public int getTankSize(int tankIndex) {
		return LiquidContainerRegistry.BUCKET_VOLUME * 5;
	}

	@Override
	protected boolean isLiquidValidForTank(int tankIdx, LiquidStack type) {
		if(type == null) { return false; }
		return type.isLiquidEqual(LiquidDictionary.getCanonicalLiquid("Water"));
	}

	@Override
	public GuiScreen getGUI(EntityPlayer player) {
		return new GuiCyaniteReprocessor(getContainer(player), this);
	}

	@Override
	public Container getContainer(EntityPlayer player) {
		return new ContainerCyaniteReprocessor(this, player);
	}
}

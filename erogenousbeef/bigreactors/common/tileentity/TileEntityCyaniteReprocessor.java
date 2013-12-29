package erogenousbeef.bigreactors.common.tileentity;

import java.util.ArrayList;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import erogenousbeef.bigreactors.api.IReactorFuel;
import erogenousbeef.bigreactors.client.gui.GuiCyaniteReprocessor;
import erogenousbeef.bigreactors.common.BRRegistry;
import erogenousbeef.bigreactors.common.tileentity.base.TileEntityPoweredInventoryFluid;
import erogenousbeef.bigreactors.gui.container.ContainerCyaniteReprocessor;
import erogenousbeef.bigreactors.utils.StaticUtils;

public class TileEntityCyaniteReprocessor extends TileEntityPoweredInventoryFluid {

	public static final int SLOT_INLET = 0;
	public static final int SLOT_OUTLET = 1;
	public static final int NUM_SLOTS = 2;
	
	protected static final int FLUID_CONSUMED = FluidContainerRegistry.BUCKET_VOLUME * 1;
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
	public boolean isItemValidForSlot(int slot, ItemStack itemstack) {
		if(itemstack == null) { return true; }
		
		IReactorFuel data = BRRegistry.getDataForSolid(itemstack);
		
		if(data == null) { return false; }
		
		if(slot == SLOT_OUTLET) {
			return data.isFuel();
		}
		else if(slot == SLOT_INLET) {
			return data.isWaste();
		}
		
		return false;
	}

	@Override
	public int getMaxEnergyStored() {
		return 10000;
	}

	@Override
	public int getCycleEnergyCost() {
		return 2000;
	}

	@Override
	public int getCycleLength() {
		return 200; // 10 seconds / 20tps
	}

	@Override
	public boolean canBeginCycle() {
		FluidStack fluid = drain(0, FLUID_CONSUMED, false);
		if(fluid == null || fluid.amount < FLUID_CONSUMED) {
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
		_inventories[SLOT_INLET] = StaticUtils.Inventory.consumeItem(_inventories[SLOT_INLET], INGOTS_CONSUMED);
		drain(0, FLUID_CONSUMED, true);
		
		return true;
	}

	@Override
	public int getNumTanks() {
		return 1;
	}

	@Override
	public int getTankSize(int tankIndex) {
		return FluidContainerRegistry.BUCKET_VOLUME * 5;
	}

	@Override
	protected boolean isFluidValidForTank(int tankIdx, FluidStack type) {
		if(type == null) { return false; }
		return type.getFluid().getID() == FluidRegistry.getFluid("water").getID();
	}

	@SideOnly(Side.CLIENT)
	@Override
	public GuiScreen getGUI(EntityPlayer player) {
		return new GuiCyaniteReprocessor(getContainer(player), this);
	}

	@Override
	public Container getContainer(EntityPlayer player) {
		return new ContainerCyaniteReprocessor(this, player);
	}
	
	@Override
	protected int getDefaultTankForFluid(Fluid fluid) {
		if(fluid.getName() == "water")
			return 0;
		else
			return FLUIDTANK_NONE;
	}
}

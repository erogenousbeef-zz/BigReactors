package erogenousbeef.bigreactors.common.tileentity;

import java.io.DataInputStream;
import java.io.IOException;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.tileentity.base.TileEntityBeefBase;
import erogenousbeef.bigreactors.common.tileentity.base.TileEntityPoweredInventoryFluid;

public class TileEntityHeatGenerator extends TileEntityPoweredInventoryFluid {

	protected static final int TANK_WATER = 0;
	protected static final int TANK_STEAM = 1;
	
	public TileEntityHeatGenerator() {
		super();
	}
	
	@Override
	public GuiScreen getGUI(EntityPlayer player) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Container getContainer(EntityPlayer player) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onReceiveGuiButtonPress(String buttonName,
			DataInputStream dataStream) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public int getNumTanks() {
		return 2;
	}

	@Override
	public int getTankSize(int tankIndex) {
		return 1000;
	}

	@Override
	protected boolean isFluidValidForTank(int tankIdx, FluidStack type) {
		if(tankIdx == TANK_WATER && type.getFluid().getID() == FluidRegistry.WATER.getID()) {
			return true;
		}
		else if(tankIdx == TANK_STEAM) {
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	protected int getDefaultTankForFluid(Fluid fluid) {
		// TODO Auto-generated method stub
		if(fluid.getID() == FluidRegistry.WATER.getID())
			return TANK_WATER;
		else if(fluid.getID() == BigReactors.fluidSteam) // TODO: STEAM
			return TANK_STEAM;
		else
			return FLUIDTANK_NONE;
	}

	@Override
	protected int getMaxEnergyStored() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getCycleEnergyCost() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getCycleLength() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean canBeginCycle() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onPoweredCycleBegin() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPoweredCycleEnd() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getSizeInventory() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getInvName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack itemstack) {
		// TODO Auto-generated method stub
		return false;
	}
}

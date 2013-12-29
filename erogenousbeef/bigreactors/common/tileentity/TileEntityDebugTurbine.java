package erogenousbeef.bigreactors.common.tileentity;

import java.io.DataInputStream;
import java.io.IOException;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import erogenousbeef.bigreactors.client.gui.GuiDebugTurbine;
import erogenousbeef.bigreactors.client.gui.GuiHeatGenerator;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.tileentity.base.TileEntityPoweredInventoryFluid;
import erogenousbeef.bigreactors.gui.container.ContainerSlotless;
import erogenousbeef.bigreactors.gui.container.ISlotlessUpdater;
import erogenousbeef.bigreactors.utils.StaticUtils;

public class TileEntityDebugTurbine extends TileEntityPoweredInventoryFluid implements ISlotlessUpdater {

	protected static final int TANK_WATER = 0;
	protected static final int TANK_STEAM = 1;
	
	public enum VentStatus {
		DoNotVent,
		VentOverflow,
		VentAll,
	};
	
	protected boolean isActive;
	protected VentStatus ventStatus;
	
	public TileEntityDebugTurbine() {
		super();
		
		isActive = false;
		ventStatus = VentStatus.DoNotVent;
	}
	
	@Override
	public void updateEntity() {
		super.updateEntity();
		
		if(isActive) {
			// Spin up via steam inputs, convert some steam back into water
			
		}
		else {
			// Spin down if spun up
		}
		
		IFluidTank[] tanks = getTanks(ForgeDirection.UNKNOWN);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen getGUI(EntityPlayer player) {
		return new GuiDebugTurbine(getContainer(player), this);
	}

	@Override
	public Container getContainer(EntityPlayer player) {
		return new ContainerSlotless(this, player);
	}
	
	@Override
	public void onReceiveGuiButtonPress(String buttonName, DataInputStream dataStream) throws IOException {
		super.onReceiveGuiButtonPress(buttonName, dataStream);
		
		if(buttonName.equals("active")) {
			isActive = !isActive;
			worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
			return;
		}
		
		if(buttonName.equals("ventNone")) {
			ventStatus = VentStatus.DoNotVent;
			worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
			return;
		}
		
		if(buttonName.equals("ventOverflow")) {
			ventStatus = VentStatus.VentOverflow;
			worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
			return;
		}

		if(buttonName.equals("ventAll")) {
			ventStatus = VentStatus.VentAll;
			worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
			return;
		}
	}

	@Override
	protected void onSendUpdate(NBTTagCompound updateTag) {
		super.onSendUpdate(updateTag);
		
		// TODO
		updateTag.setBoolean("active", isActive);
		updateTag.setInteger("vent", ventStatus.ordinal());
	}
	
	@Override
	public void onReceiveUpdate(NBTTagCompound updateTag) {
		super.onReceiveUpdate(updateTag);
		
		isActive = updateTag.getBoolean("active");
		ventStatus = VentStatus.values()[updateTag.getInteger("vent")];
		// TODO
	}

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		
		if(tag.hasKey("active")) {
			isActive = tag.getBoolean("active");
		}
		else {
			isActive = false;
		}
		
		if(tag.hasKey("vent")) {
			ventStatus = VentStatus.values()[tag.getInteger("vent")];
		}
		else {
			ventStatus = VentStatus.DoNotVent;
		}

		// TODO
	}
	
	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		
		tag.setBoolean("active", isActive);
		tag.setInteger("vent", ventStatus.ordinal());
	}
	
	// Fluids
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
		if(tankIdx == TANK_WATER && type.getFluid().getID() == FluidRegistry.WATER.getID())
			return true;
		else if(tankIdx == TANK_STEAM && type.getFluid().getID() == BigReactors.fluidSteam.getID())
			return true;
		else
			return false;
	}

	@Override
	protected int getDefaultTankForFluid(Fluid fluid) {
		if(fluid.getID() == FluidRegistry.WATER.getID())
			return TANK_WATER;
		else if(fluid.getID() == BigReactors.fluidSteam.getID())
			return TANK_STEAM;
		else
			return FLUIDTANK_NONE;
	}

	// Powered
	@Override
	protected int getMaxEnergyStored() {
		return 0;
	}

	@Override
	public int getCycleEnergyCost() {
		return 0;
	}

	@Override
	public int getCycleLength() {
		return 0;
	}

	@Override
	public boolean canBeginCycle() {
		return false;
	}

	@Override
	public void onPoweredCycleBegin() {
	}

	@Override
	public void onPoweredCycleEnd() {
	}

	// Inventory
	@Override
	public int getSizeInventory() {
		return 0;
	}

	@Override
	public String getInvName() {
		return null;
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack itemstack) {
		return false;
	}
}

package erogenousbeef.bigreactors.common.tileentity;

import java.io.DataInputStream;
import java.io.IOException;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import erogenousbeef.bigreactors.client.gui.GuiHeatGenerator;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.tileentity.base.TileEntityBeefBase;
import erogenousbeef.bigreactors.common.tileentity.base.TileEntityPoweredInventoryFluid;
import erogenousbeef.bigreactors.gui.container.ContainerHeatGenerator;

public class TileEntityHeatGenerator extends TileEntityPoweredInventoryFluid {

	protected static final int TANK_WATER = 0;
	protected static final int TANK_STEAM = 1;
	
	public float internalTemperature;
	public float internalTemperatureRate;
	public boolean isActive;
	protected static final float waterSpecificHeat = 0.1f; // mB vaporized per degree C absorbed
	
	public TileEntityHeatGenerator() {
		super();
		
		isActive = false;
		internalTemperature = FluidRegistry.WATER.getTemperature();
		internalTemperatureRate = 0.2f; // 4C per second
	}
	
	@Override
	public void updateEntity() {
		super.updateEntity();
		
		if(isActive) {
			internalTemperature += internalTemperatureRate; 
			
			if(internalTemperature > 5000f) {
				internalTemperature = 5000f;
			}
		}
		
		IFluidTank[] tanks = getTanks(ForgeDirection.UNKNOWN);
		int waterAmt = tanks[0].getFluidAmount();
		int boilingTemp = 373; // Water boils at 373K
		float tempDifferential = internalTemperature - boilingTemp;

		if(!worldObj.isRemote)
			FMLLog.info("[BRDEBUG] Temp Differential %.1f", tempDifferential);
		
		if(waterAmt > 0 && tempDifferential >= 0.0001f) {
			// Try to convert some water to steam
			float amtVaporized = waterSpecificHeat * tempDifferential;
			int amtToVaporize = Math.min(Math.round(amtVaporized), waterAmt);
			
			if(amtToVaporize > 0) {
				FluidStack fluidToFill = new FluidStack(BigReactors.fluidSteam, amtToVaporize);
				int amtActuallyVaporized = tanks[1].fill(fluidToFill, true);
				tanks[0].drain(amtActuallyVaporized, true);
				
				float heatRemoved = amtActuallyVaporized / waterSpecificHeat;
				internalTemperature -= heatRemoved;
			}
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen getGUI(EntityPlayer player) {
		return new GuiHeatGenerator(getContainer(player), this);
	}

	@Override
	public Container getContainer(EntityPlayer player) {
		// TODO Auto-generated method stub
		return new ContainerHeatGenerator(this, player);
	}
	
	@Override
	public void onReceiveGuiButtonPress(String buttonName, DataInputStream dataStream) throws IOException {
		super.onReceiveGuiButtonPress(buttonName, dataStream);
		
		if(buttonName.equals("active")) {
			isActive = !isActive;
			worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
		}
	}

	@Override
	protected void onSendUpdate(NBTTagCompound updateTag) {
		super.onSendUpdate(updateTag);
		
		updateTag.setFloat("temp", internalTemperature);
		updateTag.setBoolean("active", isActive);
	}
	
	@Override
	public void onReceiveUpdate(NBTTagCompound updateTag) {
		super.onReceiveUpdate(updateTag);
		
		internalTemperature = updateTag.getFloat("temp");
		isActive = updateTag.getBoolean("active");
	}

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		
		if(tag.hasKey("temp")) {
			internalTemperature = tag.getFloat("temp");
		}
		else {
			internalTemperature = FluidRegistry.WATER.getTemperature();
		}
		
		if(tag.hasKey("active")) {
			isActive = tag.getBoolean("active");
		}
		else {
			isActive = false;
		}
	}
	
	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		
		tag.setFloat("temp", internalTemperature);
		tag.setBoolean("active", isActive);
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

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
import erogenousbeef.bigreactors.utils.StaticUtils;

public class TileEntitySteamCreator extends TileEntityPoweredInventoryFluid {

	protected static final int TANK_WATER = 0;
	protected static final int TANK_STEAM = 1;
	
	public float internalTemperature;
	public float internalTemperatureRate;
	public float energyAbsorbed;
	public boolean isActive;
	protected static final float waterSpecificHeat = 1.0f; // mB vaporized per degree C absorbed
	protected static final float waterCriticalTemperatureMin = 100f; // Temperature above which water begins to vaporize
	protected static final float waterCriticalTemperatureOptimal = 500f; // Temperature above which water vaporizes at its optimal rate
	protected static final float heatTransferPerSquareMeter = 1f; // Transfer at most 1C worth of heat per tick
	protected static final float progressLossThreshold = 0.98f; // If your heat dips below 98% of your minimum critical temp, progress is lost

	public TileEntitySteamCreator() {
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
		
		if(internalTemperature < waterCriticalTemperatureMin) {
			// Can't vaporize yet
			if(internalTemperature < waterCriticalTemperatureMin * progressLossThreshold) {
				energyAbsorbed = 0f;
			}
			return;
		}

		IFluidTank[] tanks = getTanks(ForgeDirection.UNKNOWN);
		int waterAmt = tanks[TANK_WATER].getFluidAmount();
		int steamAmt = tanks[TANK_STEAM].getFluidAmount();
		int maxSteamAmt = tanks[TANK_STEAM].getCapacity();

		if(waterAmt > 0 && steamAmt < tanks[TANK_STEAM].getCapacity()) {
			// Not full
			float heatTransferOptimalRate = getContactArea() * heatTransferPerSquareMeter;
			float heatTransferRate = StaticUtils.ExtraMath.Lerp(0f, heatTransferOptimalRate, (internalTemperature - waterCriticalTemperatureMin) / (waterCriticalTemperatureOptimal - waterCriticalTemperatureMin));
			
			// Clamp so we don't slurp out so much heat that we go below 100C
			heatTransferRate = Math.min(heatTransferRate, internalTemperature - waterCriticalTemperatureMin);

			// Calculate how much water that heat can boil, capped at the amount present
			int waterToVaporize = Math.min(waterAmt, (int)(waterSpecificHeat * heatTransferRate));
			
			// Cap by the available space in the boiler thing.
			waterToVaporize = Math.min(waterToVaporize, maxSteamAmt - steamAmt);

			// And recalculate how much we actually transferred via vaporization
			float temperatureToTransfer = waterToVaporize / waterSpecificHeat;
			
			// And now modify everything!
			tanks[TANK_WATER].drain(waterToVaporize, true);
			
			FluidStack incoming = new FluidStack(BigReactors.fluidSteam, waterToVaporize);
			tanks[TANK_STEAM].fill(incoming, true);
			
			internalTemperature -= temperatureToTransfer;
		}
	}
	
	private float getContactArea() {
		// Debug
		return 1f;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen getGUI(EntityPlayer player) {
		return new GuiHeatGenerator(getContainer(player), this);
	}

	@Override
	public Container getContainer(EntityPlayer player) {
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

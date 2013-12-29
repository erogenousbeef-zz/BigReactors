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

	// Debugging tools
	public float internalTemperature;
	public float internalTemperatureRate;

	// Values for the heat transfer system
	protected static final float heatTransferPerSquareMeter = 1f; // Transfer at most 1C worth of heat per tick
	protected static final float progressLossThreshold = 0.98f; // If your heat dips below 98% of your minimum critical temp, progress is lost by the above value per tick
	protected static final float minimumTransfer = 0.001f; // Minimum heat transferred per tick, to prevent float easing
	
	// Gameplay values for each individual fluid
	protected static final float waterVaporizedPerDegreeAbsorbed = 1f; // mB vaporized per degree absorbed
	protected static final float waterCriticalTemperatureMin = 273f+100f; // Temperature above which water begins to vaporize (kelvin)
	protected static final float waterCriticalTemperatureOptimal = 273f+500f; // Temperature above which water vaporizes at its optimal rate (kelvin)

	protected static final float standardRestingTemperature = 293f; // 20C = room-temperature-ish
	
	// Internal values
	public float energyAbsorbed; // Partial heat absorbed.
	protected boolean isActive;
	
	public TileEntitySteamCreator() {
		super();
		
		isActive = false;
		internalTemperature = standardRestingTemperature;
		internalTemperatureRate = 0.2f; // 4C per second
		energyAbsorbed = 0f;
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
		if(internalTemperature < waterCriticalTemperatureMin) {
			// Can't vaporize yet
			if(energyAbsorbed > 0f && internalTemperature < waterCriticalTemperatureMin * progressLossThreshold) {
				energyAbsorbed = Math.max(0f, energyAbsorbed - heatTransferPerSquareMeter);
			}
			
			// Lose small amounts of energy while heating up
			float restTemp = standardRestingTemperature;
			if(tanks[TANK_WATER].getFluid() != null) {
				restTemp = tanks[TANK_WATER].getFluid().getFluid().getTemperature();
			}

			if(internalTemperature > restTemp) {
				internalTemperature = Math.max(restTemp, internalTemperature - minimumTransfer);
			}
			return;
		}

		int waterAmt = tanks[TANK_WATER].getFluidAmount();
		int steamAmt = tanks[TANK_STEAM].getFluidAmount();
		int maxSteamAmt = tanks[TANK_STEAM].getCapacity();

		if(waterAmt > 0 && steamAmt < tanks[TANK_STEAM].getCapacity()) {
			// Not full
			float heatTransferOptimalRate = getContactArea() * heatTransferPerSquareMeter;
			float heatTransferred = StaticUtils.ExtraMath.Lerp(0f, heatTransferOptimalRate, (internalTemperature - waterCriticalTemperatureMin) / (waterCriticalTemperatureOptimal - waterCriticalTemperatureMin));
			
			// Minimum transfer amount to prevent small transfers from taking forever.
			heatTransferred = Math.max(minimumTransfer, heatTransferred);
			
			// Clamp so we don't slurp out so much heat that we go below the critical temperature
			heatTransferred = Math.min(heatTransferred, internalTemperature - waterCriticalTemperatureMin);

			// Add cached heat
			float energyAvailable = heatTransferred + energyAbsorbed;

			if((waterVaporizedPerDegreeAbsorbed * energyAvailable) < 1f) {
				// If we can't actually produce 1 mB, just cache progress
				energyAbsorbed = energyAvailable;
				internalTemperature -= heatTransferred;
			}
			else {
				// Okay, now for complicated shit - we can theoretically produce at least 1 mB
				// So, we need to produce as many mBs as possible with the energy available
				// Based on the amount actually produced, see how much energy remains
				// Of that remaining energy, first drain the energyAbsorbed cache, then burn off temperature

				// Cap steam created by the amount of water left in the water tank
				int steamToCreate = Math.min(waterAmt, (int)(waterVaporizedPerDegreeAbsorbed * energyAvailable));
				
				// Cap by the available space in the boiler thing
				steamToCreate = Math.min((int)steamToCreate, maxSteamAmt - steamAmt);
	
				// And recalculate how much we actually transferred via vaporization
				float energyConsumed = (int)steamToCreate / waterVaporizedPerDegreeAbsorbed;
				
				// And now modify everything!
				tanks[TANK_WATER].drain(steamToCreate, true);
				
				FluidStack incoming = new FluidStack(BigReactors.fluidSteam, steamToCreate);
				tanks[TANK_STEAM].fill(incoming, true);

				// If we have lots of energy cached (this generally shouldn't happen), bleed off the cache first
				if(energyAbsorbed > energyConsumed) {
					energyAbsorbed = Math.max(0f, energyAbsorbed - energyConsumed);
				}
				else {
					// This should be the majority case. Remove the absorbed energy, then bleed off heat
					energyConsumed = Math.max(0f, energyConsumed - energyAbsorbed);
					energyAbsorbed = 0f;
					internalTemperature -= energyConsumed;
				}
			}
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
		updateTag.setFloat("energyAbsorbed", energyAbsorbed);
	}
	
	@Override
	public void onReceiveUpdate(NBTTagCompound updateTag) {
		super.onReceiveUpdate(updateTag);
		
		internalTemperature = updateTag.getFloat("temp");
		isActive = updateTag.getBoolean("active");
		energyAbsorbed = updateTag.getFloat("energyAbsorbed");
	}

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		
		if(tag.hasKey("temp")) {
			internalTemperature = tag.getFloat("temp");
		}
		else {
			internalTemperature = standardRestingTemperature;
		}
		
		if(tag.hasKey("active")) {
			isActive = tag.getBoolean("active");
		}
		else {
			isActive = false;
		}
		
		if(tag.hasKey("energyAbsorbed")) {
			energyAbsorbed = tag.getFloat("energyAbsorbed");
		}
	}
	
	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		
		tag.setFloat("temp", internalTemperature);
		tag.setBoolean("active", isActive);
		tag.setFloat("energyAbsorbed", energyAbsorbed);
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

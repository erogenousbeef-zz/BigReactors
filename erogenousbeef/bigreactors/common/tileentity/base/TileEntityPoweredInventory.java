package erogenousbeef.bigreactors.common.tileentity.base;

import universalelectricity.core.block.IConnector;
import universalelectricity.core.block.IVoltage;
import universalelectricity.core.electricity.ElectricityNetworkHelper;
import universalelectricity.core.electricity.ElectricityPack;
import erogenousbeef.bigreactors.api.IBeefPowerStorage;
import erogenousbeef.core.power.buildcraft.PowerProviderBeef;
import buildcraft.api.power.IPowerProvider;
import buildcraft.api.power.IPowerReceptor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;

public abstract class TileEntityPoweredInventory extends TileEntityInventory implements IPowerReceptor, IVoltage, IConnector, IBeefPowerStorage  {

	public static float energyPerMJ = 1f;
	public static float energyPerUEWatt = 0.01f; 
	
	// Internal power
	private float storedEnergy;
	private int cycledTicks;
	
	// Buildcraft
	IPowerProvider _powerProvider;
	
	public TileEntityPoweredInventory() {
		super();
		
		_powerProvider = new PowerProviderBeef();
		_powerProvider.configure(25, 1, 10, 1, 1000);
		
		cycledTicks = -1;
	}
	
	// Internal energy methods
	@Override
	public abstract float getMaxEnergyStored();
	
	/**
	 * Returns the energy cost to run a cycle. Consumed instantly when a cycle begins.
	 * @return Number of MJ needed to start a cycle.
	 */
	public abstract float getCycleEnergyCost();
	
	/**
	 * @return The length of a powered processing cycle, in ticks.
	 */
	public abstract int getCycleLength();
	
	/**
	 * Check material/non-energy requirements for starting a cycle.
	 * These requirements should NOT be consumed at the start of a cycle.
	 * @return True if a cycle can start/continue, false otherwise.
	 */
	public abstract boolean canBeginCycle();
	
	/**
	 * Perform any necessary operations at the start of a cycle.
	 * Do NOT consume resources here. Powered cycles should only consume
	 * resources at the end of a cycle.
	 * canBeginCycle() will be called each tick to ensure that the necessary
	 * conditions remain met throughout the cycle.
	 */
	public abstract void onPoweredCycleBegin();
	
	/**
	 * Perform any necessary operations at the end of a cycle.
	 * Consume and produce resources here.
	 */
	public abstract void onPoweredCycleEnd();
	
	@Override
	public float getEnergyStored() {
		return storedEnergy;
	}
	
	public int getCurrentCycleTicks() {
		return cycledTicks;
	}
	
	public boolean isActive() {
		return cycledTicks >= 0;
	}
	
	public float getCycleCompletion() {
		if(cycledTicks < 0) { return 0f; }
		else { return (float)cycledTicks / (float)getCycleLength(); }
	}
	
	// TileEntity overrides
	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		if(tag.hasKey("storedEnergy")) {
			storedEnergy = tag.getFloat("storedEnergy");
		}
		
		if(tag.hasKey("cycledTicks")) {
			cycledTicks = tag.getInteger("cycledTicks");
		}
	}
	
	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		tag.setFloat("storedEnergy", storedEnergy);
		tag.setInteger("cycledTicks", cycledTicks);
	}
	
	// TileEntity methods
	@Override
	public void invalidate()
	{
		ElectricityNetworkHelper.invalidate(this);
		super.invalidate();
	}
	
	@Override
	public void updateEntity() {
		super.updateEntity();
		
		if(!worldObj.isRemote) {
			// Consume energy if we need to.
			if(storedEnergy < getMaxEnergyStored()) {
				// Consume Buildcraft energy
				if(getPowerProvider() != null)
				{
					getPowerProvider().update(this);
	
					float mjRequired = (getMaxEnergyStored() - getEnergyStored()) * energyPerMJ;
					if(getPowerProvider().useEnergy(1, mjRequired, false) > 0)
					{
						int mjConsumed = (int)(getPowerProvider().useEnergy(1, mjRequired, true));
						storedEnergy += mjConsumed * energyPerMJ;
					}
				}
				
				// Consume UE energy
				ElectricityPack powerRequested = new ElectricityPack((getMaxEnergyStored() - getEnergyStored()) * energyPerUEWatt / getVoltage(), getVoltage());
				ElectricityPack powerPack = ElectricityNetworkHelper.consumeFromMultipleSides(this, powerRequested);
				storedEnergy += powerPack.getWatts() * energyPerUEWatt;
			}
	
			// If we're running, continue the cycle until we're done.
			if(cycledTicks >= 0) {
				cycledTicks++;
				
				// If we don't have the stuff to begin a cycle, stop now
				if(!canBeginCycle()) {
					cycledTicks = -1;
					this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
				}
				else if(cycledTicks >= getCycleLength()) {
					onPoweredCycleEnd();
					cycledTicks = -1;
					this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
				}
			}
			
			// If we've stopped running, but we can start, then start running.
			if(cycledTicks < 0 && getCycleEnergyCost() <= storedEnergy && canBeginCycle()) {
				cycledTicks = 0;
				storedEnergy -= getCycleEnergyCost();
				onPoweredCycleBegin();
				this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			}
		}
	}
	
	// TileEntityBeefBase methods
	@Override
	protected void onSendUpdate(NBTTagCompound updateTag) {
		super.onSendUpdate(updateTag);
		updateTag.setFloat("storedEnergy", this.storedEnergy);
		updateTag.setInteger("cycledTicks", this.cycledTicks);
	}
	
	@Override
	public void onReceiveUpdate(NBTTagCompound updateTag) {
		super.onReceiveUpdate(updateTag);
		this.storedEnergy = updateTag.getFloat("storedEnergy");
		this.cycledTicks = updateTag.getInteger("cycledTicks");
	}
	
	// Buildcraft methods
	@Override
	public void setPowerProvider(IPowerProvider provider) {
		this._powerProvider = provider;
	}

	@Override
	public IPowerProvider getPowerProvider() {
		return this._powerProvider;
	}

	@Override
	public void doWork() { }

	@Override
	public int powerRequest(ForgeDirection from) { 
		return (int)Math.max(_powerProvider.getMaxEnergyStored() - _powerProvider.getEnergyStored(), 0);
	}	
	
	// UE Methods

	@Override
	public double getVoltage() {
		return 120;
	}

	@Override
	public boolean canConnect(ForgeDirection direction) {
		return true;
	}	
}

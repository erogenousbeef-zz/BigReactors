package erogenousbeef.bigreactors.common.tileentity.base;

import universalelectricity.core.block.IConnector;
import universalelectricity.core.block.IVoltage;
import universalelectricity.core.electricity.ElectricityNetworkHelper;
import universalelectricity.core.electricity.ElectricityPack;
import erogenousbeef.core.power.buildcraft.PowerProviderBeef;
import buildcraft.api.power.IPowerProvider;
import buildcraft.api.power.IPowerReceptor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;

public abstract class TileEntityPoweredInventory extends TileEntityInventory implements IPowerReceptor, IVoltage, IConnector  {

	public static int energyPerMJ = 100;
	public static int energyPerUEWatt = 1; 
	
	// Internal power
	private int storedEnergy;
	private int cycledTicks;
	
	// Buildcraft
	IPowerProvider _powerProvider;
	
	// Universal Electricity
	int internalUEStorage;
	
	public TileEntityPoweredInventory() {
		super();
		
		_powerProvider = new PowerProviderBeef();
		_powerProvider.configure(25, 1, 10, 1, 1000);
		
		internalUEStorage = 0;
		cycledTicks = -1;
	}
	
	// Internal energy methods
	public abstract int getMaxEnergyStored();
	public abstract int getCycleEnergyCost();
	public abstract int getCycleLength();
	public abstract boolean canBeginCycle();
	public abstract void onPoweredCycleBegin();
	public abstract void onPoweredCycleEnd();
	
	protected void DEBUGaddEnergy(int nrg) { this.storedEnergy = Math.min(storedEnergy+nrg, getMaxEnergyStored()); }
	
	public int getEnergyStored() {
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
			storedEnergy = tag.getInteger("storedEnergy");
		}
		
		if(tag.hasKey("cycledTicks")) {
			cycledTicks = tag.getInteger("cycledTicks");
		}
	}
	
	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		tag.setInteger("storedEnergy", storedEnergy);
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
	
					int mjRequired = (getMaxEnergyStored() - getEnergyStored()) / energyPerMJ;
					if(getPowerProvider().useEnergy(1, mjRequired, false) > 0)
					{
						int mjConsumed = (int)(getPowerProvider().useEnergy(1, mjRequired, true));
						storedEnergy += mjConsumed * energyPerMJ;
					}
				}
				
				// Consume UE energy
				ElectricityPack powerRequested = new ElectricityPack((getMaxEnergyStored() - getEnergyStored()) * energyPerUEWatt / getVoltage(), getVoltage());
				ElectricityPack powerPack = ElectricityNetworkHelper.consumeFromMultipleSides(this, powerRequested);
				internalUEStorage += powerPack.getWatts();
				
				int ueProduced = (int)Math.min((internalUEStorage + powerPack.getWatts()) / energyPerUEWatt, getMaxEnergyStored() - getEnergyStored());
				storedEnergy += ueProduced;
				internalUEStorage -= ueProduced * energyPerUEWatt;			
			}
			
	
	
			// If we're running, continue the cycle until we're done.
			if(cycledTicks >= 0) {
				cycledTicks++;
				if(cycledTicks >= getCycleLength()) {
					onPoweredCycleEnd();
					cycledTicks = -1;
				}
			}
			
			// If we've stopped running, but we can start, then start running.
			if(cycledTicks < 0 && getCycleEnergyCost() <= storedEnergy && canBeginCycle()) {
				cycledTicks = 0;
				storedEnergy -= getCycleEnergyCost();
				onPoweredCycleBegin();
			}
		}
	}
	
	// TileEntityBeefBase methods
	@Override
	protected void onSendUpdate(NBTTagCompound updateTag) {
		super.onSendUpdate(updateTag);
		updateTag.setInteger("storedEnergy", this.storedEnergy);
		updateTag.setInteger("cycledTicks", this.cycledTicks);
	}
	
	@Override
	public void onReceiveUpdate(NBTTagCompound updateTag) {
		super.onReceiveUpdate(updateTag);
		this.storedEnergy = updateTag.getInteger("storedEnergy");
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

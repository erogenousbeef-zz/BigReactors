package erogenousbeef.bigreactors.common.tileentity.base;

import universalelectricity.core.block.IConnector;
import universalelectricity.core.block.IElectrical;
import universalelectricity.core.electricity.ElectricityPack;
import erogenousbeef.bigreactors.api.IBeefPowerStorage;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;

public abstract class TileEntityPoweredInventory extends TileEntityInventory implements IPowerReceptor, IElectrical, IBeefPowerStorage  {

	public static float energyPerMJ = 1f;
	public static float energyPerUEWatt = 0.01f; 
	
	// Internal power
	private int cycledTicks;
	
	// Buildcraft
	PowerHandler _powerHandler;
	
	public TileEntityPoweredInventory() {
		super();
		
		_powerHandler = new PowerHandler(this, PowerHandler.Type.MACHINE);
		
		// We use MAX_VALUE for the activation energy because I don't want to use BuildCraft's stupid doWork callback.
		_powerHandler.configure(1, getCycleEnergyCost(), Float.MAX_VALUE, getMaxEnergyStored());
		_powerHandler.configurePowerPerdition(0,  0); // Fuck power loss.

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
		return this._powerHandler.getEnergyStored();
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
		this._powerHandler.readFromNBT(tag);
		
		if(tag.hasKey("cycledTicks")) {
			cycledTicks = tag.getInteger("cycledTicks");
		}
	}
	
	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		this._powerHandler.writeToNBT(tag);
		tag.setInteger("cycledTicks", cycledTicks);
	}
	
	// TileEntity methods	
	@Override
	public void updateEntity() {
		super.updateEntity();
		
		if(!worldObj.isRemote) {
			// Energy consumption is all callback-based now.
			
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
			if(cycledTicks < 0 && getCycleEnergyCost() <= getEnergyStored() && canBeginCycle()) {
				this._powerHandler.useEnergy(getCycleEnergyCost(), getCycleEnergyCost(), true);
				cycledTicks = 0;
				onPoweredCycleBegin();
				this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			}
		}
	}
	
	// TileEntityBeefBase methods
	@Override
	protected void onSendUpdate(NBTTagCompound updateTag) {
		super.onSendUpdate(updateTag);
		this._powerHandler.writeToNBT(updateTag);
		updateTag.setInteger("cycledTicks", this.cycledTicks);
	}
	
	@Override
	public void onReceiveUpdate(NBTTagCompound updateTag) {
		super.onReceiveUpdate(updateTag);
		this._powerHandler.readFromNBT(updateTag);
		this.cycledTicks = updateTag.getInteger("cycledTicks");
	}
	
	// Buildcraft methods
	@Override
	public PowerReceiver getPowerReceiver(ForgeDirection side) {
		return this._powerHandler.getPowerReceiver();
	}
	
	@Override
	public void doWork(PowerHandler handler) {
		
		
	}
	
	@Override
	public World getWorld() { return this.worldObj; }

//	@Override
//	public int powerRequest(ForgeDirection from) { 
//		return (int)Math.max(_powerHandler.getMaxEnergyStored() - _powerHandler.getEnergyStored(), 0);
//	}	
	
	// UE Methods

	@Override
	public float getVoltage() {
		return 120f;
	}

	@Override
	public boolean canConnect(ForgeDirection direction) {
		if (direction == null || direction.equals(ForgeDirection.UNKNOWN))
		{
			return false;
		}

		return true;
	}
	
	@Override
	public float getProvide(ForgeDirection to) {
		return 0.0f;
	}
	
	@Override
	public float getRequest(ForgeDirection to) {
		if(this._powerHandler.getEnergyStored() >= this._powerHandler.getMaxEnergyStored()) {
			return 0.0f;
		}
		else {
			return (this._powerHandler.getMaxEnergyStored() - this._powerHandler.getEnergyStored()) / energyPerUEWatt;
		}
	}
	
	@Override
	public ElectricityPack provideElectricity(ForgeDirection to, ElectricityPack pack, boolean doAdd) {
		return null;
	}
	
	@Override
	public float receiveElectricity(ForgeDirection from, ElectricityPack pack, boolean doAdd) {
		float mjAccepted = this._powerHandler.addEnergy(pack.getWatts() * energyPerUEWatt);
		
		return mjAccepted / energyPerUEWatt;
	}
}

package erogenousbeef.bigreactors.common.tileentity.base;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import cofh.api.energy.EnergyStorage;
import cofh.api.energy.IEnergyHandler;

public abstract class TileEntityPoweredInventory extends TileEntityInventory implements IEnergyHandler {
	public static float energyPerRF = 1f;
	
	protected boolean m_ReceivesEnergy = true;
	protected boolean m_ProvidesEnergy = true;
	
	// Internal power
	private int cycledTicks;
	private EnergyStorage energyStorage;
	
	public TileEntityPoweredInventory() {
		super();
		
		energyStorage = new EnergyStorage(getMaxEnergyStored());
		
		cycledTicks = -1;
	}
	
	// Internal energy methods
	/**
	 * The amount of energy stored in this type of TileEntity
	 * @return The amount of energy stored. 0 or more. Only called at construction time.
	 */
	protected abstract int getMaxEnergyStored();
	
	/**
	 * Returns the energy cost to run a cycle. Consumed instantly when a cycle begins.
	 * @return Amount of RF needed to start a cycle.
	 */
	public abstract int getCycleEnergyCost();
	
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
	
	public int getCurrentCycleTicks() {
		return cycledTicks;
	}
	
	@Override
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
		
		if(tag.hasKey("energyStorage")) {
			this.energyStorage.readFromNBT(tag.getCompoundTag("energyStorage"));
		}
		
		if(tag.hasKey("cycledTicks")) {
			cycledTicks = tag.getInteger("cycledTicks");
		}
	}
	
	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		NBTTagCompound energyTag = new NBTTagCompound();
		this.energyStorage.writeToNBT(energyTag);
		tag.setTag("energyStorage", energyTag);
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
			if(cycledTicks < 0 && getCycleEnergyCost() <= energyStorage.getEnergyStored() && canBeginCycle()) {
				this.energyStorage.extractEnergy(getCycleEnergyCost(), false);
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
		NBTTagCompound energyTag = new NBTTagCompound();
		this.energyStorage.writeToNBT(energyTag);
		updateTag.setTag("energyStorage", energyTag);
		updateTag.setInteger("cycledTicks", this.cycledTicks);
	}
	
	@Override
	public void onReceiveUpdate(NBTTagCompound updateTag) {
		super.onReceiveUpdate(updateTag);
		this.energyStorage.readFromNBT(updateTag.getCompoundTag("energyStorage"));
		this.cycledTicks = updateTag.getInteger("cycledTicks");
	}
	
	/* IEnergyHandler */
	@Override
	public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate) {
		if(!m_ReceivesEnergy) { return 0; }
		return energyStorage.receiveEnergy(maxReceive, simulate);
	}

	@Override
	public int extractEnergy(ForgeDirection from, int maxExtract, boolean simulate) {
		if(!m_ProvidesEnergy) { return 0; }
		return energyStorage.extractEnergy(maxExtract, simulate);
	}

	@Override
	public boolean canConnectEnergy(ForgeDirection from) {

		return true;
	}

	@Override
	public int getEnergyStored(ForgeDirection from) {

		return energyStorage.getEnergyStored();
	}

	@Override
	public int getMaxEnergyStored(ForgeDirection from) {

		return energyStorage.getMaxEnergyStored();
	}
}

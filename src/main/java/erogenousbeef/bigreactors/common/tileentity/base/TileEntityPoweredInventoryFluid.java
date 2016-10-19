package erogenousbeef.bigreactors.common.tileentity.base;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.fluids.IFluidTank;
import erogenousbeef.bigreactors.common.interfaces.IMultipleFluidHandler;

public abstract class TileEntityPoweredInventoryFluid extends
		TileEntityPoweredInventory implements IFluidHandler, IMultipleFluidHandler {

	private FluidTank[] tanks;
	private FluidTank[][] tankExposureCache;

	protected static final FluidTank[] kEmptyFluidTankList = new FluidTank[0];
	protected static final int FLUIDTANK_NONE = -1;

	public TileEntityPoweredInventoryFluid() {
		super();

		tanks = new FluidTank[getNumTanks()];
		tankExposureCache = new FluidTank[getNumTanks()][1];

		for(int i = 0; i < getNumTanks(); i++) {
			tanks[i] = new FluidTank(getTankSize(i));
			tankExposureCache[i][0] = tanks[i];
		}
	}

	// Internal Helpers

	private void readFluidsFromNBT(NBTTagCompound tag) {
		// Initialize tanks to empty, as we send sparse updates.
		for(int i = 0; i < tanks.length; i++) {
			tanks[i].setFluid(null);
		}

		if(tag.hasKey("fluids")) {
			NBTTagList tagList = tag.getTagList("fluids", 10);
			for(int i = 0; i < tagList.tagCount(); i++) {
				NBTTagCompound fluidTag = tagList.getCompoundTagAt(i);
				int fluidIdx = fluidTag.getInteger("tagIdx");
				FluidStack newFluid = FluidStack.loadFluidStackFromNBT(fluidTag);
				tanks[fluidIdx].setFluid(newFluid);
			}
		}
	}

	private void writeFluidsToNBT(NBTTagCompound tag) {
		NBTTagList fluidTagList = new NBTTagList();
		for(int i = 0; i < getNumTanks(); i++) {
			if(tanks[i] != null && tanks[i].getFluid() != null) {
				NBTTagCompound fluidTag = new NBTTagCompound();
				fluidTag.setInteger("tagIdx", i);
				tanks[i].getFluid().writeToNBT(fluidTag);
				fluidTagList.appendTag(fluidTag);
			}
		}

		if(fluidTagList.tagCount() > 0) {
			tag.setTag("fluids", fluidTagList);
		}
	}

	// TileEntity overrides
	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		readFluidsFromNBT(tag);
	}

	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		writeFluidsToNBT(tag);
	}

	// TileEntityBeefBase
	@Override
	protected void onSendUpdate(NBTTagCompound updateTag) {
		super.onSendUpdate(updateTag);
		writeFluidsToNBT(updateTag);
	}

	@Override
	public void onReceiveUpdate(NBTTagCompound updateTag) {
		super.onReceiveUpdate(updateTag);
		readFluidsFromNBT(updateTag);
	}

	// IFluidHandler

	/**
	 * The number of fluid tanks in this machine.
	 * @return The number of fluid tanks in this machine.
	 */
	 public abstract int getNumTanks();
	 
	 /**
	  * Returns the size of the tank at a given position
	  * @param tankIndex The index of the given tank in the tanks array.
	  * @return The volume of the tank, in fluid units. 1000 = 1 Bucket.
	  */
	 public abstract int getTankSize(int tankIndex);

	 /**
	  * Returns the index of the tank which is exposed on a given world side.
	  * Remember to translate this into a reference side!
	  * @param side The world side on which the device is being queried for fluid tank exposures.
	  * @return The index of the exposed tank, or -1 for none.
	  */
	 public abstract int getExposedTankFromSide(int side);
	 
	/**
     * Fills fluid into internal tanks, distribution is left to the ITankContainer.
     * @param from Orientation the fluid is pumped in from.
     * @param resource FluidStack representing the maximum amount of fluid filled into the ITankContainer
     * @param doFill If false filling will only be simulated.
     * @return Amount of resource that was filled into internal tanks.
     */
	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
    	int tankToFill = FLUIDTANK_NONE;
    	if(from != ForgeDirection.UNKNOWN) {
    		tankToFill = getExposedTankFromSide(from.ordinal());
    	}
    	else {
    		tankToFill = getDefaultTankForFluid(resource.getFluid());
    	}

    	if(tankToFill <= FLUIDTANK_NONE) {
    		return 0;
    	} else {
    		return fill(tankToFill, resource, doFill);
    	}
    }
 
    /**
     * Fills fluid into the specified internal tank.
     * @param tankIndex the index of the tank to fill
     * @param resource FluidStack representing the maximum amount of fluid filled into the ITankContainer
     * @param doFill If false filling will only be simulated.
     * @return Amount of resource that was filled into internal tanks.
     */
    public int fill(int tankIndex, FluidStack resource, boolean doFill) {
    	if(!isFluidValidForTank(tankIndex, resource)) {
    		return 0;
    	}
    
    	int res = tanks[tankIndex].fill(resource, doFill);
    	return res;
    }

    /** Drains fluid out of internal tanks, distribution is left entirely to the IFluidHandler.
	 * 
	 * This method is not Fluid-sensitive.
     * @param from Orientation the fluid is drained to.
     * @param maxDrain Maximum amount of fluid to drain.
     * @param doDrain If false draining will only be simulated.
     * @return FluidStack representing the fluid and amount actually drained from the ITankContainer
     */
	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
    	int tankToDrain = 0;
    	if(from != ForgeDirection.UNKNOWN) {
    		tankToDrain = getExposedTankFromSide(from.ordinal());
    	}

    	if(tankToDrain <= FLUIDTANK_NONE) {
    		return null;
    	} else {
    		return drain(tankToDrain, maxDrain, doDrain);
    	}
    }

    /**
     * Drains fluid out of the specified internal tank.
     * @param tankIndex the index of the tank to drain
     * @param maxDrain Maximum amount of fluid to drain.
     * @param doDrain If false draining will only be simulated.
     * @return FluidStack representing the fluid and amount actually drained from the ITankContainer
     */
    public FluidStack drain(int tankIndex, int maxDrain, boolean doDrain) {
    	return tanks[tankIndex].drain(maxDrain, doDrain);
    }

    /**
     * Drains fluid out of internal tanks, distribution is left entirely to the IFluidHandler.
     * 
     * @param from
     *            Orientation the Fluid is drained to.
     * @param resource
     *            FluidStack representing the Fluid and maximum amount of fluid to be drained.
     * @param doDrain
     *            If false, drain will only be simulated.
     * @return FluidStack representing the Fluid and amount that was (or would have been, if
     *         simulated) drained.
     */
	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
    	int tankToDrain = 0;
    	if(from != ForgeDirection.UNKNOWN) {
    		tankToDrain = getExposedTankFromSide(from.ordinal());
    	}
    
    	if(tankToDrain == FLUIDTANK_NONE) {
    		return null;
    	}
    	else {
    		// Can't drain that fluid from that side.
    		if(!resource.isFluidEqual( tanks[tankToDrain].getFluid() )) {
    			return null;
    		}
    
    		return drain(tankToDrain, resource.amount, doDrain);
    	}
    }
    
    /**
     * @param direction tank side: UNKNOWN for default tank set
     * @return Array of {@link FluidTank}s contained in this ITankContainer for this direction
     */
    public IFluidTank[] getTanks(ForgeDirection direction) {
    	if(direction == ForgeDirection.UNKNOWN) {
    		return tanks;
    	}
    	else {
    		int exposure = getExposedTankFromSide(direction.ordinal());
    		if(exposure == FLUIDTANK_NONE) {
    			return kEmptyFluidTankList;
    		}
    
    		return tankExposureCache[exposure];
    	}
    }

    /**
     * Return the tank that this tank container desired to be used for the specified fluid type from the specified direction
     *
     * @param direction the direction
     * @param type the fluid type, null is always an acceptable value
     * @return a tank or null for no such tank
     */
    public IFluidTank getTank(ForgeDirection direction, FluidStack type) {
    	if(direction == ForgeDirection.UNKNOWN) {
    		return null;
    	}
    	else {
    		int tankIdx = getExposedTankFromSide(direction.ordinal());
    		if(tankIdx == FLUIDTANK_NONE) {
    			return null;
    		}
    
    		IFluidTank t = tanks[tankIdx];
    		if(type == null || isFluidValidForTank(tankIdx, type)) {
    			return t;
    		}
    
    		return null;
    	}
    }

	/**
	 * Returns true if the given fluid can be inserted into the given direction.
	 * 
	 * More formally, this should return true if fluid is able to enter from the given direction.
	 */
	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid) {
		int tankIdx = 0;
		if(from != ForgeDirection.UNKNOWN) {
			tankIdx = getExposedTankFromSide(from.ordinal());
		}

		if(tankIdx == FLUIDTANK_NONE) { return false; }

		IFluidTank tank = tanks[tankIdx];
		if(tank.getFluidAmount() <= 0) {
			return true;
		}
		else {
			return tank.getFluid().fluidID == fluid.getID();
		}
	}

    /**
     * Returns true if the given fluid can be extracted from the given direction.
     * 
     * More formally, this should return true if fluid is able to leave from the given direction.
     */
	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid) {
    	int tankIdx = 0;
    	if(from != ForgeDirection.UNKNOWN) {
    		tankIdx = getExposedTankFromSide(from.ordinal());
    	}

    	if(tankIdx == FLUIDTANK_NONE) { return false; }

    	IFluidTank tank = tanks[tankIdx];
    	if(tank.getFluidAmount() <= 0) {
    		return false;
    	}
    	else {
    		return tank.getFluid().fluidID == fluid.getID();
    	}
    }

    /**
     * Returns an array of objects which represent the internal tanks. These objects cannot be used
     * to manipulate the internal tanks. See {@link FluidTankInfo}.
     * 
     * @param from
     *            Orientation determining which tanks should be queried.
     * @return Info for the relevant internal tanks.
     */
	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from) {
    	return getTankInfo();
    }

    /**
     * Returns an array of objects which represent the internal tanks. These objects cannot be used
     * to manipulate the internal tanks. See {@link FluidTankInfo}.
     * 
     * @return Info for the relevant internal tanks.
     */
	@Override
	public FluidTankInfo[] getTankInfo() {
    	FluidTankInfo[] infos = new FluidTankInfo[tanks.length];
    	for(int i = 0; i < tanks.length; i++) {
    		infos[i] = tanks[i].getInfo();
    	}
    
    	return infos;
    }
    
    /**
     * Check if the given fluid is valid for the given tank.
     * Note that the fluid may be null.
     * 
     * @param tankIdx The index of the tank to check validity for.
     * @param type The fluid to check validity for, or null.
     * @return True if the fluid can go in the identified tank, false otherwise.
     */
	protected abstract boolean isFluidValidForTank(int tankIdx, FluidStack type);

	// Helpers
	/**
	 * @param fluid The fluid whose default tank is being queried.
	 * @return The index of the tank into which a given fluid should be deposited by default.
	 */
	protected abstract int getDefaultTankForFluid(Fluid fluid);
}

package erogenousbeef.bigreactors.common.tileentity.base;

import java.io.DataInputStream;
import java.io.IOException;

import cpw.mods.fml.common.network.PacketDispatcher;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.net.PacketWrapper;
import erogenousbeef.bigreactors.net.Packets;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.packet.Packet;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.ILiquidTank;
import net.minecraftforge.liquids.ITankContainer;
import net.minecraftforge.liquids.LiquidStack;
import net.minecraftforge.liquids.LiquidTank;

public abstract class TileEntityPoweredInventoryLiquid extends
		TileEntityPoweredInventory implements ITankContainer {

	private LiquidTank[] tanks;
	private int[] tankExposure;

	public static final int LIQUIDTANK_NONE = -1;
	
	public TileEntityPoweredInventoryLiquid() {
		super();
		
		tanks = new LiquidTank[getNumTanks()];
		for(int i = 0; i < getNumTanks(); i++) {
			tanks[i] = new LiquidTank(getTankSize(i));
		}

		resetLiquidExposures();
	}

	// Tank Accessors
	/**
	 * Set the externally-accessible liquid tank for a given reference direction.
	 * @param side Reference direction (north/front, south/back, west/left, east/right)
	 * @param tankIdx The index of the tank which can be accessed from that direction.
	 */
	public void setExposedTank(ForgeDirection side, int tankIdx) {
		if(side == ForgeDirection.UNKNOWN) {
			return;
		}

		if(tankExposure[side.ordinal()] == tankIdx) {
			return;
		}

		tankExposure[side.ordinal()] = tankIdx;
		
		if(!this.worldObj.isRemote) {
			Packet updatePacket = PacketWrapper.createPacket(BigReactors.CHANNEL, Packets.SmallMachineLiquidExposureUpdate,
																new Object[] { xCoord, yCoord, zCoord, side.ordinal(), tankIdx });
			PacketDispatcher.sendPacketToAllAround(xCoord, yCoord, zCoord, 50, worldObj.provider.dimensionId, updatePacket);
		}
		
	}
	
	/**
	 * Get the externally-accessible liquid tank for a given reference direction
	 * @param side Reference direction (north/front, south/back, west/left, east/right)
	 * @return The index of the exposed tank, or -1 (LIQUIDTANK_NONE) if none
	 */
	public int getExposedTankFromReferenceSide(ForgeDirection side) {
		if(side == ForgeDirection.UNKNOWN) {
			return LIQUIDTANK_NONE;
		}
		else {
			return tankExposure[side.ordinal()];
		}
	}
	
	// Internal Helpers
	
	private void readLiquidsFromNBT(NBTTagCompound tag) {
		// Initialize tanks to empty, as we send sparse updates.
		for(int i = 0; i < tanks.length; i++) {
			tanks[i].setLiquid(null);
		}

		if(tag.hasKey("liquids")) {
			NBTTagList tagList = tag.getTagList("liquids");
			for(int i = 0; i < tagList.tagCount(); i++) {
				NBTTagCompound liquidTag = (NBTTagCompound) tagList.tagAt(i);
				int liquidIdx = liquidTag.getInteger("tagIdx");
				LiquidStack newLiquid = LiquidStack.loadLiquidStackFromNBT(liquidTag);
				tanks[liquidIdx].setLiquid(newLiquid);
			}
		}
		
		resetLiquidExposures();
		if(tag.hasKey("liquidExposures")) {
			NBTTagList exposureList = tag.getTagList("liquidExposures");
			for(int i = 0; i < exposureList.tagCount(); i++) {
				NBTTagCompound exposureTag = (NBTTagCompound) exposureList.tagAt(i);
				int exposureIdx = exposureTag.getInteger("exposureIdx");
				tankExposure[exposureIdx] = exposureTag.getInteger("direction");
			}
		}		
	}
	
	private void writeLiquidsToNBT(NBTTagCompound tag) {
		NBTTagList liquidTagList = new NBTTagList();
		for(int i = 0; i < getNumTanks(); i++) {
			if(tanks[i] != null && tanks[i].getLiquid() != null) {
				NBTTagCompound liquidTag = new NBTTagCompound();
				liquidTag.setInteger("tagIdx", i);
				tanks[i].getLiquid().writeToNBT(liquidTag);
				liquidTagList.appendTag(liquidTag);
			}
		}
		
		if(liquidTagList.tagCount() > 0) {
			tag.setTag("liquids", liquidTagList);
		}
		
		// Save liquid tank exposure orientations
		NBTTagList exposureTagList = new NBTTagList();
		for(int i = 0; i < 6; i++) {
			if(tankExposure[i] == LIQUIDTANK_NONE) { continue; } 

			NBTTagCompound exposureTag = new NBTTagCompound();
			exposureTag.setInteger("exposureIdx", i);
			exposureTag.setInteger("direction", tankExposure[i]);
			exposureTagList.appendTag(exposureTag);
		}
		if(exposureTagList.tagCount() > 0) {
			tag.setTag("liquidExposures", exposureTagList);
		}
	}
	
	// TileEntity overrides
	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		readLiquidsFromNBT(tag);
	}
	
	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		writeLiquidsToNBT(tag);
	}
	
	// TileEntityBeefBase
	@Override
	protected void onSendUpdate(NBTTagCompound updateTag) {
		super.onSendUpdate(updateTag);
		writeLiquidsToNBT(updateTag);
	}
	
	@Override
	public void onReceiveUpdate(NBTTagCompound updateTag) {
		super.onReceiveUpdate(updateTag);
		readLiquidsFromNBT(updateTag);
	}

	// Networked GUI
	@Override
	public void onReceiveGuiButtonPress(String buttonName, DataInputStream dataStream) throws IOException {
		if(buttonName.equals("changeInvSide")) {
			// We can't call super here without fucking up the datastream
			int side = dataStream.readInt();
			if(tankExposure[side] != LIQUIDTANK_NONE) {
				// Clicked on something we're already exposing, iterate the exposure
				iterateLiquidTankExposure(side);
			}
			else {
				boolean wasExposed = this.getExposedSlotFromReferenceSide(side) != TileEntityInventory.INVENTORY_UNEXPOSED;
				iterateInventoryExposure(side);
				
				// If an inventory was exposed, but now no inventory is exposed, it's our turn to shine.
				if(wasExposed && this.getExposedSlotFromReferenceSide(side) == TileEntityInventory.INVENTORY_UNEXPOSED) {
					// Ah, it cycled back around. Our turn.
					iterateLiquidTankExposure(side);
				}
				// Else: Inventory's still using the exposure.
			}
		}
		else {
			super.onReceiveGuiButtonPress(buttonName, dataStream);
		}
	}
	
	protected void iterateLiquidTankExposure(int side) {
		int newExposure;
		if(tankExposure[side] == LIQUIDTANK_NONE) {
			newExposure = 0;
		}
		else {
			newExposure = tankExposure[side] + 1;
		}

		if(newExposure >= getNumTanks()) {
			newExposure = LIQUIDTANK_NONE;
		}
		
		this.setExposedTank(ForgeDirection.getOrientation(side), newExposure);
	}
	
	// ITankContainer
	
	/**
	 * The number of liquid tanks in this machine.
	 * @return The number of liquid tanks in this machine.
	 */
	 public abstract int getNumTanks();
	 
	 /**
	  * Returns the size of the tank at a given position
	  * @param tankIndex The index of the given tank in the tanks array.
	  * @return The volume of the tank, in liquid units. 1000 = 1 Bucket.
	  */
	 public abstract int getTankSize(int tankIndex);

	/**
     * Fills liquid into internal tanks, distribution is left to the ITankContainer.
     * @param from Orientation the liquid is pumped in from.
     * @param resource LiquidStack representing the maximum amount of liquid filled into the ITankContainer
     * @param doFill If false filling will only be simulated.
     * @return Amount of resource that was filled into internal tanks.
     */
    public int fill(ForgeDirection from, LiquidStack resource, boolean doFill) {
    	int tankToFill = 0;
    	if(from != ForgeDirection.UNKNOWN) {
    		tankToFill = tankExposure[this.getRotatedSide(from.ordinal())];
    	}

    	if(tankToFill == LIQUIDTANK_NONE) {
    		return 0;
    	} else {
    		return fill(tankToFill, resource, doFill);
    	}
    }
   
    /**
     * Fills liquid into the specified internal tank.
     * @param tankIndex the index of the tank to fill
     * @param resource LiquidStack representing the maximum amount of liquid filled into the ITankContainer
     * @param doFill If false filling will only be simulated.
     * @return Amount of resource that was filled into internal tanks.
     */
    public int fill(int tankIndex, LiquidStack resource, boolean doFill) {
    	if(!isLiquidValidForTank(tankIndex, resource)) {
    		return 0;
    	}
    	
    	return tanks[tankIndex].fill(resource, doFill);
    }

    /**
     * Drains liquid out of internal tanks, distribution is left to the ITankContainer.
     * @param from Orientation the liquid is drained to.
     * @param maxDrain Maximum amount of liquid to drain.
     * @param doDrain If false draining will only be simulated.
     * @return LiquidStack representing the liquid and amount actually drained from the ITankContainer
     */
    public LiquidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
    	int tankToDrain = 0;
    	if(from != ForgeDirection.UNKNOWN) {
    		tankToDrain = tankExposure[this.getRotatedSide(from.ordinal())];
    	}

    	if(tankToDrain == LIQUIDTANK_NONE) {
    		return null;
    	} else {
    		return drain(tankToDrain, maxDrain, doDrain);
    	}
    }
   
    /**
     * Drains liquid out of the specified internal tank.
     * @param tankIndex the index of the tank to drain
     * @param maxDrain Maximum amount of liquid to drain.
     * @param doDrain If false draining will only be simulated.
     * @return LiquidStack representing the liquid and amount actually drained from the ITankContainer
     */
    public LiquidStack drain(int tankIndex, int maxDrain, boolean doDrain) {
    	return tanks[tankIndex].drain(maxDrain, doDrain);
    }

    /**
     * @param direction tank side: UNKNOWN for default tank set
     * @return Array of {@link LiquidTank}s contained in this ITankContainer for this direction
     */
    public ILiquidTank[] getTanks(ForgeDirection direction) {
    	if(direction == ForgeDirection.UNKNOWN) {
    		return tanks;
    	}
    	else {
    		ILiquidTank[] exposedTanks = new ILiquidTank[1];
    		exposedTanks[0] = tanks[tankExposure[this.getRotatedSide(direction.ordinal())]];
    		
    		return exposedTanks;
    	}
    }

    /**
     * Return the tank that this tank container desired to be used for the specified liquid type from the specified direction
     *
     * @param direction the direction
     * @param type the liquid type, null is always an acceptable value
     * @return a tank or null for no such tank
     */
    public ILiquidTank getTank(ForgeDirection direction, LiquidStack type) {
    	if(direction == ForgeDirection.UNKNOWN) {
    		return null;
    	}
    	else {
    		int tankIdx = tankExposure[this.getRotatedSide(direction.ordinal())];
    		if(tankIdx == LIQUIDTANK_NONE) {
    			return null;
    		}
    		
    		ILiquidTank t = tanks[tankIdx];
    		if(type == null || isLiquidValidForTank(tankIdx, type)) {
    			return t;
    		}
    		
    		return null;
    	}
    }

    /**
     * Check if the given liquid is valid for the given tank.
     * Note that the liquid may be null.
     * 
     * @param tankIdx The index of the tank to check validity for.
     * @param type The liquid to check validity for, or null.
     * @return True if the liquid can go in the identified tank, false otherwise.
     */
	protected abstract boolean isLiquidValidForTank(int tankIdx, LiquidStack type);
	
	// Helpers
	
	private void resetLiquidExposures() {
		tankExposure = new int[6]; // 6 forgedirections
		for(int i = 0; i < 6; i++) {
			tankExposure[i] = LIQUIDTANK_NONE;
		}
	}
}

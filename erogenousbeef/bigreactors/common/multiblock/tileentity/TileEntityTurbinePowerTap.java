package erogenousbeef.bigreactors.common.multiblock.tileentity;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import cofh.api.energy.IEnergyHandler;
import erogenousbeef.bigreactors.common.multiblock.interfaces.INeighborUpdatableEntity;
import erogenousbeef.core.multiblock.MultiblockControllerBase;

public class TileEntityTurbinePowerTap extends TileEntityTurbinePartStandard implements IEnergyHandler, INeighborUpdatableEntity {

	IEnergyHandler 	rfNetwork;
	
	public TileEntityTurbinePowerTap() {
		super();
		rfNetwork = null;
	}

	// INeighborUpdatableEntity
	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, int neighborBlockID) {
		if(isConnected()) {
			checkForConnections(world, x, y, z);
		}
	}

	@Override
	public void onNeighborTileChange(World world, int x, int y, int z,
			int neighborX, int neighborY, int neighborZ) {
		if(isConnected()) {
			checkForConnections(world, x, y, z);
		}
	}

	public boolean isAttachedToPowerNetwork() {
		return rfNetwork != null;
	}
	
	// IMultiblockPart
	@Override
	public void onAttached(MultiblockControllerBase newController) {
		super.onAttached(newController);
		
		checkForConnections(this.worldObj, xCoord, yCoord, zCoord);
		
		if(!this.worldObj.isRemote) { 
			// Force a connection to neighboring objects
			this.onInventoryChanged();
		}
	}
	
	@Override
	public void onMachineAssembled(MultiblockControllerBase multiblockControllerBase) {
		super.onMachineAssembled(multiblockControllerBase);

		
		checkForConnections(this.worldObj, xCoord, yCoord, zCoord);
		
		if(!this.worldObj.isRemote) { 
			// Force a connection to neighboring objects
			this.onInventoryChanged();
		}
	}
	
	/**
	 * Check for a world connection, if we're assembled.
	 * @param world
	 * @param x
	 * @param y
	 * @param z
	 */
	protected void checkForConnections(World world, int x, int y, int z) {
		boolean wasConnected = (rfNetwork != null);
		ForgeDirection out = getOutwardsDir();
		if(out == ForgeDirection.UNKNOWN) {
			wasConnected = false;
			rfNetwork = null;
		}
		else {
			// See if our adjacent non-reactor coordinate has a TE
			rfNetwork = null;

			TileEntity te = world.getBlockTileEntity(x + out.offsetX, y + out.offsetY, z + out.offsetZ);
			if(!(te instanceof TileEntityReactorPowerTap)) {
				// Skip power taps, as they implement these APIs and we don't want to shit energy back and forth
				if(te instanceof IEnergyHandler) {
					rfNetwork = (IEnergyHandler)te;
				}
			}
		}
		
		boolean isConnected = (rfNetwork != null);
		if(wasConnected != isConnected && worldObj.isRemote) {
			// Re-render on clients
			world.markBlockForUpdate(xCoord, yCoord, zCoord);
		}
	}

	/** This will be called by the Reactor Controller when this tap should be providing power.
	 * @return Power units remaining after consumption.
	 */
	public int onProvidePower(int units) {
		if(rfNetwork == null) {
			return units;
		}

		ForgeDirection approachDirection = getOutwardsDir().getOpposite();
		int energyConsumed = rfNetwork.receiveEnergy(approachDirection, (int)units, false);
		units -= energyConsumed;
		
		return units;
	}


	// IEnergyHandler

	@Override
	public int receiveEnergy(ForgeDirection from, int maxReceive,
			boolean simulate) {
		// HAHA NO
		return 0;
	}

	@Override
	public int extractEnergy(ForgeDirection from, int maxExtract,
			boolean simulate) {
		if(!this.isConnected()) { return 0; }

		return getTurbine().extractEnergy(from, maxExtract, simulate);
	}

	@Override
	public boolean canInterface(ForgeDirection from) {
		if(!this.isConnected()) { return false; }

		return from == getOutwardsDir();
	}

	@Override
	public int getEnergyStored(ForgeDirection from) {
		if(!this.isConnected()) { return 0; }
		
		return getTurbine().getEnergyStored(from);
	}

	@Override
	public int getMaxEnergyStored(ForgeDirection from) {
		if(!this.isConnected()) { return 0; }

		return getTurbine().getMaxEnergyStored(from);
	}

}

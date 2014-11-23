package erogenousbeef.bigreactors.common.multiblock.tileentity;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import cofh.api.energy.IEnergyHandler;
import erogenousbeef.bigreactors.common.multiblock.interfaces.INeighborUpdatableEntity;
import erogenousbeef.core.multiblock.MultiblockControllerBase;

public class TileEntityReactorPowerTap extends TileEntityReactorPart implements IEnergyHandler, INeighborUpdatableEntity {
	IEnergyHandler 	rfNetwork;
	
	public TileEntityReactorPowerTap() {
		super();
		
		rfNetwork = null;
	}
	
	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block neighborBlock) {
		if(isConnected()) {
			checkForConnections(world, x, y, z);
		}
	}

	@Override
	public void onNeighborTileChange(IBlockAccess world, int x, int y, int z, int neighborX, int neighborY, int neighborZ) {
		if(isConnected()) {
			checkForConnections(world, x, y, z);
		}
	}

	// IMultiblockPart
	@Override
	public void onAttached(MultiblockControllerBase newController) {
		super.onAttached(newController);
		
		checkForConnections(this.worldObj, xCoord, yCoord, zCoord);
	}
	
	@Override
	public void onMachineAssembled(MultiblockControllerBase multiblockControllerBase) {
		super.onMachineAssembled(multiblockControllerBase);

		checkForConnections(this.worldObj, xCoord, yCoord, zCoord);
		
		// Force a connection to the power taps
		this.notifyNeighborsOfTileChange();
	}

	// Custom PowerTap methods
	/**
	 * Check for a world connection, if we're assembled.
	 * @param world
	 * @param x
	 * @param y
	 * @param z
	 */
	protected void checkForConnections(IBlockAccess world, int x, int y, int z) {
		boolean wasConnected = (rfNetwork != null);
		ForgeDirection out = getOutwardsDir();
		if(out == ForgeDirection.UNKNOWN) {
			wasConnected = false;
			rfNetwork = null;
		}
		else {
			// See if our adjacent non-reactor coordinate has a TE
			rfNetwork = null;

			TileEntity te = world.getTileEntity(x + out.offsetX, y + out.offsetY, z + out.offsetZ);
			if(!(te instanceof TileEntityReactorPowerTap)) {
				// Skip power taps, as they implement these APIs and we don't want to shit energy back and forth
				if(te instanceof IEnergyHandler) {
					IEnergyHandler handler = (IEnergyHandler)te;
					if(handler.canConnectEnergy(out.getOpposite())) {
						rfNetwork = handler;
					}
				}
			}
			
		}
		
		boolean isConnected = (rfNetwork != null);
		if(wasConnected != isConnected) {
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
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
	
	// Thermal Expansion
	@Override
	public int receiveEnergy(ForgeDirection from, int maxReceive,
			boolean simulate) {
		return 0;
	}

	@Override
	public int extractEnergy(ForgeDirection from, int maxExtract,
			boolean simulate) {
		if(!this.isConnected())
			return 0;

		if(from == getOutwardsDir()) {
			return this.getReactorController().extractEnergy(from, maxExtract, simulate);
		}

		return 0;
	}

	@Override
	public boolean canConnectEnergy(ForgeDirection from) {
		return from == getOutwardsDir();
	}

	@Override
	public int getEnergyStored(ForgeDirection from) {
		if(!this.isConnected())
			return 0;

		return this.getReactorController().getEnergyStored(from);
	}

	@Override
	public int getMaxEnergyStored(ForgeDirection from) {
		if(!this.isConnected())
			return 0;

		return this.getReactorController().getMaxEnergyStored(from);
	}
	
	public boolean hasEnergyConnection() { return rfNetwork != null; }
}

package erogenousbeef.bigreactors.common.multiblock.tileentity;

import welfare93.bigreactors.energy.IEnergyHandlerOutput;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import erogenousbeef.bigreactors.common.multiblock.interfaces.INeighborUpdatableEntity;
import erogenousbeef.core.multiblock.MultiblockControllerBase;

public class TileEntityTurbinePowerTap extends TileEntityTurbinePartStandard implements IEnergyHandlerOutput, INeighborUpdatableEntity {

	IEnergyHandlerOutput	rfNetwork;
	
	public TileEntityTurbinePowerTap() {
		super();
		rfNetwork = null;
	}

	// INeighborUpdatableEntity
	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block neighborBlockID) {
		checkForConnections(world, x, y, z);
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
		}
	}
	
	@Override
	public void onMachineAssembled(MultiblockControllerBase multiblockControllerBase) {
		super.onMachineAssembled(multiblockControllerBase);

		
		checkForConnections(this.worldObj, xCoord, yCoord, zCoord);
		
		if(!this.worldObj.isRemote) { 
			// Force a connection to neighboring objects
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

			TileEntity te = world.getTileEntity(x + out.offsetX, y + out.offsetY, z + out.offsetZ);
			if(!(te instanceof TileEntityReactorPowerTap)) {
				// Skip power taps, as they implement these APIs and we don't want to shit energy back and forth
				if(te instanceof IEnergyHandlerOutput) {
					rfNetwork = (IEnergyHandlerOutput)te;
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
		int energyConsumed = rfNetwork.addEnergy(units);
		units -= energyConsumed;
		
		return units;
	}


	// IEnergyHandler




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

	@Override
	public double getOfferedEnergy() {
		if(!this.isConnected()) { return 0; }
		return getTurbine().getEnergyStored();
	}

	@Override
	public void drawEnergy(double amount) {
		getTurbine().removeEnergy((int)amount);
		
	}

	@Override
	public boolean emitsEnergyTo(TileEntity receiver, ForgeDirection direction) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public int addEnergy(int energy) {
		return getTurbine().addEnergy(energy);
	}

	@Override
	public int removeEnergy(int energy) {
		return getTurbine().removeEnergy(energy);
	}

}

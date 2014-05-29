package erogenousbeef.bigreactors.common.multiblock.tileentity;

import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import welfare93.bigreactors.energy.IEnergyHandler;
import welfare93.bigreactors.energy.IEnergyHandlerOutput;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;
import erogenousbeef.bigreactors.common.multiblock.block.BlockReactorPart;
import erogenousbeef.bigreactors.common.multiblock.interfaces.INeighborUpdatableEntity;
import erogenousbeef.core.multiblock.MultiblockControllerBase;

public class TileEntityReactorPowerTap extends TileEntityReactorPart implements IEnergyHandlerOutput, INeighborUpdatableEntity {
	IEnergyHandlerOutput 	rfNetwork;
	
	public TileEntityReactorPowerTap() {
		super();
		
		rfNetwork = null;
	}
	
	public void onNeighborBlockChange(World world, int x, int y, int z, int neighborBlockID) {
		if(isConnected()) {
			checkForConnections(world, x, y, z);
		}
	}
	@Override
	public void invalidate() {
		super.invalidate();
		if(!this.worldObj.isRemote)MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
	}
	@Override
	public void onChunkUnload() {
		super.onChunkUnload();
		if(!this.worldObj.isRemote)MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
	}
	@Override
	public void validate() {
		super.validate();
		MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
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

		if(this.worldObj.isRemote) { return; } 
		
		checkForConnections(this.worldObj, xCoord, yCoord, zCoord);
		
		// Force a connection to the power taps
	}

	// Custom PowerTap methods
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
				if(te instanceof IEnergyHandler) {
					rfNetwork = (IEnergyHandlerOutput)te;
				}
			}
			
		}
		
		boolean isConnected = (rfNetwork != null);
		if(wasConnected != isConnected) {
			if(isConnected) {
				// Newly connected
				world.setBlockMetadataWithNotify(x, y, z, BlockReactorPart.POWERTAP_METADATA_BASE+1, 2);
			}
			else {
				// No longer connected
				world.setBlockMetadataWithNotify(x, y, z, BlockReactorPart.POWERTAP_METADATA_BASE, 2);
			}
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
	

	@Override
	public int getEnergyStored(ForgeDirection from) {
		if(!this.isConnected()) { return 0; }
		
		return getReactorController().getEnergyStored(from);
	}

	@Override
	public int getMaxEnergyStored(ForgeDirection from) {
		if(!this.isConnected()) { return 0; }

		return getReactorController().getMaxEnergyStored(from);
	}

	@Override
	public double getOfferedEnergy() {
		if(!this.isConnected()) { return 0; }
		return getReactorController().getEnergyStored();
	}

	@Override
	public void drawEnergy(double amount) {
		getReactorController().removeEnergy((int)amount);
		
	}

	@Override
	public boolean emitsEnergyTo(TileEntity receiver, ForgeDirection direction) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public int addEnergy(int energy) {
		return getReactorController().addEnergy(energy);
	}

	@Override
	public int removeEnergy(int energy) {
		return getReactorController().removeEnergy(energy);
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z,
			Block neighborBlockID) {
		// TODO Auto-generated method stub
		
	}
}

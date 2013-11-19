package erogenousbeef.bigreactors.common.tileentity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cofh.api.energy.IEnergyHandler;
import cpw.mods.fml.common.FMLLog;

import erogenousbeef.bigreactors.common.block.BlockReactorPart;
import erogenousbeef.core.common.CoordTriplet;
import erogenousbeef.core.multiblock.MultiblockControllerBase;
import universalelectricity.core.block.IConductor;
import universalelectricity.core.block.IConnector;
import universalelectricity.core.block.IElectrical;
import universalelectricity.core.electricity.ElectricityPack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

public class TileEntityReactorPowerTap extends TileEntityReactorPart implements IElectrical, IEnergyHandler {

	public static final float uePowerFactor = 0.1f;	 // 10 UE watts per 1 internal power
	public static final float rfPowerFactor = 1f;    // 1 RF per 1 internal power
	
	ForgeDirection out;
	
	IEnergyHandler 	rfNetwork;
	IConnector		ueNetwork;
	
	public TileEntityReactorPowerTap() {
		super();
		
		out = ForgeDirection.UNKNOWN;
		rfNetwork = null;
		ueNetwork = null;
	}
	
	public void onNeighborBlockChange(World world, int x, int y, int z, int neighborBlockID) {
		if(isConnected()) {
			checkForConnections(world, x, y, z);
		}
	}
	
	// IMultiblockPart
	@Override
	public void onAttached(MultiblockControllerBase newController) {
		super.onAttached(newController);
		
		checkOutwardDirection();
		checkForConnections(this.worldObj, xCoord, yCoord, zCoord);
	}
	
	@Override
	public void onMachineAssembled() {
		super.onMachineAssembled();

		if(this.worldObj.isRemote) { return; } 
		
		checkOutwardDirection();
		checkForConnections(this.worldObj, xCoord, yCoord, zCoord);
	}

	// Custom PowerTap methods
	/**
	 * Discover which direction is normal to the multiblock face.
	 */
	protected void checkOutwardDirection() {
		MultiblockControllerBase controller = this.getMultiblockController();
		CoordTriplet minCoord = controller.getMinimumCoord();
		CoordTriplet maxCoord = controller.getMaximumCoord();
		
		if(this.xCoord == minCoord.x) {
			out = ForgeDirection.WEST;
		}
		else if(this.xCoord == maxCoord.x){
			out = ForgeDirection.EAST;
		}
		else if(this.zCoord == minCoord.z) {
			out = ForgeDirection.NORTH;
		}
		else if(this.zCoord == maxCoord.z) {
			out = ForgeDirection.SOUTH;
		}
		else if(this.yCoord == minCoord.y) {
			// Just in case I end up making omnidirectional taps.
			out = ForgeDirection.DOWN;
		}
		else if(this.yCoord == maxCoord.y){
			// Just in case I end up making omnidirectional taps.
			out = ForgeDirection.UP;
		}
		else {
			// WTF BRO
			out = ForgeDirection.UNKNOWN;
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
		boolean wasConnected = (rfNetwork != null || ueNetwork != null);
		if(out == ForgeDirection.UNKNOWN) {
			wasConnected = false;
			rfNetwork = null;
			ueNetwork = null;
		}
		else {
			// See if our adjacent non-reactor coordinate has a TE
			rfNetwork = null;
			ueNetwork = null;

			TileEntity te = world.getBlockTileEntity(x + out.offsetX, y + out.offsetY, z + out.offsetZ);
			if(!(te instanceof TileEntityReactorPowerTap)) {
				// Skip power taps, as they implement these APIs and we don't want to shit energy back and forth
				if(te instanceof IEnergyHandler) {
					rfNetwork = (IEnergyHandler)te;
				}
				else if(te instanceof IConnector) {
					IConnector connector = (IConnector)te;
					if(connector.canConnect(out.getOpposite())) {
						ueNetwork = (IConnector)te;
					}
				}
			}
			
		}
		
		boolean isConnected = (rfNetwork != null || ueNetwork != null);
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
	// TODO: Eliminate this entire method
	public int onProvidePower(int units) {
		ArrayList<CoordTriplet> deadCoords = null;
		
		if(rfNetwork == null && ueNetwork == null) {
			return units;
		}
		
		if(rfNetwork != null) {
			// Thermal Expansion 3
			int rfAvailable = (int)Math.floor((float)units / rfPowerFactor);
			
			ForgeDirection approachDirection = out.getOpposite();
			
			int energyConsumed = rfNetwork.receiveEnergy(approachDirection, rfAvailable, false);
			units -= (int)((float)energyConsumed * rfPowerFactor);
		}
		else if(ueNetwork != null) {
			/* TODO: Debug me
			// Universal Electricity
			ForgeDirection approachDirection = out.getOpposite();
			IElectricityNetwork network = ElectricityNetworkHelper.getNetworkFromTileEntity(te, approachDirection);
			if(network != null) {
				double wattsAvailable = (double)units / (double)uePowerFactor;
				double wattsWanted = network.getRequest().getWatts();
				if(wattsWanted > 0) {
					if(wattsWanted > wattsAvailable) {
						wattsWanted = wattsAvailable;
					}
					
					network.startProducing(this, wattsWanted/getVoltage(), getVoltage());
					 // Rounding up to prevent free energy. Sorry bro, thermodynamics says so.
					units -= (int)Math.ceil(wattsWanted * uePowerFactor);
				} else {
					network.stopProducing(this);
				}
			}
			*/
		}
		else {
			// This can happen when UE burns out and doesn't provide a block update.
			worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, BlockReactorPart.POWERTAP_METADATA_BASE, 2);
		}
		
		return units;
	}
	
	// Universal Electricity
	
	@Override
	public boolean canConnect(ForgeDirection direction) {
		return direction == out;
	}

	@Override
	public float getVoltage() {
		// TODO: Make this selectable?
		return 120;
	}

	@Override
	public float receiveElectricity(ForgeDirection from,
			ElectricityPack receive, boolean doReceive) {
		return 0;
	}

	@Override
	public ElectricityPack provideElectricity(ForgeDirection from,
			ElectricityPack request, boolean doProvide) {
		if(from != out) {
			return null;
		}
		
		int amtWanted = (int)(request.getWatts() * uePowerFactor);
		int amtSupplied = this.getReactorController().extractEnergy(from, amtWanted, doProvide);
		return ElectricityPack.getFromWatts(amtSupplied, request.voltage);
	}

	@Override
	public float getRequest(ForgeDirection direction) {
		return 0;
	}

	@Override
	public float getProvide(ForgeDirection direction) {
		// TODO Auto-generated method stub
		return this.getReactorController().getEnergyStored() / uePowerFactor;
	}

	@Override
	public int receiveEnergy(ForgeDirection from, int maxReceive,
			boolean simulate) {
		return 0;
	}

	@Override
	public int extractEnergy(ForgeDirection from, int maxExtract,
			boolean simulate) {
		if(from == out) {
			return this.getReactorController().extractEnergy(from, maxExtract, simulate);
		}

		return 0;
	}

	@Override
	public boolean canInterface(ForgeDirection from) {
		return from == out;
	}

	@Override
	public int getEnergyStored(ForgeDirection from) {
		return this.getReactorController().getEnergyStored(from);
	}

	@Override
	public int getMaxEnergyStored(ForgeDirection from) {
		return this.getReactorController().getMaxEnergyStored(from);
	}
}

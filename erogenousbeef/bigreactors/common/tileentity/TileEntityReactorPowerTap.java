package erogenousbeef.bigreactors.common.tileentity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import erogenousbeef.bigreactors.common.block.BlockReactorPart;
import erogenousbeef.core.common.CoordTriplet;
import erogenousbeef.core.multiblock.MultiblockControllerBase;
import erogenousbeef.core.power.buildcraft.PowerProviderBeef;
import universalelectricity.core.block.IConductor;
import universalelectricity.core.block.IConnector;
import universalelectricity.core.block.IVoltage;
import universalelectricity.core.electricity.ElectricityNetworkHelper;
import universalelectricity.core.electricity.IElectricityNetwork;
import buildcraft.api.core.SafeTimeTracker;
import buildcraft.api.power.IPowerProvider;
import buildcraft.api.power.IPowerReceptor;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

public class TileEntityReactorPowerTap extends TileEntityReactorPart implements IVoltage, IConnector, IPowerReceptor {

	public static final float uePowerFactor = 0.01f;	 // 100 UE watts per 1 internal power
	public static final float bcPowerFactor = 1.00f;  // 1 MJ per 1 internal power
	
	Set<CoordTriplet> powerConnections;
	IPowerProvider powerProvider;
	
	Set<IConductor> activeConductors;
	
	public TileEntityReactorPowerTap() {
		super();
		
		powerConnections = new HashSet<CoordTriplet>();
		activeConductors = new HashSet<IConductor>();
		
		powerProvider = new PowerProviderBeef();
	}
	
	@Override
	public void onBlockAdded(World world, int x, int y, int z) {
		super.onBlockAdded(world, x, y, z);

		if(isConnected()) {
			checkForConnections(world, x, y, z);
		}
	}
	
	@Override
	public void onAttached(MultiblockControllerBase newController) {
		super.onAttached(newController);
		
		if(isConnected()) {
			checkForConnections(this.worldObj, xCoord, yCoord, zCoord);
		}
	}
	
	public void onNeighborBlockChange(World world, int x, int y, int z, int neighborBlockID) {
		if(isConnected()) {
			checkForConnections(world, x, y, z);
		}
	}
	
	// IMultiblockPart
	public void onMachineAssembled(CoordTriplet machineMinCoords, CoordTriplet machineMaxCoords) {
		checkForConnections(this.worldObj, this.xCoord, this.yCoord, this.zCoord);
	}

	public void onMachineBroken() {
		checkForConnections(this.worldObj, this.xCoord, this.yCoord, this.zCoord);
	}
	
	protected void checkForConnections(World world, int x, int y, int z) {
		CoordTriplet[] blocksToCheck = new CoordTriplet[] {
				new CoordTriplet(x+1, y, z),
				new CoordTriplet(x-1, y, z),
				new CoordTriplet(x, y+1, z),
				new CoordTriplet(x, y-1, z),
				new CoordTriplet(x, y, z+1),
				new CoordTriplet(x, y, z-1),
		};

		boolean wasConnected = powerConnections.size() > 0;
		
		for(CoordTriplet coord : blocksToCheck) {
			TileEntity te = world.getBlockTileEntity(coord.x, coord.y, coord.z);
			boolean shouldAdd = false;
			
			if(te == null || te instanceof TileEntityReactorPowerTap) {
				shouldAdd = false;
			}
			else if(te instanceof IPowerReceptor) {
				shouldAdd = true;
			}
			else if(te instanceof IConnector) {
				IConnector connector = (IConnector)te;
				ForgeDirection directionFromThereToHere = coord.getOppositeDirectionFromSourceCoords(this.xCoord, this.yCoord, this.zCoord);
				if(connector.canConnect(directionFromThereToHere)) {
					shouldAdd = true;
				}
			}
			
			if(shouldAdd && !powerConnections.contains(coord)) {
				powerConnections.add(coord);
			}
			else if(!shouldAdd && powerConnections.contains(coord)) {
				powerConnections.remove(coord);
			}
		}
		
		if(wasConnected && powerConnections.size() == 0) {
			// No longer connected
			world.setBlockMetadataWithNotify(x, y, z, BlockReactorPart.POWERTAP_METADATA_BASE, 2);
		}
		else if(!wasConnected && powerConnections.size() > 0) {
			// Newly connected
			world.setBlockMetadataWithNotify(x, y, z, BlockReactorPart.POWERTAP_METADATA_BASE+1, 2);
		}
		
		if(this.isConnected()) {
			getReactorController().onPowerTapConnectionChanged(x, y, z, powerConnections.size());
		}
	}

	// This will be called by the Reactor Controller when this tap should be providing power.
	// Returns units remaining after consumption.
	public int onProvidePower(int units) {
		ArrayList<CoordTriplet> deadCoords = null;
		
		for(CoordTriplet outputCoord : powerConnections) {
			TileEntity te = this.worldObj.getBlockTileEntity(outputCoord.x, outputCoord.y, outputCoord.z);
			if(te == null) { 
				if(deadCoords == null) {
					deadCoords = new ArrayList<CoordTriplet>();
				}
				deadCoords.add(outputCoord);
				continue;
			}
			else if(te instanceof IPowerReceptor) {
				// Buildcraft
				int mjAvailable = (int)Math.floor((float)units / bcPowerFactor);
				
				IPowerReceptor ipr = (IPowerReceptor)te;
				IPowerProvider ipp = ipr.getPowerProvider();
				ForgeDirection approachDirection = outputCoord.getOppositeDirectionFromSourceCoords(this.xCoord, this.yCoord, this.zCoord);
				
				if(ipp != null && ipp.preConditions(ipr) && ipp.getMinEnergyReceived() <= mjAvailable) {
					int energyUsed = Math.min(Math.min(ipp.getMaxEnergyReceived(), mjAvailable), ipp.getMaxEnergyStored() - (int)Math.floor(ipp.getEnergyStored()));
					ipp.receiveEnergy(energyUsed, approachDirection);
					
					units -= (int)((float)energyUsed * bcPowerFactor);
				}
			}
			else if(te instanceof IConnector) { 
				// Universal Electricity
				ForgeDirection approachDirection = outputCoord.getDirectionFromSourceCoords(this.xCoord, this.yCoord, this.zCoord);
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
			}
		}
		
		// Handle burnt-out connections that didn't trigger a neighbor update (UE, mostly)
		if(deadCoords != null && deadCoords.size() > 0) {
			powerConnections.removeAll(deadCoords);
			if(powerConnections.size() <= 0) {
				// No longer connected
				worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, BlockReactorPart.POWERTAP_METADATA_BASE, 2);
			}
			
			if(this.isConnected()) {
				getReactorController().onPowerTapConnectionChanged(xCoord, yCoord, zCoord, powerConnections.size());
			}
		}
		
		return units;
	}
	
	@Override
	public void invalidate()
	{
		ElectricityNetworkHelper.invalidate(this); // Universal Electricity
		super.invalidate();
	}
	
	// Universal Electricity
	
	@Override
	public boolean canConnect(ForgeDirection direction) {
		if(direction == ForgeDirection.UP || direction == ForgeDirection.DOWN) { return false; }
		else { return true; }
	}

	@Override
	public double getVoltage() {
		// TODO: Make this selectable?
		return 120;
	}

	// Buildcraft methods
	
	@Override
	public void setPowerProvider(IPowerProvider provider) {
		// TODO Auto-generated method stub
		this.powerProvider = provider;
	}

	@Override
	public IPowerProvider getPowerProvider() {
		// TODO Auto-generated method stub
		return this.powerProvider;
	}

	@Override
	public void doWork() { }

	@Override
	public int powerRequest(ForgeDirection from) { return 0; }
}

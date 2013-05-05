package erogenousbeef.bigreactors.common.multiblock;

import java.util.LinkedList;

import net.minecraft.block.material.Material;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.block.BlockReactorPart;
import erogenousbeef.bigreactors.common.tileentity.TileEntityFuelRod;
import erogenousbeef.bigreactors.common.tileentity.TileEntityReactorPowerTap;
import erogenousbeef.core.common.CoordTriplet;
import erogenousbeef.core.multiblock.IMultiblockPart;
import erogenousbeef.core.multiblock.MultiblockControllerBase;
import erogenousbeef.core.multiblock.MultiblockRegistry;

public class MultiblockReactor extends MultiblockControllerBase {
	// Multiblock stuff
	private World worldObj;
	private boolean isWholeMachine;
	private boolean checkForMachineWholeness;

	private LinkedList<CoordTriplet> connectedBlocks;
	private CoordTriplet saveDelegate; // Also the network delegate

	// Game stuff
	protected boolean active;
	private double latentHeat;
	private LinkedList<CoordTriplet> activePowerTaps;
	// Highest internal Y-coordinate in the fuel column
	private LinkedList<CoordTriplet> activeFuelColumns;
	
	
	public MultiblockReactor(World world) {
		super(world);

		// Multiblock stuff
		worldObj = world;
		isWholeMachine = false;
		connectedBlocks = new LinkedList<CoordTriplet>();
		saveDelegate = null;

		// Game stuff
		active = false;
		latentHeat = 0.0;
		activePowerTaps = new LinkedList<CoordTriplet>();
		activeFuelColumns = new LinkedList<CoordTriplet>();
	}
	
	@Override
	protected void assembleMachine(World world) {
		super.assembleMachine(world);
	}
	
	@Override
	protected void disassembleMachine(World world) {
		super.disassembleMachine(world);
	}

	@Override
	protected boolean isMachineWhole() {
		boolean b = super.isMachineWhole();
		
		// TODO: Ensure that there is at least one controller and control rod attached.
		
		return b;
	}
	
	@Override
	public void updateMultiblockEntity() {		
		super.updateMultiblockEntity();
		
		// TODO: Eject Waste
		
		// TODO: Inject fuel

		if(this.isActive()) {
			// Run Radiation Sim, Produce Heatz
			TileEntityFuelRod fuelRod;
			for(CoordTriplet coord: activeFuelColumns) {
				CoordTriplet c = coord.copy();
				c.y = c.y - 1;
				int blockType = worldObj.getBlockId(c.x, c.y, c.z);			
				while(blockType == BigReactors.blockYelloriumFuelRod.blockID) {
					fuelRod = (TileEntityFuelRod)worldObj.getBlockTileEntity(c.x, c.y, c.z);
					fuelRod.radiate();
				}
			}

			// Produce energy from heat
			int energyAvailable = getAvailableEnergy();
			int energyRemaining = energyAvailable;
			if(activePowerTaps.size() > 0) {
				for(CoordTriplet coord : activePowerTaps) {
					if(energyRemaining <= 0) { break; }
					
					TileEntityReactorPowerTap tap = (TileEntityReactorPowerTap)this.worldObj.getBlockTileEntity(coord.x, coord.y, coord.z);
					energyRemaining = tap.onProvidePower(energyRemaining);
				}
			}
			
			produceEnergy(energyAvailable - energyRemaining);		
		}

		// leak 1% of heat to the environment
		double latentHeatLoss = Math.max(2.0, this.latentHeat * 0.99);
		latentHeat -= latentHeatLoss;
		if(latentHeat < 0.0) { latentHeat = 0.0; }
		
		// TODO: Overload?		
	}
	
	public void onPowerTapConnectionChanged(int x, int y, int z, int numConnections) {
		CoordTriplet coord = new CoordTriplet(x, y, z);
		int prevActive = activePowerTaps.size();
		
		if(numConnections > 0) {
			// Tap has connected
			if(!activePowerTaps.contains(coord)) {
				activePowerTaps.add(coord);
			}
		} else {
			// Tap has disconnected
			activePowerTaps.remove(coord);
		}
	}

	// TODO: Real versions of these
	// Returns available energy based on reactor heat. 10energy = 1MJ
	protected int getAvailableEnergy() { return (int)latentHeat; }

	protected void produceEnergy(int amountProduced) {
		latentHeat -= (double)amountProduced;
		if(latentHeat < 0) {
			latentHeat = 0;
		}
	}
	
	public void addLatentHeat(double newCasingHeat) {
		latentHeat += newCasingHeat;
	}

	public boolean isActive() {
		return this.active;
	}

	public void setActive(Boolean act) {
		if(act == this.active) { return; }
		this.active = act;

		TileEntity te = null; 
		IMultiblockPart part = null;
		for(CoordTriplet coord : connectedBlocks) {
			te = this.worldObj.getBlockTileEntity(coord.x, coord.y, coord.z);
			if(te != null && te instanceof IMultiblockPart) {
				part = (IMultiblockPart)te;
				if(this.active) { part.onMachineActivated(); }
				else { part.onMachineDeactivated(); }
			}
		}
	}

	public double getHeat() {
		return latentHeat;
	}

	public int getActiveFuelRodCount() {
		// TODO: Make this the number of columns that actually have fuel
		return activeFuelColumns.size();
	}


	// Static validation helpers
	// Yellorium fuel rods, water and air.
	protected boolean isBlockGoodForInterior(World world, int x, int y, int z) {
		Material material = world.getBlockMaterial(x, y, z);
		if(material == net.minecraft.block.material.MaterialLiquid.water ||
			material == net.minecraft.block.material.Material.air) {
			return true;
		}
		else if(world.getBlockId(x, y, z) == BigReactors.blockYelloriumFuelRod.blockID) {
			// TODO: Make this a reactor part like any other?
			// Ensure that the block above is either a fuel rod or a control rod
			
			int blockTypeAbove = world.getBlockId(x, y+1, z);
			int blockMetaAbove = world.getBlockMetadata(x,  y+1, z);
			if(blockTypeAbove != BigReactors.blockYelloriumFuelRod.blockID &&
				!(blockTypeAbove == BigReactors.blockReactorPart.blockID && BlockReactorPart.isControlRod(blockMetaAbove))) {
				return false;
			}
			// It is, ok.
			
			// This will always require fuel rods to run the entire height of the reactor.
			// You can prove it by induction.
			// Go on, do it. I'm not going to put that shit in a comment.
			// also i'm drunk
			
			// Ensure that the block below is either a fuel rod or casing.
			int blockTypeBelow = world.getBlockId(x, y-1, z);
			int blockMetaBelow = world.getBlockMetadata(x, y-1, z);
			if(blockTypeAbove != BigReactors.blockYelloriumFuelRod.blockID &&
					!(blockTypeAbove == BigReactors.blockReactorPart.blockID && BlockReactorPart.isCasing(blockMetaAbove))) {
					return false;
				}
			
			return true;
		}

		return false;
	}
	
	@Override
	public void writeToNBT(NBTTagCompound data) {
		super.writeToNBT(data);
		// TODO Save/Load
	}

	public void readFromNBT(NBTTagCompound data) {
		super.readFromNBT(data);
		// TODO Save/Load
	}

	@Override
	protected int getMinimumNumberOfBlocksForAssembledMachine() {
		return 26;
	}
}

package erogenousbeef.bigreactors.common.multiblock;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

import erogenousbeef.bigreactors.api.HeatPulse;
import erogenousbeef.bigreactors.api.IBeefPowerStorage;
import erogenousbeef.bigreactors.api.IRadiationPulse;
import erogenousbeef.bigreactors.common.BRUtilities;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.block.BlockReactorPart;
import erogenousbeef.bigreactors.common.tileentity.TileEntityFuelRod;
import erogenousbeef.bigreactors.common.tileentity.TileEntityReactorAccessPort;
import erogenousbeef.bigreactors.common.tileentity.TileEntityReactorControlRod;
import erogenousbeef.bigreactors.common.tileentity.TileEntityReactorPart;
import erogenousbeef.bigreactors.common.tileentity.TileEntityReactorPowerTap;
import erogenousbeef.bigreactors.net.PacketWrapper;
import erogenousbeef.bigreactors.net.Packets;
import erogenousbeef.core.common.CoordTriplet;
import erogenousbeef.core.multiblock.IMultiblockPart;
import erogenousbeef.core.multiblock.MultiblockControllerBase;
import erogenousbeef.core.multiblock.MultiblockRegistry;

public class MultiblockReactor extends MultiblockControllerBase implements IBeefPowerStorage {
	// Game stuff
	protected boolean active;
	private double latentHeat;
	private double storedEnergy;	// Internal units
	
	private LinkedList<CoordTriplet> activePowerTaps;
	// Highest internal Y-coordinate in the fuel column
	private LinkedList<CoordTriplet> attachedControlRods;
	private LinkedList<CoordTriplet> attachedAccessPorts;
	private LinkedList<CoordTriplet> attachedControllers;

	private Set<EntityPlayer> updatePlayers;
	private int ticksSinceLastUpdate;
	private static final int ticksBetweenUpdates = 3;
	private static final int maxEnergyStored = 1000000;
	
	public MultiblockReactor(World world) {
		super(world);

		// Game stuff
		active = false;
		latentHeat = 0.0;
		storedEnergy = 0;
		activePowerTaps = new LinkedList<CoordTriplet>();
		attachedControlRods = new LinkedList<CoordTriplet>();
		attachedAccessPorts = new LinkedList<CoordTriplet>();
		attachedControllers = new LinkedList<CoordTriplet>();
		
		updatePlayers = new HashSet<EntityPlayer>();
		
		ticksSinceLastUpdate = 0;
	}
	
	public void beginUpdatingPlayer(EntityPlayer playerToUpdate) {
		updatePlayers.add(playerToUpdate);
		sendIndividualUpdate(playerToUpdate);
	}
	
	public void stopUpdatingPlayer(EntityPlayer playerToRemove) {
		updatePlayers.remove(playerToRemove);
	}
	
	@Override
	protected void onBlockAdded(IMultiblockPart part) {
		CoordTriplet coord = part.getWorldLocation();
		if(part instanceof TileEntityReactorAccessPort) {
			if(!attachedAccessPorts.contains(coord)) {
				attachedAccessPorts.add(coord);
			}
		}
		else if(part instanceof TileEntityReactorControlRod) {
			if(!attachedControlRods.contains(coord)) {
				attachedControlRods.add(coord);
			}
		}
		else if(part instanceof TileEntityReactorPart) {
			int metadata = ((TileEntityReactorPart)part).getBlockMetadata();
			if(BlockReactorPart.isController(metadata) && !attachedControllers.contains(coord)) {
				attachedControllers.add(coord);
			}
		}
	}
	
	@Override
	protected void onBlockRemoved(IMultiblockPart part) {
		CoordTriplet coord = part.getWorldLocation();
		if(part instanceof TileEntityReactorAccessPort) {
			if(attachedAccessPorts.contains(coord)) {
				attachedAccessPorts.remove(coord);
			}
		}
		else if(part instanceof TileEntityReactorControlRod) {
			attachedControlRods.remove(coord);
			if(attachedControlRods.contains(coord)) {
				attachedControlRods.remove(coord);
			}
		}
		else if(part instanceof TileEntityReactorPart) {
			int metadata = ((TileEntityReactorPart)part).getBlockMetadata();
			if(BlockReactorPart.isController(metadata) && attachedControllers.contains(coord)) {
				attachedControllers.remove(coord);
			}
		}
	}
	
	@Override
	protected void assembleMachine() {
		super.assembleMachine();
	}
	
	@Override
	protected void disassembleMachine() {
		this.active = false;
		super.disassembleMachine();
	}

	@Override
	protected boolean isMachineWhole() {
		// Ensure that there is at least one controller and control rod attached.
		if(attachedControlRods.size() < 1) {
			return false;
		}
		
		if(attachedControllers.size() < 1) {
			return false;
		}
		
		return super.isMachineWhole();
	}
	
	@Override
	public void updateMultiblockEntity() {		
		super.updateMultiblockEntity();
		
		if(!this.isWholeMachine) { return; }
		
		double oldHeat = this.getHeat();

		// How much waste do we have?
		int wasteAmt = 0;
		int freeFuelSpace = 0;
		
		double newHeat = 0.0;
		IRadiationPulse radiationResult;

		// Look for waste and run radiation & heat simulations
		TileEntityReactorControlRod controlRod;
		for(CoordTriplet coord : attachedControlRods) {
			controlRod = (TileEntityReactorControlRod)worldObj.getBlockTileEntity(coord.x, coord.y, coord.z);
			wasteAmt += controlRod.getWasteAmount();
			freeFuelSpace += controlRod.getSizeOfFuelTank() - controlRod.getFuelAmount();
			
			if(this.isActive()) {
				radiationResult = controlRod.radiate();
				this.addStoredEnergy(radiationResult.getPowerProduced());
				newHeat += radiationResult.getHeatProduced();
			}
			
			HeatPulse heatPulse = controlRod.onRadiateHeat(getHeat());
			if(heatPulse != null) {
				this.addStoredEnergy(heatPulse.powerProduced);
				newHeat += heatPulse.heatChange;
			}
		}

		// Now apply delta-heat
		latentHeat += newHeat;
		
		// If we can, poop out waste and inject new fuel
		if(freeFuelSpace >= 1000 || wasteAmt >= 1000) {
			
			ItemStack wasteToDistribute = null;
			if(wasteAmt >= 1000) {
				// TODO: Make this query the existing fuel type for the right type of waste to create
				wasteToDistribute = OreDictionary.getOres("ingotCyanite").get(0).copy();
				wasteToDistribute.stackSize = wasteAmt/1000;
			}

			int fuelIngotsToConsume = freeFuelSpace / 1000;
			int fuelIngotsConsumed = 0;
			
			// Distribute waste, slurp in ingots.
			for(CoordTriplet coord : attachedAccessPorts) {
				if(fuelIngotsToConsume <= 0 && (wasteToDistribute == null || wasteToDistribute.stackSize <= 0)) {
					break;
				}

				TileEntityReactorAccessPort port = (TileEntityReactorAccessPort)worldObj.getBlockTileEntity(coord.x, coord.y, coord.z);
				ItemStack fuelStack = port.getStackInSlot(TileEntityReactorAccessPort.SLOT_INLET);
				
				if(fuelStack != null) {
					if(fuelStack.stackSize >= fuelIngotsToConsume) {
						fuelStack = BRUtilities.consumeItem(fuelStack, fuelIngotsToConsume);
						fuelIngotsConsumed = fuelIngotsToConsume;
						fuelIngotsToConsume = 0;
					}
					else {
						fuelIngotsConsumed += fuelStack.stackSize;
						fuelIngotsToConsume -= fuelStack.stackSize;
						fuelStack = BRUtilities.consumeItem(fuelStack, fuelStack.stackSize);
					}
					port.setInventorySlotContents(TileEntityReactorAccessPort.SLOT_INLET, fuelStack);
				}

				if(wasteToDistribute != null && wasteToDistribute.stackSize > 0) {
					tryDistributeWaste(port, coord, wasteToDistribute, false);
				}
			}
			
			// If we have waste leftover and we have multiple ports, go back over them for the
			// outlets.
			if(wasteToDistribute != null && wasteToDistribute.stackSize > 0 && attachedAccessPorts.size() > 1) {
				for(CoordTriplet coord : attachedAccessPorts) {
					if(wasteToDistribute == null || wasteToDistribute.stackSize <= 0) {
						break;
					}

					TileEntityReactorAccessPort port = (TileEntityReactorAccessPort)worldObj.getBlockTileEntity(coord.x, coord.y, coord.z);
					tryDistributeWaste(port, coord, wasteToDistribute, true);
				}
			}
			
			// Okay... let's modify the fuel rods now
			if((wasteToDistribute != null && wasteToDistribute.stackSize != wasteAmt / 1000) || fuelIngotsConsumed > 0) {
				int fuelToDistribute = fuelIngotsConsumed * 1000;
				int wasteToConsume = 0;
				if(wasteToDistribute != null) {
					wasteToConsume = ((wasteAmt/1000) - wasteToDistribute.stackSize) * 1000;
				}
				
				for(CoordTriplet coord : attachedControlRods) {
					if(wasteToConsume <= 0 && fuelToDistribute <= 0) { break; }
					
					controlRod = (TileEntityReactorControlRod)worldObj.getBlockTileEntity(coord.x, coord.y, coord.z);
					if(wasteToConsume > 0) {
						int amtDrained = controlRod.removeWaste(controlRod.getWasteType(), wasteToConsume, true);
						wasteToConsume -= amtDrained;
					}
					
					if(fuelToDistribute > 0) {
						if(controlRod.getFuelType() == null) {
							// TODO: Discover fuel type
							ItemStack fuel = OreDictionary.getOres("ingotUranium").get(0).copy();
							int fuelAdded = controlRod.addFuel(fuel, fuelToDistribute, true);
						}
					}
				}
			}
		} // End fuel/waste autotransfer

		if(this.isActive()) {
			// Distribute available power
			int energyAvailable = (int)getStoredEnergy();
			int energyRemaining = energyAvailable;
			if(activePowerTaps.size() > 0) {
				for(CoordTriplet coord : activePowerTaps) {
					if(energyRemaining <= 0) { break; }
					
					TileEntityReactorPowerTap tap = (TileEntityReactorPowerTap)this.worldObj.getBlockTileEntity(coord.x, coord.y, coord.z);
					energyRemaining = tap.onProvidePower(energyRemaining);
				}
			}
			
			if(energyAvailable != energyRemaining) {
				reduceStoredEnergy((double)(energyAvailable - energyRemaining));
			}
		}

		// leak 1% of heat to the environment
		// TODO: Replace this with a better equation, so low heats leak less
		// and high heats leak far more.
		
		// 1% base loss rate, +1% per thousand degrees C
		double lossRate = 0.01 + ((double)this.latentHeat * 0.000001);
		
		double latentHeatLoss = Math.max(1.0, this.latentHeat * 0.01);
		latentHeat -= latentHeatLoss;
		if(latentHeat < 0.0) { latentHeat = 0.0; }

		// Send updates periodically
		ticksSinceLastUpdate++;
		if(ticksSinceLastUpdate >= ticksBetweenUpdates) {
			ticksSinceLastUpdate = 0;
			sendTickUpdate();
		}
		
		// TODO: Overload/overheat
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

	public double getStoredEnergy() {
		return storedEnergy;
	}

	public void setStoredEnergy(double newEnergy) {
		storedEnergy = newEnergy;
	}
	
	public void addStoredEnergy(double newEnergy) {
		storedEnergy += newEnergy;
		if(storedEnergy > maxEnergyStored) { storedEnergy = maxEnergyStored; }
	}

	protected void reduceStoredEnergy(double energy) {
		storedEnergy -= energy;
		if(storedEnergy < 0.0) {
			storedEnergy = 0.0;
		}
	}
	
	public void addLatentHeat(double newCasingHeat) {
		latentHeat += newCasingHeat;
	}

	public boolean isActive() {
		return this.active;
	}

	public void setActive(boolean act) {
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
			else {
			}
		}
	}

	public double getHeat() {
		return latentHeat;
	}
	
	public void setHeat(double newHeat) {
		latentHeat = newHeat;
	}

	public int getFuelColumnCount() {
		return attachedControlRods.size();
	}


	// Static validation helpers
	// Water and air.
	protected boolean isBlockGoodForInterior(World world, int x, int y, int z) {
		Material material = world.getBlockMaterial(x, y, z);
		if(material == net.minecraft.block.material.MaterialLiquid.water ||
			material == net.minecraft.block.material.Material.air) {
			return true;
		}

		return false;
	}
	
	@Override
	public void writeToNBT(NBTTagCompound data) {
		data.setBoolean("reactorActive", this.active);
		data.setDouble("heat", this.latentHeat);
		data.setDouble("storedEnergy", this.storedEnergy);
	}

	@Override
	public void readFromNBT(NBTTagCompound data) {
		if(data.hasKey("reactorActive")) {
			this.active = data.getBoolean("reactorActive");
		}
		
		if(data.hasKey("heat")) {
			this.latentHeat = data.getDouble("heat");
		}
		
		if(data.hasKey("storedEnergy")) {
			this.storedEnergy = data.getDouble("storedEnergy");
		}
	}

	@Override
	protected int getMinimumNumberOfBlocksForAssembledMachine() {
		// Hollow cube.
		return 26;
	}

	@Override
	public void formatDescriptionPacket(NBTTagCompound data) {
	}

	@Override
	public void decodeDescriptionPacket(NBTTagCompound data) {
	}

	protected Packet getUpdatePacket() {
		return PacketWrapper.createPacket(BigReactors.CHANNEL,
				 Packets.ReactorControllerFullUpdate,
				 new Object[] { referenceCoord.x,
								referenceCoord.y,
								referenceCoord.z,
								this.active,
								this.latentHeat,
								this.storedEnergy});
	}
	
	/**
	 * Sends a full state update to a player.
	 */
	protected void sendIndividualUpdate(EntityPlayer player) {
		if(this.worldObj.isRemote) { return; }

		PacketDispatcher.sendPacketToPlayer(getUpdatePacket(), (Player)player);
	}
	
	/**
	 * Send an update to any clients with GUIs open
	 */
	protected void sendTickUpdate() {
		if(this.worldObj.isRemote) { return; }
		if(this.updatePlayers.size() <= 0) { return; }
		
		Packet data = getUpdatePacket();

		for(EntityPlayer player : updatePlayers) {
			PacketDispatcher.sendPacketToPlayer(data, (Player)player);
		}
	}
	
	private void tryDistributeWaste(TileEntityReactorAccessPort port, CoordTriplet coord, ItemStack wasteToDistribute, boolean distributeToInputs) {
		ItemStack wasteStack = port.getStackInSlot(TileEntityReactorAccessPort.SLOT_OUTLET);
		int metadata = worldObj.getBlockMetadata(coord.x, coord.y, coord.z);

		if(metadata == BlockReactorPart.ACCESSPORT_OUTLET || (distributeToInputs || attachedAccessPorts.size() < 2)) {
			// Dump waste preferentially to outlets, unless we only have one access port
			if(wasteStack == null) {
				if(wasteToDistribute.stackSize > port.getInventoryStackLimit()) {
					ItemStack newStack = wasteToDistribute.splitStack(port.getInventoryStackLimit());
					port.setInventorySlotContents(TileEntityReactorAccessPort.SLOT_OUTLET, newStack);
				}
				else {
					port.setInventorySlotContents(TileEntityReactorAccessPort.SLOT_OUTLET, wasteToDistribute);
					wasteToDistribute.stackSize = 0;
				}
			}
			else {
				ItemStack existingStack = port.getStackInSlot(TileEntityReactorAccessPort.SLOT_OUTLET);
				if(existingStack.isItemEqual(wasteToDistribute)) {
					if(existingStack.stackSize + wasteToDistribute.stackSize <= existingStack.getMaxStackSize()) {
						existingStack.stackSize += wasteToDistribute.stackSize;
						wasteToDistribute.stackSize = 0;
					}
					else {
						int amt = existingStack.getMaxStackSize() - existingStack.stackSize;
						wasteToDistribute.stackSize -= existingStack.getMaxStackSize() - existingStack.stackSize;
						existingStack.stackSize += amt;
					}
				}
			}
			
			port.onWasteReceived();
		}
	}

	@Override
	protected void onMachineMerge(MultiblockControllerBase otherMachine) {
		this.activePowerTaps.clear();
		this.attachedAccessPorts.clear();
		this.attachedControllers.clear();
		this.attachedControlRods.clear();
	}

	// IBeefPowerStorage
	@Override
	public int getEnergyStored() {
		return (int)storedEnergy;
	}

	@Override
	public int getMaxEnergyStored() {
		return maxEnergyStored;
	}
}

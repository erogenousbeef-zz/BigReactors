package erogenousbeef.bigreactors.common.multiblock;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;
import cofh.api.energy.IEnergyHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import erogenousbeef.bigreactors.api.HeatPulse;
import erogenousbeef.bigreactors.api.IRadiationPulse;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.block.BlockReactorPart;
import erogenousbeef.bigreactors.common.tileentity.TileEntityReactorAccessPort;
import erogenousbeef.bigreactors.common.tileentity.TileEntityReactorControlRod;
import erogenousbeef.bigreactors.common.tileentity.TileEntityReactorPart;
import erogenousbeef.bigreactors.common.tileentity.TileEntityReactorPowerTap;
import erogenousbeef.bigreactors.common.tileentity.TileEntityReactorRedNetPort;
import erogenousbeef.bigreactors.net.PacketWrapper;
import erogenousbeef.bigreactors.net.Packets;
import erogenousbeef.bigreactors.utils.StaticUtils;
import erogenousbeef.core.common.CoordTriplet;
import erogenousbeef.core.multiblock.IMultiblockPart;
import erogenousbeef.core.multiblock.MultiblockControllerBase;

public class MultiblockReactor extends MultiblockControllerBase implements IEnergyHandler {
	// Game stuff
	protected boolean active;
	private float latentHeat;
	private WasteEjectionSetting wasteEjection;
	private float energyStored;

	// UI stuff
	private float energyGeneratedLastTick;
	private int fuelConsumedLastTick;
	
	public enum WasteEjectionSetting {
		kAutomatic,					// Full auto, always remove waste
		kAutomaticOnlyIfCanReplace, // Remove only if it can be replaced with fuel
		kManual, 					// Manual, only on button press
	}
	
	private Set<CoordTriplet> attachedPowerTaps;
	private Set<CoordTriplet> attachedRedNetPorts;

	// TODO: Convert these to sets.
	private LinkedList<CoordTriplet> attachedControlRods; 	// Highest internal Y-coordinate in the fuel column
	private LinkedList<CoordTriplet> attachedAccessPorts;
	private LinkedList<CoordTriplet> attachedControllers;

	private Set<EntityPlayer> updatePlayers;
	private int ticksSinceLastUpdate;
	private static final int ticksBetweenUpdates = 3;
	private static final int maxEnergyStored = 10000000;
	
	public MultiblockReactor(World world) {
		super(world);

		// Game stuff
		active = false;
		latentHeat = 0f;
		energyGeneratedLastTick = 0f;
		fuelConsumedLastTick = 0;
		wasteEjection = WasteEjectionSetting.kAutomatic;
		attachedPowerTaps = new HashSet<CoordTriplet>();
		attachedRedNetPorts = new HashSet<CoordTriplet>();
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
		else if(part instanceof TileEntityReactorPowerTap) {
			attachedPowerTaps.add(coord);
		}
		else if(part instanceof TileEntityReactorRedNetPort) {
			attachedRedNetPorts.add(coord);
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
		else if(part instanceof TileEntityReactorPowerTap) {
			attachedPowerTaps.remove(coord);
		}
		else if(part instanceof TileEntityReactorRedNetPort) {
			attachedRedNetPorts.remove(coord);
		}
		else if(part instanceof TileEntityReactorPart) {
			int metadata = ((TileEntityReactorPart)part).getBlockMetadata();
			if(BlockReactorPart.isController(metadata) && attachedControllers.contains(coord)) {
				attachedControllers.remove(coord);
			}
		}
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
	
	// Update loop. Only called when the machine is assembled.
	@Override
	public boolean update() {
		if(Float.isNaN(this.getHeat())) {
			this.setHeat(0.0f);
		}
		
		float oldHeat = this.getHeat();
		float oldEnergy = this.getEnergyStored();
		energyGeneratedLastTick = 0f;
		fuelConsumedLastTick = 0;

		// How much waste do we have?
		int wasteAmt = 0;
		int freeFuelSpace = 0;
		
		float newHeat = 0f;
		IRadiationPulse radiationResult;

		// Look for waste and run radiation & heat simulations
		TileEntityReactorControlRod controlRod;
		for(CoordTriplet coord : attachedControlRods) {
			controlRod = (TileEntityReactorControlRod)worldObj.getBlockTileEntity(coord.x, coord.y, coord.z);
			if(controlRod == null) { continue; } // Happens due to chunk unloads

			if(this.isActive()) {
				int fuelChange = controlRod.getFuelAmount();
				radiationResult = controlRod.radiate();
				fuelChange -= controlRod.getFuelAmount();
				if(fuelChange > 0) { fuelConsumedLastTick += fuelChange; }

				this.generateEnergy(radiationResult.getPowerProduced());
			}
			
			HeatPulse heatPulse = controlRod.onRadiateHeat(getHeat());
			if(heatPulse != null) {
				this.addLatentHeat(heatPulse.heatChange);
			}
			
			wasteAmt += controlRod.getWasteAmount();
			freeFuelSpace += controlRod.getSizeOfFuelTank() - controlRod.getTotalContainedAmount();
		}
		
		// If we can, poop out waste and inject new fuel.
		// TODO: Change so control rods are individually considered for fueling instead
		// of doing it in aggregate.
		if(freeFuelSpace >= 1000 || wasteAmt >= 1000) {
			// Auto/Replace: Discover amount of available fuel and peg wasteAmt to that.
			if(this.wasteEjection == WasteEjectionSetting.kAutomaticOnlyIfCanReplace) {
				int fuelIngotsAvailable = 0;
				for(CoordTriplet coord : attachedAccessPorts) {
					TileEntityReactorAccessPort port = (TileEntityReactorAccessPort)worldObj.getBlockTileEntity(coord.x, coord.y, coord.z);
					if(port == null) { continue; }

					ItemStack fuelStack = port.getStackInSlot(TileEntityReactorAccessPort.SLOT_INLET);
					if(fuelStack != null) {
						fuelIngotsAvailable += fuelStack.stackSize;
					}
				}

				if(wasteAmt/1000 > fuelIngotsAvailable) {
					wasteAmt = fuelIngotsAvailable * 1000;
				}
				
				// Consider any space made by distributable waste to be free space.
				freeFuelSpace += wasteAmt;
			} else if(this.wasteEjection == WasteEjectionSetting.kManual) {
				// Manual just means to suppress waste injection, not ignore incoming fuel. Sooo..
				wasteAmt = 0;
			}
			else {
				// Automatic - consider waste to be spare space for fuel
				freeFuelSpace += wasteAmt;
			}
			
			if(freeFuelSpace >= 1000 || wasteAmt >= 1000) {
				tryEjectWaste(freeFuelSpace, wasteAmt);
			}
		}

		// leak 1% of heat to the environment per tick
		// TODO: Better equation.
		if(latentHeat > 0.0f) {
			float latentHeatLoss = Math.max(0.05f, this.latentHeat * 0.01f);
			if(this.latentHeat < latentHeatLoss) { latentHeatLoss = latentHeat; }
			this.addLatentHeat(-1 * latentHeatLoss);

			// Generate power based on the amount of heat lost
			this.generateEnergy(latentHeatLoss * BigReactors.powerPerHeat);
		}
		
		if(latentHeat < 0.0f) { setHeat(0.0f); }
		
		// Distribute available power
		int energyAvailable = (int)getEnergyStored();
		int energyRemaining = energyAvailable;
		if(attachedPowerTaps.size() > 0 && energyRemaining > 0) {
			// First, try to distribute fairly
			int splitEnergy = energyRemaining / attachedPowerTaps.size();
			for(CoordTriplet coord : attachedPowerTaps) {
				if(energyRemaining <= 0) { break; }
				
				TileEntityReactorPowerTap tap = (TileEntityReactorPowerTap)this.worldObj.getBlockTileEntity(coord.x, coord.y, coord.z);
				if(tap == null) { continue; }

				energyRemaining -= splitEnergy - tap.onProvidePower(splitEnergy);
			}

			// Next, just hose out whatever we can, if we have any left
			if(energyRemaining > 0) {
				for(CoordTriplet coord : attachedPowerTaps) {
					if(energyRemaining <= 0) { break; }
					
					TileEntityReactorPowerTap tap = (TileEntityReactorPowerTap)this.worldObj.getBlockTileEntity(coord.x, coord.y, coord.z);
					if(tap == null) { continue; }

					energyRemaining = tap.onProvidePower(energyRemaining);
				}
			}
		}
		
		if(energyAvailable != energyRemaining) {
			reduceStoredEnergy((energyAvailable - energyRemaining));
		}

		// Send updates periodically
		ticksSinceLastUpdate++;
		if(ticksSinceLastUpdate >= ticksBetweenUpdates) {
			ticksSinceLastUpdate = 0;
			sendTickUpdate();
		}
		
		// TODO: Overload/overheat

		// Update any connected rednet networks
		for(CoordTriplet coord : attachedRedNetPorts) {
			TileEntityReactorRedNetPort port = (TileEntityReactorRedNetPort)worldObj.getBlockTileEntity(coord.x, coord.y, coord.z);
			if(port == null) { continue; }
			port.updateRedNetNetwork();
		}

		return (oldHeat != this.getHeat() || oldEnergy != this.getEnergyStored());
	}
	
	public void setStoredEnergy(float oldEnergy) {
		energyStored = oldEnergy;
		if(energyStored < 0.0 || Float.isNaN(energyStored)) {
			energyStored = 0.0f;
		}
		else if(energyStored > maxEnergyStored) {
			energyStored = maxEnergyStored;
		}
	}
	
	/**
	 * Generate energy, internally. Will be multiplied by the BR Setting powerProductionMultiplier
	 * @param newEnergy Base, unmultiplied energy to generate
	 */
	protected void generateEnergy(float newEnergy) {
		this.energyGeneratedLastTick += newEnergy * BigReactors.powerProductionMultiplier;
		this.addStoredEnergy(newEnergy * BigReactors.powerProductionMultiplier);
	}

	/**
	 * Add some energy to the internal storage buffer.
	 * Will not increase the buffer above the maximum or reduce it below 0.
	 * @param newEnergy
	 */
	protected void addStoredEnergy(float newEnergy) {
		if(Float.isNaN(newEnergy)) { return; }

		energyStored += newEnergy;
		if(energyStored > maxEnergyStored) {
			energyStored = maxEnergyStored;
		}
		if(-0.00001f < energyStored && energyStored < 0.00001f) {
			// Clamp to zero
			energyStored = 0f;
		}
	}

	/**
	 * Remove some energy from the internal storage buffer.
	 * Will not reduce the buffer below 0.
	 * @param energy Amount by which the buffer should be reduced.
	 */
	protected void reduceStoredEnergy(float energy) {
		this.addStoredEnergy(-1f * energy);
	}
	
	protected void addLatentHeat(float newCasingHeat) {
		if(Float.isNaN(newCasingHeat)) {
			return;
		}

		latentHeat += newCasingHeat;
		// Clamp to zero to prevent floating point issues
		if(-0.00001f < latentHeat && latentHeat < 0.00001f) { latentHeat = 0.0f; }
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
			if(te instanceof IMultiblockPart) {
				part = (IMultiblockPart)te;
				if(this.active) { part.onMachineActivated(); }
				else { part.onMachineDeactivated(); }
			}
		}
	}

	public float getHeat() {
		return latentHeat;
	}
	
	public void setHeat(float newHeat) {
		if(Float.isNaN(newHeat)) {
			latentHeat = 0.0f;
		}
		else {
			latentHeat = newHeat;
		}
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
		data.setFloat("heat", this.latentHeat);
		data.setFloat("storedEnergy", this.energyStored);
		data.setInteger("wasteEjection", this.wasteEjection.ordinal());
	}

	@Override
	public void readFromNBT(NBTTagCompound data) {
		if(data.hasKey("reactorActive")) {
			setActive(data.getBoolean("reactorActive"));
		}
		
		if(data.hasKey("heat")) {
			setHeat(data.getFloat("heat"));
		}
		else {
			setHeat(0.0f);
		}
		
		if(data.hasKey("storedEnergy")) {
			setStoredEnergy(data.getFloat("storedEnergy"));
		}
		else {
			setStoredEnergy(0.0f);
		}
		
		if(data.hasKey("wasteEjection")) {
			this.wasteEjection = WasteEjectionSetting.values()[data.getInteger("wasteEjection")];
		}
	}

	@Override
	protected int getMinimumNumberOfBlocksForAssembledMachine() {
		// Hollow cube.
		return 26;
	}

	@Override
	public void formatDescriptionPacket(NBTTagCompound data) {
		data.setInteger("wasteEjection", this.wasteEjection.ordinal());
	}

	@Override
	public void decodeDescriptionPacket(NBTTagCompound data) {
		if(data.hasKey("wasteEjection")) {
			this.wasteEjection = WasteEjectionSetting.values()[data.getInteger("wasteEjection")];
		}
	}

	protected Packet getUpdatePacket() {
		return PacketWrapper.createPacket(BigReactors.CHANNEL,
				 Packets.ReactorControllerFullUpdate,
				 new Object[] { referenceCoord.x,
								referenceCoord.y,
								referenceCoord.z,
								this.active,
								this.latentHeat,
								energyStored,
								this.energyGeneratedLastTick,
								this.fuelConsumedLastTick});
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
	 * @param energyGenerated 
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
					port.setInventorySlotContents(TileEntityReactorAccessPort.SLOT_OUTLET, wasteToDistribute.copy());
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
		this.attachedPowerTaps.clear();
		this.attachedRedNetPorts.clear();
		this.attachedAccessPorts.clear();
		this.attachedControllers.clear();
		this.attachedControlRods.clear();
	}

	public float getEnergyStored() {
		return energyStored;
	}

	/**
	 * Increment the waste ejection setting by 1 value.
	 */
	public void changeWasteEjection() {
		WasteEjectionSetting[] settings = WasteEjectionSetting.values();
		int newIdx = this.wasteEjection.ordinal() + 1;
		if(newIdx >= settings.length) {
			newIdx = 0;
		}
		
		WasteEjectionSetting newSetting = settings[newIdx];
		
		setWasteEjection(newSetting);
	}
	
	/**
	 * Directly set the waste ejection setting. Will dispatch network updates
	 * from server to interested clients.
	 * @param newSetting The new waste ejection setting.
	 */
	public void setWasteEjection(WasteEjectionSetting newSetting) {
		if(this.wasteEjection != newSetting) {
			this.wasteEjection = newSetting;
			
			if(!this.worldObj.isRemote) {
				if(this.updatePlayers.size() > 0) {
					Packet updatePacket = PacketWrapper.createPacket(BigReactors.CHANNEL,
							 Packets.ReactorWasteEjectionSettingUpdate,
							 new Object[] { referenceCoord.x,
											referenceCoord.y,
											referenceCoord.z,
											this.wasteEjection.ordinal() });
					
					for(EntityPlayer player : updatePlayers) {
						PacketDispatcher.sendPacketToPlayer(updatePacket, (Player)player);
					}
				}
			}
		}
	}
	
	public WasteEjectionSetting getWasteEjection() {
		return this.wasteEjection;
	}
	
	public void ejectWaste() {
		TileEntityReactorControlRod controlRod;
		int wasteAmt = 0;
		int freeFuelSpace = 0;
		for(CoordTriplet coord : attachedControlRods) {
			controlRod = (TileEntityReactorControlRod)worldObj.getBlockTileEntity(coord.x, coord.y, coord.z);
			if(controlRod == null) { continue; }

			wasteAmt += controlRod.getWasteAmount();
			freeFuelSpace += controlRod.getSizeOfFuelTank() - controlRod.getFuelAmount();
		}
		
		if(freeFuelSpace >= 1000 || wasteAmt >= 1000) {
			tryEjectWaste(freeFuelSpace, wasteAmt);
		}
	}
	
	/**
	 * Honestly attempt to eject waste and inject fuel, up to a certain amount.
	 * @param fuelAmt Amount of fuel to inject.
	 * @param wasteAmt Amount of waste to eject.
	 */
	protected void tryEjectWaste(int fuelAmt, int wasteAmt) {
		if(fuelAmt < 1000 && wasteAmt < 1000) { return; }

		ItemStack wasteToDistribute = null;
		if(wasteAmt >= 1000) {
			// TODO: Make this query the existing fuel type for the right type of waste to create
			wasteToDistribute = OreDictionary.getOres("ingotCyanite").get(0).copy();
			wasteToDistribute.stackSize = wasteAmt/1000;
		}

		int fuelIngotsToConsume = fuelAmt / 1000;
		int fuelIngotsConsumed = 0;
		
		// Distribute waste, slurp in ingots.
		for(CoordTriplet coord : attachedAccessPorts) {
			if(fuelIngotsToConsume <= 0 && (wasteToDistribute == null || wasteToDistribute.stackSize <= 0)) {
				break;
			}

			TileEntityReactorAccessPort port = (TileEntityReactorAccessPort)worldObj.getBlockTileEntity(coord.x, coord.y, coord.z);
			if(port == null) { continue; }

			ItemStack fuelStack = port.getStackInSlot(TileEntityReactorAccessPort.SLOT_INLET);
			
			if(fuelStack != null) {
				if(fuelStack.stackSize >= fuelIngotsToConsume) {
					fuelStack = StaticUtils.Inventory.consumeItem(fuelStack, fuelIngotsToConsume);
					fuelIngotsConsumed = fuelIngotsToConsume;
					fuelIngotsToConsume = 0;
				}
				else {
					fuelIngotsConsumed += fuelStack.stackSize;
					fuelIngotsToConsume -= fuelStack.stackSize;
					fuelStack = StaticUtils.Inventory.consumeItem(fuelStack, fuelStack.stackSize);
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
				if(port == null) { continue; }

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
			
			TileEntityReactorControlRod controlRod;
			for(CoordTriplet coord : attachedControlRods) {
				if(wasteToConsume <= 0 && fuelToDistribute <= 0) { break; }
				
				controlRod = (TileEntityReactorControlRod)worldObj.getBlockTileEntity(coord.x, coord.y, coord.z);
				if(controlRod == null) { continue; }

				if(wasteToConsume > 0 && controlRod.getWasteAmount() > 0) {
					int amtDrained = controlRod.removeWaste(new FluidStack(controlRod.getWasteType(), wasteToConsume), wasteToConsume, true);
					wasteToConsume -= amtDrained;
				}
				
				if(fuelToDistribute > 0) {
					if(controlRod.getFuelType() == null) {
						// TODO: Discover fuel type
						FluidStack fuel = new FluidStack(BigReactors.fluidYellorium, fuelToDistribute);
						fuelToDistribute -= controlRod.addFuel(fuel, fuelToDistribute, true);
					}
					else {
						fuelToDistribute -= controlRod.addFuel(new FluidStack(controlRod.getFuelType(), fuelToDistribute), fuelToDistribute, true);
					}
				}
			}
		}
	} // End fuel/waste autotransfer		

	@Override
	protected void onMachineAssembled() {
	}

	@Override
	protected void onMachineRestored() {
	}

	@Override
	protected void onMachinePaused() {
	}

	@Override
	protected void onMachineDisassembled() {
		this.active = false;
	}

	@Override
	protected int getMaximumXSize() {
		return BigReactors.maximumReactorSize;
	}

	@Override
	protected int getMaximumZSize() {
		return BigReactors.maximumReactorSize;
	}

	@Override
	protected int getMaximumYSize() {
		return BigReactors.maximumReactorHeight;
	}

	/**
	 * Used to update the UI
	 */
	public void setEnergyGeneratedLastTick(float energyGeneratedLastTick) {
		this.energyGeneratedLastTick = energyGeneratedLastTick;
	}

	/**
	 * UI Helper
	 */
	public float getEnergyGeneratedLastTick() {
		return this.energyGeneratedLastTick;
	}
	
	/**
	 * Used to update the UI
	 */
	public void setFuelConsumedLastTick(int fuelConsumed) {
		this.fuelConsumedLastTick = fuelConsumed;
	}
	
	/**
	 * UI Helper
	 */
	public int getFuelConsumedLastTick() {
		return this.fuelConsumedLastTick;
	}

	/**
	 * UI Helper
	 * @return Percentile fuel richness (fuel/fuel+waste), or 0 if all control rods are empty
	 */
	public float getFuelRichness() {
		int amtFuel, amtWaste;
		TileEntityReactorControlRod controlRod = null;
		amtFuel = amtWaste = 0;

		for(CoordTriplet coord : this.attachedControlRods) {
			controlRod = (TileEntityReactorControlRod)worldObj.getBlockTileEntity(coord.x, coord.y, coord.z);
			if(controlRod == null) { continue; } // Happens due to chunk unloads
		
			amtFuel += controlRod.getFuelAmount();
			amtWaste += controlRod.getWasteAmount();
		}

		if(amtFuel + amtWaste <= 0f) { return 0f; }
		else { return (float)amtFuel / (float)(amtFuel+amtWaste); }
	}

	/** DO NOT USE **/
	@Override
	public int receiveEnergy(ForgeDirection from, int maxReceive,
			boolean simulate) {
		int amtReceived = (int)Math.min(maxReceive, Math.floor(this.maxEnergyStored - this.energyStored));
		if(!simulate) {
			this.addStoredEnergy(amtReceived);
		}
		return amtReceived;
	}

	@Override
	public int extractEnergy(ForgeDirection from, int maxExtract,
			boolean simulate) {
		int amtRemoved = (int)Math.min(maxExtract, this.energyStored);
		if(!simulate) {
			this.reduceStoredEnergy(amtRemoved);
		}
		return amtRemoved;
	}

	@Override
	public boolean canInterface(ForgeDirection from) {
		return false;
	}

	@Override
	public int getEnergyStored(ForgeDirection from) {
		return (int)energyStored;
	}

	@Override
	public int getMaxEnergyStored(ForgeDirection from) {
		return maxEnergyStored;
	}

	public int getMaxEnergyPerTick() {
		return this.maxEnergyStored;
	}
	
	// Redstone helper
	public void setAllControlRodInsertionValues(int newValue) {
		if(this.assemblyState != AssemblyState.Assembled) { return; }
		
		TileEntity te;
		TileEntityReactorControlRod cr;
		for(CoordTriplet coord : attachedControlRods) {
			te = this.worldObj.getBlockTileEntity(coord.x, coord.y, coord.z);
			if(te instanceof TileEntityReactorControlRod) {
				cr = (TileEntityReactorControlRod)te;
				cr.setControlRodInsertion((short)newValue);
			}
		}
	}

	public CoordTriplet[] getControlRodLocations() {
		CoordTriplet[] coords = new CoordTriplet[this.attachedControlRods.size()];
		int i = 0;
		for(CoordTriplet coord : this.attachedControlRods) {
			coords[i++] = coord.copy();
		}
		return coords;
	}
}

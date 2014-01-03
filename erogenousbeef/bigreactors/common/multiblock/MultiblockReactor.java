package erogenousbeef.bigreactors.common.multiblock;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.oredict.OreDictionary;
import cofh.api.energy.IEnergyHandler;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import erogenousbeef.bigreactors.api.HeatPulse;
import erogenousbeef.bigreactors.api.IRadiationPulse;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.interfaces.IReactorFuelInfo;
import erogenousbeef.bigreactors.common.interfaces.IReactorTickable;
import erogenousbeef.bigreactors.common.multiblock.block.BlockReactorPart;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorAccessPort;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorControlRod;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorPart;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorPowerTap;
import erogenousbeef.bigreactors.net.PacketWrapper;
import erogenousbeef.bigreactors.net.Packets;
import erogenousbeef.bigreactors.utils.StaticUtils;
import erogenousbeef.core.common.CoordTriplet;
import erogenousbeef.core.multiblock.IMultiblockPart;
import erogenousbeef.core.multiblock.MultiblockControllerBase;
import erogenousbeef.core.multiblock.MultiblockValidationException;

public class MultiblockReactor extends MultiblockControllerBase implements IEnergyHandler, IReactorFuelInfo {
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
	
	private Set<TileEntityReactorPowerTap> attachedPowerTaps;
	private Set<IReactorTickable> attachedTickables;

	private Set<TileEntityReactorControlRod> attachedControlRods; 	// Highest internal Y-coordinate in the fuel column
	private Set<TileEntityReactorAccessPort> attachedAccessPorts;
	private Set<TileEntityReactorPart> attachedControllers;

	private Set<EntityPlayer> updatePlayers;
	private int ticksSinceLastUpdate;
	private static final int ticksBetweenUpdates = 3;
	private static final int maxEnergyStored = 10000000;
	
	public MultiblockReactor(World world) {
		super(world);

		// Game stuff
		active = false;
		latentHeat = 0f;
		energyStored = 0f;
		energyGeneratedLastTick = 0f;
		fuelConsumedLastTick = 0;
		wasteEjection = WasteEjectionSetting.kAutomatic;
		attachedPowerTaps = new HashSet<TileEntityReactorPowerTap>();
		attachedTickables = new HashSet<IReactorTickable>();
		attachedControlRods = new HashSet<TileEntityReactorControlRod>();
		attachedAccessPorts = new HashSet<TileEntityReactorAccessPort>();
		attachedControllers = new HashSet<TileEntityReactorPart>();

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
		if(part instanceof TileEntityReactorAccessPort) {
			attachedAccessPorts.add((TileEntityReactorAccessPort)part);
		}
		
		if(part instanceof TileEntityReactorControlRod) {
			attachedControlRods.add((TileEntityReactorControlRod)part);
		}

		if(part instanceof TileEntityReactorPowerTap) {
			attachedPowerTaps.add((TileEntityReactorPowerTap)part);
		}

		if(part instanceof TileEntityReactorPart) {
			TileEntityReactorPart reactorPart = (TileEntityReactorPart)part;
			if(BlockReactorPart.isController(reactorPart.getBlockMetadata())) {
				attachedControllers.add(reactorPart);
			}
		}

		if(part instanceof IReactorTickable) {
			attachedTickables.add((IReactorTickable)part);
		}
	}
	
	@Override
	protected void onBlockRemoved(IMultiblockPart part) {
		if(part instanceof TileEntityReactorAccessPort) {
			attachedAccessPorts.remove((TileEntityReactorAccessPort)part);
		}

		if(part instanceof TileEntityReactorControlRod) {
			attachedControlRods.remove((TileEntityReactorControlRod)part);
		}

		if(part instanceof TileEntityReactorPowerTap) {
			attachedPowerTaps.remove((TileEntityReactorPowerTap)part);
		}

		if(part instanceof TileEntityReactorPart) {
			TileEntityReactorPart reactorPart = (TileEntityReactorPart)part;
			if(BlockReactorPart.isController(reactorPart.getBlockMetadata())) {
				attachedControllers.remove(reactorPart);
			}
		}

		if(part instanceof IReactorTickable) {
			attachedTickables.remove((IReactorTickable)part);
		}
	}
	
	@Override
	protected boolean isMachineWhole() throws MultiblockValidationException {
		// Ensure that there is at least one controller and control rod attached.
		if(attachedControlRods.size() < 1) {
			throw new MultiblockValidationException("Not enough control rods. Reactors require at least 1.");
		}
		
		if(attachedControllers.size() < 1) {
			throw new MultiblockValidationException("Not enough controllers. Reactors require at least 1.");
		}
		
		return super.isMachineWhole();
	}
	
	@Override
	public void updateClient() {}
	
	// Update loop. Only called when the machine is assembled.
	@Override
	public boolean updateServer() {
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
		for(TileEntityReactorControlRod controlRod : attachedControlRods) {
			if(controlRod == null || !controlRod.isConnected()) { continue; }
			
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
				for(TileEntityReactorAccessPort port : attachedAccessPorts) {
					if(port == null || !port.isConnected()) { continue; }

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
			for(TileEntityReactorPowerTap powerTap : attachedPowerTaps) {
				if(energyRemaining <= 0) { break; }
				if(powerTap == null || !powerTap.isConnected()) { continue; }

				energyRemaining -= splitEnergy - powerTap.onProvidePower(splitEnergy);
			}

			// Next, just hose out whatever we can, if we have any left
			if(energyRemaining > 0) {
				for(TileEntityReactorPowerTap powerTap : attachedPowerTaps) {
					if(energyRemaining <= 0) { break; }
					if(powerTap == null || !powerTap.isConnected()) { continue; }

					energyRemaining = powerTap.onProvidePower(energyRemaining);
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

		// Update any connected tickables
		for(IReactorTickable tickable : attachedTickables) {
			if(tickable == null) { continue; }
			tickable.onReactorTick();
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
	// Water, air, and metal blocks
	protected boolean isBlockGoodForInterior(World world, int x, int y, int z) {
		Material material = world.getBlockMaterial(x, y, z);
		if(material == net.minecraft.block.material.MaterialLiquid.water ||
			material == net.minecraft.block.material.Material.air) {
			return true;
		}
		
		int blockId = world.getBlockId(x, y, z);
		if(blockId == Block.blockIron.blockID || blockId == Block.blockGold.blockID || blockId == Block.blockDiamond.blockID) {
			return true;
		}
		
		// Permit TE fluids
		if(blockId > 0 && blockId < Block.blocksList.length) {
			Block blockClass = Block.blocksList[blockId];
			if(blockClass instanceof IFluidBlock) {
				Fluid fluid = ((IFluidBlock)blockClass).getFluid();
				String fluidName = fluid.getName();
				if(fluidName.equals("redstone") || fluidName.equals("pyrotheum") ||
					fluidName.equals("cryotheum") || fluidName.equals("glowstone") ||
					fluidName.equals("ender")) {
					return true;
				}
			}
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
			setHeat(Math.max(getHeat(), data.getFloat("heat")));
		}
		
		if(data.hasKey("storedEnergy")) {
			setStoredEnergy(Math.max(getEnergyStored(), data.getFloat("storedEnergy")));
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
		data.setFloat("energy", this.energyStored);
		data.setFloat("heat", this.latentHeat);
		data.setBoolean("isActive", this.isActive());
	}

	@Override
	public void decodeDescriptionPacket(NBTTagCompound data) {
		if(data.hasKey("wasteEjection")) {
			this.wasteEjection = WasteEjectionSetting.values()[data.getInteger("wasteEjection")];
		}
		
		if(data.hasKey("isActive")) {
			this.setActive(data.getBoolean("isActive"));
		}
		
		if(data.hasKey("energy")) {
			this.energyStored = data.getFloat("energyStored");
		}
		
		if(data.hasKey("heat")) {
			this.latentHeat = data.getFloat("heat");
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
	
	private void tryDistributeWaste(TileEntityReactorAccessPort port, ItemStack wasteToDistribute, boolean distributeToInputs) {
		ItemStack wasteStack = port.getStackInSlot(TileEntityReactorAccessPort.SLOT_OUTLET);
		CoordTriplet coord = port.getWorldLocation();
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
	protected void onAssimilated(MultiblockControllerBase otherMachine) {
		this.attachedPowerTaps.clear();
		this.attachedTickables.clear();
		this.attachedAccessPorts.clear();
		this.attachedControllers.clear();
		this.attachedControlRods.clear();
	}
	
	@Override
	protected void onAssimilate(MultiblockControllerBase otherMachine) {
		if(!(otherMachine instanceof MultiblockReactor)) {
			FMLLog.warning("[%s] Reactor @ %s is attempting to assimilate a non-Reactor machine! That machine's data will be lost!", worldObj.isRemote?"CLIENT":"SERVER", referenceCoord);
			return;
		}
		
		MultiblockReactor otherReactor = (MultiblockReactor)otherMachine;

		// TODO FIXME: Only change heat based on relative sizes
		if(otherReactor.latentHeat > this.latentHeat) { latentHeat = otherReactor.latentHeat; }
		this.addStoredEnergy(otherReactor.getEnergyStored());
	}
	
	@Override
	public void onAttachedPartWithMultiblockData(IMultiblockPart part, NBTTagCompound data) {
		this.readFromNBT(data);
	}
	
	@Override
	public void getOrphanData(IMultiblockPart newOrphan, int oldSize, int newSize, NBTTagCompound dataContainer) {
		this.writeToNBT(dataContainer);
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
		int wasteAmt = 0;
		int freeFuelSpace = 0;
		for(TileEntityReactorControlRod controlRod : attachedControlRods) {
			if(controlRod == null || !controlRod.isConnected()) { continue; }

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
		for(TileEntityReactorAccessPort port : attachedAccessPorts) {
			if(fuelIngotsToConsume <= 0 && (wasteToDistribute == null || wasteToDistribute.stackSize <= 0)) {
				break;
			}
			
			if(port == null || !port.isConnected()) { continue; }

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
				tryDistributeWaste(port, wasteToDistribute, false);
			}
		}
		
		// If we have waste leftover and we have multiple ports, go back over them for the
		// outlets.
		if(wasteToDistribute != null && wasteToDistribute.stackSize > 0 && attachedAccessPorts.size() > 1) {
			for(TileEntityReactorAccessPort port : attachedAccessPorts) {
				if(wasteToDistribute == null || wasteToDistribute.stackSize <= 0) {
					break;
				}

				if(port == null || !port.isConnected()) { continue; }

				tryDistributeWaste(port, wasteToDistribute, true);
			}
		}
		
		// Okay... let's modify the fuel rods now
		if((wasteToDistribute != null && wasteToDistribute.stackSize != wasteAmt / 1000) || fuelIngotsConsumed > 0) {
			int fuelToDistribute = fuelIngotsConsumed * 1000;
			int wasteToConsume = 0;
			if(wasteToDistribute != null) {
				wasteToConsume = ((wasteAmt/1000) - wasteToDistribute.stackSize) * 1000;
			}
			
			for(TileEntityReactorControlRod controlRod : attachedControlRods) {
				if(wasteToConsume <= 0 && fuelToDistribute <= 0) { break; }
				if(controlRod == null || !controlRod.isConnected()) { continue; }
				
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
		// Force an update of the client's multiblock information
		worldObj.markBlockForUpdate(referenceCoord.x, referenceCoord.y, referenceCoord.z);
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
		amtFuel = amtWaste = 0;

		for(TileEntityReactorControlRod controlRod : attachedControlRods) {
			if(controlRod == null || !controlRod.isConnected()) { continue; } // Happens due to chunk unloads
		
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
		
		for(TileEntityReactorControlRod cr : attachedControlRods) {
			if(cr != null && cr.isConnected()) {
				cr.setControlRodInsertion((short)newValue);
			}
		}
	}
	
	public void changeAllControlRodInsertionValues(short delta) {
		if(this.assemblyState != AssemblyState.Assembled) { return; }
		
		for(TileEntityReactorControlRod cr : attachedControlRods) {
			if(cr != null && cr.isConnected()) {
				cr.setControlRodInsertion( (short) (cr.getControlRodInsertion() + delta) );
			}
		}
	}

	public CoordTriplet[] getControlRodLocations() {
		CoordTriplet[] coords = new CoordTriplet[this.attachedControlRods.size()];
		int i = 0;
		for(TileEntityReactorControlRod cr : attachedControlRods) {
			coords[i++] = cr.getWorldLocation();
		}
		return coords;
	}

	public int getFuelAmount() {
		if(this.assemblyState != AssemblyState.Assembled) {
			return 0;
		}
		
		int fuel = 0;
		for(TileEntityReactorControlRod cr : attachedControlRods) {
			if(cr != null && cr.isConnected()) {
				fuel += cr.getFuelAmount();
			}
		}
		
		return fuel;
	}
	
	public int getWasteAmount() {
		if(this.assemblyState != AssemblyState.Assembled) {
			return 0;
		}

		int waste = 0;
		for(TileEntityReactorControlRod cr : attachedControlRods) {
			if(cr != null && cr.isConnected()) {
				waste += cr.getWasteAmount();
			}
		}
		
		return waste;
	}

	public int getEnergyStoredPercentage() {
		return (int)(this.energyStored / (float)this.maxEnergyStored * 100f);
	}

	public int getMaxFuelAmountPerColumn() {
		return (this.getMaximumCoord().y - this.getMinimumCoord().y - 1) * TileEntityReactorControlRod.maxTotalFluidPerBlock;
	}

	@Override
	public int getCapacity() {
		return getMaxFuelAmountPerColumn() * this.getFuelColumnCount();
	}
}

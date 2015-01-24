package erogenousbeef.bigreactors.common.multiblock;

import io.netty.buffer.ByteBuf;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidBlock;
import cofh.api.energy.IEnergyProvider;
import cofh.lib.util.helpers.ItemHelper;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import erogenousbeef.bigreactors.api.IHeatEntity;
import erogenousbeef.bigreactors.api.registry.Reactants;
import erogenousbeef.bigreactors.api.registry.ReactorInterior;
import erogenousbeef.bigreactors.common.BRLog;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.data.RadiationData;
import erogenousbeef.bigreactors.common.data.StandardReactants;
import erogenousbeef.bigreactors.common.interfaces.IMultipleFluidHandler;
import erogenousbeef.bigreactors.common.interfaces.IReactorFuelInfo;
import erogenousbeef.bigreactors.common.multiblock.block.BlockReactorPart;
import erogenousbeef.bigreactors.common.multiblock.helpers.CoolantContainer;
import erogenousbeef.bigreactors.common.multiblock.helpers.FuelContainer;
import erogenousbeef.bigreactors.common.multiblock.helpers.RadiationHelper;
import erogenousbeef.bigreactors.common.multiblock.interfaces.IActivateable;
import erogenousbeef.bigreactors.common.multiblock.interfaces.ITickableMultiblockPart;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorAccessPort;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorControlRod;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorCoolantPort;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorFuelRod;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorGlass;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorPart;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorPowerTap;
import erogenousbeef.bigreactors.net.CommonPacketHandler;
import erogenousbeef.bigreactors.net.message.multiblock.ReactorUpdateMessage;
import erogenousbeef.bigreactors.net.message.multiblock.ReactorUpdateWasteEjectionMessage;
import erogenousbeef.bigreactors.utils.StaticUtils;
import erogenousbeef.core.common.CoordTriplet;
import erogenousbeef.core.multiblock.IMultiblockPart;
import erogenousbeef.core.multiblock.MultiblockControllerBase;
import erogenousbeef.core.multiblock.MultiblockValidationException;
import erogenousbeef.core.multiblock.rectangular.RectangularMultiblockControllerBase;

public class MultiblockReactor extends RectangularMultiblockControllerBase implements IEnergyProvider, IReactorFuelInfo, IMultipleFluidHandler, IActivateable {
	public static final int FuelCapacityPerFuelRod = 4 * Reactants.standardSolidReactantAmount; // 4 ingots per rod
	
	public static final int FLUID_SUPERHEATED = CoolantContainer.HOT;
	public static final int FLUID_COOLANT = CoolantContainer.COLD;
	
	private static final float passiveCoolingPowerEfficiency = 0.5f; // 50% power penalty, so this comes out as about 1/3 a basic water-cooled reactor
	private static final float passiveCoolingTransferEfficiency = 0.2f; // 20% of available heat transferred per tick when passively cooled
	private static final float reactorHeatLossConductivity = 0.001f; // circa 1RF per tick per external surface block
	
	// Game stuff - stored
	protected boolean active;
	private float reactorHeat;
	private float fuelHeat;
	private WasteEjectionSetting wasteEjection;
	private float energyStored;
	protected FuelContainer fuelContainer;
	protected RadiationHelper radiationHelper;
	protected CoolantContainer coolantContainer;

	// Game stuff - derived at runtime
	protected float fuelToReactorHeatTransferCoefficient;
	protected float reactorToCoolantSystemHeatTransferCoefficient;
	protected float reactorHeatLossCoefficient;
	
	protected Iterator<TileEntityReactorFuelRod> currentFuelRod;
	int reactorVolume;

	// UI stuff
	private float energyGeneratedLastTick;
	private float fuelConsumedLastTick;
	
	public enum WasteEjectionSetting {
		kAutomatic,					// Full auto, always remove waste
		kManual, 					// Manual, only on button press
	}
	public static final WasteEjectionSetting[] s_EjectionSettings = WasteEjectionSetting.values();
	
	// Lists of connected parts
	private Set<TileEntityReactorPowerTap> attachedPowerTaps;
	private Set<ITickableMultiblockPart> attachedTickables;

	private Set<TileEntityReactorControlRod> attachedControlRods; 	// Highest internal Y-coordinate in the fuel column
	private Set<TileEntityReactorAccessPort> attachedAccessPorts;
	private Set<TileEntityReactorPart> attachedControllers;
	
	private Set<TileEntityReactorFuelRod> attachedFuelRods;
	private Set<TileEntityReactorCoolantPort> attachedCoolantPorts;
	
	private Set<TileEntityReactorGlass> attachedGlass;

	// Updates
	private Set<EntityPlayer> updatePlayers;
	private int ticksSinceLastUpdate;
	private static final int ticksBetweenUpdates = 3;
	private static final int maxEnergyStored = 10000000;
	
	public MultiblockReactor(World world) {
		super(world);

		// Game stuff
		active = false;
		reactorHeat = 0f;
		fuelHeat = 0f;
		energyStored = 0f;
		wasteEjection = WasteEjectionSetting.kAutomatic;

		// Derived stats
		fuelToReactorHeatTransferCoefficient = 0f;
		reactorToCoolantSystemHeatTransferCoefficient = 0f;
		reactorHeatLossCoefficient = 0f;
		
		// UI and stats
		energyGeneratedLastTick = 0f;
		fuelConsumedLastTick = 0f;
		
		
		attachedPowerTaps = new HashSet<TileEntityReactorPowerTap>();
		attachedTickables = new HashSet<ITickableMultiblockPart>();
		attachedControlRods = new HashSet<TileEntityReactorControlRod>();
		attachedAccessPorts = new HashSet<TileEntityReactorAccessPort>();
		attachedControllers = new HashSet<TileEntityReactorPart>();
		attachedFuelRods = new HashSet<TileEntityReactorFuelRod>();
		attachedCoolantPorts = new HashSet<TileEntityReactorCoolantPort>();
		attachedGlass = new HashSet<TileEntityReactorGlass>();
		
		currentFuelRod = null;

		updatePlayers = new HashSet<EntityPlayer>();
		
		ticksSinceLastUpdate = 0;
		fuelContainer = new FuelContainer();
		radiationHelper = new RadiationHelper();
		coolantContainer = new CoolantContainer();
		
		reactorVolume = 0;
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
			TileEntityReactorControlRod controlRod = (TileEntityReactorControlRod)part; 
			attachedControlRods.add(controlRod);
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

		if(part instanceof ITickableMultiblockPart) {
			attachedTickables.add((ITickableMultiblockPart)part);
		}
		
		if(part instanceof TileEntityReactorFuelRod) {
			TileEntityReactorFuelRod fuelRod = (TileEntityReactorFuelRod)part;
			attachedFuelRods.add(fuelRod);

			// Reset iterator
			currentFuelRod = attachedFuelRods.iterator();

			if(worldObj.isRemote) {
				worldObj.markBlockForUpdate(fuelRod.xCoord, fuelRod.yCoord, fuelRod.zCoord);
			}
		}
		
		if(part instanceof TileEntityReactorCoolantPort) {
			attachedCoolantPorts.add((TileEntityReactorCoolantPort) part);
		}
		
		if(part instanceof TileEntityReactorGlass) {
			attachedGlass.add((TileEntityReactorGlass)part);
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

		if(part instanceof ITickableMultiblockPart) {
			attachedTickables.remove((ITickableMultiblockPart)part);
		}
		
		if(part instanceof TileEntityReactorFuelRod) {
			attachedFuelRods.remove(part);
			currentFuelRod = attachedFuelRods.iterator();
		}
		
		if(part instanceof TileEntityReactorCoolantPort) {
			attachedCoolantPorts.remove((TileEntityReactorCoolantPort)part);
		}
		
		if(part instanceof TileEntityReactorGlass) {
			attachedGlass.remove((TileEntityReactorGlass)part);
		}
	}
	
	@Override
	protected void isMachineWhole() throws MultiblockValidationException {
		// Ensure that there is at least one controller and control rod attached.
		if(attachedControlRods.size() < 1) {
			throw new MultiblockValidationException("Not enough control rods. Reactors require at least 1.");
		}
		
		if(attachedControllers.size() < 1) {
			throw new MultiblockValidationException("Not enough controllers. Reactors require at least 1.");
		}
		
		super.isMachineWhole();
	}

	@Override
	public void updateClient() {}
	
	// Update loop. Only called when the machine is assembled.
	@Override
	public boolean updateServer() {
		if(Float.isNaN(this.getReactorHeat())) {
			this.setReactorHeat(0.0f);
		}
		
		float oldHeat = this.getReactorHeat();
		float oldEnergy = this.getEnergyStored();
		energyGeneratedLastTick = 0f;
		fuelConsumedLastTick = 0f;

		float newHeat = 0f;
		
		if(getActive()) {
			// Select a control rod to radiate from. Reset the iterator and select a new Y-level if needed.
			if(!currentFuelRod.hasNext()) {
				currentFuelRod = attachedFuelRods.iterator();
			}

			// Radiate from that control rod
			TileEntityReactorFuelRod source  = currentFuelRod.next();
			TileEntityReactorControlRod sourceControlRod = (TileEntityReactorControlRod)worldObj.getTileEntity(source.xCoord, getMaximumCoord().y, source.zCoord);
			if(sourceControlRod != null)
			{
				RadiationData radData = radiationHelper.radiate(worldObj, fuelContainer, source, sourceControlRod, getFuelHeat(), getReactorHeat(), attachedControlRods.size());

				// Assimilate results of radiation
				if(radData != null) {
					addFuelHeat(radData.getFuelHeatChange(attachedFuelRods.size()));
					addReactorHeat(radData.getEnvironmentHeatChange(getReactorVolume()));
					fuelConsumedLastTick += radData.fuelUsage;
				}
			}
		}

		// Allow radiation to decay even when reactor is off.
		radiationHelper.tick(getActive());

		// If we can, poop out waste and inject new fuel.
		if(wasteEjection == WasteEjectionSetting.kAutomatic) {
			ejectWaste(false, null);
		}
		
		refuel();

		// Heat Transfer: Fuel Pool <> Reactor Environment
		float tempDiff = fuelHeat - reactorHeat;
		if(tempDiff > 0.01f) {
			float rfTransferred = tempDiff * fuelToReactorHeatTransferCoefficient;
			float fuelRf = StaticUtils.Energy.getRFFromVolumeAndTemp(attachedFuelRods.size(), fuelHeat);
			
			fuelRf -= rfTransferred;
			setFuelHeat(StaticUtils.Energy.getTempFromVolumeAndRF(attachedFuelRods.size(), fuelRf));

			// Now see how much the reactor's temp has increased
			float reactorRf = StaticUtils.Energy.getRFFromVolumeAndTemp(getReactorVolume(), getReactorHeat());
			reactorRf += rfTransferred;
			setReactorHeat(StaticUtils.Energy.getTempFromVolumeAndRF(getReactorVolume(), reactorRf));
		}

		// If we have a temperature differential between environment and coolant system, move heat between them.
		tempDiff = getReactorHeat() - getCoolantTemperature();
		if(tempDiff > 0.01f) {
			float rfTransferred = tempDiff * reactorToCoolantSystemHeatTransferCoefficient;
			float reactorRf = StaticUtils.Energy.getRFFromVolumeAndTemp(getReactorVolume(), getReactorHeat());

			if(isPassivelyCooled()) {
				rfTransferred *= passiveCoolingTransferEfficiency;
				generateEnergy(rfTransferred * passiveCoolingPowerEfficiency);
			}
			else {
				rfTransferred -= coolantContainer.onAbsorbHeat(rfTransferred);
				energyGeneratedLastTick = coolantContainer.getFluidVaporizedLastTick(); // Piggyback so we don't have useless stuff in the update packet
			}

			reactorRf -= rfTransferred;
			setReactorHeat(StaticUtils.Energy.getTempFromVolumeAndRF(getReactorVolume(), reactorRf));
		}

		// Do passive heat loss - this is always versus external environment
		tempDiff = getReactorHeat() - getPassiveCoolantTemperature();
		if(tempDiff > 0.000001f) {
			float rfLost = Math.max(1f, tempDiff * reactorHeatLossCoefficient); // Lose at least 1RF/t
			float reactorNewRf = Math.max(0f, StaticUtils.Energy.getRFFromVolumeAndTemp(getReactorVolume(), getReactorHeat()) - rfLost);
			setReactorHeat(StaticUtils.Energy.getTempFromVolumeAndRF(getReactorVolume(), reactorNewRf));
		}
		
		// Prevent cryogenics
		if(reactorHeat < 0f) { setReactorHeat(0f); }
		if(fuelHeat < 0f) { setFuelHeat(0f); }
		
		// Distribute available power
		int energyAvailable = (int)getEnergyStored();
		int energyRemaining = energyAvailable;
		if(attachedPowerTaps.size() > 0 && energyRemaining > 0) {
			// First, try to distribute fairly
			int splitEnergy = energyRemaining / attachedPowerTaps.size();
			for(TileEntityReactorPowerTap powerTap : attachedPowerTaps) {
				if(energyRemaining <= 0) { break; }
				if(!powerTap.isConnected()) { continue; }

				energyRemaining -= splitEnergy - powerTap.onProvidePower(splitEnergy);
			}

			// Next, just hose out whatever we can, if we have any left
			if(energyRemaining > 0) {
				for(TileEntityReactorPowerTap powerTap : attachedPowerTaps) {
					if(energyRemaining <= 0) { break; }
					if(!powerTap.isConnected()) { continue; }

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
		for(ITickableMultiblockPart tickable : attachedTickables) {
			tickable.onMultiblockServerTick();
		}

		if(attachedGlass.size() > 0 && fuelContainer.shouldUpdate()) {
			markReferenceCoordForUpdate();
		}
		
		return (oldHeat != this.getReactorHeat() || oldEnergy != this.getEnergyStored());
	}
	
	public void setEnergyStored(float oldEnergy) {
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
		newEnergy = newEnergy * BigReactors.powerProductionMultiplier * BigReactors.reactorPowerProductionMultiplier;
		this.energyGeneratedLastTick += newEnergy;
		this.addStoredEnergy(newEnergy);
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
	
	public void setActive(boolean act) {
		if(act == this.active) { return; }
		this.active = act;
		
		for(IMultiblockPart part : connectedParts) {
			if(this.active) { part.onMachineActivated(); }
			else { part.onMachineDeactivated(); }
		}
		
		if(worldObj.isRemote) {
			// Force controllers to re-render on client
			for(IMultiblockPart part : attachedControllers) {
				worldObj.markBlockForUpdate(part.xCoord, part.yCoord, part.zCoord);
			}
		}
		else {
			this.markReferenceCoordForUpdate();
		}
	}

	protected void addReactorHeat(float newCasingHeat) {
		if(Float.isNaN(newCasingHeat)) {
			return;
		}

		reactorHeat += newCasingHeat;
		// Clamp to zero to prevent floating point issues
		if(-0.00001f < reactorHeat && reactorHeat < 0.00001f) { reactorHeat = 0.0f; }
	}
	
	public float getReactorHeat() {
		return reactorHeat;
	}
	
	public void setReactorHeat(float newHeat) {
		if(Float.isNaN(newHeat)) {
			reactorHeat = 0.0f;
		}
		else {
			reactorHeat = newHeat;
		}
	}

	protected void addFuelHeat(float additionalHeat) {
		if(Float.isNaN(additionalHeat)) { return; }
		
		fuelHeat += additionalHeat;
		if(-0.00001f < fuelHeat & fuelHeat < 0.00001f) { fuelHeat = 0f; }
	}
	
	public float getFuelHeat() { return fuelHeat; }
	
	public void setFuelHeat(float newFuelHeat) {
		if(Float.isNaN(newFuelHeat)) { fuelHeat = 0f; }
		else { fuelHeat = newFuelHeat; }
	}
	
	public int getFuelRodCount() {
		return attachedControlRods.size();
	}

	// Static validation helpers
	// Water, air, and metal blocks
	@Override
	protected void isBlockGoodForInterior(World world, int x, int y, int z) throws MultiblockValidationException {
		if(world.isAirBlock(x, y, z)) { return; } // Air is OK

		Material material = world.getBlock(x, y, z).getMaterial();
		if(material == net.minecraft.block.material.MaterialLiquid.water) {
			return;
		}
		
		Block block = world.getBlock(x, y, z);
		if(block == Blocks.iron_block || block == Blocks.gold_block || block == Blocks.diamond_block || block == Blocks.emerald_block) {
			return;
		}
		
		// Permit registered moderator blocks
		int metadata = world.getBlockMetadata(x, y, z);

		if(ReactorInterior.getBlockData(ItemHelper.oreProxy.getOreName(new ItemStack(block, 1, metadata))) != null) {
			return;
		}

		// Permit TE fluids
		if(block != null) {
			if(block instanceof IFluidBlock) {
				Fluid fluid = ((IFluidBlock)block).getFluid();
				String fluidName = fluid.getName();
				if(ReactorInterior.getFluidData(fluidName) != null) { return; }

				throw new MultiblockValidationException(String.format("%d, %d, %d - The fluid %s is not valid for the reactor's interior", x, y, z, fluidName));
			}
			else {
				throw new MultiblockValidationException(String.format("%d, %d, %d - %s is not valid for the reactor's interior", x, y, z, block.getLocalizedName()));
			}
		}
		else {
			throw new MultiblockValidationException(String.format("%d, %d, %d - Null block found, not valid for the reactor's interior", x, y, z));
		}
	}
	
	@Override
	public void writeToNBT(NBTTagCompound data) {
		data.setBoolean("reactorActive", this.active);
		data.setFloat("heat", this.reactorHeat);
		data.setFloat("fuelHeat", fuelHeat);
		data.setFloat("storedEnergy", this.energyStored);
		data.setInteger("wasteEjection2", this.wasteEjection.ordinal());
		data.setTag("fuelContainer", fuelContainer.writeToNBT(new NBTTagCompound()));
		data.setTag("radiation", radiationHelper.writeToNBT(new NBTTagCompound()));
		data.setTag("coolantContainer", coolantContainer.writeToNBT(new NBTTagCompound()));
	}

	@Override
	public void readFromNBT(NBTTagCompound data) {
		if(data.hasKey("reactorActive")) {
			setActive(data.getBoolean("reactorActive"));
		}
		
		if(data.hasKey("heat")) {
			setReactorHeat(Math.max(getReactorHeat(), data.getFloat("heat")));
		}
		
		if(data.hasKey("storedEnergy")) {
			setEnergyStored(Math.max(getEnergyStored(), data.getFloat("storedEnergy")));
		}
		
		if(data.hasKey("wasteEjection2")) {
			this.wasteEjection = s_EjectionSettings[data.getInteger("wasteEjection2")];
		}
		
		if(data.hasKey("fuelHeat")) {
			setFuelHeat(data.getFloat("fuelHeat"));
		}
		
		if(data.hasKey("fuelContainer")) {
			fuelContainer.readFromNBT(data.getCompoundTag("fuelContainer"));
		}
		
		if(data.hasKey("radiation")) {
			radiationHelper.readFromNBT(data.getCompoundTag("radiation"));
		}
		
		if(data.hasKey("coolantContainer")) {
			coolantContainer.readFromNBT(data.getCompoundTag("coolantContainer"));
		}
	}

	@Override
	protected int getMinimumNumberOfBlocksForAssembledMachine() {
		// Hollow cube.
		return 26;
	}

	@Override
	public void formatDescriptionPacket(NBTTagCompound data) {
		writeToNBT(data);
	}

	@Override
	public void decodeDescriptionPacket(NBTTagCompound data) {
		readFromNBT(data);
		onFuelStatusChanged();
	}

	// Network & Storage methods
	/*
	 * Serialize a reactor into a given Byte buffer
	 * @param buf The byte buffer to serialize into
	 */
	public void serialize(ByteBuf buf) {
		int fuelTypeID, wasteTypeID, coolantTypeID, vaporTypeID;

		// Marshal fluid types into integers
		{
			Fluid coolantType, vaporType;
			coolantType = coolantContainer.getCoolantType();
			vaporType = coolantContainer.getVaporType();			
			coolantTypeID = coolantType == null ? -1 : coolantType.getID();
			vaporTypeID = vaporType == null ? -1 : vaporType.getID();
		}

		// Basic data
		buf.writeBoolean(active);
		buf.writeFloat(reactorHeat);
		buf.writeFloat(fuelHeat);
		buf.writeFloat(energyStored);
		buf.writeFloat(radiationHelper.getFertility());
		
		// Statistics
		buf.writeFloat(energyGeneratedLastTick);
		buf.writeFloat(fuelConsumedLastTick);
		
		// Coolant data
		buf.writeInt(coolantTypeID);
		buf.writeInt(coolantContainer.getCoolantAmount());
		buf.writeInt(vaporTypeID);
		buf.writeInt(coolantContainer.getVaporAmount());
		
		fuelContainer.serialize(buf);
	}

	/*
	 * Deserialize a reactor's data from a given Byte buffer
	 * @param buf The byte buffer containing reactor data
	 */
	public void deserialize(ByteBuf buf) {
		// Basic data
		setActive(buf.readBoolean());
		setReactorHeat(buf.readFloat());
		setFuelHeat(buf.readFloat());
		setEnergyStored(buf.readFloat());
		radiationHelper.setFertility(buf.readFloat());
		
		// Statistics
		setEnergyGeneratedLastTick(buf.readFloat());
		setFuelConsumedLastTick(buf.readFloat());
		

		// Coolant data
		int coolantTypeID = buf.readInt();
		int coolantAmt = buf.readInt();
		int vaporTypeID = buf.readInt();
		int vaporAmt = buf.readInt();

		// Fuel & waste data
		fuelContainer.deserialize(buf);

		if(coolantTypeID == -1) {
			coolantContainer.emptyCoolant();
		}
		else {
			coolantContainer.setCoolant(new FluidStack(FluidRegistry.getFluid(coolantTypeID), coolantAmt));
		}
		
		if(vaporTypeID == -1) {
			coolantContainer.emptyVapor();
		}
		else {
			coolantContainer.setVapor(new FluidStack(FluidRegistry.getFluid(vaporTypeID), vaporAmt));
		}
		
	}
	
	protected IMessage getUpdatePacket() {
        return new ReactorUpdateMessage(this);
	}
	
	/**
	 * Sends a full state update to a player.
	 */
	protected void sendIndividualUpdate(EntityPlayer player) {
		if(this.worldObj.isRemote) { return; }

        CommonPacketHandler.INSTANCE.sendTo(getUpdatePacket(), (EntityPlayerMP)player);
	}
	
	/**
	 * Send an update to any clients with GUIs open
	 */
	protected void sendTickUpdate() {
		if(this.worldObj.isRemote) { return; }
		if(this.updatePlayers.size() <= 0) { return; }

		for(EntityPlayer player : updatePlayers) {
            CommonPacketHandler.INSTANCE.sendTo(getUpdatePacket(), (EntityPlayerMP)player);
		}
	}
	
	/**
	 * Attempt to distribute a stack of ingots to a given access port, sensitive to the amount and type of ingots already in it.
	 * @param port The port to which we're distributing ingots.
	 * @param itemsToDistribute The stack of ingots to distribute. Will be modified during the operation and may be returned with stack size 0.
	 * @param distributeToInputs Should we try to send ingots to input ports?
	 * @return The number of waste items distributed, i.e. the differential in stack size for wasteToDistribute.
	 */
	private int tryDistributeItems(TileEntityReactorAccessPort port, ItemStack itemsToDistribute, boolean distributeToInputs) {
		ItemStack existingStack = port.getStackInSlot(TileEntityReactorAccessPort.SLOT_OUTLET);
		int initialWasteAmount = itemsToDistribute.stackSize;
		if(!port.isInlet() || (distributeToInputs || attachedAccessPorts.size() < 2)) {
			// Dump waste preferentially to outlets, unless we only have one access port
			if(existingStack == null) {
				if(itemsToDistribute.stackSize > port.getInventoryStackLimit()) {
					ItemStack newStack = itemsToDistribute.splitStack(port.getInventoryStackLimit());
					port.setInventorySlotContents(TileEntityReactorAccessPort.SLOT_OUTLET, newStack);
				}
				else {
					port.setInventorySlotContents(TileEntityReactorAccessPort.SLOT_OUTLET, itemsToDistribute.copy());
					itemsToDistribute.stackSize = 0;
				}
			}
			else if(existingStack.isItemEqual(itemsToDistribute)) {
				if(existingStack.stackSize + itemsToDistribute.stackSize <= existingStack.getMaxStackSize()) {
					existingStack.stackSize += itemsToDistribute.stackSize;
					itemsToDistribute.stackSize = 0;
				}
				else {
					int amt = existingStack.getMaxStackSize() - existingStack.stackSize;
					itemsToDistribute.stackSize -= existingStack.getMaxStackSize() - existingStack.stackSize;
					existingStack.stackSize += amt;
				}
			}

			port.onItemsReceived();
		}
		
		return initialWasteAmount - itemsToDistribute.stackSize;
	}

	@Override
	protected void onAssimilated(MultiblockControllerBase otherMachine) {
		this.attachedPowerTaps.clear();
		this.attachedTickables.clear();
		this.attachedAccessPorts.clear();
		this.attachedControllers.clear();
		this.attachedControlRods.clear();
		currentFuelRod = null;
	}
	
	@Override
	protected void onAssimilate(MultiblockControllerBase otherMachine) {
		if(!(otherMachine instanceof MultiblockReactor)) {
			BRLog.warning("[%s] Reactor @ %s is attempting to assimilate a non-Reactor machine! That machine's data will be lost!", worldObj.isRemote?"CLIENT":"SERVER", getReferenceCoord());
			return;
		}
		
		MultiblockReactor otherReactor = (MultiblockReactor)otherMachine;

		if(otherReactor.reactorHeat > this.reactorHeat) { setReactorHeat(otherReactor.reactorHeat); }
		if(otherReactor.fuelHeat > this.fuelHeat) { setFuelHeat(otherReactor.fuelHeat); }

		if(otherReactor.getEnergyStored() > this.getEnergyStored()) { this.setEnergyStored(otherReactor.getEnergyStored()); }

		fuelContainer.merge(otherReactor.fuelContainer);
		radiationHelper.merge(otherReactor.radiationHelper);
		coolantContainer.merge(otherReactor.coolantContainer);
	}
	
	@Override
	public void onAttachedPartWithMultiblockData(IMultiblockPart part, NBTTagCompound data) {
		this.readFromNBT(data);
	}
	
	public float getEnergyStored() {
		return energyStored;
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
				markReferenceCoordDirty();

				if(this.updatePlayers.size() > 0) {
					for(EntityPlayer player : updatePlayers) {
                        CommonPacketHandler.INSTANCE.sendTo(new ReactorUpdateWasteEjectionMessage(this), (EntityPlayerMP)player);
					}
				}
			}
		}
	}
	
	public WasteEjectionSetting getWasteEjection() {
		return this.wasteEjection;
	}

	protected void refuel() {
		// For now, we only need to check fuel ports when we have more space than can accomodate 1 ingot
		if(fuelContainer.getRemainingSpace() < Reactants.standardSolidReactantAmount) {
			return;
		}
		
		int amtAdded = 0;
		
		// Loop: Consume input reactants from all ports
		for(TileEntityReactorAccessPort port : attachedAccessPorts)
		{
			if(fuelContainer.getRemainingSpace() <= 0) { break; }

			if(!port.isConnected())	{ continue; }

			// See what type of reactant the port contains; if none, skip it.
			String portReactantType = port.getInputReactantType();
			int portReactantAmount = port.getInputReactantAmount();
			if(portReactantType == null || portReactantAmount <= 0) { continue; }
			
			if(!Reactants.isFuel(portReactantType)) { continue; } // Skip nonfuels

			// HACK; TEMPORARY
			// Alias blutonium to yellorium temporarily, until mixed fuels are implemented
			if(portReactantType.equals(StandardReactants.blutonium)) {
				portReactantType = StandardReactants.yellorium;
			}
			
			// How much fuel can we actually add from this type of reactant?
			int amountToAdd = fuelContainer.addFuel(portReactantType, portReactantAmount, false);
			if(amountToAdd <= 0) { continue; }
			
			int portCanAdd = port.consumeReactantItem(amountToAdd);
			if(portCanAdd <= 0) { continue; }
			
			amtAdded = fuelContainer.addFuel(portReactantType, portReactantAmount, true);
		}
		
		if(amtAdded > 0) {
			markReferenceCoordForUpdate();
			markReferenceCoordDirty();
		}
	}
	
	/**
	 * Attempt to eject waste contained in the reactor
	 * @param dumpAll If true, any waste remaining after ejection will be discarded.
	 * @param destination If set, waste will only be ejected to ports with coordinates matching this one.
	 */
	public void ejectWaste(boolean dumpAll, CoordTriplet destination)
	{
		// For now, we can optimize by only running this when we have enough waste to product an ingot
		int amtEjected = 0;

		String wasteReactantType = fuelContainer.getWasteType();
		if(wasteReactantType == null) { 
			return;
		}

		int minimumReactantAmount = Reactants.getMinimumReactantToProduceSolid(wasteReactantType);
		if(fuelContainer.getWasteAmount() >= minimumReactantAmount) {

			for(TileEntityReactorAccessPort port : attachedAccessPorts) {
				if(fuelContainer.getWasteAmount() < minimumReactantAmount) {
					continue;
				}
				
				if(!port.isConnected()) { continue; }
				if(destination != null && !destination.equals(port.xCoord, port.yCoord, port.zCoord)) {
					continue;
				}
				
				// First time through, we eject only to outlet ports
				if(destination == null && !port.isInlet()) {
					int reactantEjected = port.emitReactant(wasteReactantType, fuelContainer.getWasteAmount());
					fuelContainer.dumpWaste(reactantEjected);
					amtEjected += reactantEjected;
				}
			}
			
			if(destination == null && fuelContainer.getWasteAmount() > minimumReactantAmount) {
				// Loop a second time when destination is null and we still have waste
				for(TileEntityReactorAccessPort port : attachedAccessPorts) {
					if(fuelContainer.getWasteAmount() < minimumReactantAmount) {
						continue;
					}
					
					if(!port.isConnected()) { continue; }
					int reactantEjected = port.emitReactant(wasteReactantType, fuelContainer.getWasteAmount());
					fuelContainer.dumpWaste(reactantEjected);
					amtEjected += reactantEjected;
				}
			}
		}

		if(dumpAll)
		{
			amtEjected += fuelContainer.getWasteAmount();
			fuelContainer.setWaste(null);
		}

		if(amtEjected > 0) {
			markReferenceCoordForUpdate();
			markReferenceCoordDirty();
		}
	}
	
	/**
	 * Eject fuel contained in the reactor.
	 * @param dumpAll If true, any remaining fuel will simply be lost.
	 * @param destination If not null, then fuel will only be distributed to a port matching these coordinates.
	 */
	public void ejectFuel(boolean dumpAll, CoordTriplet destination) {
		// For now, we can optimize by only running this when we have enough waste to product an ingot
		int amtEjected = 0;

		String fuelReactantType = fuelContainer.getFuelType();
		if(fuelReactantType == null) { 
			return;
		}

		int minimumReactantAmount = Reactants.getMinimumReactantToProduceSolid(fuelReactantType);
		if(fuelContainer.getFuelAmount() >= minimumReactantAmount) {

			for(TileEntityReactorAccessPort port : attachedAccessPorts) {
				if(fuelContainer.getFuelAmount() < minimumReactantAmount) {
					continue;
				}
				
				if(!port.isConnected()) { continue; }
				if(destination != null && !destination.equals(port.xCoord, port.yCoord, port.zCoord)) {
					continue;
				}
				
				int reactantEjected = port.emitReactant(fuelReactantType, fuelContainer.getFuelAmount());
				fuelContainer.dumpFuel(reactantEjected);
				amtEjected += reactantEjected;
			}
		}

		if(dumpAll)
		{
			amtEjected += fuelContainer.getFuelAmount();
			fuelContainer.setFuel(null);
		}

		if(amtEjected > 0) {
			markReferenceCoordForUpdate();
			markReferenceCoordDirty();
		}
	}

	@Override
	protected void onMachineAssembled() {
		recalculateDerivedValues();
	}

	@Override
	protected void onMachineRestored() {
		recalculateDerivedValues();
	}

	@Override
	protected void onMachinePaused() {
	}

	@Override
	protected void onMachineDisassembled() {
		this.active = false;
	}

	private void recalculateDerivedValues() {
		// Recalculate size of fuel/waste tank via fuel rods
		CoordTriplet minCoord, maxCoord;
		minCoord = getMinimumCoord();
		maxCoord = getMaximumCoord();
		
		fuelContainer.setCapacity(attachedFuelRods.size() * FuelCapacityPerFuelRod);

		// Calculate derived stats
		
		// Calculate heat transfer based on fuel rod environment
		fuelToReactorHeatTransferCoefficient = 0f;
		for(TileEntityReactorFuelRod fuelRod : attachedFuelRods) {
			fuelToReactorHeatTransferCoefficient += fuelRod.getHeatTransferRate();
		}

		// Calculate heat transfer to coolant system based on reactor interior surface area.
		// This is pretty simple to start with - surface area of the rectangular prism defining the interior.
		int xSize = maxCoord.x - minCoord.x - 1;
		int ySize = maxCoord.y - minCoord.y - 1;
		int zSize = maxCoord.z - minCoord.z - 1;
		
		int surfaceArea = 2 * (xSize * ySize + xSize * zSize + ySize * zSize);
		
		reactorToCoolantSystemHeatTransferCoefficient = IHeatEntity.conductivityIron * surfaceArea;

		// Calculate passive heat loss.
		// Get external surface area
		xSize += 2;
		ySize += 2;
		zSize += 2;
		
		surfaceArea = 2 * (xSize * ySize + xSize * zSize + ySize * zSize);
		reactorHeatLossCoefficient = reactorHeatLossConductivity * surfaceArea;
		
		if(worldObj.isRemote) {
			// Make sure our fuel rods re-render
			this.onFuelStatusChanged();
		}
		else {
			// Force an update of the client's multiblock information
			markReferenceCoordForUpdate();
		}
		
		calculateReactorVolume();
		
		if(attachedCoolantPorts.size() > 0) {
			int outerVolume = StaticUtils.ExtraMath.Volume(minCoord, maxCoord) - reactorVolume;
			coolantContainer.setCapacity(Math.max(0, Math.min(50000, outerVolume * 100)));
		}
		else {
			coolantContainer.setCapacity(0);
		}
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
	public void setFuelConsumedLastTick(float fuelConsumed) {
		fuelConsumedLastTick = fuelConsumed;
	}
	
	/**
	 * UI Helper
	 */
	public float getFuelConsumedLastTick() {
		return fuelConsumedLastTick;
	}

	/**
	 * UI Helper
	 * @return Percentile fuel richness (fuel/fuel+waste), or 0 if all control rods are empty
	 */
	public float getFuelRichness() {
		int amtFuel, amtWaste;
		amtFuel = fuelContainer.getFuelAmount();
		amtWaste = fuelContainer.getWasteAmount();

		if(amtFuel + amtWaste <= 0f) { return 0f; }
		else { return (float)amtFuel / (float)(amtFuel+amtWaste); }
	}

	// IEnergyProvider
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
	public boolean canConnectEnergy(ForgeDirection from) {
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

	// Redstone helper
	public void setAllControlRodInsertionValues(int newValue) {
		if(this.assemblyState != AssemblyState.Assembled) { return; }
		
		for(TileEntityReactorControlRod cr : attachedControlRods) {
			if(cr.isConnected()) {
				cr.setControlRodInsertion((short)newValue);
			}
		}
	}
	
	public void changeAllControlRodInsertionValues(short delta) {
		if(this.assemblyState != AssemblyState.Assembled) { return; }
		
		for(TileEntityReactorControlRod cr : attachedControlRods) {
			if(cr.isConnected()) {
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
		return fuelContainer.getFuelAmount();
	}

	public int getWasteAmount() {
		return fuelContainer.getWasteAmount();
	}
	
	public String getFuelType() {
		return fuelContainer.getFuelType();
	}
	
	public String getWasteType() {
		return fuelContainer.getWasteType();
	}

	public int getEnergyStoredPercentage() {
		return (int)(this.energyStored / (float)this.maxEnergyStored * 100f);
	}

	@Override
	public int getCapacity() {
		if(worldObj.isRemote && assemblyState != AssemblyState.Assembled) {
			// Estimate capacity
			return attachedFuelRods.size() * FuelCapacityPerFuelRod;
		}

		return fuelContainer.getCapacity();
	}
	
	public float getFuelFertility() {
		return radiationHelper.getFertilityModifier();
	}
	
	// Coolant subsystem
	public CoolantContainer getCoolantContainer() {
		return coolantContainer;
	}
	
	protected float getPassiveCoolantTemperature() {
		return IHeatEntity.ambientHeat;
	}

	protected float getCoolantTemperature() {
		if(isPassivelyCooled()) {
			return getPassiveCoolantTemperature();
		}
		else {
			return coolantContainer.getCoolantTemperature(getReactorHeat());
		}
	}
	
	public boolean isPassivelyCooled() {
		if(coolantContainer == null || coolantContainer.getCapacity() <= 0) { return true; }
		else { return false; }
	}
	
	protected int getReactorVolume() {
		return reactorVolume;
	}
	
	protected void calculateReactorVolume() {
		CoordTriplet minInteriorCoord = getMinimumCoord();
		minInteriorCoord.x += 1;
		minInteriorCoord.y += 1;
		minInteriorCoord.z += 1;
		
		CoordTriplet maxInteriorCoord = getMaximumCoord();
		maxInteriorCoord.x -= 1;
		maxInteriorCoord.y -= 1;
		maxInteriorCoord.z -= 1;
		
		reactorVolume = StaticUtils.ExtraMath.Volume(minInteriorCoord, maxInteriorCoord);
	}

	// Client-only
	protected void onFuelStatusChanged() {
		if(worldObj.isRemote) {
			// On the client, re-render all the fuel rod blocks when the fuel status changes
			for(TileEntityReactorFuelRod fuelRod : attachedFuelRods) {
				worldObj.markBlockForUpdate(fuelRod.xCoord, fuelRod.yCoord, fuelRod.zCoord);
			}
		}
	}

	private static final FluidTankInfo[] emptyTankInfo = new FluidTankInfo[0];
	
	@Override
	public FluidTankInfo[] getTankInfo() {
		if(isPassivelyCooled()) { return emptyTankInfo; }
		
		return coolantContainer.getTankInfo(-1);
	}
	
	@Override
	public boolean getActive() {
		return this.active;
	}
	
	public String getDebugInfo() {
		StringBuilder sb = new StringBuilder();
		sb.append("Assembled: ").append(Boolean.toString(isAssembled())).append("\n");
		sb.append("Attached Blocks: ").append(Integer.toString(connectedParts.size())).append("\n");
		if(getLastValidationException() != null) {
			sb.append("Validation Exception:\n").append(getLastValidationException().getMessage()).append("\n");
		}
		
		if(isAssembled()) {
			sb.append("\nActive: ").append(Boolean.toString(getActive()));
			sb.append("\nStored Energy: ").append(Float.toString(getEnergyStored()));
			sb.append("\nCasing Heat: ").append(Float.toString(getReactorHeat()));
			sb.append("\nFuel Heat: ").append(Float.toString(getFuelHeat()));
			sb.append("\n\nReactant Tanks:\n");
			sb.append( fuelContainer.getDebugInfo() );
			sb.append("\n\nActively Cooled: ").append(Boolean.toString(!isPassivelyCooled()));
			if(!isPassivelyCooled()) {
				sb.append("\n\nCoolant Tanks:\n");
				sb.append( coolantContainer.getDebugInfo() );
			}
		}

		return sb.toString();
	}
}

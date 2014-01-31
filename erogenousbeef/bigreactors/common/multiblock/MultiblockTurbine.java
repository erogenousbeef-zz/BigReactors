package erogenousbeef.bigreactors.common.multiblock;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.oredict.OreDictionary;
import cofh.api.energy.IEnergyHandler;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.interfaces.IMultipleFluidHandler;
import erogenousbeef.bigreactors.common.multiblock.block.BlockTurbinePart;
import erogenousbeef.bigreactors.common.multiblock.block.BlockTurbineRotorPart;
import erogenousbeef.bigreactors.common.multiblock.interfaces.ITickableMultiblockPart;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityTurbinePartBase;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityTurbinePowerTap;
import erogenousbeef.bigreactors.gui.container.ISlotlessUpdater;
import erogenousbeef.bigreactors.net.PacketWrapper;
import erogenousbeef.bigreactors.net.Packets;
import erogenousbeef.bigreactors.utils.StaticUtils;
import erogenousbeef.core.common.CoordTriplet;
import erogenousbeef.core.multiblock.IMultiblockPart;
import erogenousbeef.core.multiblock.MultiblockControllerBase;
import erogenousbeef.core.multiblock.MultiblockValidationException;
import erogenousbeef.core.multiblock.rectangular.RectangularMultiblockControllerBase;

public class MultiblockTurbine extends RectangularMultiblockControllerBase implements IEnergyHandler, IMultipleFluidHandler, ISlotlessUpdater {

	public enum VentStatus {
		VentOverflow,
		VentAll,
		DoNotVent
	};
	
	// UI updates
	private Set<EntityPlayer> updatePlayers;
	private int ticksSinceLastUpdate;
	private static final int ticksBetweenUpdates = 3;

	// Fluid tanks. Input = Steam, Output = Water.
	public static final int TANK_INPUT = 0;
	public static final int TANK_OUTPUT = 1;
	public static final int NUM_TANKS = 2;
	public static final int FLUID_NONE = -1;
	private FluidTank[] tanks;
	
	static final float maxEnergyStored = 1000000f; // 1 MegaRF
	
	// Persistent game data
	float energyStored;
	boolean active;
	float rotorSpeed;
	
	// Player settings
	VentStatus ventStatus;
	
	// Derivable game data
	int bladeSurfaceArea; // # of blocks that are blades
	int rotorMass; // 10 = 1 standard block-weight
	int coilSize;  // number of blocks in the coils
	
	// Inductor dynamic constants - get from a table on assembly
	float inductorDragCoefficient = 0.01f; // Keep this small, as it gets multiplied by v^2 and dominates at high speeds. Higher = more drag from the inductor vs. aerodynamic drag = more efficient energy conversion.
	float inductionEnergyBonus = 1f; // Bonus to energy generation based on construction materials. 1 = plain iron.
	float inductionEnergyExponentBonus = 0f; // Exponential bonus to energy generation. Use this for very rare materials or special constructs.

	// TODO: Calculate this on assembly. Better blades should be lighter and have less drag.
	// Rotor dynamic constants - calculate on assembly
	float rotorDragCoefficient = 0.1f; // totally arbitrary. Allow upgrades to decrease this. This is the drag of the ROTOR.
	float bladeDragCoefficient = 0.04f; // From wikipedia, drag of a standard airfoil. This is the drag of the BLADES.
	// Penalize suboptimal shapes with worse drag (i.e. increased drag without increasing lift)
	// Suboptimal is defined as "not a christmas-tree shape". At worst, drag is increased 4x.
	
	// Game balance constants
	final static float bladeLiftCoefficient = 0.75f; // From wikipedia, lift of a standard airfoil 
	final static float inductionEnergyCoefficient = 1.2f; // Power to raise the induced current by. This makes higher RPMs more efficient.
	
	float energyGeneratedLastTick;
	
	private Set<IMultiblockPart> attachedControllers;
	private Set<IMultiblockPart> attachedRotorBearings;
	
	private Set<TileEntityTurbinePowerTap> attachedPowerTaps;
	private Set<ITickableMultiblockPart> attachedTickables;
	
	// Data caches for validation
	private Set<CoordTriplet> foundCoils;
	private Set<CoordTriplet> foundRotors;
	private Set<CoordTriplet> foundBlades;

	private static final ForgeDirection[] RotorXBladeDirections = new ForgeDirection[] { ForgeDirection.UP, ForgeDirection.SOUTH, ForgeDirection.DOWN, ForgeDirection.NORTH };
	private static final ForgeDirection[] RotorZBladeDirections = new ForgeDirection[] { ForgeDirection.UP, ForgeDirection.EAST, ForgeDirection.DOWN, ForgeDirection.WEST };

	public MultiblockTurbine(World world) {
		super(world);

		updatePlayers = new HashSet<EntityPlayer>();
		
		ticksSinceLastUpdate = 0;
		
		tanks = new FluidTank[NUM_TANKS];
		for(int i = 0; i < NUM_TANKS; i++)
			tanks[i] = new FluidTank(1000);
		
		attachedControllers = new HashSet<IMultiblockPart>();
		attachedRotorBearings = new HashSet<IMultiblockPart>();
		attachedPowerTaps = new HashSet<TileEntityTurbinePowerTap>();
		attachedTickables = new HashSet<ITickableMultiblockPart>();
		
		energyStored = 0f;
		active = false;
		ventStatus = VentStatus.VentOverflow;
		rotorSpeed = 0f;
		
		bladeSurfaceArea = 0;
		rotorMass = 0;
		coilSize = 0;
		energyGeneratedLastTick = 0f;
		
		foundCoils = new HashSet<CoordTriplet>();
		foundRotors = new HashSet<CoordTriplet>();
		foundBlades = new HashSet<CoordTriplet>();
	}

	/**
	 * Sends a full state update to a player.
	 */
	protected void sendIndividualUpdate(EntityPlayer player) {
		if(this.worldObj.isRemote) { return; }

		PacketDispatcher.sendPacketToPlayer(getUpdatePacket(), (Player)player);
	}

	protected Packet getUpdatePacket() {
		// Capture compacted fluid data first
		int inputFluidID, inputFluidAmt, outputFluidID, outputFluidAmt;
		
		FluidStack inputFluid, outputFluid;
		inputFluid = tanks[TANK_INPUT].getFluid();
		outputFluid = tanks[TANK_OUTPUT].getFluid();
		
		if(inputFluid == null || inputFluid.amount <= 0) {
			inputFluidID = FLUID_NONE;
			inputFluidAmt = 0;
		}
		else {
			inputFluidID = inputFluid.getFluid().getID();
			inputFluidAmt = inputFluid.amount;
		}
		
		if(outputFluid == null || outputFluid.amount <= 0) {
			outputFluidID = FLUID_NONE;
			outputFluidAmt = 0;
		}
		else {
			outputFluidID = outputFluid.getFluid().getID();
			outputFluidAmt = outputFluid.amount;
		}

		CoordTriplet referenceCoord = getReferenceCoord();
		return PacketWrapper.createPacket(BigReactors.CHANNEL,
				 Packets.MultiblockTurbineFullUpdate,
				 new Object[] { referenceCoord.x,
								referenceCoord.y,
								referenceCoord.z, 
								inputFluidID,
								inputFluidAmt,
								outputFluidID,
								outputFluidAmt,
								energyStored,
								rotorSpeed,
								energyGeneratedLastTick
		});
	}

	/**
	 * Parses a full-update packet. Only used on the client.
	 * @param data The data input stream containing the update data.
	 * @throws IOException
	 */
	public void onReceiveUpdatePacket(DataInputStream data) throws IOException {
		int inputFluidID = data.readInt();
		int inputFluidAmt = data.readInt();
		int outputFluidID = data.readInt();
		int outputFluidAmt = data.readInt();
		energyStored = data.readFloat();
		rotorSpeed = data.readFloat();
		energyGeneratedLastTick = data.readFloat();
		
		if(inputFluidID == FLUID_NONE || inputFluidAmt <= 0) {
			tanks[TANK_INPUT].setFluid(null);
		}
		else {
			Fluid fluid = FluidRegistry.getFluid(inputFluidID);
			if(fluid == null) {
				FMLLog.warning("[CLIENT] Multiblock Turbine received an unknown fluid of type %d, setting input tank to empty", inputFluidID);
				tanks[TANK_INPUT].setFluid(null);
			}
			else {
				tanks[TANK_INPUT].setFluid(new FluidStack(fluid, inputFluidAmt));
			}
		}

		if(outputFluidID == FLUID_NONE || outputFluidAmt <= 0) {
			tanks[TANK_OUTPUT].setFluid(null);
		}
		else {
			Fluid fluid = FluidRegistry.getFluid(outputFluidID);
			if(fluid == null) {
				FMLLog.warning("[CLIENT] Multiblock Turbine received an unknown fluid of type %d, setting output tank to empty", outputFluidID);
				tanks[TANK_OUTPUT].setFluid(null);
			}
			else {
				tanks[TANK_OUTPUT].setFluid(new FluidStack(fluid, outputFluidAmt));
			}
		}
	}

	/**
	 * Send an update to any clients with GUIs open
	 */
	protected void sendTickUpdate() {
		if(this.updatePlayers.size() <= 0) { return; }
		
		Packet data = getUpdatePacket();

		for(EntityPlayer player : updatePlayers) {
			PacketDispatcher.sendPacketToPlayer(data, (Player)player);
		}
	}

	// MultiblockControllerBase overrides

	@Override
	public void onAttachedPartWithMultiblockData(IMultiblockPart part, NBTTagCompound data) {
		// TODO: Additional validation to only replace current data if current data is "worse" than NBT data
		readFromNBT(data);
	}

	@Override
	protected void onBlockAdded(IMultiblockPart newPart) {
		if(newPart instanceof TileEntityTurbinePartBase) {
			CoordTriplet coord = newPart.getWorldLocation();
			int metadata = worldObj.getBlockMetadata(coord.x, coord.y, coord.z);
			if(metadata == BlockTurbinePart.METADATA_BEARING) {
				this.attachedRotorBearings.add(newPart);
			}
		}
		
		if(newPart instanceof TileEntityTurbinePowerTap) {
			attachedPowerTaps.add((TileEntityTurbinePowerTap)newPart);
		}
		
		if(newPart instanceof ITickableMultiblockPart) {
			attachedTickables.add((ITickableMultiblockPart)newPart);
		}
	}

	@Override
	protected void onBlockRemoved(IMultiblockPart oldPart) {
		this.attachedRotorBearings.remove(oldPart);
		
		if(oldPart instanceof TileEntityTurbinePowerTap) {
			attachedPowerTaps.remove((TileEntityTurbinePowerTap)oldPart);
		}

		if(oldPart instanceof ITickableMultiblockPart) {
			attachedTickables.remove((ITickableMultiblockPart)oldPart);
		}
	}

	@Override
	protected void onMachineAssembled() {
		recalculateDerivedStatistics();
	}

	@Override
	protected void onMachineRestored() {
		recalculateDerivedStatistics();
	}

	@Override
	protected void onMachinePaused() {
	}

	@Override
	protected void onMachineDisassembled() {
		rotorMass = 0;
		bladeSurfaceArea = 0;
		coilSize = 0;
	}

	// Validation code
	@Override
	protected void isMachineWhole() throws MultiblockValidationException {
		if(attachedRotorBearings.size() != 1) {
			throw new MultiblockValidationException("Turbines require exactly 1 rotor bearing");
		}
		
		// Set up validation caches
		foundBlades.clear();
		foundRotors.clear();
		foundCoils.clear();
		
		super.isMachineWhole();
		
		// Now do additional validation based on the coils/blades/rotors that were found
		
		// Check that we have a rotor that goes all the way up the bearing
		TileEntityTurbinePartBase rotorPart = (TileEntityTurbinePartBase)attachedRotorBearings.iterator().next();
		
		// Rotor bearing must calculate outwards dir, as this is normally only calculated in onMachineAssembled().
		rotorPart.recalculateOutwardsDirection(getMinimumCoord(), getMaximumCoord());
		
		// Find out which way the rotor runs. Obv, this is inwards from the bearing.
		ForgeDirection rotorDir = rotorPart.getOutwardsDir().getOpposite();
		CoordTriplet rotorCoord = rotorPart.getWorldLocation();
		
		CoordTriplet minRotorCoord = getMinimumCoord();
		CoordTriplet maxRotorCoord = getMaximumCoord();
		
		// Constrain min/max rotor coords to where the rotor bearing is and the block opposite it
		if(rotorDir.offsetX == 0) {
			minRotorCoord.x = maxRotorCoord.x = rotorCoord.x;
		}
		if(rotorDir.offsetY == 0) {
			minRotorCoord.y = maxRotorCoord.y = rotorCoord.y;
		}
		if(rotorDir.offsetZ == 0) {
			minRotorCoord.z = maxRotorCoord.z = rotorCoord.z;
		}

		// Figure out where the rotor ends and which directions are normal to the rotor's 4 faces (this is where blades emit from)
		CoordTriplet endRotorCoord = rotorCoord.equals(minRotorCoord) ? maxRotorCoord : minRotorCoord;
		endRotorCoord.translate(rotorDir.getOpposite());

		ForgeDirection[] bladeDirections;
		if(rotorDir.offsetY != 0) { 
			bladeDirections = StaticUtils.CardinalDirections;
		}
		else if(rotorDir.offsetX != 0) {
			bladeDirections = RotorXBladeDirections;
		}
		else {
			bladeDirections = RotorZBladeDirections;
		}

		// Move along the length of the rotor, 1 block at a time
		boolean encounteredCoils = false;
		while(!foundRotors.isEmpty() && !rotorCoord.equals(endRotorCoord)) {
			rotorCoord.translate(rotorDir);
			
			// Ensure we find a rotor block along the length of the entire rotor
			if(!foundRotors.remove(rotorCoord)) {
				throw new MultiblockValidationException(String.format("%s - This block must contain a rotor. The rotor must begin at the bearing and run the entire length of the turbine", rotorCoord));
			}
			
			// Now move out in the 4 rotor normals, looking for blades and coils
			CoordTriplet checkCoord = rotorCoord.copy();
			boolean encounteredBlades = false;
			for(ForgeDirection bladeDir : bladeDirections) {
				checkCoord.copy(rotorCoord);
				boolean foundABlade = false;
				checkCoord.translate(bladeDir);
				
				// If we find 1 blade, we can keep moving along the normal to find more blades
				while(foundBlades.remove(checkCoord)) {
					// We found a coil already?! NOT ALLOWED.
					if(encounteredCoils) {
						throw new MultiblockValidationException(String.format("%s - Rotor blades must be placed closer to the rotor bearing than all other parts inside a turbine", checkCoord));
					}
					foundABlade = encounteredBlades = true;
					checkCoord.translate(bladeDir);
				}

				// If this block wasn't a blade, check to see if it was a coil
				if(!foundABlade) {
					if(foundCoils.remove(checkCoord)) {
						encounteredCoils = true;

						// We cannot have blades and coils intermix. This prevents intermixing, depending on eval order.
						if(encounteredBlades) {
							throw new MultiblockValidationException(String.format("%s - Metal blocks must by placed further from the rotor bearing than all rotor blades", checkCoord));
						}
						
						// Check the two coil spots in the 'corners', which are permitted if they're connected to the main rotor coil somehow
						CoordTriplet coilCheck = checkCoord.copy();
						coilCheck.translate(bladeDir.getRotation(rotorDir));
						foundCoils.remove(coilCheck);
						coilCheck.copy(checkCoord);
						coilCheck.translate(bladeDir.getRotation(rotorDir.getOpposite()));
						foundCoils.remove(coilCheck);
					}
					// Else: It must have been air.
				}
			}
		}
		
		if(!rotorCoord.equals(endRotorCoord)) {
			throw new MultiblockValidationException("The rotor shaft must extend the entire length of the turbine interior.");
		}
		
		// Ensure that we encountered all the rotor, blade and coil blocks. If not, there's loose stuff inside the turbine.
		if(!foundRotors.isEmpty()) {
			throw new MultiblockValidationException(String.format("Found %d rotor blocks that are not attached to the main rotor. All rotor blocks must be in a column extending the entire length of the turbine, starting from the bearing.", foundRotors.size()));
		}

		if(!foundBlades.isEmpty()) {
			throw new MultiblockValidationException(String.format("Found %d rotor blades that are not attached to the rotor. All rotor blades must extend continuously from the rotor's shaft.", foundBlades.size()));
		}
		
		if(!foundCoils.isEmpty()) {
			throw new MultiblockValidationException(String.format("Found %d metal blocks which were not in a ring around the rotor. All metal blocks must be in rings, or partial rings, around the rotor.", foundCoils.size()));
		}

		// A-OK!
	}
	
	@Override
	protected void isBlockGoodForInterior(World world, int x, int y, int z) throws MultiblockValidationException {
		// We only allow air and functional parts in turbines.

		// Air is ok
		if(world.isAirBlock(x, y, z)) { return; }

		int blockId = world.getBlockId(x, y, z);
		int metadata = world.getBlockMetadata(x,y,z);

		// Allow rotors and stuff
		if(blockId == BigReactors.blockTurbineRotorPart.blockID) { 
			if(BlockTurbineRotorPart.isRotorBlade(metadata)) {
				foundBlades.add(new CoordTriplet(x,y,z));
			}
			else if(BlockTurbineRotorPart.isRotorShaft(metadata)) {
				foundRotors.add(new CoordTriplet(x,y,z));
			}
			else {
				throw new MultiblockValidationException("%d, %d, %d - Found an unrecognized turbine rotor part. This is a bug, please report it.");
			}
			return;
		}

		// Coil windings below here:
		if(isBlockPartOfCoil(x, y, z, blockId, metadata)) { 
			foundCoils.add(new CoordTriplet(x,y,z));
			return;
		}

		// Everything else, gtfo
		throw new MultiblockValidationException(String.format("%d, %d, %d is invalid for a turbine interior. Only rotor parts, metal blocks and empty space are allowed.", x, y, z));
	}

	@Override
	protected int getMinimumNumberOfBlocksForAssembledMachine() {
		// Hollow 5x5x4 cube (100 - 18), interior minimum is 3x3x2
		return 82;
	}

	@Override
	protected int getMaximumXSize() {
		return BigReactors.maximumTurbineSize;
	}

	@Override
	protected int getMaximumZSize() {
		return BigReactors.maximumTurbineSize;
	}

	@Override
	protected int getMaximumYSize() {
		return BigReactors.maximumTurbineHeight;
	}
	
	@Override
	protected int getMinimumXSize() { return 5; }

	@Override
	protected int getMinimumYSize() { return 4; }

	@Override
	protected int getMinimumZSize() { return 5; }
	
	
	@Override
	protected void onAssimilate(MultiblockControllerBase assimilated) {
	}

	@Override
	protected void onAssimilated(MultiblockControllerBase assimilator) {
		attachedControllers.clear();
		attachedRotorBearings.clear();
		attachedTickables.clear();
		attachedPowerTaps.clear();
	}

	@Override
	protected boolean updateServer() {
		energyGeneratedLastTick = 0f;
		
		// Generate energy based on steam
		int steamIn = 0; // mB. Based on water, actually. Probably higher for steam. Measure it.
		float fluidEnergyDensity = 0.001f;

		if(isActive()) {
			// TODO: Lookup fluid parameters from a table
			fluidEnergyDensity = 0.001f; // effectively, force-units per mB. (one mB-force or mBf). Stand-in for fluid density.

			// Spin up via steam inputs, convert some steam back into water
			steamIn = tanks[TANK_INPUT].getFluidAmount();
			
			if(ventStatus == VentStatus.DoNotVent) {
				// Cap steam used to available space, if not venting
				// TODO: Calculate difference from available space
				int availableSpace = tanks[TANK_OUTPUT].getCapacity() - tanks[TANK_OUTPUT].getFluidAmount();
				steamIn = Math.max(steamIn, availableSpace);
			}
		}
		
		if(steamIn > 0 || rotorSpeed > 0) {
			// Induction-driven torque pulled out of my ass.
			float inductionTorque = (float)(Math.pow(rotorSpeed*0.1f, 1.5)*inductorDragCoefficient*coilSize);

			// Aerodynamic drag equation. Thanks, Mr. Euler.
			float aerodynamicDragTorque = (float)Math.pow(rotorSpeed, 2) * fluidEnergyDensity * bladeDragCoefficient * bladeSurfaceArea / 2f;

			// Frictional drag equation. Basically, a small amount of constant drag based on the size of your rotor.
			float frictionalDragTorque = rotorDragCoefficient * rotorMass;
			
			// Aerodynamic lift equation. Also via Herr Euler.
			float liftTorque = 2 * (float)Math.pow(steamIn, 2) * fluidEnergyDensity * bladeLiftCoefficient * bladeSurfaceArea;

			// Yay for derivation. We're assuming delta-Time is always 1, as we're always calculating for 1 tick.
			// TODO: When calculating rotor mass, factor in a division by two to eliminate the constant term.
			float deltaV = (2 * (liftTorque + -1f*inductionTorque + -1f*aerodynamicDragTorque + -1f*frictionalDragTorque)) / rotorMass;

			float energyToGenerate = (float)Math.pow(inductionTorque, inductionEnergyCoefficient + inductionEnergyExponentBonus) * inductionEnergyBonus * BigReactors.powerProductionMultiplier;
			generateEnergy(energyToGenerate);

			rotorSpeed += deltaV;
			if(rotorSpeed < 0f) { rotorSpeed = 0f; }
			
			// And create some water
			if(steamIn > 0) {
				Fluid effluent = FluidRegistry.WATER;
				FluidStack effluentStack = new FluidStack(effluent, steamIn);
				fill(TANK_OUTPUT, effluentStack, true);
				drain(TANK_INPUT, steamIn, true);
			}
		}
		
		int energyAvailable = (int)getEnergyStored();
		int energyRemaining = energyAvailable;
		if(energyStored > 0 && attachedPowerTaps.size() > 0) {
			// First, try to distribute fairly
			int splitEnergy = energyRemaining / attachedPowerTaps.size();
			for(TileEntityTurbinePowerTap powerTap : attachedPowerTaps) {
				if(energyRemaining <= 0) { break; }
				if(powerTap == null || !powerTap.isConnected()) { continue; }

				energyRemaining -= splitEnergy - powerTap.onProvidePower(splitEnergy);
			}

			// Next, just hose out whatever we can, if we have any left
			if(energyRemaining > 0) {
				for(TileEntityTurbinePowerTap powerTap : attachedPowerTaps) {
					if(energyRemaining <= 0) { break; }
					if(powerTap == null || !powerTap.isConnected()) { continue; }

					energyRemaining = powerTap.onProvidePower(energyRemaining);
				}
			}
		}
		
		if(energyAvailable != energyRemaining) {
			reduceStoredEnergy((energyAvailable - energyRemaining));
		}
		
		for(ITickableMultiblockPart part : attachedTickables) {
			part.onMultiblockServerTick();
		}
		
		ticksSinceLastUpdate++;
		if(ticksSinceLastUpdate >= ticksBetweenUpdates) {
			sendTickUpdate();
			ticksSinceLastUpdate = 0;
		}
		
		return false;
	}

	@Override
	protected void updateClient() {
	}

	@Override
	public void writeToNBT(NBTTagCompound data) {
		data.setCompoundTag("inputTank", tanks[TANK_INPUT].writeToNBT(new NBTTagCompound()));
		data.setCompoundTag("outputTank", tanks[TANK_OUTPUT].writeToNBT(new NBTTagCompound()));
		data.setBoolean("active", active);
		data.setFloat("energy", energyStored);
		data.setInteger("ventStatus", ventStatus.ordinal());
		data.setFloat("rotorSpeed", rotorSpeed);
	}

	@Override
	public void readFromNBT(NBTTagCompound data) {
		if(data.hasKey("inputTank")) {
			tanks[TANK_INPUT].readFromNBT(data.getCompoundTag("inputTank"));
		}
		
		if(data.hasKey("outputTank")) {
			tanks[TANK_OUTPUT].readFromNBT(data.getCompoundTag("outputTank"));
		}
		
		if(data.hasKey("active")) {
			active = data.getBoolean("active");
		}
		
		if(data.hasKey("energy")) {
			energyStored = data.getFloat("energy");
		}
		
		if(data.hasKey("ventStatus")) {
			ventStatus = VentStatus.values()[data.getInteger("ventStatus")];
		}
		
		if(data.hasKey("rotorSpeed")) {
			rotorSpeed = data.getFloat("rotorSpeed");
			if(Float.isNaN(rotorSpeed)) { rotorSpeed = 0f; }
		}
	}

	@Override
	public void formatDescriptionPacket(NBTTagCompound data) {
		writeToNBT(data);
	}

	@Override
	public void decodeDescriptionPacket(NBTTagCompound data) {
		readFromNBT(data);
	}

	@Override
	public void getOrphanData(IMultiblockPart newOrphan, int oldSize, int newSize, NBTTagCompound dataContainer) {
		writeToNBT(dataContainer);
	}

	// Nondirectional FluidHandler implementation, similar to IFluidHandler

	public int fill(int tank, FluidStack resource, boolean doFill) {
		if(!canFill(tank, resource.getFluid())) {
			return 0;
		}
		
		return tanks[tank].fill(resource, doFill);
	}

	public FluidStack drain(int tank, FluidStack resource, boolean doDrain) {
		if(canDrain(tank, resource.getFluid())) {
			return tanks[tank].drain(resource.amount, doDrain);
		}
		
		return null;
	}

	public FluidStack drain(int tank, int maxDrain, boolean doDrain) {
		if(tank < 0 || tank >= NUM_TANKS) { return null; }
		
		return tanks[tank].drain(maxDrain, doDrain);
	}

	public boolean canFill(int tank, Fluid fluid) {
		if(tank < 0 || tank >= NUM_TANKS) { return false; }
		
		FluidStack fluidStack = tanks[tank].getFluid();
		if(fluidStack != null) {
			return fluidStack.getFluid().getID() == fluid.getID();
		}
		else if(tank == TANK_INPUT) {
			// TODO: Input tank can only be filled with compatible fluids from a registry
			return fluid.getName().equals("steam");
		}
		else {
			// Output tank can be filled with anything. Don't be a dumb.
			return true;
		}
	}

	public boolean canDrain(int tank, Fluid fluid) {
		if(tank < 0 || tank >= NUM_TANKS) { return false; }
		FluidStack fluidStack = tanks[tank].getFluid();
		if(fluidStack == null) {
			return false;
		}
		
		return fluidStack.getFluid().getID() == fluid.getID();
	}

	public FluidTankInfo[] getTankInfo() {
		FluidTankInfo[] infos = new FluidTankInfo[NUM_TANKS];
		for(int i = 0; i < NUM_TANKS; i++) {
			infos[i] = tanks[i].getInfo();
		}

		return infos;
	}
	
	public FluidTankInfo getTankInfo(int tankIdx) {
		return tanks[tankIdx].getInfo();
	}

	// IEnergyHandler

	@Override
	public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate) {
		// haha no
		return 0;
	}

	@Override
	public int extractEnergy(ForgeDirection from, int maxExtract, boolean simulate) {
		int energyExtracted = Math.min((int)energyStored, maxExtract);
		
		if(!simulate) {
			energyStored -= energyExtracted;
		}
		
		return energyExtracted;
	}

	@Override
	public boolean canInterface(ForgeDirection from) {
		return true;
	}

	@Override
	public int getEnergyStored(ForgeDirection from) {
		return (int)energyStored;
	}

	@Override
	public int getMaxEnergyStored(ForgeDirection from) {
		return (int)maxEnergyStored;
	}
	
	// Energy Helpers
	public float getEnergyStored() {
		return energyStored;
	}
	
	/**
	 * Remove some energy from the internal storage buffer.
	 * Will not reduce the buffer below 0.
	 * @param energy Amount by which the buffer should be reduced.
	 */
	protected void reduceStoredEnergy(float energy) {
		addStoredEnergy(-1f * energy);
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
		energyGeneratedLastTick += newEnergy * BigReactors.powerProductionMultiplier;
		addStoredEnergy(newEnergy * BigReactors.powerProductionMultiplier);
	}
	
	// Activity state
	public boolean isActive() {
		return active;
	}

	public void setActive(boolean newValue) {
		if(newValue != active) {
			this.active = newValue;
			for(IMultiblockPart part : connectedParts) {
				if(this.active) { part.onMachineActivated(); }
				else { part.onMachineDeactivated(); }
			}
			
			CoordTriplet referenceCoord = getReferenceCoord();
			worldObj.markBlockForUpdate(referenceCoord.x, referenceCoord.y, referenceCoord.z);
		}
	}

	// ISlotlessUpdater
	@Override
	public void beginUpdatingPlayer(EntityPlayer playerToUpdate) {
		updatePlayers.add(playerToUpdate);
		sendIndividualUpdate(playerToUpdate);
	}
	
	@Override
	public void stopUpdatingPlayer(EntityPlayer playerToRemove) {
		updatePlayers.remove(playerToRemove);
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
		// TODO DISTANCE CHECK
		return true;
	}
	
	private boolean isBlockPartOfCoil(int x, int y, int z) {
		return isBlockPartOfCoil(x, y, z, worldObj.getBlockId(x,y,z), worldObj.getBlockMetadata(x, y, z));
	}
	
	private boolean isBlockPartOfCoil(int x, int y, int z, int blockID, int metadata) {
		// Allow vanilla iron and gold blocks
		if(blockID == Block.blockGold.blockID || blockID == Block.blockIron.blockID) { return true; }
		
		// Check the oredict to see if it's copper, or a funky kind of gold/iron block
		int oreId = OreDictionary.getOreID(new ItemStack(blockID, 1, metadata));

		// Not oredicted? Buzz off.
		if(oreId < 0) { return false; }
		
		String oreName = OreDictionary.getOreName(oreId);
		if(oreName.equals("blockCopper") || oreName.equals("blockIron") || oreName.equals("blockGold")) { return true; }
		
		return false;
	}
	
	/**
	 * Recalculate rotor and coil parameters
	 */
	private void recalculateDerivedStatistics() {
		CoordTriplet minInterior, maxInterior;
		minInterior = getMinimumCoord();
		maxInterior = getMaximumCoord();
		minInterior.x++; minInterior.y++; minInterior.z++;
		maxInterior.x++; maxInterior.y++; maxInterior.z++;
		
		rotorMass = 0;
		bladeSurfaceArea = 0;
		coilSize = 0;

		// Loop over interior space. Calculate mass and blade area of rotor and size of coils
		for(int x = minInterior.x; x <= maxInterior.x; x++) {
			for(int y = minInterior.y; y <= maxInterior.y; y++) {
				for(int z = minInterior.z; z <= maxInterior.z; z++) {
					int blockId = worldObj.getBlockId(x, y, z);
					int metadata = worldObj.getBlockMetadata(x, y, z);

					if(blockId == BigReactors.blockTurbineRotorPart.blockID) {
						rotorMass += BigReactors.blockTurbineRotorPart.getRotorMass(blockId, metadata);
						if(BlockTurbineRotorPart.isRotorBlade(metadata)) {
							bladeSurfaceArea += 1;
						}
					}
					
					if(isBlockPartOfCoil(x, y, z, blockId, metadata)) {
						coilSize += 1;
					}
				} // end z
			} // end y
		} // end x loop - looping over interior
	}
	
	public float getRotorSpeed() { return rotorSpeed; }
	public float getEnergyGeneratedLastTick() { return energyGeneratedLastTick; }

	public float getMaxRotorSpeed() {
		return 2000f;
	}
}

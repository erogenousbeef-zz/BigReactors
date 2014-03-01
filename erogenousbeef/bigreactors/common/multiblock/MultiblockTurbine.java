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
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import erogenousbeef.bigreactors.common.BRLog;
import erogenousbeef.bigreactors.common.BRRegistry;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.interfaces.IMultipleFluidHandler;
import erogenousbeef.bigreactors.common.multiblock.block.BlockTurbinePart;
import erogenousbeef.bigreactors.common.multiblock.block.BlockTurbineRotorPart;
import erogenousbeef.bigreactors.common.multiblock.helpers.CoilPartData;
import erogenousbeef.bigreactors.common.multiblock.helpers.FloatUpdateTracker;
import erogenousbeef.bigreactors.common.multiblock.interfaces.ITickableMultiblockPart;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityTurbinePartBase;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityTurbinePartGlass;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityTurbinePowerTap;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityTurbineRotorPart;
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
	public static final int TANK_SIZE = 2000;
	private FluidTank[] tanks;
	
	static final float maxEnergyStored = 1000000f; // 1 MegaRF
	
	// Persistent game data
	float energyStored;
	boolean active;
	float rotorEnergy;
	
	// Player settings
	VentStatus ventStatus;
	int maxIntakeRate;
	
	// Derivable game data
	int bladeSurfaceArea; // # of blocks that are blades
	int rotorMass; // 10 = 1 standard block-weight
	int coilSize;  // number of blocks in the coils
	
	// Inductor dynamic constants - get from a table on assembly
	float inductorDragCoefficient = inductorBaseDragCoefficient;
	float inductionEfficiency = 0.5f; // Final energy rectification efficiency. Averaged based on coil material and shape. 0.25-0.5 = iron, 0.75-0.9 = diamond, 1 = perfect.
	float inductionEnergyExponentBonus = 1f; // Exponential bonus to energy generation. Use this for very rare materials or special constructs.

	// Rotor dynamic constants - calculate on assembly
	float rotorDragCoefficient = 0.01f; // RF/t lost to friction per unit of mass in the rotor.
	float bladeDragCoefficient = 0.00025f; // RF/t lost to friction per blade block, multiplied by rotor speed squared. - includes a 50% reduction to factor in constant parts of the drag equation
	float frictionalDrag	   = 0f;

	// Penalize suboptimal shapes with worse drag (i.e. increased drag without increasing lift)
	// Suboptimal is defined as "not a christmas-tree shape". At worst, drag is increased 4x.
	
	// Game balance constants
	public final static int inputFluidPerBlade = 25; // mB
	public static final float inductorBaseDragCoefficient = 0.1f; // RF/t extracted per coil block, multiplied by rotor speed squared.
	
	float energyGeneratedLastTick;
	int fluidConsumedLastTick;
	float rotorEfficiencyLastTick;
	
	private Set<IMultiblockPart> attachedControllers;
	private Set<TileEntityTurbinePartBase> attachedRotorBearings;
	
	private Set<TileEntityTurbinePowerTap> attachedPowerTaps;
	private Set<ITickableMultiblockPart> attachedTickables;
	
	private Set<TileEntityTurbineRotorPart> attachedRotorShafts;
	private Set<TileEntityTurbineRotorPart> attachedRotorBlades;
	
	private Set<TileEntityTurbinePartGlass> attachedGlass; 
	
	// Data caches for validation
	private Set<CoordTriplet> foundCoils;

	private FloatUpdateTracker rpmUpdateTracker;
	
	private static final ForgeDirection[] RotorXBladeDirections = new ForgeDirection[] { ForgeDirection.UP, ForgeDirection.SOUTH, ForgeDirection.DOWN, ForgeDirection.NORTH };
	private static final ForgeDirection[] RotorZBladeDirections = new ForgeDirection[] { ForgeDirection.UP, ForgeDirection.EAST, ForgeDirection.DOWN, ForgeDirection.WEST };
	
	public MultiblockTurbine(World world) {
		super(world);

		updatePlayers = new HashSet<EntityPlayer>();
		
		ticksSinceLastUpdate = 0;
		
		tanks = new FluidTank[NUM_TANKS];
		for(int i = 0; i < NUM_TANKS; i++)
			tanks[i] = new FluidTank(TANK_SIZE);
		
		attachedControllers = new HashSet<IMultiblockPart>();
		attachedRotorBearings = new HashSet<TileEntityTurbinePartBase>();
		attachedPowerTaps = new HashSet<TileEntityTurbinePowerTap>();
		attachedTickables = new HashSet<ITickableMultiblockPart>();
		attachedRotorShafts = new HashSet<TileEntityTurbineRotorPart>();
		attachedRotorBlades = new HashSet<TileEntityTurbineRotorPart>();
		attachedGlass = new HashSet<TileEntityTurbinePartGlass>();
		
		energyStored = 0f;
		active = false;
		ventStatus = VentStatus.VentOverflow;
		rotorEnergy = 0f;
		maxIntakeRate = TANK_SIZE;
		
		bladeSurfaceArea = 0;
		rotorMass = 0;
		coilSize = 0;
		energyGeneratedLastTick = 0f;
		fluidConsumedLastTick = 0;
		rotorEfficiencyLastTick = 1f;
		
		foundCoils = new HashSet<CoordTriplet>();
		
		rpmUpdateTracker = new FloatUpdateTracker(100, 5, 10f, 100f); // Minimum 10RPM difference for slow updates, if change > 100 RPM, update every 5 ticks
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
								rotorEnergy,
								energyGeneratedLastTick,
								maxIntakeRate,
								active,
								ventStatus.ordinal(),
								fluidConsumedLastTick,
								rotorEfficiencyLastTick
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
		rotorEnergy = data.readFloat();
		energyGeneratedLastTick = data.readFloat();
		maxIntakeRate = data.readInt();
		setActive(data.readBoolean());
		ventStatus = VentStatus.values()[data.readInt()];
		fluidConsumedLastTick = data.readInt();
		rotorEfficiencyLastTick = data.readFloat();

		if(inputFluidID == FLUID_NONE || inputFluidAmt <= 0) {
			tanks[TANK_INPUT].setFluid(null);
		}
		else {
			Fluid fluid = FluidRegistry.getFluid(inputFluidID);
			if(fluid == null) {
				BRLog.warning("[CLIENT] Multiblock Turbine received an unknown fluid of type %d, setting input tank to empty", inputFluidID);
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
				BRLog.warning("[CLIENT] Multiblock Turbine received an unknown fluid of type %d, setting output tank to empty", outputFluidID);
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

	public void onNetworkPacket(int packetType, DataInputStream data) throws IOException {
		// Client->Server Packets
		if(packetType == Packets.MultiblockActivateButton) {
			boolean nowActive = data.readBoolean();
			setActive(nowActive);
		}

		if(packetType == Packets.MultiblockTurbineGovernorUpdate) {
			setMaxIntakeRate(data.readInt());
		}
		
		if(packetType == Packets.MultiblockTurbineVentUpdate) {
			int idx = data.readInt();
			if(idx >= 0 && idx < VentStatus.values().length) {
				ventStatus = VentStatus.values()[idx];
				markReferenceCoordDirty();
			}
		}

		// Server->Client Packets
		if(packetType == Packets.MultiblockTurbineFullUpdate) {
			onReceiveUpdatePacket(data);
		}
		
		// Bidirectional packets
	}
	
	// MultiblockControllerBase overrides

	@Override
	public void onAttachedPartWithMultiblockData(IMultiblockPart part, NBTTagCompound data) {
		readFromNBT(data);
	}

	@Override
	protected void onBlockAdded(IMultiblockPart newPart) {
		if(newPart instanceof TileEntityTurbinePartBase) {
			CoordTriplet coord = newPart.getWorldLocation();
			int metadata = worldObj.getBlockMetadata(coord.x, coord.y, coord.z);
			if(metadata == BlockTurbinePart.METADATA_BEARING) {
				this.attachedRotorBearings.add((TileEntityTurbinePartBase)newPart);
			}
		}
		
		if(newPart instanceof TileEntityTurbinePowerTap) {
			attachedPowerTaps.add((TileEntityTurbinePowerTap)newPart);
		}
		
		if(newPart instanceof ITickableMultiblockPart) {
			attachedTickables.add((ITickableMultiblockPart)newPart);
		}
		
		if(newPart instanceof TileEntityTurbineRotorPart) {
			TileEntityTurbineRotorPart turbinePart = (TileEntityTurbineRotorPart)newPart;
			if(turbinePart.isRotorShaft()) {
				attachedRotorShafts.add(turbinePart);
			}
			
			if(turbinePart.isRotorBlade()) {
				attachedRotorBlades.add(turbinePart);
			}
		}
		
		if(newPart instanceof TileEntityTurbinePartGlass) {
			attachedGlass.add((TileEntityTurbinePartGlass)newPart);
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
		
		if(oldPart instanceof TileEntityTurbineRotorPart) {
			TileEntityTurbineRotorPart turbinePart = (TileEntityTurbineRotorPart)oldPart;
			if(turbinePart.isRotorShaft()) {
				attachedRotorShafts.remove(turbinePart);
			}
			
			if(turbinePart.isRotorBlade()) {
				attachedRotorBlades.remove(turbinePart);
			}
		}
		
		if(oldPart instanceof TileEntityTurbinePartGlass) {
			attachedGlass.remove((TileEntityTurbinePartGlass)oldPart);
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
		
		rotorEnergy = 0f; // Kill energy when machines get broken by players/explosions
		rpmUpdateTracker.setValue(0f);
	}

	// Validation code
	@Override
	protected void isMachineWhole() throws MultiblockValidationException {
		if(attachedRotorBearings.size() != 1) {
			throw new MultiblockValidationException("Turbines require exactly 1 rotor bearing");
		}
		
		// Set up validation caches
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

		Set<CoordTriplet> rotorShafts = new HashSet<CoordTriplet>(attachedRotorShafts.size());
		Set<CoordTriplet> rotorBlades = new HashSet<CoordTriplet>(attachedRotorBlades.size());
		
		for(TileEntityTurbineRotorPart part : attachedRotorShafts) {
			rotorShafts.add(part.getWorldLocation());
		}

		for(TileEntityTurbineRotorPart part : attachedRotorBlades) {
			rotorBlades.add(part.getWorldLocation());
		}
		
		// Move along the length of the rotor, 1 block at a time
		boolean encounteredCoils = false;
		while(!rotorShafts.isEmpty() && !rotorCoord.equals(endRotorCoord)) {
			rotorCoord.translate(rotorDir);
			
			// Ensure we find a rotor block along the length of the entire rotor
			if(!rotorShafts.remove(rotorCoord)) {
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
				while(rotorBlades.remove(checkCoord)) {
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
		if(!rotorShafts.isEmpty()) {
			throw new MultiblockValidationException(String.format("Found %d rotor blocks that are not attached to the main rotor. All rotor blocks must be in a column extending the entire length of the turbine, starting from the bearing.", rotorShafts.size()));
		}

		if(!rotorBlades.isEmpty()) {
			throw new MultiblockValidationException(String.format("Found %d rotor blades that are not attached to the rotor. All rotor blades must extend continuously from the rotor's shaft.", rotorBlades.size()));
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

		// Coil windings below here:
		if(getCoilPartData(x, y, z, blockId, metadata) != null) { 
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
	protected void onAssimilate(MultiblockControllerBase otherMachine) {
		if(!(otherMachine instanceof MultiblockTurbine)) {
			BRLog.warning("[%s] Turbine @ %s is attempting to assimilate a non-Turbine machine! That machine's data will be lost!", worldObj.isRemote?"CLIENT":"SERVER", getReferenceCoord());
			return;
		}
		
		MultiblockTurbine otherTurbine = (MultiblockTurbine)otherMachine;
		
		rotorEnergy = Math.max(rotorEnergy, otherTurbine.rotorEnergy);
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
		fluidConsumedLastTick = 0;
		rotorEfficiencyLastTick = 1f;
		
		// Generate energy based on steam
		int steamIn = 0; // mB. Based on water, actually. Probably higher for steam. Measure it.

		if(isActive()) {
			// Spin up via steam inputs, convert some steam back into water.
			// Use at most the user-configured max, or the amount in the tank, whichever is less.
			steamIn = Math.min(maxIntakeRate, tanks[TANK_INPUT].getFluidAmount());
			
			if(ventStatus == VentStatus.DoNotVent) {
				// Cap steam used to available space, if not venting
				int availableSpace = tanks[TANK_OUTPUT].getCapacity() - tanks[TANK_OUTPUT].getFluidAmount();
				steamIn = Math.min(steamIn, availableSpace);
			}
		}
		
		if(steamIn > 0 || rotorEnergy > 0) {
			float rotorSpeed = getRotorSpeed();

			// RFs lost to aerodynamic drag.
			float aerodynamicDragTorque = (float)rotorSpeed * bladeDragCoefficient * bladeSurfaceArea;

			float liftTorque = 0f;
			if(steamIn > 0) {
				// TODO: Lookup fluid parameters from a table
				float fluidEnergyDensity = 10f; // RF per mB

				// Cap amount of steam we can fully extract energy from based on blade size
				int steamToProcess = bladeSurfaceArea * inputFluidPerBlade;
				steamToProcess = Math.min(steamToProcess, steamIn);
				liftTorque = steamToProcess * fluidEnergyDensity;

				// Did we have excess steam for our blade size?
				if(steamToProcess < steamIn) {
					// Extract some percentage of the remaining steam's energy, based on how many blades are missing
					steamToProcess = steamIn - steamToProcess;
					float bladeEfficiency = 1f;
					int neededBlades = steamIn / inputFluidPerBlade; // round in the player's favor
					int missingBlades = neededBlades - bladeSurfaceArea;
					bladeEfficiency = 1f - (float)missingBlades / (float)neededBlades;
					liftTorque += steamToProcess * fluidEnergyDensity * bladeEfficiency;

					rotorEfficiencyLastTick = liftTorque / (steamIn * fluidEnergyDensity);
				}
			}

			// Yay for derivation. We're assuming delta-Time is always 1, as we're always calculating for 1 tick.
			// RFs available to coils
			float inductionTorque = rotorSpeed * inductorDragCoefficient * coilSize;
			float energyToGenerate = (float)Math.pow(inductionTorque, inductionEnergyExponentBonus) * inductionEfficiency;
			if(energyToGenerate > 0f) {
				// Efficiency curve. Rotors are 50% less efficient when not near 900/1800 RPMs.
				float efficiency = (float)(0.25*Math.cos(rotorSpeed/(45.5*Math.PI))) + 0.75f;
				if(rotorSpeed < 500) {
					efficiency = Math.min(0.5f, efficiency);
				}

				generateEnergy(energyToGenerate * efficiency);
			}

			rotorEnergy += liftTorque + -1f*inductionTorque + -1f*aerodynamicDragTorque + -1f*frictionalDrag;
			if(rotorEnergy < 0f) { rotorEnergy = 0f; }
			
			// And create some water
			if(steamIn > 0) {
				fluidConsumedLastTick = steamIn;
				drain(TANK_INPUT, steamIn, true);
				
				if(ventStatus != VentStatus.VentAll) {
					Fluid effluent = FluidRegistry.WATER;
					FluidStack effluentStack = new FluidStack(effluent, steamIn);
					fill(TANK_OUTPUT, effluentStack, true);
				}
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
		
		if(rpmUpdateTracker.shouldUpdate(getRotorSpeed())) {
			markReferenceCoordDirty();
		}

		return energyGeneratedLastTick > 0 || fluidConsumedLastTick > 0;
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
		data.setFloat("rotorEnergy", rotorEnergy);
		data.setInteger("maxIntakeRate", maxIntakeRate);
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
			setActive(data.getBoolean("active"));
		}
		
		if(data.hasKey("energy")) {
			setEnergyStored(data.getFloat("energy"));
		}
		
		if(data.hasKey("ventStatus")) {
			ventStatus = VentStatus.values()[data.getInteger("ventStatus")];
		}
		
		if(data.hasKey("rotorEnergy")) {
			rotorEnergy = data.getFloat("rotorEnergy");
			if(Float.isNaN(rotorEnergy) || Float.isInfinite(rotorEnergy)) { rotorEnergy = 0f; }
			
			if(!worldObj.isRemote) {
				rpmUpdateTracker.setValue(getRotorSpeed());
			}
		}
		
		if(data.hasKey("maxIntakeRate")) {
			maxIntakeRate = data.getInteger("maxIntakeRate");
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

	private void setEnergyStored(float newEnergy) {
		if(Float.isInfinite(newEnergy) || Float.isNaN(newEnergy)) { return; }

		energyStored = Math.max(0f, Math.min(maxEnergyStored, newEnergy));
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

			markReferenceCoordDirty();
		}
		
		if(worldObj.isRemote) {
			// Force controllers to re-render on client
			for(IMultiblockPart part : attachedControllers) {
				worldObj.markBlockForUpdate(part.xCoord, part.yCoord, part.zCoord);
			}
			
			for(TileEntityTurbineRotorPart part : attachedRotorBlades) {
				worldObj.markBlockForUpdate(part.xCoord, part.yCoord, part.zCoord);
			}
			
			for(TileEntityTurbineRotorPart part : attachedRotorShafts) {
				worldObj.markBlockForUpdate(part.xCoord, part.yCoord, part.zCoord);
			}
		}
	}

	// Governor
	public int getMaxIntakeRate() { return maxIntakeRate; }

	public void setMaxIntakeRate(int newRate) {
		maxIntakeRate = Math.min(TANK_SIZE, Math.max(0, newRate));
		markReferenceCoordDirty();
	}
	
	// for GUI use
	public int getMaxIntakeRateMax() { return TANK_SIZE; }
	
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
		return true;
	}
	
	private CoilPartData getCoilPartData(int x, int y, int z) {
		return getCoilPartData(x, y, z, worldObj.getBlockId(x,y,z), worldObj.getBlockMetadata(x, y, z));
	}
	
	private CoilPartData getCoilPartData(int x, int y, int z, int blockID, int metadata) {
		// Allow vanilla iron and gold blocks
		if(blockID == Block.blockIron.blockID) { return BRRegistry.getCoilPartData("blockIron"); }
		if(blockID == Block.blockGold.blockID) { return BRRegistry.getCoilPartData("blockGold"); }
		
		// Check the oredict to see if it's copper, or a funky kind of gold/iron block
		int oreId = OreDictionary.getOreID(new ItemStack(blockID, 1, metadata));

		// Not oredicted? Buzz off.
		if(oreId < 0) { return null; }
		
		// TODO: Registry lookup
		String oreName = OreDictionary.getOreName(oreId);
		return BRRegistry.getCoilPartData(oreName);
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
		float coilEfficiency = 0f;
		float coilBonus = 0f;
		float coilDragCoefficient = 0f;

		// Loop over interior space. Calculate mass and blade area of rotor and size of coils
		for(int x = minInterior.x; x <= maxInterior.x; x++) {
			for(int y = minInterior.y; y <= maxInterior.y; y++) {
				for(int z = minInterior.z; z <= maxInterior.z; z++) {
					int blockId = worldObj.getBlockId(x, y, z);
					int metadata = worldObj.getBlockMetadata(x, y, z);
					CoilPartData coilData = null;

					if(blockId == BigReactors.blockTurbineRotorPart.blockID) {
						rotorMass += BigReactors.blockTurbineRotorPart.getRotorMass(blockId, metadata);
						if(BlockTurbineRotorPart.isRotorBlade(metadata)) {
							bladeSurfaceArea += 1;
						}
					}
					
					coilData = getCoilPartData(x, y, z, blockId, metadata); 
					if(coilData != null) {
						coilEfficiency += coilData.efficiency;
						coilBonus += coilData.bonus;
						coilDragCoefficient += coilData.energyExtractionRate;
						coilSize += 1;
					}
				} // end z
			} // end y
		} // end x loop - looping over interior
		
		inductionEfficiency = (coilEfficiency * 0.33f) / coilSize;
		inductionEnergyExponentBonus = Math.max(1f, (coilBonus / coilSize));
		frictionalDrag = rotorMass * rotorDragCoefficient;
		inductorDragCoefficient = (coilDragCoefficient / coilSize) * inductorBaseDragCoefficient;
	}
	
	public float getRotorSpeed() {
		if(attachedRotorBlades.size() <= 0 || rotorMass <= 0) { return 0f; }
		return rotorEnergy / (attachedRotorBlades.size() * rotorMass);
	}

	public float getEnergyGeneratedLastTick() { return energyGeneratedLastTick; }
	public int   getFluidConsumedLastTick() { return fluidConsumedLastTick; }
	public int	 getNumRotorBlades() { return attachedRotorBlades.size(); }
	public float getRotorEfficiencyLastTick() { return rotorEfficiencyLastTick; }

	public float getMaxRotorSpeed() {
		return 2000f;
	}
	
	public int getRotorMass() {
		return rotorMass;
	}
	
	public VentStatus getVentSetting() {
		return ventStatus;
	}
	
	protected void markReferenceCoordDirty() {
		if(worldObj == null || worldObj.isRemote) { return; }

		CoordTriplet referenceCoord = getReferenceCoord();
		if(referenceCoord == null) { return; }

		rpmUpdateTracker.onExternalUpdate();
		
		TileEntity saveTe = worldObj.getBlockTileEntity(referenceCoord.x, referenceCoord.y, referenceCoord.z);
		worldObj.markTileEntityChunkModified(referenceCoord.x, referenceCoord.y, referenceCoord.z, saveTe);
		worldObj.markBlockForUpdate(referenceCoord.x, referenceCoord.y, referenceCoord.z);
	}
	
	// For client usage only
	public ForgeDirection getRotorDirection() {
		if(attachedRotorBearings.size() < 1) {
			return ForgeDirection.UNKNOWN;
		}
		
		if(!this.isAssembled()) {
			return ForgeDirection.UNKNOWN;
		}
		
		TileEntityTurbinePartBase rotorBearing = attachedRotorBearings.iterator().next();
		return rotorBearing.getOutwardsDir().getOpposite();
	}
	
	public boolean hasGlass() { return attachedGlass.size() > 0; }
}

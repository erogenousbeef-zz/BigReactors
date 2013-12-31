package erogenousbeef.bigreactors.common.multiblock;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.net.PacketWrapper;
import erogenousbeef.bigreactors.net.Packets;
import erogenousbeef.core.multiblock.IMultiblockPart;
import erogenousbeef.core.multiblock.MultiblockControllerBase;

public class MultiblockTurbine extends MultiblockControllerBase {

	private Set<EntityPlayer> updatePlayers;
	private int ticksSinceLastUpdate;
	private static final int ticksBetweenUpdates = 3;

	// Fluid tanks. Input = Steam, Output = Water.
	public static final int TANK_INPUT = 0;
	public static final int TANK_OUTPUT = 1;
	public static final int NUM_TANKS = 2;
	public static final int FLUID_NONE = -1;
	private FluidTank[] tanks;
	
	private Set<IMultiblockPart> attachedControllers;
	
	public MultiblockTurbine(World world) {
		super(world);

		updatePlayers = new HashSet<EntityPlayer>();
		
		ticksSinceLastUpdate = 0;
		
		tanks = new FluidTank[NUM_TANKS];
		for(int i = 0; i < NUM_TANKS; i++)
			tanks[i] = new FluidTank(0);
		
		attachedControllers = new HashSet<IMultiblockPart>();
	}

	// GUI Updates
	public void beginUpdatingPlayer(EntityPlayer playerToUpdate) {
		updatePlayers.add(playerToUpdate);
		sendIndividualUpdate(playerToUpdate);
	}
	
	public void stopUpdatingPlayer(EntityPlayer playerToRemove) {
		updatePlayers.remove(playerToRemove);
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
		
		// TODO: Write a handler for this
		return PacketWrapper.createPacket(BigReactors.CHANNEL,
				 Packets.MultiblockTurbineFullUpdate,
				 new Object[] { referenceCoord.x,
								referenceCoord.y,
								referenceCoord.z, 
								inputFluidID,
								inputFluidAmt,
								outputFluidID,
								outputFluidAmt
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
			tanks[TANK_INPUT].setFluid(null);
		}
		else {
			Fluid fluid = FluidRegistry.getFluid(outputFluidID);
			if(fluid == null) {
				FMLLog.warning("[CLIENT] Multiblock Turbine received an unknown fluid of type %d, setting output tank to empty", outputFluidID);
				tanks[TANK_INPUT].setFluid(null);
			}
			else {
				tanks[TANK_INPUT].setFluid(new FluidStack(fluid, outputFluidAmt));
			}
		}
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

	// MultiblockControllerBase overrides

	@Override
	public void onAttachedPartWithMultiblockData(IMultiblockPart part, NBTTagCompound data) {
		// TODO: Additional validation to only replace current data if current data is "worse" than NBT data
		readFromNBT(data);
	}

	@Override
	protected void onBlockAdded(IMultiblockPart newPart) {
		// TODO
	}

	@Override
	protected void onBlockRemoved(IMultiblockPart oldPart) {
		// TODO
	}

	@Override
	protected void onMachineAssembled() {
		// TODO FIXME
		int machineSize = 1;
		for(int i = 0; i < NUM_TANKS; i++) {
			tanks[i].setCapacity(machineSize * 100);
		}
	}

	@Override
	protected void onMachineRestored() {
	}

	@Override
	protected void onMachinePaused() {
	}

	@Override
	protected void onMachineDisassembled() {
	}

	// Validation code
	@Override
	protected boolean isBlockGoodForInterior(World world, int x, int y, int z) {
		// We only allow air and functional parts in turbines.
		
		return true; // TODO FIXME world.isAirBlock(x, y, z);
	}

	@Override
	protected int getMinimumNumberOfBlocksForAssembledMachine() {
		// Hollow 5x5x5 cube (125 - 64)
		return 61;
	}

	@Override
	protected int getMaximumXSize() {
		// TODO: Configurable
		return 16;
	}

	@Override
	protected int getMaximumZSize() {
		// TODO: Configurable
		return 16;
	}

	@Override
	protected int getMaximumYSize() {
		// TODO: Configurable
		return 32;
	}
	
	@Override
	protected void onAssimilate(MultiblockControllerBase assimilated) {
		// TODO Auto-generated method stub
	}

	@Override
	protected void onAssimilated(MultiblockControllerBase assimilator) {
		// TODO Auto-generated method stub
	}

	@Override
	protected boolean updateServer() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void updateClient() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writeToNBT(NBTTagCompound data) {
		data.setCompoundTag("inputTank", tanks[TANK_INPUT].writeToNBT(new NBTTagCompound()));
		data.setCompoundTag("outputTank", tanks[TANK_OUTPUT].writeToNBT(new NBTTagCompound()));
	}

	@Override
	public void readFromNBT(NBTTagCompound data) {
		if(data.hasKey("inputTank")) {
			tanks[TANK_INPUT].readFromNBT(data.getCompoundTag("inputTank"));
		}
		
		if(data.hasKey("outputTank")) {
			tanks[TANK_OUTPUT].readFromNBT(data.getCompoundTag("outputTank"));
		}
	}

	@Override
	public void formatDescriptionPacket(NBTTagCompound data) {
		data.setCompoundTag("inputTank", tanks[TANK_INPUT].writeToNBT(new NBTTagCompound()));
		data.setCompoundTag("outputTank", tanks[TANK_OUTPUT].writeToNBT(new NBTTagCompound()));
	}

	@Override
	public void decodeDescriptionPacket(NBTTagCompound data) {
		if(data.hasKey("inputTank")) {
			tanks[TANK_INPUT].readFromNBT(data.getCompoundTag("inputTank"));
		}
		
		if(data.hasKey("outputTank")) {
			tanks[TANK_OUTPUT].readFromNBT(data.getCompoundTag("outputTank"));
		}
	}

	@Override
	public void getOrphanData(IMultiblockPart newOrphan, int oldSize, int newSize, NBTTagCompound dataContainer) {
		// TODO: Proportionally allocate things like fluids
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
			// TODO: Input tank can only be filled with compatible fluids
			return true;
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

	public boolean isActive() {
		// TODO Auto-generated method stub
		return false;
	}

}

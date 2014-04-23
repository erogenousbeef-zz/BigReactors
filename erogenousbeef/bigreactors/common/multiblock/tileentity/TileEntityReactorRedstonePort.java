package erogenousbeef.bigreactors.common.multiblock.tileentity;

import net.minecraft.block.Block;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import erogenousbeef.bigreactors.client.gui.GuiReactorRedstonePort;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.multiblock.MultiblockReactor;
import erogenousbeef.bigreactors.common.multiblock.block.BlockReactorRedstonePort;
import erogenousbeef.bigreactors.common.multiblock.interfaces.ITickableMultiblockPart;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorRedNetPort.CircuitType;
import erogenousbeef.bigreactors.gui.container.ContainerBasic;
import erogenousbeef.core.multiblock.MultiblockControllerBase;
import erogenousbeef.core.multiblock.MultiblockValidationException;

public class TileEntityReactorRedstonePort extends TileEntityReactorPartBase
		implements ITickableMultiblockPart {

	protected CircuitType circuitType;
	protected int outputLevel;
	protected boolean activeOnPulse;
	protected boolean greaterThan; // if false, less than
	
	protected int ticksSinceLastUpdate;
	
	// These are local-only and used for handy state calculations
	protected boolean isExternallyPowered;
	
	public TileEntityReactorRedstonePort() {
		super();
		
		circuitType = circuitType.DISABLED;
		isExternallyPowered = false;
		ticksSinceLastUpdate = 0;
	}
	
	// Redstone methods
	public boolean isRedstoneActive() {
		if(!this.isConnected()) { return false; }

		MultiblockReactor reactor = (MultiblockReactor)getMultiblockController();

		switch(circuitType) {
		case outputFuelTemperature:
			return checkVariable((int)reactor.getFuelHeat());
		case outputCasingTemperature:
			return checkVariable((int)reactor.getReactorHeat());
		case outputFuelMix:
			return checkVariable((int)(reactor.getFuelRichness()*100));
		case outputFuelAmount:
			return checkVariable(reactor.getFuelAmount());
		case outputWasteAmount:
			return checkVariable(reactor.getWasteAmount());
		case outputEnergyAmount:
			return checkVariable(reactor.getEnergyStoredPercentage());
		case DISABLED:
			return false;
		default:
			return this.isExternallyPowered;
		}
	}
	
	public boolean isInput() {
		return TileEntityReactorRedNetPort.isInput(this.circuitType);
	}
	
	public boolean isOutput() {
		return TileEntityReactorRedNetPort.isOutput(this.circuitType);
	}
	
	protected boolean checkVariable(int value) {
		if(this.greaterThan) {
			return value > getOutputLevel();
		}
		else {
			return value < getOutputLevel();
		}
	}
	
	public void sendRedstoneUpdate() {
		if(this.worldObj != null && !this.worldObj.isRemote) {
			int md;

			if(this.isOutput()) {
				md = isRedstoneActive() ? BlockReactorRedstonePort.META_REDSTONE_LIT : BlockReactorRedstonePort.META_REDSTONE_UNLIT;
			}
			else {
				md = isExternallyPowered ? BlockReactorRedstonePort.META_REDSTONE_LIT : BlockReactorRedstonePort.META_REDSTONE_UNLIT;
			}

			if(md != this.worldObj.getBlockMetadata(xCoord, yCoord, zCoord)) {
				this.worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, md, 3);
			}
		}
	}
	
	public void onNeighborBlockChange(int x, int y, int z, int neighborBlockID) {
		if(!this.isConnected()) { return; }

		if(this.isInput()) {
			ForgeDirection out = getOutwardsDir();
			boolean nowPowered = isReceivingRedstonePowerFrom(worldObj, xCoord+out.offsetX, yCoord+out.offsetY, zCoord+out.offsetZ, out, neighborBlockID);

			if(this.isExternallyPowered != nowPowered) {
				this.isExternallyPowered = nowPowered;
				this.onRedstoneInputUpdated();
				this.sendRedstoneUpdate();
			}
		}
		else {
			this.isExternallyPowered = false;
		}
	}

	// Called to do business logic when the redstone value has changed
	protected void onRedstoneInputUpdated() {
		if(!this.isConnected()) { return; }

		MultiblockReactor reactor = (MultiblockReactor)getMultiblockController();
		switch(this.circuitType) {
		case inputActive:
			if(this.isInputActiveOnPulse()) {
				if(this.isExternallyPowered) {
					reactor.setActive(!reactor.isActive());
				}
			}
			else {
				reactor.setActive(this.isExternallyPowered);
			}
			break;
		case inputSetControlRod:
			// On/off only
			if(this.isInputActiveOnPulse()) {
				if(this.isExternallyPowered) {
					if(this.shouldSetControlRodsInsteadOfChange()) {
						reactor.setAllControlRodInsertionValues(this.outputLevel);
					}
					else {
						reactor.changeAllControlRodInsertionValues((short)this.outputLevel); // Can be negative, don't want to mask.
					}
				}
			}
			else {
				if(this.isExternallyPowered) {
					reactor.setAllControlRodInsertionValues(getControlRodLevelWhileOn());
				}
				else {
					reactor.setAllControlRodInsertionValues(getControlRodLevelWhileOff());
				}
			}
			break;
		case inputEjectWaste:
			// Pulse only
			if(this.isExternallyPowered) {
				reactor.ejectWaste(false, null);
			}
			break;
		default:
			break;
		}
	}
	
	public int getOutputLevel() { return outputLevel; }
	public int getControlRodLevelWhileOff() { return ((outputLevel & 0xFF00) << 8) & 0xFF; }
	public int getControlRodLevelWhileOn () { return outputLevel & 0xFF; }
	
	public static int packControlRodLevels(byte off, byte on) {
		return (((int)off >> 8) & 0xFF00) | (on & 0xFF);
	}

	public static int unpackControlRodLevelOn(int level) {
		return level & 0xff;
	}
	
	public static int unpackControlRodLevelOff(int level) {
		return ((level % 0xff00) << 8) & 0xff;
	}

	public boolean isInputActiveOnPulse() {
		return this.activeOnPulse;
	}

	/**
	 * @param newType The type of the new circuit.
	 * @param param1 For input/control rods, the level(s) to change or set. For outputs, the numerical value
	 * @param greater Than For outputs, whether to activate when greater than or less than the outputLevel value. For input/control rods, whether to set (true) or change (false) the values.
	 */
	public void onReceiveUpdatePacket(int newType, int outputLevel, boolean greaterThan, boolean activeOnPulse) {
		boolean oldTypeWasInput = this.isInput();
		this.circuitType = CircuitType.values()[newType];
		this.outputLevel = outputLevel;
		this.greaterThan = greaterThan;
		this.activeOnPulse = activeOnPulse;

		if(isAlwaysActiveOnPulse(circuitType)) { this.activeOnPulse = true; }
		else if(TileEntityReactorRedNetPort.isOutput(this.circuitType)) { this.activeOnPulse = false; }
		
		// Do updates
		if(this.isInput()) {
			// Update inputs so we don't pulse/change automatically
			ForgeDirection out = getOutwardsDir();
			this.isExternallyPowered = isReceivingRedstonePowerFrom(worldObj, xCoord+out.offsetX, yCoord+out.offsetY, zCoord+out.offsetZ, out);
			if(!this.isInputActiveOnPulse()) {
				onRedstoneInputUpdated();
			}
		}
		else {
			this.isExternallyPowered = false;
		}

		// Ensure visuals and metadata reflect our new settings & state
		this.sendRedstoneUpdate();

		if(!this.worldObj.isRemote) {
			// Propagate the new settings
			this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		}
	}
	
	@SideOnly(Side.CLIENT)
	public boolean getGreaterThan() { return this.greaterThan; }
	
	public CircuitType getCircuitType() { return this.circuitType; }
	private boolean shouldSetControlRodsInsteadOfChange() { return !greaterThan; }

	public void onRedNetUpdate(int powerLevel) {
		if(this.isInput()) {
			boolean wasPowered = this.isExternallyPowered;
			this.isExternallyPowered = powerLevel > 0;
			if(wasPowered != this.isExternallyPowered) {
				this.onRedstoneInputUpdated();
				this.sendRedstoneUpdate();
			}
		}
	}
	
	/**
	 * Call with the coordinates of the block to check and the direction
	 * towards that block from your block.
	 * If the block towards which this block is emitting power lies north,
	 * then pass in south.
	 */
	private boolean isReceivingRedstonePowerFrom(World world, int x, int y, int z, ForgeDirection dir) {
		// This is because of bugs in vanilla redstone wires
		int blockId = world.getBlockId(x, y, z);
		return isReceivingRedstonePowerFrom(world, x, y, z, dir, blockId);
	}
	
	/**
	 * Call with the coordinates of the block to check and the direction
	 * towards that block from your block.
	 * If the block towards which this block is emitting power lies north,
	 * then pass in south.
	 */
	private boolean isReceivingRedstonePowerFrom(World world, int x, int y, int z, ForgeDirection dir, int neighborBlockId) {
		if(neighborBlockId == Block.redstoneWire.blockID) {
			// Use metadata because of vanilla redstone wire bugs
			return world.getBlockMetadata(x, y, z) > 0;
		}
		else {
			return world.getIndirectPowerOutput(x, y, z, dir.ordinal()) || world.isBlockProvidingPowerTo(x, y, z, dir.ordinal()) > 0;
		}
	}
	
	// TileEntity overrides

	// Only refresh if we're switching functionality
	// Warning: dragonz!
	@Override
    public boolean shouldRefresh(int oldID, int newID, int oldMeta, int newMeta, World world, int x, int y, int z)
    {
		if(oldID != newID) {
			return true;
		}
	
		// All redstone ports are the same, we just use metadata to easily signal changes.
		return false;
    }

	// IReactorTickable
	/**
	 * Updates the redstone block's status, if it's an output network, if there is one.
	 * Will only send one update per N ticks, where N is a configurable setting.
	 */
	public void onMultiblockServerTick() {
		if(!this.isConnected()) { return; }

		ticksSinceLastUpdate++;
		if(ticksSinceLastUpdate < BigReactors.ticksPerRedstoneUpdate) { return; }

		if(this.isOutput()) {
			// Will no-op if there's no change.
			this.sendRedstoneUpdate();
		}
		ticksSinceLastUpdate = 0;
	}
	
	// MultiblockTileEntityBase methods
	private void readData(NBTTagCompound data) {
		if(data.hasKey("circuitType")) {
			this.circuitType = circuitType.values()[data.getInteger("circuitType")];
		}
		
		if(data.hasKey("outputLevel")) {
			this.outputLevel = data.getInteger("outputLevel");
		}
		
		if(data.hasKey("greaterThan")) {
			this.greaterThan = data.getBoolean("greaterThan");
		}
		
		if(data.hasKey("activeOnPulse")) {
			this.activeOnPulse = data.getBoolean("activeOnPulse");
		}
	}
	
	private void writeData(NBTTagCompound data) {
		data.setInteger("circuitType", this.circuitType.ordinal());
		data.setInteger("outputLevel", this.outputLevel);
		data.setBoolean("greaterThan", this.greaterThan);
		data.setBoolean("activeOnPulse", this.activeOnPulse);
	}

	@Override
	public void readFromNBT(NBTTagCompound data) {
		super.readFromNBT(data);
		this.readData(data);
		this.sendRedstoneUpdate();
	}

	@Override
	public void writeToNBT(NBTTagCompound data) {
		super.writeToNBT(data);
		this.writeData(data);
	}
	
	@Override
	public void decodeDescriptionPacket(NBTTagCompound data) {
		super.decodeDescriptionPacket(data);
		this.readData(data);
	}

	@Override
	public void encodeDescriptionPacket(NBTTagCompound data) {
		super.encodeDescriptionPacket(data);
		data.setInteger("circuitType", this.circuitType.ordinal());
		data.setInteger("outputLevel", this.outputLevel);
		data.setBoolean("greaterThan", this.greaterThan);
		data.setBoolean("activeOnPulse", this.activeOnPulse);
	}
	
	@Override
	public void isGoodForFrame() throws MultiblockValidationException {
		throw new MultiblockValidationException(String.format("%d, %d, %d - Redstone ports may only be placed on a reactor's external side faces, not as part of the frame", xCoord, yCoord, zCoord));
	}

	@Override
	public void isGoodForSides() throws MultiblockValidationException {
	}

	@Override
	public void isGoodForTop() throws MultiblockValidationException {
		throw new MultiblockValidationException(String.format("%d, %d, %d - Redstone ports may only be placed on a reactor's external side faces, not the top", xCoord, yCoord, zCoord));
	}

	@Override
	public void isGoodForBottom() throws MultiblockValidationException {
		throw new MultiblockValidationException(String.format("%d, %d, %d - Redstone ports may only be placed on a reactor's external side faces, not the bottom", xCoord, yCoord, zCoord));
	}

	@Override
	public void isGoodForInterior() throws MultiblockValidationException {
		throw new MultiblockValidationException(String.format("%d, %d, %d - Redstone ports may not be placed in a reactor's interior", xCoord, yCoord, zCoord));
	}

	@Override
	public void onMachineAssembled(MultiblockControllerBase controller) {
		super.onMachineAssembled(controller);
		this.sendRedstoneUpdate();
	}

	@Override
	public void onMachineBroken() {
		super.onMachineBroken();
		this.sendRedstoneUpdate();
	}

	@Override
	public void onMachineActivated() {
		this.sendRedstoneUpdate();
	}

	@Override
	public void onMachineDeactivated() {
		this.sendRedstoneUpdate();
	}

	// IBeefGuiEntity
	@SideOnly(Side.CLIENT)
	@Override
	public Object getGuiElement(InventoryPlayer inventoryPlayer) {
		return new GuiReactorRedstonePort(new ContainerBasic(), this);
	}

	@Override
	public Object getContainer(InventoryPlayer inventoryPlayer) {
		return new ContainerBasic();
	}

	public static boolean isAlwaysActiveOnPulse(CircuitType circuitType) {
		return circuitType == CircuitType.inputEjectWaste;
	}
}

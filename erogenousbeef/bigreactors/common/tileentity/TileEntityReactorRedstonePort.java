package erogenousbeef.bigreactors.common.tileentity;

import java.io.DataInputStream;
import java.io.IOException;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import erogenousbeef.bigreactors.api.HeatPulse;
import erogenousbeef.bigreactors.api.IHeatEntity;
import erogenousbeef.bigreactors.api.IRadiationModerator;
import erogenousbeef.bigreactors.api.IRadiationPulse;
import erogenousbeef.bigreactors.client.gui.GuiReactorRedstonePort;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.block.BlockReactorRedstonePort;
import erogenousbeef.bigreactors.common.multiblock.IReactorTickable;
import erogenousbeef.bigreactors.common.multiblock.MultiblockReactor;
import erogenousbeef.bigreactors.common.tileentity.TileEntityReactorRedNetPort.CircuitType;
import erogenousbeef.bigreactors.gui.IBeefGuiEntity;
import erogenousbeef.bigreactors.gui.container.ContainerBasic;
import erogenousbeef.core.common.CoordTriplet;
import erogenousbeef.core.multiblock.MultiblockControllerBase;
import erogenousbeef.core.multiblock.MultiblockTileEntityBase;

public class TileEntityReactorRedstonePort extends MultiblockTileEntityBase
		implements IRadiationModerator, IHeatEntity, IBeefGuiEntity, IReactorTickable {

	protected ForgeDirection out;
	protected CircuitType circuitType;
	protected int outputLevel;
	protected boolean activeOnPulse;
	protected boolean greaterThan; // if false, less than
	
	protected int ticksSinceLastUpdate;
	
	// These are local-only and used for handy state calculations
	protected boolean isExternallyPowered;
	
	public TileEntityReactorRedstonePort() {
		super();
		
		out = ForgeDirection.UNKNOWN;
		circuitType = circuitType.DISABLED;
		isExternallyPowered = false;
		ticksSinceLastUpdate = 0;
	}
	
	// Redstone methods
	public boolean isRedstoneActive() {
		if(!this.isConnected()) { return false; }

		MultiblockReactor reactor = (MultiblockReactor)getMultiblockController();

		switch(circuitType) {
		case outputTemperature:
			return checkVariable((int)reactor.getHeat());
		case outputFuelMix:
			return checkVariable((int)(reactor.getFuelRichness()*100));
		case outputFuelAmount:
			return checkVariable(reactor.getFuelAmount());
		case outputWasteAmount:
			return checkVariable(reactor.getWasteAmount());
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
			boolean nowPowered = this.worldObj.getIndirectPowerOutput(xCoord+out.offsetX, yCoord+out.offsetY, zCoord+out.offsetZ, out.getOpposite().ordinal());

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
				reactor.ejectWaste();
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
			this.isExternallyPowered = this.worldObj.getIndirectPowerOutput(xCoord+out.offsetX, yCoord+out.offsetY, zCoord+out.offsetZ, out.getOpposite().ordinal());
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
	public ForgeDirection getOutwardsDirection() {
		if(out == ForgeDirection.UNKNOWN) {
			recalculateOutDirection();
		}

		return out;
	}
	
	@SideOnly(Side.CLIENT)
	public boolean getGreaterThan() { return this.greaterThan; }
	
	public CircuitType getCircuitType() { return this.circuitType; }
	private boolean shouldSetControlRodsInsteadOfChange() { return !greaterThan; }

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
	public void onReactorTick() {
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
	public void formatDescriptionPacket(NBTTagCompound data) {
		super.formatDescriptionPacket(data);
		data.setInteger("circuitType", this.circuitType.ordinal());
		data.setInteger("outputLevel", this.outputLevel);
		data.setBoolean("greaterThan", this.greaterThan);
		data.setBoolean("activeOnPulse", this.activeOnPulse);
	}
	
	@Override
	public MultiblockControllerBase getNewMultiblockControllerObject() {
		return new MultiblockReactor(this.worldObj);
	}

	@Override
	public boolean isGoodForFrame() {
		return false;
	}

	@Override
	public boolean isGoodForSides() {
		return true;
	}

	@Override
	public boolean isGoodForTop() {
		return false;
	}

	@Override
	public boolean isGoodForBottom() {
		return false;
	}

	@Override
	public boolean isGoodForInterior() {
		return false;
	}

	@Override
	public void onMachineAssembled() {
		recalculateOutDirection();
		this.sendRedstoneUpdate();
	}

	@Override
	public void onMachineBroken() {
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

	@Override
	public void onAttached(MultiblockControllerBase newController) {
		super.onAttached(newController);
		
		recalculateOutDirection();
	}

	// This doesn't work right on the client...
	private void recalculateOutDirection() {
		MultiblockControllerBase controller = this.getMultiblockController();
		CoordTriplet minCoord = controller.getMinimumCoord();
		CoordTriplet maxCoord = controller.getMaximumCoord();

		if(this.xCoord == minCoord.x) {
			out = ForgeDirection.WEST;
		}
		else if(this.xCoord == maxCoord.x){
			out = ForgeDirection.EAST;
		}
		else if(this.zCoord == minCoord.z) {
			out = ForgeDirection.NORTH;
		}
		else if(this.zCoord == maxCoord.z) {
			out = ForgeDirection.SOUTH;
		}
		else if(this.yCoord == minCoord.y) {
			// Just in case I end up making omnidirectional taps.
			out = ForgeDirection.DOWN;
		}
		else if(this.yCoord == maxCoord.y){
			// Just in case I end up making omnidirectional taps.
			out = ForgeDirection.UP;
		}
		else {
			// WTF BRO
			out = ForgeDirection.UNKNOWN;
		}
	}

	// IRadiationModerator
	@Override
	public void receiveRadiationPulse(IRadiationPulse radiation) {
		float freePower = radiation.getSlowRadiation() * 0.25f;
		
		// Convert 25% of incident radiation to power, for balance reasons.
		radiation.addPower(freePower);
		
		// Slow radiation is all lost now
		radiation.setSlowRadiation(0);
		
		// And zero out the TTL so evaluation force-stops
		radiation.setTimeToLive(0);
	}

	// IHeatEntity
	@Override
	public float getHeat() {
		if(!this.isConnected()) { return 0f; }
		return ((MultiblockReactor)getMultiblockController()).getHeat();
	}

	@Override
	public float getThermalConductivity() {
		// Using iron so there's no disadvantage to reactor glass.
		return IHeatEntity.conductivityIron;
	}

	@Override
	public float onAbsorbHeat(IHeatEntity source, HeatPulse pulse, int faces, int contactArea) {
		float deltaTemp = source.getHeat() - getHeat();
		// If the source is cooler than the reactor, then do nothing
		if(deltaTemp <= 0.0f) {
			return 0.0f;
		}

		float heatToAbsorb = deltaTemp * getThermalConductivity() * (1.0f/(float)faces) * contactArea;

		pulse.heatChange += heatToAbsorb;

		return heatToAbsorb;
	}

	@Override
	public HeatPulse onRadiateHeat(float ambientHeat) {
		// Ignore, glass doesn't re-radiate heat
		return null;
	}

	// IBeefGuiEntity
	@Override
	public GuiScreen getGUI(EntityPlayer player) {
		return new GuiReactorRedstonePort(new ContainerBasic(), this);
	}

	@Override
	public Container getContainer(EntityPlayer player) {
		return new ContainerBasic();
	}

	@Override
	public void beginUpdatingPlayer(EntityPlayer player) {
	}

	@Override
	public void stopUpdatingPlayer(EntityPlayer player) {
	}

	@Override
	public void onReceiveGuiButtonPress(String buttonName,
			DataInputStream dataStream) throws IOException {
	}
	
	public static boolean isAlwaysActiveOnPulse(CircuitType circuitType) {
		return circuitType == CircuitType.inputEjectWaste;
	}
}

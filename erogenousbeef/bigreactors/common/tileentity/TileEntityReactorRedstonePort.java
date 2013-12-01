package erogenousbeef.bigreactors.common.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import erogenousbeef.core.common.CoordTriplet;
import erogenousbeef.core.multiblock.MultiblockControllerBase;
import erogenousbeef.core.multiblock.MultiblockTileEntityBase;
import erogenousbeef.bigreactors.api.HeatPulse;
import erogenousbeef.bigreactors.api.IHeatEntity;
import erogenousbeef.bigreactors.api.IRadiationModerator;
import erogenousbeef.bigreactors.api.IRadiationPulse;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.block.BlockReactorRedstonePort;
import erogenousbeef.bigreactors.common.multiblock.MultiblockReactor;
import erogenousbeef.bigreactors.common.tileentity.TileEntityReactorRedNetPort.CircuitType;

public class TileEntityReactorRedstonePort extends MultiblockTileEntityBase
		implements IRadiationModerator, IHeatEntity {

	protected ForgeDirection out;
	protected CircuitType circuitType;
	protected int outputLevel;
	protected boolean greaterThan; // if false, less than
	
	protected boolean wasLit;
	
	public TileEntityReactorRedstonePort() {
		super();
		
		out = ForgeDirection.UNKNOWN;
		circuitType = circuitType.DISABLED;
		wasLit = false;
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
		case outputWasteAmount:
			// TODO: These
			return false;
		default:
			return false;
		}
	}
	
	public boolean isInput() {
		return TileEntityReactorRedNetPort.isInput(this.circuitType);
	}
	
	public boolean isOutput() {
		return TileEntityReactorRedNetPort.isOutput(this.circuitType);
	}
	
	protected boolean checkVariable(int value) {
		if(value > outputLevel && this.greaterThan) {
			return true;
		}
		else {
			return value < outputLevel;
		}
	}
	
	public void sendRedstoneUpdate() {
		if(this.worldObj != null && !this.worldObj.isRemote) {
			int md = BlockReactorRedstonePort.META_REDSTONE_LIT;

			if(this.isOutput()) {
				md = isRedstoneActive() ? BlockReactorRedstonePort.META_REDSTONE_LIT : BlockReactorRedstonePort.META_REDSTONE_UNLIT;
			}

			this.worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, BlockReactorRedstonePort.META_REDSTONE_LIT, 3);
		}
	}
	
	@SideOnly(Side.CLIENT)
	public ForgeDirection getOutwardsDirection() {
		if(out == ForgeDirection.UNKNOWN) {
			recalculateOutDirection();
		}

		return out;
	}
	
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

	// MultiblockTileEntityBase methods
	@Override
	public void decodeDescriptionPacket(NBTTagCompound data) {
		super.decodeDescriptionPacket(data);
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
		this.sendRedstoneUpdate();
		recalculateOutDirection();
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

}

package erogenousbeef.bigreactors.common.tileentity;

import net.minecraft.tileentity.TileEntity;
import erogenousbeef.bigreactors.api.HeatPulse;
import erogenousbeef.bigreactors.api.IHeatEntity;
import erogenousbeef.bigreactors.api.IRadiationModerator;
import erogenousbeef.bigreactors.api.IRadiationPulse;
import erogenousbeef.bigreactors.common.multiblock.MultiblockReactor;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorControlRod;
import erogenousbeef.core.multiblock.IMultiblockPart;
import erogenousbeef.core.multiblock.MultiblockControllerBase;
import erogenousbeef.core.multiblock.MultiblockTileEntityBase;
import erogenousbeef.core.multiblock.MultiblockValidationException;

public class TileEntityFuelRod extends MultiblockTileEntityBase implements IRadiationModerator, IHeatEntity {

	// Y-value of the control rod, if this Fuel Rod is attached to one
	protected int controlRodY;
	protected boolean isAssembled;
	
	public TileEntityFuelRod() {
		super();
		
		isAssembled = false;
		controlRodY = 0;
	}
	
	// We're just a proxy and data-capsule.
	@Override
    public boolean canUpdate() { return false; }
	
	
	public void onAssemble(TileEntityReactorControlRod controlRod) {
		this.controlRodY = controlRod.yCoord;
		this.isAssembled = true;
	}
	
	public void onDisassemble() {
		this.controlRodY = 0;
		this.isAssembled = false;
	}

	// IRadiationModerator
	@Override
	public void receiveRadiationPulse(IRadiationPulse radiation) {
		if(this.isAssembled) {
			TileEntity te = this.worldObj.getBlockTileEntity(xCoord, controlRodY, zCoord);
			if(te != null && te instanceof IRadiationModerator) {
				((IRadiationModerator)te).receiveRadiationPulse(radiation);
			}
		}
	}

	// IHeatEntity
	@Override
	public float getHeat() {
		if(this.isAssembled) {
			TileEntity te = this.worldObj.getBlockTileEntity(xCoord, controlRodY, zCoord);
			if(te != null && te instanceof IHeatEntity) {
				return ((IHeatEntity)te).getHeat();
			}
		}

		return IHeatEntity.ambientHeat;
	}

	@Override
	public float getThermalConductivity() {
		if(this.isAssembled) {
			TileEntity te = this.worldObj.getBlockTileEntity(xCoord, controlRodY, zCoord);
			if(te != null && te instanceof IHeatEntity) {
				return ((IHeatEntity)te).getThermalConductivity();
			}
		}

		return IHeatEntity.conductivityCopper;
	}

	@Override
	public float onAbsorbHeat(IHeatEntity source, HeatPulse pulse, int faces, int contactArea) {
		if(this.isAssembled) {
			TileEntity te = this.worldObj.getBlockTileEntity(xCoord, controlRodY, zCoord);
			if(te != null && te instanceof IHeatEntity) {
				return ((IHeatEntity)te).onAbsorbHeat(source, pulse, faces, contactArea);
			}
		}

		// perform standard calculation, this is a flaw in the algorithm
		float deltaTemp = source.getHeat() - getHeat();
		if(deltaTemp <= 0.0f) {
			return 0.0f;
		}

		return deltaTemp * 0.05f * getThermalConductivity() * (1.0f/(float)faces) * contactArea;
	}

	/**
	 * This method is used to leak heat from the fuel rods
	 * into the reactor. It should run regardless of activity.
	 * @param ambientHeat The heat of the reactor surrounding the fuel rod.
	 * @return A HeatPulse containing the environmental results of radiating heat.
	 */
	@Override
	public HeatPulse onRadiateHeat(float ambientHeat) {
		// We don't do this.
		return null;
	}

	@Override
	public MultiblockControllerBase createNewMultiblock() {
		return new MultiblockReactor(this.worldObj);
	}
	
	@Override
	public Class<? extends MultiblockControllerBase> getMultiblockControllerType() { return MultiblockReactor.class; }

	@Override
	public void isGoodForFrame() throws MultiblockValidationException {
		throw new MultiblockValidationException(String.format("%d, %d, %d - fuel rods may only be placed in the reactor interior", xCoord, yCoord, zCoord));
	}

	@Override
	public void isGoodForSides() throws MultiblockValidationException {
		throw new MultiblockValidationException(String.format("%d, %d, %d - fuel rods may only be placed in the reactor interior", xCoord, yCoord, zCoord));
	}

	@Override
	public void isGoodForTop() throws MultiblockValidationException {
		throw new MultiblockValidationException(String.format("%d, %d, %d - fuel rods may only be placed in the reactor interior", xCoord, yCoord, zCoord));
	}

	@Override
	public void isGoodForBottom() throws MultiblockValidationException {
		throw new MultiblockValidationException(String.format("%d, %d, %d - fuel rods may only be placed in the reactor interior", xCoord, yCoord, zCoord));
	}

	@Override
	public void isGoodForInterior() throws MultiblockValidationException {
		// Check above and below. Above must be fuel rod or control rod.
		TileEntity entityAbove = this.worldObj.getBlockTileEntity(xCoord, yCoord+1, zCoord);
		if(!(entityAbove instanceof TileEntityFuelRod || entityAbove instanceof TileEntityReactorControlRod)) {
			throw new MultiblockValidationException(String.format("Fuel rod at %d, %d, %d must be part of a vertical column that reaches the entire height of the reactor, with a control rod on top.", xCoord, yCoord, zCoord));
		}

		// Below must be fuel rod or the base of the reactor.
		TileEntity entityBelow = this.worldObj.getBlockTileEntity(xCoord, yCoord-1, zCoord);
		if(entityBelow instanceof TileEntityFuelRod) {
			return;
		}
		else if(entityBelow instanceof IMultiblockPart) {
			((IMultiblockPart)entityBelow).isGoodForBottom();
			return;
		}
		
		throw new MultiblockValidationException(String.format("Fuel rod at %d, %d, %d must be part of a vertical column that reaches the entire height of the reactor, with a control rod on top.", xCoord, yCoord, zCoord));
	}

	@Override
	public void onMachineAssembled(MultiblockControllerBase multiblockControllerBase) {
	}

	@Override
	public void onMachineBroken() {
	}

	@Override
	public void onMachineActivated() {
	}

	@Override
	public void onMachineDeactivated() {
	}
}

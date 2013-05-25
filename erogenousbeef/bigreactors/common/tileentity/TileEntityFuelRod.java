package erogenousbeef.bigreactors.common.tileentity;

import java.util.Random;

import erogenousbeef.bigreactors.api.HeatPulse;
import erogenousbeef.bigreactors.api.IHeatEntity;
import erogenousbeef.bigreactors.api.IRadiationModerator;
import erogenousbeef.bigreactors.api.IRadiationPulse;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.RadiationPulse;
import erogenousbeef.bigreactors.common.multiblock.MultiblockReactor;
import erogenousbeef.core.multiblock.IMultiblockPart;
import erogenousbeef.core.multiblock.MultiblockControllerBase;
import erogenousbeef.core.multiblock.MultiblockTileEntityBase;
import net.minecraft.block.material.Material;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet132TileEntityData;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.ILiquidTank;
import net.minecraftforge.liquids.ITankContainer;
import net.minecraftforge.liquids.LiquidContainerRegistry;
import net.minecraftforge.liquids.LiquidStack;
import net.minecraftforge.liquids.LiquidTank;

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
	public double getHeat() {
		if(this.isAssembled) {
			TileEntity te = this.worldObj.getBlockTileEntity(xCoord, controlRodY, zCoord);
			if(te != null && te instanceof IHeatEntity) {
				return ((IHeatEntity)te).getHeat();
			}
		}

		return IHeatEntity.ambientHeat;
	}

	@Override
	public double getThermalConductivity() {
		if(this.isAssembled) {
			TileEntity te = this.worldObj.getBlockTileEntity(xCoord, controlRodY, zCoord);
			if(te != null && te instanceof IHeatEntity) {
				return ((IHeatEntity)te).getThermalConductivity();
			}
		}

		return IHeatEntity.conductivityCopper;
	}

	@Override
	public double onAbsorbHeat(IHeatEntity source, HeatPulse pulse, int faces, int contactArea) {
		if(this.isAssembled) {
			TileEntity te = this.worldObj.getBlockTileEntity(xCoord, controlRodY, zCoord);
			if(te != null && te instanceof IHeatEntity) {
				return ((IHeatEntity)te).onAbsorbHeat(source, pulse, faces, contactArea);
			}
		}

		// perform standard calculation, this is a flaw in the algorithm
		double deltaTemp = source.getHeat() - getHeat();
		if(deltaTemp <= 0.0) {
			return 0.0;
		}

		return deltaTemp * 0.05 * getThermalConductivity() * (1.0/(double)faces) * contactArea;
	}

	/**
	 * This method is used to leak heat from the fuel rods
	 * into the reactor. It should run regardless of activity.
	 * @param ambientHeat The heat of the reactor surrounding the fuel rod.
	 * @return A HeatPulse containing the environmental results of radiating heat.
	 */
	@Override
	public HeatPulse onRadiateHeat(double ambientHeat) {
		// We don't do this.
		return null;
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
		return false;
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
		// Check above and below. Above must be fuel rod or control rod.
		TileEntity entityAbove = this.worldObj.getBlockTileEntity(xCoord, yCoord+1, zCoord);
		if(entityAbove != null && (entityAbove instanceof TileEntityFuelRod || entityAbove instanceof TileEntityReactorControlRod)) {
			return true;
		}
		
		// Below must be fuel rod or the base of the reactor.
		TileEntity entityBelow = this.worldObj.getBlockTileEntity(xCoord, yCoord+1, zCoord);
		if(entityBelow instanceof TileEntityFuelRod) {
			return true;
		}
		else if(entityBelow instanceof IMultiblockPart) {
			return ((IMultiblockPart)entityBelow).isGoodForBottom();
		}
		
		return false;
	}

	@Override
	public void onMachineAssembled() {
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

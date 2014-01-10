package erogenousbeef.bigreactors.common.multiblock.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import erogenousbeef.bigreactors.common.multiblock.MultiblockTurbine;

public class TileEntityTurbineFluidPort extends TileEntityTurbinePartStandard implements IFluidHandler {

	public enum FluidFlow {
		In,
		Out
	}

	FluidFlow flowSetting;
	
	public TileEntityTurbineFluidPort() {
		super();
		flowSetting = FluidFlow.In;
	}

	public TileEntityTurbineFluidPort(int metadata) {
		super(metadata);
		flowSetting = FluidFlow.In;
	}
	
	public void setFluidFlowDirection(FluidFlow newDirection) {
		flowSetting = newDirection;
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}
	
	@Override
	public void writeToNBT(NBTTagCompound data) {
		super.writeToNBT(data);
		data.setInteger("flowSetting", flowSetting.ordinal());
	}
	
	@Override
	public void readFromNBT(NBTTagCompound data) {
		super.readFromNBT(data);
		if(data.hasKey("flowSetting")) {
			flowSetting = FluidFlow.values()[data.getInteger("flowSetting")];
		}
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
		if(!isConnected() || from != getOutwardsDir()) { return 0; }

		return getTurbine().fill(getTankIndex(), resource, doFill);
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource,
			boolean doDrain) {
		if(resource == null || !isConnected() || from != getOutwardsDir()) { return resource; }

		return getTurbine().drain(getTankIndex(), resource, doDrain);
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
		if(!isConnected() || from != getOutwardsDir()) { return null; }

		return getTurbine().drain(getTankIndex(), maxDrain, doDrain);
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid) {
		if(!isConnected() || from != getOutwardsDir()) { return false; }
		
		return getTurbine().canFill(getTankIndex(), fluid);
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid) {
		if(!isConnected() || from != getOutwardsDir()) { return false; }
		
		return getTurbine().canDrain(getTankIndex(), fluid);
	}

	protected static final FluidTankInfo[] emptyTankInfoArray = new FluidTankInfo[0];
	
	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from) {
		if(!isConnected() || from != getOutwardsDir()) { return emptyTankInfoArray; }
		return new FluidTankInfo[] { getTurbine().getTankInfo(getTankIndex()) };
	}
	
	private int getTankIndex() {
		if(flowSetting == FluidFlow.In) { return MultiblockTurbine.TANK_INPUT; }
		else { return MultiblockTurbine.TANK_OUTPUT; }
	}
	
	public FluidFlow getFlowDirection() { return flowSetting; }
}

package erogenousbeef.bigreactors.common.multiblock.tileentity;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import erogenousbeef.bigreactors.common.multiblock.MultiblockTurbine;
import erogenousbeef.bigreactors.common.multiblock.interfaces.INeighborUpdatableEntity;
import erogenousbeef.bigreactors.common.multiblock.interfaces.ITickableMultiblockPart;
import erogenousbeef.core.multiblock.MultiblockControllerBase;

public class TileEntityTurbineFluidPort extends TileEntityTurbinePartStandard implements IFluidHandler, INeighborUpdatableEntity, ITickableMultiblockPart {

	public enum FluidFlow {
		In,
		Out
	}

	FluidFlow flowSetting;
	IFluidHandler pumpDestination;
	
	public TileEntityTurbineFluidPort() {
		super();
		flowSetting = FluidFlow.In;
		pumpDestination = null;
	}

	public void setFluidFlowDirection(FluidFlow newDirection, boolean markDirty) {
		flowSetting = newDirection;

		if(!worldObj.isRemote) {
			if(markDirty) {
				this.notifyNeighborsOfBlockChange();
				this.markDirty();
			}
		}

		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}
	
	@Override
	public void onMachineAssembled(MultiblockControllerBase multiblockControllerBase)
	{
		super.onMachineAssembled(multiblockControllerBase);
		checkForAdjacentTank();
		
		if(!this.worldObj.isRemote) { 
			// Force a connection to neighboring objects
			this.notifyNeighborsOfTileChange();
			this.notifyNeighborsOfBlockChange();
		}
	}

	@Override
	public void onMachineBroken()
	{
		super.onMachineBroken();
		pumpDestination = null;
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
	public void encodeDescriptionPacket(NBTTagCompound data) {
		super.encodeDescriptionPacket(data);
		data.setInteger("flowSetting", flowSetting.ordinal());
	}
	
	@Override
	public void decodeDescriptionPacket(NBTTagCompound data) {
		super.decodeDescriptionPacket(data);
		if(data.hasKey("flowSetting")) {
			flowSetting = FluidFlow.values()[data.getInteger("flowSetting")];
		}
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
		if(!isConnected() || from != getOutwardsDir()) { return 0; }

		if(flowSetting != FluidFlow.In) {
			return 0;
		}
		
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
		
		if(flowSetting != FluidFlow.In) {
			return false;
		}
		
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
	
	// ITickableMultiblockPart
	
	@Override
	public void onMultiblockServerTick() {
		// Try to pump steam out, if an outlet
		if(pumpDestination == null || flowSetting != FluidFlow.Out)
			return;

		MultiblockTurbine turbine = getTurbine();
		FluidStack fluidToDrain = turbine.drain(MultiblockTurbine.TANK_OUTPUT, turbine.TANK_SIZE, false);
		
		if(fluidToDrain != null && fluidToDrain.amount > 0)
		{
			fluidToDrain.amount = pumpDestination.fill(getOutwardsDir().getOpposite(), fluidToDrain, true);
			turbine.drain(MultiblockTurbine.TANK_OUTPUT, fluidToDrain, true);
		}
	}
	
	// INeighborUpdatableEntity
	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block neighborBlock) {
		if(!world.isRemote) {
			checkForAdjacentTank();
		}
	}
	
	@Override
	public void onNeighborTileChange(IBlockAccess world, int x, int y, int z, int neighborX, int neighborY, int neighborZ) {
		if(!worldObj.isRemote) {
			checkForAdjacentTank();
		}
	}
	
	// Private Helpers
	protected void checkForAdjacentTank()
	{
		pumpDestination = null;
		if(worldObj.isRemote || flowSetting == FluidFlow.In) {
			return;
		}

		ForgeDirection outDir = getOutwardsDir();
		if(outDir == ForgeDirection.UNKNOWN) {
			return;
		}
		
		TileEntity neighbor = worldObj.getTileEntity(xCoord + outDir.offsetX, yCoord + outDir.offsetY, zCoord + outDir.offsetZ);
		if(neighbor instanceof IFluidHandler) {
			pumpDestination = (IFluidHandler)neighbor;
		}
	}
}

package erogenousbeef.bigreactors.common.multiblock.tileentity;

import erogenousbeef.bigreactors.common.multiblock.MultiblockHeatExchanger;
import erogenousbeef.bigreactors.common.multiblock.interfaces.INeighborUpdatableEntity;
import erogenousbeef.bigreactors.common.multiblock.interfaces.ITickableMultiblockPart;
import erogenousbeef.core.multiblock.MultiblockControllerBase;
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

public class TileEntityExchangerFluidPort extends TileEntityExchangerPart implements IFluidHandler, INeighborUpdatableEntity, ITickableMultiblockPart {
	
	public enum PortDirection {
		PrimaryInlet,
		PrimaryOutlet,
		SecondaryInlet,
		SecondaryOutlet
	};
	private static final PortDirection[] s_PortDirections = PortDirection.values();
	private static final FluidTankInfo[] s_NoTanks = new FluidTankInfo[0];
	
	private static int[] s_ExchangerIndices = {
		MultiblockHeatExchanger.CONDENSER_INLET,
		MultiblockHeatExchanger.CONDENSER_OUTLET,
		MultiblockHeatExchanger.EVAPORATOR_INLET,
		MultiblockHeatExchanger.EVAPORATOR_OUTLET
	};
	
	private IFluidHandler m_PumpDestination;
	private PortDirection m_PortDirection;

	private boolean isInlet() { return m_PortDirection == PortDirection.PrimaryInlet || m_PortDirection == PortDirection.SecondaryInlet; }
	private boolean isOutlet() { return m_PortDirection == PortDirection.PrimaryOutlet || m_PortDirection == PortDirection.SecondaryOutlet; }
	
	public TileEntityExchangerFluidPort() {
		super();
		m_PortDirection = PortDirection.PrimaryInlet;
	}

	@Override
	protected void encodeDescriptionPacket(NBTTagCompound packetData) {
		super.encodeDescriptionPacket(packetData);
		
		packetData.setInteger("direction", m_PortDirection.ordinal());
	}
	
	@Override
	protected void decodeDescriptionPacket(NBTTagCompound packetData) {
		super.decodeDescriptionPacket(packetData);
		
		if(packetData.hasKey("direction")) {
			setPortDirection(s_PortDirections[packetData.getInteger("direction")], false);
		}
	}
	
	public PortDirection getPortDirection() { return m_PortDirection; }
	
	public void setPortDirection(PortDirection newDirection, boolean markDirty) {
		if(newDirection == m_PortDirection) { return; }
		
		m_PortDirection = newDirection;
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		if(!worldObj.isRemote) {
			if(isOutlet()) {
				checkForAdjacentTank();
			}
			else {
				m_PortDirection = null;
			}

			if(markDirty) {
				markDirty();
			}
			else {
				notifyNeighborsOfTileChange();
			}
		}
		else {
			notifyNeighborsOfTileChange();
		}
	}

	private void checkForAdjacentTank() {
		m_PumpDestination = null;
		if(worldObj.isRemote || !isOutlet()) {
			return;
		}

		ForgeDirection outDir = getOutwardsDir();
		if(outDir == ForgeDirection.UNKNOWN) {
			return;
		}
		
		TileEntity neighbor = worldObj.getTileEntity(xCoord + outDir.offsetX, yCoord + outDir.offsetY, zCoord + outDir.offsetZ);
		if(neighbor instanceof IFluidHandler) {
			m_PumpDestination = (IFluidHandler)neighbor;
		}
	}

	@Override
	public void onMultiblockServerTick() {
		if(!isOutlet() || m_PumpDestination == null) { return; }
		
		MultiblockHeatExchanger exchanger = this.getExchangerController();

		// Pick outlet tank based on direction setting
		int exchangerIdx = s_ExchangerIndices[m_PortDirection.ordinal()];
		if(exchanger.getFluidAmount(exchangerIdx) <= 0) { return; } // Shortcut to reduce allocations
		
		FluidStack fluidToPump = exchanger.drain(exchangerIdx, exchanger.getCapacity(exchangerIdx), false);
		ForgeDirection pumpFromDir = getOutwardsDir().getOpposite();

		if(fluidToPump == null || fluidToPump.amount <= 0 || !m_PumpDestination.canFill(pumpFromDir, fluidToPump.getFluid())) {
			return;
		}
		
		// Acquire amount which can be filled
		int fluidAccepted = m_PumpDestination.fill(pumpFromDir, fluidToPump, false);
		if(fluidAccepted <= 0) { return; }
		
		// Pump out however much we can (limited by the destination or amount contained)
		fluidToPump.amount = Math.min(fluidAccepted, fluidToPump.amount);
		fluidToPump = exchanger.drain(exchangerIdx, fluidToPump.amount, true);
		int fluidPumped = m_PumpDestination.fill(pumpFromDir, fluidToPump, true);
		fluidToPump.amount = fluidToPump.amount - fluidPumped;

		// If any fluid could not be pumped, try to return it to the source.
		if(fluidToPump.amount > 0) {
			exchanger.returnFluid(exchangerIdx, fluidToPump);
		}
	}

	@Override
	public void onMachineAssembled(MultiblockControllerBase multiblockControllerBase)
	{
		super.onMachineAssembled(multiblockControllerBase);
		checkForAdjacentTank();

		this.notifyNeighborsOfTileChange();

		// Re-render on the client
		if(worldObj.isRemote) {
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		}
	}
	
	@Override
	public void onMachineBroken()
	{
		super.onMachineBroken();
		m_PumpDestination = null;
		
		if(worldObj.isRemote) {
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		}
	}
	
	// INeighborUpdatableEntity
	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block neighborBlock) {
		checkForAdjacentTank();
	}
	
	@Override
	public void onNeighborTileChange(IBlockAccess world, int x, int y, int z, int neighborX, int neighborY, int neighborZ) {
		checkForAdjacentTank();
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
		if(!isInlet() || resource == null || getOutwardsDir() != from) { return 0; }
		
		MultiblockHeatExchanger exchanger = getExchangerController();
		return exchanger.fill(s_ExchangerIndices[m_PortDirection.ordinal()], resource, doFill);
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource,
			boolean doDrain) {
		if(!isOutlet() || from != getOutwardsDir() || resource == null) { return null; }

		MultiblockHeatExchanger exchanger = getExchangerController();
		return exchanger.drain(s_ExchangerIndices[m_PortDirection.ordinal()], resource, doDrain);
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
		if(!isOutlet() || from != getOutwardsDir() || maxDrain <= 0) { return null; }

		MultiblockHeatExchanger exchanger = getExchangerController();
		return exchanger.drain(s_ExchangerIndices[m_PortDirection.ordinal()], maxDrain, doDrain);
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid) {
		if(!isInlet() || from != getOutwardsDir()) { return false; }

		MultiblockHeatExchanger exchanger = getExchangerController();
		return exchanger.canFill(s_ExchangerIndices[m_PortDirection.ordinal()],  fluid);
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid) {
		if(!isOutlet() || from != getOutwardsDir()) { return false; }

		MultiblockHeatExchanger exchanger = getExchangerController();
		return exchanger.canDrain(s_ExchangerIndices[m_PortDirection.ordinal()], fluid);
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from) {
		if(from != getOutwardsDir()) { return s_NoTanks; }

		// TODO: Pick tank based on direction setting
		MultiblockHeatExchanger exchanger = getExchangerController();
		return exchanger.getSingleTankInfoArray(s_ExchangerIndices[m_PortDirection.ordinal()]);
	}
}

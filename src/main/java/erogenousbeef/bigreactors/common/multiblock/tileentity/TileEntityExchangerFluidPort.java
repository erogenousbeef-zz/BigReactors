package erogenousbeef.bigreactors.common.multiblock.tileentity;

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
			if(!isInlet()) {
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
		if(worldObj.isRemote || isInlet()) {
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
		if(isOutlet() && m_PumpDestination != null) {
			// TODO: Pump shit
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
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource,
			boolean doDrain) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from) {
		// TODO Auto-generated method stub
		return null;
	}

	
	
}

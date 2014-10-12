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
import erogenousbeef.bigreactors.common.multiblock.helpers.CoolantContainer;
import erogenousbeef.bigreactors.common.multiblock.interfaces.INeighborUpdatableEntity;
import erogenousbeef.bigreactors.common.multiblock.interfaces.ITickableMultiblockPart;
import erogenousbeef.core.multiblock.MultiblockControllerBase;

public class TileEntityReactorCoolantPort extends TileEntityReactorPart implements IFluidHandler, INeighborUpdatableEntity, ITickableMultiblockPart {

	boolean inlet;
	IFluidHandler pumpDestination;
	
	public TileEntityReactorCoolantPort() {
		super();
		
		inlet = true;
		pumpDestination = null;
	}
	
	public boolean isInlet() { return inlet; }

	public void setInlet(boolean shouldBeInlet, boolean markDirty) {
		if(inlet == shouldBeInlet) { return; }

		inlet = shouldBeInlet;
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		
		if(!worldObj.isRemote) {
			if(!inlet) {
				checkForAdjacentTank();
			}
			else {
				pumpDestination = null;
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
	
	// MultiblockTileEntityBase
	@Override
	protected void encodeDescriptionPacket(NBTTagCompound packetData) {
		super.encodeDescriptionPacket(packetData);
		
		packetData.setBoolean("inlet", inlet);
	}
	
	@Override
	protected void decodeDescriptionPacket(NBTTagCompound packetData) {
		super.decodeDescriptionPacket(packetData);
		
		if(packetData.hasKey("inlet")) {
			setInlet(packetData.getBoolean("inlet"), false);
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
		pumpDestination = null;
		
		if(worldObj.isRemote) {
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		}
	}
	
	// TileEntity
	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		
		if(tag.hasKey("inlet")) {
			inlet = tag.getBoolean("inlet");
		}
	}
	
	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		tag.setBoolean("inlet", inlet);
	}

	// IFluidHandler
	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
		if(!isConnected() || !inlet || from != getOutwardsDir()) { return 0; }
		
		CoolantContainer cc = getReactorController().getCoolantContainer();
		return cc.fill(getConnectedTank(), resource, doFill);
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource,
			boolean doDrain) {
		if(!isConnected() || from != getOutwardsDir()) { return null; }

		CoolantContainer cc = getReactorController().getCoolantContainer();
		return cc.drain(getConnectedTank(), resource, doDrain);
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
		if(!isConnected() || from != getOutwardsDir()) { return null; }
		CoolantContainer cc = getReactorController().getCoolantContainer();
		return cc.drain(getConnectedTank(), maxDrain, doDrain);
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid) {
		if(!isConnected() || from != getOutwardsDir()) { return false; }

		if(!inlet) { return false; } // Prevent pipes from filling up the output tank inadvertently

		CoolantContainer cc = getReactorController().getCoolantContainer();
		return cc.canFill(getConnectedTank(), fluid);
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid) {
		if(!isConnected() || from != getOutwardsDir()) { return false; }
		CoolantContainer cc = getReactorController().getCoolantContainer();
		return cc.canDrain(getConnectedTank(), fluid);
	}

	private static FluidTankInfo[] emptyTankArray = new FluidTankInfo[0];
	
	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from) {
		if(!isConnected() || from != getOutwardsDir()) { return emptyTankArray; }

		CoolantContainer cc = getReactorController().getCoolantContainer();
		return cc.getTankInfo(getConnectedTank());
	}
	
	// ITickableMultiblockPart
	
	@Override
	public void onMultiblockServerTick() {
		// Try to pump steam out, if an outlet
		if(pumpDestination == null || isInlet())
			return;

		CoolantContainer cc = getReactorController().getCoolantContainer();
		FluidStack fluidToDrain = cc.drain(CoolantContainer.HOT, cc.getCapacity(), false);
		
		if(fluidToDrain != null && fluidToDrain.amount > 0)
		{
			fluidToDrain.amount = pumpDestination.fill(getOutwardsDir().getOpposite(), fluidToDrain, true);
			cc.drain(CoolantContainer.HOT, fluidToDrain, true);
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

	// Private Helpers
	private int getConnectedTank() {
		if(inlet) {
			return CoolantContainer.COLD;
		}
		else {
			return CoolantContainer.HOT;
		}
	}

	protected void checkForAdjacentTank()
	{
		pumpDestination = null;
		if(worldObj.isRemote || isInlet()) {
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

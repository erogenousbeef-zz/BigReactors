package erogenousbeef.bigreactors.common.multiblock.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import erogenousbeef.bigreactors.common.multiblock.helpers.CoolantContainer;

public class TileEntityReactorCoolantPort extends TileEntityReactorPart implements IFluidHandler {

	boolean inlet;
	
	public TileEntityReactorCoolantPort() {
		super();
		
		inlet = true;
	}
	
	public boolean isInlet() { return inlet; }

	public void setInlet(boolean shouldBeInlet) {
		if(inlet == shouldBeInlet) { return; }

		inlet = shouldBeInlet;
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
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
			inlet = packetData.getBoolean("inlet");
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
		if(!isConnected() || from != getOutwardsDir().getOpposite()) { return 0; }
		
		if(!inlet) { return 0; }

		CoolantContainer cc = getReactorController().getCoolantContainer();
		return cc.fill(getConnectedTank(), resource, doFill);
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource,
			boolean doDrain) {
		if(!isConnected() || from != getOutwardsDir().getOpposite()) { return null; }

		CoolantContainer cc = getReactorController().getCoolantContainer();
		return cc.drain(getConnectedTank(), resource, doDrain);
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
		if(!isConnected() || from != getOutwardsDir().getOpposite()) { return null; }
		CoolantContainer cc = getReactorController().getCoolantContainer();
		return cc.drain(getConnectedTank(), maxDrain, doDrain);
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid) {
		if(!isConnected() || from != getOutwardsDir().getOpposite()) { return false; }

		if(!inlet) { return false; } // Prevent pipes from filling up the output tank inadvertently

		CoolantContainer cc = getReactorController().getCoolantContainer();
		return cc.canFill(getConnectedTank(), fluid);
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid) {
		if(!isConnected() || from != getOutwardsDir().getOpposite()) { return false; }
		CoolantContainer cc = getReactorController().getCoolantContainer();
		return cc.canDrain(getConnectedTank(), fluid);
	}

	private static FluidTankInfo[] emptyTankArray = new FluidTankInfo[0];
	
	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from) {
		if(!isConnected() || from != getOutwardsDir().getOpposite()) { return emptyTankArray; }

		CoolantContainer cc = getReactorController().getCoolantContainer();
		return cc.getTankInfo(getConnectedTank());
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
}

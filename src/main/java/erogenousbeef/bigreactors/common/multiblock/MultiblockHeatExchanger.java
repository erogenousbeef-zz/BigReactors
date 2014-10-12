package erogenousbeef.bigreactors.common.multiblock;

import cofh.api.energy.IEnergyHandler;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidTankInfo;
import erogenousbeef.bigreactors.common.interfaces.IMultipleFluidHandler;
import erogenousbeef.bigreactors.common.multiblock.interfaces.IActivateable;
import erogenousbeef.core.multiblock.IMultiblockPart;
import erogenousbeef.core.multiblock.MultiblockControllerBase;
import erogenousbeef.core.multiblock.rectangular.RectangularMultiblockControllerBase;

public class MultiblockHeatExchanger extends
		RectangularMultiblockControllerBase 
		implements IActivateable, IMultipleFluidHandler {

	public MultiblockHeatExchanger(World world) {
		super(world);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onAttachedPartWithMultiblockData(IMultiblockPart part,
			NBTTagCompound data) {
		// TODO Auto-generated method stub
	}

	@Override
	protected void onBlockAdded(IMultiblockPart newPart) {
		// TODO Auto-generated method stub
	}

	@Override
	protected void onBlockRemoved(IMultiblockPart oldPart) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onMachineAssembled() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onMachineRestored() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onMachinePaused() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onMachineDisassembled() {
		// TODO Auto-generated method stub

	}

	@Override
	protected int getMinimumNumberOfBlocksForAssembledMachine() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected int getMaximumXSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected int getMaximumZSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected int getMaximumYSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected void onAssimilate(MultiblockControllerBase assimilated) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onAssimilated(MultiblockControllerBase assimilator) {
		// TODO Auto-generated method stub

	}

	@Override
	protected boolean updateServer() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void updateClient() {
		// TODO Auto-generated method stub

	}

	@Override
	public void writeToNBT(NBTTagCompound data) {
		// TODO Auto-generated method stub

	}

	@Override
	public void readFromNBT(NBTTagCompound data) {
		// TODO Auto-generated method stub

	}

	@Override
	public void formatDescriptionPacket(NBTTagCompound data) {
		// TODO Auto-generated method stub

	}

	@Override
	public void decodeDescriptionPacket(NBTTagCompound data) {
		// TODO Auto-generated method stub

	}

	@Override
	public FluidTankInfo[] getTankInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean getActive() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setActive(boolean active) {
		// TODO Auto-generated method stub
		
	}

	public String getDebugInfo() {
		// TODO
		return "TODO";
	}
}

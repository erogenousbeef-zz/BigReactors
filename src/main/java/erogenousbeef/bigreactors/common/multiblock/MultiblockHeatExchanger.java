package erogenousbeef.bigreactors.common.multiblock;

import java.util.HashSet;

import cofh.api.energy.IEnergyHandler;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidTankInfo;
import erogenousbeef.bigreactors.common.interfaces.IMultipleFluidHandler;
import erogenousbeef.bigreactors.common.multiblock.helpers.CoolantContainer;
import erogenousbeef.bigreactors.common.multiblock.interfaces.IActivateable;
import erogenousbeef.bigreactors.common.multiblock.interfaces.ITickableMultiblockPart;
import erogenousbeef.core.multiblock.IMultiblockPart;
import erogenousbeef.core.multiblock.MultiblockControllerBase;
import erogenousbeef.core.multiblock.rectangular.RectangularMultiblockControllerBase;

public class MultiblockHeatExchanger extends
		RectangularMultiblockControllerBase 
		implements IActivateable, IMultipleFluidHandler {

	private HashSet<ITickableMultiblockPart> m_TickableParts;
	private boolean m_Active;
	
	private CoolantContainer m_Primary;
	private CoolantContainer m_Secondary;
	private CoolantContainer[] m_Containers;

	public MultiblockHeatExchanger(World world) {
		super(world);
		m_TickableParts = new HashSet<ITickableMultiblockPart>();
		m_Active = false;
		m_Primary = new CoolantContainer();
		m_Secondary = new CoolantContainer();
		m_Containers = new CoolantContainer[] { m_Primary, m_Secondary };
	}

	@Override
	public void onAttachedPartWithMultiblockData(IMultiblockPart part,
			NBTTagCompound data) {
		// TODO Auto-generated method stub
	}

	@Override
	protected void onBlockAdded(IMultiblockPart newPart) {
		if(newPart instanceof ITickableMultiblockPart) {
			m_TickableParts.add((ITickableMultiblockPart)newPart);
		}
	}

	@Override
	protected void onBlockRemoved(IMultiblockPart oldPart) {
		m_TickableParts.remove(oldPart);
	}

	@Override
	protected void onMachineAssembled() {
	}

	@Override
	protected void onMachineRestored() {
	}

	@Override
	protected void onMachinePaused() {
	}

	@Override
	protected void onMachineDisassembled() {
		setActive(false);
	}

	@Override
	protected int getMinimumNumberOfBlocksForAssembledMachine() {
		return 36; // 4 x 3 x 3, which translates to a filled 2x1x1 interior
	}

	@Override
	protected int getMaximumXSize() {
		return 15; // TODO: Setting
	}

	@Override
	protected int getMaximumZSize() {
		return 15; // TODO: Setting
	}

	@Override
	protected int getMaximumYSize() {
		return 32; // TODO: Setting
	}

	@Override
	protected void onAssimilate(MultiblockControllerBase assimilated) {
	}

	@Override
	protected void onAssimilated(MultiblockControllerBase assimilator) {
	}

	@Override
	protected boolean updateServer() {
		return false;
	}

	@Override
	protected void updateClient() {
	}

	@Override
	public void writeToNBT(NBTTagCompound data) {
		data.setBoolean("active", m_Active);
	}

	@Override
	public void readFromNBT(NBTTagCompound data) {
		if(data.hasKey("active")) {
			setActive(data.getBoolean("active"));
		}
	}

	@Override
	public void formatDescriptionPacket(NBTTagCompound data) {
		writeToNBT(data);
	}

	@Override
	public void decodeDescriptionPacket(NBTTagCompound data) {
		readFromNBT(data);
	}

	@Override
	public FluidTankInfo[] getTankInfo() {
		FluidTankInfo[] tankInfo = new FluidTankInfo[4];
		for(int i = 0; i < 2; i++) {
			for(int j = 0; j < 2; j++) {
				tankInfo[i * 2 + j] = m_Containers[i].getSingleTankInfo(j);
			}
		}

		return tankInfo;
	}

	@Override
	public boolean getActive() {
		return m_Active;
	}

	@Override
	public void setActive(boolean active) {
		if(active != m_Active) {
			m_Active = active;
			markReferenceCoordDirty();
		}
	}

	public String getDebugInfo() {
		// TODO
		return "TODO";
	}
}

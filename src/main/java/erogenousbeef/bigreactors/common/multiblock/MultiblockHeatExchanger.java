package erogenousbeef.bigreactors.common.multiblock;

import java.util.HashSet;

import cofh.api.energy.IEnergyHandler;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidTankInfo;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.interfaces.IMultipleFluidHandler;
import erogenousbeef.bigreactors.common.multiblock.block.BlockExchangerPart;
import erogenousbeef.bigreactors.common.multiblock.helpers.CondenserContainer;
import erogenousbeef.bigreactors.common.multiblock.helpers.CoolantContainer;
import erogenousbeef.bigreactors.common.multiblock.helpers.SteamEvapContainer;
import erogenousbeef.bigreactors.common.multiblock.interfaces.IActivateable;
import erogenousbeef.bigreactors.common.multiblock.interfaces.ITickableMultiblockPart;
import erogenousbeef.core.common.CoordTriplet;
import erogenousbeef.core.multiblock.IMultiblockPart;
import erogenousbeef.core.multiblock.MultiblockControllerBase;
import erogenousbeef.core.multiblock.MultiblockValidationException;
import erogenousbeef.core.multiblock.rectangular.RectangularMultiblockControllerBase;

public class MultiblockHeatExchanger extends
		RectangularMultiblockControllerBase 
		implements IActivateable, IMultipleFluidHandler {

	private HashSet<ITickableMultiblockPart> m_TickableParts;
	private boolean m_Active;
	
	private CondenserContainer m_Condenser;
	private SteamEvapContainer m_Evaporator;

	public MultiblockHeatExchanger(World world) {
		super(world);
		m_TickableParts = new HashSet<ITickableMultiblockPart>();
		m_Active = false;
		m_Condenser = new CondenserContainer();
		m_Evaporator = new SteamEvapContainer();
	}

	// Ensure that heat/steam pipes only have two connections.
	@Override
	protected void isBlockGoodForInterior(World world, int x, int y, int z) throws MultiblockValidationException {
		Block b = world.getBlock(x, y, z);
		if(b == BigReactors.blockExchangerInteriorPart) {
			// Check neighbors
			CoordTriplet center = new CoordTriplet(x, y, z);
			int connectedAdjoiningBlocks = 0;
			int metadata = world.getBlockMetadata(x, y, z);

			for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
				CoordTriplet c = center.copy();
				c.translate(dir);
				
				Block adjacentBlock = world.getBlock(c.x, c.y, c.z);
				if(adjacentBlock == BigReactors.blockExchangerInteriorPart || adjacentBlock == BigReactors.blockExchangerPart) {
					int adjacentMetadata = world.getBlockMetadata(c.x, c.y, c.z);
					if( (adjacentBlock == BigReactors.blockExchangerInteriorPart && adjacentMetadata == metadata) ||
						(adjacentBlock == BigReactors.blockExchangerPart && adjacentMetadata == BlockExchangerPart.METADATA_FLUIDPORT)) {
						// Heat pipes connect to adjacent heat pipes of the same type or fluid ports
						connectedAdjoiningBlocks++;
					}
				}
			}

			if(connectedAdjoiningBlocks != 2) {
				throw new MultiblockValidationException(String.format("%d, %d, %d - Exchanger pipes must only connect to two other blocks", x, y, z));
			}
		}
		else {
			super.isBlockGoodForInterior(world, x, y, z);
		}
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
		return 34; // 4 x 3 x 3, with a 2x1x1 core
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
		tankInfo[0] = m_Condenser.getSingleTankInfo(CondenserContainer.HOT);
		tankInfo[1] = m_Condenser.getSingleTankInfo(CondenserContainer.COLD);
		tankInfo[2] = m_Evaporator.getSingleTankInfo(SteamEvapContainer.STEAM);
		tankInfo[3] = m_Evaporator.getSingleTankInfo(SteamEvapContainer.WATER);
		
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

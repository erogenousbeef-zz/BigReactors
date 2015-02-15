package erogenousbeef.bigreactors.common.multiblock;

import java.util.HashSet;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import erogenousbeef.bigreactors.api.IHeatEntity;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.interfaces.IMultipleFluidHandler;
import erogenousbeef.bigreactors.common.multiblock.block.BlockExchangerInteriorPart;
import erogenousbeef.bigreactors.common.multiblock.block.BlockExchangerPart;
import erogenousbeef.bigreactors.common.multiblock.helpers.CondenserContainer;
import erogenousbeef.bigreactors.common.multiblock.helpers.FluidHelper;
import erogenousbeef.bigreactors.common.multiblock.helpers.SteamEvapContainer;
import erogenousbeef.bigreactors.common.multiblock.helpers.ThermalHelper;
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
	private int m_AdjacentDifferingPipes;
	private int m_CondenserPipes;
	private int m_EvaporatorPipes;

	private boolean m_Active;
	
	private CondenserContainer 	m_Condenser;
	private SteamEvapContainer 	m_Evaporator;
	private ThermalHelper 		m_HeatBuffer;
	
	public static final int CONDENSER_INLET = 0;
	public static final int CONDENSER_OUTLET = 1;
	public static final int EVAPORATOR_INLET = 2;
	public static final int EVAPORATOR_OUTLET = 3;
	
	private static final int[] s_TankMappings = {
		CondenserContainer.HOT,
		CondenserContainer.COLD,
		SteamEvapContainer.WATER,
		SteamEvapContainer.STEAM
	};
	
	public MultiblockHeatExchanger(World world) {
		super(world);
		m_TickableParts = new HashSet<ITickableMultiblockPart>();
		m_Active = false;
		m_Condenser = new CondenserContainer();
		m_Evaporator = new SteamEvapContainer();
		m_HeatBuffer = new ThermalHelper();

		m_AdjacentDifferingPipes = 0;
		m_CondenserPipes = 0;
		m_EvaporatorPipes = 0;
	}
	
	@Override
	protected void isMachineWhole() throws MultiblockValidationException {
		// Hijack the validation process to figure out how much contact
		// exists between the condenser & evaporator pipes.
		m_AdjacentDifferingPipes = 0;
		m_EvaporatorPipes = 0;
		m_CondenserPipes = 0;
		super.isMachineWhole();
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
			
			if(metadata == BlockExchangerInteriorPart.METADATA_PRIMARY)
				m_CondenserPipes++;
			else
				m_EvaporatorPipes++;

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
					
					if(adjacentBlock == BigReactors.blockExchangerInteriorPart && adjacentMetadata != metadata) {
						m_AdjacentDifferingPipes++;
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
	public void onAttachedPartWithMultiblockData(IMultiblockPart part, NBTTagCompound data) {
		readFromNBT(data);
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
	
	private void recalculateCoefficients() {
		// Precondition: At this point, validation has completed and m_AdjacentDifferingPipes
		// contains 2 * the number of contact points between pipes.
		// (This is because each contact will be detected twice - once from each direction.)
		
		int totalPipes = m_CondenserPipes + m_EvaporatorPipes;

		// Calculate steam tank heat transfer coefficient
		// Best Possible Value = 8 * Total Number of Pipes
		// 8 = 2 detections * 4 possible contacting faces

		float optimalValue = totalPipes * 8f;
		float baseCoefficient = (float)m_AdjacentDifferingPipes / optimalValue;
		
		// Clamp between air and perfect transfer. Air is min to prevent weird-ass shit and
		// assume that some heat propagates (v. inefficiently) through the empty space anyway.
		baseCoefficient = Math.min(1f, Math.max(IHeatEntity.conductivityAir, baseCoefficient));

		// Note: While correct, this is kind of a double penalty for building poorly.
		// Test to see if this is fun. Possibly always set one of these to 1?
		m_Condenser.setRfTransferCoefficient(baseCoefficient);
		m_Evaporator.setRfTransferCoefficient(baseCoefficient);
		
		// Calculate mass of thermal buffer based on total number of pipes present
		m_HeatBuffer.setMass(totalPipes * 2); // TODO: Some balanced constant for thermal mass?
		// TODO: Maybe allow additional mass by adding random-ass metal blocks?
	
		// Calculate size of condenser and evaporator tanks, based on individual pipe lengths
		m_Condenser.setCapacity(m_CondenserPipes * 1000);
		m_Evaporator.setCapacity(m_EvaporatorPipes * 1000);
	}

	@Override
	protected void onMachineAssembled() {
		recalculateCoefficients();
	}

	@Override
	protected void onMachineRestored() {
		recalculateCoefficients();
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
		MultiblockHeatExchanger assimilatedExchanger = (MultiblockHeatExchanger)assimilated;
		m_Condenser.merge(assimilatedExchanger.m_Condenser);
		m_Evaporator.merge(assimilatedExchanger.m_Evaporator);
		m_HeatBuffer.merge(assimilatedExchanger.m_HeatBuffer);
		m_Active |= assimilatedExchanger.m_Active;
	}

	@Override
	protected void onAssimilated(MultiblockControllerBase assimilator) {
	}

	@Override
	protected boolean updateServer() {
		boolean changed = false;

		// Heat ThermalHelper from Condenser
		changed |= 0f != m_Condenser.condense(m_HeatBuffer);

		// Cool ThermalHelper via Evaporator
		changed |= 0f != m_Evaporator.evaporate(m_HeatBuffer);

		return changed;
	}

	@Override
	protected void updateClient() {
	}

	@Override
	public void writeToNBT(NBTTagCompound data) {
		data.setBoolean("active", m_Active);
		data.setTag("condenser", m_Condenser.writeToNBT(new NBTTagCompound()));
		data.setTag("evaporator", m_Evaporator.writeToNBT(new NBTTagCompound()));
		data.setFloat("heat", m_HeatBuffer.getRf());
	}

	@Override
	public void readFromNBT(NBTTagCompound data) {
		if(data.hasKey("active")) {
			setActive(data.getBoolean("active"));
		}
		
		if(data.hasKey("heat")) {
			m_HeatBuffer.setRf(data.getFloat("heat"));
		}
		
		if(data.hasKey("condenser")) {
			m_Condenser.readFromNBT((NBTTagCompound)data.getTag("condenser"));
		}
		
		if(data.hasKey("evaporator")) {
			m_Evaporator.readFromNBT((NBTTagCompound)data.getTag("evaporator"));
		}
	}

	@Override
	public void formatDescriptionPacket(NBTTagCompound data) {
		writeToNBT(data);
		data.setInteger("steamProducedLastTick", m_Evaporator.getSteamProducedLastTick());
		data.setInteger("fluidCondensedLastTick", m_Condenser.getFluidCondensedLastTick());
	}

	@Override
	public void decodeDescriptionPacket(NBTTagCompound data) {
		readFromNBT(data);
		if(data.hasKey("steamProducedLastTick")) {
			m_Evaporator.setSteamProducedLastTick(data.getInteger("steamProducedLastTick"));
		}
		else {
			m_Evaporator.setSteamProducedLastTick(0);
		}
		
		if(data.hasKey("fluidCondensedLastTick")) {
			m_Condenser.setFluidCondensedLastTick(data.getInteger("fluidCondensedLastTick"));
		}
		else {
			m_Condenser.setFluidCondensedLastTick(0);
		}
	}

	@Override
	public FluidTankInfo[] getTankInfo() {
		FluidTankInfo[] tankInfo = new FluidTankInfo[s_TankMappings.length];
		for(int i = 0; i < s_TankMappings.length; i++) {
			tankInfo[i] = getSingleTankInfo(i);
		}
		
		return tankInfo;
	}
	
	public int getFluidAmount(int idx) {
		FluidHelper helper = getHelperFromPublicIdx(idx);
		return helper.getFluidAmount(s_TankMappings[idx]);
	}
	
	public int getCapacity(int idx) {
		FluidHelper helper = getHelperFromPublicIdx(idx);
		return helper.getCapacity();
	}

	public boolean canDrain(int idx, Fluid fluid) {
		FluidHelper helper = getHelperFromPublicIdx(idx);
		return helper.canDrain(s_TankMappings[idx], fluid);
	}
	
	public boolean canFill(int idx, Fluid fluid) {
		FluidHelper helper = getHelperFromPublicIdx(idx);
		return helper.canFill(s_TankMappings[idx], fluid);
	}
	
	public int fill(int idx, FluidStack resource, boolean doFill) {
		FluidHelper helper = getHelperFromPublicIdx(idx);
		return helper.fill(s_TankMappings[idx], resource, doFill);
	}
	
	public FluidStack drain(int idx, FluidStack resource, boolean doDrain) {
		FluidHelper helper = getHelperFromPublicIdx(idx);
		return helper.drain(s_TankMappings[idx], resource, doDrain);
	}
	
	public FluidStack drain(int idx, int amount, boolean doDrain) {
		FluidHelper helper = getHelperFromPublicIdx(idx);
		return helper.drain(s_TankMappings[idx], amount, doDrain);
	}
	
	public FluidTankInfo getSingleTankInfo(int idx) {
		FluidHelper helper = getHelperFromPublicIdx(idx);
		return helper.getSingleTankInfo(s_TankMappings[idx]);
	}
	
	public FluidTankInfo[] getSingleTankInfoArray(int idx) {
		FluidHelper helper = getHelperFromPublicIdx(idx);
		return helper.getSingleTankInfoArray(s_TankMappings[idx]);
	}

	// Return any unused fluid after a drain operation. There should not be any remainder.
	// For safety's sake, any remainder will be discarded.
	public void returnFluid(int idx, FluidStack fluidToReturn) {
		FluidHelper helper = getHelperFromPublicIdx(idx);
		helper.fill(s_TankMappings[idx], fluidToReturn, true);
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
	
	private FluidHelper getHelperFromPublicIdx(int idx) {
		if(idx < 2)
			return m_Condenser;
		else
			return m_Evaporator;
	}
}

package erogenousbeef.bigreactors.common.multiblock.tileentity;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.IFluidBlock;
import cofh.lib.util.helpers.ItemHelper;
import erogenousbeef.bigreactors.api.IHeatEntity;
import erogenousbeef.bigreactors.api.IRadiationModerator;
import erogenousbeef.bigreactors.api.data.ReactorInteriorData;
import erogenousbeef.bigreactors.api.registry.ReactorInterior;
import erogenousbeef.bigreactors.common.data.RadiationData;
import erogenousbeef.bigreactors.common.data.RadiationPacket;
import erogenousbeef.bigreactors.common.multiblock.MultiblockReactor;
import erogenousbeef.bigreactors.common.multiblock.helpers.RadiationHelper;
import erogenousbeef.bigreactors.utils.StaticUtils;
import erogenousbeef.core.multiblock.MultiblockValidationException;
import erogenousbeef.core.multiblock.rectangular.RectangularMultiblockTileEntityBase;

public class TileEntityReactorFuelRod extends TileEntityReactorPartBase implements IRadiationModerator, IHeatEntity {

	public TileEntityReactorFuelRod() {
		super();
	}
	
	// IRadiationModerator
	@Override
	public void moderateRadiation(RadiationData data, RadiationPacket radiation) {
		if(!isConnected()) { return; }

		// Grab control rod insertion and reactor heat
		MultiblockReactor reactor = getReactorController();
		float heat = reactor.getFuelHeat();
		
		int maxY = reactor.getMaximumCoord().y;
		TileEntity te = worldObj.getTileEntity(xCoord, maxY, zCoord);
		if(!(te instanceof TileEntityReactorControlRod)) {
			return;
		}

		// Scale control rod insertion 0..1
		float controlRodInsertion = Math.min(1f, Math.max(0f, ((float)((TileEntityReactorControlRod)te).getControlRodInsertion())/100f));
		
		// Fuel absorptiveness is determined by control rod + a heat modifier.
		// Starts at 1 and decays towards 0.05, reaching 0.6 at 1000 and just under 0.2 at 2000. Inflection point at about 500-600.
		// Harder radiation makes absorption more difficult.
		float baseAbsorption = (float)(1.0 - (0.95 * Math.exp(-10 * Math.exp(-0.0022 * heat)))) * (1f - (radiation.hardness / getFuelHardnessDivisor()));

		// Some fuels are better at absorbing radiation than others
		float scaledAbsorption = Math.min(1f, baseAbsorption * getFuelAbsorptionCoefficient());

		// Control rods increase total neutron absorption, but decrease the total neutrons which fertilize the fuel
		// Absorb up to 50% better with control rods inserted.
		float controlRodBonus = (1f - scaledAbsorption) * controlRodInsertion * 0.5f;
		float controlRodPenalty = scaledAbsorption * controlRodInsertion * 0.5f;
		
		float radiationAbsorbed = (scaledAbsorption + controlRodBonus) * radiation.intensity;
		float fertilityAbsorbed = (scaledAbsorption - controlRodPenalty) * radiation.intensity;
		
		float fuelModerationFactor = getFuelModerationFactor();
		fuelModerationFactor += fuelModerationFactor * controlRodInsertion + controlRodInsertion; // Full insertion doubles the moderation factor of the fuel as well as adding its own level
		
		radiation.intensity = Math.max(0f, radiation.intensity - radiationAbsorbed);
		radiation.hardness /= fuelModerationFactor;
		
		// Being irradiated both heats up the fuel and also enhances its fertility
		data.fuelRfChange += radiationAbsorbed * RadiationHelper.rfPerRadiationUnit;
		data.fuelAbsorbedRadiation += fertilityAbsorbed;
	}

	// 1, upwards. How well does this fuel moderate, but not stop, radiation? Anything under 1.5 is "poor", 2-2.5 is "good", above 4 is "excellent".
	private float getFuelModerationFactor() {
		return 1.5f;
	}

	// 0..1. How well does this fuel absorb radiation?
	private float getFuelAbsorptionCoefficient() {
		// TODO: Lookup type of fuel and get data from there
		return 0.5f;
	}
	
	// Goes up from 1. How tolerant is this fuel of hard radiation?
	private float getFuelHardnessDivisor() {
		return 1.0f;
	}
	
	// IHeatEntity
	@Override
	public float getThermalConductivity() {
		return IHeatEntity.conductivityCopper;
	}

	// RectangularMultiblockTileEntityBase
	@Override
	public void isGoodForFrame() throws MultiblockValidationException {
		throw new MultiblockValidationException(String.format("%d, %d, %d - fuel rods may only be placed in the reactor interior", xCoord, yCoord, zCoord));
	}

	@Override
	public void isGoodForSides() throws MultiblockValidationException {
		throw new MultiblockValidationException(String.format("%d, %d, %d - fuel rods may only be placed in the reactor interior", xCoord, yCoord, zCoord));
	}

	@Override
	public void isGoodForTop() throws MultiblockValidationException {
		throw new MultiblockValidationException(String.format("%d, %d, %d - fuel rods may only be placed in the reactor interior", xCoord, yCoord, zCoord));
	}

	@Override
	public void isGoodForBottom() throws MultiblockValidationException {
		throw new MultiblockValidationException(String.format("%d, %d, %d - fuel rods may only be placed in the reactor interior", xCoord, yCoord, zCoord));
	}

	@Override
	public void isGoodForInterior() throws MultiblockValidationException {
		// Check above and below. Above must be fuel rod or control rod.
		TileEntity entityAbove = this.worldObj.getTileEntity(xCoord, yCoord + 1, zCoord);
		if(!(entityAbove instanceof TileEntityReactorFuelRod || entityAbove instanceof TileEntityReactorControlRod)) {
			throw new MultiblockValidationException(String.format("Fuel rod at %d, %d, %d must be part of a vertical column that reaches the entire height of the reactor, with a control rod on top.", xCoord, yCoord, zCoord));
		}

		// Below must be fuel rod or the base of the reactor.
		TileEntity entityBelow = this.worldObj.getTileEntity(xCoord, yCoord - 1, zCoord);
		if(entityBelow instanceof TileEntityReactorFuelRod) {
			return;
		}
		else if(entityBelow instanceof RectangularMultiblockTileEntityBase) {
			((RectangularMultiblockTileEntityBase)entityBelow).isGoodForBottom();
			return;
		}
		
		throw new MultiblockValidationException(String.format("Fuel rod at %d, %d, %d must be part of a vertical column that reaches the entire height of the reactor, with a control rod on top.", xCoord, yCoord, zCoord));
	}

	@Override
	public void onMachineActivated() {
	}

	@Override
	public void onMachineDeactivated() {
	}

	// Reactor information retrieval methods
	
	/**
	 * Returns the rate of heat transfer from this block to the reactor environment, based on this block's surrounding blocks.
	 * Note that this method queries the world, so use it sparingly.
	 * 
	 * @return Heat transfer rate from fuel rod to reactor environment, in Centigrade per tick.
	 */
	public float getHeatTransferRate() {
		float heatTransferRate = 0f;

		TileEntity te;
		for(ForgeDirection dir: StaticUtils.CardinalDirections) {
			te = worldObj.getTileEntity(xCoord + dir.offsetX, yCoord + dir.offsetY, zCoord + dir.offsetZ);
			if(te instanceof TileEntityReactorFuelRod) {
				// We don't transfer to other fuel rods, due to heat pooling.
				continue;
			}
			else if(te instanceof IHeatEntity) {
				heatTransferRate += ((IHeatEntity)te).getThermalConductivity();
			}
			else if(worldObj.isAirBlock(xCoord + dir.offsetX, yCoord + dir.offsetY, zCoord + dir.offsetZ)) {
				heatTransferRate += IHeatEntity.conductivityAir;
			}
			else {

				Block block = worldObj.getBlock(xCoord + dir.offsetX, yCoord + dir.offsetY, zCoord + dir.offsetZ);
				int metadata = worldObj.getBlockMetadata(xCoord + dir.offsetX, yCoord + dir.offsetY, zCoord + dir.offsetZ);
				heatTransferRate += getConductivityFromBlock(block, metadata);
			}
		}

		return heatTransferRate;
	}
	
	private float getConductivityFromBlock(Block block, int metadata) {
		ReactorInteriorData interiorData = null;
		
		if(block == Blocks.iron_block) {
			interiorData = ReactorInterior.getBlockData("blockIron");
		}
		else if(block == Blocks.gold_block) {
			interiorData = ReactorInterior.getBlockData("blockGold");
		}
		else if(block == Blocks.diamond_block) {
			interiorData = ReactorInterior.getBlockData("blockDiamond");
		}
		else if(block == Blocks.emerald_block) {
			interiorData = ReactorInterior.getBlockData("blockEmerald");
		}
		else {
			interiorData = ReactorInterior.getBlockData(ItemHelper.oreProxy.getOreName(new ItemStack(block, 1, metadata)));

			if(interiorData == null && block instanceof IFluidBlock) {
				Fluid fluid = ((IFluidBlock)block).getFluid();
				if(fluid != null) {
					interiorData = ReactorInterior.getFluidData(fluid.getName());
				}
			}
		}
		
		if(interiorData == null) {
			interiorData = RadiationHelper.airData;
		}
		
		return interiorData.heatConductivity;
	}
}

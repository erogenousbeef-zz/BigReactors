package erogenousbeef.bigreactors.common.multiblock.tileentity.creative;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import erogenousbeef.bigreactors.common.multiblock.MultiblockTurbine;
import erogenousbeef.bigreactors.common.multiblock.interfaces.ITickableMultiblockPart;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityTurbinePartStandard;

public class TileEntityTurbineCreativeSteamGenerator extends TileEntityTurbinePartStandard implements ITickableMultiblockPart {

	public TileEntityTurbineCreativeSteamGenerator() {
		super();
	}

	@Override
	public void onMultiblockServerTick() {
		if(isConnected() && getTurbine().isActive()) {
			Fluid steam = FluidRegistry.getFluid("steam");
			
			getTurbine().fill(MultiblockTurbine.TANK_INPUT, new FluidStack(steam, getTurbine().getMaxIntakeRate()), true);
		}
	}
}

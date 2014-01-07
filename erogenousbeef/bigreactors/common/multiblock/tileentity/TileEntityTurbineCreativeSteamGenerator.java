package erogenousbeef.bigreactors.common.multiblock.tileentity;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import erogenousbeef.bigreactors.common.multiblock.MultiblockTurbine;
import erogenousbeef.bigreactors.common.multiblock.interfaces.ITickableMultiblockPart;

public class TileEntityTurbineCreativeSteamGenerator extends
		TileEntityTurbinePart implements ITickableMultiblockPart {

	public TileEntityTurbineCreativeSteamGenerator() {
		super();
	}

	public TileEntityTurbineCreativeSteamGenerator(int metadata) {
		super(metadata);
	}

	@Override
	public void onMultiblockServerTick() {
		if(isConnected() && getTurbine().isActive()) {
			Fluid steam = FluidRegistry.getFluid("steam");
			
			// TODO: Replace 200 with measured number. How much steam can you duct?
			getTurbine().fill(MultiblockTurbine.TANK_INPUT, new FluidStack(steam, 200), true);
		}
	}
}

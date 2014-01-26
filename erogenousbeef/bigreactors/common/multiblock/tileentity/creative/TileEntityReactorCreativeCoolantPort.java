package erogenousbeef.bigreactors.common.multiblock.tileentity.creative;

import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import erogenousbeef.bigreactors.common.multiblock.MultiblockReactor;
import erogenousbeef.bigreactors.common.multiblock.interfaces.ITickableMultiblockPart;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorCoolantPort;

public class TileEntityReactorCreativeCoolantPort extends TileEntityReactorCoolantPort implements ITickableMultiblockPart {

	public TileEntityReactorCreativeCoolantPort() {
		super();
	}

	@Override
	public void onMultiblockServerTick() {
		if(!isConnected()) { return; }
		
		MultiblockReactor reactor = getReactorController();

		if(!reactor.isActive()) { return; }
		
		if(isInlet()) {
			reactor.getCoolantContainer().addCoolant(new FluidStack(FluidRegistry.WATER, 100));
		}
		else {
			reactor.getCoolantContainer().emptyVapor();
		}
	}
}

package erogenousbeef.bigreactors.common.multiblock.tileentity.creative;

import cpw.mods.fml.common.FMLLog;
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

		if(isInlet()) {
			reactor.getCoolantContainer().addCoolant(new FluidStack(FluidRegistry.WATER, 100));
		}
		else {
			reactor.getCoolantContainer().emptyVapor();
		}
	}
	
	public void forceAddWater() {
		if(!isConnected()) { return; }
		
		MultiblockReactor reactor = getReactorController();
		FMLLog.info("force adding 1000mB of water");
		reactor.getCoolantContainer().addCoolant(new FluidStack(FluidRegistry.WATER, 1000));
	}
}

package erogenousbeef.bigreactors.common;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;
import erogenousbeef.core.multiblock.MultiblockTickHandler;

public class CommonProxy {

	public void preInit() {
	}

	public void init() {
		BigReactors.registerTileEntities();
		
		NetworkRegistry.instance().registerGuiHandler(BRLoader.instance, null);
		TickRegistry.registerTickHandler(new MultiblockTickHandler(), Side.SERVER);
	}
}

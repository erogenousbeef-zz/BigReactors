package erogenousbeef.bigreactors.common;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;
import erogenousbeef.bigreactors.gui.BigReactorsGUIHandler;
import erogenousbeef.core.multiblock.MultiblockServerTickHandler;

public class CommonProxy {

	public void preInit() {
	}

	public void init() {
		BigReactors.registerTileEntities();
		
		NetworkRegistry.instance().registerGuiHandler(BRLoader.instance, new BigReactorsGUIHandler());
		TickRegistry.registerTickHandler(new MultiblockServerTickHandler(), Side.SERVER);
	}
}

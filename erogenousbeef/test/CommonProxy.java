package erogenousbeef.test;

import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;
import erogenousbeef.core.multiblock.MultiblockServerTickHandler;

public class CommonProxy {

	public void preInit() {
		
	}
	
	public void init() {
		TestMod.registerTileEntities();
		TickRegistry.registerTickHandler(new MultiblockServerTickHandler(), Side.SERVER);
	}
}

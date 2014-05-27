package erogenousbeef.test;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;
import erogenousbeef.core.multiblock.MultiblockClientTickHandler;
import erogenousbeef.core.multiblock.MultiblockServerTickHandler;
import erogenousbeef.test.client.RendererMultiblockTester;
import erogenousbeef.test.common.TileEntityMultiblockTester;

public class ClientProxy extends CommonProxy {

	@Override
	public void preInit() {
		super.preInit();
	}
	
	@Override
	public void init() {
		super.init();
		
		// Bind special renderers here
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMultiblockTester.class, new RendererMultiblockTester());
		TickRegistry.registerTickHandler(new MultiblockClientTickHandler(), Side.CLIENT);
		
		FMLLog.info("Tick handler registered on client");
	}
	
}

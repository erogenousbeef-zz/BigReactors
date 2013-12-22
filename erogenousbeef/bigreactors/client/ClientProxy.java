package erogenousbeef.bigreactors.client;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import erogenousbeef.bigreactors.client.renderer.SimpleRendererControlRod;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.CommonProxy;
import erogenousbeef.bigreactors.common.block.BlockReactorControlRod;
import erogenousbeef.core.multiblock.MultiblockClientTickHandler;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {
	@Override
	public void preInit()
	{

	}

	@Override
	public void init()
	{
		super.init();

		TickRegistry.registerTickHandler(new MultiblockClientTickHandler(), Side.CLIENT);
		
		BlockReactorControlRod.renderId = RenderingRegistry.getNextAvailableRenderId();
		ISimpleBlockRenderingHandler controlRodISBRH = new SimpleRendererControlRod();
		RenderingRegistry.registerBlockHandler(BigReactors.blockReactorControlRod.getRenderType(), controlRodISBRH);
	}
}

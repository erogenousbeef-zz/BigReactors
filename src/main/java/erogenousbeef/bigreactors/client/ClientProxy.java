package erogenousbeef.bigreactors.client;

import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.TextureStitchEvent;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import erogenousbeef.bigreactors.client.renderer.ExchangerPipeSimpleRenderer;
import erogenousbeef.bigreactors.client.renderer.RotorSimpleRenderer;
import erogenousbeef.bigreactors.client.renderer.RotorSpecialRenderer;
import erogenousbeef.bigreactors.client.renderer.SimpleRendererFuelRod;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.CommonProxy;
import erogenousbeef.bigreactors.common.multiblock.MultiblockTurbine;
import erogenousbeef.bigreactors.common.multiblock.block.BlockExchangerInteriorPart;
import erogenousbeef.bigreactors.common.multiblock.block.BlockFuelRod;
import erogenousbeef.bigreactors.common.multiblock.block.BlockTurbineRotorPart;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityTurbineRotorBearing;
import erogenousbeef.bigreactors.gui.BeefGuiIconManager;
import erogenousbeef.core.multiblock.MultiblockClientTickHandler;
import erogenousbeef.core.multiblock.MultiblockControllerBase;
import erogenousbeef.core.multiblock.MultiblockRegistry;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {
	public static BeefGuiIconManager GuiIcons;
	public static CommonBlockIconManager CommonBlockIcons;

	public static long lastRenderTime = Minecraft.getSystemTime();
	
	public ClientProxy() {
		GuiIcons = new BeefGuiIconManager();
		CommonBlockIcons = new CommonBlockIconManager();
	}
	
	@Override
	public void preInit() {}

	@Override
	public void init()
	{
		super.init();

		FMLCommonHandler.instance().bus().register(new MultiblockClientTickHandler());
        FMLCommonHandler.instance().bus().register(new BRRenderTickHandler());

		BlockFuelRod.renderId = RenderingRegistry.getNextAvailableRenderId();
		ISimpleBlockRenderingHandler fuelRodISBRH = new SimpleRendererFuelRod();
		RenderingRegistry.registerBlockHandler(BlockFuelRod.renderId, fuelRodISBRH);
		
		BlockTurbineRotorPart.renderId = RenderingRegistry.getNextAvailableRenderId();
		ISimpleBlockRenderingHandler rotorISBRH = new RotorSimpleRenderer();
		RenderingRegistry.registerBlockHandler(BlockTurbineRotorPart.renderId, rotorISBRH);	
		
		if(BigReactors.blockTurbinePart != null) {
			ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTurbineRotorBearing.class, new RotorSpecialRenderer());
		}
		
		BlockExchangerInteriorPart.renderId = RenderingRegistry.getNextAvailableRenderId();
		ISimpleBlockRenderingHandler exchangerPipeISBRH = new ExchangerPipeSimpleRenderer();
		RenderingRegistry.registerBlockHandler(BlockExchangerInteriorPart.renderId, exchangerPipeISBRH);
	}

	@Override
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void registerIcons(TextureStitchEvent.Pre event) {
		if(event.map.getTextureType() == BeefIconManager.TERRAIN_TEXTURE) {
			BigReactors.registerNonBlockFluidIcons(event.map);
			GuiIcons.registerIcons(event.map);
			CommonBlockIcons.registerIcons(event.map);
		}
		// else if(event.map.textureType == BeefIconManager.ITEM_TEXTURE) { }

		// Reset any controllers which have TESRs which cache displaylists with UV data in 'em
		// This is necessary in case a texture pack changes UV coordinates on us
		Set<MultiblockControllerBase> controllers = MultiblockRegistry.getControllersFromWorld(FMLClientHandler.instance().getClient().theWorld);
		if(controllers != null) {
			for(MultiblockControllerBase controller: controllers) {
				if(controller instanceof MultiblockTurbine) {
					((MultiblockTurbine)controller).resetCachedRotors();
				}
			}
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void setIcons(TextureStitchEvent.Post event) {
		BigReactors.setNonBlockFluidIcons();
	}
}

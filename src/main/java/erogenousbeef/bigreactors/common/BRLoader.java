package erogenousbeef.bigreactors.common;

import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import erogenousbeef.bigreactors.common.data.StandardReactants;
import erogenousbeef.core.multiblock.MultiblockEventHandler;

@Mod(modid = BRLoader.MOD_ID, name = BigReactors.NAME, version = BRConfig.VERSION, acceptedMinecraftVersions = BRConfig.MINECRAFT_VERSION, dependencies = BRLoader.DEPENDENCIES)
public class BRLoader {

	public static final String MOD_ID = BigReactors.MODID;
    public static final String DEPENDENCIES = "required-after:Forge@[10.13.0.1205,);required-after:CoFHCore;after:ThermalExpansion";
	
	@Instance(MOD_ID)
	public static BRLoader instance;

	@SidedProxy(clientSide = "erogenousbeef.bigreactors.client.ClientProxy", serverSide = "erogenousbeef.bigreactors.common.CommonProxy")
	public static CommonProxy proxy;
	
	@Mod.Metadata(MOD_ID)
	public static ModMetadata metadata;
	
	private MultiblockEventHandler multiblockEventHandler;
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		BigReactors.registerOres(0, true);
		BigReactors.registerIngots(0);
		BigReactors.registerFuelRods(0, true);
		BigReactors.registerReactorPartBlocks(0, true);
		BigReactors.registerTurbineParts();
		BigReactors.registerExchangerParts();
		BigReactors.registerDevices(0,  true);
		BigReactors.registerFluids(0,  true);
		BigReactors.registerCreativeParts(0, true);
		BigReactors.registerItems();

		StandardReactants.register();
		
		BigReactors.eventHandler = new BREventHandler();
		MinecraftForge.EVENT_BUS.register(BigReactors.eventHandler);
		MinecraftForge.EVENT_BUS.register(proxy);
		
		multiblockEventHandler = new MultiblockEventHandler();
		MinecraftForge.EVENT_BUS.register(multiblockEventHandler);
		
		proxy.preInit();
		
		Fluid waterFluid = FluidRegistry.WATER; // Force-load water to prevent startup crashes
	}

	@EventHandler
	public void load(FMLInitializationEvent evt)
	{
		proxy.init();
		BigReactors.register(this);
	}
	
	@EventHandler
	public void postInit(FMLPostInitializationEvent evt) {
		proxy.postInit();
	}
	
	@EventHandler
	public void onIMCEvent(FMLInterModComms.IMCEvent event) {
		// TODO
	}
	
	// GAME EVENT HANDLERS
	// FORGE EVENT HANDLERS

	// Handle bucketing of reactor fluids
	@SubscribeEvent
    public void onBucketFill(FillBucketEvent e)
    {
        if(e.current.getItem() != Items.bucket)
        {
            return;
        }
        ItemStack filledBucket = fillBucket(e.world, e.target);
        if(filledBucket != null)
        {
            e.world.setBlockToAir(e.target.blockX, e.target.blockY, e.target.blockZ);
            e.result = filledBucket;
            e.setResult(Result.ALLOW);
        }
    }
    
    private ItemStack fillBucket(World world, MovingObjectPosition mop)
    {
        Block block = world.getBlock(mop.blockX, mop.blockY, mop.blockZ);
        if(block == BigReactors.fluidCyaniteStill) return new ItemStack(BigReactors.fluidCyaniteBucketItem);
        else if(block == BigReactors.fluidYelloriumStill) return new ItemStack(BigReactors.fluidYelloriumBucketItem);
        else return null;
    }
}

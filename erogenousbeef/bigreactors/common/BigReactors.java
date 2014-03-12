package erogenousbeef.bigreactors.common;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.Icon;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import erogenousbeef.bigreactors.api.IHeatEntity;
import erogenousbeef.bigreactors.common.block.BlockBRGenericFluid;
import erogenousbeef.bigreactors.common.block.BlockBRMetal;
import erogenousbeef.bigreactors.common.block.BlockBROre;
import erogenousbeef.bigreactors.common.block.BlockBRSmallMachine;
import erogenousbeef.bigreactors.common.data.ReactorFuel;
import erogenousbeef.bigreactors.common.data.ReactorSolidMapping;
import erogenousbeef.bigreactors.common.item.ItemBRBucket;
import erogenousbeef.bigreactors.common.item.ItemBlockBigReactors;
import erogenousbeef.bigreactors.common.item.ItemBlockReactorPart;
import erogenousbeef.bigreactors.common.item.ItemIngot;
import erogenousbeef.bigreactors.common.multiblock.MultiblockReactor;
import erogenousbeef.bigreactors.common.multiblock.block.BlockFuelRod;
import erogenousbeef.bigreactors.common.multiblock.block.BlockMBCreativePart;
import erogenousbeef.bigreactors.common.multiblock.block.BlockMultiblockGlass;
import erogenousbeef.bigreactors.common.multiblock.block.BlockReactorControlRod;
import erogenousbeef.bigreactors.common.multiblock.block.BlockReactorPart;
import erogenousbeef.bigreactors.common.multiblock.block.BlockReactorRedstonePort;
import erogenousbeef.bigreactors.common.multiblock.block.BlockTurbinePart;
import erogenousbeef.bigreactors.common.multiblock.block.BlockTurbineRotorPart;
import erogenousbeef.bigreactors.common.multiblock.helpers.RadiationHelper;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorAccessPort;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorComputerPort;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorControlRod;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorCoolantPort;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorFuelRod;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorGlass;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorPart;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorPowerTap;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorRedNetPort;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorRedstonePort;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityTurbineComputerPort;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityTurbineFluidPort;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityTurbinePartGlass;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityTurbinePartStandard;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityTurbinePowerTap;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityTurbineRotorBearing;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityTurbineRotorPart;
import erogenousbeef.bigreactors.common.multiblock.tileentity.creative.TileEntityReactorCreativeCoolantPort;
import erogenousbeef.bigreactors.common.multiblock.tileentity.creative.TileEntityTurbineCreativeSteamGenerator;
import erogenousbeef.bigreactors.common.tileentity.TileEntityCyaniteReprocessor;
import erogenousbeef.bigreactors.world.BRSimpleOreGenerator;
import erogenousbeef.bigreactors.world.BRWorldGenerator;

public class BigReactors {

	public static final String NAME 	= "Big Reactors";
	public static final String CHANNEL 	= "BigReactors";
	public static final String RESOURCE_PATH = "/assets/bigreactors/";
	
	public static final CreativeTabs TAB = new CreativeTabBR(CreativeTabs.getNextID(), CHANNEL);

	public static final String TEXTURE_NAME_PREFIX = "bigreactors:";
	
	public static final String TEXTURE_DIRECTORY = RESOURCE_PATH + "textures/";
	public static final String GUI_DIRECTORY = TEXTURE_NAME_PREFIX + "textures/gui/";
	public static final String BLOCK_TEXTURE_DIRECTORY = TEXTURE_DIRECTORY + "blocks/";
	public static final String ITEM_TEXTURE_DIRECTORY = TEXTURE_DIRECTORY + "items/";
	public static final String MODEL_TEXTURE_DIRECTORY = TEXTURE_DIRECTORY + "models/";

	public static final String LANGUAGE_PATH = RESOURCE_PATH + "languages/";
	private static final String[] LANGUAGES_SUPPORTED = new String[] { "en_US" };
	
	public static final int BLOCK_ID_PREFIX = 1750;
	
	public static Block blockYelloriteOre;
	public static BlockBRMetal blockMetal;
	public static Block blockYelloriumFuelRod;
	public static BlockReactorPart blockReactorPart;
	public static Block blockReactorControlRod;
	public static Block blockReactorRedstonePort; // UGH. Why does the redstone API not allow me to check metadata? :(
	
	public static BlockTurbinePart blockTurbinePart;
	public static BlockTurbineRotorPart blockTurbineRotorPart;

	public static BlockMultiblockGlass blockMultiblockGlass;
	public static BlockMBCreativePart blockMultiblockCreativePart;
	
	public static Block blockRadiothermalGen;
	public static Block blockSmallMachine;
	
	public static Block fluidYelloriumStill;
	public static Block fluidCyaniteStill;
	public static Block fluidFuelColumnStill;
	
	// Buckets for bucketing reactor fluids
	public static Item fluidYelloriumBucketItem;
	public static Item fluidCyaniteBucketItem;
	
	public static Fluid fluidYellorium;
	public static Fluid fluidCyanite;
	public static Fluid fluidFuelColumn;
	
	public static Fluid fluidSteam;
	public static boolean registeredOwnSteam;
	
	public static final int defaultFluidColorFuel = 0xbcba50;
	public static final int defaultFluidColorWaste = 0x4d92b5;
	
	public static final int ITEM_ID_PREFIX = 17750;
	public static ItemIngot ingotGeneric;

	public static BRSimpleOreGenerator yelloriteOreGeneration;
	
	public static boolean INITIALIZED = false;
	public static boolean enableWorldGen = true;
	public static boolean enableWorldGenInNegativeDimensions = false;
	public static boolean enableWorldRegeneration = true;
	public static int userWorldGenVersion = 0;

	public static BREventHandler eventHandler = null;
	public static BigReactorsTickHandler tickHandler = null;
	public static BRWorldGenerator worldGenerator = null;
	
	private static boolean registeredTileEntities = false;
	public static int maximumReactorSize = MultiblockReactor.DIMENSION_UNBOUNDED;
	public static int maximumReactorHeight = MultiblockReactor.DIMENSION_UNBOUNDED;
	public static int ticksPerRedstoneUpdate = 20; // Once per second, roughly
	
	public static int maximumTurbineSize = 16;
	public static int maximumTurbineHeight = 32;
	
	public static float powerProductionMultiplier = 1.0f;
	public static float fuelUsageMultiplier = 1.0f;
	
	public static boolean isValentinesDay = false; // Easter Egg :)
	
	// Game Balance values
	
	protected static Icon iconSteamStill;
	protected static Icon iconSteamFlowing;
	protected static Icon iconFuelColumnStill;
	protected static Icon iconFuelColumnFlowing;
	
	private static boolean registerYelloriteSmeltToUranium = true;
	private static boolean registerYelloriumAsUranium = true;
	
	/**
	 * Call this function in your mod init stage.
	 */
	public static void register(Object modInstance)
	{

		if (!INITIALIZED)
		{
			loadLanguages(BigReactors.LANGUAGE_PATH, LANGUAGES_SUPPORTED);

			// General config loading
			BRConfig.CONFIGURATION.load();
			enableWorldGen = BRConfig.CONFIGURATION.get("WorldGen", "enableWorldGen", true, "If false, disables all world gen from Big Reactors; all other worldgen settings are automatically overridden").getBoolean(true);
			enableWorldGenInNegativeDimensions = BRConfig.CONFIGURATION.get("WorldGen", "enableWorldGenInNegativeDims", false, "Run BR world generation in negative dimension IDs? (default: false) If you don't know what this is, leave it alone.").getBoolean(false);
			enableWorldRegeneration = BRConfig.CONFIGURATION.get("WorldGen", "enableWorldRegeneration", false, "Run BR World Generation in chunks that have already been generated, but have not been modified by Big Reactors before. This is largely useful for worlds that existed before BigReactors was released.").getBoolean(false);
			userWorldGenVersion = BRConfig.CONFIGURATION.get("WorldGen", "userWorldGenVersion", 0, "User-set world generation version. Increase this by 1 if you want Big Reactors to re-run world generation in your world.").getInt();

			boolean registerCoalFurnaceRecipe = BRConfig.CONFIGURATION.get("Recipes", "registerCoalForSmelting", true, "If set, coal will be smeltable into graphite bars. Disable this if other mods need to smelt coal into their own products. (Default: true)").getBoolean(true);
			boolean registerCharcoalFurnaceRecipe = BRConfig.CONFIGURATION.get("Recipes", "registerCharcoalForSmelting", true, "If set, charcoal will be smeltable into graphite bars. Disable this if other mods need to smelt charcoal into their own products. (Default: true)").getBoolean(true);
			boolean registerCoalCraftingRecipe = BRConfig.CONFIGURATION.get("Recipes", "registerGraphiteCoalCraftingRecipes", false, "If set, graphite bars can be crafted from 2 gravel, 1 coal. Use this if other mods interfere with the smelting recipe. (Default: false)").getBoolean(false);
			boolean registerCharcoalCraftingRecipe = BRConfig.CONFIGURATION.get("Recipes", "registerGraphiteCharcoalCraftingRecipes", false, "If set, graphite bars can be crafted from 2 gravel, 1 charcoal. Use this if other mods interfere with the smelting recipe. (Default: false)").getBoolean(false);
			registerYelloriteSmeltToUranium = BRConfig.CONFIGURATION.get("Recipes", "registerYelloriteSmeltToUranium", true, "If set, yellorite ore will smelt into whichever item is registered as ingotUranium in the ore dictionary. If false, it will smelt into ingotYellorium. (Default: true)").getBoolean(true);
			
			boolean useSteelForIron = BRConfig.CONFIGURATION.get("Recipes", "requireSteelInsteadOfIron", false, "If set, then all Big Reactors components will require steel ingots (ingotSteel) in place of iron ingots. Will be ignored if no other mod registers steel ingots. (default: false)").getBoolean(false);
			boolean useExpensiveGlass = BRConfig.CONFIGURATION.get("Recipes", "requireObsidianGlass", false, "If set, then Big Reactors will require hardened or reinforced glass (glassHardened or glassReinforced) instead of plain glass. Will be ignored if no other mod registers those glass types. (default: false)").getBoolean(false);
			
			maximumReactorSize = BRConfig.CONFIGURATION.get("General", "maxReactorSize", 32, "The maximum valid size of a reactor in the X/Z plane, in blocks. Lower this if your server's players are building ginormous reactors.").getInt();
			maximumReactorHeight = BRConfig.CONFIGURATION.get("General", "maxReactorHeight", 48, "The maximum valid size of a reactor in the Y dimension, in blocks. Lower this if your server's players are building ginormous reactors. Bigger Y sizes have far less performance impact than X/Z sizes.").getInt();
			ticksPerRedstoneUpdate = BRConfig.CONFIGURATION.get("General", "ticksPerRedstoneUpdate", 20, "Number of ticks between updates for redstone/rednet ports.").getInt();
			powerProductionMultiplier = (float)BRConfig.CONFIGURATION.get("General", "powerProductionMultiplier", 1.0f, "A multiplier for balancing overall power production from Big Reactors. Defaults to 1.").getDouble(1.0);
			fuelUsageMultiplier = (float)BRConfig.CONFIGURATION.get("General", "fuelUsageMultiplier", 1.0f, "A multiplier for balancing fuel consumption. Defaults to 1.").getDouble(1.0);

			maximumTurbineSize = BRConfig.CONFIGURATION.get("General",  "maxTurbineSize", 16, "The maximum valid size of a turbine in the X/Z plane, in blocks. Lower this for smaller turbines, which means lower max output. Turbines will be limited to twice this value in height.").getInt();
			maximumTurbineHeight = maximumTurbineSize * 2;
			
			BRConfig.CONFIGURATION.save();

			if(enableWorldGen) {
				worldGenerator = new BRWorldGenerator();
				GameRegistry.registerWorldGenerator(worldGenerator);
			}
			
			// Patch up vanilla being stupid - most mods already do this, so it's usually a no-op
			if(OreDictionary.getOres("ingotIron").size() <= 0) {
				OreDictionary.registerOre("ingotIron", Item.ingotIron);
			}
			
			if(OreDictionary.getOres("ingotGold").size() <= 0) {
				OreDictionary.registerOre("ingotGold", Item.ingotGold);
			}

			if(OreDictionary.getOreID(new ItemStack(Block.glass)) < 0) {
				OreDictionary.registerOre("glass", Block.glass);
			}
			
			// Use steel if the players are masochists and someone else has supplied steel.
			String ironOrSteelIngot = "ingotIron";
			if(useSteelForIron && OreDictionary.getOres("ingotSteel").size() > 0) {
				ironOrSteelIngot = "ingotSteel";
			}
			
			String yelloriumIngot = "ingotYellorium";
			String blutoniumIngot = "ingotBlutonium";
			if(registerYelloriumAsUranium) {
				yelloriumIngot = "ingotUranium";
				blutoniumIngot = "ingotPlutonium";
			}
			
			/*
			 * Register Recipes
			 */
			// Recipe Registry
			
			// Yellorium
			if (blockYelloriteOre != null)
			{
				ItemStack product;

				if(registerYelloriteSmeltToUranium) {
					product = OreDictionary.getOres("ingotUranium").get(0).copy();
				}
				else {
					product = OreDictionary.getOres("ingotYellorium").get(0).copy();
				}

				FurnaceRecipes.smelting().addSmelting(blockYelloriteOre.blockID, 0, product, 0.5f);
			}
			
			
			// Metal blocks
			if(blockMetal != null && ingotGeneric != null) {
				blockMetal.registerIngotRecipes(ingotGeneric);
			}
			
			if(ingotGeneric != null) {
				// Kind of a hack. Maps all ItemIngot dusts to ingots.
				for(int i = 0; i < ItemIngot.DUST_OFFSET; i++) {
					FurnaceRecipes.smelting().addSmelting(ingotGeneric.itemID, i+ItemIngot.DUST_OFFSET, new ItemStack(ingotGeneric, 1, i), 0f);
				}
			}
			
			ItemStack ingotGraphite = OreDictionary.getOres("ingotGraphite").get(0).copy();
			
			if(registerCoalFurnaceRecipe) {
				// Coal -> Graphite
				FurnaceRecipes.smelting().addSmelting(Item.coal.itemID, 0, ingotGraphite, 1);				
			}
			
			if(registerCharcoalFurnaceRecipe) {
				// Charcoal -> Graphite
				FurnaceRecipes.smelting().addSmelting(Item.coal.itemID, 1, ingotGraphite, 1);
			}
			
			if(registerCoalCraftingRecipe) {
				GameRegistry.addRecipe(new ShapedOreRecipe( ingotGraphite, new Object[] { "GCG", 'G', Block.gravel, 'C', new ItemStack(Item.coal, 1, 0) } ));
			}
			
			if(registerCharcoalCraftingRecipe) {
				GameRegistry.addRecipe(new ShapedOreRecipe( ingotGraphite, new Object[] { "GCG", 'G', Block.gravel, 'C', new ItemStack(Item.coal, 1, 1) } ));
			}
			
			// Basic Parts: Reactor Casing, Fuel Rods
			if(blockYelloriumFuelRod != null) {
				GameRegistry.addRecipe(new ShapedOreRecipe( new ItemStack(blockYelloriumFuelRod, 1), new Object[] { "ICI", "IUI", "ICI", 'I', ironOrSteelIngot, 'C', "ingotGraphite", 'U', yelloriumIngot } ));
			}

			if(blockReactorPart != null) {
				ItemStack reactorPartStack = ((BlockReactorPart) BigReactors.blockReactorPart).getReactorCasingItemStack(); 
				reactorPartStack.stackSize = 4;
				GameRegistry.addRecipe(new ShapedOreRecipe(reactorPartStack, new Object[] { "ICI", "CUC", "ICI", 'I', ironOrSteelIngot, 'C', "ingotGraphite", 'U', yelloriumIngot }));
			}
			
			// Advanced Parts: Control Rod, Access Port, Power Tap, Controller
			if(blockReactorPart != null) {
				ItemStack reactorPartStack = BigReactors.blockReactorPart.getReactorControllerItemStack(); 
				
				GameRegistry.addRecipe(new ShapedOreRecipe(reactorPartStack, new Object[] { "C C", "GDG", "CRC", 'D', Item.diamond, 'G', yelloriumIngot, 'C', "reactorCasing", 'R', Item.redstone }));

				reactorPartStack = BigReactors.blockReactorPart.getReactorPowerTapItemStack();
				GameRegistry.addRecipe(new ShapedOreRecipe(reactorPartStack, new Object[] { "CRC", "R R", "CRC", 'C', "reactorCasing", 'R', Item.redstone }));

				reactorPartStack = BigReactors.blockReactorPart.getAccessPortItemStack();
				GameRegistry.addRecipe(new ShapedOreRecipe(reactorPartStack, new Object[] { "C C", " V ", "CPC", 'C', "reactorCasing", 'V', Block.chest, 'P', Block.pistonBase }));

				reactorPartStack = BigReactors.blockReactorPart.getCoolantPortItemStack();
				GameRegistry.addRecipe(new ShapedOreRecipe(reactorPartStack, new Object[] { "C C", "IVI", "CPC", 'C', "reactorCasing", 'V', Item.bucketEmpty, 'P', Block.pistonBase, 'I', ironOrSteelIngot }));
				
				if(Loader.isModLoaded("MineFactoryReloaded")) {
					reactorPartStack = BigReactors.blockReactorPart.getRedNetPortItemStack();
					GameRegistry.addRecipe(new ShapedOreRecipe(reactorPartStack, new Object[] { "CRC", "RGR", "CRC", 'C', "reactorCasing", 'R', "cableRedNet", 'G', "ingotGold" }));
				}
				
				if(Loader.isModLoaded("ComputerCraft") || Loader.isModLoaded("OpenComputers")) {
					reactorPartStack = BigReactors.blockReactorPart.getComputerPortItemStack();
					GameRegistry.addRecipe(new ShapedOreRecipe(reactorPartStack, new Object[] { "CRC", "GPG", "CRC", 'C', "reactorCasing", 'R', Item.redstone, 'G', "ingotGold", 'P', Item.redstoneRepeater }));
				}
			}
			
			if(blockMultiblockGlass != null) {
				ItemStack reactorGlassStack = blockMultiblockGlass.getItemStack("reactor");
				ItemStack turbineGlassStack = blockMultiblockGlass.getItemStack("turbine");
				
				if(useExpensiveGlass && (OreDictionary.getOres("glassReinforced").size() > 0 || OreDictionary.getOres("glassHardened").size() > 0)) {
					GameRegistry.addRecipe(new ShapedOreRecipe(reactorGlassStack, new Object[] {"GCG", 'G', "glassReinforced", 'C', "reactorCasing" } ));
					GameRegistry.addRecipe(new ShapedOreRecipe(reactorGlassStack, new Object[] {"GCG", 'G', "glassHardened", 'C', "reactorCasing" } ));
					
					GameRegistry.addRecipe(new ShapedOreRecipe(turbineGlassStack, new Object[] {"GCG", 'G', "glassReinforced", 'C', "turbineHousing" } ));
					GameRegistry.addRecipe(new ShapedOreRecipe(turbineGlassStack, new Object[] {"GCG", 'G', "glassHardened", 'C', "turbineHousing" } ));
				}
				else {
					GameRegistry.addRecipe(new ShapedOreRecipe(reactorGlassStack, new Object[] { "GCG", 'G', "glass", 'C', "reactorCasing" } ));
					GameRegistry.addRecipe(new ShapedOreRecipe(turbineGlassStack, new Object[] { "GCG", 'G', "glass", 'C', "turbineHousing" } ));
				}
			}
			
			if(blockReactorControlRod != null) {
				ItemStack reactorControlRodStack = new ItemStack(BigReactors.blockReactorControlRod, 1);
				GameRegistry.addRecipe(new ShapedOreRecipe(reactorControlRodStack, new Object[] { "CGC", "GRG", "CUC", 'G', "ingotGraphite", 'C', "reactorCasing", 'R', Item.redstone, 'U', yelloriumIngot }));
			}
			
			if(blockSmallMachine != null) {
				ItemStack cyaniteReprocessorStack = ((BlockBRSmallMachine)blockSmallMachine).getCyaniteReprocessorItemStack();
				GameRegistry.addRecipe(new ShapedOreRecipe(cyaniteReprocessorStack, new Object[] { "CIC", "PFP", "CRC", 'C', "reactorCasing", 'I', ironOrSteelIngot, 'F', blockYelloriumFuelRod, 'P', Block.pistonBase, 'R', Item.redstone}));
			}
			
			if(blockReactorRedstonePort != null) {
				ItemStack redstonePortStack = new ItemStack(BigReactors.blockReactorRedstonePort, 1);
				GameRegistry.addRecipe(new ShapedOreRecipe(redstonePortStack, new Object[] { "CRC", "RGR", "CRC", 'C', "reactorCasing", 'R', Item.redstone, 'G', Item.ingotGold }));
			}
			
			if(blockTurbinePart != null) {
				ItemStack turbineHousing = blockTurbinePart.getItemStack("housing");
				ItemStack turbineController = blockTurbinePart.getItemStack("controller");
				ItemStack turbinePowerTap = blockTurbinePart.getItemStack("powerTap");
				ItemStack turbineFluidPort = blockTurbinePart.getItemStack("fluidPort");
				ItemStack turbineBearing = blockTurbinePart.getItemStack("bearing");

				turbineHousing.stackSize = 4;
				GameRegistry.addRecipe(new ShapedOreRecipe(turbineHousing, new Object[] { "IGI", "QCQ", "IGI", 'C', "ingotCyanite", 'I', ironOrSteelIngot, 'Q', Item.netherQuartz, 'G', "ingotGraphite" }));
				GameRegistry.addRecipe(new ShapedOreRecipe(turbineController, new Object[] { "H H", "BDB", "H H", 'H', "turbineHousing", 'D', Item.diamond, 'B', blutoniumIngot}));
				GameRegistry.addRecipe(new ShapedOreRecipe(turbinePowerTap, new Object[] { "HRH", "R R", "HRH", 'H', "turbineHousing", 'R', Item.redstone}));
				GameRegistry.addRecipe(new ShapedOreRecipe(turbineFluidPort, new Object[] { "H H", "IVI", "HPH", 'H', "turbineHousing", 'I', ironOrSteelIngot, 'V', Item.bucketEmpty, 'P', Block.pistonBase}));
				GameRegistry.addRecipe(new ShapedOreRecipe(turbineBearing, new Object[] { "HRH", "DDD", "HRH", 'H', "turbineHousing", 'D', Item.diamond, 'R', "turbineRotorShaft"}));

				if(Loader.isModLoaded("ComputerCraft") || Loader.isModLoaded("OpenComputers")) {
					ItemStack turbineComputerPort = blockTurbinePart.getItemStack("computerPort");
					GameRegistry.addRecipe(new ShapedOreRecipe(turbineComputerPort, new Object[] { "HRH", "GPG", "HRH", 'H', "turbineHousing", 'G', "ingotGold", 'R', "turbineRotorShaft"}));
					
				}
			}
			
			if(blockTurbineRotorPart != null) {
				ItemStack rotorShaft = blockTurbineRotorPart.getItemStack("rotor");
				ItemStack rotorBlade = blockTurbineRotorPart.getItemStack("blade");

				GameRegistry.addRecipe(new ShapedOreRecipe(rotorShaft, new Object[] { "ICI", 'C', "ingotCyanite", 'I', ironOrSteelIngot }));
				GameRegistry.addRecipe(new ShapedOreRecipe(rotorBlade, new Object[] { "CII", 'C', "ingotCyanite", 'I', ironOrSteelIngot }));
			}
			
			registerGameBalanceData();
		}

		INITIALIZED = true;
	}
	
	
	/**
	 * Call this to register Tile Entities
	 * 
	 * @return
	 */
	public static void registerTileEntities()
	{
		if (!registeredTileEntities)
		{
			GameRegistry.registerTileEntity(TileEntityReactorPowerTap.class, 	"BRReactorPowerTap");
			GameRegistry.registerTileEntity(TileEntityReactorPart.class, 		"BRReactorPart");
			GameRegistry.registerTileEntity(TileEntityReactorAccessPort.class,	"BRReactorAccessPort");
			GameRegistry.registerTileEntity(TileEntityReactorGlass.class,		"BRReactorGlass");
			GameRegistry.registerTileEntity(TileEntityReactorFuelRod.class, 			"BRFuelRod");
			GameRegistry.registerTileEntity(TileEntityCyaniteReprocessor.class, "BRCyaniteReprocessor");
			
			GameRegistry.registerTileEntity(TileEntityReactorControlRod.class, "BRReactorControlRod");
			GameRegistry.registerTileEntity(TileEntityReactorRedNetPort.class, "BRReactorRedNetPort");
			GameRegistry.registerTileEntity(TileEntityReactorRedstonePort.class,"BRReactorRedstonePort");
			GameRegistry.registerTileEntity(TileEntityReactorComputerPort.class, "BRReactorComputerPort");
			GameRegistry.registerTileEntity(TileEntityReactorCoolantPort.class, "BRReactorCoolantPort");
			GameRegistry.registerTileEntity(TileEntityReactorCreativeCoolantPort.class, "BRReactorCreativeCoolantPort");

			GameRegistry.registerTileEntity(TileEntityTurbinePartStandard.class,  "BRTurbinePart");
			GameRegistry.registerTileEntity(TileEntityTurbinePowerTap.class, "BRTurbinePowerTap");
			GameRegistry.registerTileEntity(TileEntityTurbineFluidPort.class, "BRTurbineFluidPort");
			GameRegistry.registerTileEntity(TileEntityTurbineComputerPort.class, "BRTurbineComputerPort");
			GameRegistry.registerTileEntity(TileEntityTurbinePartGlass.class,  "BRTurbineGlass");
			GameRegistry.registerTileEntity(TileEntityTurbineRotorBearing.class, "BRTurbineRotorBearing");
			GameRegistry.registerTileEntity(TileEntityTurbineRotorPart.class, "BRTurbineRotorPart");
			GameRegistry.registerTileEntity(TileEntityTurbineCreativeSteamGenerator.class, "BRTurbineCreativeSteamGenerator");

			registeredTileEntities = true;
		}
	}


	public static ItemStack registerOres(int i, boolean b) {
		BRConfig.CONFIGURATION.load();

		if (blockYelloriteOre == null) {
			blockYelloriteOre = new BlockBROre(BRConfig.CONFIGURATION.getBlock("YelloriteOre", BigReactors.BLOCK_ID_PREFIX + 0).getInt());
			GameRegistry.registerBlock(BigReactors.blockYelloriteOre, ItemBlockBigReactors.class, "YelloriteOre");
			OreDictionary.registerOre("oreYellorite", blockYelloriteOre);
		}

		if(blockMetal == null) {
			blockMetal = new BlockBRMetal(BRConfig.CONFIGURATION.getBlock("MetalBlock", BigReactors.BLOCK_ID_PREFIX + 13).getInt());
			GameRegistry.registerBlock(BigReactors.blockMetal, ItemBlockBigReactors.class, "BRMetalBlock");
			blockMetal.registerOreDictEntries();
		}

		boolean genYelloriteOre = BRConfig.CONFIGURATION.get("WorldGen", "GenerateYelloriteOre", true, "Add yellorite ore during world generation?").getBoolean(true);
		if (yelloriteOreGeneration == null && genYelloriteOre)
		{
			// Magic number: 1 = stone
			int clustersPerChunk;
			int orePerCluster;
			int maxY;
			
			clustersPerChunk = BRConfig.CONFIGURATION.get("WorldGen", "MaxYelloriteClustersPerChunk", 5, "Maximum number of clusters per chunk; will generate at least half this number, rounded down").getInt();
			orePerCluster = BRConfig.CONFIGURATION.get("WorldGen", "MaxYelloriteOrePerCluster", 10, "Maximum number of blocks to generate in each cluster; will usually generate at least half this number").getInt();
			maxY = BRConfig.CONFIGURATION.get("WorldGen", "YelloriteMaxY", 50, "Maximum height (Y coordinate) in the world to generate yellorite ore").getInt();
			int[] dimensionBlacklist = BRConfig.CONFIGURATION.get("WorldGen", "YelloriteDimensionBlacklist", new int[] {}, "Dimensions in which yellorite ore should not be generated; Nether/End automatically included").getIntList();
			
			yelloriteOreGeneration = new BRSimpleOreGenerator(blockYelloriteOre.blockID, 0, Block.stone.blockID,
											clustersPerChunk/2, clustersPerChunk, 4, maxY, orePerCluster);

			// Per KingLemming's request, bonus yellorite around y12. :)
			BRSimpleOreGenerator yelloriteOreGeneration2 = new BRSimpleOreGenerator(blockYelloriteOre.blockID, 0, Block.stone.blockID,
					1, 2, 11, 13, orePerCluster);

			if(dimensionBlacklist != null) {
				for(int dimension : dimensionBlacklist) {
					yelloriteOreGeneration.blacklistDimension(dimension);
					yelloriteOreGeneration2.blacklistDimension(dimension);
				}
			}

			BRWorldGenerator.addGenerator(BigReactors.yelloriteOreGeneration);
			BRWorldGenerator.addGenerator(yelloriteOreGeneration2);
		}
		
		BRConfig.CONFIGURATION.save();

		return new ItemStack(blockYelloriteOre);
	}


	public static ItemStack registerIngots(int id) {
		if (BigReactors.ingotGeneric == null)
		{
			BRConfig.CONFIGURATION.load();
			registerYelloriumAsUranium = BRConfig.CONFIGURATION.get("Recipes", "registerYelloriumAsUranium", true, "If set, yellorium will be registered in the ore dictionary as ingotUranium as well as ingotYellorium. Otherwise, it will only be registered as ingotYellorium. (Default: true)").getBoolean(true);
			BigReactors.ingotGeneric = new ItemIngot(BRConfig.CONFIGURATION.getItem("IngotYellorium", BigReactors.ITEM_ID_PREFIX + 0).getInt());

			// Register all generic ingots & dusts
			String itemName;
			for(int i = 0; i < ItemIngot.TYPES.length; i++) {
				itemName = ItemIngot.TYPES[i];
				OreDictionary.registerOre(itemName, ingotGeneric.getItemStackForType(itemName));
			}
			
			// Add aliases, if appropriate
			if(registerYelloriumAsUranium) {
				OreDictionary.registerOre("ingotUranium", ingotGeneric.getItemStackForType("ingotYellorium"));
				OreDictionary.registerOre("ingotPlutonium", ingotGeneric.getItemStackForType("ingotBlutonium"));
				OreDictionary.registerOre("dustUranium", ingotGeneric.getItemStackForType("dustYellorium"));
				OreDictionary.registerOre("dustPlutonium", ingotGeneric.getItemStackForType("dustBlutonium"));
			}

			BRConfig.CONFIGURATION.save();
		}

		return new ItemStack(ingotGeneric);
	}


	public static void registerFuelRods(int id, boolean require) {
		if(BigReactors.blockReactorControlRod == null) {
			BRConfig.CONFIGURATION.load();
			BigReactors.blockReactorControlRod = new BlockReactorControlRod(BRConfig.CONFIGURATION.getBlock("ReactorControlRod", BigReactors.BLOCK_ID_PREFIX + 3).getInt(), Material.iron);
			GameRegistry.registerBlock(BigReactors.blockReactorControlRod, ItemBlock.class, "ReactorControlRod");
			BRConfig.CONFIGURATION.save();
		}

		if(BigReactors.blockYelloriumFuelRod == null) {
			BRConfig.CONFIGURATION.load();
			BigReactors.blockYelloriumFuelRod = new BlockFuelRod(BRConfig.CONFIGURATION.getBlock("YelloriumFuelRod", BigReactors.BLOCK_ID_PREFIX + 1).getInt(), Material.iron);
			GameRegistry.registerBlock(BigReactors.blockYelloriumFuelRod, ItemBlock.class, "YelloriumFuelRod");
			BRConfig.CONFIGURATION.save();
		}
	}


	public static void registerReactorPartBlocks(int id, boolean require) {
		if(BigReactors.blockReactorPart == null) {
			BRConfig.CONFIGURATION.load();
			BigReactors.blockReactorPart = new BlockReactorPart(BRConfig.CONFIGURATION.getBlock("ReactorPart", BigReactors.BLOCK_ID_PREFIX + 2).getInt(), Material.iron);
			GameRegistry.registerBlock(BigReactors.blockReactorPart, ItemBlockReactorPart.class, "BRReactorPart");

			OreDictionary.registerOre("reactorCasing", 		 BigReactors.blockReactorPart.getReactorCasingItemStack());
			OreDictionary.registerOre("reactorController", 	 BigReactors.blockReactorPart.getReactorControllerItemStack());
			OreDictionary.registerOre("reactorPowerTap", 	 BigReactors.blockReactorPart.getReactorPowerTapItemStack());
			OreDictionary.registerOre("reactorRedNetPort", 	 BigReactors.blockReactorPart.getRedNetPortItemStack());
			OreDictionary.registerOre("reactorComputerPort", BigReactors.blockReactorPart.getComputerPortItemStack());
			OreDictionary.registerOre("reactorCoolantPort",  BigReactors.blockReactorPart.getCoolantPortItemStack());
			BRConfig.CONFIGURATION.save();
		}
		
		if(BigReactors.blockMultiblockGlass == null) {
			BRConfig.CONFIGURATION.load();
			
			BigReactors.blockMultiblockGlass = new BlockMultiblockGlass(BRConfig.CONFIGURATION.getBlock("ReactorGlass",  BigReactors.BLOCK_ID_PREFIX + 7).getInt(), Material.glass);
			GameRegistry.registerBlock(BigReactors.blockMultiblockGlass, ItemBlockBigReactors.class, "BRMultiblockGlass");
			
			OreDictionary.registerOre("glassReactor", blockMultiblockGlass.getItemStack("reactor"));
			OreDictionary.registerOre("glassTurbine", blockMultiblockGlass.getItemStack("turbine"));
			
			BRConfig.CONFIGURATION.save();
		}
		
		if(BigReactors.blockReactorRedstonePort == null) {
			BRConfig.CONFIGURATION.load();
			
			BigReactors.blockReactorRedstonePort = new BlockReactorRedstonePort(BRConfig.CONFIGURATION.getBlock("ReactorRedstonePort",  BigReactors.BLOCK_ID_PREFIX + 9).getInt(), Material.iron);
			GameRegistry.registerBlock(BigReactors.blockReactorRedstonePort, ItemBlock.class, "BRReactorRedstonePort");
			OreDictionary.registerOre("reactorRedstonePort", new ItemStack(blockReactorRedstonePort, 1));
			
			BRConfig.CONFIGURATION.save();
		}
	}
	
	public static void registerTurbineParts() {
		if(BigReactors.blockTurbinePart == null) {
			BRConfig.CONFIGURATION.load();
			BigReactors.blockTurbinePart = new BlockTurbinePart(BRConfig.CONFIGURATION.getBlock("TurbinePart", BigReactors.BLOCK_ID_PREFIX + 10).getInt(), Material.iron);
			GameRegistry.registerBlock(BigReactors.blockTurbinePart, ItemBlockBigReactors.class, "BRTurbinePart");

			OreDictionary.registerOre("turbineHousing", 	BigReactors.blockTurbinePart.getItemStack("housing"));
			OreDictionary.registerOre("turbineController", 	BigReactors.blockTurbinePart.getItemStack("controller"));
			OreDictionary.registerOre("turbinePowerTap", 	BigReactors.blockTurbinePart.getItemStack("powerTap"));
			OreDictionary.registerOre("turbineFluidPort", 	BigReactors.blockTurbinePart.getItemStack("fluidPort"));
			OreDictionary.registerOre("turbineBearing", 	BigReactors.blockTurbinePart.getItemStack("bearing"));

			BRConfig.CONFIGURATION.save();
		}

		if(BigReactors.blockTurbineRotorPart == null) {
			BRConfig.CONFIGURATION.load();
			BigReactors.blockTurbineRotorPart = new BlockTurbineRotorPart(BRConfig.CONFIGURATION.getBlock("TurbineRotorPart", BigReactors.BLOCK_ID_PREFIX + 11).getInt(), Material.iron);
			GameRegistry.registerBlock(BigReactors.blockTurbineRotorPart, ItemBlockBigReactors.class, "BRTurbineRotorPart");

			OreDictionary.registerOre("turbineRotorShaft", 	BigReactors.blockTurbineRotorPart.getItemStack("rotor"));
			OreDictionary.registerOre("turbineRotorBlade", 	BigReactors.blockTurbineRotorPart.getItemStack("blade"));

			BRConfig.CONFIGURATION.save();
		}
	}
	
	public static void registerSmallMachines(int id, boolean require) {
		if(BigReactors.blockSmallMachine == null) {
			BRConfig.CONFIGURATION.load();

			BigReactors.blockSmallMachine = new BlockBRSmallMachine(BRConfig.CONFIGURATION.getBlock("SmallMachine", BigReactors.BLOCK_ID_PREFIX + 8).getInt(), Material.iron);
			GameRegistry.registerBlock(BigReactors.blockSmallMachine, ItemBlockBigReactors.class, "BRSmallMachine");
			
			OreDictionary.registerOre("brSmallMachineCyaniteProcessor", ((BlockBRSmallMachine)BigReactors.blockSmallMachine).getCyaniteReprocessorItemStack());
			
			BRConfig.CONFIGURATION.save();
		}
	}
	
	public static void registerCreativeParts(int id, boolean require) {
		BRConfig.CONFIGURATION.load();
		
		boolean regCreativeParts = BRConfig.CONFIGURATION.get("General", "registerCreativeMultiblockParts", true, "If true, creative parts for reactors, turbines and other multiblocks will be registered.").getBoolean(true);
		if(regCreativeParts && BigReactors.blockMultiblockCreativePart == null) {
			BigReactors.blockMultiblockCreativePart = new BlockMBCreativePart(BRConfig.CONFIGURATION.getBlock("MultiblockCreativePart", BigReactors.BLOCK_ID_PREFIX + 12).getInt(), Material.iron);
			GameRegistry.registerBlock(BigReactors.blockMultiblockCreativePart, ItemBlockBigReactors.class, "BRMultiblockCreativePart");
		}
		
		BRConfig.CONFIGURATION.save();
	}
	
	public static void registerFluids(int id, boolean require) {
		if(BigReactors.fluidYelloriumStill == null) {
			BRConfig.CONFIGURATION.load();
			
			int fluidYelloriumID = BRConfig.CONFIGURATION.getBlock("LiquidYelloriumStill", BigReactors.BLOCK_ID_PREFIX + 4).getInt();
			
			BigReactors.fluidYellorium = FluidRegistry.getFluid("yellorium");
			if(fluidYellorium == null) {
				fluidYellorium = new Fluid("yellorium");
				fluidYellorium.setBlockID(fluidYelloriumID);
				fluidYellorium.setDensity(100);
				fluidYellorium.setGaseous(false);
				fluidYellorium.setLuminosity(10);
				fluidYellorium.setRarity(EnumRarity.uncommon);
				fluidYellorium.setTemperature(295);
				fluidYellorium.setViscosity(100);
				fluidYellorium.setUnlocalizedName("bigreactors.yellorium.still");
				FluidRegistry.registerFluid(fluidYellorium);
			}

			BlockBRGenericFluid liqY = new BlockBRGenericFluid(fluidYelloriumID, BigReactors.fluidYellorium, "yellorium");
			BigReactors.fluidYelloriumStill = liqY;
			
			GameRegistry.registerBlock(BigReactors.fluidYelloriumStill, ItemBlock.class, BigReactors.fluidYelloriumStill.getUnlocalizedName());

			fluidYelloriumBucketItem = (new ItemBRBucket(BRConfig.CONFIGURATION.getItem("BucketYellorium", BigReactors.ITEM_ID_PREFIX + 1).getInt(), liqY.blockID)).setUnlocalizedName("bucket.yellorium").setMaxStackSize(1).setContainerItem(Item.bucketEmpty);
			
			BRConfig.CONFIGURATION.save();
		}
		
		if(BigReactors.fluidCyaniteStill == null) {
			BRConfig.CONFIGURATION.load();
			
			int fluidCyaniteID = BRConfig.CONFIGURATION.getBlock("LiquidCyaniteStill", BigReactors.BLOCK_ID_PREFIX + 5).getInt();
			
			BigReactors.fluidCyanite = FluidRegistry.getFluid("cyanite");
			if(fluidCyanite == null) {
				fluidCyanite = new Fluid("cyanite");
				fluidCyanite.setBlockID(fluidCyaniteID);
				fluidCyanite.setDensity(100);
				fluidCyanite.setGaseous(false);
				fluidCyanite.setLuminosity(6);
				fluidCyanite.setRarity(EnumRarity.uncommon);
				fluidCyanite.setTemperature(295);
				fluidCyanite.setViscosity(100);
				fluidCyanite.setUnlocalizedName("bigreactors.cyanite.still");
				FluidRegistry.registerFluid(fluidCyanite);
			}

			BlockBRGenericFluid liqDY = new BlockBRGenericFluid(fluidCyaniteID, fluidCyanite, "cyanite");
			BigReactors.fluidCyaniteStill = liqDY;
			GameRegistry.registerBlock(BigReactors.fluidCyaniteStill, ItemBlock.class, BigReactors.fluidCyaniteStill.getUnlocalizedName());
			
			fluidCyaniteBucketItem = (new ItemBRBucket(BRConfig.CONFIGURATION.getItem("BucketCyanite", BigReactors.ITEM_ID_PREFIX + 2).getInt(), liqDY.blockID)).setUnlocalizedName("bucket.cyanite").setMaxStackSize(1).setContainerItem(Item.bucketEmpty);
			
			BRConfig.CONFIGURATION.save();
		}

		if(BigReactors.fluidFuelColumnStill == null) {
			BRConfig.CONFIGURATION.load();
			
			BigReactors.fluidFuelColumn = FluidRegistry.getFluid("fuelColumn");
			if(fluidFuelColumn == null) {
				fluidFuelColumn = new Fluid("fuelColumn");
				fluidFuelColumn.setUnlocalizedName("bigreactors.fuelColumn.still");
				FluidRegistry.registerFluid(fluidFuelColumn);				
			}

			BRConfig.CONFIGURATION.save();
		}
		
		fluidSteam = FluidRegistry.getFluid("steam");
		registeredOwnSteam = false;
		if(fluidSteam == null) {
			// FINE THEN
			BRConfig.CONFIGURATION.load();
			
			fluidSteam = new Fluid("steam");
			fluidSteam.setUnlocalizedName("steam");
			fluidSteam.setTemperature(1000); // For consistency with TE
			fluidSteam.setGaseous(true);
			fluidSteam.setLuminosity(0);
			fluidSteam.setRarity(EnumRarity.common);
			fluidSteam.setDensity(6);
			
			registeredOwnSteam = true;
			
			FluidRegistry.registerFluid(fluidSteam);

			BRConfig.CONFIGURATION.save();
		}

	}

	// This must be done in init or later
	protected static void registerGameBalanceData() {
		ItemStack yelloriumStack 	= ingotGeneric.getItemStackForType("ingotYellorium");
		ItemStack cyaniteStack 		= ingotGeneric.getItemStackForType("ingotCyanite");
		ItemStack blutoniumStack 	= ingotGeneric.getItemStackForType("ingotBlutonium");

		// Register fluids as fuels
		BRRegistry.registerReactorFluidData("yellorium", new ReactorFuel(fluidYellorium, BigReactors.defaultFluidColorFuel, true, false, fluidCyanite));
		BRRegistry.registerReactorFluidData("cyanite", new ReactorFuel(fluidCyanite, BigReactors.defaultFluidColorWaste, false, true/*, fluidBlutonium */)); // TODO: Make a blutonium fluid

		BRRegistry.registerReactorSolidToFuelMapping(new ReactorSolidMapping(yelloriumStack, new FluidStack(fluidYellorium, 1000)));
		BRRegistry.registerReactorSolidToWasteMapping(new ReactorSolidMapping(cyaniteStack, new FluidStack(fluidCyanite, 1000)));
		
		ItemStack blockYellorium = blockMetal.getItemStackForMaterial("Yellorium");
		BRRegistry.registerReactorSolidToFuelMapping(new ReactorSolidMapping(blockYellorium, new FluidStack(fluidYellorium, 9000)));

		BRRegistry.registerReactorFluidToSolidMapping(fluidYellorium.getName(), yelloriumStack);
		BRRegistry.registerReactorFluidToSolidMapping(fluidCyanite.getName(), cyaniteStack);

		// TODO: Make a proper blutonium fluid
		Fluid fluidBlutonium = fluidYellorium;
		BRRegistry.registerReactorSolidToFuelMapping(new ReactorSolidMapping(blutoniumStack, new FluidStack(fluidBlutonium, 1000)));

		ItemStack blockBlutonium = blockMetal.getItemStackForMaterial("Blutonium");
		BRRegistry.registerReactorSolidToFuelMapping(new ReactorSolidMapping(blockBlutonium, new FluidStack(fluidBlutonium, 9000)));

		BRConfig.CONFIGURATION.load();
		boolean enableFantasyMetals = BRConfig.CONFIGURATION.get("General", "enableMetallurgyFantasyMetalsInTurbines", true, "If true, allows Metallurgy's fantasy metals to be used as part of turbine coils. Default: true").getBoolean(true);
		boolean enableComedy 		= BRConfig.CONFIGURATION.get("General", "enableComedy", true, "If true, allows weird stuff inside reactors, like MFR sewage and pink slime. Default: true").getBoolean(true);
		BRConfig.CONFIGURATION.save();

		BRRegistry.registerCoilPart("blockIron", 1f, 1f, 1f);
		BRRegistry.registerCoilPart("blockGold", 2f, 1f, 1.75f);

		BRRegistry.registerCoilPart("blockCopper",		1.2f, 1f, 1.2f);	// TE, lots of mods
		BRRegistry.registerCoilPart("blockOsmium",		1.2f, 1f, 1.2f);	// Mekanism
		BRRegistry.registerCoilPart("blockBrass",		1.4f, 1f, 1.2f);	// Metallurgy
		BRRegistry.registerCoilPart("blockBronze",		1.4f, 1f, 1.2f);	// Mekanism, many others
		BRRegistry.registerCoilPart("blockAluminum",	1.5f, 1f, 1.3f);	// TiCo, couple others
		BRRegistry.registerCoilPart("blockSteel",		1.5f, 1f, 1.3f);	// Metallurgy, Mek, etc.
		BRRegistry.registerCoilPart("blockInvar", 		1.5f, 1f, 1.4f);	// TE
		BRRegistry.registerCoilPart("blockSilver", 		1.7f, 1f, 1.5f);	// TE, lots of mods
		BRRegistry.registerCoilPart("blockElectrum", 	2.5f, 1f, 2.0f);	// TE, lots of mods
		BRRegistry.registerCoilPart("blockElectrumFlux",2.5f, 1.01f, 2.2f);	// Redstone Arsenal, note small energy bonus (7% at 1000RF/t output)
		BRRegistry.registerCoilPart("blockPlatinum",	3.0f, 1f, 2.5f);	// TE, lots of mods
		BRRegistry.registerCoilPart("blockShiny",		3.0f, 1f, 2.5f);	// TE
		BRRegistry.registerCoilPart("blockTitanium",	3.1f, 1f, 2.7f);	// Mariculture
		BRRegistry.registerCoilPart("blockEnderium",	3.0f, 1.02f, 3.0f);	// TE, note tiny energy bonus!	(14% at 1000RF/t output)

		if(enableFantasyMetals) {
			// Metallurgy fantasy metals
			BRRegistry.registerCoilPart("blockMithril", 	2.2f, 1f, 1.5f);
			BRRegistry.registerCoilPart("blockOrichalcum", 	2.3f, 1f, 1.7f);
			BRRegistry.registerCoilPart("blockQuicksilver",	2.6f, 1f, 1.8f);
			BRRegistry.registerCoilPart("blockHaderoth",	3.0f, 1f, 2.0f);
			BRRegistry.registerCoilPart("blockCelenegil",	3.3f, 1f, 2.25f);
			BRRegistry.registerCoilPart("blockTartarite",	3.5f, 1f, 2.5f);
		}
		
		BRRegistry.registerReactorInteriorBlock("blockIron",		0.50f, 0.75f, 1.40f, IHeatEntity.conductivityIron);
		BRRegistry.registerReactorInteriorBlock("blockGold",		0.52f, 0.80f, 1.45f, IHeatEntity.conductivityGold);
		BRRegistry.registerReactorInteriorBlock("blockDiamond",		0.55f, 0.85f, 1.50f, IHeatEntity.conductivityDiamond);
		BRRegistry.registerReactorInteriorBlock("blockEmerald",		0.55f, 0.85f, 1.50f, IHeatEntity.conductivityEmerald);
		BRRegistry.registerReactorInteriorBlock("blockGraphite",	0.10f, 0.50f, 2.00f, IHeatEntity.conductivityGold); // Graphite: a great moderator!
		BRRegistry.registerReactorInteriorBlock("glass",			0.20f, 0.25f, 1.10f, IHeatEntity.conductivityGlass);
		
		// Mod blocks
		BRRegistry.registerReactorInteriorBlock("blockCopper", 		0.50f, 0.75f, 1.40f, IHeatEntity.conductivityCopper);
		BRRegistry.registerReactorInteriorBlock("blockOsmium", 		0.51f, 0.77f, 1.41f, IHeatEntity.conductivityCopper);
		BRRegistry.registerReactorInteriorBlock("blockBrass", 		0.51f, 0.77f, 1.41f, IHeatEntity.conductivityCopper);
		BRRegistry.registerReactorInteriorBlock("blockBronze", 		0.51f, 0.77f, 1.41f, IHeatEntity.conductivityCopper);
		BRRegistry.registerReactorInteriorBlock("blockAluminum", 	0.50f, 0.78f, 1.42f, IHeatEntity.conductivityIron);
		BRRegistry.registerReactorInteriorBlock("blockSteel",	 	0.50f, 0.78f, 1.42f, IHeatEntity.conductivityIron);
		BRRegistry.registerReactorInteriorBlock("blockInvar",	 	0.50f, 0.79f, 1.43f, IHeatEntity.conductivityIron);
		BRRegistry.registerReactorInteriorBlock("blockSilver", 		0.51f, 0.79f, 1.43f, IHeatEntity.conductivitySilver);
		BRRegistry.registerReactorInteriorBlock("blockElectrum", 	0.53f, 0.82f, 1.47f, 2.2f); // Between gold and emerald
		BRRegistry.registerReactorInteriorBlock("blockElectrumFlux",0.54f, 0.83f, 1.48f, 2.4f); // Between gold and emerald
		BRRegistry.registerReactorInteriorBlock("blockPlatinum", 	0.57f, 0.86f, 1.58f, IHeatEntity.conductivityEmerald);
		BRRegistry.registerReactorInteriorBlock("blockShiny", 		0.57f, 0.86f, 1.58f, IHeatEntity.conductivityEmerald);		
		BRRegistry.registerReactorInteriorBlock("blockTitanium", 	0.58f, 0.87f, 1.59f, 2.7f); // Mariculture
		BRRegistry.registerReactorInteriorBlock("blockEnderium", 	0.60f, 0.88f, 1.60f, IHeatEntity.conductivityDiamond);

		if(enableFantasyMetals) {
			BRRegistry.registerReactorInteriorBlock("blockMithril", 	0.53f, 0.81f, 1.45f, IHeatEntity.conductivitySilver);
			BRRegistry.registerReactorInteriorBlock("blockOrichalcum", 	0.52f, 0.83f, 1.46f, 1.7f);	// Between silver and gold
			BRRegistry.registerReactorInteriorBlock("blockQuicksilver", 0.53f, 0.84f, 1.48f, IHeatEntity.conductivityGold);
			BRRegistry.registerReactorInteriorBlock("blockHaderoth", 	0.54f, 0.84f, 1.49f, IHeatEntity.conductivityEmerald);
			BRRegistry.registerReactorInteriorBlock("blockCelenegil", 	0.54f, 0.84f, 1.49f, IHeatEntity.conductivityDiamond);
			BRRegistry.registerReactorInteriorBlock("blockTartarite", 	0.65f, 0.90f, 1.62f, 4f); // Between diamond and graphene
		}

		//Water: 0.33f, 0.5f, 1.33f
		BRRegistry.registerReactorInteriorFluid("water", RadiationHelper.waterData.absorption, RadiationHelper.waterData.heatEfficiency, RadiationHelper.waterData.moderation, IHeatEntity.conductivityWater);
		BRRegistry.registerReactorInteriorFluid("redstone",		0.75f, 0.55f, 1.60f, IHeatEntity.conductivityEmerald);
		BRRegistry.registerReactorInteriorFluid("glowstone",	0.20f, 0.60f, 1.75f, IHeatEntity.conductivityCopper);
		BRRegistry.registerReactorInteriorFluid("cryotheum",	0.66f, 0.95f, 6.00f, IHeatEntity.conductivityDiamond); // Cryotheum: an amazing moderator!
		BRRegistry.registerReactorInteriorFluid("ender",		0.90f, 0.75f, 2.00f, IHeatEntity.conductivityGold);
		BRRegistry.registerReactorInteriorFluid("pyrotheum",	0.66f, 0.90f, 1.00f, IHeatEntity.conductivityIron);
		
		BRRegistry.registerReactorInteriorFluid("life essence", 0.70f, 0.55f, 1.75f, IHeatEntity.conductivityGold); // From Blood Magic

		if(enableComedy) {
			BRRegistry.registerReactorInteriorBlock("blockMeat", 	0.50f, 0.33f, 1.33f, IHeatEntity.conductivityStone);
			BRRegistry.registerReactorInteriorBlock("blockMeatRaw",	0.40f, 0.50f, 1.50f, IHeatEntity.conductivityStone);
			BRRegistry.registerReactorInteriorFluid("meat",			0.40f, 0.60f, 1.33f, IHeatEntity.conductivityStone);
			BRRegistry.registerReactorInteriorFluid("pinkSlime",	0.45f, 0.70f, 1.50f, IHeatEntity.conductivityIron);
			BRRegistry.registerReactorInteriorFluid("sewage",		0.50f, 0.65f, 1.44f, IHeatEntity.conductivityIron);
		}
	}
	
	// Stolen wholesale from Universal Electricity. Thanks Cal!
	/**
	 * Loads all the language files for a mod. This supports the loading of "child" language files
	 * for sub-languages to be loaded all from one file instead of creating multiple of them. An
	 * example of this usage would be different Spanish sub-translations (es_MX, es_YU).
	 * 
	 * @param languagePath - The path to the mod's language file folder.
	 * @param languageSupported - The languages supported. E.g: new String[]{"en_US", "en_AU",
	 * "en_UK"}
	 * @return The amount of language files loaded successfully.
	 */
	public static int loadLanguages(String languagePath, String[] languageSupported)
	{
		int languages = 0;

		/**
		 * Load all languages.
		 */
		for (String language : languageSupported)
		{
			LanguageRegistry.instance().loadLocalization(languagePath + language + ".properties", language, false);

			if (LanguageRegistry.instance().getStringLocalization("children", language) != "")
			{
				try
				{
					String[] children = LanguageRegistry.instance().getStringLocalization("children", language).split(",");

					for (String child : children)
					{
						if (child != "" || child != null)
						{
							LanguageRegistry.instance().loadLocalization(languagePath + language + ".properties", child, false);
							languages++;
						}
					}
				}
				catch (Exception e)
				{
					BRLog.severe("Failed to load a child language file.");
					e.printStackTrace();
				}
			}

			languages++;
		}

		return languages;
	}

	// Thanks KingLemming!
	@SideOnly(Side.CLIENT)
	public static void registerNonBlockFluidIcons(TextureMap map) {
		iconFuelColumnStill = map.registerIcon(TEXTURE_NAME_PREFIX + "fluid.fuelColumn.still");
		iconFuelColumnFlowing = map.registerIcon(TEXTURE_NAME_PREFIX + "fluid.fuelColumn.flowing");
		
		if(registeredOwnSteam) {
			iconSteamStill = map.registerIcon(TEXTURE_NAME_PREFIX + "fluid.steam.still");
			iconSteamFlowing = map.registerIcon(TEXTURE_NAME_PREFIX + "fluid.steam.flowing");
		}
	}


	@SideOnly(Side.CLIENT)
	public static void setNonBlockFluidIcons() {
		fluidFuelColumn.setIcons(iconFuelColumnStill, iconFuelColumnFlowing);
		
		if(registeredOwnSteam) {
			fluidSteam.setIcons(iconSteamStill, iconSteamFlowing);
		}
	}
}

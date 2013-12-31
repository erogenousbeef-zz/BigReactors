package erogenousbeef.bigreactors.common;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.texture.TextureObject;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.Icon;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import erogenousbeef.bigreactors.api.IHeatEntity;
import erogenousbeef.bigreactors.common.block.BlockBRGenericFluid;
import erogenousbeef.bigreactors.common.block.BlockBROre;
import erogenousbeef.bigreactors.common.block.BlockBRSmallMachine;
import erogenousbeef.bigreactors.common.item.ItemBRBucket;
import erogenousbeef.bigreactors.common.item.ItemBlockBROre;
import erogenousbeef.bigreactors.common.item.ItemBlockBigReactors;
import erogenousbeef.bigreactors.common.item.ItemBlockReactorPart;
import erogenousbeef.bigreactors.common.item.ItemBlockSmallMachine;
import erogenousbeef.bigreactors.common.item.ItemBlockTurbinePart;
import erogenousbeef.bigreactors.common.item.ItemBlockYelloriumFuelRod;
import erogenousbeef.bigreactors.common.item.ItemIngot;
import erogenousbeef.bigreactors.common.multiblock.MultiblockReactor;
import erogenousbeef.bigreactors.common.multiblock.block.BlockFuelRod;
import erogenousbeef.bigreactors.common.multiblock.block.BlockReactorControlRod;
import erogenousbeef.bigreactors.common.multiblock.block.BlockReactorGlass;
import erogenousbeef.bigreactors.common.multiblock.block.BlockReactorPart;
import erogenousbeef.bigreactors.common.multiblock.block.BlockReactorRedstonePort;
import erogenousbeef.bigreactors.common.multiblock.block.BlockTurbinePart;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorAccessPort;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorComputerPort;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorControlRod;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorGlass;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorPart;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorPowerTap;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorRedNetPort;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorRedstonePort;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityTurbinePart;
import erogenousbeef.bigreactors.common.tileentity.TileEntityCyaniteReprocessor;
import erogenousbeef.bigreactors.common.tileentity.TileEntityDebugTurbine;
import erogenousbeef.bigreactors.common.tileentity.TileEntityFuelRod;
import erogenousbeef.bigreactors.common.tileentity.TileEntitySteamCreator;
import erogenousbeef.bigreactors.common.tileentity.TileEntityRTG;
import erogenousbeef.bigreactors.world.BRSimpleOreGenerator;
import erogenousbeef.bigreactors.world.BRWorldGenerator;
import erogenousbeef.core.multiblock.MultiblockRegistry;

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
	public static Block blockYelloriumFuelRod;
	public static Block blockReactorPart;
	public static Block blockReactorGlass;
	public static Block blockReactorControlRod;
	public static Block blockReactorRedstonePort; // UGH. Why does the redstone API not allow me to check metadata? :(
	
	public static BlockTurbinePart blockTurbinePart;
	
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
	
	public static float powerProductionMultiplier = 1.0f;
	
	// Game Balance values
	public static final float powerPerHeat = IHeatEntity.powerPerHeat; // RF units per C dissipated
	
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
			
			BRConfig.CONFIGURATION.save();

			if(enableWorldGen) {
				worldGenerator = new BRWorldGenerator();
				GameRegistry.registerWorldGenerator(worldGenerator);
			}
			
			// Patch up vanilla being stupid - most mods already do this, so it's usually a no-op
			if(OreDictionary.getOres("ingotIron").size() <= 0) {
				OreDictionary.registerOre("ingotIron", Item.ingotIron);
			}
			
			// Use steel if the players are masochists and someone else has supplied steel.
			String ironOrSteelIngot = "ingotIron";
			if(useSteelForIron && OreDictionary.getOres("ingotSteel").size() > 0) {
				ironOrSteelIngot = "ingotSteel";
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
				if(registerYelloriumAsUranium)
					GameRegistry.addRecipe(new ShapedOreRecipe( new ItemStack(blockYelloriumFuelRod, 1), new Object[] { "ICI", "IUI", "ICI", 'I', ironOrSteelIngot, 'C', "ingotGraphite", 'U', "ingotUranium" } ));
				else
					GameRegistry.addRecipe(new ShapedOreRecipe( new ItemStack(blockYelloriumFuelRod, 1), new Object[] { "ICI", "IUI", "ICI", 'I', ironOrSteelIngot, 'C', "ingotGraphite", 'U', "ingotYellorium" } ));
			}

			if(blockReactorPart != null) {
				ItemStack reactorPartStack = ((BlockReactorPart) BigReactors.blockReactorPart).getReactorCasingItemStack(); 
				reactorPartStack.stackSize = 4;
				if(registerYelloriumAsUranium)
					GameRegistry.addRecipe(new ShapedOreRecipe(reactorPartStack, new Object[] { "ICI", "CUC", "ICI", 'I', ironOrSteelIngot, 'C', "ingotGraphite", 'U', "ingotUranium" }));
				else
					GameRegistry.addRecipe(new ShapedOreRecipe(reactorPartStack, new Object[] { "ICI", "CUC", "ICI", 'I', ironOrSteelIngot, 'C', "ingotGraphite", 'U', "ingotYellorium" }));
			}
			
			// Advanced Parts: Control Rod, Access Port, Power Tap, Controller
			if(blockReactorPart != null) {
				ItemStack reactorPartStack = ((BlockReactorPart) BigReactors.blockReactorPart).getReactorControllerItemStack(); 
				
				if(registerYelloriumAsUranium)
					GameRegistry.addRecipe(new ShapedOreRecipe(reactorPartStack, new Object[] { "C C", "GDG", "CRC", 'D', Item.diamond, 'G', "ingotUranium", 'C', "reactorCasing", 'R', Item.redstone }));
				else
					GameRegistry.addRecipe(new ShapedOreRecipe(reactorPartStack, new Object[] { "C C", "GDG", "CRC", 'D', Item.diamond, 'G', "ingotYellorium", 'C', "reactorCasing", 'R', Item.redstone }));
				reactorPartStack = ((BlockReactorPart) BigReactors.blockReactorPart).getReactorPowerTapItemStack();
				GameRegistry.addRecipe(new ShapedOreRecipe(reactorPartStack, new Object[] { "CRC", "R R", "CRC", 'C', "reactorCasing", 'R', Item.redstone }));

				reactorPartStack = ((BlockReactorPart) BigReactors.blockReactorPart).getAccessPortItemStack();
				GameRegistry.addRecipe(new ShapedOreRecipe(reactorPartStack, new Object[] { "C C", " V ", "CPC", 'C', "reactorCasing", 'V', Block.chest, 'P', Block.pistonBase }));

				if(Loader.isModLoaded("MineFactoryReloaded")) {
					reactorPartStack = ((BlockReactorPart) BigReactors.blockReactorPart).getRedNetPortItemStack();
					GameRegistry.addRecipe(new ShapedOreRecipe(reactorPartStack, new Object[] { "CRC", "RGR", "CRC", 'C', "reactorCasing", 'R', "cableRedNet", 'G', Item.ingotGold }));
				}
				
				if(Loader.isModLoaded("ComputerCraft")) {
					reactorPartStack = ((BlockReactorPart) BigReactors.blockReactorPart).getComputerPortItemStack();
					GameRegistry.addRecipe(new ShapedOreRecipe(reactorPartStack, new Object[] { "CRC", "GPG", "CRC", 'C', "reactorCasing", 'R', Item.redstone, 'G', Item.ingotGold, 'P', "reactorRedstonePort" }));
				}
			}
			
			if(blockReactorGlass != null) {
				ItemStack reactorGlassStack = new ItemStack(BigReactors.blockReactorGlass, 2);
				
				if(useExpensiveGlass && (OreDictionary.getOres("glassReinforced").size() > 0 || OreDictionary.getOres("glassHardened").size() > 0)) {
					GameRegistry.addRecipe(new ShapedOreRecipe(reactorGlassStack, new Object[] { "   ", "GCG", "   ", 'G', "glassReinforced", 'C', "reactorCasing" } ));
					GameRegistry.addRecipe(new ShapedOreRecipe(reactorGlassStack, new Object[] { "   ", "GCG", "   ", 'G', "glassHardened", 'C', "reactorCasing" } ));
				}
				else {
					GameRegistry.addRecipe(new ShapedOreRecipe(reactorGlassStack, new Object[] { "   ", "GCG", "   ", 'G', Block.glass, 'C', "reactorCasing" } ));
					if(OreDictionary.getOres("glass").size() > 0) {
						GameRegistry.addRecipe(new ShapedOreRecipe(reactorGlassStack, new Object[] { "   ", "GCG", "   ", 'G', "glass", 'C', "reactorCasing" } ));
					}
				}
			}
			
			if(blockReactorControlRod != null) {
				ItemStack reactorControlRodStack = new ItemStack(BigReactors.blockReactorControlRod, 1);
				if(registerYelloriumAsUranium)
					GameRegistry.addRecipe(new ShapedOreRecipe(reactorControlRodStack, new Object[] { "CGC", "GRG", "CUC", 'G', "ingotGraphite", 'C', "reactorCasing", 'R', Item.redstone, 'U', "ingotUranium" }));
				else
					GameRegistry.addRecipe(new ShapedOreRecipe(reactorControlRodStack, new Object[] { "CGC", "GRG", "CUC", 'G', "ingotGraphite", 'C', "reactorCasing", 'R', Item.redstone, 'U', "ingotYellorium" }));
			}
			
			if(blockSmallMachine != null) {
				ItemStack cyaniteReprocessorStack = ((BlockBRSmallMachine)blockSmallMachine).getCyaniteReprocessorItemStack();
				GameRegistry.addRecipe(new ShapedOreRecipe(cyaniteReprocessorStack, new Object[] { "CIC", "PFP", "CRC", 'C', "reactorCasing", 'I', ironOrSteelIngot, 'F', blockYelloriumFuelRod, 'P', Block.pistonBase, 'R', Item.redstone}));
			}
			
			if(blockReactorRedstonePort != null) {
				ItemStack redstonePortStack = new ItemStack(BigReactors.blockReactorRedstonePort, 1);
				GameRegistry.addRecipe(new ShapedOreRecipe(redstonePortStack, new Object[] { "CRC", "RGR", "CRC", 'C', "reactorCasing", 'R', Item.redstone, 'G', Item.ingotGold }));
			}
			
			registerReactorFuelData();
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
			GameRegistry.registerTileEntity(TileEntityFuelRod.class, 			"BRFuelRod");
			GameRegistry.registerTileEntity(TileEntityRTG.class, 				"BRRadiothermalGen");
			GameRegistry.registerTileEntity(TileEntityCyaniteReprocessor.class, "BRCyaniteReprocessor");
			GameRegistry.registerTileEntity(TileEntitySteamCreator.class, 		"BRHeatGenerator");
			GameRegistry.registerTileEntity(TileEntityDebugTurbine.class,		"BRDebugTurbine");
			
			GameRegistry.registerTileEntity(TileEntityReactorControlRod.class, "BRReactorControlRod");
			GameRegistry.registerTileEntity(TileEntityReactorRedNetPort.class, "BRReactorRedNetPort");
			GameRegistry.registerTileEntity(TileEntityReactorRedstonePort.class,"BRReactorRedstonePort");
			GameRegistry.registerTileEntity(TileEntityReactorComputerPort.class, "BRReactorComputerPort");

			GameRegistry.registerTileEntity(TileEntityTurbinePart.class,  "BRTurbinePart");
			registeredTileEntities = true;
		}
	}


	public static ItemStack registerOres(int i, boolean b) {
		BRConfig.CONFIGURATION.load();

		if (blockYelloriteOre == null)
		{
			blockYelloriteOre = new BlockBROre(BRConfig.CONFIGURATION.getBlock("YelloriteOre", BigReactors.BLOCK_ID_PREFIX + 0).getInt());
			GameRegistry.registerBlock(BigReactors.blockYelloriteOre, ItemBlockBROre.class, "YelloriteOre");
			OreDictionary.registerOre("oreYellorite", blockYelloriteOre);
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
			GameRegistry.registerBlock(BigReactors.blockReactorControlRod, ItemBlockBigReactors.class, "ReactorControlRod");
			BRConfig.CONFIGURATION.save();
		}

		if(BigReactors.blockYelloriumFuelRod == null) {
			BRConfig.CONFIGURATION.load();
			BigReactors.blockYelloriumFuelRod = new BlockFuelRod(BRConfig.CONFIGURATION.getBlock("YelloriumFuelRod", BigReactors.BLOCK_ID_PREFIX + 1).getInt(), Material.iron);
			GameRegistry.registerBlock(BigReactors.blockYelloriumFuelRod, ItemBlockYelloriumFuelRod.class, "YelloriumFuelRod");
			BRConfig.CONFIGURATION.save();
		}
	}


	public static void registerReactorPartBlocks(int id, boolean require) {
		if(BigReactors.blockReactorPart == null) {
			BRConfig.CONFIGURATION.load();
			BigReactors.blockReactorPart = new BlockReactorPart(BRConfig.CONFIGURATION.getBlock("ReactorPart", BigReactors.BLOCK_ID_PREFIX + 2).getInt(), Material.iron);
			GameRegistry.registerBlock(BigReactors.blockReactorPart, ItemBlockReactorPart.class, "BRReactorPart");

			OreDictionary.registerOre("reactorCasing", 		((BlockReactorPart) BigReactors.blockReactorPart).getReactorCasingItemStack());
			OreDictionary.registerOre("reactorController", 	((BlockReactorPart) BigReactors.blockReactorPart).getReactorControllerItemStack());
			OreDictionary.registerOre("reactorPowerTap", 	((BlockReactorPart) BigReactors.blockReactorPart).getReactorPowerTapItemStack());
			OreDictionary.registerOre("reactorRedNetPort", 	((BlockReactorPart) BigReactors.blockReactorPart).getRedNetPortItemStack());
			OreDictionary.registerOre("reactorComputerPort", ((BlockReactorPart) BigReactors.blockReactorPart).getComputerPortItemStack());
			BRConfig.CONFIGURATION.save();
		}
		
		if(BigReactors.blockReactorGlass == null) {
			BRConfig.CONFIGURATION.load();
			
			BigReactors.blockReactorGlass = new BlockReactorGlass(BRConfig.CONFIGURATION.getBlock("ReactorGlass",  BigReactors.BLOCK_ID_PREFIX + 7).getInt(), Material.glass);
			GameRegistry.registerBlock(BigReactors.blockReactorGlass, ItemBlockBigReactors.class, "BRReactorGlass");
			
			BRConfig.CONFIGURATION.save();
		}
		
		if(BigReactors.blockReactorRedstonePort == null) {
			BRConfig.CONFIGURATION.load();
			
			BigReactors.blockReactorRedstonePort = new BlockReactorRedstonePort(BRConfig.CONFIGURATION.getBlock("ReactorRedstonePort",  BigReactors.BLOCK_ID_PREFIX + 9).getInt(), Material.iron);
			GameRegistry.registerBlock(BigReactors.blockReactorRedstonePort, ItemBlockBigReactors.class, "BRReactorRedstonePort");
			OreDictionary.registerOre("reactorRedstonePort", new ItemStack(blockReactorRedstonePort, 1));
			
			BRConfig.CONFIGURATION.save();
		}
	}
	
	public static void registerTurbineParts() {
		if(BigReactors.blockTurbinePart == null) {
			BRConfig.CONFIGURATION.load();
			BigReactors.blockTurbinePart = new BlockTurbinePart(BRConfig.CONFIGURATION.getBlock("TurbinePart", BigReactors.BLOCK_ID_PREFIX + 10).getInt(), Material.iron);
			GameRegistry.registerBlock(BigReactors.blockTurbinePart, ItemBlockTurbinePart.class, "BRTurbinePart");

			OreDictionary.registerOre("turbineHousing", 	BigReactors.blockTurbinePart.getItemStack("housing"));
			OreDictionary.registerOre("turbineController", 	BigReactors.blockTurbinePart.getItemStack("controller"));
			OreDictionary.registerOre("turbinePowerTap", 	BigReactors.blockTurbinePart.getItemStack("powerTap"));
			OreDictionary.registerOre("turbineFluidPort", 	BigReactors.blockTurbinePart.getItemStack("fluidPort"));
			BRConfig.CONFIGURATION.save();
		}
	}
	
	public static void registerSmallMachines(int id, boolean require) {
		if(BigReactors.blockSmallMachine == null) {
			BRConfig.CONFIGURATION.load();

			BigReactors.blockSmallMachine = new BlockBRSmallMachine(BRConfig.CONFIGURATION.getBlock("SmallMachine", BigReactors.BLOCK_ID_PREFIX + 8).getInt(), Material.iron);
			GameRegistry.registerBlock(BigReactors.blockSmallMachine, ItemBlockSmallMachine.class, "BRSmallMachine");
			
			OreDictionary.registerOre("brSmallMachineCyaniteProcessor", ((BlockBRSmallMachine)BigReactors.blockSmallMachine).getCyaniteReprocessorItemStack());
			OreDictionary.registerOre("brSmallMachineHeatGenerator", ((BlockBRSmallMachine)BigReactors.blockSmallMachine).getHeatGeneratorItemStack());
			
			BRConfig.CONFIGURATION.save();
		}
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
			
			GameRegistry.registerBlock(BigReactors.fluidYelloriumStill, ItemBlockBigReactors.class, BigReactors.fluidYelloriumStill.getUnlocalizedName());

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
			GameRegistry.registerBlock(BigReactors.fluidCyaniteStill, ItemBlockBigReactors.class, BigReactors.fluidCyaniteStill.getUnlocalizedName());
			
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
	protected static void registerReactorFuelData() {
		// Register fluids as fuels
		BRRegistry.registerReactorFluid(new ReactorFuel(fluidYellorium, BigReactors.defaultFluidColorFuel, true, false, fluidCyanite));
		BRRegistry.registerReactorFluid(new ReactorFuel(fluidCyanite, BigReactors.defaultFluidColorWaste, false, true/*, fluidBlutonium */)); // TODO: Make a blutonium fluid
		
		ItemStack yelloriumStack 	= ingotGeneric.getItemStackForType("ingotYellorium");
		ItemStack cyaniteStack 		= ingotGeneric.getItemStackForType("ingotCyanite");
		ItemStack blutoniumStack 	= ingotGeneric.getItemStackForType("ingotBlutonium");
		
		BRRegistry.registerSolidMapping(new ReactorSolidMapping(yelloriumStack, fluidYellorium));
		BRRegistry.registerSolidMapping(new ReactorSolidMapping(cyaniteStack, fluidCyanite));

		// TODO: Fix the color of this
		// TODO: Make a proper blutonium fluid
		BRRegistry.registerSolidMapping(new ReactorSolidMapping(blutoniumStack, fluidYellorium));
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
					FMLLog.severe("Failed to load a child language file.");
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

package erogenousbeef.bigreactors.common;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.Icon;
import net.minecraftforge.liquids.LiquidDictionary;
import net.minecraftforge.liquids.LiquidStack;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import universalelectricity.prefab.TranslationHelper;
import cpw.mods.fml.common.registry.GameRegistry;
import erogenousbeef.bigreactors.common.block.BlockBROre;
import erogenousbeef.bigreactors.common.block.BlockBRSmallMachine;
import erogenousbeef.bigreactors.common.block.BlockFuelRod;
import erogenousbeef.bigreactors.common.block.BlockBRGenericLiquid;
import erogenousbeef.bigreactors.common.block.BlockRTG;
import erogenousbeef.bigreactors.common.block.BlockReactorControlRod;
import erogenousbeef.bigreactors.common.block.BlockReactorGlass;
import erogenousbeef.bigreactors.common.block.BlockReactorPart;
import erogenousbeef.bigreactors.common.item.ItemBlockBROre;
import erogenousbeef.bigreactors.common.item.ItemBlockBigReactors;
import erogenousbeef.bigreactors.common.item.ItemBlockRTG;
import erogenousbeef.bigreactors.common.item.ItemBlockReactorPart;
import erogenousbeef.bigreactors.common.item.ItemBlockSmallMachine;
import erogenousbeef.bigreactors.common.item.ItemBlockYelloriumFuelRod;
import erogenousbeef.bigreactors.common.item.ItemIngot;
import erogenousbeef.bigreactors.common.multiblock.MultiblockReactor;
import erogenousbeef.bigreactors.common.tileentity.TileEntityCyaniteReprocessor;
import erogenousbeef.bigreactors.common.tileentity.TileEntityFuelRod;
import erogenousbeef.bigreactors.common.tileentity.TileEntityRTG;
import erogenousbeef.bigreactors.common.tileentity.TileEntityReactorAccessPort;
import erogenousbeef.bigreactors.common.tileentity.TileEntityReactorControlRod;
import erogenousbeef.bigreactors.common.tileentity.TileEntityReactorGlass;
import erogenousbeef.bigreactors.common.tileentity.TileEntityReactorPart;
import erogenousbeef.bigreactors.common.tileentity.TileEntityReactorPowerTap;

public class BigReactors {

	public static final String NAME 	= "Big Reactors";
	public static final String CHANNEL 	= "BigReactors";
	public static final String RESOURCE_PATH = "/mods/bigreactors/";
	
	public static final CreativeTabs TAB = new CreativeTabBR(CreativeTabs.getNextID(), CHANNEL);
	
	public static final String TEXTURE_DIRECTORY = RESOURCE_PATH + "textures/";
	public static final String GUI_DIRECTORY = TEXTURE_DIRECTORY + "gui/";
	public static final String BLOCK_TEXTURE_DIRECTORY = TEXTURE_DIRECTORY + "blocks/";
	public static final String ITEM_TEXTURE_DIRECTORY = TEXTURE_DIRECTORY + "items/";
	public static final String MODEL_TEXTURE_DIRECTORY = TEXTURE_DIRECTORY + "models/";

	public static final String TEXTURE_NAME_PREFIX = "bigreactors:";

	public static final String LANGUAGE_PATH = RESOURCE_PATH + "languages/";
	private static final String[] LANGUAGES_SUPPORTED = new String[] { "en_US" };
	
	public static final int BLOCK_ID_PREFIX = 1750;
	
	public static Block blockYelloriteOre;
	public static Block blockYelloriumFuelRod;
	public static Block blockReactorPart;
	public static Block blockReactorGlass;
	public static Block blockReactorControlRod;
	
	public static Block blockRadiothermalGen;
	public static Block blockSmallMachine;
	
	public static Block liquidYelloriumStill;
	public static Block liquidCyaniteStill;
	public static Block liquidFuelColumnStill;
	
	public static LiquidStack liquidYellorium;
	public static LiquidStack liquidCyanite;
	public static LiquidStack liquidFuelColumn;
	
	public static final int defaultLiquidColorFuel = 0xbcba50;
	public static final int defaultLiquidColorWaste = 0x4d92b5;
	
	public static final int ITEM_ID_PREFIX = 17750;
	public static Item ingotGeneric;

	public static BRSimpleOreGenerator yelloriteOreGeneration;
	
	public static boolean INITIALIZED = false;
	public static boolean enableWorldGen = true;
	public static boolean enableWorldGenInNegativeDimensions = false;
	public static boolean enableWorldRegeneration = true;

	public static BREventHandler eventHandler = null;
	public static BigReactorsTickHandler tickHandler = null;
	public static BRWorldGenerator worldGenerator = null;
	
	private static boolean registeredTileEntities = false;
	public static int maximumReactorSize = MultiblockReactor.DIMENSION_UNBOUNDED;
	public static int maximumReactorHeight = MultiblockReactor.DIMENSION_UNBOUNDED;
	
	// Game Balance values
	public static final float powerPerHeat = 2.0f; // Power units per C dissipated
	
	/**
	 * Call this function in your mod init stage.
	 */
	public static void register(Object modInstance)
	{

		if (!INITIALIZED)
		{
			TranslationHelper.loadLanguages(BigReactors.LANGUAGE_PATH, LANGUAGES_SUPPORTED);

			// General config loading
			BRConfig.CONFIGURATION.load();
			enableWorldGen = BRConfig.CONFIGURATION.get("WorldGen", "enableWorldGen", true, "If false, disables all world gen from Big Reactors; all other worldgen settings are automatically overridden").getBoolean(true);
			enableWorldGenInNegativeDimensions = BRConfig.CONFIGURATION.get("WorldGen", "enableWorldGenInNegativeDims", false, "Run BR world generation in negative dimension IDs? (default: false) If you don't know what this is, leave it alone.").getBoolean(false);
			enableWorldRegeneration = BRConfig.CONFIGURATION.get("WorldGen", "enableWorldRegeneration", false, "Run BR World Generation in chunks that have already been generated, but have not been modified by Big Reactors before. This is largely useful for worlds that existed before BigReactors was released.").getBoolean(false);
			if(enableWorldGen) {
				worldGenerator = new BRWorldGenerator();
				GameRegistry.registerWorldGenerator(worldGenerator);
			}
			
			maximumReactorSize = BRConfig.CONFIGURATION.get("General", "maxReactorSize", 32, "The maximum valid size of a reactor in the X/Z plane, in blocks. Lower this if your server's players are building ginormous reactors.").getInt();
			maximumReactorHeight = BRConfig.CONFIGURATION.get("General", "maxReactorHeight", 48, "The maximum valid size of a reactor in the Y dimension, in blocks. Lower this if your server's players are building ginormous reactors. Bigger Y sizes have far less performance impact than X/Z sizes.").getInt();
			BRConfig.CONFIGURATION.save();
			
			/*
			 * Register Recipes
			 */
			// Recipe Registry
			
			// Yellorium
			if (blockYelloriteOre != null)
			{
				FurnaceRecipes.smelting().addSmelting(blockYelloriteOre.blockID, 0, OreDictionary.getOres("ingotUranium").get(0), 0.5f);
			}

			// TODO: Configurable recipes.
			ItemStack ingotUranium = null;
			ItemStack ingotCyanite = null;
			ItemStack ingotGraphite = null;
			if(OreDictionary.getOres("ingotUranium") != null) {
				ingotUranium = OreDictionary.getOres("ingotUranium").get(0);
			}
			if(OreDictionary.getOres("ingotDepletedUranium") != null) {
				ingotCyanite = OreDictionary.getOres("ingotCyanite").get(0);
			}
			if(OreDictionary.getOres("ingotGraphite") != null) {
				ingotGraphite = OreDictionary.getOres("ingotGraphite").get(0);
			}
			
			// Coal -> Graphite
			FurnaceRecipes.smelting().addSmelting(Item.coal.itemID, 0, ingotGraphite, 1);
			// Charcoal -> Graphite
			FurnaceRecipes.smelting().addSmelting(Item.coal.itemID, 1, ingotGraphite, 1);
			
			// Basic Parts: Reactor Casing, Fuel Rods
			if(blockYelloriumFuelRod != null) {
				GameRegistry.addRecipe(new ShapedOreRecipe( new ItemStack(blockYelloriumFuelRod, 1), new Object[] { "ICI", "IUI", "ICI", 'I', "ingotIron", 'C', "ingotGraphite", 'U', "ingotUranium" } ));
			}

			if(blockReactorPart != null) {
				ItemStack reactorPartStack = ((BlockReactorPart) BigReactors.blockReactorPart).getReactorCasingItemStack(); 
				reactorPartStack.stackSize = 4;
				GameRegistry.addRecipe(new ShapedOreRecipe(reactorPartStack, new Object[] { "ICI", "CUC", "ICI", 'I', "ingotIron", 'C', "ingotGraphite", 'U', "ingotUranium" }));
			}
			
			// Advanced Parts: Control Rod, Access Port, Power Tap, Controller
			if(blockReactorPart != null) {
				ItemStack reactorPartStack = ((BlockReactorPart) BigReactors.blockReactorPart).getReactorControllerItemStack(); 
				GameRegistry.addRecipe(new ShapedOreRecipe(reactorPartStack, new Object[] { "C C", "GDG", "CRC", 'D', Item.diamond, 'G', "ingotUranium", 'C', "reactorCasing", 'R', Item.redstone }));
				
				reactorPartStack = ((BlockReactorPart) BigReactors.blockReactorPart).getReactorPowerTapItemStack();
				GameRegistry.addRecipe(new ShapedOreRecipe(reactorPartStack, new Object[] { "CRC", "R R", "CRC", 'C', "reactorCasing", 'R', Item.redstone }));

				reactorPartStack = ((BlockReactorPart) BigReactors.blockReactorPart).getAccessPortItemStack();
				GameRegistry.addRecipe(new ShapedOreRecipe(reactorPartStack, new Object[] { "C C", " V ", "CPC", 'C', "reactorCasing", 'V', Block.chest, 'P', Block.pistonBase }));
			}
			
			if(blockReactorGlass != null) {
				ItemStack reactorGlassStack = new ItemStack(BigReactors.blockReactorGlass, 2);
				GameRegistry.addRecipe(new ShapedOreRecipe(reactorGlassStack, new Object[] { "   ", "GCG", "   ", 'G', Block.glass, 'C', "reactorCasing" } ));
			}
			
			if(blockReactorControlRod != null) {
				ItemStack reactorControlRodStack = new ItemStack(BigReactors.blockReactorControlRod, 1);
				GameRegistry.addRecipe(new ShapedOreRecipe(reactorControlRodStack, new Object[] { "CGC", "GRG", "CUC", 'G', "ingotGraphite", 'C', "reactorCasing", 'R', Item.redstone, 'U', "ingotUranium" }));
			}
			
			if(blockSmallMachine != null) {
				ItemStack cyaniteReprocessorStack = ((BlockBRSmallMachine)blockSmallMachine).getCyaniteReprocessorItemStack();
				GameRegistry.addRecipe(new ShapedOreRecipe(cyaniteReprocessorStack, new Object[] { "CIC", "PFP", "CRC", 'C', "reactorCasing", 'I', "ingotIron", 'F', blockYelloriumFuelRod, 'P', Block.pistonBase, 'R', Item.redstone}));
			}
			
			/* TODO: Fixme
			if(blockRadiothermalGen != null) {
				GameRegistry.addRecipe(new ItemStack(blockRadiothermalGen, 1), new Object[] {
					"III",
					"IUI",
					"III",
					Character.valueOf('I'), Item.ingotIron,
					Character.valueOf('U'), new ItemStack(blockYelloriumFuelRod, 1)
				});
			}
			*/
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
			
			GameRegistry.registerTileEntity(TileEntityReactorControlRod.class, "BRReactorControlRod");
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
			float oreGenChance;
			float oreGenMultiplier;
			
			clustersPerChunk = BRConfig.CONFIGURATION.get("WorldGen", "YelloriteClustersPerChunk", 4, "Target number of clusters per chunk; note that this isn't a guarantee").getInt();
			orePerCluster = BRConfig.CONFIGURATION.get("WorldGen", "YelloriteOrePerCluster", 4, "Minimum number of blocks to generate in each cluster; usually guaranteed").getInt();
			maxY = BRConfig.CONFIGURATION.get("WorldGen", "YelloriteMaxY", 50, "Maximum height (Y coordinate) in the world to generate yellorite ore").getInt();
			oreGenChance = (float)BRConfig.CONFIGURATION.get("WorldGen", "YelloriteOreGenBaseChance", 0.75, "Base chance to generate additional ore above the minimum number per cluster").getDouble(0.75);
			oreGenMultiplier = (float)BRConfig.CONFIGURATION.get("WorldGen", "YelloriteOreGenChanceMultiplier", 0.5, "For each additional ore generated above the minimum number, generation chance is multiplied by this").getDouble(0.5);
			int[] dimensionBlacklist = BRConfig.CONFIGURATION.get("WorldGen", "YelloriteDimensionBlacklist", new int[] {}, "Dimensions in which yellorite ore should not be generated; Nether/End automatically included").getIntList();
			
			yelloriteOreGeneration = new BRSimpleOreGenerator(blockYelloriteOre.blockID, 0, Block.stone.blockID,
											clustersPerChunk, maxY, orePerCluster, oreGenChance, oreGenMultiplier);
			if(dimensionBlacklist != null) {
				for(int dimension : dimensionBlacklist) {
					yelloriteOreGeneration.blacklistDimension(dimension);
				}
			}
			
			BRWorldGenerator.addGenerator(BigReactors.yelloriteOreGeneration);
		}
		
		BRConfig.CONFIGURATION.save();

		return new ItemStack(blockYelloriteOre);
	}


	public static ItemStack registerIngots(int id, boolean require) {
		if (BigReactors.ingotGeneric == null)
		{
			BRConfig.CONFIGURATION.load();
			BigReactors.ingotGeneric = new ItemIngot(BRConfig.CONFIGURATION.getItem("IngotYellorium", BigReactors.ITEM_ID_PREFIX + 0).getInt());

			if (OreDictionary.getOres("ingotUranium").size() <= 0 || require)
			{
				ItemStack yelloriumStack = new ItemStack(ingotGeneric, 1, 0);
				OreDictionary.registerOre("ingotUranium", yelloriumStack);
				BRRegistry.registerFuel(new ReactorFuel(yelloriumStack, BigReactors.defaultLiquidColorFuel));
			}
			
			if (OreDictionary.getOres("ingotCyanite").size() <= 0 || require)
			{
				ItemStack cyaniteStack = new ItemStack(ingotGeneric, 1, 1);
				OreDictionary.registerOre("ingotCyanite", cyaniteStack);
				BRRegistry.registerWaste(new ReactorFuel(cyaniteStack, BigReactors.defaultLiquidColorWaste));
			}
			
			if (OreDictionary.getOres("ingotGraphite").size() <= 0 || require)
			{
				OreDictionary.registerOre("ingotGraphite", new ItemStack(ingotGeneric, 1, 2));
			}
			
			if (OreDictionary.getOres("ingotPlutonium").size() <= 0 || require)
			{
				ItemStack blutoniumStack = new ItemStack(ingotGeneric, 1, 3);
				OreDictionary.registerOre("ingotPlutonium", blutoniumStack);
				// TODO: Fix the color of this
				BRRegistry.registerFuel(new ReactorFuel(blutoniumStack, 0x2222ee));
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

			BRConfig.CONFIGURATION.save();
		}
		
		if(BigReactors.blockReactorGlass == null) {
			BRConfig.CONFIGURATION.load();
			
			BigReactors.blockReactorGlass = new BlockReactorGlass(BRConfig.CONFIGURATION.getBlock("ReactorGlass",  BigReactors.BLOCK_ID_PREFIX + 7).getInt(), Material.glass);
			GameRegistry.registerBlock(BigReactors.blockReactorGlass, ItemBlockBigReactors.class, "BRReactorGlass");
			
			BRConfig.CONFIGURATION.save();
			
		}
	}
	
	public static void registerSmallMachines(int id, boolean require) {
		if(BigReactors.blockSmallMachine == null) {
			BRConfig.CONFIGURATION.load();

			BigReactors.blockSmallMachine = new BlockBRSmallMachine(BRConfig.CONFIGURATION.getBlock("SmallMachine", BigReactors.BLOCK_ID_PREFIX + 8).getInt(), Material.iron);
			GameRegistry.registerBlock(BigReactors.blockSmallMachine, ItemBlockSmallMachine.class, "BRSmallMachine");
			
			OreDictionary.registerOre("brSmallMachineCyaniteProcessor", ((BlockBRSmallMachine)BigReactors.blockSmallMachine).getCyaniteReprocessorItemStack());
			
			BRConfig.CONFIGURATION.save();
		}
	}
	
	public static void registerYelloriumLiquids(int id, boolean require) {
		if(BigReactors.liquidYelloriumStill == null) {
			BRConfig.CONFIGURATION.load();
			
			BlockBRGenericLiquid liqY = new BlockBRGenericLiquid(BRConfig.CONFIGURATION.getBlock("LiquidYelloriumStill", BigReactors.BLOCK_ID_PREFIX + 4).getInt(), "yellorium");
			BigReactors.liquidYelloriumStill = liqY;
			
			GameRegistry.registerBlock(BigReactors.liquidYelloriumStill, ItemBlockBigReactors.class, BigReactors.liquidYelloriumStill.getUnlocalizedName());
			
			LiquidStack liquidYelloriumStack = new LiquidStack(liquidYelloriumStill, 1);
			BigReactors.liquidYellorium = LiquidDictionary.getOrCreateLiquid("yellorium", liquidYelloriumStack);
			
			BRRegistry.registerFuel(new ReactorFuel(liquidYelloriumStack.asItemStack(), BigReactors.defaultLiquidColorFuel));
			BRConfig.CONFIGURATION.save();
		}
		
		if(BigReactors.liquidCyaniteStill == null) {
			BRConfig.CONFIGURATION.load();
			
			BlockBRGenericLiquid liqDY = new BlockBRGenericLiquid(BRConfig.CONFIGURATION.getBlock("LiquidCyaniteStill", BigReactors.BLOCK_ID_PREFIX + 5).getInt(), "cyanite");
			BigReactors.liquidCyaniteStill = liqDY;
			GameRegistry.registerBlock(BigReactors.liquidCyaniteStill, ItemBlockBigReactors.class, BigReactors.liquidCyaniteStill.getUnlocalizedName());
			
			LiquidStack liquidCyaniteStack = new LiquidStack(liquidCyaniteStill, 1);
			BigReactors.liquidCyanite = LiquidDictionary.getOrCreateLiquid("cyanite", liquidCyaniteStack);
			
			BRRegistry.registerWaste(new ReactorFuel(liquidCyaniteStack.asItemStack(), BigReactors.defaultLiquidColorWaste));
			BRConfig.CONFIGURATION.save();
		}

		if(BigReactors.liquidFuelColumnStill == null) {
			BRConfig.CONFIGURATION.load();
			
			BlockBRGenericLiquid liqFC = new BlockBRGenericLiquid(BRConfig.CONFIGURATION.getBlock("LiquidFuelColumnStill", BigReactors.BLOCK_ID_PREFIX + 6).getInt(), "fuelColumn");
			BigReactors.liquidFuelColumnStill = liqFC;
			GameRegistry.registerBlock(BigReactors.liquidFuelColumnStill, ItemBlockBigReactors.class, BigReactors.liquidFuelColumnStill.getUnlocalizedName());
			BigReactors.liquidFuelColumn = LiquidDictionary.getOrCreateLiquid("brFuelColumnVisualLiquid", new LiquidStack(liquidFuelColumnStill, 1));

			BRConfig.CONFIGURATION.save();
		}
	}	
}

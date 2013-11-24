package erogenousbeef.bigreactors.common;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.texture.TextureObject;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.Icon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.event.Event.Result;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import erogenousbeef.bigreactors.common.block.BlockBROre;
import erogenousbeef.bigreactors.common.block.BlockBRSmallMachine;
import erogenousbeef.bigreactors.common.block.BlockFuelRod;
import erogenousbeef.bigreactors.common.block.BlockBRGenericFluid;
import erogenousbeef.bigreactors.common.block.BlockRTG;
import erogenousbeef.bigreactors.common.block.BlockReactorControlRod;
import erogenousbeef.bigreactors.common.block.BlockReactorGlass;
import erogenousbeef.bigreactors.common.block.BlockReactorPart;
import erogenousbeef.bigreactors.common.item.ItemBRBucket;
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
import erogenousbeef.bigreactors.common.tileentity.TileEntityReactorRedNetPort;
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
	public static Block blockYelloriumFuelRod;
	public static Block blockReactorPart;
	public static Block blockReactorGlass;
	public static Block blockReactorControlRod;
	
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
	
	public static final int defaultFluidColorFuel = 0xbcba50;
	public static final int defaultFluidColorWaste = 0x4d92b5;
	
	public static final int ITEM_ID_PREFIX = 17750;
	public static Item ingotGeneric;

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
	
	public static float powerProductionMultiplier = 1.0f;
	
	// Game Balance values
	public static final float powerPerHeat = 25f; // RF units per C dissipated
	public static final int ticksPerRedNetUpdate = 20; // Once per second, roughly
	
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

			maximumReactorSize = BRConfig.CONFIGURATION.get("General", "maxReactorSize", 32, "The maximum valid size of a reactor in the X/Z plane, in blocks. Lower this if your server's players are building ginormous reactors.").getInt();
			maximumReactorHeight = BRConfig.CONFIGURATION.get("General", "maxReactorHeight", 48, "The maximum valid size of a reactor in the Y dimension, in blocks. Lower this if your server's players are building ginormous reactors. Bigger Y sizes have far less performance impact than X/Z sizes.").getInt();

			powerProductionMultiplier = (float)BRConfig.CONFIGURATION.get("General", "powerProductionMultiplier", 1.0f, "A multiplier for balancing overall power production from Big Reactors. Defaults to 1.").getDouble(1.0);
			
			BRConfig.CONFIGURATION.save();

			if(enableWorldGen) {
				worldGenerator = new BRWorldGenerator();
				GameRegistry.registerWorldGenerator(worldGenerator);
			}
			
			/*
			 * Register Recipes
			 */
			// Recipe Registry
			
			// Yellorium
			if (blockYelloriteOre != null)
			{
				FurnaceRecipes.smelting().addSmelting(blockYelloriteOre.blockID, 0, OreDictionary.getOres("ingotUranium").get(0), 0.5f);
			}
			
			if(ingotGeneric != null) {
				// Kind of a hack. Maps all ItemIngot dusts to ingots.
				for(int i = 0; i < ItemIngot.DUST_OFFSET; i++) {
					FurnaceRecipes.smelting().addSmelting(ingotGeneric.itemID, i+ItemIngot.DUST_OFFSET, new ItemStack(ingotGeneric, 1, i), 0f);
				}
			}
			
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
			
			if(registerCoalFurnaceRecipe) {
				// Coal -> Graphite
				FurnaceRecipes.smelting().addSmelting(Item.coal.itemID, 0, ingotGraphite, 1);				
			}
			
			if(registerCharcoalFurnaceRecipe) {
				// Charcoal -> Graphite
				FurnaceRecipes.smelting().addSmelting(Item.coal.itemID, 1, ingotGraphite, 1);
			}
			
			if(registerCoalCraftingRecipe) {
				GameRegistry.addRecipe(new ShapedOreRecipe( ingotGraphite.copy(), new Object[] { "GCG", 'G', Block.gravel, 'C', new ItemStack(Item.coal, 1, 0) } ));
			}
			
			if(registerCharcoalCraftingRecipe) {
				GameRegistry.addRecipe(new ShapedOreRecipe( ingotGraphite.copy(), new Object[] { "GCG", 'G', Block.gravel, 'C', new ItemStack(Item.coal, 1, 1) } ));
			}
			
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

				reactorPartStack = ((BlockReactorPart) BigReactors.blockReactorPart).getRedNetPortItemStack();
				GameRegistry.addRecipe(new ShapedOreRecipe(reactorPartStack, new Object[] { "CRC", "RRR", "CRC", 'C', "reactorCasing", 'R', Item.redstone }));
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
			
			GameRegistry.registerTileEntity(TileEntityReactorControlRod.class, "BRReactorControlRod");
			GameRegistry.registerTileEntity(TileEntityReactorRedNetPort.class, "BRReactorRedNetPort");
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
			
			// Per KingLemming's request, bonus yellorite at y12. :)
			BRSimpleOreGenerator yelloriteOreGeneration2 = new BRSimpleOreGenerator(blockYelloriteOre.blockID, 0, Block.stone.blockID,
					0, 2, 12, 12, orePerCluster, oreGenChance * 0.25f, oreGenMultiplier * 0.25f);
			if(dimensionBlacklist != null) {
				for(int dimension : dimensionBlacklist) {
					yelloriteOreGeneration2.blacklistDimension(dimension);
				}
			}
			
			BRWorldGenerator.addGenerator(yelloriteOreGeneration2);
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
			}
			
			if (OreDictionary.getOres("ingotCyanite").size() <= 0 || require)
			{
				ItemStack cyaniteStack = new ItemStack(ingotGeneric, 1, 1);
				OreDictionary.registerOre("ingotCyanite", cyaniteStack);
			}
			
			if (OreDictionary.getOres("ingotGraphite").size() <= 0 || require)
			{
				OreDictionary.registerOre("ingotGraphite", new ItemStack(ingotGeneric, 1, 2));
			}
			
			if (OreDictionary.getOres("ingotPlutonium").size() <= 0 || require)
			{
				ItemStack blutoniumStack = new ItemStack(ingotGeneric, 1, 3);
				OreDictionary.registerOre("ingotPlutonium", blutoniumStack);
			}

			// Dusts
			
			if (OreDictionary.getOres("dustUranium").size() <= 0 || require)
			{
				ItemStack yelloriumDustStack = new ItemStack(ingotGeneric, 1, 4);
				OreDictionary.registerOre("dustUranium", yelloriumDustStack);
			}

			if (OreDictionary.getOres("dustCyanite").size() <= 0 || require)
			{
				ItemStack cyaniteDustStack = new ItemStack(ingotGeneric, 1, 5);
				OreDictionary.registerOre("dustCyanite", cyaniteDustStack);
			}

			if (OreDictionary.getOres("dustGraphite").size() <= 0 || require)
			{
				ItemStack graphiteDustStack = new ItemStack(ingotGeneric, 1, 6);
				OreDictionary.registerOre("dustGraphite", graphiteDustStack);
			}

			if (OreDictionary.getOres("dustPlutonium").size() <= 0 || require)
			{
				ItemStack blutoniumDustStack = new ItemStack(ingotGeneric, 1, 7);
				OreDictionary.registerOre("dustPlutonium", blutoniumDustStack);
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
			
			int fuelColumnFluidID = BRConfig.CONFIGURATION.getBlock("LiquidFuelColumnStill", BigReactors.BLOCK_ID_PREFIX + 6).getInt();
			
			BigReactors.fluidFuelColumn = FluidRegistry.getFluid("fuelColumn");
			if(fluidFuelColumn == null) {
				fluidFuelColumn = new Fluid("fuelColumn");
				fluidFuelColumn.setBlockID(fuelColumnFluidID);
				fluidFuelColumn.setUnlocalizedName("bigreactors.fuelColumn.still");
				FluidRegistry.registerFluid(fluidFuelColumn);				
			}
			
			BlockBRGenericFluid liqFC = new BlockBRGenericFluid(fuelColumnFluidID, fluidFuelColumn, "fuelColumn");
			BigReactors.fluidFuelColumnStill = liqFC;
			GameRegistry.registerBlock(BigReactors.fluidFuelColumnStill, ItemBlockBigReactors.class, BigReactors.fluidFuelColumnStill.getUnlocalizedName());

			BRConfig.CONFIGURATION.save();
		}
		
		fluidSteam = FluidRegistry.getFluid("steam");
		if(fluidSteam == null) {
			// FINE THEN
			BRConfig.CONFIGURATION.load();
			
			fluidSteam = new Fluid("steam");
			fluidSteam.setUnlocalizedName("steam");
			fluidSteam.setTemperature(373);
			fluidSteam.setGaseous(true);
			fluidSteam.setLuminosity(0);
			fluidSteam.setRarity(EnumRarity.common);
			fluidSteam.setDensity(6);
			
			if(Minecraft.getMinecraft().theWorld.isRemote) {
				// Register icons on clients
			}
			
			FluidRegistry.registerFluid(fluidSteam);

			BRConfig.CONFIGURATION.save();
		}

	}
	
	// This must be done in init or later
	protected static void registerReactorFuelData() {
		// Register fluids as fuels
		BRRegistry.registerReactorFluid(new ReactorFuel(fluidYellorium, BigReactors.defaultFluidColorFuel, true, false, fluidCyanite));
		BRRegistry.registerReactorFluid(new ReactorFuel(fluidCyanite, BigReactors.defaultFluidColorWaste, false, true/*, fluidBlutonium */)); // TODO: Make a blutonium fluid
		
		ItemStack yelloriumStack 	= new ItemStack(ingotGeneric, 1, 0);
		ItemStack cyaniteStack 		= new ItemStack(ingotGeneric, 1, 1);
		ItemStack blutoniumStack 	= new ItemStack(ingotGeneric, 1, 2);
		
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
}

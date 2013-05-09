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
import universalelectricity.core.UniversalElectricity;
import universalelectricity.prefab.TranslationHelper;
import universalelectricity.prefab.ore.OreGenBase;
import universalelectricity.prefab.ore.OreGenReplaceStone;
import universalelectricity.prefab.ore.OreGenerator;
import cpw.mods.fml.common.registry.GameRegistry;
import erogenousbeef.bigreactors.common.block.BlockBROre;
import erogenousbeef.bigreactors.common.block.BlockFuelRod;
import erogenousbeef.bigreactors.common.block.BlockBRGenericLiquid;
import erogenousbeef.bigreactors.common.block.BlockRTG;
import erogenousbeef.bigreactors.common.block.BlockReactorGlass;
import erogenousbeef.bigreactors.common.block.BlockReactorPart;
import erogenousbeef.bigreactors.common.item.ItemBlockBROre;
import erogenousbeef.bigreactors.common.item.ItemBlockBigReactors;
import erogenousbeef.bigreactors.common.item.ItemBlockRTG;
import erogenousbeef.bigreactors.common.item.ItemBlockReactorPart;
import erogenousbeef.bigreactors.common.item.ItemBlockYelloriumFuelRod;
import erogenousbeef.bigreactors.common.item.ItemIngot;
import erogenousbeef.bigreactors.common.tileentity.TileEntityFuelRod;
import erogenousbeef.bigreactors.common.tileentity.TileEntityRTG;
import erogenousbeef.bigreactors.common.tileentity.TileEntityReactorAccessPort;
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
	public static Block blockRadiothermalGen;
	
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

	public static OreGenBase yelloriteOreGeneration;
	
	public static boolean INITIALIZED = false;

	private static boolean registeredTileEntities = false;
	
	/**
	 * Call this function in your mod init stage.
	 */
	public static void register(Object modInstance)
	{

		if (!INITIALIZED)
		{
			System.out.println("Big Reactors loaded: " + TranslationHelper.loadLanguages(BigReactors.LANGUAGE_PATH, LANGUAGES_SUPPORTED) + " languages");

			/**
			 * Register Recipes
			 */
			// Recipe Registry
			
			// Yellorium
			if (blockYelloriteOre != null)
			{
				System.out.println("Registered yellorite -> yellorium");
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
				ingotCyanite = OreDictionary.getOres("ingotDepletedUranium").get(0);
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
				GameRegistry.addRecipe(reactorPartStack, new Object[] { "ICI", "C C", "ICI", 'I', Item.ingotIron, 'C', ingotGraphite.getItem() });
			}
			
			// Advanced Parts: Control Rod, Access Port, Power Tap, Controller
			if(blockReactorPart != null) {
				ItemStack singleReactorCasing = ((BlockReactorPart) BigReactors.blockReactorPart).getReactorCasingItemStack();
				
				ItemStack reactorPartStack = ((BlockReactorPart) BigReactors.blockReactorPart).getReactorControllerItemStack(); 
				GameRegistry.addRecipe(reactorPartStack, new Object[] { "C C", "GDG", "CRC", 'D', Item.diamond, 'G', ingotGraphite.getItem(), 'C', singleReactorCasing.getItem(), 'R', Item.redstone });
				
				reactorPartStack = ((BlockReactorPart) BigReactors.blockReactorPart).getReactorControlRodItemStack(); 
				GameRegistry.addRecipe(reactorPartStack, new Object[] { "CGC", "GRG", "C C", 'G', ingotGraphite.getItem(), 'C', singleReactorCasing.getItem(), 'R', Item.redstone });

				reactorPartStack = ((BlockReactorPart) BigReactors.blockReactorPart).getReactorPowerTapItemStack();
				GameRegistry.addRecipe(reactorPartStack, new Object[] { "CRC", "R R", "CRC", 'C', singleReactorCasing.getItem(), 'R', Item.redstone });

				reactorPartStack = ((BlockReactorPart) BigReactors.blockReactorPart).getAccessPortItemStack();
				GameRegistry.addRecipe(reactorPartStack, new Object[] { "C C", " V ", "CPC", 'C', singleReactorCasing.getItem(), 'V', Block.chest, 'P', Block.pistonBase });
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

		if (yelloriteOreGeneration == null)
		{
			yelloriteOreGeneration = new OreGenReplaceStone("Yellorite Ore", "oreYellorite", new ItemStack(BigReactors.blockYelloriteOre, 1, 0), 60, 26, 4).enable(BRConfig.CONFIGURATION);
			OreGenerator.addOre(BigReactors.yelloriteOreGeneration);
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
				OreDictionary.registerOre("ingotUranium", new ItemStack(ingotGeneric, 1, 0));
			}
			
			if (OreDictionary.getOres("ingotDepletedUranium").size() <= 0 || require)
			{
				OreDictionary.registerOre("ingotDepletedUranium", new ItemStack(ingotGeneric, 1, 1));
			}
			
			if (OreDictionary.getOres("ingotGraphite").size() <= 0 || require)
			{
				OreDictionary.registerOre("ingotGraphite", new ItemStack(ingotGeneric, 1, 2));
			}
			
			if (OreDictionary.getOres("ingotPlutonium").size() <= 0 || require)
			{
				OreDictionary.registerOre("ingotPlutonium", new ItemStack(ingotGeneric, 1, 3));
			}
			BRConfig.CONFIGURATION.save();
		}

		return new ItemStack(ingotGeneric);
		
	}


	public static void registerFuelRods(int id, boolean require) {
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
			OreDictionary.registerOre("reactorControlRod", 	((BlockReactorPart) BigReactors.blockReactorPart).getReactorControlRodItemStack());
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
	
	public static void registerRadiothermalGen(int id, boolean require) {
		if(BigReactors.blockRadiothermalGen == null) {
			BRConfig.CONFIGURATION.load();
			
			BigReactors.blockRadiothermalGen = new BlockRTG(BRConfig.CONFIGURATION.getBlock("RadiothermalGen", BigReactors.BLOCK_ID_PREFIX + 3).getInt(), Material.iron);
			GameRegistry.registerBlock(BigReactors.blockRadiothermalGen, ItemBlockRTG.class, "BRRadiothermalGen");
			BRConfig.CONFIGURATION.save();
		}
	}
	
	public static void registerYelloriumLiquids(int id, boolean require) {
		if(BigReactors.liquidYelloriumStill == null) {
			BRConfig.CONFIGURATION.load();
			
			BlockBRGenericLiquid liqY = new BlockBRGenericLiquid(BRConfig.CONFIGURATION.getBlock("LiquidYelloriumStill", BigReactors.BLOCK_ID_PREFIX + 4).getInt(), "yellorium");
			BigReactors.liquidYelloriumStill = liqY;
			
			GameRegistry.registerBlock(BigReactors.liquidYelloriumStill, ItemBlockBigReactors.class, BigReactors.liquidYelloriumStill.getUnlocalizedName());
			BigReactors.liquidYellorium = LiquidDictionary.getOrCreateLiquid("yellorium", new LiquidStack(liquidYelloriumStill, 1));
			
			BRRegistry.registerReactorFuel(liqY, liqY);
			BRConfig.CONFIGURATION.save();
		}
		
		if(BigReactors.liquidCyaniteStill == null) {
			BRConfig.CONFIGURATION.load();
			
			BlockBRGenericLiquid liqDY = new BlockBRGenericLiquid(BRConfig.CONFIGURATION.getBlock("LiquidCyaniteStill", BigReactors.BLOCK_ID_PREFIX + 5).getInt(), "cyanite");
			BigReactors.liquidCyaniteStill = liqDY;
			GameRegistry.registerBlock(BigReactors.liquidCyaniteStill, ItemBlockBigReactors.class, BigReactors.liquidCyaniteStill.getUnlocalizedName());
			BigReactors.liquidCyanite = LiquidDictionary.getOrCreateLiquid("cyanite", new LiquidStack(liquidCyaniteStill, 1));
			
			BRRegistry.registerReactorFuel(liqDY, liqDY);
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

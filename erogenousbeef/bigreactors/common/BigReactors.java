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
import erogenousbeef.bigreactors.common.block.BlockReactorPart;
import erogenousbeef.bigreactors.common.item.ItemBlockBROre;
import erogenousbeef.bigreactors.common.item.ItemBlockBigReactors;
import erogenousbeef.bigreactors.common.item.ItemBlockRTG;
import erogenousbeef.bigreactors.common.item.ItemBlockReactorPart;
import erogenousbeef.bigreactors.common.item.ItemBlockYelloriumFuelRod;
import erogenousbeef.bigreactors.common.item.ItemIngot;
import erogenousbeef.bigreactors.common.tileentity.TileEntityFuelRod;
import erogenousbeef.bigreactors.common.tileentity.TileEntityRTG;
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
	public static Block blockRadiothermalGen;
	
	public static Block liquidYelloriumStill;
	public static Block liquidDepletedYelloriumStill;
	public static Block liquidFuelColumnStill;
	
	public static LiquidStack liquidYellorium;
	public static LiquidStack liquidDepletedYellorium;
	public static LiquidStack liquidFuelColumn;
	
	public static final int defaultLiquidColorFuel = 0xbcba50;
	public static final int defaultLiquidColorWaste = 0x4d92b5;
	
	public static final int ITEM_ID_PREFIX = 17750;
	public static Item ingotYellorium;

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
			
			// Copper
			if (blockYelloriteOre != null)
			{
				System.out.println("Registered yellorite -> yellorium");
				FurnaceRecipes.smelting().addSmelting(blockYelloriteOre.blockID, 0, OreDictionary.getOres("ingotUranium").get(0), 0.5f);
			}

			if(blockYelloriumFuelRod != null) {
				GameRegistry.addRecipe(new ShapedOreRecipe( new ItemStack(blockYelloriumFuelRod, 1), new Object[] { "CUC", "CUC", "CUC", 'C', "ingotCopper", 'U', "ingotUranium" } ));				
			}
			
			if(blockRadiothermalGen != null) {
				GameRegistry.addRecipe(new ItemStack(blockRadiothermalGen, 1), new Object[] {
					"III",
					"IUI",
					"III",
					Character.valueOf('I'), Item.ingotIron,
					Character.valueOf('U'), new ItemStack(blockYelloriumFuelRod, 1)
				});
			}
			// TODO: Add recipes for reactor parts.
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
		if (BigReactors.ingotYellorium == null)
		{
			BRConfig.CONFIGURATION.load();
			BigReactors.ingotYellorium = new ItemIngot(BRConfig.CONFIGURATION.getItem("IngotYellorium", BigReactors.ITEM_ID_PREFIX + 0).getInt());

			if (OreDictionary.getOres("ingotUranium").size() <= 0 || require)
			{
				OreDictionary.registerOre("ingotUranium", new ItemStack(ingotYellorium, 1, 0));
			}

			BRConfig.CONFIGURATION.save();
		}

		return new ItemStack(ingotYellorium);
		
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
		
		if(BigReactors.liquidDepletedYelloriumStill == null) {
			BRConfig.CONFIGURATION.load();
			
			BlockBRGenericLiquid liqDY = new BlockBRGenericLiquid(BRConfig.CONFIGURATION.getBlock("LiquidDepletedYelloriumStill", BigReactors.BLOCK_ID_PREFIX + 5).getInt(), "depletedYellorium");
			BigReactors.liquidDepletedYelloriumStill = liqDY;
			GameRegistry.registerBlock(BigReactors.liquidDepletedYelloriumStill, ItemBlockBigReactors.class, BigReactors.liquidDepletedYelloriumStill.getUnlocalizedName());
			BigReactors.liquidDepletedYellorium = LiquidDictionary.getOrCreateLiquid("depletedYellorium", new LiquidStack(liquidDepletedYelloriumStill, 1));
			
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

package erogenousbeef.bigreactors.common;

import java.util.ArrayList;
import java.util.HashSet;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import cofh.core.util.oredict.OreDictionaryArbiter;
import cofh.lib.util.helpers.ItemHelper;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import erogenousbeef.bigreactors.api.IHeatEntity;
import erogenousbeef.bigreactors.api.registry.Reactants;
import erogenousbeef.bigreactors.api.registry.ReactorConversions;
import erogenousbeef.bigreactors.api.registry.ReactorInterior;
import erogenousbeef.bigreactors.api.registry.TurbineCoil;
import erogenousbeef.bigreactors.common.block.BlockBRDevice;
import erogenousbeef.bigreactors.common.block.BlockBRGenericFluid;
import erogenousbeef.bigreactors.common.block.BlockBRMetal;
import erogenousbeef.bigreactors.common.block.BlockBROre;
import erogenousbeef.bigreactors.common.data.StandardFluids;
import erogenousbeef.bigreactors.common.data.StandardReactants;
import erogenousbeef.bigreactors.common.item.ItemBRBucket;
import erogenousbeef.bigreactors.common.item.ItemBeefDebugTool;
import erogenousbeef.bigreactors.common.item.ItemBlockBigReactors;
import erogenousbeef.bigreactors.common.item.ItemIngot;
import erogenousbeef.bigreactors.common.multiblock.MultiblockReactor;
import erogenousbeef.bigreactors.common.multiblock.MultiblockTurbine;
import erogenousbeef.bigreactors.common.multiblock.block.BlockExchangerInteriorPart;
import erogenousbeef.bigreactors.common.multiblock.block.BlockExchangerPart;
import erogenousbeef.bigreactors.common.multiblock.block.BlockFuelRod;
import erogenousbeef.bigreactors.common.multiblock.block.BlockMBCreativePart;
import erogenousbeef.bigreactors.common.multiblock.block.BlockMultiblockGlass;
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
	public static final String MODID	= "BigReactors";
	public static final String CHANNEL 	=  MODID.toLowerCase();
	public static final String RESOURCE_PATH = "/assets/bigreactors/";
	
	public static final CreativeTabs TAB = new CreativeTabBR(MODID);

	public static final String TEXTURE_NAME_PREFIX = "bigreactors:";
	
	public static final String TEXTURE_DIRECTORY = RESOURCE_PATH + "textures/";
	public static final String GUI_DIRECTORY = TEXTURE_NAME_PREFIX + "textures/gui/";
	public static final String BLOCK_TEXTURE_DIRECTORY = TEXTURE_DIRECTORY + "blocks/";
	public static final String ITEM_TEXTURE_DIRECTORY = TEXTURE_DIRECTORY + "items/";
	public static final String MODEL_TEXTURE_DIRECTORY = TEXTURE_DIRECTORY + "models/";

	public static final String LANGUAGE_PATH = RESOURCE_PATH + "languages/";
	private static final String[] LANGUAGES_SUPPORTED = new String[] { "de_DE", "en_US", "es_SP", "nl_NL", "pl_PL", "pt_BR", "ru_RU", "sv_SE", "zh_CN" };
	
	public static final int BLOCK_ID_PREFIX = 1750;
	
	public static Block blockYelloriteOre;
	public static BlockBRMetal blockMetal;
	public static Block blockYelloriumFuelRod;
	public static BlockReactorPart blockReactorPart;
	public static Block blockReactorRedstonePort; // UGH. Why does the redstone API not allow me to check metadata? :(
	
	public static BlockTurbinePart blockTurbinePart;
	public static BlockTurbineRotorPart blockTurbineRotorPart;

	public static BlockExchangerInteriorPart blockExchangerInteriorPart;
	public static BlockExchangerPart blockExchangerPart;
	
	public static BlockMultiblockGlass blockMultiblockGlass;
	public static BlockMBCreativePart blockMultiblockCreativePart;
	
	public static Block blockRadiothermalGen;
	public static Block blockDevice;
	
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

	public static ItemBeefDebugTool itemDebugTool;
	
	public static BRSimpleOreGenerator yelloriteOreGeneration;
	
	public static boolean INITIALIZED = false;
	public static boolean enableWorldGen = true;
	public static boolean enableWorldGenInNegativeDimensions = false;
	public static boolean enableWorldRegeneration = true;
	public static int userWorldGenVersion = 0;

	public static BREventHandler eventHandler = null;
	public static BigReactorsTickHandler tickHandler = null;
	public static BRWorldGenerator worldGenerator = null;
	public static HashSet<Integer> dimensionWhitelist = new HashSet<Integer>();
	
	private static boolean registeredTileEntities = false;
	public static int maximumReactorSize = MultiblockReactor.DIMENSION_UNBOUNDED;
	public static int maximumReactorHeight = MultiblockReactor.DIMENSION_UNBOUNDED;
	public static int ticksPerRedstoneUpdate = 20; // Once per second, roughly
	
	public static int maximumTurbineSize = 16;
	public static int maximumTurbineHeight = 32;
	
	public static float powerProductionMultiplier = 1.0f;
	public static float fuelUsageMultiplier = 1.0f;
	
	public static float reactorPowerProductionMultiplier = 1.0f;
	public static float turbinePowerProductionMultiplier = 1.0f;
	
	public static float turbineCoilDragMultiplier = 1.0f;
	public static float turbineAeroDragMultiplier = 1.0f;
	public static float turbineMassDragMultiplier = 1.0f;
	public static float	turbineFluidPerBladeMultiplier = 1.0f;
	
	public static boolean isValentinesDay = false; // Easter Egg :)
	
	// Game Balance values
	
	protected static IIcon iconSteamStill;
	protected static IIcon iconSteamFlowing;
	protected static IIcon iconFuelColumnStill;
	protected static IIcon iconFuelColumnFlowing;
	
	private static boolean registerYelloriteSmeltToUranium = true;
	private static boolean registerYelloriumAsUranium = true;
	
	/**
	 * Call this function in your mod init stage.
	 */
	public static void register(Object modInstance)
	{

		if (!INITIALIZED)
		{

			// General config loading
			BRConfig.CONFIGURATION.load();
			enableWorldGen = BRConfig.CONFIGURATION.get("WorldGen", "enableWorldGen", true, "If false, disables all world gen from Big Reactors; all other worldgen settings are automatically overridden").getBoolean(true);
			enableWorldGenInNegativeDimensions = BRConfig.CONFIGURATION.get("WorldGen", "enableWorldGenInNegativeDims", false, "Run BR world generation in negative dimension IDs? (default: false) If you don't know what this is, leave it alone.").getBoolean(false);
			enableWorldRegeneration = BRConfig.CONFIGURATION.get("WorldGen", "enableWorldRegeneration", false, "Run BR World Generation in chunks that have already been generated, but have not been modified by Big Reactors before. This is largely useful for worlds that existed before BigReactors was released.").getBoolean(false);
			userWorldGenVersion = BRConfig.CONFIGURATION.get("WorldGen", "userWorldGenVersion", 0, "User-set world generation version. Increase this by 1 if you want Big Reactors to re-run world generation in your world.").getInt();
			int[] worldGenDimensionWhitelist = BRConfig.CONFIGURATION.get("WorldGen", "dimensionWhitelist", new int[] {}, "If enableWorldGenInNegativeDimensions is false, you may add negative dimensions to this whitelist to selectively enable worldgen in them.").getIntList();
			for(int i : worldGenDimensionWhitelist) {
				dimensionWhitelist.add(i);
			}

			boolean registerCoalFurnaceRecipe = BRConfig.CONFIGURATION.get("Recipes", "registerCoalForSmelting", true, "If set, coal will be smeltable into graphite bars. Disable this if other mods need to smelt coal into their own products. (Default: true)").getBoolean(true);
			boolean registerCharcoalFurnaceRecipe = BRConfig.CONFIGURATION.get("Recipes", "registerCharcoalForSmelting", true, "If set, charcoal will be smeltable into graphite bars. Disable this if other mods need to smelt charcoal into their own products. (Default: true)").getBoolean(true);
			boolean registerCoalCraftingRecipe = BRConfig.CONFIGURATION.get("Recipes", "registerGraphiteCoalCraftingRecipes", false, "If set, graphite bars can be crafted from 2 gravel, 1 coal. Use this if other mods interfere with the smelting recipe. (Default: false)").getBoolean(false);
			boolean registerCharcoalCraftingRecipe = BRConfig.CONFIGURATION.get("Recipes", "registerGraphiteCharcoalCraftingRecipes", false, "If set, graphite bars can be crafted from 2 gravel, 1 charcoal. Use this if other mods interfere with the smelting recipe. (Default: false)").getBoolean(false);
			registerYelloriteSmeltToUranium = BRConfig.CONFIGURATION.get("Recipes", "registerYelloriteSmeltToUranium", true, "If set, yellorite ore will smelt into whichever item is registered as ingotUranium in the ore dictionary. If false, it will smelt into ingotYellorium. (Default: true)").getBoolean(true);
			
			boolean useSteelForIron = BRConfig.CONFIGURATION.get("Recipes", "requireSteelInsteadOfIron", false, "If set, then all Big Reactors components will require steel ingots (ingotSteel) in place of iron ingots. Will be ignored if no other mod registers steel ingots. (default: false)").getBoolean(false);
			boolean useExpensiveGlass = BRConfig.CONFIGURATION.get("Recipes", "requireObsidianGlass", false, "If set, then Big Reactors will require hardened or reinforced glass (blockGlassHardened or glassReinforced) instead of plain glass. Will be ignored if no other mod registers those glass types. (default: false)").getBoolean(false);
			
			boolean enableReactorPowerTapRecipe = BRConfig.CONFIGURATION.get("Recipes", "enableReactorPowerTapRecipe", true, "If set, reactor power taps can be crafted, allowing players to use passive-cooled reactors.").getBoolean(true);
			boolean enableCyaniteFromYelloriumRecipe = BRConfig.CONFIGURATION.get("Recipes", "enableCyaniteFromYelloriumRecipe", true, "If set, cyanite will be craftable from yellorium ingots and sand.").getBoolean(true);

			maximumReactorSize = BRConfig.CONFIGURATION.get("General", "maxReactorSize", 32, "The maximum valid size of a reactor in the X/Z plane, in blocks. Lower this if your server's players are building ginormous reactors.").getInt();
			maximumReactorHeight = BRConfig.CONFIGURATION.get("General", "maxReactorHeight", 48, "The maximum valid size of a reactor in the Y dimension, in blocks. Lower this if your server's players are building ginormous reactors. Bigger Y sizes have far less performance impact than X/Z sizes.").getInt();
			ticksPerRedstoneUpdate = BRConfig.CONFIGURATION.get("General", "ticksPerRedstoneUpdate", 20, "Number of ticks between updates for redstone/rednet ports.").getInt();
			powerProductionMultiplier = (float)BRConfig.CONFIGURATION.get("General", "powerProductionMultiplier", 1.0f, "A multiplier for balancing overall power production from Big Reactors. Defaults to 1.").getDouble(1.0);
			fuelUsageMultiplier = (float)BRConfig.CONFIGURATION.get("General", "fuelUsageMultiplier", 1.0f, "A multiplier for balancing fuel consumption. Defaults to 1.").getDouble(1.0);

			reactorPowerProductionMultiplier = (float)BRConfig.CONFIGURATION.get("General", "powerProductionMultiplier", 1.0f, "A multiplier for balancing reactor power production. Stacks with powerProductionMultiplier. Defaults to 1.").getDouble(1.0);
			turbinePowerProductionMultiplier = (float)BRConfig.CONFIGURATION.get("General", "powerProductionMultiplier", 1.0f, "A multiplier for balancing turbine power production. Stacks with powerProductionMultiplier. Defaults to 1.").getDouble(1.0);
			
			maximumTurbineSize = BRConfig.CONFIGURATION.get("General",  "maxTurbineSize", 16, "The maximum valid size of a turbine in the X/Z plane, in blocks. Lower this for smaller turbines, which means lower max output.").getInt();
			maximumTurbineHeight = BRConfig.CONFIGURATION.get("General",  "maxTurbineHeight", 32, "The maximum valid height of a turbine (Y axis), in blocks. (Default: 32)").getInt();
			
			turbineCoilDragMultiplier = (float)BRConfig.CONFIGURATION.get("General", "turbineCoilDragMultiplier", 1.0, "A multiplier for balancing coil size. Multiplies the amount of energy drawn per coil block per tick. (Default: 1)").getDouble(1.0);
			turbineAeroDragMultiplier = (float)BRConfig.CONFIGURATION.get("General", "turbineAeroDragMultiplier", 1.0, "A multiplier for balancing rotor sizes. Multiplies the amount of energy lost to aerodynamic drag per tick. (Default: 1)").getDouble(1.0);
			turbineMassDragMultiplier = (float)BRConfig.CONFIGURATION.get("General", "turbineMassDragMultiplier", 1.0, "A multiplier for balancing rotor sizes. Multiplies the amount of energy lost to friction per tick. (Default: 1)").getDouble(1.0);
			turbineFluidPerBladeMultiplier = (float)BRConfig.CONFIGURATION.get("General", "turbineFluidPerBladeMultiplier", 1.0, "A multiplier for balancing coil size. Multiplies the amount of fluid each blade block can process (base of 25 will be multiplied, then rounded down to the nearest integer). (Default: 1)").getDouble(1.0);

			
			
			MultiblockTurbine.inputFluidPerBlade = (int) Math.floor(MultiblockTurbine.inputFluidPerBlade * turbineFluidPerBladeMultiplier);
			MultiblockTurbine.inductorBaseDragCoefficient *= turbineCoilDragMultiplier;

			BRConfig.CONFIGURATION.save();

			if(enableWorldGen) {
				worldGenerator = new BRWorldGenerator();
				GameRegistry.registerWorldGenerator(worldGenerator, 0);
			}
			
			// Patch up vanilla being stupid - most mods already do this, so it's usually a no-op
			if(!ItemHelper.oreNameExists("ingotIron")) {
				OreDictionary.registerOre("ingotIron", new ItemStack(Items.iron_ingot, 1));
			}
			
			if(!ItemHelper.oreNameExists("ingotGold")) {
				OreDictionary.registerOre("ingotGold", new ItemStack(Items.gold_ingot, 1));
			}
			
			if(!ItemHelper.oreNameExists("blockSnow")) {
				OreDictionary.registerOre("blockSnow", new ItemStack(Blocks.snow, 1));
			}
			
			if(!ItemHelper.oreNameExists("blockIce")) {
				OreDictionary.registerOre("blockIce", new ItemStack(Blocks.ice, 1));
			}

			if(!ItemHelper.oreNameExists("blockGlassColorless")) {
				OreDictionary.registerOre("blockGlassColorless", new ItemStack(Blocks.glass, 1));
			}
			
			// Use steel if the players are masochists and someone else has supplied steel.
			String ironOrSteelIngot = "ingotIron";
			if(useSteelForIron) {
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
					ArrayList<ItemStack> candidateOres = OreDictionaryArbiter.getOres("ingotUranium");
					if(candidateOres == null || candidateOres.size() <= 0) {
						BRLog.warning("Config value registerYelloriteSmeltToUranium is set to True, but there are no ores registered as ingotUranium in the ore dictionary! Falling back to using standard yellorium only.");
						candidateOres = OreDictionaryArbiter.getOres("ingotYellorium");
					}
					product = candidateOres.get(0).copy();
				}
				else {
					product = OreDictionaryArbiter.getOres("ingotYellorium").get(0).copy();
				}

				GameRegistry.addSmelting(blockYelloriteOre, product, 0.5f);
			}
			
			
			// Metal blocks
			if(blockMetal != null && ingotGeneric != null) {
				blockMetal.registerIngotRecipes(ingotGeneric);
			}
			
			if(blockMetal != null) {
				// Ludicrite block. Because.
				GameRegistry.addRecipe(new ShapedOreRecipe(blockMetal.getItemStackForMaterial("Ludicrite"), "BPB", "ENE", "BPB", 'N', Items.nether_star, 'P', Items.ender_pearl, 'E', Blocks.emerald_block, 'B', blutoniumIngot));
				if(ItemHelper.getOre("blockEnderium") != null) {
					// Ok, how about some ludicrous shit here. Enderium and blaze rods. Have fun, bucko.
					GameRegistry.addRecipe(new ShapedOreRecipe(blockMetal.getItemStackForMaterial("Ludicrite"), "BRB", "E E", "BRB", 'B', blutoniumIngot, 'R', Items.blaze_rod, 'E', "blockEnderium"));
				}
			}

			if(ingotGeneric != null) {
				// Map all dusts to ingots.
				for(int i = 0; i < ItemIngot.MATERIALS.length; i++) {
					ItemStack ingotStack = ingotGeneric.getIngotItem(ItemIngot.MATERIALS[i]);
					ItemStack dustStack = ingotGeneric.getDustItem(ItemIngot.MATERIALS[i]);
					GameRegistry.addSmelting(dustStack, ingotStack, 0f);
				}
			}
			
			ItemStack ingotGraphite = OreDictionaryArbiter.getOres("ingotGraphite").get(0).copy();
			ItemStack ingotCyanite = OreDictionaryArbiter.getOres("ingotCyanite").get(0).copy();
			
			if(registerCoalFurnaceRecipe) {
				// Coal -> Graphite
				GameRegistry.addSmelting(Items.coal, ingotGraphite, 1);
			}
			
			if(registerCharcoalFurnaceRecipe) {
				// Charcoal -> Graphite
				GameRegistry.addSmelting(new ItemStack(Items.coal, 1, 1), ingotGraphite, 1);
			}
			
			if(registerCoalCraftingRecipe) {
				GameRegistry.addRecipe(new ShapedOreRecipe(ingotGraphite, "GCG", 'G', Blocks.gravel, 'C', new ItemStack(Items.coal, 1, 0)));
			}
			
			if(registerCharcoalCraftingRecipe) {
				GameRegistry.addRecipe(new ShapedOreRecipe( ingotGraphite, "GCG", 'G', Blocks.gravel, 'C', new ItemStack(Items.coal, 1, 1)));
			}
			
			if(enableCyaniteFromYelloriumRecipe) {
				GameRegistry.addRecipe(new ShapelessOreRecipe(ingotCyanite, yelloriumIngot, Blocks.sand ));
			}

			// Basic Parts: Reactor Casing, Fuel Rods
			if(blockYelloriumFuelRod != null) {
				GameRegistry.addRecipe(new ShapedOreRecipe( new ItemStack(blockYelloriumFuelRod, 1), "ICI", "IUI", "ICI", 'I', ironOrSteelIngot, 'C', "ingotGraphite", 'U', yelloriumIngot));
			}

			if(blockReactorPart != null) {
				ItemStack reactorPartStack = BigReactors.blockReactorPart.getReactorCasingItemStack();
				reactorPartStack.stackSize = 4;
				GameRegistry.addRecipe(new ShapedOreRecipe(reactorPartStack, "ICI", "CUC", "ICI", 'I', ironOrSteelIngot, 'C', "ingotGraphite", 'U', yelloriumIngot));
			}
			
			// Advanced Parts: Control Rod, Access Port, Power Tap, Controller
			if(blockReactorPart != null) {
				ItemStack reactorPartStack = BigReactors.blockReactorPart.getReactorControllerItemStack(); 
				
				GameRegistry.addRecipe(new ShapedOreRecipe(reactorPartStack, "C C", "GDG", "CRC", 'D', Items.diamond, 'G', yelloriumIngot, 'C', "reactorCasing", 'R', Items.redstone));

				if(enableReactorPowerTapRecipe) {
					reactorPartStack = BigReactors.blockReactorPart.getReactorPowerTapItemStack();
					GameRegistry.addRecipe(new ShapedOreRecipe(reactorPartStack, "CRC", "R R", "CRC", 'C', "reactorCasing", 'R', Items.redstone));
				}

				reactorPartStack = BigReactors.blockReactorPart.getAccessPortItemStack();
				GameRegistry.addRecipe(new ShapedOreRecipe(reactorPartStack, "C C", " V ", "CPC", 'C', "reactorCasing", 'V', Blocks.chest, 'P', Blocks.piston));

				reactorPartStack = BigReactors.blockReactorPart.getCoolantPortItemStack();
				GameRegistry.addRecipe(new ShapedOreRecipe(reactorPartStack, "C C", "IVI", "CPC", 'C', "reactorCasing", 'V', Items.bucket, 'P', Blocks.piston, 'I', ironOrSteelIngot));
				
				reactorPartStack = BigReactors.blockReactorPart.getControlRodItemStack();
				GameRegistry.addRecipe(new ShapedOreRecipe(reactorPartStack, "CGC", "GRG", "CUC", 'G', "ingotGraphite", 'C', "reactorCasing", 'R', Items.redstone, 'U', yelloriumIngot));
				
				if(Loader.isModLoaded("MineFactoryReloaded")) {
					reactorPartStack = BigReactors.blockReactorPart.getRedNetPortItemStack();
					GameRegistry.addRecipe(new ShapedOreRecipe(reactorPartStack, "CRC", "RGR", "CRC", 'C', "reactorCasing", 'R', "cableRedNet", 'G', "ingotGold"));
				}
				
				if(Loader.isModLoaded("ComputerCraft") || Loader.isModLoaded("OpenComputers")) {
					reactorPartStack = BigReactors.blockReactorPart.getComputerPortItemStack();
					GameRegistry.addRecipe(new ShapedOreRecipe(reactorPartStack, "CRC", "GPG", "CRC", 'C', "reactorCasing", 'R', Items.redstone, 'G', "ingotGold", 'P', Items.repeater));
				}
			}
			
			if(blockMultiblockGlass != null) {
				ItemStack reactorGlassStack = blockMultiblockGlass.getItemStack("reactor");
				ItemStack turbineGlassStack = blockMultiblockGlass.getItemStack("turbine");
				
				if(useExpensiveGlass && (ItemHelper.oreNameExists("glassReinforced") || ItemHelper.oreNameExists("blockGlassHardened"))) {
					GameRegistry.addRecipe(new ShapedOreRecipe(reactorGlassStack, "GCG", 'G', "glassReinforced", 'C', "reactorCasing"));
					GameRegistry.addRecipe(new ShapedOreRecipe(reactorGlassStack, "GCG", 'G', "blockGlassHardened", 'C', "reactorCasing"));
					
					GameRegistry.addRecipe(new ShapedOreRecipe(turbineGlassStack, "GCG", 'G', "glassReinforced", 'C', "turbineHousing"));
					GameRegistry.addRecipe(new ShapedOreRecipe(turbineGlassStack, "GCG", 'G', "blockGlassHardened", 'C', "turbineHousing"));
				}
				else {
					GameRegistry.addRecipe(new ShapedOreRecipe(reactorGlassStack, "GCG", 'G', "blockGlassColorless", 'C', "reactorCasing"));
					GameRegistry.addRecipe(new ShapedOreRecipe(turbineGlassStack, "GCG", 'G', "blockGlassColorless", 'C', "turbineHousing"));
				}
			}
			
			if(blockDevice != null) {
				ItemStack cyaniteReprocessorStack = ((BlockBRDevice)blockDevice).getCyaniteReprocessorItemStack();
				GameRegistry.addRecipe(new ShapedOreRecipe(cyaniteReprocessorStack, "CIC", "PFP", "CRC", 'C', "reactorCasing", 'I', ironOrSteelIngot, 'F', blockYelloriumFuelRod, 'P', Blocks.piston, 'R', Items.redstone));
			}
			
			if(blockReactorRedstonePort != null) {
				ItemStack redstonePortStack = new ItemStack(BigReactors.blockReactorRedstonePort, 1);
				GameRegistry.addRecipe(new ShapedOreRecipe(redstonePortStack, "CRC", "RGR", "CRC", 'C', "reactorCasing", 'R', Items.redstone, 'G', Items.gold_ingot));
			}
			
			if(blockTurbinePart != null) {
				ItemStack turbineHousing = blockTurbinePart.getItemStack("housing");
				ItemStack turbineController = blockTurbinePart.getItemStack("controller");
				ItemStack turbinePowerTap = blockTurbinePart.getItemStack("powerTap");
				ItemStack turbineFluidPort = blockTurbinePart.getItemStack("fluidPort");
				ItemStack turbineBearing = blockTurbinePart.getItemStack("bearing");

				turbineHousing.stackSize = 4;
				GameRegistry.addRecipe(new ShapedOreRecipe(turbineHousing, "IGI", "QCQ", "IGI", 'C', "ingotCyanite", 'I', ironOrSteelIngot, 'Q', Items.quartz, 'G', "ingotGraphite"));
				GameRegistry.addRecipe(new ShapedOreRecipe(turbineController, "H H", "BDB", "H H", 'H', "turbineHousing", 'D', Items.diamond, 'B', blutoniumIngot));
				GameRegistry.addRecipe(new ShapedOreRecipe(turbinePowerTap, "HRH", "R R", "HRH", 'H', "turbineHousing", 'R', Items.redstone));
				GameRegistry.addRecipe(new ShapedOreRecipe(turbineFluidPort, "H H", "IVI", "HPH", 'H', "turbineHousing", 'I', ironOrSteelIngot, 'V', Items.bucket, 'P', Blocks.piston));
				GameRegistry.addRecipe(new ShapedOreRecipe(turbineBearing, "HRH", "DDD", "HRH", 'H', "turbineHousing", 'D', Items.diamond, 'R', "turbineRotorShaft"));

				if(Loader.isModLoaded("ComputerCraft") || Loader.isModLoaded("OpenComputers")) {
					ItemStack turbineComputerPort = blockTurbinePart.getItemStack("computerPort");
					GameRegistry.addRecipe(new ShapedOreRecipe(turbineComputerPort, "HRH", "GPG", "HRH", 'H', "turbineHousing", 'G', "ingotGold", 'R', "turbineRotorShaft"));
					
				}
			}
			
			if(blockTurbineRotorPart != null) {
				ItemStack rotorShaft = blockTurbineRotorPart.getItemStack("rotor");
				ItemStack rotorBlade = blockTurbineRotorPart.getItemStack("blade");

				GameRegistry.addRecipe(new ShapedOreRecipe(rotorShaft, "ICI", 'C', "ingotCyanite", 'I', ironOrSteelIngot));
				GameRegistry.addRecipe(new ShapedOreRecipe(rotorBlade, "CII", 'C', "ingotCyanite", 'I', ironOrSteelIngot));
			}
			
			if(blockExchangerPart != null) {
				// TODO: Register recipes for heat exchanger parts
			}
			
			if(blockExchangerInteriorPart != null) {
				// TODO: Register recipes for heat exchanger pipe parts
			}
			
			registerGameBalanceData();
		}

		INITIALIZED = true;
	}
	
	
	/**
	 * Call this to register Tile Entities
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
			blockYelloriteOre = new BlockBROre();
			GameRegistry.registerBlock(BigReactors.blockYelloriteOre, ItemBlockBigReactors.class, "YelloriteOre");
			ItemStack yelloriteStack = new ItemStack(blockYelloriteOre, 1);
			OreDictionary.registerOre("oreYellorite", yelloriteStack);
			OreDictionary.registerOre("oreYellorium", yelloriteStack); // For convenience of mods which fiddle with recipes
		}

		if(blockMetal == null) {
			blockMetal = new BlockBRMetal();
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
			
			yelloriteOreGeneration = new BRSimpleOreGenerator(blockYelloriteOre, 0, Blocks.stone,
											clustersPerChunk/2, clustersPerChunk, 4, maxY, orePerCluster);

			// Per KingLemming's request, bonus yellorite around y12. :)
			BRSimpleOreGenerator yelloriteOreGeneration2 = new BRSimpleOreGenerator(blockYelloriteOre, 0, Blocks.stone,
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
			BigReactors.ingotGeneric = new ItemIngot();
            GameRegistry.registerItem(ingotGeneric, "BRIngot");

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
		if(BigReactors.blockYelloriumFuelRod == null) {
			BRConfig.CONFIGURATION.load();
			BigReactors.blockYelloriumFuelRod = new BlockFuelRod(Material.iron);
			GameRegistry.registerBlock(BigReactors.blockYelloriumFuelRod, ItemBlock.class, "YelloriumFuelRod");
			BRConfig.CONFIGURATION.save();
		}
	}


	public static void registerReactorPartBlocks(int id, boolean require) {
		if(BigReactors.blockReactorPart == null) {
			BRConfig.CONFIGURATION.load();
			BigReactors.blockReactorPart = new BlockReactorPart(Material.iron);
			GameRegistry.registerBlock(BigReactors.blockReactorPart, ItemBlockBigReactors.class, "BRReactorPart");

			OreDictionary.registerOre("reactorCasing", BigReactors.blockReactorPart.getReactorCasingItemStack());
			OreDictionary.registerOre("reactorController", BigReactors.blockReactorPart.getReactorControllerItemStack());
			OreDictionary.registerOre("reactorPowerTap", BigReactors.blockReactorPart.getReactorPowerTapItemStack());
			OreDictionary.registerOre("reactorRedNetPort", BigReactors.blockReactorPart.getRedNetPortItemStack());
			OreDictionary.registerOre("reactorComputerPort", BigReactors.blockReactorPart.getComputerPortItemStack());
			OreDictionary.registerOre("reactorCoolantPort", BigReactors.blockReactorPart.getCoolantPortItemStack());
			OreDictionary.registerOre("reactorControlRod", BigReactors.blockReactorPart.getControlRodItemStack());

			BRConfig.CONFIGURATION.save();
		}
		
		if(BigReactors.blockMultiblockGlass == null) {
			BRConfig.CONFIGURATION.load();
			
			BigReactors.blockMultiblockGlass = new BlockMultiblockGlass(Material.glass);
			GameRegistry.registerBlock(BigReactors.blockMultiblockGlass, ItemBlockBigReactors.class, "BRMultiblockGlass");
			
			OreDictionary.registerOre("glassReactor", blockMultiblockGlass.getItemStack("reactor"));
			OreDictionary.registerOre("glassTurbine", blockMultiblockGlass.getItemStack("turbine"));
			
			BRConfig.CONFIGURATION.save();
		}
		
		if(BigReactors.blockReactorRedstonePort == null) {
			BRConfig.CONFIGURATION.load();
			
			BigReactors.blockReactorRedstonePort = new BlockReactorRedstonePort(Material.iron);
			GameRegistry.registerBlock(BigReactors.blockReactorRedstonePort, ItemBlock.class, "BRReactorRedstonePort");
			OreDictionary.registerOre("reactorRedstonePort", new ItemStack(blockReactorRedstonePort, 1));
			
			BRConfig.CONFIGURATION.save();
		}
	}
	
	public static void registerTurbineParts() {
		if(BigReactors.blockTurbinePart == null) {
			BigReactors.blockTurbinePart = new BlockTurbinePart(Material.iron);
			GameRegistry.registerBlock(BigReactors.blockTurbinePart, ItemBlockBigReactors.class, "BRTurbinePart");

			OreDictionary.registerOre("turbineHousing", BigReactors.blockTurbinePart.getItemStack("housing"));
			OreDictionary.registerOre("turbineController", BigReactors.blockTurbinePart.getItemStack("controller"));
			OreDictionary.registerOre("turbinePowerTap", BigReactors.blockTurbinePart.getItemStack("powerTap"));
			OreDictionary.registerOre("turbineFluidPort", BigReactors.blockTurbinePart.getItemStack("fluidPort"));
			OreDictionary.registerOre("turbineBearing", BigReactors.blockTurbinePart.getItemStack("bearing"));
		}

		if(BigReactors.blockTurbineRotorPart == null) {
			BigReactors.blockTurbineRotorPart = new BlockTurbineRotorPart(Material.iron);
			GameRegistry.registerBlock(BigReactors.blockTurbineRotorPart, ItemBlockBigReactors.class, "BRTurbineRotorPart");

			OreDictionary.registerOre("turbineRotorShaft", BigReactors.blockTurbineRotorPart.getItemStack("rotor"));
			OreDictionary.registerOre("turbineRotorBlade", BigReactors.blockTurbineRotorPart.getItemStack("blade"));
		}
	}
	
	public static void registerExchangerParts() {
		if(BigReactors.blockExchangerPart == null) {
			BigReactors.blockExchangerPart = new BlockExchangerPart(Material.iron);
			GameRegistry.registerBlock(BigReactors.blockExchangerPart, ItemBlockBigReactors.class, "BRExchangerPart");
			
			OreDictionary.registerOre("heatExchangerCasing", BigReactors.blockExchangerPart.getItemStack("casing"));
			OreDictionary.registerOre("heatExchangerController", BigReactors.blockExchangerPart.getItemStack("controller"));
			OreDictionary.registerOre("heatExchangerFluidPort", BigReactors.blockExchangerPart.getItemStack("fluidPort"));
			OreDictionary.registerOre("heatExchangerComputerPort", BigReactors.blockExchangerPart.getItemStack("computerPort"));
		}
		
		if(BigReactors.blockExchangerInteriorPart == null) {
			BigReactors.blockExchangerInteriorPart = new BlockExchangerInteriorPart(Material.iron);
			GameRegistry.registerBlock(BigReactors.blockExchangerInteriorPart, ItemBlockBigReactors.class,"BRExchangerInteriorPart");
			
			OreDictionary.registerOre("heatExchangerInteriorPrimaryPipe", BigReactors.blockExchangerInteriorPart.getItemStack("primary"));
			OreDictionary.registerOre("heatExchangerInteriorSecondaryPipe", BigReactors.blockExchangerInteriorPart.getItemStack("secondary"));
		}
	}
	
	public static void registerDevices(int id, boolean require) {
		if(BigReactors.blockDevice == null) {
			BRConfig.CONFIGURATION.load();

			BigReactors.blockDevice = new BlockBRDevice(Material.iron);
			GameRegistry.registerBlock(BigReactors.blockDevice, ItemBlockBigReactors.class, "BRDevice");
			
			OreDictionary.registerOre("brDeviceCyaniteProcessor", ((BlockBRDevice)BigReactors.blockDevice).getCyaniteReprocessorItemStack());
			
			BRConfig.CONFIGURATION.save();
		}
	}
	
	public static void registerCreativeParts(int id, boolean require) {
		BRConfig.CONFIGURATION.load();
		
		boolean regCreativeParts = BRConfig.CONFIGURATION.get("General", "registerCreativeMultiblockParts", true, "If true, creative parts for reactors, turbines and other multiblocks will be registered.").getBoolean(true);
		if(regCreativeParts && BigReactors.blockMultiblockCreativePart == null) {
			BigReactors.blockMultiblockCreativePart = new BlockMBCreativePart(Material.iron);
			GameRegistry.registerBlock(BigReactors.blockMultiblockCreativePart, ItemBlockBigReactors.class, "BRMultiblockCreativePart");
		}
		
		BRConfig.CONFIGURATION.save();
	}
	
	public static void registerFluids(int id, boolean require) {
		if(BigReactors.fluidYelloriumStill == null) {
			BRConfig.CONFIGURATION.load();
			
			BigReactors.fluidYellorium = FluidRegistry.getFluid("yellorium");
			if(fluidYellorium == null) {
				fluidYellorium = new Fluid("yellorium");
				fluidYellorium.setDensity(100);
				fluidYellorium.setGaseous(false);
				fluidYellorium.setLuminosity(10);
				fluidYellorium.setRarity(EnumRarity.uncommon);
				fluidYellorium.setTemperature(295);
				fluidYellorium.setViscosity(100);
				fluidYellorium.setUnlocalizedName("bigreactors.yellorium.still");
				FluidRegistry.registerFluid(fluidYellorium);
			}

			BlockBRGenericFluid liqY = new BlockBRGenericFluid(BigReactors.fluidYellorium, "yellorium");
			BigReactors.fluidYelloriumStill = liqY;
			
			GameRegistry.registerBlock(BigReactors.fluidYelloriumStill, ItemBlock.class, BigReactors.fluidYelloriumStill.getUnlocalizedName());

			fluidYelloriumBucketItem = (new ItemBRBucket(liqY)).setUnlocalizedName("bucket.yellorium").setMaxStackSize(1).setContainerItem(Items.bucket);
            GameRegistry.registerItem(fluidYelloriumBucketItem, "bucketYellorium");
			
			BRConfig.CONFIGURATION.save();
		}
		
		if(BigReactors.fluidCyaniteStill == null) {
			BRConfig.CONFIGURATION.load();
			
			BigReactors.fluidCyanite = FluidRegistry.getFluid("cyanite");
			if(fluidCyanite == null) {
				fluidCyanite = new Fluid("cyanite");
				fluidCyanite.setDensity(100);
				fluidCyanite.setGaseous(false);
				fluidCyanite.setLuminosity(6);
				fluidCyanite.setRarity(EnumRarity.uncommon);
				fluidCyanite.setTemperature(295);
				fluidCyanite.setViscosity(100);
				fluidCyanite.setUnlocalizedName("bigreactors.cyanite.still");
				FluidRegistry.registerFluid(fluidCyanite);
			}

			BlockBRGenericFluid liqDY = new BlockBRGenericFluid(fluidCyanite, "cyanite");
			BigReactors.fluidCyaniteStill = liqDY;
			GameRegistry.registerBlock(BigReactors.fluidCyaniteStill, ItemBlock.class, BigReactors.fluidCyaniteStill.getUnlocalizedName());
			
			fluidCyaniteBucketItem = (new ItemBRBucket(liqDY)).setUnlocalizedName("bucket.cyanite").setMaxStackSize(1).setContainerItem(Items.bucket);
            GameRegistry.registerItem(fluidCyaniteBucketItem, "bucketCyanite");
			
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
		// Register water<>steam
		StandardFluids.register();

		// Register ingot & block => reactant mappings
		StandardReactants.yelloriumMapping = Reactants.registerSolid("ingotYellorium", StandardReactants.yellorium);
		StandardReactants.cyaniteMapping = Reactants.registerSolid("ingotCyanite", StandardReactants.cyanite);

		Reactants.registerSolid("ingotBlutonium",  StandardReactants.blutonium);
		
		ItemStack blockYellorium = blockMetal.getItemStackForMaterial("Yellorium");
		Reactants.registerSolid(blockYellorium, StandardReactants.yellorium, Reactants.standardSolidReactantAmount * 9);

		ItemStack blockBlutonium = blockMetal.getItemStackForMaterial("Blutonium");
		Reactants.registerSolid(blockBlutonium, StandardReactants.blutonium, Reactants.standardSolidReactantAmount * 9);

		// Register fluid => reactant mappings
		Reactants.registerFluid(fluidYellorium, StandardReactants.yellorium);
		Reactants.registerFluid(fluidCyanite, StandardReactants.cyanite);
		
		// Register reactant => reactant conversions for making cyanite
		ReactorConversions.register(StandardReactants.yellorium, StandardReactants.cyanite);
		ReactorConversions.register(StandardReactants.blutonium, StandardReactants.cyanite);
		
		BRConfig.CONFIGURATION.load();
		boolean enableFantasyMetals = BRConfig.CONFIGURATION.get("General", "enableMetallurgyFantasyMetalsInTurbines", true, "If true, allows Metallurgy's fantasy metals to be used as part of turbine coils. Default: true").getBoolean(true);
		boolean enableComedy 		= BRConfig.CONFIGURATION.get("General", "enableComedy", true, "If true, allows weird stuff inside reactors, like MFR sewage and pink slime. Default: true").getBoolean(true);
		BRConfig.CONFIGURATION.save();

		TurbineCoil.registerBlock("blockIron", 1f, 1f, 1f);
		TurbineCoil.registerBlock("blockGold", 2f, 1f, 1.75f);

		TurbineCoil.registerBlock("blockCopper",		1.2f, 1f, 1.2f);	// TE, lots of mods
		TurbineCoil.registerBlock("blockOsmium",		1.2f, 1f, 1.2f);	// Mekanism
		TurbineCoil.registerBlock("blockZinc",		1.35f, 1f, 1.3f);
		TurbineCoil.registerBlock("blockLead",		1.35f, 1.01f, 1.3f);// TE, Mekanism, some others
		TurbineCoil.registerBlock("blockBrass",		1.4f, 1f, 1.2f);	// Metallurgy
		TurbineCoil.registerBlock("blockBronze",		1.4f, 1f, 1.2f);	// Mekanism, many others
		TurbineCoil.registerBlock("blockAluminum",	1.5f, 1f, 1.3f);	// TiCo, couple others
		TurbineCoil.registerBlock("blockSteel",		1.5f, 1f, 1.3f);	// Metallurgy, Mek, etc.
		TurbineCoil.registerBlock("blockInvar", 		1.5f, 1f, 1.4f);	// TE
		TurbineCoil.registerBlock("blockSilver", 		1.7f, 1f, 1.5f);	// TE, lots of mods
		TurbineCoil.registerBlock("blockElectrum", 	2.5f, 1f, 2.0f);	// TE, lots of mods
		TurbineCoil.registerBlock("blockElectrumFlux",2.5f, 1.01f, 2.2f);	// Redstone Arsenal, note small energy bonus (7% at 1000RF/t output)
		TurbineCoil.registerBlock("blockPlatinum",	3.0f, 1f, 2.5f);	// TE, lots of mods
		TurbineCoil.registerBlock("blockShiny",		3.0f, 1f, 2.5f);	// TE
		TurbineCoil.registerBlock("blockTitanium",	3.1f, 1f, 2.7f);	// Mariculture
		TurbineCoil.registerBlock("blockEnderium",	3.0f, 1.02f, 3.0f);	// TE, note tiny energy bonus!	(14% at 1000RF/t output)

		TurbineCoil.registerBlock("blockLudicrite",  3.5f, 1.02f, 3.5f);
		
		if(enableFantasyMetals) {
			// Metallurgy fantasy metals
			TurbineCoil.registerBlock("blockMithril", 	2.2f, 1f, 1.5f);
			TurbineCoil.registerBlock("blockOrichalcum", 	2.3f, 1f, 1.7f);
			TurbineCoil.registerBlock("blockQuicksilver",	2.6f, 1f, 1.8f);
			TurbineCoil.registerBlock("blockHaderoth",	3.0f, 1f, 2.0f);
			TurbineCoil.registerBlock("blockCelenegil",	3.3f, 1f, 2.25f);
			TurbineCoil.registerBlock("blockTartarite",	3.5f, 1f, 2.5f);
			TurbineCoil.registerBlock("blockManyullyn",	3.5f, 1f, 2.5f);
		}
		
		ReactorInterior.registerBlock("blockIron",		0.50f, 0.75f, 1.40f, IHeatEntity.conductivityIron);
		ReactorInterior.registerBlock("blockGold",		0.52f, 0.80f, 1.45f, IHeatEntity.conductivityGold);
		ReactorInterior.registerBlock("blockDiamond",		0.55f, 0.85f, 1.50f, IHeatEntity.conductivityDiamond);
		ReactorInterior.registerBlock("blockEmerald",		0.55f, 0.85f, 1.50f, IHeatEntity.conductivityEmerald);
		ReactorInterior.registerBlock("blockGraphite",	0.10f, 0.50f, 2.00f, IHeatEntity.conductivityGold); // Graphite: a great moderator!
		ReactorInterior.registerBlock("blockGlassColorless", 0.20f, 0.25f, 1.10f, IHeatEntity.conductivityGlass);
		ReactorInterior.registerBlock("blockIce",			0.33f, 0.33f, 1.15f, IHeatEntity.conductivityWater);
		ReactorInterior.registerBlock("blockSnow",		0.15f, 0.33f, 1.05f, IHeatEntity.conductivityWater / 2f);
		
		// Mod blocks
		ReactorInterior.registerBlock("blockCopper", 		0.50f, 0.75f, 1.40f, IHeatEntity.conductivityCopper);
		ReactorInterior.registerBlock("blockOsmium", 		0.51f, 0.77f, 1.41f, IHeatEntity.conductivityCopper);
		ReactorInterior.registerBlock("blockBrass", 		0.51f, 0.77f, 1.41f, IHeatEntity.conductivityCopper);
		ReactorInterior.registerBlock("blockBronze", 		0.51f, 0.77f, 1.41f, IHeatEntity.conductivityCopper);
		ReactorInterior.registerBlock("blockZinc",		0.51f, 0.77f, 1.41f, IHeatEntity.conductivityCopper);
		ReactorInterior.registerBlock("blockAluminum", 	0.50f, 0.78f, 1.42f, IHeatEntity.conductivityIron);
		ReactorInterior.registerBlock("blockSteel",	 	0.50f, 0.78f, 1.42f, IHeatEntity.conductivityIron);
		ReactorInterior.registerBlock("blockInvar",	 	0.50f, 0.79f, 1.43f, IHeatEntity.conductivityIron);
		ReactorInterior.registerBlock("blockSilver", 		0.51f, 0.79f, 1.43f, IHeatEntity.conductivitySilver);
		ReactorInterior.registerBlock("blockLead",		0.75f, 0.75f, 1.75f, IHeatEntity.conductivitySilver);
		ReactorInterior.registerBlock("blockElectrum", 	0.53f, 0.82f, 1.47f, 2.2f); // Between gold and emerald
		ReactorInterior.registerBlock("blockElectrumFlux",0.54f, 0.83f, 1.48f, 2.4f); // Between gold and emerald
		ReactorInterior.registerBlock("blockPlatinum", 	0.57f, 0.86f, 1.58f, IHeatEntity.conductivityEmerald);
		ReactorInterior.registerBlock("blockShiny", 		0.57f, 0.86f, 1.58f, IHeatEntity.conductivityEmerald);		
		ReactorInterior.registerBlock("blockTitanium", 	0.58f, 0.87f, 1.59f, 2.7f); // Mariculture
		ReactorInterior.registerBlock("blockEnderium", 	0.60f, 0.88f, 1.60f, IHeatEntity.conductivityDiamond);

		if(enableFantasyMetals) {
			ReactorInterior.registerBlock("blockMithril", 	0.53f, 0.81f, 1.45f, IHeatEntity.conductivitySilver);
			ReactorInterior.registerBlock("blockOrichalcum", 	0.52f, 0.83f, 1.46f, 1.7f);	// Between silver and gold
			ReactorInterior.registerBlock("blockQuicksilver", 0.53f, 0.84f, 1.48f, IHeatEntity.conductivityGold);
			ReactorInterior.registerBlock("blockHaderoth", 	0.54f, 0.84f, 1.49f, IHeatEntity.conductivityEmerald);
			ReactorInterior.registerBlock("blockCelenegil", 	0.54f, 0.84f, 1.49f, IHeatEntity.conductivityDiamond);
			ReactorInterior.registerBlock("blockTartarite", 	0.65f, 0.90f, 1.62f, 4f); // Between diamond and graphene
			ReactorInterior.registerBlock("blockManyullyn",	0.68f, 0.88f, 1.75f, 4.5f);
		}

		//Water: 0.33f, 0.5f, 1.33f
		ReactorInterior.registerFluid("water", RadiationHelper.waterData.absorption, RadiationHelper.waterData.heatEfficiency, RadiationHelper.waterData.moderation, IHeatEntity.conductivityWater);
		ReactorInterior.registerFluid("redstone",		0.75f, 0.55f, 1.60f, IHeatEntity.conductivityEmerald);
		ReactorInterior.registerFluid("glowstone",	0.20f, 0.60f, 1.75f, IHeatEntity.conductivityCopper);
		ReactorInterior.registerFluid("cryotheum",	0.66f, 0.95f, 6.00f, IHeatEntity.conductivityDiamond); // Cryotheum: an amazing moderator!
		ReactorInterior.registerFluid("ender",		0.90f, 0.75f, 2.00f, IHeatEntity.conductivityGold);
		ReactorInterior.registerFluid("pyrotheum",	0.66f, 0.90f, 1.00f, IHeatEntity.conductivityIron);
		
		ReactorInterior.registerFluid("life essence", 0.70f, 0.55f, 1.75f, IHeatEntity.conductivityGold); // From Blood Magic

		if(enableComedy) {
			ReactorInterior.registerBlock("blockMeat", 	0.50f, 0.33f, 1.33f, IHeatEntity.conductivityStone);
			ReactorInterior.registerBlock("blockMeatRaw",	0.40f, 0.50f, 1.50f, IHeatEntity.conductivityStone);
			ReactorInterior.registerFluid("meat",			0.40f, 0.60f, 1.33f, IHeatEntity.conductivityStone);
			ReactorInterior.registerFluid("pinkSlime",	0.45f, 0.70f, 1.50f, IHeatEntity.conductivityIron);
			ReactorInterior.registerFluid("sewage",		0.50f, 0.65f, 1.44f, IHeatEntity.conductivityIron);
		}
	}
	
	public static void registerItems() {
		if(itemDebugTool == null) {
			itemDebugTool = new ItemBeefDebugTool();
			GameRegistry.registerItem(itemDebugTool, "BRDebugTool");
		}
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

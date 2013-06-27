package erogenousbeef.bigreactors.common;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import appeng.api.Util;

import thermalexpansion.api.crafting.CraftingHelpers;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.oredict.OreDictionary;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.ReflectionHelper;
import cpw.mods.fml.relauncher.Side;
import erogenousbeef.bigreactors.gui.BigReactorsGUIHandler;
import erogenousbeef.bigreactors.common.item.ItemIngot;

public class CommonProxy {

	public void preInit() {
	}

	public void init() {
		BigReactors.registerTileEntities();
		
		NetworkRegistry.instance().registerGuiHandler(BRLoader.instance, new BigReactorsGUIHandler());
		BigReactors.tickHandler = new BigReactorsTickHandler();
		TickRegistry.registerTickHandler(BigReactors.tickHandler, Side.SERVER);
	}

	public void postInit() {
		BRConfig.CONFIGURATION.load();
		boolean autoAddUranium = BRConfig.CONFIGURATION.get("Compatibility", "autoAddUranium",
															true,
															"If true, automatically adds all "
															+"unregistered ingots found as clones"
															+"of standard yellorium fuel").getBoolean(true);
		if(autoAddUranium) {
			List<ItemStack> candidates = OreDictionary.getOres("ingotUranium");
			for(ItemStack candidate : candidates) {
				// If they're already registered, this will NOT overwrite the existing registration
				BRRegistry.registerFuel(new ReactorFuel(candidate, BigReactors.defaultLiquidColorFuel));
			}
		}

		BRConfig.CONFIGURATION.save();
		
		ItemIngot ingotGeneric = ((ItemIngot)BigReactors.ingotGeneric);
		
		ItemStack yelloriteOre 	= new ItemStack(BigReactors.blockYelloriteOre, 1);
		ItemStack ingotYellorium= ingotGeneric.getItemStackForType("ingotYellorium");
		ItemStack ingotCyanite 	= ingotGeneric.getItemStackForType("ingotCyanite");
		ItemStack ingotGraphite = ingotGeneric.getItemStackForType("ingotGraphite");
		ItemStack ingotBlutonium= ingotGeneric.getItemStackForType("ingotBlutonium");
		ItemStack dustYellorium = ingotGeneric.getItemStackForType("dustYellorium");
		ItemStack dustCyanite 	= ingotGeneric.getItemStackForType("dustCyanite");
		ItemStack dustGraphite 	= ingotGeneric.getItemStackForType("dustGraphite");
		ItemStack dustBlutonium = ingotGeneric.getItemStackForType("dustBlutonium");

		// Some mods make me do this myself. :V
		ItemStack doubledYelloriumDust = dustYellorium.copy();
		doubledYelloriumDust.stackSize = 2;
		
		
		if(Loader.isModLoaded("ThermalExpansion")) {
			if(yelloriteOre != null && ingotYellorium != null) {
				CraftingHelpers.addSmelterOreToIngotsRecipe(yelloriteOre.copy(), ingotYellorium.copy());
			}
			
			if(yelloriteOre != null && dustYellorium != null) {
				CraftingHelpers.addPulverizerOreToDustRecipe(yelloriteOre.copy(), dustYellorium.copy());
			}
			
			if(ingotYellorium != null && dustYellorium != null) {
				CraftingHelpers.addPulverizerIngotToDustRecipe(ingotYellorium.copy(), dustYellorium.copy());
				CraftingHelpers.addSmelterDustToIngotsRecipe(dustYellorium.copy(), ingotYellorium.copy());
			}

			if(ingotCyanite != null && dustCyanite != null) {
				CraftingHelpers.addPulverizerIngotToDustRecipe(ingotCyanite.copy(), dustCyanite.copy());
				CraftingHelpers.addSmelterDustToIngotsRecipe(dustCyanite.copy(), ingotCyanite.copy());
			}

			if(ingotGraphite != null && dustGraphite != null) {
				CraftingHelpers.addPulverizerIngotToDustRecipe(ingotGraphite.copy(), dustGraphite.copy());
				CraftingHelpers.addSmelterDustToIngotsRecipe(dustGraphite.copy(), ingotGraphite.copy());
			}

			if(ingotBlutonium != null && dustBlutonium != null) {
				CraftingHelpers.addPulverizerIngotToDustRecipe(ingotBlutonium.copy(), dustBlutonium.copy());
				CraftingHelpers.addSmelterDustToIngotsRecipe(dustBlutonium.copy(), ingotBlutonium.copy());
			}
		}
		
		if(Loader.isModLoaded("AppliedEnergistics")) {
			appeng.api.IGrinderRecipeManager grinderRM = appeng.api.Util.getGrinderRecipeManage();

			if(grinderRM != null) {
				if(yelloriteOre != null && dustYellorium != null) {
					grinderRM.addRecipe(yelloriteOre.copy(), doubledYelloriumDust.copy(), 4);
				}
			
				if(ingotYellorium != null && dustYellorium != null) {
					grinderRM.addRecipe(ingotYellorium.copy(), dustYellorium.copy(), 2);
				}

				if(ingotCyanite != null && dustCyanite != null) {
					grinderRM.addRecipe(ingotCyanite.copy(), dustCyanite.copy(), 2);
				}

				if(ingotGraphite != null && dustGraphite != null) {
					grinderRM.addRecipe(ingotGraphite.copy(), dustGraphite.copy(), 2);
				}

				if(ingotBlutonium != null && dustBlutonium != null) {
					grinderRM.addRecipe(ingotBlutonium.copy(), dustBlutonium.copy(), 2);
				}
			}
		}
		
		if(Loader.isModLoaded("Mekanism")) {
			if(yelloriteOre != null && dustYellorium != null) {
				addMekanismEnrichmentChamberRecipe(yelloriteOre.copy(), doubledYelloriumDust.copy());
				addMekanismCombinerRecipe(dustYellorium.copy(), yelloriteOre.copy());
			}
		
			if(ingotYellorium != null && dustYellorium != null) {
				addMekanismCrusherRecipe(ingotYellorium.copy(), dustYellorium.copy());
			}

			if(ingotCyanite != null && dustCyanite != null) {
				addMekanismCrusherRecipe(ingotCyanite.copy(), dustCyanite.copy());
			}

			if(ingotGraphite != null && dustGraphite != null) {
				addMekanismCrusherRecipe(ingotGraphite.copy(), dustGraphite.copy());
			}

			if(ingotBlutonium != null && dustBlutonium != null) {
				addMekanismCrusherRecipe(ingotBlutonium.copy(), dustBlutonium.copy());
			}
		}
	}
	
	/// Mekanism Compat - taken from Mekanism's API. Extracted to allow compat with last known green build.
	/**
	 * Add an Enrichment Chamber recipe. (Ore -> 2 Dust)
	 * @param input - input ItemStack
	 * @param output - output ItemStack
	 */
	public static void addMekanismEnrichmentChamberRecipe(ItemStack input, ItemStack output)
	{
		try {
			Class recipeClass = Class.forName("mekanism.common.RecipeHandler");
			Method m = recipeClass.getMethod("addEnrichmentChamberRecipe", ItemStack.class, ItemStack.class);
			m.invoke(null, input, output);
		} catch(Exception e) {
			System.err.println("[Mekanism] Error while adding recipe: " + e.getMessage());
		}
	}

	
	/**
	 * Add a Combiner recipe. (2 Dust + Cobble -> Ore)
	 * @param input - input ItemStack
	 * @param output - output ItemStack
	 */
	public static void addMekanismCombinerRecipe(ItemStack input, ItemStack output)
	{
		try {
			Class recipeClass = Class.forName("mekanism.common.RecipeHandler");
			Method m = recipeClass.getMethod("addCombinerRecipe", ItemStack.class, ItemStack.class);
			m.invoke(null, input, output);
		} catch(Exception e) {
			System.err.println("[Mekanism] Error while adding recipe: " + e.getMessage());
		}
	}
	
	/**
	 * Add a Crusher recipe. (Ingot -> Dust)
	 * @param input - input ItemStack
	 * @param output - output ItemStack
	 */
	public static void addMekanismCrusherRecipe(ItemStack input, ItemStack output)
	{
		try {
			Class recipeClass = Class.forName("mekanism.common.RecipeHandler");
			Method m = recipeClass.getMethod("addCrusherRecipe", ItemStack.class, ItemStack.class);
			m.invoke(null, input, output);
		} catch(Exception e) {
			System.err.println("[Mekanism] Error while adding recipe: " + e.getMessage());
		}
	}	
}

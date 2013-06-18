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
		
		
		List<ItemStack> yelloriteOres = OreDictionary.getOres("oreYellorite");
		List<ItemStack> yelloriumIngots = OreDictionary.getOres("ingotUranium");
		List<ItemStack> cyaniteIngots = OreDictionary.getOres("ingotCyanite");
		List<ItemStack> graphiteIngots = OreDictionary.getOres("ingotGraphite");
		List<ItemStack> blutoniumIngots = OreDictionary.getOres("ingotPlutonium");
		List<ItemStack> dustYelloriums = OreDictionary.getOres("dustUranium");
		List<ItemStack> dustCyanites = OreDictionary.getOres("dustCyanite");
		List<ItemStack> dustGraphites = OreDictionary.getOres("dustGraphite");
		List<ItemStack> dustBlutonums = OreDictionary.getOres("dustPlutonium");

		if(Loader.isModLoaded("ThermalExpansion")) {
			if(yelloriteOres != null && !yelloriteOres.isEmpty() && dustYelloriums != null && !dustYelloriums.isEmpty()) {
				CraftingHelpers.addPulverizerOreToDustRecipe(yelloriteOres.get(0), dustYelloriums.get(0));
			}
			
			if(yelloriumIngots != null && !yelloriumIngots.isEmpty() && dustYelloriums != null && !dustYelloriums.isEmpty()) {
				CraftingHelpers.addPulverizerIngotToDustRecipe(yelloriumIngots.get(0), dustYelloriums.get(0));
				CraftingHelpers.addSmelterDustToIngotsRecipe(dustYelloriums.get(0), yelloriumIngots.get(0));
			}

			if(cyaniteIngots != null && !cyaniteIngots.isEmpty() && dustCyanites != null && !dustCyanites.isEmpty()) {
				CraftingHelpers.addPulverizerIngotToDustRecipe(cyaniteIngots.get(0), dustCyanites.get(0));
				CraftingHelpers.addSmelterDustToIngotsRecipe(dustCyanites.get(0), cyaniteIngots.get(0));
			}

			if(graphiteIngots != null && !graphiteIngots.isEmpty() && dustGraphites != null && !dustGraphites.isEmpty()) {
				CraftingHelpers.addPulverizerIngotToDustRecipe(graphiteIngots.get(0), dustGraphites.get(0));
				CraftingHelpers.addSmelterDustToIngotsRecipe(dustGraphites.get(0), graphiteIngots.get(0));
			}

			if(blutoniumIngots != null && !blutoniumIngots.isEmpty() && dustBlutonums != null && !dustBlutonums.isEmpty()) {
				CraftingHelpers.addPulverizerIngotToDustRecipe(blutoniumIngots.get(0), dustBlutonums.get(0));
				CraftingHelpers.addSmelterDustToIngotsRecipe(dustBlutonums.get(0), blutoniumIngots.get(0));
			}
		}
		
		if(Loader.isModLoaded("AppliedEnergistics")) {
			// TODO: Tell AlgorithmX2 that this method is broken :(
			appeng.api.IGrinderRecipeManager grinderRM = appeng.api.Util.getGrinderRecipeManage();

			if(grinderRM != null) {
				if(yelloriteOres != null && !yelloriteOres.isEmpty() && dustYelloriums != null && !dustYelloriums.isEmpty()) {
					grinderRM.addRecipe(yelloriteOres.get(0), dustYelloriums.get(0), 4);
				}
			
				if(yelloriumIngots != null && !yelloriumIngots.isEmpty() && dustYelloriums != null && !dustYelloriums.isEmpty()) {
					grinderRM.addRecipe(yelloriumIngots.get(0), dustYelloriums.get(0), 2);
				}

				if(cyaniteIngots != null && !cyaniteIngots.isEmpty() && dustCyanites != null && !dustCyanites.isEmpty()) {
					grinderRM.addRecipe(cyaniteIngots.get(0), dustCyanites.get(0), 2);
				}

				if(graphiteIngots != null && !graphiteIngots.isEmpty() && dustGraphites != null && !dustGraphites.isEmpty()) {
					grinderRM.addRecipe(graphiteIngots.get(0), dustGraphites.get(0), 2);
				}

				if(blutoniumIngots != null && !blutoniumIngots.isEmpty() && dustBlutonums != null && !dustBlutonums.isEmpty()) {
					grinderRM.addRecipe(blutoniumIngots.get(0), dustBlutonums.get(0), 2);
				}
			}
		}
	}
}

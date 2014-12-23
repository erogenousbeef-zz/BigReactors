package erogenousbeef.bigreactors.utils.intermod;

import java.lang.reflect.Method;

import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.Optional;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.item.ItemIngot;

public class ModHelperMekanism extends ModHelperBase {

	@Optional.Method(modid = "Mekanism")
	@Override
	public void register() {
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
		ItemStack doubledYelloriumDust = null;
		if(dustYellorium != null) {
			doubledYelloriumDust = dustYellorium.copy();
			doubledYelloriumDust.stackSize = 2;
		}

		if(yelloriteOre != null && doubledYelloriumDust != null) {
			addMekanismEnrichmentChamberRecipe(yelloriteOre.copy(), doubledYelloriumDust.copy());
			ItemStack octupledYelloriumDust = dustYellorium.copy();
			octupledYelloriumDust.stackSize = 8;
			addMekanismCombinerRecipe(octupledYelloriumDust, yelloriteOre.copy());
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
	
	/// Mekanism Compat - taken from Mekanism's API. Extracted to allow compat with last known green build.
	/**
	 * Add an Enrichment Chamber recipe. (Ore -> 2 Dust)
	 * @param input - input ItemStack
	 * @param output - output ItemStack
	 */
	public static void addMekanismEnrichmentChamberRecipe(ItemStack input, ItemStack output)
	{
		try {
			Class recipeClass = Class.forName("mekanism.api.RecipeHelper");
			Method m = recipeClass.getMethod("addEnrichmentChamberRecipe", ItemStack.class, ItemStack.class);
			m.invoke(null, input, output);
		} catch(Exception e) {
			System.err.println("[Mekanism] Error while adding recipe: " + e.getMessage());
		}
	}

	/**
	 * Add a Combiner recipe. (8 Dust + Cobble -> Ore)
	 * @param input - input ItemStack
	 * @param output - output ItemStack
	 */
	public static void addMekanismCombinerRecipe(ItemStack input, ItemStack output)
	{
		try {
			Class recipeClass = Class.forName("mekanism.api.RecipeHelper");
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
			Class recipeClass = Class.forName("mekanism.api.RecipeHelper");
			Method m = recipeClass.getMethod("addCrusherRecipe", ItemStack.class, ItemStack.class);
			m.invoke(null, input, output);
		} catch(Exception e) {
			System.err.println("[Mekanism] Error while adding recipe: " + e.getMessage());
		}
	}
}

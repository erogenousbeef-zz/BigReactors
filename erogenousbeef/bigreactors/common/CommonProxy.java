package erogenousbeef.bigreactors.common;

import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;
import powercrystals.minefactoryreloaded.api.FactoryRegistry;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import erogenousbeef.bigreactors.common.data.ReactorSolidMapping;
import erogenousbeef.bigreactors.common.item.ItemIngot;
import erogenousbeef.bigreactors.gui.BigReactorsGUIHandler;
import erogenousbeef.core.multiblock.MultiblockServerTickHandler;

public class CommonProxy {

	public void preInit() {
	}

	public void init() {
		BigReactors.registerTileEntities();
		
		NetworkRegistry.instance().registerGuiHandler(BRLoader.instance, new BigReactorsGUIHandler());
		BigReactors.tickHandler = new BigReactorsTickHandler();
		TickRegistry.registerTickHandler(BigReactors.tickHandler, Side.SERVER);
		TickRegistry.registerTickHandler(new MultiblockServerTickHandler(), Side.SERVER);
		
		sendInterModAPIMessages();
	}

	private void sendInterModAPIMessages() {
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

		ItemStack doubledYelloriumDust = null;
		if(dustYellorium != null) {
			doubledYelloriumDust = dustYellorium.copy();
			doubledYelloriumDust.stackSize = 2;
		}

		if(Loader.isModLoaded("ThermalExpansion")) {
			ItemStack sandStack = new ItemStack(Block.sand, 1);
			ItemStack doubleYellorium = ingotYellorium.copy();
			doubleYellorium.stackSize = 2;

			if(yelloriteOre != null && ingotYellorium != null) {
				addInductionSmelterRecipe(yelloriteOre, sandStack, doubleYellorium, 1600);
			}
			
			if(yelloriteOre != null && doubledYelloriumDust != null) {
				addPulverizerRecipe(yelloriteOre, doubledYelloriumDust, 4000);
			}
			
			if(ingotYellorium != null && dustYellorium != null) {
				addPulverizerRecipe(ingotYellorium, dustYellorium, 2400);
				addInductionSmelterRecipe(doubledYelloriumDust, sandStack, doubleYellorium, 200);
			}

			if(ingotCyanite != null && dustCyanite != null) {
				addPulverizerRecipe(ingotCyanite, dustCyanite, 2400);
				
				ItemStack doubleDust = dustCyanite.copy();
				doubleDust.stackSize = 2;
				ItemStack doubleIngot = ingotCyanite.copy();
				doubleIngot.stackSize = 2;
				addInductionSmelterRecipe(doubleDust, sandStack, doubleIngot, 200);
			}

			if(ingotGraphite != null && dustGraphite != null) {
				addPulverizerRecipe(ingotGraphite, dustGraphite, 2400);

				ItemStack doubleDust = dustGraphite.copy();
				doubleDust.stackSize = 2;
				ItemStack doubleIngot = ingotGraphite.copy();
				doubleIngot.stackSize = 2;
				addInductionSmelterRecipe(doubleDust, sandStack, doubleIngot, 200);
			}

			if(ingotBlutonium != null && dustBlutonium != null) {
				addPulverizerRecipe(ingotBlutonium, dustBlutonium, 2400);

				ItemStack doubleDust = dustBlutonium.copy();
				doubleDust.stackSize = 2;
				ItemStack doubleIngot = ingotBlutonium.copy();
				doubleIngot.stackSize = 2;
				addInductionSmelterRecipe(doubleDust, sandStack, doubleIngot, 200);
			}
		}
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
				BRRegistry.registerReactorSolidToFuelMapping(new ReactorSolidMapping(candidate, new FluidStack(BigReactors.fluidYellorium, 1000)));
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
		ItemStack doubledYelloriumDust = null;
		if(dustYellorium != null) {
			doubledYelloriumDust = dustYellorium.copy();
			doubledYelloriumDust.stackSize = 2;
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
			if(yelloriteOre != null && doubledYelloriumDust != null) {
				addMekanismEnrichmentChamberRecipe(yelloriteOre.copy(), doubledYelloriumDust.copy());
				addMekanismCombinerRecipe(doubledYelloriumDust.copy(), yelloriteOre.copy());
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
		
		if(Loader.isModLoaded("MineFactoryReloaded")) {
			FactoryRegistry.registerLaserOre(2, yelloriteOre.copy());
			FactoryRegistry.addLaserPreferredOre(9, yelloriteOre.copy()); // Register yellorite with cyan, becuz.
		}
		
		// Easter Egg - Check if today is valentine's day. If so, change all particles to hearts.
		Calendar calendar = Calendar.getInstance();
		BigReactors.isValentinesDay = (calendar.get(Calendar.MONTH) == 2 && calendar.get(Calendar.DAY_OF_MONTH) == 14);
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
	
	// Thermal Expansion IMC APIs, thanks Skyboy!
	protected void addPulverizerRecipe(ItemStack from, ItemStack to, int energy) {
		NBTTagCompound message = new NBTTagCompound();
		message.setInteger("energy", energy);
		message.setCompoundTag("input", from.writeToNBT(new NBTTagCompound()));
		message.setCompoundTag("primaryOutput",to.writeToNBT(new NBTTagCompound()));
		sendInterModMessage("ThermalExpansion", "PulverizerRecipe", message);
	}
	
	protected void addInductionSmelterRecipe(ItemStack firstInput, ItemStack secondInput, ItemStack output, int energy) {
		NBTTagCompound message = new NBTTagCompound();
		message.setInteger("energy", energy);
		message.setCompoundTag("primaryInput", firstInput.writeToNBT(new NBTTagCompound()));
		message.setCompoundTag("secondaryInput", secondInput.writeToNBT(new NBTTagCompound()));
		message.setCompoundTag("primaryOutput", output.writeToNBT(new NBTTagCompound()));
		sendInterModMessage("ThermalExpansion", "SmelterRecipe", message);
	}
	
	protected void sendInterModMessage(String to, String type, NBTTagCompound message) {
		FMLInterModComms.sendMessage(to, type, message);
	}
	
	@SideOnly(Side.CLIENT)
	@ForgeSubscribe
	public void registerIcons(TextureStitchEvent.Pre event) {
	}
	
	@SideOnly(Side.CLIENT)
	@ForgeSubscribe
	public void setIcons(TextureStitchEvent.Post event) {
	}
}

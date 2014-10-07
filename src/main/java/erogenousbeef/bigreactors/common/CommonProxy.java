package erogenousbeef.bigreactors.common;

import java.util.Calendar;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.TextureStitchEvent;
import cofh.api.modhelpers.ThermalExpansionHelper;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import erogenousbeef.bigreactors.api.registry.Reactants;
import erogenousbeef.bigreactors.common.data.StandardReactants;
import erogenousbeef.bigreactors.common.item.ItemIngot;
import erogenousbeef.bigreactors.gui.BigReactorsGUIHandler;
import erogenousbeef.bigreactors.net.CommonPacketHandler;
import erogenousbeef.bigreactors.utils.intermod.IMCHelper;
import erogenousbeef.bigreactors.utils.intermod.ModHelperBase;
import erogenousbeef.bigreactors.utils.intermod.ModHelperComputerCraft;
import erogenousbeef.bigreactors.utils.intermod.ModHelperMekanism;
import erogenousbeef.core.multiblock.MultiblockServerTickHandler;

public class CommonProxy {

	public void preInit() {
	}

	public void init() {
		BigReactors.registerTileEntities();
		
		CommonPacketHandler.init();

		NetworkRegistry.INSTANCE.registerGuiHandler(BRLoader.instance, new BigReactorsGUIHandler());
		BigReactors.tickHandler = new BigReactorsTickHandler();
		FMLCommonHandler.instance().bus().register(BigReactors.tickHandler);
        FMLCommonHandler.instance().bus().register(new MultiblockServerTickHandler());
		
		sendInterModAPIMessages();

		if(Loader.isModLoaded("VersionChecker")) {
			FMLInterModComms.sendRuntimeMessage(BRLoader.MOD_ID, "VersionChecker", "addVersionCheck", "http://big-reactors.com/version.json");
		}
	}

	private void sendInterModAPIMessages() {
		ItemIngot ingotGeneric = BigReactors.ingotGeneric;
		ItemStack yelloriteOre 	= new ItemStack(BigReactors.blockYelloriteOre, 1);

		final int YELLORIUM = 0;
		
		String[] names = ItemIngot.MATERIALS;
		ItemStack[] ingots = new ItemStack[names.length];
		ItemStack[] dusts = new ItemStack[names.length];
		
		for(int i = 0; i < names.length; i++) {
			ingots[i] = ingotGeneric.getIngotItem(names[i]);
			dusts[i] = ingotGeneric.getDustItem(names[i]);
		}
		
		ItemStack doubledYelloriumDust = null;
		if(dusts[YELLORIUM] != null) {
			doubledYelloriumDust = dusts[YELLORIUM].copy();
			doubledYelloriumDust.stackSize = 2;
		}

		if(Loader.isModLoaded("ThermalExpansion")) {
			ItemStack sandStack = new ItemStack(Blocks.sand, 1);
			ItemStack doubleYellorium = ingots[YELLORIUM].copy();
			doubleYellorium.stackSize = 2;

			// TODO: Remove ThermalExpansionHelper once addSmelterRecipe and addPulverizerRecipe aren't broken
			if(ingots[YELLORIUM] != null) {
				ThermalExpansionHelper.addFurnaceRecipe(400, yelloriteOre, ingots[YELLORIUM]);
				ThermalExpansionHelper.addSmelterRecipe(1600, yelloriteOre, sandStack, doubleYellorium);
			}

			if(doubledYelloriumDust != null) {
				ThermalExpansionHelper.addPulverizerRecipe(4000, yelloriteOre, doubledYelloriumDust);
				ThermalExpansionHelper.addSmelterRecipe(200, doubledYelloriumDust, sandStack, doubleYellorium);
			}

			for(int i = 0; i < ingots.length; i++) {
				if(ingots[i] == null || dusts[i] == null) { continue; }

				ThermalExpansionHelper.addPulverizerRecipe(2400, ingots[i], dusts[i]);
				ThermalExpansionHelper.addSmelterRecipe(200, doubledYelloriumDust, sandStack, doubleYellorium);

				ItemStack doubleDust = dusts[i].copy();
				doubleDust.stackSize = 2;
				ItemStack doubleIngot = ingots[i].copy();
				doubleIngot.stackSize = 2;

				ThermalExpansionHelper.addSmelterRecipe(200, doubleDust, sandStack, doubleIngot);
			}
		} // END: IsModLoaded - ThermalExpansion
		
		if(Loader.isModLoaded("MineFactoryReloaded")) {
			// Add yellorite to yellow focus list.
			IMCHelper.MFR.addOreToMiningLaserFocus(yelloriteOre, 2);
            
            // Make Yellorite the 'preferred' ore for lime focus
            IMCHelper.MFR.setMiningLaserFocusPreferredOre(yelloriteOre, 9);
		} // END: IsModLoaded - MineFactoryReloaded
		
		if(Loader.isModLoaded("appliedenergistics2")) {
			if(doubledYelloriumDust != null) {
				IMCHelper.AE2.addGrinderRecipe(yelloriteOre, doubledYelloriumDust, 4);
			}
		
			for(int i = 0; i < ingots.length; i++) {
				if(ingots[i] == null || dusts[i] == null) { continue; }
				IMCHelper.AE2.addGrinderRecipe(ingots[i], dusts[i], 2);
			}
		} // END: IsModLoaded - AE2
	}

	public void postInit() {
		BRConfig.CONFIGURATION.load();
		boolean autoAddUranium = BRConfig.CONFIGURATION.get("Compatibility", "autoAddUranium",
															true,
															"If true, automatically adds all "
															+"unregistered ingots found as clones"
															+"of standard yellorium fuel").getBoolean(true);
		if(autoAddUranium) {
			Reactants.registerSolid("ingotUranium", StandardReactants.yellorium);
		}

		BRConfig.CONFIGURATION.save();
		
		registerWithOtherMods();
		
		// Easter Egg - Check if today is valentine's day. If so, change all particles to hearts.
		Calendar calendar = Calendar.getInstance();
		BigReactors.isValentinesDay = (calendar.get(Calendar.MONTH) == 2 && calendar.get(Calendar.DAY_OF_MONTH) == 14);
	}
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void registerIcons(TextureStitchEvent.Pre event) {
	}
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void setIcons(TextureStitchEvent.Post event) {
	}
	
	/// Mod Interoperability ///
	void registerWithOtherMods() {
		ModHelperBase modHelper;
		
		ModHelperBase.detectMods();
		
		modHelper = new ModHelperComputerCraft();
		modHelper.register();
		
		modHelper = new ModHelperMekanism();
		modHelper.register();
	}
}

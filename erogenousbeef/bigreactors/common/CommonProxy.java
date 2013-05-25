package erogenousbeef.bigreactors.common;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;
import erogenousbeef.bigreactors.gui.BigReactorsGUIHandler;
import erogenousbeef.core.multiblock.MultiblockServerTickHandler;

public class CommonProxy {

	public void preInit() {
	}

	public void init() {
		BigReactors.registerTileEntities();
		
		NetworkRegistry.instance().registerGuiHandler(BRLoader.instance, new BigReactorsGUIHandler());
		TickRegistry.registerTickHandler(new MultiblockServerTickHandler(), Side.SERVER);
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
	}
}

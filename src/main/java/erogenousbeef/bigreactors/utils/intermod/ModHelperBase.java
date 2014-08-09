package erogenousbeef.bigreactors.utils.intermod;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModAPIManager;

public class ModHelperBase {

	public static boolean useCofh;
	public static boolean useBuildcraftTools;
	public static boolean useBuildcraftTransport;
	
	public void register() {}
	
	public static void detectMods() {

		useCofh = Loader.isModLoaded("CoFHCore");
		useBuildcraftTools = ModAPIManager.INSTANCE.hasAPI("BuildCraftAPI|tools");
		useBuildcraftTransport = ModAPIManager.INSTANCE.hasAPI("BuildCraftAPI|transport");
		
	}

}

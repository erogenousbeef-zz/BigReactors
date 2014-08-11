package erogenousbeef.bigreactors.client;

import java.util.HashMap;

import erogenousbeef.bigreactors.common.BigReactors;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.IIcon;

public class CommonBlockIconManager extends BeefIconManager {

	public static final int DEFAULT = 0;
	public static final int OPEN = 1;
	public static final int ITEM_RED = 2;
	public static final int ITEM_GREEN = 3;
	public static final int FLUID_BLUE = 4;
	public static final int FLUID_WHITE = 5;
	
	@Override
	protected String[] getIconNames() {
		return new String[] {
				"default",
				"open",
				"itemRed",
				"itemGreen",
				"fluidBlue",
				"fluidWhite"
		};
	}

	@Override
	protected String getPath() {
		return "common/";
	}
}

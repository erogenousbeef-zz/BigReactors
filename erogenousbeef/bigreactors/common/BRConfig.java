package erogenousbeef.bigreactors.common;

import java.io.File;

import net.minecraftforge.common.Configuration;
import cpw.mods.fml.common.Loader;

public class BRConfig {
	/**
	 * The version of Big Reactors.
	 */
	public static final int MAJOR_VERSION = 0;
	public static final int MINOR_VERSION = 3;
	public static final int REVISION_VERSION = 0;
	public static final String STATUS_VERSION = "rc"; // a/alpha, b/beta, f/final, rc/release candidate, x/experimental
	public static final String BUGFIX_VERSION = "3";
	public static final String VERSION = MAJOR_VERSION + "." + MINOR_VERSION + "." + REVISION_VERSION + STATUS_VERSION + BUGFIX_VERSION;
	public static final int WORLDGEN_VERSION = 1; // Bump this when changing world generation so the world regens

	/**
	 * The Big Reactors configuration file.
	 */
	public static final Configuration CONFIGURATION = new Configuration(new File(Loader.instance().getConfigDir(), "BigReactors/BigReactors.cfg"));

	static
	{
		/**
		 * Loads the configuration and sets all the values.
		 */
		CONFIGURATION.load();
		CONFIGURATION.save();
	}
}

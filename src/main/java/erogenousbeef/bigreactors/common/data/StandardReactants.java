package erogenousbeef.bigreactors.common.data;

import erogenousbeef.bigreactors.api.data.SourceProductMapping;
import erogenousbeef.bigreactors.api.registry.Reactants;

public class StandardReactants {

	public static final String yellorium = "yellorium";
	public static final String cyanite = "cyanite";
	public static final String blutonium = "blutonium";
	
	public static final int colorYellorium = 0;
	public static final int colorCyanite = 0;
	
	// These are used as fallbacks
	public static SourceProductMapping yelloriumMapping;
	public static SourceProductMapping cyaniteMapping;
	
	public static void register() {
		Reactants.registerReactant(yellorium, 0, colorYellorium);
		Reactants.registerReactant(cyanite, 0, colorCyanite);
		Reactants.registerReactant(blutonium, 0, colorYellorium);
	}
	
}

package erogenousbeef.bigreactors.api.registry;

import java.util.HashMap;
import java.util.Map;

import erogenousbeef.bigreactors.api.data.ReactorInteriorData;
import erogenousbeef.bigreactors.common.BRLog;

public class ReactorInterior {
	private static Map<String, ReactorInteriorData> _reactorModeratorBlocks = new HashMap<String, ReactorInteriorData>();
	private static Map<String, ReactorInteriorData> _reactorModeratorFluids = new HashMap<String, ReactorInteriorData>();

	/**
	 * @param absorption	How much radiation this material absorbs and converts to heat. 0.0 = none, 1.0 = all.
	 * @param heatEfficiency How efficiently radiation is converted to heat. 0 = no heat, 1 = all heat.
	 * @param moderation	How well this material moderates radiation. This is a divisor; should not be below 1.
	 */
	public static void registerBlock(String oreDictName, float absorption, float heatEfficiency, float moderation, float heatConductivity) {
		if(_reactorModeratorBlocks.containsKey(oreDictName)) {
			BRLog.warning("Overriding existing radiation moderator block data for oredict name <%s>", oreDictName);
			ReactorInteriorData data = _reactorModeratorBlocks.get(oreDictName);
			data.absorption = absorption;
			data.heatEfficiency = heatEfficiency;
			data.moderation = moderation;
		}
		else {
			_reactorModeratorBlocks.put(oreDictName, new ReactorInteriorData(absorption, heatEfficiency, moderation, heatConductivity));
		}
	}

	/**
	 * @param absorption	How much radiation this material absorbs and converts to heat. 0.0 = none, 1.0 = all.
	 * @param heatEfficiency How efficiently radiation is converted to heat. 0 = no heat, 1 = all heat.
	 * @param moderation	How well this material moderates radiation. This is a divisor; should not be below 1.
	 */
	public static void registerFluid(String fluidName, float absorption, float heatEfficiency, float moderation, float heatConductivity) {
		if(_reactorModeratorFluids.containsKey(fluidName)) {
			BRLog.warning("Overriding existing radiation moderator fluid data for fluid name <%s>", fluidName);
			ReactorInteriorData data = _reactorModeratorFluids.get(fluidName);
			data.absorption = absorption;
			data.heatEfficiency = heatEfficiency;
			data.moderation = moderation;
		}
		else {
			_reactorModeratorFluids.put(fluidName, new ReactorInteriorData(absorption, heatEfficiency, moderation, heatConductivity));
		}
	}
	
	public static ReactorInteriorData getBlockData(String oreDictName) {
		return _reactorModeratorBlocks.get(oreDictName);
	}

	public static ReactorInteriorData getFluidData(String fluidName) {
		return _reactorModeratorFluids.get(fluidName);
	}	
}

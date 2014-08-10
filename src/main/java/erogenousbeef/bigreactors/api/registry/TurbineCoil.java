package erogenousbeef.bigreactors.api.registry;

import java.util.HashMap;
import java.util.Map;

import erogenousbeef.bigreactors.api.data.CoilPartData;
import erogenousbeef.bigreactors.common.BRLog;

public class TurbineCoil {
	private static Map<String, CoilPartData> _blocks = new HashMap<String, CoilPartData>();

	/**
	 * Register a block as permissible in a turbine's inductor coil.
	 * @param oreDictName Name of the block, as registered in the ore dictionary
	 * @param efficiency  Efficiency of the block. 1.0 == iron, 2.0 == gold, etc.
	 * @param bonus		  Energy bonus of the block, if any. Normally 1.0. This is an exponential term and should only be used for EXTREMELY rare blocks!
	 */
	public static void registerBlock(String oreDictName, float efficiency, float bonus, float extractionRate) {
		if(_blocks.containsKey(oreDictName)) {
			CoilPartData data = _blocks.get(oreDictName);
			BRLog.warning("Overriding existing coil part data for oredict name <%s>, original values: eff %.2f / bonus %.2f, new values: eff %.2f / bonus %.2f", oreDictName, data.efficiency, data.bonus, efficiency, bonus); 
			data.efficiency = efficiency;
			data.bonus = bonus;
		}
		else {
			_blocks.put(oreDictName, new CoilPartData(efficiency, bonus, extractionRate));
		}
	}

	public static CoilPartData getBlockData(String oreDictName) {
		return _blocks.get(oreDictName);
	}
}

package erogenousbeef.bigreactors.api.registry;

import java.util.HashMap;
import java.util.Map;

import net.minecraftforge.fluids.Fluid;
import erogenousbeef.bigreactors.api.data.FluidStateData;

public class FluidStates {

	private static Map<String, FluidStateData> m_Data = new HashMap<String, FluidStateData>();
	
	public static void registerData(FluidStateData data) {
		if(data == null) { return; }
		
		if(m_Data.containsKey(data.getLiquid().getName())) {
			throw new IllegalArgumentException(data.getLiquid().getName() + " already has data associated");
		}

		if(m_Data.containsKey(data.getGas().getName())) {
			throw new IllegalArgumentException(data.getLiquid().getName() + " already has data associated");
		}
		
		m_Data.put(data.getLiquid().getName(), data);
		m_Data.put(data.getGas().getName(), data);
	}
	
	public static FluidStateData get(String name) {
		assert(name != null);
		return m_Data.get(name);
	}
	
	public static FluidStateData get(Fluid f) {
		assert(f != null);
		return m_Data.get(f.getName());
	}
}

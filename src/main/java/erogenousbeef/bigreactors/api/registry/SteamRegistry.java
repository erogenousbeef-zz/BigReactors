package erogenousbeef.bigreactors.api.registry;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

public class SteamRegistry {

	private static RangeMap<Integer, Fluid> m_SteamMappings = null;
	private static Map<Fluid, Range<Integer>> m_FluidMappings = new HashMap<Fluid, Range<Integer>>();

	public static boolean isInitialized() { return m_SteamMappings != null; }

	public static void setDefault(Fluid defaultSteam) {
		if(m_SteamMappings != null) {
			throw new IllegalStateException("Cannot call setDefault after initializing the map");
		}
		m_SteamMappings = TreeRangeMap.create();
		
		Range<Integer> rangeAll = Range.all();
		m_SteamMappings.put(rangeAll, defaultSteam);
	}

	public static void register(int minExponent, int maxExponent, Fluid newSteam) {
		if(newSteam == null) { throw new IllegalArgumentException("newSteam cannot be null"); }
		if(m_FluidMappings.containsKey(newSteam)) {
			throw new IllegalArgumentException("Fluid " + newSteam.getName() + "is already mapped");
		}

		Range<Integer> range;
		if(maxExponent < minExponent) {
			int x = maxExponent;
			maxExponent = minExponent;
			minExponent = x;
		}
		
		if(maxExponent == minExponent) {
			range = Range.singleton(minExponent);
		}
		else {
			range = Range.closed(minExponent, maxExponent);
		}
		
		m_SteamMappings.put(range, newSteam);
		m_FluidMappings.put(newSteam, range);
	}
	
	public static Fluid get(int exponent) {
		return m_SteamMappings.get(exponent);
	}
	
	public static Fluid getFluidForRf(float rf) {
		if(rf <= 0) { return m_SteamMappings.get(0); }
		return get((int)Math.log10(rf));
	}
	
	public static float getBaseRfAmountForFluid(Fluid f) {
		if(f == null || !m_FluidMappings.containsKey(f)) { return 1f; }
		Range<Integer> range = m_FluidMappings.get(f);
		if(!range.hasLowerBound()) { return 1f; }
		return range.lowerEndpoint();
	}
	
	public static float getRfInStack(FluidStack stack) {
		if(stack == null) { return 0f; }
		Fluid f = stack.getFluid();
		if(f == null) { return 0f; }
		Range<Integer> range = m_FluidMappings.get(stack.getFluid());
		
		float baseAmount = 1f;
		if(range.hasLowerBound()) {
			baseAmount = (float)Math.pow(10, range.lowerEndpoint());
		}
		
		return baseAmount * stack.amount;
	}
}

package erogenousbeef.bigreactors.common;

import java.util.HashMap;
import java.util.Map;

import erogenousbeef.bigreactors.api.IReactorFuelLiquid;

import net.minecraftforge.liquids.ILiquid;

public abstract class BRRegistry {

	private static Map<Integer, IReactorFuelLiquid> _reactorLiquidFuels = new HashMap<Integer, IReactorFuelLiquid>();
	
	public static void registerReactorFuel(ILiquid liquid, IReactorFuelLiquid fuelData) {
		if(!_reactorLiquidFuels.containsKey(liquid.stillLiquidId())) {
			_reactorLiquidFuels.put(liquid.stillLiquidId(), fuelData);
		}
	}
	
	public static Map<Integer, IReactorFuelLiquid> getReactorFuelLiquids() {
		return _reactorLiquidFuels;
	}
}

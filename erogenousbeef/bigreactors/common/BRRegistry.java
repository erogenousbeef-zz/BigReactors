package erogenousbeef.bigreactors.common;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import erogenousbeef.bigreactors.api.IReactorFuel;

import net.minecraft.item.ItemStack;
import net.minecraftforge.liquids.ILiquid;

public abstract class BRRegistry {
	
	// TODO: Fixme. This does not work, ItemStack does not implement equals().
	private static List<IReactorFuel> _reactorFuels = new LinkedList<IReactorFuel>();
	private static List<IReactorFuel> _reactorWastes = new LinkedList<IReactorFuel>();
	
	public static void registerFuel(IReactorFuel fuelData) {
		if(!_reactorFuels.contains(fuelData)) {
			_reactorFuels.add(fuelData);
		}
	}
	
	public static IReactorFuel getDataForFuel(ItemStack fuelItem) {
		for(IReactorFuel candidate : _reactorFuels) {
			if(candidate.equals(fuelItem)) {
				return candidate;
			}
		}
		
		return null;
	}

	public static void registerWaste(IReactorFuel fuelData) {
		if(!_reactorWastes.contains(fuelData)) {
			_reactorWastes.add(fuelData);
		}
	}
	
	public static IReactorFuel getDataForWaste(ItemStack wasteItem) {
		for(IReactorFuel candidate : _reactorWastes) {
			if(candidate.equals(wasteItem)) {
				return candidate;
			}
		}
		
		return null;
	}

}

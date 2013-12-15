package erogenousbeef.bigreactors.common;

import java.util.LinkedList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import cpw.mods.fml.common.FMLLog;
import erogenousbeef.bigreactors.api.IReactorFuel;
import erogenousbeef.bigreactors.api.IReactorSolid;

public abstract class BRRegistry {
	
	private static List<IReactorFuel> _reactorFuels = new LinkedList<IReactorFuel>();
	private static List<IReactorFuel> _reactorWastes = new LinkedList<IReactorFuel>();
	
	private static List<IReactorSolid> _reactorSolids = new LinkedList<IReactorSolid>();
	
	public static void registerReactorFluid(IReactorFuel fuelData) {
		if(fuelData.isFuel() && fuelData.isWaste()) {
			throw new IllegalArgumentException("Can't register something that's both a fuel and a waste!");
		}

		if(fuelData.isFuel() && !_reactorFuels.contains(fuelData)) {
			_reactorFuels.add(fuelData);
		}

		if(fuelData.isWaste() && !_reactorWastes.contains(fuelData)) {
			_reactorWastes.add(fuelData);
		}
	}
	
	/**
	 * Maps a solid Item onto a Fluid-keyed Fuel or Waste.
	 * @param fuelMapping
	 */
	public static void registerSolidMapping(IReactorSolid fuelMapping) {
		if(!_reactorSolids.contains(fuelMapping)) {
			_reactorSolids.add(fuelMapping);
			
			IReactorFuel matchingFuel = null;
			for(IReactorFuel f : _reactorFuels) {
				if(fuelMapping.isFluidEqual(f.getReferenceFluid())) {
					matchingFuel = f;
					break;
				}
			}
			
			if(matchingFuel == null) {
				for(IReactorFuel w : _reactorWastes) {
					if(fuelMapping.isFluidEqual(w.getReferenceFluid())) {
						matchingFuel = w;
						break;
					}
				}
			}
			
			if(matchingFuel == null) {
				// LOG A WARNING - WE DO NOT HAVE A MATCHING REGISTERED FUEL YET!
				FMLLog.warning("Big Reactors: Registered a solid fuel mapping from solid %s to fluid %s, but there is no registered fuel data for that fluid yet!", fuelMapping.getReferenceItem().getUnlocalizedName(), fuelMapping.getReferenceFluid().getUnlocalizedName());
			}
		}
	}
	
	public static IReactorFuel getDataForSolid(ItemStack fuelItem) {
		if(fuelItem == null) { return null; }
		for(IReactorSolid candidate : _reactorSolids) {
			if(candidate.isItemEqual(fuelItem)) {
				IReactorFuel data = getDataForFuel(candidate.getReferenceFluid());
				if(data == null) {
					data = getDataForWaste(candidate.getReferenceFluid());
				}
				
				return data;
			}
		}
		
		return null;
	}
	
	public static IReactorFuel getDataForFuel(Fluid fluid) {
		if(fluid == null) { return null; }
		for(IReactorFuel candidate : _reactorFuels) {
			if(candidate.isFuelEqual(fluid)) {
				return candidate;
			}
		}
		
		return null;
	}

	public static IReactorFuel getDataForWaste(Fluid fluid) {
		if(fluid == null) { return null; }
		for(IReactorFuel candidate : _reactorWastes) {
			if(candidate.isFuelEqual(fluid)) {
				return candidate;
			}
		}
		
		return null;
	}

	public static IReactorFuel getDataForFluid(Fluid fluid) {
		IReactorFuel data = getDataForFuel(fluid);
		if(data == null) {
			return getDataForWaste(fluid);
		}
		else {
			return data;
		}
	}
	
}

package erogenousbeef.bigreactors.common;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import erogenousbeef.bigreactors.api.IReactorFuel;
import erogenousbeef.bigreactors.api.IReactorSolid;
import erogenousbeef.bigreactors.common.multiblock.helpers.CoilPartData;
import erogenousbeef.bigreactors.common.multiblock.helpers.ReactorInteriorData;

public abstract class BRRegistry {
	
	private static List<IReactorFuel> _reactorFuels = new LinkedList<IReactorFuel>();
	private static List<IReactorFuel> _reactorWastes = new LinkedList<IReactorFuel>();
	
	private static List<IReactorSolid> _reactorSolids = new LinkedList<IReactorSolid>();
	
	private static Map<String, CoilPartData> _turbineCoilParts = new HashMap<String, CoilPartData>();
	private static Map<String, ReactorInteriorData> _reactorModeratorBlocks = new HashMap<String, ReactorInteriorData>();
	private static Map<String, ReactorInteriorData> _reactorModeratorFluids = new HashMap<String, ReactorInteriorData>();

	/**
	 * @param absorption	How much radiation this material absorbs and converts to heat. 0.0 = none, 1.0 = all.
	 * @param heatEfficiency How efficiently radiation is converted to heat. 0 = no heat, 1 = all heat.
	 * @param moderation	How well this material moderates radiation. This is a divisor; should not be below 1.
	 */
	public static void registerReactorInteriorBlock(String oreDictName, float absorption, float heatEfficiency, float moderation, float heatConductivity) {
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
	public static void registerReactorInteriorFluid(String fluidName, float absorption, float heatEfficiency, float moderation, float heatConductivity) {
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
	
	
	public static ReactorInteriorData getReactorInteriorBlockData(String oreDictName) {
		return _reactorModeratorBlocks.get(oreDictName);
	}

	public static ReactorInteriorData getReactorInteriorFluidData(String oreDictName) {
		return _reactorModeratorFluids.get(oreDictName);
	}
	
	/**
	 * Register a block as permissible in a turbine's inductor coil.
	 * @param oreDictName Name of the block, as registered in the ore dictionary
	 * @param efficiency  Efficiency of the block. 1.0 == iron, 2.0 == gold, etc.
	 * @param bonus		  Energy bonus of the block, if any. Normally 1.0. This is an exponential term and should only be used for EXTREMELY rare blocks!
	 */
	public static void registerCoilPart(String oreDictName, float efficiency, float bonus, float extractionRate) {
		if(_turbineCoilParts.containsKey(oreDictName)) {
			CoilPartData data = _turbineCoilParts.get(oreDictName);
			BRLog.warning("Overriding existing coil part data for oredict name <%s>, original values: eff %.2f / bonus %.2f, new values: eff %.2f / bonus %.2f", oreDictName, data.efficiency, data.bonus, efficiency, bonus); 
			data.efficiency = efficiency;
			data.bonus = bonus;
		}
		else {
			_turbineCoilParts.put(oreDictName, new CoilPartData(efficiency, bonus, extractionRate));
		}
	}
	
	public static CoilPartData getCoilPartData(String oreDictName) {
		return _turbineCoilParts.get(oreDictName);
	}
	
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
				BRLog.warning("Big Reactors: Registered a solid fuel mapping from solid %s to fluid %s, but there is no registered fuel data for that fluid yet!", fuelMapping.getReferenceItem().getUnlocalizedName(), fuelMapping.getReferenceFluid().getUnlocalizedName());
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

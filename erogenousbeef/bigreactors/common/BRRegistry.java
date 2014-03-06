package erogenousbeef.bigreactors.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import erogenousbeef.bigreactors.api.IReactorFuel;
import erogenousbeef.bigreactors.api.IReactorSolid;
import erogenousbeef.bigreactors.common.data.ReactorSolidMapping;
import erogenousbeef.bigreactors.common.multiblock.helpers.CoilPartData;
import erogenousbeef.bigreactors.common.multiblock.helpers.ReactorInteriorData;

public abstract class BRRegistry {
	
	private static Map<String, ItemStack> _reactorFluidToSolid = new HashMap<String, ItemStack>();
	private static Set<ReactorSolidMapping> _reactorSolidToFuel = new CopyOnWriteArraySet<ReactorSolidMapping>(); // This won't work
	private static Set<ReactorSolidMapping> _reactorSolidToWaste = new CopyOnWriteArraySet<ReactorSolidMapping>(); // This won't work

	private static Map<String, IReactorFuel> _reactorFluids = new HashMap<String, IReactorFuel>();
	
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
	
	public static void registerReactorFluidToSolidMapping(String fluidName, ItemStack outputPerBucket)
	{
		_reactorFluidToSolid.put(fluidName, outputPerBucket);
	}
	
	public static void registerReactorSolidToFuelMapping(ReactorSolidMapping solidMapping)
	{
		registerToList(solidMapping, _reactorSolidToFuel);
	}
	
	public static void registerReactorSolidToWasteMapping(ReactorSolidMapping solidMapping)
	{
		registerToList(solidMapping, _reactorSolidToWaste);
	}
	
	protected static void registerToList(ReactorSolidMapping mapping, Set<ReactorSolidMapping> set)
	{
		ArrayList<ReactorSolidMapping> itemsToRemove = new ArrayList<ReactorSolidMapping>();
		
		for(ReactorSolidMapping existingMapping : set)
		{
			if(existingMapping.isItemEqual(mapping.getReferenceItem()))
				itemsToRemove.add(existingMapping);
		}
		
		set.removeAll(itemsToRemove);
		set.add(mapping);

	}
	
	public static void registerReactorFluid(String fluidName, IReactorFuel fuelInfo)
	{
		_reactorFluids.put(fluidName, fuelInfo);
	}
	
	public static IReactorFuel getReactorFluidInfo(String fluidName)
	{
		return _reactorFluids.get(fluidName);
	}
	
	public static FluidStack getReactorMappingForFuel(ItemStack sourceItem)
	{
		return getFluidFromSet(sourceItem, _reactorSolidToFuel);
	}
	
	public static FluidStack getReactorMappingForWaste(ItemStack sourceItem)
	{
		return getFluidFromSet(sourceItem, _reactorSolidToWaste);
	}
	
	protected static FluidStack getFluidFromSet(ItemStack sourceItem, Set<ReactorSolidMapping> set)
	{
		for(ReactorSolidMapping existingMapping : set)
		{
			if(existingMapping.isItemEqual(sourceItem))
			{
				return existingMapping.getReferenceFluid();
			}
		}
		
		return null;
	}
	
	public static ItemStack getReactorSolidForFluid(String fluidName)
	{
		return _reactorFluidToSolid.get(fluidName);
	}
}

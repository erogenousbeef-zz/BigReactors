package erogenousbeef.bigreactors.api;

public interface IHeatEntity {
	public static final float ambientHeat = 20.0f; // Normal ambient temperature
	public static final float powerPerHeat = 10f;  // RF power units per C radiated
												   // Note that this is controlled via a BigReactors config setting
												   // For all internal calculations!
	
	/**
	 * Returns the amount of heat in the entity, in celsius.
	 * @return The amount of heat in the entity, in celsius.
	 */
	public float getHeat();
	
	/**
	 * The thermal conductivity of the entity.
	 * This is the percentage of the available heat difference
	 * that will be absorbed in one tick, with 1 being 100%.
	 * Numbers over 1 are ignored.
	 * @return Thermal conductivity constant, the percentage heat difference to absorb in 1 sec
	 */
	public float getThermalConductivity();
	
	/**
	 * Called when a nearby heat radiator would like this
	 * entity to absorb some heat from it.
	 * This should NOT be used to transfer heat back to the source.
	 * @param source The entity attempting to radiate heat into this entity.
	 * @param pulse The object tracking the external results of this heat exchange
	 * @param faces The numbers of faces on which the source is radiating. Reduce your absorption proportionally to 1/faces.
	 * @param contactArea The relative size of the area in contact, in blocks. For example, if your object is a normal block, this is 1. If it's a multiblock, it's the number of blocks in contact. Two 1x3x1 columns would use 3, for example.
	 * @return The amount of heat absorbed by this entity from the source, 0 if none.
	 */
	public float onAbsorbHeat(IHeatEntity source, HeatPulse pulse, int faces, int contactArea);
	
	/**
	 * Called when an entity should try to radiate heat.
	 * Should set HeatPulse.powerProduced to the amount of energy to produce
	 * Should set HeatPulse.heatChange to the difference in ambient reactor heat
	 * @param ambientHeat The ambient heat of the environment around the object.
	 * @return A HeatPulse object containing the results of heat radiation
	 */
	public HeatPulse onRadiateHeat(float ambientHeat);
	
	// Centigrade to transfer per tick per degree centigrade of difference
	public static final float conductivityAir = 0.005f;
	public static final float conductivityRubber = 0.001f;
	public static final float conductivityWater = 0.01f;
	public static final float conductivityStone = 0.015f;
	public static final float conductivityGlass = 0.03f;
	public static final float conductivityIron = 0.06f; // Stainless steel, really.
	public static final float conductivityCopper = 0.1f;
	public static final float conductivityGold = 0.2f;
	public static final float conductivityDiamond = 0.3f;
	public static final float conductivityGraphene = 0.5f;
}

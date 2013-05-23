package erogenousbeef.bigreactors.api;

public interface IHeatEntity {
	public static final double ambientHeat = 20.0; // Normal ambient temperature
	
	/**
	 * Returns the amount of heat in the entity, in celsius.
	 * @return The amount of heat in the entity, in celsius.
	 */
	public double getHeat();
	
	/**
	 * The thermal conductivity of the entity.
	 * This is the percentage of the available heat difference
	 * that will be absorbed in one second, with 1 being 100%.
	 * Numbers over 1 are ignored.
	 * @return Thermal conductivity constant, the percentage heat difference to absorb in 1 sec
	 */
	public double getThermalConductivity();
	
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
	public double onAbsorbHeat(IHeatEntity source, HeatPulse pulse, int faces, int contactArea);
	
	/**
	 * Called when an entity should try to radiate heat.
	 * Should set HeatPulse.powerProduced to the amount of energy to produce
	 * Should set HeatPulse.heatChange to the difference in ambient reactor heat
	 * @param ambientHeat The ambient heat of the environment around the object.
	 * @return A HeatPulse object containing the results of heat radiation
	 */
	public HeatPulse onRadiateHeat(double ambientHeat);
	
	
	public static final double conductivityAir = 0.0005;
	public static final double conductivityRubber = 0.001;
	public static final double conductivityWater = 0.01;
	public static final double conductivityStone = 0.02;
	public static final double conductivityGlass = 0.02;
	public static final double conductivityIron = 0.05; // Stainless steel, really.
	public static final double conductivityCopper = 0.1;
	public static final double conductivityDiamond = 0.15;
	public static final double conductivityGraphene = 0.25;
}

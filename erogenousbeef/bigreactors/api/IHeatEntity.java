package erogenousbeef.bigreactors.api;

public interface IHeatEntity {
	public static final float ambientHeat = 20.0f; // Normal ambient temperature
	
	/**
	 * Returns the amount of heat in the entity, in celsius.
	 * @return The amount of heat in the entity, in celsius.
	 */
	public float getHeat();
	
	/**
	 * The thermal conductivity of the entity.
	 * This is the amount of heat (in C) that this entity transfers
	 * over a unit area (1x1 square) in one tick, per degree-C difference.
	 * (Yes, I know centigrade != joules, it's an abstraction)
	 * @return Thermal conductivity constant, see above.
	 */
	public float getThermalConductivity();
	
	// Centigrade to transfer per tick per degree centigrade of difference on a single exposed face (1x1)
	public static final float conductivityAir = 0.005f;
	public static final float conductivityRubber = 0.001f;
	public static final float conductivityWater = 0.01f;
	public static final float conductivityStone = 0.015f;
	public static final float conductivityGlass = 0.03f;
	public static final float conductivityIron = 0.06f; // Stainless steel, really.
	public static final float conductivityCopper = 0.1f;
	public static final float conductivityGold = 0.2f;
	public static final float conductivityEmerald = 0.25f;
	public static final float conductivityDiamond = 0.3f;
	public static final float conductivityGraphene = 0.5f;
}

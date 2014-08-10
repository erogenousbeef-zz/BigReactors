package erogenousbeef.bigreactors.api.data;

public class ReactorInteriorData {
	public float absorption, heatEfficiency, moderation;
	public float heatConductivity;

	/**
	 * @param absorption	How much radiation this material absorbs and converts to heat. 0.0 = none, 1.0 = all.
	 * @param heatEfficiency How efficiently radiation is converted to heat. 0 = no heat, 1 = all heat.
	 * @param moderation	How well this material moderates radiation. This is a divisor; should not be below 1.
	 * @param heatConductivity How well this material conducts heat, in RF/t/m2.
	 */
	public ReactorInteriorData(float absorption, float heatEfficiency, float moderation, float heatConductivity) {
		this.absorption = Math.max(0f, Math.min(1f, absorption));
		this.heatEfficiency = Math.max(0f, Math.min(1f, heatEfficiency));
		this.moderation = Math.max(1f, moderation);
		this.heatConductivity = Math.max(0f, heatConductivity);
	}
}

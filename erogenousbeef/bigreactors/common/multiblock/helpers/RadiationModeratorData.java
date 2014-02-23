package erogenousbeef.bigreactors.common.multiblock.helpers;

public class RadiationModeratorData {
	public float absorption, heatEfficiency, moderation;

	/**
	 * @param absorption	How much radiation this material absorbs and converts to heat. 0.0 = none, 1.0 = all.
	 * @param heatEfficiency How efficiently radiation is converted to heat. 0 = no heat, 1 = all heat.
	 * @param moderation	How well this material moderates radiation. This is a divisor; should not be below 1.
	 */
	public RadiationModeratorData(float absorption, float heatEfficiency, float moderation) {
		this.absorption = Math.max(0f, Math.min(1f, absorption));
		this.heatEfficiency = Math.max(0f, Math.min(1f, heatEfficiency));
		this.moderation = Math.max(1f, moderation);
	}
}

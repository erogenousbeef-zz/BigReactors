package erogenousbeef.bigreactors.common.multiblock.helpers;

/**
 * Helper for reactor radiation game logic
 * @author Erogenous Beef
 */
public class RadiationHelper {

	// Game Balance Values
	// TODO: Make these configurable
	private static final float maximumNeutronsPerFuel = 50000f; // Should be a few minutes per ingot, on average.
	private static final float neutronsPerFuel = 0.001f; // neutrons per fuel unit
	private static final float heatPerNeutron = 0.1f; // C per fission event
	private static final float powerPerNeutron = 10f; // RF units per fission event
	private static final float wasteNeutronPenalty = 0.01f;
	private static final float incidentNeutronFuelRate = 0.25f;
	private static final float incidentRadiationDecayRate = 0.5f;

	public RadiationHelper() {
	}

}

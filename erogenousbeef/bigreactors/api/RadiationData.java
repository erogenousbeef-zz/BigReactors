package erogenousbeef.bigreactors.api;

public class RadiationData {

	public float fuelUsage = 0f;
	public float environmentHeatChange = 0f;
	public float fuelHeatChange = 0f;
	public float fuelAbsorbedRadiation = 0f; // in rad-units

	public RadiationData() {
		fuelUsage = 0f;
		environmentHeatChange = 0f;
		fuelHeatChange = 0f;
		fuelAbsorbedRadiation = 0f;
	}
}

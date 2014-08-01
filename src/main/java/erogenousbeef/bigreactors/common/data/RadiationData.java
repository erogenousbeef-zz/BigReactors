package erogenousbeef.bigreactors.common.data;

import erogenousbeef.bigreactors.utils.StaticUtils;

public class RadiationData {

	public float fuelUsage = 0f;
	public float environmentRfChange = 0f; // Amount of RF absorbed by the environment
	public float fuelRfChange = 0f;		   // Amount of RF absorbed by the fuel
	public float fuelAbsorbedRadiation = 0f; // in rad-units

	public RadiationData() {
		fuelUsage = 0f;
		environmentRfChange = 0f;
		fuelRfChange = 0f;
		fuelAbsorbedRadiation = 0f;
	}
	
	public float getEnvironmentHeatChange(int environmentVolume) {
		return StaticUtils.Energy.getTempFromVolumeAndRF(environmentVolume, environmentRfChange);
	}

	public float getFuelHeatChange(int fuelVolume) {
		return StaticUtils.Energy.getTempFromVolumeAndRF(fuelVolume, fuelRfChange);
		
	}
}

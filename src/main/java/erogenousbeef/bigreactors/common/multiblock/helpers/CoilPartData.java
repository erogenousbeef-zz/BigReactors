package erogenousbeef.bigreactors.common.multiblock.helpers;

public class CoilPartData {
	
	public float efficiency;
	public float bonus;
	public float energyExtractionRate; // 1.0 = normal

	public CoilPartData(float efficiency, float bonus, float extractionRate) {
		this.efficiency = efficiency;
		this.bonus = bonus;
		this.energyExtractionRate = extractionRate;
	}
}

package erogenousbeef.bigreactors.common.multiblock.helpers;

public class FloatUpdateTracker {

	int ticksSinceLastUpdate;
	int ticksBetweenUpdates;
	int ticksBetweenUrgentUpdates;
	float value;
	float minimumDifference;
	float maximumDifference;

	public FloatUpdateTracker(int minimumTicksBetweenUpdates, int minimumTicksBetweenUrgentUpdates, float minimumSpreadForUpdate, float maximumSpreadForUpdate) {
		ticksSinceLastUpdate = 0;
		value = 0f;
		ticksBetweenUpdates = minimumTicksBetweenUpdates;
		ticksBetweenUrgentUpdates = Math.min(ticksBetweenUpdates, minimumTicksBetweenUrgentUpdates);
		minimumDifference = minimumSpreadForUpdate;
		maximumDifference = Math.max(minimumDifference, maximumSpreadForUpdate);
	}
	
	public void setValue(float v) { 
		value = v;
		ticksSinceLastUpdate = 0;
	}
	
	public void onExternalUpdate() { ticksSinceLastUpdate = 0; }
	
	public boolean shouldUpdate(float currentValue) {
		ticksSinceLastUpdate++;
		
		if(ticksSinceLastUpdate < ticksBetweenUrgentUpdates) {
			return false;
		}

		float spread = Math.abs(currentValue - value);
		if(spread >= maximumDifference) {
			ticksSinceLastUpdate = 0;
			value = currentValue;
			return true;
		}
		
		if(ticksSinceLastUpdate < ticksBetweenUpdates) {
			return false;
		}
		
		if(spread >= minimumDifference) {
			ticksSinceLastUpdate = 0;
			value = currentValue;
			return true;
		}
		else {
			ticksSinceLastUpdate = ticksBetweenUpdates;
			return false;
		}
	}
}

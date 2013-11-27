package erogenousbeef.bigreactors.api;

public interface IRadiationPulse {
	public float getFastRadiation();
	public float getSlowRadiation();
	public float getPowerProduced();
	
	public int getTimeToLive();
	
	public void setFastRadiation(float newValue);
	public void setSlowRadiation(float newValue);
	public void addPower(float d);

	public void setTimeToLive(int newTTL);
	public void changeTTL(int difference);
}

package erogenousbeef.bigreactors.api;

public interface IRadiationPulse {
	public int getFastRadiation();
	public int getSlowRadiation();
	public int getPowerProduced();
	public double getHeatProduced();
	
	public int getTimeToLive();
	
	public void setFastRadiation(int newValue);
	public void setSlowRadiation(int newValue);
	public void addPower(int additionalPower);
	public void changeHeat(double difference);

	public void setTimeToLive(int newTTL);
	public void changeTTL(int difference);
}

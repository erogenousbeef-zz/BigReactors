package erogenousbeef.bigreactors.api;

public interface IRadiationPulse {
	public double getFastRadiation();
	public double getSlowRadiation();
	public double getPowerProduced();
	public double getHeatProduced();
	
	public int getTimeToLive();
	
	public void setFastRadiation(double newValue);
	public void setSlowRadiation(double newValue);
	public void addPower(double d);
	public void changeHeat(double difference);

	public void setTimeToLive(int newTTL);
	public void changeTTL(int difference);
}

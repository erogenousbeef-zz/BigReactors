package erogenousbeef.bigreactors.common;

import erogenousbeef.bigreactors.api.IRadiationPulse;

public class RadiationPulse implements IRadiationPulse {

	int fast;
	int slow;
	int ttl;
	double power;
	double heat;
	
	public RadiationPulse() { 
		fast = slow = 0;
		ttl = 1;
		heat = 0.0;
		power = 0.0;
	}
	
	public RadiationPulse(int fast, int slow, int timeToLive, double heat) {
		this.fast = fast;
		this.slow = slow;
		this.ttl = timeToLive;
		this.power = 0.0;
		this.heat = heat;
	}
	
	@Override
	public int getFastRadiation() {
		return fast;
	}

	@Override
	public int getSlowRadiation() {
		return slow;
	}

	@Override
	public int getTimeToLive() {
		return ttl;
	}

	@Override
	public void setFastRadiation(int newValue) {
		fast = newValue;
	}

	@Override
	public void setSlowRadiation(int newValue) {
		slow = newValue;
	}

	@Override
	public void setTimeToLive(int newTTL) {
		this.ttl = newTTL;
	}
	
	@Override
	public void changeTTL(int difference) {
		this.ttl += difference;
	}

	@Override
	public int getPowerProduced() {
		return (int)power;
	}

	@Override
	public void addPower(double additionalPower) {
		power += additionalPower;
	}

	@Override
	public double getHeatProduced() {
		return heat;
	}

	@Override
	public void changeHeat(double difference) {
		heat += difference;
	}
}

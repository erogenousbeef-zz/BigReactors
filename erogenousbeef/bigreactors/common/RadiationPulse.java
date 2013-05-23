package erogenousbeef.bigreactors.common;

import erogenousbeef.bigreactors.api.IRadiationPulse;

public class RadiationPulse implements IRadiationPulse {

	double fast;
	double slow;
	int ttl;
	double power;
	double heat;
	
	public RadiationPulse() { 
		fast = slow = 0;
		ttl = 1;
		heat = 0.0;
		power = 0.0;
	}
	
	public RadiationPulse(double fast, double slow, int timeToLive, double heat, double power) {
		this.fast = fast;
		this.slow = slow;
		this.ttl = timeToLive;
		this.power = power;
		this.heat = heat;
	}
	
	@Override
	public double getFastRadiation() {
		return fast;
	}

	@Override
	public double getSlowRadiation() {
		return slow;
	}

	@Override
	public int getTimeToLive() {
		return ttl;
	}

	@Override
	public void setFastRadiation(double newValue) {
		fast = newValue;
	}

	@Override
	public void setSlowRadiation(double newValue) {
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
	public double getPowerProduced() {
		return power;
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

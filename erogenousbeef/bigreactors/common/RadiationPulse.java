package erogenousbeef.bigreactors.common;

import erogenousbeef.bigreactors.api.IRadiationPulse;

public class RadiationPulse implements IRadiationPulse {

	int fast;
	int slow;
	int ttl;
	
	public RadiationPulse() { 
		fast = slow = 0;
		ttl = 1;
	}
	
	public RadiationPulse(int fast, int slow, int timeToLive) {
		this.fast = fast;
		this.slow = slow;
		this.ttl = timeToLive;
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
}

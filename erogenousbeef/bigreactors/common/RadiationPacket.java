package erogenousbeef.bigreactors.common;

import erogenousbeef.bigreactors.api.IRadiationPacket;

public class RadiationPacket implements IRadiationPacket {

	int fast;
	int slow;
	
	public RadiationPacket() { 
		fast = slow = 0;
	}
	
	public RadiationPacket(int fast, int slow) {
		this.fast = fast;
		this.slow = slow;
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
	public void setFastRadiation(int newValue) {
		fast = newValue;
	}

	@Override
	public void setSlowRadiation(int newValue) {
		slow = newValue;
	}

	public void scaleDown(int i) {
		fast = fast / i;
		slow = slow / i;
	}
}

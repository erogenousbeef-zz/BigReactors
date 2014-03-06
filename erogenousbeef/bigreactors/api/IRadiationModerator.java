package erogenousbeef.bigreactors.api;

import erogenousbeef.bigreactors.common.data.RadiationData;
import erogenousbeef.bigreactors.common.data.RadiationPacket;

public interface IRadiationModerator {
	public void moderateRadiation(RadiationData returnData, RadiationPacket radiation);
}

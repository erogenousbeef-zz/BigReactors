package erogenousbeef.bigreactors.net.helpers;

import io.netty.buffer.ByteBuf;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorRedNetPort.CircuitType;
import erogenousbeef.core.common.CoordTriplet;

public class RedNetChange {
	int channelID;
	CircuitType circuitType;
	boolean pulseOrToggle;
	CoordTriplet coord;
	
	public RedNetChange(int channelID, CircuitType circuitType, boolean pulseOrToggle, CoordTriplet coord) {
		this.channelID = channelID;
		this.circuitType = circuitType;
		this.pulseOrToggle = pulseOrToggle;
		this.coord = coord;
	}
	
	public static RedNetChange fromBytes(ByteBuf buf) {
		int channelID = buf.readInt();
		CircuitType type = CircuitType.s_Types[buf.readInt()];
		boolean pulseOrToggle = false;

		if(CircuitType.canBeToggledBetweenPulseAndNormal(type)) {
			pulseOrToggle = buf.readBoolean();
		}
		
		CoordTriplet coord = null;
		if(CircuitType.hasCoordinate(type)) {
			boolean coordNull = buf.readBoolean();
			if(!coordNull) {
				coord = new CoordTriplet(buf.readInt(), buf.readInt(), buf.readInt());
			}
		}
		
		return new RedNetChange(channelID, type, pulseOrToggle, coord);
	}
	
	public void toBytes(ByteBuf buf) {
		buf.writeInt(channelID);
		buf.writeInt(circuitType.ordinal());
		
		if(CircuitType.canBeToggledBetweenPulseAndNormal(circuitType)) {
			buf.writeBoolean(pulseOrToggle);
		}
		
		if(CircuitType.hasCoordinate(circuitType)) {
			buf.writeBoolean(coord == null);
			if(coord != null) {
				buf.writeInt(coord.x);
				buf.writeInt(coord.y);
				buf.writeInt(coord.z);
			}
		}
	}

	public int getChannel() { return channelID; }
	public CircuitType getType() { return this.circuitType; }
	public CoordTriplet getCoord() { return coord; }
	public boolean getPulseOrToggle() { return this.pulseOrToggle; }
}

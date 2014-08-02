package erogenousbeef.bigreactors.net.message;

import io.netty.buffer.ByteBuf;

import java.io.DataInputStream;

import cpw.mods.fml.common.network.simpleimpl.IMessage;

import erogenousbeef.bigreactors.utils.NetworkUtils;

public class MultiblockMessage implements IMessage {
	public enum Type {
		ReactorStatus(0),
		TurbineStatus(1),
		
		ButtonEject(2),
		ButtonActivate(3),
		
		UpdateAccessPort(4),
		UpdateWasteEjectionSetting(5),
		UpdateTurbineGovernor(6),
		UpdateTurbineVent(7),
		UpdateTurbineInductor(8),
		
		;
		
		private Type(int value) { this.value = value; }
		private final int value;
		public int getValue() { return value; }
	}

	private static final Type[] s_Types = Type.values();
	
	protected Type type;
    protected int x, y, z;
    protected Object[] data;
    protected DataInputStream dis;

    protected MultiblockMessage(Type type, int x, int y, int z, Object... data) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.z = z;
        this.data = data;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        type = s_Types[buf.readInt()];
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        dis = NetworkUtils.toDataInputStream(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(type.getValue());
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        for(Object obj : data) {
            NetworkUtils.writeObjectToByteBuf(buf, obj);
        }
    }
}

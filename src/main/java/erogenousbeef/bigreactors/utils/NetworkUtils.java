package erogenousbeef.bigreactors.utils;

import io.netty.buffer.ByteBuf;
import cpw.mods.fml.common.network.ByteBufUtils;

public class NetworkUtils {

    public static void writeObjectToByteBuf(ByteBuf buf, Object obj) {
        Class objClass = obj.getClass();

        if (objClass.equals(Boolean.class))
        {
            buf.writeBoolean((Boolean) obj);
        }
        else if (objClass.equals(Byte.class))
        {
            buf.writeByte((Byte) obj);
        }
        else if (objClass.equals(Integer.class))
        {
            buf.writeInt((Integer) obj);
        }
        else if (objClass.equals(String.class))
        {
            ByteBufUtils.writeUTF8String(buf, (String) obj);
        }
        else if (objClass.equals(Double.class))
        {
            buf.writeDouble((Double) obj);
        }
        else if (objClass.equals(Float.class))
        {
            buf.writeFloat((Float) obj);
        }
        else if (objClass.equals(Long.class))
        {
            buf.writeLong((Long) obj);
        }
        else if (objClass.equals(Short.class))
        {
            buf.writeShort((Short) obj);
        }
        else {
            throw new IllegalArgumentException("Unrecognized class for network serialization");
        }
    }
}

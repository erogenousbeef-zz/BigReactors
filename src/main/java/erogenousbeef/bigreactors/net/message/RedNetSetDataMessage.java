package erogenousbeef.bigreactors.net.message;

import java.io.DataInputStream;
import java.io.IOException;

import net.minecraft.tileentity.TileEntity;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorRedNetPort;
import erogenousbeef.bigreactors.utils.NetworkUtils;
import io.netty.buffer.ByteBuf;

public class RedNetSetDataMessage implements IMessage, IMessageHandler<RedNetSetDataMessage, IMessage> {
    private int x, y, z;
    private Object[] data;
    private DataInputStream dis;

    public RedNetSetDataMessage(int x, int y, int z, Object... data) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.data = data;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        dis = NetworkUtils.toDataInputStream(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        for(Object obj : data) {
            NetworkUtils.writeObjectToByteBuf(buf, obj);
        }
    }

    @Override
    public IMessage onMessage(RedNetSetDataMessage message, MessageContext ctx) {
        TileEntity te = ctx.getServerHandler().playerEntity.worldObj.getTileEntity(x, y, z);
        if(te != null && te instanceof TileEntityReactorRedNetPort) {
            try {
                ((TileEntityReactorRedNetPort)te).decodeSettings(dis, true);
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}

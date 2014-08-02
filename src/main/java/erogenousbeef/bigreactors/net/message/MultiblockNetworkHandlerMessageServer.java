package erogenousbeef.bigreactors.net.message;

import java.io.DataInputStream;
import java.io.IOException;

import net.minecraft.tileentity.TileEntity;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import erogenousbeef.bigreactors.common.BRLog;
import erogenousbeef.bigreactors.common.multiblock.interfaces.IMultiblockNetworkHandler;
import erogenousbeef.bigreactors.utils.NetworkUtils;
import io.netty.buffer.ByteBuf;

public class MultiblockNetworkHandlerMessageServer implements IMessage, IMessageHandler<MultiblockNetworkHandlerMessageServer, IMessage> {
    private int id, x, y, z;
    private Object[] data;
    private DataInputStream dis;

    public MultiblockNetworkHandlerMessageServer(int id, int x, int y, int z, Object... data) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.z = z;
        this.data = data;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        id = buf.readInt();
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        dis = NetworkUtils.toDataInputStream(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(id);
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        for(Object obj : data) {
            NetworkUtils.writeObjectToByteBuf(buf, obj);
        }
    }

    @Override
    public IMessage onMessage(MultiblockNetworkHandlerMessageServer message, MessageContext ctx) {
        TileEntity te = ctx.getServerHandler().playerEntity.worldObj.getTileEntity(x, y, z);
        if(te != null && te instanceof IMultiblockNetworkHandler) {
            try {
                ((IMultiblockNetworkHandler)te).onNetworkPacket(id, dis);
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}

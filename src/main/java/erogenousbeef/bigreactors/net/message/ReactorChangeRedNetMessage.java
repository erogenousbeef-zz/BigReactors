package erogenousbeef.bigreactors.net.message;

import io.netty.buffer.ByteBuf;

import java.io.IOException;

import net.minecraft.tileentity.TileEntity;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import erogenousbeef.bigreactors.common.BRLog;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorRedNetPort;
import erogenousbeef.bigreactors.net.message.base.WorldMessageServer;
import erogenousbeef.bigreactors.utils.NetworkUtils;

public class ReactorChangeRedNetMessage extends WorldMessageServer {
    private Object[] data;
    private ByteBuf bytes;

    public ReactorChangeRedNetMessage() { super(); data = null; bytes = null; }
    
    public ReactorChangeRedNetMessage(int x, int y, int z, Object... data) {
    	super(x, y, z);
        this.data = data;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
    	super.fromBytes(buf);
        this.bytes = buf.readBytes(buf.readableBytes());
    }

    @Override
    public void toBytes(ByteBuf buf) {
    	super.toBytes(buf);

        for(Object obj : data) {
            NetworkUtils.writeObjectToByteBuf(buf, obj);
        }
    }

    public static class Handler extends WorldMessageServer.Handler<ReactorChangeRedNetMessage> {
        @Override
        public IMessage handleMessage(ReactorChangeRedNetMessage message, MessageContext ctx, TileEntity te) {
            if(te instanceof TileEntityReactorRedNetPort) {
                try {
                    ((TileEntityReactorRedNetPort)te).decodeSettings(message.bytes, true);
                } catch(IOException e) {
                	BRLog.warning("Error while changing rednet data on block @ %d, %d, %d: %s", te.xCoord, te.yCoord, te.zCoord, e.getMessage());
                    e.printStackTrace();
                }
            }
            return null;
        }
    }
}

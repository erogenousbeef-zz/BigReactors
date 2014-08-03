package erogenousbeef.bigreactors.net.message;

import io.netty.buffer.ByteBuf;

import java.io.IOException;

import net.minecraft.tileentity.TileEntity;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import erogenousbeef.bigreactors.common.BRLog;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorRedNetPort;
import erogenousbeef.bigreactors.net.message.base.TileMessageServer;
import erogenousbeef.bigreactors.net.message.base.WorldMessageServer;
import erogenousbeef.bigreactors.utils.NetworkUtils;

public class ReactorRedNetPortChangeMessage extends TileMessageServer<TileEntityReactorRedNetPort> {
    private Object[] data;
    private ByteBuf bytes;

    public ReactorRedNetPortChangeMessage() { super(); data = null; bytes = null; }
    
    public ReactorRedNetPortChangeMessage(TileEntityReactorRedNetPort port, Object... data) {
    	super(port);
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

    public static class Handler extends TileMessageServer.Handler<ReactorRedNetPortChangeMessage,
    															  TileEntityReactorRedNetPort> {
        @Override
        public IMessage handle(ReactorRedNetPortChangeMessage message, MessageContext ctx, TileEntityReactorRedNetPort port) {
            try {
                port.decodeSettings(message.bytes, true);
            } catch(IOException e) {
            	BRLog.warning("Error while changing rednet data on block @ %d, %d, %d: %s", message.x, message.y, message.z, e.getMessage());
                e.printStackTrace();
            }
            return null;
        }
        
        @Override
        public TileEntityReactorRedNetPort getImpl(TileEntity te) {
        	return te instanceof TileEntityReactorRedNetPort ?
        			(TileEntityReactorRedNetPort)te : null;
        }
    }
}

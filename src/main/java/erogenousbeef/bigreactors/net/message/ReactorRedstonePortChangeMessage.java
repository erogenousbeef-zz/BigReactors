package erogenousbeef.bigreactors.net.message;

import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorRedstonePort;
import erogenousbeef.bigreactors.net.message.base.TileMessageServer;
import erogenousbeef.bigreactors.net.message.base.WorldMessageServer;

public class ReactorRedstonePortChangeMessage extends TileMessageServer<TileEntityReactorRedstonePort> {
    private int newCircut, newLevel;
    private boolean newGt, pulse;

    public ReactorRedstonePortChangeMessage() { super(); }
    
    public ReactorRedstonePortChangeMessage(TileEntityReactorRedstonePort port, int newCircut, int newLevel, boolean newGt, boolean pulse) {
    	super(port);
        this.newCircut = newCircut;
        this.newLevel = newLevel;
        this.newGt = newGt;
        this.pulse = pulse;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
    	super.fromBytes(buf);
        newCircut = buf.readInt();
        newLevel = buf.readInt();
        newGt = buf.readBoolean();
        pulse = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
    	super.toBytes(buf);
        buf.writeInt(newCircut);
        buf.writeInt(newLevel);
        buf.writeBoolean(newGt);
        buf.writeBoolean(pulse);
    }

    public static class Handler extends TileMessageServer.Handler<ReactorRedstonePortChangeMessage,
    															  TileEntityReactorRedstonePort> {
        @Override
        public IMessage handle(ReactorRedstonePortChangeMessage message, MessageContext ctx, TileEntityReactorRedstonePort te) {
        	te.onReceiveUpdatePacket(message.newCircut, message.newLevel, message.newGt, message.pulse);
            return null;
        }
        
        @Override
        public TileEntityReactorRedstonePort getImpl(TileEntity te) {
        	return te instanceof TileEntityReactorRedstonePort ?
        			(TileEntityReactorRedstonePort)te : null;
        }
    }
}

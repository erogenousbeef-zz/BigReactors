package erogenousbeef.bigreactors.net.message;

import io.netty.buffer.ByteBuf;

import java.io.IOException;

import net.minecraft.tileentity.TileEntity;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import erogenousbeef.bigreactors.common.BRLog;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorRedNetPort;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorRedNetPort.CircuitType;
import erogenousbeef.bigreactors.net.helpers.RedNetChange;
import erogenousbeef.bigreactors.net.message.base.TileMessageServer;
import erogenousbeef.bigreactors.net.message.base.WorldMessageServer;
import erogenousbeef.core.common.CoordTriplet;

public class ReactorRedNetPortChangeMessage extends TileMessageServer<TileEntityReactorRedNetPort> {
    private RedNetChange[] changes;
    
    public ReactorRedNetPortChangeMessage() { super(); changes = null; }
    
    public ReactorRedNetPortChangeMessage(TileEntityReactorRedNetPort port, RedNetChange[] changes) {
    	super(port);
    	this.changes = changes;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
    	super.fromBytes(buf);
    	
    	int numChanges = buf.readInt();
    	if(numChanges < 1) { return; }
    	
    	changes = new RedNetChange[numChanges];
    	for(int i = 0; i < numChanges; i++) {
    		changes[i] = RedNetChange.fromBytes(buf);
    	}
    }

    @Override
    public void toBytes(ByteBuf buf) {
    	super.toBytes(buf);

    	if(changes == null || changes.length < 1) {
    		buf.writeInt(0);
    		return;
    	}
    	
    	buf.writeInt(changes.length);
    	for(int i = 0; i < changes.length; i++) {
    		changes[i].toBytes(buf);
    	}
    }

    public static class Handler extends TileMessageServer.Handler<ReactorRedNetPortChangeMessage,
    															  TileEntityReactorRedNetPort> {
        @Override
        public IMessage handle(ReactorRedNetPortChangeMessage message, MessageContext ctx, TileEntityReactorRedNetPort port) {
            port.onCircuitUpdate(message.changes);
            return null;
        }
        
        @Override
        public TileEntityReactorRedNetPort getImpl(TileEntity te) {
        	return te instanceof TileEntityReactorRedNetPort ?
        			(TileEntityReactorRedNetPort)te : null;
        }
    }
}

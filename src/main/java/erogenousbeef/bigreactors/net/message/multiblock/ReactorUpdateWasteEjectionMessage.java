package erogenousbeef.bigreactors.net.message.multiblock;

import io.netty.buffer.ByteBuf;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import erogenousbeef.bigreactors.common.multiblock.MultiblockReactor;
import erogenousbeef.bigreactors.net.message.base.ReactorMessageClient;

public class ReactorUpdateWasteEjectionMessage extends ReactorMessageClient {
    private int newSetting;
    
    public ReactorUpdateWasteEjectionMessage() { super(); newSetting = 0; }

    public ReactorUpdateWasteEjectionMessage(MultiblockReactor reactor) {
    	super(reactor);
    	newSetting = reactor.getWasteEjection().ordinal();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
    	super.fromBytes(buf);
        newSetting = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
    	super.toBytes(buf);
        buf.writeInt(newSetting);
    }

    public static class Handler extends ReactorMessageClient.Handler<ReactorUpdateWasteEjectionMessage> {
        @Override
        public IMessage handleMessage(ReactorUpdateWasteEjectionMessage message, MessageContext ctx, MultiblockReactor reactor) {
        	reactor.setWasteEjection(MultiblockReactor.s_EjectionSettings[message.newSetting]);
            return null;
        }    	
    }
}

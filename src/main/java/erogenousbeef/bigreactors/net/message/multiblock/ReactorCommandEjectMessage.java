package erogenousbeef.bigreactors.net.message.multiblock;

import io.netty.buffer.ByteBuf;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import erogenousbeef.bigreactors.common.multiblock.MultiblockReactor;
import erogenousbeef.bigreactors.net.message.base.ReactorMessageServer;

public class ReactorCommandEjectMessage extends ReactorMessageServer {
	protected boolean ejectFuel;
	protected boolean dumpExcess;
	
	public ReactorCommandEjectMessage() { 
		super();
		ejectFuel = dumpExcess = false;
	}
	
	public ReactorCommandEjectMessage(MultiblockReactor reactor, boolean ejectFuel, boolean dumpExcess) {
		super(reactor);
		this.ejectFuel = ejectFuel;
		this.dumpExcess = dumpExcess;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		super.fromBytes(buf);
		ejectFuel = buf.readBoolean();
		dumpExcess = buf.readBoolean();
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		super.toBytes(buf);
		buf.writeBoolean(ejectFuel);
		buf.writeBoolean(dumpExcess);
	}
	
	public static class Handler extends ReactorMessageServer.Handler<ReactorCommandEjectMessage> {
		@Override
		public IMessage handleMessage(ReactorCommandEjectMessage message, MessageContext ctx, MultiblockReactor reactor) {
			if(message.ejectFuel) {
				reactor.ejectFuel(message.dumpExcess, null);
			}
			else {
				reactor.ejectWaste(message.dumpExcess, null);
			}
			return null;
		}
	}
}

package erogenousbeef.bigreactors.net.message.multiblock;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import erogenousbeef.bigreactors.common.multiblock.MultiblockReactor;
import erogenousbeef.bigreactors.common.multiblock.MultiblockReactor.WasteEjectionSetting;
import erogenousbeef.bigreactors.net.message.base.ReactorMessageServer;

public class ReactorChangeWasteEjectionMessage extends ReactorMessageServer {
	int newSetting;
	public ReactorChangeWasteEjectionMessage() { super(); newSetting = 0; }
	public ReactorChangeWasteEjectionMessage(MultiblockReactor reactor, WasteEjectionSetting setting) {
		super(reactor);
		newSetting = setting.ordinal();
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		super.toBytes(buf);
		buf.writeInt(newSetting);
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		super.fromBytes(buf);
		newSetting = buf.readInt();
	}
	
	public static class Handler extends ReactorMessageServer.Handler<ReactorChangeWasteEjectionMessage> {
		
		@Override
		protected IMessage handleMessage(
				ReactorChangeWasteEjectionMessage message, MessageContext ctx,
				MultiblockReactor reactor) {
			reactor.setWasteEjection(MultiblockReactor.s_EjectionSettings[message.newSetting]);
			return null;
		}
	}
}

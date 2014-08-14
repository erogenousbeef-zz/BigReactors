package erogenousbeef.bigreactors.net.message.multiblock;

import io.netty.buffer.ByteBuf;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import erogenousbeef.bigreactors.common.multiblock.MultiblockReactor;
import erogenousbeef.bigreactors.net.message.base.ReactorMessageClient;

public class ReactorUpdateMessage extends ReactorMessageClient {
	ByteBuf data;

	public ReactorUpdateMessage() { super(); }
	public ReactorUpdateMessage(MultiblockReactor reactor) {
		super(reactor);
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		super.fromBytes(buf);
		data = buf.readBytes(buf.readableBytes());
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		super.toBytes(buf);
		reactor.serialize(buf);
	}
	
	public static class Handler extends ReactorMessageClient.Handler<ReactorUpdateMessage> {
		@Override
		protected IMessage handleMessage(ReactorUpdateMessage message,
				MessageContext ctx, MultiblockReactor reactor) {
			reactor.deserialize(message.data);
			return null;
		}
	}
}

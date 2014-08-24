package erogenousbeef.bigreactors.net.message.multiblock;

import io.netty.buffer.ByteBuf;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import erogenousbeef.bigreactors.common.multiblock.MultiblockTurbine;
import erogenousbeef.bigreactors.net.message.base.TurbineMessageClient;

public class TurbineUpdateMessage extends TurbineMessageClient {
	protected ByteBuf data;
	
	public TurbineUpdateMessage() { super(); data = null; }
	public TurbineUpdateMessage(MultiblockTurbine turbine) {
		super(turbine);
		data = null;
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		super.toBytes(buf);
		turbine.serialize(buf);
	}
	
	@Override public void fromBytes(ByteBuf buf) {
		super.fromBytes(buf);
		data = buf.readBytes(buf.readableBytes());
	}
	
	public static class Handler extends TurbineMessageClient.Handler<TurbineUpdateMessage> {
		@Override
		protected IMessage handleMessage(TurbineUpdateMessage message,
				MessageContext ctx, MultiblockTurbine turbine) {
			turbine.deserialize(message.data);
			return null;
		}
	}
}

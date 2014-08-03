package erogenousbeef.bigreactors.net.message.multiblock;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import erogenousbeef.bigreactors.common.multiblock.MultiblockTurbine;
import erogenousbeef.bigreactors.net.message.base.TurbineMessageServer;

public class TurbineChangeMaxIntakeMessage extends TurbineMessageServer {
	int newSetting;
	public TurbineChangeMaxIntakeMessage() { super(); newSetting = MultiblockTurbine.MAX_PERMITTED_FLOW; }
	public TurbineChangeMaxIntakeMessage(MultiblockTurbine turbine, int newSetting) {
		super(turbine);
		this.newSetting = newSetting;
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
	
	public static class Handler extends TurbineMessageServer.Handler<TurbineChangeMaxIntakeMessage> {
		@Override
		protected IMessage handleMessage(TurbineChangeMaxIntakeMessage message,
				MessageContext ctx, MultiblockTurbine turbine) {
			turbine.setMaxIntakeRate(message.newSetting);
			return null;
		}
	}
}

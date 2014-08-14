package erogenousbeef.bigreactors.net.message.multiblock;

import io.netty.buffer.ByteBuf;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import erogenousbeef.bigreactors.common.multiblock.MultiblockTurbine;
import erogenousbeef.bigreactors.net.message.base.TurbineMessageServer;

public class TurbineChangeInductorMessage extends TurbineMessageServer {
	boolean newSetting;
	public TurbineChangeInductorMessage() { super(); newSetting = true; }
	public TurbineChangeInductorMessage(MultiblockTurbine turbine, boolean newSetting) {
		super(turbine);
		this.newSetting = newSetting;
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		super.toBytes(buf);
		buf.writeBoolean(newSetting);
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		super.fromBytes(buf);
		newSetting = buf.readBoolean();
	}
	
	public static class Handler extends TurbineMessageServer.Handler<TurbineChangeInductorMessage> {
		@Override
		protected IMessage handleMessage(TurbineChangeInductorMessage message,
				MessageContext ctx, MultiblockTurbine turbine) {
			turbine.setInductorEngaged(message.newSetting, true);
			return null;
		}
	}
}

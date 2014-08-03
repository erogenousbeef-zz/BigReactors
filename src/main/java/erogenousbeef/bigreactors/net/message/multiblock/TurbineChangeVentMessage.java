package erogenousbeef.bigreactors.net.message.multiblock;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import erogenousbeef.bigreactors.common.multiblock.MultiblockTurbine;
import erogenousbeef.bigreactors.net.message.base.TurbineMessageServer;

public class TurbineChangeVentMessage extends TurbineMessageServer {
	int newSetting;
	public TurbineChangeVentMessage() { super(); newSetting = 0; }
	public TurbineChangeVentMessage(MultiblockTurbine turbine, MultiblockTurbine.VentStatus newStatus) {
		super(turbine);
		this.newSetting = newStatus.ordinal();
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
	
	public static class Handler extends TurbineMessageServer.Handler<TurbineChangeVentMessage> {
		@Override
		protected IMessage handleMessage(TurbineChangeVentMessage message,
				MessageContext ctx, MultiblockTurbine turbine) {
			turbine.setVentStatus(MultiblockTurbine.s_VentStatuses[message.newSetting], true);
			return null;
		}
	}
}

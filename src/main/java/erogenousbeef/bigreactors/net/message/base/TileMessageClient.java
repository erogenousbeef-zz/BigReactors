package erogenousbeef.bigreactors.net.message.base;

import net.minecraft.tileentity.TileEntity;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import erogenousbeef.bigreactors.common.BRLog;

public class TileMessageClient<TE extends TileEntity> extends WorldMessageClient {

	protected TileMessageClient() {}
	protected TileMessageClient(TE te) {
		super(te.xCoord, te.yCoord, te.zCoord);
	}
	
	protected abstract static class Handler<MESSAGE extends TileMessageClient, TE extends TileEntity> extends WorldMessageClient.Handler<MESSAGE> {
		protected abstract IMessage handle(MESSAGE message, MessageContext ctx, TE te);
		protected abstract TE getImpl(TileEntity te);
		
		@Override
		protected IMessage handleMessage(MESSAGE message, MessageContext ctx,
				TileEntity te) {
			TE concrete = getImpl(te);
			if(concrete != null) {
				return handle(message, ctx, concrete);
			}
			else {
				BRLog.error("Received a TileMessageClient for a non-resolvable TileEntity @ %d, %d, %d", message.x, message.y, message.z);
			}
			return null;
		}
		
	}
	
}

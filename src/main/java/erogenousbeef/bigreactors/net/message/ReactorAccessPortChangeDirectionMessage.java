package erogenousbeef.bigreactors.net.message;

import net.minecraft.tileentity.TileEntity;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import erogenousbeef.bigreactors.common.multiblock.MultiblockReactor;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorAccessPort;
import erogenousbeef.bigreactors.net.message.base.ReactorMessageServer;
import erogenousbeef.bigreactors.net.message.base.TileMessageServer;

public class ReactorAccessPortChangeDirectionMessage extends TileMessageServer<TileEntityReactorAccessPort> {
	public ReactorAccessPortChangeDirectionMessage() {}
	public ReactorAccessPortChangeDirectionMessage(TileEntityReactorAccessPort port) {
		super(port);
	}
	
	public static class Handler extends TileMessageServer.Handler<ReactorAccessPortChangeDirectionMessage, TileEntityReactorAccessPort> {
		@Override
		protected IMessage handle(ReactorAccessPortChangeDirectionMessage message,
				MessageContext ctx, TileEntityReactorAccessPort te) {
			te.toggleDirection();
			return null;
		}

		@Override
		protected TileEntityReactorAccessPort getImpl(TileEntity te) {
			if(te instanceof TileEntityReactorAccessPort) {
				return (TileEntityReactorAccessPort)te;
			}
			return null;
		}
		
	}
}

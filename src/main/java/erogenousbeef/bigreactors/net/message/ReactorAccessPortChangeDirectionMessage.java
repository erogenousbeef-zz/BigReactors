package erogenousbeef.bigreactors.net.message;

import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import erogenousbeef.bigreactors.common.multiblock.MultiblockReactor;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorAccessPort;
import erogenousbeef.bigreactors.net.message.base.ReactorMessageServer;
import erogenousbeef.bigreactors.net.message.base.TileMessageServer;

public class ReactorAccessPortChangeDirectionMessage extends TileMessageServer<TileEntityReactorAccessPort> {
	private boolean newSetting;
	public ReactorAccessPortChangeDirectionMessage() { super(); newSetting = true; }
	public ReactorAccessPortChangeDirectionMessage(TileEntityReactorAccessPort port, boolean inlet) {
		super(port);
		newSetting = inlet;
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
	
	public static class Handler extends TileMessageServer.Handler<ReactorAccessPortChangeDirectionMessage, TileEntityReactorAccessPort> {
		@Override
		protected IMessage handle(ReactorAccessPortChangeDirectionMessage message,
				MessageContext ctx, TileEntityReactorAccessPort te) {
			te.setInlet(message.newSetting);
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

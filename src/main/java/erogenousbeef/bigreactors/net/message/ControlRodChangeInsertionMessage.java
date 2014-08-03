package erogenousbeef.bigreactors.net.message;

import net.minecraft.tileentity.TileEntity;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorControlRod;
import erogenousbeef.bigreactors.net.message.base.WorldMessageServer;

public class ControlRodChangeInsertionMessage extends WorldMessageServer {
	protected int amount;
	
	public ControlRodChangeInsertionMessage() { super(); amount = 0; }
	public ControlRodChangeInsertionMessage(int x, int y, int z, int amount) { 
		super(x, y, z);
		this.amount = amount;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		super.fromBytes(buf);
		amount = buf.readInt();
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		super.toBytes(buf);
		buf.writeInt(amount);
	}

	public static class Handler extends WorldMessageServer.Handler<ControlRodChangeInsertionMessage> {
		@Override
		protected IMessage handleMessage(ControlRodChangeInsertionMessage message, 
										MessageContext ctx, TileEntity te) {
			if(te instanceof TileEntityReactorControlRod) {
				((TileEntityReactorControlRod)te).onClientControlRodChange(message.amount);
			}
			return null;
		}
	}
}

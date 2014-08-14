package erogenousbeef.bigreactors.net.message;

import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorControlRod;
import erogenousbeef.bigreactors.net.message.base.WorldMessageServer;

public class ControlRodChangeInsertionMessage extends WorldMessageServer {
	protected int amount;
	protected boolean changeAll;
	
	public ControlRodChangeInsertionMessage() { super(); amount = 0; changeAll = false; }
	public ControlRodChangeInsertionMessage(int x, int y, int z, int amount, boolean all) { 
		super(x, y, z);
		this.amount = amount;
		this.changeAll = all;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		super.fromBytes(buf);
		amount = buf.readInt();
		changeAll = buf.readBoolean();
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		super.toBytes(buf);
		buf.writeInt(amount);
		buf.writeBoolean(changeAll);
	}

	public static class Handler extends WorldMessageServer.Handler<ControlRodChangeInsertionMessage> {
		@Override
		protected IMessage handleMessage(ControlRodChangeInsertionMessage message, 
										MessageContext ctx, TileEntity te) {
			if(te instanceof TileEntityReactorControlRod) {
				TileEntityReactorControlRod rod = (TileEntityReactorControlRod)te;
				int newInsertion = rod.getControlRodInsertion() + (short)message.amount;
				if(message.changeAll && rod.getReactorController() != null)
				{
					rod.getReactorController().setAllControlRodInsertionValues(newInsertion);
				}
				else {
					rod.setControlRodInsertion((short)newInsertion);
				}
			}
			return null;
		}
	}
}

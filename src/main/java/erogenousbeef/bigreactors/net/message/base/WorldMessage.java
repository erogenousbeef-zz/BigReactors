package erogenousbeef.bigreactors.net.message.base;

import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import erogenousbeef.bigreactors.common.BRLog;

/**
 * This class implements a message which is being sent to a specific location
 * in a game world.
 * 
 * @author Yoru
 *
 */
public abstract class WorldMessage implements IMessage {
	protected int x, y, z;
	
	protected WorldMessage() {}
	protected WorldMessage(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		x = buf.readInt();
		y = buf.readInt();
		z = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(x);
		buf.writeInt(y);
		buf.writeInt(z);
	}

	
	public abstract static class Handler<M extends WorldMessage> implements IMessageHandler<M, IMessage> {

		protected abstract World getWorld(MessageContext ctxt);
		
		protected abstract IMessage handleMessage(M message, MessageContext ctx, TileEntity te);
		
		public IMessage onMessage(WorldMessage message, MessageContext ctx) {
			World world = getWorld(ctx);
			if(world == null) {
				BRLog.fatal("Unable to resolve world from messagecontext for WorldMessage");
				return null;
			}
			
			TileEntity te = world.getTileEntity(message.x, message.y, message.z);
			if(te == null) {
				BRLog.error("Unable to find tile entity for WorldMessage at %d, %d, %d", message.x, message.y, message.z);
				return null;
			}

			return handleMessage((M)message, ctx, te);
		}
	}	
}

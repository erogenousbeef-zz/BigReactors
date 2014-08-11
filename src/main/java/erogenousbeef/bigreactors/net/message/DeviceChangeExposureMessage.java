package erogenousbeef.bigreactors.net.message;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import io.netty.buffer.ByteBuf;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import erogenousbeef.bigreactors.common.BRLog;
import erogenousbeef.bigreactors.common.tileentity.base.TileEntityBeefBase;
import erogenousbeef.bigreactors.common.tileentity.base.TileEntityInventory;
import erogenousbeef.bigreactors.net.message.base.WorldMessage;
import erogenousbeef.bigreactors.net.message.base.WorldMessageServer;

/**
 * A message signifying that a user would like to change
 * the inventory/fluid exposure status of the side of a block.
 * @author Erogenous Beef
 *
 */
public class DeviceChangeExposureMessage extends WorldMessageServer {
	private int side;
	private boolean increment;
	
	public DeviceChangeExposureMessage() {}

	public DeviceChangeExposureMessage(int x, int y, int z, int side, boolean increment) {
		super(x, y, z);
		this.side = side;
		this.increment = increment;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		super.fromBytes(buf);
		this.side = buf.readInt();
		this.increment = buf.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		super.toBytes(buf);
		buf.writeInt(side);
		buf.writeBoolean(increment);
	}
	
	public static class Handler extends WorldMessageServer.Handler<DeviceChangeExposureMessage> {
		@Override
		protected IMessage handleMessage(DeviceChangeExposureMessage message, MessageContext ctx, TileEntity te) {
			if(te instanceof TileEntityBeefBase) {
				TileEntityBeefBase beefTe = (TileEntityBeefBase)te;
				if(message.increment) {
					beefTe.incrSide(message.side);
				}
				else {
					beefTe.decrSide(message.side);
				}
			}
			else {
				BRLog.warning("Received SideChangeMessage for TE at %d, %d, %d, but it was not a TE with an iterable side exposure!", te.xCoord, te.yCoord, te.zCoord);
			}
			return null;
		}
	}
}

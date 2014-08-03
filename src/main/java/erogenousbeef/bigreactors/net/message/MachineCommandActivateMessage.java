package erogenousbeef.bigreactors.net.message;

import net.minecraft.tileentity.TileEntity;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import erogenousbeef.bigreactors.common.BRLog;
import erogenousbeef.bigreactors.common.interfaces.IActivateable;
import erogenousbeef.bigreactors.net.message.base.WorldMessageServer;
import erogenousbeef.core.common.CoordTriplet;

/**
 * Send a "setActive" command to any IActivateable machine.
 * Currently used for multiblock reactors and turbines.
 * @see erogenousbeef.bigreactors.common.interfaces.IActivateable
 * @author Erogenous Beef
 *
 */
public class MachineCommandActivateMessage extends WorldMessageServer {
	protected boolean setActive;
	public MachineCommandActivateMessage() { super(); setActive = true; }

	protected MachineCommandActivateMessage(CoordTriplet coord, boolean setActive) {
		super(coord.x, coord.y, coord.z);
		this.setActive = setActive;
	}

	public MachineCommandActivateMessage(IActivateable machine, boolean setActive) {
		this(machine.getReferenceCoord(), setActive);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		super.toBytes(buf);
		buf.writeBoolean(setActive);
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		super.fromBytes(buf);
		setActive = buf.readBoolean();
	}
	
	public static class Handler extends WorldMessageServer.Handler<MachineCommandActivateMessage> {
		@Override
		protected IMessage handleMessage(MachineCommandActivateMessage message,
				MessageContext ctx, TileEntity te) {
			if(te instanceof IActivateable) {
				IActivateable machine = (IActivateable)te;
				machine.setActive(message.setActive);
			}
			else {
				BRLog.error("Received a MachineCommandActivateMessage for %d, %d, %d but found no activateable machine", message.x, message.y, message.z);
			}
			return null;
		}
	}
	
}

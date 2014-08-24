package erogenousbeef.bigreactors.net.message.base;

import net.minecraft.tileentity.TileEntity;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import erogenousbeef.bigreactors.common.BRLog;
import erogenousbeef.bigreactors.common.multiblock.MultiblockTurbine;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityTurbinePartBase;
import erogenousbeef.core.common.CoordTriplet;

public class TurbineMessageClient extends WorldMessageClient {
	protected MultiblockTurbine turbine;
	
	protected TurbineMessageClient() { super(); turbine = null; }
	protected TurbineMessageClient(MultiblockTurbine turbine, CoordTriplet referenceCoord) {
		super(referenceCoord.x, referenceCoord.y, referenceCoord.z);
		this.turbine = turbine;
	}
	protected TurbineMessageClient(MultiblockTurbine turbine) {
		this(turbine, turbine.getReferenceCoord());
	}
	
	public static abstract class Handler<M extends TurbineMessageClient> extends WorldMessageClient.Handler<M> {
		protected abstract IMessage handleMessage(M message, MessageContext ctx, MultiblockTurbine turbine);

		@Override
		protected IMessage handleMessage(M message, MessageContext ctx, TileEntity te) {
			if(te instanceof TileEntityTurbinePartBase) {
				MultiblockTurbine reactor = ((TileEntityTurbinePartBase)te).getTurbine();
				if(reactor != null) {
					return handleMessage(message, ctx, reactor);
				}
				else {
					BRLog.error("Received TurbineMessageClient for a turbine part @ %d, %d, %d which has no attached turbine", te.xCoord, te.yCoord, te.zCoord);
				}
			}
			else {
				BRLog.error("Received TurbineMessageClient for a non-turbine-part block @ %d, %d, %d", message.x, message.y, message.z);
			}
			return null;
		}
	}
}

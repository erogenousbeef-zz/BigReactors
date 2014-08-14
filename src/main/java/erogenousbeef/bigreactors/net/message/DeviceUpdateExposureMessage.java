package erogenousbeef.bigreactors.net.message;

import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import erogenousbeef.bigreactors.common.tileentity.base.TileEntityBeefBase;
import erogenousbeef.bigreactors.net.message.base.WorldMessageClient;

public class DeviceUpdateExposureMessage extends WorldMessageClient {

	int[] exposures;
	
	public DeviceUpdateExposureMessage() {
		super();
		exposures = null;
	}
	
	public DeviceUpdateExposureMessage(int x, int y, int z, int[] exposures) {
		super(x, y, z);
		this.exposures = new int[exposures.length];
		System.arraycopy(exposures, 0, this.exposures, 0, this.exposures.length);
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		super.toBytes(buf);

		buf.writeInt(exposures.length);
		for(int i = 0; i < exposures.length; i++) {
			buf.writeInt(exposures[i]);
		}
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		super.fromBytes(buf);

		int numExposures = buf.readInt();
		assert(numExposures > 0);
		exposures = new int[numExposures];
		for(int i = 0; i < numExposures; i++) {
			exposures[i] = buf.readInt();
		}
	}
	
	public static class Handler extends WorldMessageClient.Handler<DeviceUpdateExposureMessage> {
		@Override
		protected IMessage handleMessage(DeviceUpdateExposureMessage message,
				MessageContext ctx, TileEntity te) {
			if(te instanceof TileEntityBeefBase) {
				TileEntityBeefBase beefTe = (TileEntityBeefBase)te;
				beefTe.setSides(message.exposures);
			}
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	
}

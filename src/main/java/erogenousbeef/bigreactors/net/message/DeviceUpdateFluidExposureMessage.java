package erogenousbeef.bigreactors.net.message;

import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import erogenousbeef.bigreactors.common.tileentity.base.TileEntityPoweredInventoryFluid;
import erogenousbeef.bigreactors.net.message.base.WorldMessageClient;

public class DeviceUpdateFluidExposureMessage extends WorldMessageClient {
    private int side, tankIdx;

    public DeviceUpdateFluidExposureMessage() { super(); side = -1; tankIdx = -1; }
    
    public DeviceUpdateFluidExposureMessage(int x, int y, int z, int side, int tankIdx) {
    	super(x, y, z);
        this.side = side;
        this.tankIdx = tankIdx;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
    	super.fromBytes(buf);
        side = buf.readInt();
        tankIdx = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
    	super.toBytes(buf);
        buf.writeInt(side);
        buf.writeInt(tankIdx);
    }

    public static class Handler extends WorldMessageClient.Handler<DeviceUpdateFluidExposureMessage> {
        @Override
        public IMessage handleMessage(DeviceUpdateFluidExposureMessage message, MessageContext ctx, TileEntity te) {
            if(te instanceof TileEntityPoweredInventoryFluid) {
                ((TileEntityPoweredInventoryFluid)te).setExposedTank(ForgeDirection.getOrientation(message.side), message.tankIdx);
                getWorld(ctx).markBlockForUpdate(message.x, message.y, message.z);
            }
            return null;
        }
    }
}

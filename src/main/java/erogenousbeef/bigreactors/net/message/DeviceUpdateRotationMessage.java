package erogenousbeef.bigreactors.net.message;

import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;
import cofh.api.tileentity.IReconfigurableFacing;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import erogenousbeef.bigreactors.net.message.base.WorldMessageClient;

public class DeviceUpdateRotationMessage extends WorldMessageClient {
    private int newOrientation;

    public DeviceUpdateRotationMessage() { super(); newOrientation = 0; }
    
    public DeviceUpdateRotationMessage(int x, int y, int z, int newOrientation) {
    	super(x, y, z);
        this.newOrientation = newOrientation;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
    	super.fromBytes(buf);
        newOrientation = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
    	super.toBytes(buf);
        buf.writeInt(newOrientation);
    }

    public static class Handler extends WorldMessageClient.Handler<DeviceUpdateRotationMessage> {
        @Override
        public IMessage handleMessage(DeviceUpdateRotationMessage message, MessageContext ctx, TileEntity te) {
            if(te instanceof IReconfigurableFacing) {
                ((IReconfigurableFacing)te).setFacing(message.newOrientation);
                getWorld(ctx).markBlockForUpdate(message.x, message.y, message.z);
            }
            return null;
        }
    }
}

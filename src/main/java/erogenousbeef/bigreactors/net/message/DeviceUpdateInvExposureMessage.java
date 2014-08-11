package erogenousbeef.bigreactors.net.message;

import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import erogenousbeef.bigreactors.common.tileentity.base.TileEntityInventory;
import erogenousbeef.bigreactors.net.message.base.WorldMessageClient;

public class DeviceUpdateInvExposureMessage extends WorldMessageClient {
    private int referenceSide, slot;

    public DeviceUpdateInvExposureMessage() { super(); referenceSide = -1; slot = -1; }
    
    public DeviceUpdateInvExposureMessage(int x, int y, int z, int referenceSide, int slot) {
    	super(x, y, z);
        this.referenceSide = referenceSide;
        this.slot = slot;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
    	super.fromBytes(buf);
        referenceSide = buf.readInt();
        slot = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
    	super.toBytes(buf);
        buf.writeInt(referenceSide);
        buf.writeInt(slot);
    }
    
    public static class Handler extends WorldMessageClient.Handler<DeviceUpdateInvExposureMessage> {
        @Override
        public IMessage handleMessage(DeviceUpdateInvExposureMessage message, MessageContext ctx, TileEntity te) {
            if(te instanceof TileEntityInventory) {
                ((TileEntityInventory)te).setExposedInventorySlotReference(message.referenceSide, message.slot);
                getWorld(ctx).markBlockForUpdate(message.x, message.y, message.z);
            }
            return null;
        }
    }
}

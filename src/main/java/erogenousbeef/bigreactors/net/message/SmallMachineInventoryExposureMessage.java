package erogenousbeef.bigreactors.net.message;

import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import erogenousbeef.bigreactors.common.tileentity.base.TileEntityInventory;

public class SmallMachineInventoryExposureMessage implements IMessage {
    private int x, y, z, referenceSide, slot;

    public SmallMachineInventoryExposureMessage() {}
    
    public SmallMachineInventoryExposureMessage(int x, int y, int z, int referenceSide, int slot) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.referenceSide = referenceSide;
        this.slot = slot;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        referenceSide = buf.readInt();
        slot = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeInt(referenceSide);
        buf.writeInt(slot);
    }

    
    public static class Handler implements IMessageHandler<SmallMachineInventoryExposureMessage, IMessage> {
        @Override
        public IMessage onMessage(SmallMachineInventoryExposureMessage message, MessageContext ctx) {
            TileEntity te = FMLClientHandler.instance().getWorldClient().getTileEntity(message.x, message.y, message.z);
            if(te != null && te instanceof TileEntityInventory) {
                ((TileEntityInventory)te).setExposedInventorySlotReference(message.referenceSide, message.slot);
                FMLClientHandler.instance().getWorldClient().markBlockForUpdate(message.x, message.y, message.z);
            }
            return null;
        }
    }
}

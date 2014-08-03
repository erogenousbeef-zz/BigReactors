package erogenousbeef.bigreactors.net.message;

import net.minecraft.tileentity.TileEntity;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorRedstonePort;
import io.netty.buffer.ByteBuf;

public class RedstoneSetDataMessage implements IMessage {
    private int x, y, z, newCircut, newLevel;
    private boolean newGt, pulse;

    public RedstoneSetDataMessage() {}
    
    public RedstoneSetDataMessage(int x, int y, int z, int newCircut, int newLevel, boolean newGt, boolean pulse) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.newCircut = newCircut;
        this.newLevel = newLevel;
        this.newGt = newGt;
        this.pulse = pulse;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        newCircut = buf.readInt();
        newLevel = buf.readInt();
        newGt = buf.readBoolean();
        pulse = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeInt(newCircut);
        buf.writeInt(newLevel);
        buf.writeBoolean(newGt);
        buf.writeBoolean(pulse);
    }

    public static class Handler implements IMessageHandler<RedstoneSetDataMessage, IMessage> {
        @Override
        public IMessage onMessage(RedstoneSetDataMessage message, MessageContext ctx) {
            TileEntity te = ctx.getServerHandler().playerEntity.worldObj.getTileEntity(message.x, message.y, message.z);
            if(te != null && te instanceof TileEntityReactorRedstonePort) {
                ((TileEntityReactorRedstonePort)te).onReceiveUpdatePacket(message.newCircut, message.newLevel, message.newGt, message.pulse);
            }
            return null;
        }
    }
}

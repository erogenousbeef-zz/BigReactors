package erogenousbeef.bigreactors.net.message;

import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorControlRod;

public class ControlRodUpdateMessage implements IMessage {
    private int x, y, z;
    private short controlRodInsertion;

    public ControlRodUpdateMessage() {}
    
    public ControlRodUpdateMessage(int x, int y, int z, short controlRodInsertion) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.controlRodInsertion = controlRodInsertion;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        controlRodInsertion = buf.readShort();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeShort(controlRodInsertion);
    }

    public static class Handler implements IMessageHandler<ControlRodUpdateMessage, IMessage>
    {
        @Override
        public IMessage onMessage(ControlRodUpdateMessage message, MessageContext ctx) {
            TileEntity te = FMLClientHandler.instance().getWorldClient().getTileEntity(message.x, message.y, message.z);
            if(te != null && te instanceof TileEntityReactorControlRod) {
                ((TileEntityReactorControlRod)te).onControlRodUpdate(message.controlRodInsertion);
            }
            return null;
        }
    }
}

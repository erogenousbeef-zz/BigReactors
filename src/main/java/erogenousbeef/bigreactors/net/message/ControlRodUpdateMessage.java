package erogenousbeef.bigreactors.net.message;

import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorControlRod;
import erogenousbeef.bigreactors.net.message.base.WorldMessageClient;

public class ControlRodUpdateMessage extends WorldMessageClient {
    private short insertion;

    public ControlRodUpdateMessage() { super(); insertion = 0; }
    
    public ControlRodUpdateMessage(int x, int y, int z, short controlRodInsertion) {
    	super(x, y, z);
        this.insertion = controlRodInsertion;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
    	super.fromBytes(buf);
        insertion = buf.readShort();
    }

    @Override
    public void toBytes(ByteBuf buf) {
    	super.toBytes(buf);
        buf.writeShort(insertion);
    }

    public static class Handler extends WorldMessageClient.Handler<ControlRodUpdateMessage>
    {
        @Override
        protected IMessage handleMessage(ControlRodUpdateMessage message, MessageContext ctx, TileEntity te) {
            if(te instanceof TileEntityReactorControlRod) {
                ((TileEntityReactorControlRod)te).onControlRodUpdate(message.insertion);
            }
            return null;
        }
    }
}

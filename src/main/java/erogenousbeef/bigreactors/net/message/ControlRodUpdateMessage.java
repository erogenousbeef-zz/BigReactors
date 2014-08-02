package erogenousbeef.bigreactors.net.message;

import net.minecraft.tileentity.TileEntity;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorControlRod;
import io.netty.buffer.ByteBuf;

public class ControlRodUpdateMessage implements IMessage, IMessageHandler<ControlRodUpdateMessage, IMessage> {
    private int x, y, z;
    private short controlRodInsertion;

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

    @Override
    public IMessage onMessage(ControlRodUpdateMessage message, MessageContext ctx) {
        TileEntity te = FMLClientHandler.instance().getWorldClient().getTileEntity(x, y, z);
        if(te != null && te instanceof TileEntityReactorControlRod) {
            ((TileEntityReactorControlRod)te).onControlRodUpdate(controlRodInsertion);
        }
        return null;
    }
}

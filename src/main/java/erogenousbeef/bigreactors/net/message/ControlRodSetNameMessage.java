package erogenousbeef.bigreactors.net.message;

import net.minecraft.tileentity.TileEntity;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorControlRod;
import io.netty.buffer.ByteBuf;

public class ControlRodSetNameMessage implements IMessage, IMessageHandler<ControlRodSetNameMessage, IMessage> {
    private int x, y, z;
    private String name;

    public ControlRodSetNameMessage(int x, int y, int z, String name) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.name = name;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        name = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        ByteBufUtils.writeUTF8String(buf, name);
    }

    @Override
    public IMessage onMessage(ControlRodSetNameMessage message, MessageContext ctx) {
        TileEntity te = ctx.getServerHandler().playerEntity.worldObj.getTileEntity(x, y, z);
        if(te != null && te instanceof TileEntityReactorControlRod) {
            ((TileEntityReactorControlRod)te).setName(name);
        }
        return null;
    }
}

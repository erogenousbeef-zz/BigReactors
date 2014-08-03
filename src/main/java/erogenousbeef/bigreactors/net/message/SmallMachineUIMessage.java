package erogenousbeef.bigreactors.net.message;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import erogenousbeef.bigreactors.common.tileentity.base.TileEntityBeefBase;

public class SmallMachineUIMessage implements IMessage {
    private int x, y, z;
    private NBTTagCompound compound;

    public SmallMachineUIMessage() {}
    
    public SmallMachineUIMessage(int x, int y, int z, NBTTagCompound compound) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.compound = compound;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        compound = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        ByteBufUtils.writeTag(buf, compound);
    }

    public static class Handler implements IMessageHandler<SmallMachineUIMessage, IMessage> {
        @Override
        public IMessage onMessage(SmallMachineUIMessage message, MessageContext ctx) {
            TileEntity te = FMLClientHandler.instance().getWorldClient().getTileEntity(message.x, message.y, message.z);
            if(te != null && te instanceof TileEntityBeefBase) {
                ((TileEntityBeefBase)te).onReceiveUpdate(message.compound);
            }
            return null;
        }
    }
}

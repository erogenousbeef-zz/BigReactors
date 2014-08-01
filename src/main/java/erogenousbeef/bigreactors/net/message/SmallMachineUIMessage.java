package erogenousbeef.bigreactors.net.message;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import erogenousbeef.bigreactors.common.tileentity.base.TileEntityBeefBase;
import io.netty.buffer.ByteBuf;

public class SmallMachineUIMessage implements IMessage, IMessageHandler<SmallMachineUIMessage, IMessage> {
    private int x, y, z;
    private NBTTagCompound compound;

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

    @Override
    public IMessage onMessage(SmallMachineUIMessage message, MessageContext ctx) {
        TileEntity te = FMLClientHandler.instance().getWorldClient().getTileEntity(x, y, z);
        if(te != null && te instanceof TileEntityBeefBase) {
            ((TileEntityBeefBase)te).onReceiveUpdate(compound);
        }
        return null;
    }
}

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
import erogenousbeef.bigreactors.net.message.base.WorldMessageClient;

public class DeviceUpdateMessage extends WorldMessageClient {
    private NBTTagCompound compound;

    public DeviceUpdateMessage() { super(); compound = null; }
    
    public DeviceUpdateMessage(int x, int y, int z, NBTTagCompound compound) {
    	super(x, y, z);
        this.compound = compound;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
    	super.fromBytes(buf);
        compound = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
    	super.toBytes(buf);
        ByteBufUtils.writeTag(buf, compound);
    }

    public static class Handler extends WorldMessageClient.Handler<DeviceUpdateMessage> {
        @Override
        public IMessage handleMessage(DeviceUpdateMessage message, MessageContext ctx, TileEntity te) {
            if(te instanceof TileEntityBeefBase) {
                ((TileEntityBeefBase)te).onReceiveUpdate(message.compound);
            }
            return null;
        }
    }
}

package erogenousbeef.bigreactors.net.message;

import io.netty.buffer.ByteBuf;

import java.io.IOException;

import net.minecraft.tileentity.TileEntity;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import erogenousbeef.bigreactors.gui.IBeefGuiEntity;
import erogenousbeef.bigreactors.utils.NetworkUtils;

public class GuiButtonPressMessage implements IMessage {
    private int x, y, z;
    private String buttonName;
    private Object[] data;
    private ByteBuf dis;

    public GuiButtonPressMessage() {}
    
    public GuiButtonPressMessage(int x, int y, int z, String buttonName, Object... data) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.buttonName = buttonName;
        this.data = data;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        buttonName = ByteBufUtils.readUTF8String(buf);
        dis = buf.readBytes(buf.readableBytes());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        ByteBufUtils.writeUTF8String(buf, buttonName);
        for(Object obj: data) {
            NetworkUtils.writeObjectToByteBuf(buf, obj);
        }
    }

    public static class Handler implements IMessageHandler<GuiButtonPressMessage, IMessage> {
        @Override
        public IMessage onMessage(GuiButtonPressMessage message, MessageContext ctx) {
            TileEntity te = ctx.getServerHandler().playerEntity.worldObj.getTileEntity(message.x, message.y, message.z);
            if(te != null && te instanceof IBeefGuiEntity) {
                try {
                    ((IBeefGuiEntity)te).onReceiveGuiButtonPress(message.buttonName, message.dis);
                } catch(IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }
}

package erogenousbeef.bigreactors.net.message;

import java.io.DataInputStream;
import java.io.IOException;

import net.minecraft.tileentity.TileEntity;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import erogenousbeef.bigreactors.gui.IBeefGuiEntity;
import erogenousbeef.bigreactors.utils.NetworkUtils;
import io.netty.buffer.ByteBuf;

public class GuiButtonPressMessage implements IMessage, IMessageHandler<GuiButtonPressMessage, IMessage> {
    private int x, y, z;
    private String buttonName;
    private Object[] data;
    private DataInputStream dis;

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
        dis = NetworkUtils.toDataInputStream(buf);
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

    @Override
    public IMessage onMessage(GuiButtonPressMessage message, MessageContext ctx) {
        TileEntity te = ctx.getServerHandler().playerEntity.worldObj.getTileEntity(x, y, z);
        if(te != null && te instanceof IBeefGuiEntity) {
            try {
                ((IBeefGuiEntity)te).onReceiveGuiButtonPress(buttonName, dis);
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}

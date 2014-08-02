package erogenousbeef.bigreactors.net.message;

import java.io.DataInputStream;
import java.io.IOException;

import net.minecraft.tileentity.TileEntity;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import erogenousbeef.bigreactors.common.multiblock.interfaces.IMultiblockNetworkHandler;
import erogenousbeef.bigreactors.utils.NetworkUtils;
import io.netty.buffer.ByteBuf;

public class MultiblockMessageClient extends MultiblockMessage {
    public MultiblockMessageClient(Type type, int x, int y, int z, Object... data) {
    	super(type, x, y, z, data);
    }

    public static class Handler implements  IMessageHandler<MultiblockMessageClient, IMessage>
    {
        @Override
        public IMessage onMessage(MultiblockMessageClient message, MessageContext ctx) {
            TileEntity te = FMLClientHandler.instance().getWorldClient().getTileEntity(message.x, message.y, message.z);
            if(te != null && te instanceof IMultiblockNetworkHandler) {
                try {
                    ((IMultiblockNetworkHandler)te).onNetworkPacket(message.type, message.dis);
                } catch(IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }
}

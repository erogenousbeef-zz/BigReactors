package erogenousbeef.bigreactors.net.message;

import java.io.IOException;

import net.minecraft.tileentity.TileEntity;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import erogenousbeef.bigreactors.common.multiblock.interfaces.IMultiblockNetworkHandler;

public class MultiblockMessageServer extends MultiblockMessage {
	public MultiblockMessageServer() {}

    public MultiblockMessageServer(Type type, int x, int y, int z, Object... data) {
    	super(type, x, y, z, data);
    }

    public static class Handler implements IMessageHandler<MultiblockMessageServer, IMessage>
    {
        @Override
        public IMessage onMessage(MultiblockMessageServer message, MessageContext ctx) {
            TileEntity te = ctx.getServerHandler().playerEntity.worldObj.getTileEntity(message.x, message.y, message.z);
            if(te != null && te instanceof IMultiblockNetworkHandler) {
                try {
                    ((IMultiblockNetworkHandler)te).onNetworkPacket(message.type, message.bytes);
                } catch(IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }    
}

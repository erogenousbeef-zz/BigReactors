package erogenousbeef.bigreactors.net.message;

import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import erogenousbeef.bigreactors.common.multiblock.MultiblockReactor;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorPart;
import erogenousbeef.bigreactors.net.message.base.WorldMessageClient;

// TODO: Refactor this into a MultiblockMessageClient
public class ReactorUpdateWasteEjectionMessage extends WorldMessageClient {
    private int newSetting;
    
    public ReactorUpdateWasteEjectionMessage() { super(); newSetting = 0; }

    public ReactorUpdateWasteEjectionMessage(int x, int y, int z, int newSetting) {
    	super(x, y, z);
        this.newSetting = newSetting;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
    	super.fromBytes(buf);
        newSetting = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
    	super.toBytes(buf);
        buf.writeInt(newSetting);
    }

    public static class Handler extends WorldMessageClient.Handler<ReactorUpdateWasteEjectionMessage> {
    	protected static final MultiblockReactor.WasteEjectionSetting[] s_Values = MultiblockReactor.WasteEjectionSetting.values();

        @Override
        public IMessage handleMessage(ReactorUpdateWasteEjectionMessage message, MessageContext ctx, TileEntity te) {
            if(te instanceof TileEntityReactorPart) {
                ((TileEntityReactorPart)te).getReactorController().setWasteEjection(s_Values[message.newSetting]);
            }
            return null;
        }    	
    }
}

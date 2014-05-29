package welfare93.bigreactors.packet;

import net.minecraft.entity.player.EntityPlayer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public abstract class AbstractPacket {

	public abstract void handleServerSide(EntityPlayer player);

	public abstract void handleClientSide(EntityPlayer player);

	public abstract void decodeInto(ChannelHandlerContext ctx, ByteBuf slice) ;

	public abstract void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer);

}

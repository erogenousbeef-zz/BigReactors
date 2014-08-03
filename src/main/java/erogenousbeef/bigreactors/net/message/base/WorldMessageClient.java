package erogenousbeef.bigreactors.net.message.base;

import net.minecraft.world.World;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

/**
 * A message to a client user which is grounded in world-space.
 * Generally, such messages will be sent FROM the server.
 * @author Erogenous Beef
 */
public abstract class WorldMessageClient extends WorldMessage {
	protected WorldMessageClient() { super(); }
	protected WorldMessageClient(int x, int y, int z) {
		super(x, y, z);
	}

	public abstract static class Handler<M extends WorldMessageClient> extends WorldMessage.Handler<M> {
		@Override 
		protected World getWorld(MessageContext ctx) {
			return FMLClientHandler.instance().getWorldClient();
		}
	}
}

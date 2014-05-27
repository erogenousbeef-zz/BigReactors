package erogenousbeef.core.multiblock;

import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.EventPriority;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;

/**
 * In your mod, subscribe this on both the client and server sides side to handle chunk
 * load events for your multiblock machines.
 * Chunks can load asynchronously in environments like MCPC+, so we cannot
 * process any blocks that are in chunks which are still loading.
 */
public class MultiblockEventHandler {
	@ForgeSubscribe(priority = EventPriority.NORMAL)
	public void onChunkLoad(ChunkEvent.Load loadEvent) {
		Chunk chunk = loadEvent.getChunk();
		World world = loadEvent.world;
		MultiblockRegistry.onChunkLoaded(world, chunk.xPosition, chunk.zPosition);
	}

	// Cleanup, for nice memory usageness
	@ForgeSubscribe(priority = EventPriority.NORMAL)
	public void onWorldUnload(WorldEvent.Unload unloadWorldEvent) {
		MultiblockRegistry.onWorldUnloaded(unloadWorldEvent.world);
	}
}

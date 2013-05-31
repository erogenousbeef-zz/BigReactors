package erogenousbeef.bigreactors.common;

import net.minecraft.world.ChunkCoordIntPair;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.world.ChunkDataEvent;

public class BREventHandler {

	@ForgeSubscribe
	public void chunkSave(ChunkDataEvent.Save saveEvent) {
		if(BigReactors.enableWorldGen) {
			saveEvent.getData().setInteger("BigReactorsWorldGen", BRConfig.WORLDGEN_VERSION);
		}
	}
	
	@ForgeSubscribe
	public void chunkLoad(ChunkDataEvent.Load loadEvent) {
		if(!BigReactors.enableWorldRegeneration || !BigReactors.enableWorldGen) {
			return;
		}

		if(loadEvent.getData().getInteger("BigReactorsWorldGen") == BRConfig.WORLDGEN_VERSION) {
			return;
		}
		
		if(!BigReactors.enableWorldGenInNegativeDimensions && loadEvent.world.provider.dimensionId < 0) {
			return;
		}
		
		ChunkCoordIntPair coordPair = loadEvent.getChunk().getChunkCoordIntPair();
		BigReactors.tickHandler.addRegenChunk(loadEvent.world.provider.dimensionId, coordPair);
	}
	
}

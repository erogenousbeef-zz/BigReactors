package erogenousbeef.bigreactors.common;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

import net.minecraft.world.ChunkCoordIntPair;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;

public class BigReactorsTickHandler {

	protected HashMap<Integer, Queue<ChunkCoordIntPair>> chunkRegenMap;
	protected static final long maximumDeltaTimeNanoSecs = 16000000; // 16 milliseconds
	
	public void addRegenChunk(int dimensionId, ChunkCoordIntPair chunkCoord) {
		if(chunkRegenMap == null) {
			chunkRegenMap = new HashMap<Integer, Queue<ChunkCoordIntPair>>();
		}
		
		if(!chunkRegenMap.containsKey(dimensionId)) {
			LinkedList<ChunkCoordIntPair> list = new LinkedList<ChunkCoordIntPair>();
			list.add(chunkCoord);
			chunkRegenMap.put(dimensionId, list);
		}
		else {
			if(!chunkRegenMap.get(dimensionId).contains(chunkCoord)) {
				chunkRegenMap.get(dimensionId).add(chunkCoord);
			}
		}
	}

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if(event.side == Side.SERVER && event.phase == TickEvent.Phase.END) {
            if(chunkRegenMap == null) { return; }

            if(event.world.isRemote) { return; }

            int dimensionId = event.world.provider.dimensionId;

            if(chunkRegenMap.containsKey(dimensionId)) {
                // Split up regen so it takes at most 16 millisec per frame to allow for ~55-60 FPS
                Queue<ChunkCoordIntPair> chunksToGen = chunkRegenMap.get(dimensionId);
                long startTime = System.nanoTime();
                while(System.nanoTime() - startTime < maximumDeltaTimeNanoSecs && !chunksToGen.isEmpty()) {
                    // Regenerate chunk
                    ChunkCoordIntPair nextChunk = chunksToGen.poll();
                    if(nextChunk == null) { break; }

                    Random fmlRandom = new Random(event.world.getSeed());
                    long xSeed = fmlRandom.nextLong() >> 2 + 1L;
                    long zSeed = fmlRandom.nextLong() >> 2 + 1L;
                    fmlRandom.setSeed((xSeed * nextChunk.chunkXPos + zSeed * nextChunk.chunkZPos) ^ event.world.getSeed());

                    BigReactors.worldGenerator.generateChunk(fmlRandom, nextChunk.chunkXPos, nextChunk.chunkZPos, event.world);
                }

                if(chunksToGen.isEmpty()) {
                    chunkRegenMap.remove(dimensionId);
                }
            }
        }
    }
}

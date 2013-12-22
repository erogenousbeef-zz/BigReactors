package erogenousbeef.bigreactors.common;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import cpw.mods.fml.common.IScheduledTickHandler;
import cpw.mods.fml.common.TickType;

public class BigReactorsTickHandler implements IScheduledTickHandler {

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
	
	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) {
		if(!type.contains(TickType.WORLD)) {
			return;
		}
		
		if(chunkRegenMap == null) { return; }
		
		World world = (World)tickData[0];
		
		if(world.isRemote) { return; }

		int dimensionId = world.provider.dimensionId;
		
		if(chunkRegenMap.containsKey(dimensionId)) {
			// Split up regen so it takes at most 16 millisec per frame to allow for ~55-60 FPS
			Queue<ChunkCoordIntPair> chunksToGen = chunkRegenMap.get(dimensionId);
			long startTime = System.nanoTime();
			while(System.nanoTime() - startTime < maximumDeltaTimeNanoSecs && !chunksToGen.isEmpty()) {
				// Regenerate chunk
				ChunkCoordIntPair nextChunk = chunksToGen.poll();
				if(nextChunk == null) { break; }

		        Random fmlRandom = new Random(world.getSeed());
		        long xSeed = fmlRandom.nextLong() >> 2 + 1L;
		        long zSeed = fmlRandom.nextLong() >> 2 + 1L;
		        fmlRandom.setSeed((xSeed * nextChunk.chunkXPos + zSeed * nextChunk.chunkZPos) ^ world.getSeed());
				
				BigReactors.worldGenerator.generateChunk(fmlRandom, nextChunk.chunkXPos, nextChunk.chunkZPos, world);
			}
			
			if(chunksToGen.isEmpty()) {
				chunkRegenMap.remove(dimensionId);
			}
		}
	}

	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) {
	}

	@Override
	public EnumSet<TickType> ticks() {
		return EnumSet.of(TickType.WORLD);
	}

	@Override
	public String getLabel() {
		return "BigReactors:TickHandler";
	}

	@Override
	public int nextTickSpacing() {
		return 1;
	}
}

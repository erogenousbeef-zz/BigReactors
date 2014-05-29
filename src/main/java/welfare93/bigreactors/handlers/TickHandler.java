package welfare93.bigreactors.handlers;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.ServerTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Type;
import cpw.mods.fml.common.gameevent.TickEvent.WorldTickEvent;
import cpw.mods.fml.relauncher.Side;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.core.multiblock.MultiblockRegistry;

public class TickHandler {

	public static TickHandler instance;
	public TickHandler()
	{
		instance=this;
	}
	@SubscribeEvent
	public void onWorldTick(WorldTickEvent event) {
		if(event.phase==Phase.START)
		{
			if(event.side==Side.SERVER || event.side==Side.CLIENT)
				MultiblockRegistry.tickStart(event.world);
		}
		else if(event.phase==Phase.END)
		{
			if(event.side==Side.SERVER || event.side==Side.CLIENT)
				{MultiblockRegistry.tickEnd(event.world);tickEnd(event.type, event.world);}
		}
		
	}
	@SubscribeEvent
	public void onClientTick(ClientTickEvent event) {

		if(event.phase==Phase.START)
		{
			if(event.side==Side.SERVER || event.side==Side.CLIENT)
				MultiblockRegistry.tickStart(Minecraft.getMinecraft().thePlayer.worldObj);
		}
		else if(event.phase==Phase.END)
		{
			if(event.side==Side.SERVER || event.side==Side.CLIENT)
				{MultiblockRegistry.tickEnd(Minecraft.getMinecraft().thePlayer.worldObj);}
		}
		
	}
	
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
	
	public void tickEnd(Type type,World world) {
		if(type!=Type.WORLD) {
			return;
		}
		
		if(chunkRegenMap == null) { return; }
		
		
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


}

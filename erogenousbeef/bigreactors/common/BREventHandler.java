package erogenousbeef.bigreactors.common;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.world.ChunkDataEvent;

public class BREventHandler {

	@ForgeSubscribe
	public void chunkSave(ChunkDataEvent.Save saveEvent) {
		if(BigReactors.enableWorldGen) {
			NBTTagCompound saveData = saveEvent.getData();
			
			saveData.setInteger("BigReactorsWorldGen", BRConfig.WORLDGEN_VERSION);
			saveData.setInteger("BigReactorsUserWorldGen", BigReactors.userWorldGenVersion);
		}
	}
	
	@ForgeSubscribe
	public void chunkLoad(ChunkDataEvent.Load loadEvent) {
		if(!BigReactors.enableWorldRegeneration || !BigReactors.enableWorldGen) {
			return;
		}

		NBTTagCompound loadData = loadEvent.getData();
		if(loadData.getInteger("BigReactorsWorldGen") == BRConfig.WORLDGEN_VERSION &&
				loadData.getInteger("BigReactorsUserWorldGen") == BigReactors.userWorldGenVersion) {
			return;
		}
		
		if(!BigReactors.enableWorldGenInNegativeDimensions && loadEvent.world.provider.dimensionId < 0) {
			return;
		}
		
		ChunkCoordIntPair coordPair = loadEvent.getChunk().getChunkCoordIntPair();
		BigReactors.tickHandler.addRegenChunk(loadEvent.world.provider.dimensionId, coordPair);
	}
	
}

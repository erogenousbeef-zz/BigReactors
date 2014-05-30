package erogenousbeef.bigreactors.common;

import welfare93.bigreactors.handlers.TickHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraftforge.event.world.ChunkDataEvent;

public class BREventHandler {

	@SubscribeEvent 
	public void chunkSave(ChunkDataEvent.Save saveEvent) {
		if(BigReactors.enableWorldGen) {
			NBTTagCompound saveData = saveEvent.getData();
			
			saveData.setInteger("BigReactorsWorldGen", BRConfig.WORLDGEN_VERSION);
			saveData.setInteger("BigReactorsUserWorldGen", BigReactors.userWorldGenVersion);
		}
	}
	
	@SubscribeEvent 
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
		TickHandler.instance.addRegenChunk(loadEvent.world.provider.dimensionId, coordPair);
	}
	
}

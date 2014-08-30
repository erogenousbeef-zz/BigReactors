package erogenousbeef.bigreactors.world;

import java.util.Random;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import cpw.mods.fml.common.IWorldGenerator;
import erogenousbeef.bigreactors.utils.StaticUtils;

public class BRWorldGenerator implements IWorldGenerator {

	protected static Set<BRSimpleOreGenerator> oreGenerators = null;
	
	public static void addGenerator(BRSimpleOreGenerator newGenerator) {
		if(oreGenerators == null) {
			oreGenerators = new CopyOnWriteArraySet<BRSimpleOreGenerator>();
		}
		oreGenerators.add(newGenerator);
	}
	
	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world,
			IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
		generateChunk(random, chunkX, chunkZ, world);
	}
	
	public void generateChunk(Random random, int chunkX, int chunkZ, World world) {
		if(oreGenerators == null) {
			return;
		}

		// Ignore negative dimension IDs if we're disallowing negative dimensions
		// OR if this dimensionID is not on the whitelist.
		if(!StaticUtils.WorldGen.shouldGenerateInDimension(world.provider.dimensionId)) {
			return;
		}

		for(BRSimpleOreGenerator generator : oreGenerators) {
			if(generator.shouldGenerateInWorld(world)) {
				generator.generateChunk(world, random, chunkX, chunkZ);
			}
		}
	}
}

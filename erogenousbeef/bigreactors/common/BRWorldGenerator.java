package erogenousbeef.bigreactors.common;

import java.util.Random;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import cpw.mods.fml.common.IWorldGenerator;

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
		if(oreGenerators == null) {
			return;
		}

		for(BRSimpleOreGenerator generator : oreGenerators) {
			if(generator.shouldGenerateInWorld(world, chunkProvider)) {
				generator.generate(world, random, chunkX, chunkZ);
			}
		}
	}
}

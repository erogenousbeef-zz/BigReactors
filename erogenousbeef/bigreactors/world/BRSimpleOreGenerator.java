package erogenousbeef.bigreactors.world;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.CopyOnWriteArraySet;

import erogenousbeef.core.common.CoordTriplet;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderEnd;
import net.minecraft.world.gen.ChunkProviderHell;
import net.minecraftforge.oredict.OreDictionary;

/**
 * This class handles simple ore generation.
 * @author Yoru
 *
 */
public class BRSimpleOreGenerator {

	protected int blockToGenerate;
	protected int blockMetadata;
	protected String strBlockToGenerate;
	
	protected int blockToReplace;
	
	protected int minClustersPerChunk;
	protected int maxClustersPerChunk;
	protected int minOrePerCluster;
	protected int maxOrePerCluster;
	protected int minY;
	protected int maxY;
	protected float oreGenChance;
	protected float oreSpreadChance;
	
	// TODO: Add booleans for "generate in nether" and "generate in end"
	// For now, we never generate in those dimensions.
	protected Set<Integer> dimensionBlacklist;
	
	public BRSimpleOreGenerator(int blockToGenerate, int blockMetadata, int blockToReplace, int clustersPerChunk, int maxY, int minOrePerCluster, float oreGenChance, float oreSpreadChance) {
		this.blockToGenerate = blockToGenerate;
		this.blockMetadata = blockMetadata;
		this.strBlockToGenerate = null;
		this.blockToReplace = blockToReplace;
		this.minClustersPerChunk = maxClustersPerChunk = clustersPerChunk;
		this.minOrePerCluster = minOrePerCluster;
		this.maxOrePerCluster = minOrePerCluster * 2;
		this.oreGenChance = oreGenChance;
		this.oreSpreadChance = oreSpreadChance;
		this.minY = 0;
		this.maxY = maxY;
		
		dimensionBlacklist = new CopyOnWriteArraySet<Integer>();
	}
	
	public BRSimpleOreGenerator(int blockToGenerate, int blockMetadata, int blockToReplace, int minClustersPerChunk, int maxClustersPerChunk, int minY, int maxY, int minOrePerCluster, float oreGenChance, float oreSpreadChance) {
		this(blockToGenerate, blockMetadata, blockToReplace, maxClustersPerChunk, maxY, minOrePerCluster, oreGenChance, oreSpreadChance);
		this.minClustersPerChunk = minClustersPerChunk;
		this.minY = minY;
	}
	
	public void blacklistDimension(int dimensionId) {
		dimensionBlacklist.add(dimensionId);
	}
	
	/**
	 * Call to generate in the given chunk in the given world.
	 * Performs no validation as to whether this generator should run (per user settings).
	 * @param world
	 * @param random
	 * @param chunkX
	 * @param chunkZ
	 */
	public void generate(World world, Random random, int chunkX, int chunkZ) {
		int clustersToGen = minClustersPerChunk;
		if(maxClustersPerChunk > minClustersPerChunk) {
			clustersToGen += random.nextInt(maxClustersPerChunk - minClustersPerChunk);
		}

		if(this.blockToGenerate == -2) {
			// Invalid generator. Do nothing.
			return;
		}

		if(this.blockToGenerate == -1) {
			ArrayList<ItemStack> ores = OreDictionary.getOres(this.strBlockToGenerate);
			if(ores == null || ores.size() < 1) {
				// Invalid!
				this.blockToGenerate = -2;
				return;
			}
			
			// Else valid
			ItemStack ore = ores.get(0);
			this.blockToGenerate = ore.itemID;
			this.blockMetadata = ore.getItemDamage();
		}
		
		int chunkBaseX = chunkX << 4;
		int chunkBaseZ = chunkZ << 4;
		
		for(int i = 0; i < clustersToGen; i++) {
			int x,y,z;
			x = chunkBaseX + random.nextInt(16);
			z = chunkBaseZ + random.nextInt(16);
			y = this.minY + random.nextInt(this.maxY - this.minY);
			
			generateAtLocation(world, random, x, y, z, chunkBaseX, chunkBaseZ);
		}
	}
	
	// helper
	protected int generateAtLocation(World world, Random random, int x, int y, int z, int chunkX, int chunkZ) {
		if(world.getBlockId(x, y, z) != this.blockToReplace) { return 0; }
		
		int oresGenerated = 0;
		Stack<CoordTriplet> genStack = new Stack<CoordTriplet>();
		genStack.add(new CoordTriplet(x, y, z));
		
		float currentOreGenChance = this.oreGenChance;
		CoordTriplet coord;
		
		while(oresGenerated < this.maxOrePerCluster && !genStack.isEmpty()) {
			coord = genStack.pop();
			if(coord == null) { break; } // weird.
			if(oresGenerated > this.minOrePerCluster && random.nextFloat() <= currentOreGenChance) { continue; }
			
			if(oresGenerated > this.minOrePerCluster) {
				currentOreGenChance *= this.oreSpreadChance;
			}

			world.setBlock(coord.x, coord.y, coord.z, this.blockToGenerate, this.blockMetadata, 2);
			oresGenerated++;
			
			// Add nearby blocks, in random order to be fair
			int[] dirs = { 0, 1, 2, 3, 4, 5 };
			shuffleArray(dirs, random);
			for(int dir : dirs) {
				switch(dir) {
				case 0:
					if(coord.x-1 >= chunkX && world.getBlockId(coord.x-1, coord.y, coord.z) == this.blockToReplace) {
						genStack.add(new CoordTriplet(coord.x-1, coord.y, coord.z));
					}
					break;
				case 1:
					if(coord.x+1 < chunkX + 16 && world.getBlockId(coord.x+1, coord.y, coord.z) == this.blockToReplace) {
						genStack.add(new CoordTriplet(coord.x+1, coord.y, coord.z));
					}
					break;
				case 2:
					if(coord.y-1 >= this.minY && world.getBlockId(coord.x, coord.y-1, coord.z) == this.blockToReplace) {
						genStack.add(new CoordTriplet(coord.x, coord.y-1, coord.z));
					}
					break;
				case 3:
					if(coord.y+1 <= this.maxY && world.getBlockId(coord.x, coord.y+1, coord.z) == this.blockToReplace) {
						genStack.add(new CoordTriplet(coord.x, coord.y+1, coord.z));
					}
					break;
				case 4:
					if(coord.z-1 >= chunkZ && world.getBlockId(coord.x, coord.y, coord.z-1) == this.blockToReplace) {
						genStack.add(new CoordTriplet(coord.x, coord.y, coord.z-1));
					}
					break;
				default:
					if(coord.z+1 < chunkZ + 16 && world.getBlockId(coord.x, coord.y, coord.z+1) == this.blockToReplace) {
						genStack.add(new CoordTriplet(coord.x, coord.y, coord.z+1));
					}
					break;
				}
			}
		}
		
		return oresGenerated;
	}
	
	// Simple Fisher-Yates shuffle.
	private void shuffleArray(int[] dirs, Random random) {
		for(int i = dirs.length - 1; i >= 0; i--) {
			int idx = random.nextInt(i+1);
			int tmp = dirs[idx];
			dirs[idx] = dirs[i];
			dirs[i] = tmp;
		}
	}

	/**
	 * Call to discover if this generator WISHES to generate in this world.
	 * @param world
	 * @param chunkProvider
	 * @return
	 */
	public boolean shouldGenerateInWorld(World world) {
		IChunkProvider chunkProvider = world.getChunkProvider();
		if(dimensionBlacklist.contains(world.provider.dimensionId)) {
			return false;
		}
		else if(chunkProvider instanceof ChunkProviderHell) {
			return false;
		}
		else if(chunkProvider instanceof ChunkProviderEnd) {
			return false;
		}

		return true;
	}

	@Override
	public boolean equals(Object o) {
		if(o instanceof BRSimpleOreGenerator) {
			BRSimpleOreGenerator other = (BRSimpleOreGenerator)o;
			if(this.blockToGenerate >= 0) {
				return this.blockToGenerate == other.blockToGenerate && this.blockMetadata == other.blockMetadata;
			}
			else {
				if(other.blockToGenerate >= 0) {
					return false;
				}
				else {
					return this.strBlockToGenerate.equals(other.strBlockToGenerate);
				}
			}
		}
		else {
			return false;
		}
	}
}

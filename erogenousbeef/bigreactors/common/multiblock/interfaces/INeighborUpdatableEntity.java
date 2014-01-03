package erogenousbeef.bigreactors.common.multiblock.interfaces;

import net.minecraft.world.World;

public interface INeighborUpdatableEntity {
	
	/**
	 * Called from a Block class's onNeighborBlockChange
	 * @param world The world containing the tileentity
	 * @param x Tile Entity's xcoord
	 * @param y Tile Entity's ycoord
	 * @param z Tile Entity's zcoord
	 * @param neighborBlockID Block ID of the block that changed
	 */
	public void onNeighborBlockChange(World world, int x, int y, int z, int neighborBlockID);

}

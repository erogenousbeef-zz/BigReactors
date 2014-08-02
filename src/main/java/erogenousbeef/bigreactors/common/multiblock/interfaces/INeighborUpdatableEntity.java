package erogenousbeef.bigreactors.common.multiblock.interfaces;

import net.minecraft.block.Block;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public interface INeighborUpdatableEntity {
	
	/**
	 * Called from a Block class's onNeighborBlockChange
	 * @param world The world containing the tileentity
	 * @param x Tile Entity's xcoord
	 * @param y Tile Entity's ycoord
	 * @param z Tile Entity's zcoord
	 * @param neighborBlock Block that changed
	 */
	public void onNeighborBlockChange(World world, int x, int y, int z, Block neighborBlock);
	
	/**
	 * Called from a Block class's onNeighborTileChange
	 * @param world The world containing the TileEntity
	 * @param x Tile entity's Xcoord
	 * @param y Tile entity's Ycoord
	 * @param z Tile entity's Zcoord
	 * @param neighborX Changed neighbor's Xcoord
	 * @param neighborY Changed neighbor's Ycoord
	 * @param neighborZ Changed neighbor's Zcoord
	 */
	public void onNeighborTileChange(IBlockAccess world, int x, int y, int z, int neighborX, int neighborY, int neighborZ);

}

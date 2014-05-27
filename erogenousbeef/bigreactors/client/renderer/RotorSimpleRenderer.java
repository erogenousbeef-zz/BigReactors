package erogenousbeef.bigreactors.client.renderer;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.multiblock.block.BlockTurbineRotorPart;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityTurbineRotorPart;
import erogenousbeef.bigreactors.utils.StaticUtils;

public class RotorSimpleRenderer implements ISimpleBlockRenderingHandler {

	protected static final double rotorSize = 0.2D;
	
	public RotorSimpleRenderer() {
	}

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelID,
			RenderBlocks renderer) {
		if(metadata == BlockTurbineRotorPart.METADATA_SHAFT) {
			
			Tessellator tessellator = Tessellator.instance;
			
	        GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
	        tessellator.startDrawingQuads();
	        tessellator.setNormal(0.0F, -1.0F, 0.0F);
			
			renderRotorShaft(block, renderer, metadata, ForgeDirection.UP, new boolean[] { true, true, true, true }, 0, 0, 0, true);
			
	        tessellator.draw();
	        GL11.glTranslatef(0.5F, 0.5F, 0.5F);
		}
		else {
			Tessellator tessellator = Tessellator.instance;
			
	        GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
	        tessellator.startDrawingQuads();
	        tessellator.setNormal(0.0F, -1.0F, 0.0F);
			renderBlade(renderer, 0, 0, 0, block, metadata, ForgeDirection.UP);
	        tessellator.draw();
	        GL11.glTranslatef(0.5F, 0.5F, 0.5F);
		}
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z,
			Block block, int modelId, RenderBlocks renderer) {
		int metadata = world.getBlockMetadata(x, y, z);
		TileEntity te = world.getTileEntity(x, y, z);
		
		if(te instanceof TileEntityTurbineRotorPart) {
			TileEntityTurbineRotorPart rotorPart = (TileEntityTurbineRotorPart)te;
			if(rotorPart.isConnected() && rotorPart.getTurbine().isAssembled() && rotorPart.getTurbine().isActive()) {
				// Don't draw if the turbine's active.
				return false;
			}
		}
		
		if(BlockTurbineRotorPart.isRotorShaft(metadata)) {
			ForgeDirection majorAxis = findRotorMajorAxis(world, x, y, z, block);
			
			boolean[] hasBlades = findBlades(world, x, y, z, block, majorAxis);
			
			renderRotorShaft(block, renderer, metadata, majorAxis, hasBlades, x, y, z, false);
		}
		else {
			renderBladeFromWorld(renderer, world, x, y, z, block, metadata);
		}
		return true;
	}

	@Override
	public boolean shouldRender3DInInventory(int var1) {
		return true;
	}

	@Override
	public int getRenderId() {
		return BlockTurbineRotorPart.renderId;
	}

	public static void renderRotorShaft(Block block, RenderBlocks renderer, int metadata, ForgeDirection majorAxis, boolean[] hasBlades, int x, int y, int z, boolean drawOuterRectangle) {
		double xMin, yMin, zMin;
		double xMax, yMax, zMax;
		xMin = yMin = zMin = 0.5D - rotorSize;
		xMax = yMax = zMax = 0.5D + rotorSize;
		
		if(majorAxis.offsetX != 0) {
			xMax = 1D;
			xMin = 0D;
		}
		if(majorAxis.offsetY != 0) {
			yMax = 1D;
			yMin = 0D;
		}
		if(majorAxis.offsetZ != 0) {
			zMax = 1D;
			zMin = 0D;
		}

        Tessellator.instance.setColorRGBA(255, 255, 255, 255);
        renderer.setRenderBoundsFromBlock(block);
        renderer.setOverrideBlockTexture(null);
		
        renderer.setRenderBounds(xMin, yMin, zMin, xMax, yMax, zMax);
        renderer.renderFaceYNeg(block, x, y, z, renderer.getBlockIconFromSideAndMetadata(block, 0, metadata));
        renderer.setRenderBounds(xMin, yMin, zMin, xMax, yMax, zMax);
        renderer.renderFaceYPos(block, x, y, z, renderer.getBlockIconFromSideAndMetadata(block, 1, metadata));
        renderer.setRenderBounds(xMin, yMin, zMin, xMax, yMax, zMax);
        renderer.renderFaceZNeg(block, x, y, z, renderer.getBlockIconFromSideAndMetadata(block, 2, metadata));
        renderer.setRenderBounds(xMin, yMin, zMin, xMax, yMax, zMax);
        renderer.renderFaceZPos(block, x, y, z, renderer.getBlockIconFromSideAndMetadata(block, 3, metadata));
        renderer.setRenderBounds(xMin, yMin, zMin, xMax, yMax, zMax);
        renderer.renderFaceXNeg(block, x, y, z, renderer.getBlockIconFromSideAndMetadata(block, 4, metadata));
        renderer.setRenderBounds(xMin, yMin, zMin, xMax, yMax, zMax);
        renderer.renderFaceXPos(block, x, y, z, renderer.getBlockIconFromSideAndMetadata(block, 5, metadata));
        renderer.setRenderBounds(0D, 0D, 0D, 1D, 1D, 1D);
        
        // Render blade surfaces, if present
        ForgeDirection[] bladeDirs = StaticUtils.neighborsBySide[majorAxis.ordinal()];
        for(int i = 0; i < bladeDirs.length; i++) {
        	if(hasBlades[i]) {
        		renderRotorBladeConnection(renderer, block, metadata, majorAxis, bladeDirs[i], x, y, z, drawOuterRectangle);
        	}
        }

        renderer.setRenderBounds(0D, 0D, 0D, 1D, 1D, 1D);
	}

	private void renderBladeFromWorld(RenderBlocks renderer, IBlockAccess world, int x, int y, int z, Block block, int metadata) {
		TileEntity te = world.getTileEntity(x, y, z);
		ForgeDirection rotorDir = ForgeDirection.UNKNOWN;
		if(te instanceof TileEntityTurbineRotorPart) {
			TileEntityTurbineRotorPart rotorPart = (TileEntityTurbineRotorPart)te;
			if(rotorPart.isConnected()) {
				rotorDir = rotorPart.getTurbine().getRotorDirection();
			}
		}
		
		if(rotorDir == ForgeDirection.UNKNOWN) {
			// Go walkies!
			ArrayList<ForgeDirection> bladeDirs = new ArrayList<ForgeDirection>();

			// First check, surrounding area.
			ForgeDirection[] dirsToCheck = ForgeDirection.VALID_DIRECTIONS;
			for(ForgeDirection dir : dirsToCheck) {
				Block neighborBlock = world.getBlock(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ);
				if(neighborBlock == block) {
					// Blade or rotor?!
					int neighborMetadata = world.getBlockMetadata(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ);
					if(BlockTurbineRotorPart.isRotorShaft(neighborMetadata)) {
						// SEXY TIMES
						rotorDir = findRotorMajorAxis(world, x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ, block);
						break;
					}
					else if(BlockTurbineRotorPart.isRotorBlade(neighborMetadata)) {
						// We'll move in that direction then...
						bladeDirs.add(dir);
					}
				}
			}
			
			// Still no luck eh?
			while(rotorDir == ForgeDirection.UNKNOWN && !bladeDirs.isEmpty()) {
				ForgeDirection dir = bladeDirs.remove(bladeDirs.size() - 1); // Trim off the end to avoid shifting crap in memory
				int curX = x + dir.offsetX;
				int curY = y + dir.offsetY;
				int curZ = z + dir.offsetZ;
				
				int dist = 0;
				while(world.getBlock(curX, curY, curZ) == block && dist < 32) { // only go up to 32 blocks in any direction without finding a rotor, for sanity
					int curMeta = world.getBlockMetadata(curX, curY, curZ);
					if(BlockTurbineRotorPart.isRotorShaft(curMeta)) {
						// Huz ZAH!
						rotorDir = findRotorMajorAxis(world, curX, curY, curZ, block);
						break;
					}
					else if(BlockTurbineRotorPart.isRotorBlade(curMeta)) {
						curX += dir.offsetX;
						curY += dir.offsetY;
						curZ += dir.offsetZ;
						dist++;
					}
					else {
						break;
					}
				}
			}
		}

		renderBlade(renderer, x, y, z, block, metadata, rotorDir);
	}
	
	public static void renderBlade(RenderBlocks renderer, int x, int y, int z, Block block, int metadata, ForgeDirection rotorDir) {
		if(rotorDir == ForgeDirection.UNKNOWN) {
			rotorDir = ForgeDirection.UP;
		}
		
		double xMin, yMin, zMin, xMax, yMax, zMax;
		xMin = yMin = zMin = 0D;
		xMax = yMax = zMax = 1D;
		
		if(rotorDir.offsetX != 0) {
			xMin = 0.45D;
			xMax = 0.55D;
		}
		else if(rotorDir.offsetY != 0) {
			yMin = 0.45D;
			yMax = 0.55D;
		}
		else if(rotorDir.offsetZ != 0) {
			zMin = 0.45D;
			zMax = 0.55D;
		}
		
        Tessellator.instance.setColorRGBA(255, 255, 255, 255);
        renderer.setRenderBoundsFromBlock(block);
        renderer.setOverrideBlockTexture(null);
		
        renderer.setRenderBounds(xMin, yMin, zMin, xMax, yMax, zMax);
        renderer.renderFaceYNeg(block, x, y, z, renderer.getBlockIconFromSideAndMetadata(block, 0, metadata));
        renderer.setRenderBounds(xMin, yMin, zMin, xMax, yMax, zMax);
        renderer.renderFaceYPos(block, x, y, z, renderer.getBlockIconFromSideAndMetadata(block, 1, metadata));
        renderer.setRenderBounds(xMin, yMin, zMin, xMax, yMax, zMax);
        renderer.renderFaceZNeg(block, x, y, z, renderer.getBlockIconFromSideAndMetadata(block, 2, metadata));
        renderer.setRenderBounds(xMin, yMin, zMin, xMax, yMax, zMax);
        renderer.renderFaceZPos(block, x, y, z, renderer.getBlockIconFromSideAndMetadata(block, 3, metadata));
        renderer.setRenderBounds(xMin, yMin, zMin, xMax, yMax, zMax);
        renderer.renderFaceXNeg(block, x, y, z, renderer.getBlockIconFromSideAndMetadata(block, 4, metadata));
        renderer.setRenderBounds(xMin, yMin, zMin, xMax, yMax, zMax);
        renderer.renderFaceXPos(block, x, y, z, renderer.getBlockIconFromSideAndMetadata(block, 5, metadata));
        renderer.setRenderBounds(0D, 0D, 0D, 1D, 1D, 1D);
	}
	
	/**
	 * @param world
	 * @param x
	 * @param y
	 * @param z
	 * @param block
	 * @param metadata
	 * @return The major axis of the rotor. This is always one of the positive directions.
	 */
	private static ForgeDirection findRotorMajorAxis(IBlockAccess world, int x, int y, int z, Block block) {
		ForgeDirection retDir = ForgeDirection.UP;
		
		for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			if(world.getBlock(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ) == block &&
					BlockTurbineRotorPart.isRotorShaft(world.getBlockMetadata(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ))) {
				retDir = dir;
				break;
			}
		}
		
		if(retDir == ForgeDirection.DOWN || retDir == ForgeDirection.NORTH || retDir == ForgeDirection.WEST) {
			retDir = retDir.getOpposite();
		}
		
		return retDir; // Defaults to up if there's no neighbors of the same type
	}
	
	private static boolean[] findBlades(IBlockAccess world, int x, int y, int z, Block block, ForgeDirection majorAxis) {
		boolean[] ret = new boolean[4];
		ForgeDirection[] dirsToCheck = StaticUtils.neighborsBySide[majorAxis.ordinal()];
		
		for(int i = 0; i < dirsToCheck.length; i++) {
			ForgeDirection dir = dirsToCheck[i];
			if(world.getBlock(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ) == block &&
					BlockTurbineRotorPart.isRotorBlade(world.getBlockMetadata(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ))) {
				ret[i] = true;
			}
			else {
				ret[i] = false;
			}
		}
		
		return ret;
	}
	
	private static ForgeDirection[][] normals = {
		{ForgeDirection.UNKNOWN, ForgeDirection.UNKNOWN, ForgeDirection.WEST, ForgeDirection.EAST, ForgeDirection.NORTH, ForgeDirection.SOUTH}, // DOWN
		{ForgeDirection.UNKNOWN, ForgeDirection.UNKNOWN, ForgeDirection.WEST, ForgeDirection.EAST, ForgeDirection.NORTH, ForgeDirection.SOUTH}, // UP
		{ForgeDirection.EAST, ForgeDirection.WEST, ForgeDirection.UNKNOWN, ForgeDirection.UNKNOWN, ForgeDirection.DOWN, ForgeDirection.UP}, // NORTH
		{ForgeDirection.EAST, ForgeDirection.WEST, ForgeDirection.UNKNOWN, ForgeDirection.UNKNOWN, ForgeDirection.DOWN, ForgeDirection.UP}, // SOUTH
		{ForgeDirection.SOUTH, ForgeDirection.NORTH, ForgeDirection.DOWN, ForgeDirection.UP, ForgeDirection.UNKNOWN, ForgeDirection.UNKNOWN}, // WEST
		{ForgeDirection.SOUTH, ForgeDirection.NORTH, ForgeDirection.DOWN, ForgeDirection.UP, ForgeDirection.UNKNOWN, ForgeDirection.UNKNOWN}, // EAST
	};
	
	private static ForgeDirection findNormal(ForgeDirection majorAxis, ForgeDirection minorAxis) {
		return normals[majorAxis.ordinal()][minorAxis.ordinal()];
	}
	
	private static void renderRotorBladeConnection(RenderBlocks renderer, Block block, int metadata,
			ForgeDirection rotorDir, ForgeDirection bladeDir, int x, int y, int z, boolean drawOuterRectangle) {

		// This is the dimension in which the blade expands
		ForgeDirection normal = findNormal(rotorDir, bladeDir);
		
		// Used for proper calculation of the IJK coords
		int rotorDirMultiplier = rotorDir.offsetX < 0 || rotorDir.offsetY < 0 || rotorDir.offsetZ < 0 ? -1 : 1;
		int bladeDirMultiplier = bladeDir.offsetX < 0 || bladeDir.offsetY < 0 || bladeDir.offsetZ < 0 ? -1 : 1;
		int normalDirMultiplier = normal.offsetX < 0 || normal.offsetY < 0 || normal.offsetZ < 0 ? -1 : 1;

		// Compute the 8 coordinates of the inner and outer rectangles in IJK space, which we'll re-orient later
		// I = blade dir, J = rotor dir, K = normal dir
		double rotorDirectionOffset = 0.05D;
		double bladeInnerOffset = 0.2D;
		double bladeOuterOffset = 0.5D;
		double normalInnerOffset = 0.2D;
		double normalOuterOffset = 0.4D;
		
		double rotorOffsets[] = new double[8];
		rotorOffsets[0] = rotorOffsets[3] = rotorOffsets[4] = rotorOffsets[7] = 0.5D + (rotorDirMultiplier * rotorDirectionOffset);
		rotorOffsets[1] = rotorOffsets[2] = rotorOffsets[5] = rotorOffsets[6] = 0.5D - (rotorDirMultiplier * rotorDirectionOffset);
		
		double bladeOffsets[] = new double[8];
		bladeOffsets[0] = bladeOffsets[1] = bladeOffsets[2] = bladeOffsets[3] = 0.5D + (bladeDirMultiplier * bladeInnerOffset);
		bladeOffsets[4] = bladeOffsets[5] = bladeOffsets[6] = bladeOffsets[7] = 0.5D + (bladeDirMultiplier * bladeOuterOffset);

		double normalOffsets[] = new double[8];
		normalOffsets[0] = normalOffsets[1] = 0.5D - (normalDirMultiplier * normalInnerOffset); 
		normalOffsets[2] = normalOffsets[3] = 0.5D + (normalDirMultiplier * normalInnerOffset); 
		normalOffsets[4] = normalOffsets[5] = 0.5D - (normalDirMultiplier * normalOuterOffset); 
		normalOffsets[6] = normalOffsets[7] = 0.5D + (normalDirMultiplier * normalOuterOffset);
		
		// Now calculate our 8 coordinates in XYZ space from IJK space
		double[] xCoords = {0D, 0D, 0D, 0D, 0D, 0D, 0D, 0D};
		double[] yCoords = {0D, 0D, 0D, 0D, 0D, 0D, 0D, 0D};
		double[] zCoords = {0D, 0D, 0D, 0D, 0D, 0D, 0D, 0D};

		int xMagRotor = Math.abs(rotorDir.offsetX);
		int xMagBlade = Math.abs(bladeDir.offsetX);
		int xMagNormal = Math.abs(normal.offsetX);
		int yMagRotor = Math.abs(rotorDir.offsetY);
		int yMagBlade = Math.abs(bladeDir.offsetY);
		int yMagNormal = Math.abs(normal.offsetY);
		int zMagRotor = Math.abs(rotorDir.offsetZ);
		int zMagBlade = Math.abs(bladeDir.offsetZ);
		int zMagNormal = Math.abs(normal.offsetZ);
		
		for(int i = 0; i < 8; i++) {
			xCoords[i] = rotorOffsets[i] * xMagRotor + bladeOffsets[i] * xMagBlade + normalOffsets[i] * xMagNormal;
			yCoords[i] = rotorOffsets[i] * yMagRotor + bladeOffsets[i] * yMagBlade + normalOffsets[i] * yMagNormal;
			zCoords[i] = rotorOffsets[i] * zMagRotor + bladeOffsets[i] * zMagBlade + normalOffsets[i] * zMagNormal;
		}

		// Calculate UV coords for each face.
		double[] u = {0D, 0D, 16D, 16D};
		double[] v = {0D, 16D, 16D, 0D};

		IIcon icon = BigReactors.blockTurbineRotorPart.getRotorConnectorIcon();
		for(int i = 0; i < 4; i++) {
			u[i] = icon.getInterpolatedU(u[i]);
			v[i] = icon.getInterpolatedV(v[i]);
		}
		
		// Element buffer, which of these do we draw?
		int[][] quads;
		if(rotorDir.offsetX != 0 || (bladeDir.offsetX != 0 && rotorDir.offsetY != 0)) {
			quads = quadSet2;
		}
		else {
			quads = quadSet1;
		}

		Tessellator tessellator = Tessellator.instance;
		tessellator.addTranslation(x, y, z);
		
		for(int face = drawOuterRectangle ? 0 : 1; face < quads.length; face++) {
			for(int vertex = 0; vertex < quads[face].length; vertex++) {
				int idx = quads[face][vertex];
				tessellator.addVertexWithUV(xCoords[idx], yCoords[idx], zCoords[idx], u[vertex], v[vertex]);
			}
		}
		
		tessellator.addTranslation(-x, -y, -z);
		renderer.setRenderBounds(0D, 0D, 0D, 1D, 1D, 1D);
	}
	
	private static final int[][] quadSet1 = {
			{4, 5, 6, 7}, // Outer rectangular face of the rotor
			{7, 3, 0, 4}, // "top" rhombus
			{6, 5, 1, 2}, // "bottom" rhombus
			{0, 1, 5, 4}, // "left" irregular rectangle
			{7, 6, 2, 3}, // "right irregular rectangle
	};
	
	private static final int[][] quadSet2 = {
			{7, 6, 5, 4}, // Outer rectangular face of the rotor
			{4, 0, 3, 7}, // "top" rhombus
			{2, 1, 5, 6}, // "bottom" rhombus
			{4, 5, 1, 0}, // "left" irregular rectangle
			{3, 2, 6, 7}, // "right irregular rectangle
	};
}

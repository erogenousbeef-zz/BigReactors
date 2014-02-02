package erogenousbeef.bigreactors.client.renderer;

import org.lwjgl.opengl.GL11;

import net.minecraft.block.Block;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.ForgeDirection;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import erogenousbeef.bigreactors.common.multiblock.block.BlockTurbineRotorPart;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityTurbineRotorPart;
import erogenousbeef.bigreactors.utils.StaticUtils;

public class RotorSimpleRenderer implements ISimpleBlockRenderingHandler {

	protected double rotorSize = 0.2D;
	
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
			
			renderRotorShaft(block, renderer, metadata, ForgeDirection.UP, new boolean[] { true, true, true, true }, 0, 0, 0);
			
	        tessellator.draw();
	        GL11.glTranslatef(0.5F, 0.5F, 0.5F);
		}
		else {
			Tessellator tessellator = Tessellator.instance;
			
	        GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
	        tessellator.startDrawingQuads();
	        tessellator.setNormal(0.0F, -1.0F, 0.0F);
			_renderBlade(renderer, 0, 0, 0, block, metadata, ForgeDirection.UP);

			// TODO - DEBUG REMOVEME
			//renderRotorBladeConnection(renderer, block, metadata, ForgeDirection.UP, ForgeDirection.EAST, 0, 0, 0);
	        tessellator.draw();
	        GL11.glTranslatef(0.5F, 0.5F, 0.5F);
		}
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z,
			Block block, int modelId, RenderBlocks renderer) {
		int metadata = world.getBlockMetadata(x, y, z);
		if(BlockTurbineRotorPart.isRotorShaft(metadata)) {
			ForgeDirection majorAxis = findRotorMajorAxis(world, x, y, z, block, metadata);
			
			boolean[] hasBlades = findBlades(world, x, y, z, block, majorAxis);
			
			renderRotorShaft(block, renderer, metadata, majorAxis, hasBlades, x, y, z);
		}
		else {
			renderBlade(renderer, world, x, y, z, block, metadata);
		}
		return true;
	}

	@Override
	public boolean shouldRender3DInInventory() {
		return true;
	}

	@Override
	public int getRenderId() {
		return BlockTurbineRotorPart.renderId;
	}

	private void renderRotorShaft(Block block, RenderBlocks renderer, int metadata, ForgeDirection majorAxis, boolean[] hasBlades, int x, int y, int z) {
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
        
        // TODO: Render blade surfaces if present
        /*
        ForgeDirection[] bladeDirs = StaticUtils.neighborsBySide[majorAxis.ordinal()];
        for(int i = 0; i < hasBlades.length; i++) {
        	if(hasBlades[i]) {
        		renderRotorBladeConnection(renderer, block, metadata, majorAxis, bladeDirs[i], x, y, z);
        	}
        }
        */
        //renderRotorBladeConnection(renderer, block, metadata, majorAxis, ForgeDirection.EAST, x, y, z);
        //renderer.setRenderBounds(0D, 0D, 0D, 1D, 1D, 1D);
	}

	private void renderBlade(RenderBlocks renderer, IBlockAccess world, int x, int y, int z, Block block, int metadata) {
		TileEntity te = world.getBlockTileEntity(x, y, z);
		ForgeDirection rotorDir = ForgeDirection.UNKNOWN;
		if(te instanceof TileEntityTurbineRotorPart) {
			TileEntityTurbineRotorPart rotorPart = (TileEntityTurbineRotorPart)te;
			if(rotorPart.isConnected()) {
				rotorDir = rotorPart.getTurbine().getRotorDirection();
			}
		}
		
		_renderBlade(renderer, x, y, z, block, metadata, rotorDir);
	}
	
	private void _renderBlade(RenderBlocks renderer, int x, int y, int z, Block block, int metadata, ForgeDirection rotorDir) {
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
	private ForgeDirection findRotorMajorAxis(IBlockAccess world, int x, int y, int z, Block block, int metadata) {
		ForgeDirection retDir = ForgeDirection.UP;
		
		for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			if(world.getBlockId(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ) == block.blockID &&
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
	
	private boolean[] findBlades(IBlockAccess world, int x, int y, int z, Block block, ForgeDirection majorAxis) {
		boolean[] ret = new boolean[4];
		ForgeDirection[] dirsToCheck = StaticUtils.neighborsBySide[majorAxis.ordinal()];
		
		for(int i = 0; i < dirsToCheck.length; i++) {
			ForgeDirection dir = dirsToCheck[i];
			if(world.getBlockId(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ) == block.blockID &&
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
	
	private ForgeDirection findNormal(ForgeDirection majorAxis, ForgeDirection minorAxis) {
		return normals[majorAxis.ordinal()][minorAxis.ordinal()];
	}
	
	private void renderRotorBladeConnection(RenderBlocks renderer, Block block, int metadata,
			ForgeDirection majorAxis, ForgeDirection forgeDirection, int x, int y, int z) {

		// This is the dimension in which the blade expands
		ForgeDirection normal = findNormal(majorAxis, forgeDirection);

		double[] expandingCoords = {0.1D, 0.3D, 0.7D, 0.9D};
		double[] majorAxisCoords = {0.3D, 0.3D, 0.7D, 0.7D};
		double[] minorAxisCoords = {1.0D, 1.0D, 0.7D, 0.7D};
		
		switch(forgeDirection) {
		case EAST:
			break;
		case WEST:
			break;
		case SOUTH:
			break;
		case NORTH:
			break;
		case DOWN:
			break;
		case UP:
		default:
			break;
		}

		// TODO: Rotate so that this is equivalent to rendering a blade pointing east (x-pos)
		
		Tessellator tessellator = Tessellator.instance;
		
		tessellator.addTranslation(x, y, z);
		
		renderBladeYNeg(0, 0, 0, block.getIcon(0, metadata));
		renderBladeYPos(block.getIcon(1, metadata));
		renderBladeZPos(block.getIcon(2, metadata));
		renderBladeZNeg(block.getIcon(3, metadata));

		renderer.setRenderBounds(1D, 0.3D, 0.1D, 1D, 0.7D, 0.9D);
		renderer.renderFaceXPos(block, 0, 0, 0, block.getIcon(5, metadata));
		
		tessellator.addTranslation(-x, -y, -z);
		renderer.setRenderBounds(0D, 0D, 0D, 1D, 1D, 1D);
	}

	private void renderBladeYNeg(int x, int y, int z, Icon icon) {
		Tessellator tessellator = Tessellator.instance;
		
		double zMin1, zMin2, zMax1, zMax2;
		
		zMin1 = 0.6D;
		zMin2 = 0.4D;
		zMax1 = 0D;
		zMax2 = 1D;
        double u1 = (double)icon.getInterpolatedU(zMin1 * 16.0D);
        double u2 = (double)icon.getInterpolatedU(zMin2 * 16.0D);
        double u3 = (double)icon.getInterpolatedU(zMax1 * 16.0D);
        double u4 = (double)icon.getInterpolatedU(zMax2 * 16.0D);
        double v1 = (double)icon.getInterpolatedV(16.0D - 0.7D * 16.0D);
        double v2 = (double)icon.getInterpolatedV(0D);
		
		tessellator.setNormal(0f, -1f, 0f);
		tessellator.addVertexWithUV(1D, 0.3D, 0.1D, u1, v1);
		tessellator.addVertexWithUV(1D, 0.3D, 0.9D, u2, v1);
		tessellator.addVertexWithUV(0.7D, 0.3D, 0.7D, u4, v2);
		tessellator.addVertexWithUV(0.7D, 0.3D, 0.3D, u3, v2);
	}

	private void renderBladeYPos(Icon icon) {
		Tessellator tessellator = Tessellator.instance;
		
		double zMin1, zMin2, zMax1, zMax2;
		
		zMin1 = 0.6D;
		zMin2 = 0.4D;
		zMax1 = 0D;
		zMax2 = 1D;
        double u1 = (double)icon.getInterpolatedU(zMin1 * 16.0D);
        double u2 = (double)icon.getInterpolatedU(zMin2 * 16.0D);
        double u3 = (double)icon.getInterpolatedU(zMax1 * 16.0D);
        double u4 = (double)icon.getInterpolatedU(zMax2 * 16.0D);
        double v1 = (double)icon.getInterpolatedV(16.0D - 0.7D * 16.0D);
        double v2 = (double)icon.getInterpolatedV(0D);
		
		tessellator.setNormal(0f, -1f, 0f);
		tessellator.addVertexWithUV(0.7D, 0.7D, 0.3D, u1, v1);
		tessellator.addVertexWithUV(0.7D, 0.7D, 0.7D, u2, v1);
		tessellator.addVertexWithUV(1.0D, 0.7D, 0.9D, u4, v2);
		tessellator.addVertexWithUV(1.0D, 0.7D, 0.1D, u3, v2);
	}

	private void renderBladeZPos(Icon icon) {
		Tessellator tessellator = Tessellator.instance;
		double zMin1, zMin2, zMax1, zMax2;
		
		zMin1 = 0.6D;
		zMin2 = 0.4D;
		zMax1 = 0D;
		zMax2 = 1D;
        double u1 = (double)icon.getInterpolatedU(zMin1 * 16.0D);
        double u2 = (double)icon.getInterpolatedU(zMin2 * 16.0D);
        double u3 = (double)icon.getInterpolatedU(zMax1 * 16.0D);
        double u4 = (double)icon.getInterpolatedU(zMax2 * 16.0D);
        double v1 = (double)icon.getInterpolatedV(16.0D - 0.7D * 16.0D);
        double v2 = (double)icon.getInterpolatedV(0D);
		
		tessellator.setNormal(0f, 0f, 1f);
		tessellator.addVertexWithUV(0.7D, 0.7D, 0.7D, u1, v1);
		tessellator.addVertexWithUV(0.7D, 0.3D, 0.7D, u2, v1);
		tessellator.addVertexWithUV(1.0D, 0.3D, 0.9D, u4, v2);
		tessellator.addVertexWithUV(1.0D, 0.7D, 0.9D, u3, v2);
	}
	
	private void renderBladeZNeg(Icon icon) {
		Tessellator tessellator = Tessellator.instance;
		double zMin1, zMin2, zMax1, zMax2;
		
		zMin1 = 0.6D;
		zMin2 = 0.4D;
		zMax1 = 0D;
		zMax2 = 1D;
        double u1 = (double)icon.getInterpolatedU(zMin1 * 16.0D);
        double u2 = (double)icon.getInterpolatedU(zMin2 * 16.0D);
        double u3 = (double)icon.getInterpolatedU(zMax1 * 16.0D);
        double u4 = (double)icon.getInterpolatedU(zMax2 * 16.0D);
        double v1 = (double)icon.getInterpolatedV(16.0D - 0.7D * 16.0D);
        double v2 = (double)icon.getInterpolatedV(0D);
		
		tessellator.setNormal(0f, 0f, -1f);
		tessellator.addVertexWithUV(1D, 0.7D, 0.1D, u1, v1);
		tessellator.addVertexWithUV(1D, 0.3D, 0.1D, u2, v1);
		tessellator.addVertexWithUV(0.7D, 0.3D, 0.3D, u4, v2);
		tessellator.addVertexWithUV(0.7D, 0.7D, 0.3D, u3, v2);
	}
	
}

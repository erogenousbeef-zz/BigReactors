package erogenousbeef.bigreactors.client.renderer;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.Icon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

// Shamelessly stolen from BuildCraft

public class RenderHelpers {

	private static RenderBlocks renderBlocks = new RenderBlocks();
	
	public static class BlockInterface {
		public double minX;
		public double minY;
		public double minZ;
		public double maxX;
		public double maxY;
		public double maxZ;
		
		public Block baseBlock = Block.sand;
		public Icon texture = null;
		
		public Icon getBlockTextureFromSide(int i) {
			if (texture == null) {
				return baseBlock.getBlockTextureFromSide(i);
			}
			else {
				return texture;
			}
		}
		
		public float getBlockBrightness(IBlockAccess iblockaccess, int x, int y, int z) {
			return baseBlock.getBlockBrightness(iblockaccess, x, y, z);
		}
	}
	
	public static void renderBlock(BlockInterface block, IBlockAccess blockAccess, int x, int y, int z, boolean doLight, boolean doTessellating) {
		float[] colorBySide = new float[] { 0.5f, 1.0f, 0.8f, 0.8f, 0.6f, 0.6f };
		renderBlocks.renderMaxX = block.maxX;
		renderBlocks.renderMaxY = block.maxY;
		renderBlocks.renderMaxZ = block.maxZ;
		renderBlocks.renderMinY = block.minY;
		renderBlocks.renderMinX = block.minX;
		renderBlocks.renderMinZ = block.minZ;
		renderBlocks.enableAO = false;
		
		Tessellator tessellator = Tessellator.instance;
		
		if(doTessellating) {
			tessellator.startDrawingQuads();
		}
		
		float f4 = 0, f5 = 0;
		if(doLight) {
			f4 = block.getBlockBrightness(blockAccess, x, y, z);			
		}
		
		for(int side = 0; side < colorBySide.length; ++side) {
			if(doLight) {
				f5 = block.getBlockBrightness(blockAccess, x, y, z);
				if(f5 < f4) {
					f5 = f4;
				}
				
				tessellator.setColorOpaque_F(colorBySide[side] * f5, colorBySide[side] * f5, colorBySide[side] * f5);
			}
			
			switch(side) {
			case 0:
				renderBlocks.renderFaceYNeg(null, 0, 0, 0, block.getBlockTextureFromSide(side));
				break;
			case 1:
				renderBlocks.renderFaceYPos(null, 0, 0, 0, block.getBlockTextureFromSide(side));
				break;
			case 2:
				renderBlocks.renderFaceXPos(null, 0, 0, 0, block.getBlockTextureFromSide(side));
				break;
			case 3:
				renderBlocks.renderFaceXNeg(null, 0, 0, 0, block.getBlockTextureFromSide(side));
				break;
			case 4:
				renderBlocks.renderFaceZPos(null, 0, 0, 0, block.getBlockTextureFromSide(side));
				break;
			case 5:
			default:
				renderBlocks.renderFaceZNeg(null, 0, 0, 0, block.getBlockTextureFromSide(side));
			}
			
		}
		
		if(doTessellating) {
			tessellator.draw();
		}
	}
	
}

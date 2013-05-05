package erogenousbeef.bigreactors.client.renderer;

import java.util.HashMap;

import org.lwjgl.opengl.GL11;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;

import erogenousbeef.bigreactors.client.renderer.RenderHelpers.BlockInterface;
import erogenousbeef.bigreactors.common.BRRegistry;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.tileentity.TileEntityFuelRod;

import net.minecraft.block.Block;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.RenderEngine;
import net.minecraft.client.renderer.texture.TextureStitched;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.liquids.LiquidStack;

public class RendererFuelRod extends TileEntitySpecialRenderer {

	private final static int displayStages = 10;
	
	private final HashMap<LiquidStack, int[]> stage = new HashMap<LiquidStack, int[]>();
	
	private int[] getDisplayListsForLiquid(LiquidStack liquid, World world) {
		if(stage.containsKey(liquid)) {
			return stage.get(liquid);
		}
		
		int[] newDisplayList = new int[displayStages];
		stage.put(liquid, newDisplayList);
		
		BlockInterface block = new BlockInterface();
		block.baseBlock = Block.waterStill;
		block.texture = liquid.getRenderingIcon();
		
		if(liquid.itemID < Block.blocksList.length && Block.blocksList[liquid.itemID] != null) {
			block.baseBlock = Block.blocksList[liquid.itemID];
		}
		
		for(int i = 0; i < displayStages; ++i) {
			double sideLength = 0.05 + (0.44 * i / displayStages);
			
			newDisplayList[i] = GLAllocation.generateDisplayLists(1);
			GL11.glNewList(newDisplayList[i], GL11.GL_COMPILE);
			block.minX = 0.5 - sideLength;
			block.minZ = 0.5 - sideLength;
			block.minY = 0.05;
			
			block.maxX = 0.5 + sideLength;
			block.maxZ = 0.5 + sideLength;
			block.maxY = 0.95;
			
			RenderHelpers.renderBlock(block, world, 0, 0, 0, false, true);
			
			GL11.glEndList();
		}
		
		return newDisplayList;
	}
	
	@Override
	public void renderTileEntityAt(TileEntity tileentity, double x, double y,
			double z, float f) {
		TileEntityFuelRod fuelRod = (TileEntityFuelRod)tileentity;
		
		LiquidStack fuelLiquid = fuelRod.getFuel();
		LiquidStack wasteLiquid = fuelRod.getWaste();
		
		if(fuelLiquid == null && wasteLiquid == null) { 
			return;
		}
		
		LiquidStack refFuelLiquid = null;
		if(fuelLiquid != null) {
			refFuelLiquid = fuelLiquid.canonical();			
		}
		
		LiquidStack refWasteLiquid = null;
		if(wasteLiquid != null) {
			refWasteLiquid = wasteLiquid.canonical();			
		}

		boolean noFuel = (refFuelLiquid == null || fuelLiquid.amount <= 0);
		boolean noWaste = (refWasteLiquid == null || wasteLiquid.amount <= 0);
		if(noFuel && noWaste) {
			// Tank is empty.
			return;
		}
		else if(noWaste) {
			renderSingleLiquid(fuelLiquid, fuelRod, x, y, z);
		}
		else if(noFuel) {
			renderSingleLiquid(wasteLiquid, fuelRod, x, y, z);
		} else {
			// Render both liquids, blend their registered colors.
			int[] displayList = getDisplayListsForLiquid(BigReactors.liquidFuelColumn, fuelRod.worldObj);
			
			int totalLiquid = fuelRod.getTotalLiquid();
			float liquidProportion = (float)totalLiquid / (float) (fuelRod.maxTotalLiquid);

			// Obtain registered colors for blending
			int blendColor = getRegisteredLiquidColor(refWasteLiquid);
			int baseColor = getRegisteredLiquidColor(refFuelLiquid);
			
	        float baseR = unpackR(baseColor);
	        float baseG = unpackG(baseColor);
	        float baseB = unpackB(baseColor);
			
	        float blendR = unpackR(blendColor);
	        float blendG = unpackG(blendColor);
	        float blendB = unpackB(blendColor);
	        
	        float proportion = (float)fuelLiquid.amount / (float)totalLiquid;
			int displayListIndex = (int) (liquidProportion * (displayStages - 1));

			// Render, lerping between the colors.
			// TODO: Should this be cached in a lookup table for efficiency?
			doRender(displayList[displayListIndex], BigReactors.liquidFuelColumn.getTextureSheet(), 
					lerp(blendR, baseR, proportion), lerp(blendG, baseG, proportion), lerp(blendB, baseB, proportion),
					 x, y, z);
		} // END: Render both liquids
	}
	
	protected void renderSingleLiquid(LiquidStack referenceLiquid, TileEntityFuelRod fuelRod, double x, double y, double z) {
		int[] displayList = getDisplayListsForLiquid(referenceLiquid, fuelRod.worldObj);
		
		if(displayList == null) {
			return;
		}
		
		int displayListItem = displayList[(int) ((float)fuelRod.getTotalLiquid() / (float) (fuelRod.maxTotalLiquid) * (displayStages - 1))];
		
		doRender(displayListItem, referenceLiquid.getTextureSheet(), 1f, 1f, 1f, x, y, z);
	}
	
	protected void doRender(int displayListItem, String textureSheet, float r, float g, float b,double x, double y, double z) {
		GL11.glPushMatrix();
		GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glDisable(GL11.GL_LIGHTING); // GL_LIGHTING
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glTranslatef((float)x, (float)y, (float)z);

		bindTextureByName(textureSheet);
		
		GL11.glColor4f(r, g, b, 1f);
		
		GL11.glCallList(displayListItem);
		
		GL11.glPopAttrib();
		GL11.glPopMatrix();		
	}

	// Returns the registered liquid color if there is one; 0 otherwise.
	protected int getRegisteredLiquidColor(LiquidStack liquid) {
		if(BRRegistry.getReactorFuelLiquids().containsKey(liquid.itemID)) {
			return BRRegistry.getReactorFuelLiquids().get(liquid.itemID).getBlendColor();
		}
		return 0;
	}
	
	protected static float unpackR(int rgb) {
		return (float)(rgb >> 16 & 255) / 255.0F;
	}
	
	protected static float unpackG(int rgb) {
		return (float)(rgb >> 8 & 255) / 255.0F;
	}
	
	protected static float unpackB(int rgb) {
		return (float)(rgb & 255) / 255.0F;
	}
	
	// Linear interpolate between a min and max value over the interval 0..1.
	protected static float lerp(float min, float max, float value) {
		return min + (max-min)*value;
	}
}

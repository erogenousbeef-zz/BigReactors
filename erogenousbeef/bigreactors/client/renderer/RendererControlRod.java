package erogenousbeef.bigreactors.client.renderer;

import java.util.HashMap;

import org.lwjgl.opengl.GL11;

import erogenousbeef.bigreactors.api.IReactorFuel;
import erogenousbeef.bigreactors.client.renderer.RenderHelpers.BlockInterface;
import erogenousbeef.bigreactors.common.BRRegistry;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.tileentity.TileEntityFuelRod;
import erogenousbeef.bigreactors.common.tileentity.TileEntityReactorControlRod;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.liquids.LiquidStack;

public class RendererControlRod extends TileEntitySpecialRenderer {

	private final static int displayStages = 20;
	private final HashMap<LiquidStack, HashMap<Integer, int[]>> stage = new HashMap<LiquidStack, HashMap<Integer, int[]>>();
	
	private final HashMap<Integer, Integer> rodStages = new HashMap<Integer, Integer>();
	
	/**
	 * Returns the display list for a control rod of the given length.
	 * @param length The length of the rod, in blocks. This should be something like insertion% * columnHeight.
	 * @return The display list for this control rod.
	 */
	private int getDisplayListForControlRod(double length, World world) {
		int blox = (int)Math.min(Math.ceil(length), TileEntityReactorControlRod.maxFuelRodsBelow);
		if(rodStages.containsKey(blox)) {
			return rodStages.get(blox);
		}
		
		// TODO: Supply my own icon
		BlockInterface block = new BlockInterface();
		block.baseBlock = Block.obsidian;
		block.texture = Block.obsidian.getBlockTextureFromSide(0);
		
		// Now draw the rod itself, a skinny thing of a given length
		int newDisplayList = GLAllocation.generateDisplayLists(1);
		GL11.glNewList(newDisplayList, GL11.GL_COMPILE);
		block.minX = 0.40;
		block.minY = 0.00;
		block.minZ = 0.40;
		
		block.maxX = 0.60;
		block.maxY = (double)blox;
		block.maxZ = 0.60;
		
		RenderHelpers.renderBlock(block, world, 0, 0, 0, false, true);
		GL11.glEndList();
		
		rodStages.put(blox, newDisplayList);
		
		return newDisplayList;
	}
	
	private int[] getDisplayListsForLiquid(LiquidStack liquid, int numBlocks, World world) {
		if(!stage.containsKey(liquid)) {
			stage.put(liquid, new HashMap<Integer, int[]>());
		}
		
		HashMap<Integer, int[]> innerMap = stage.get(liquid);
		if(innerMap.containsKey(numBlocks)) {
			return innerMap.get(numBlocks);
		}

		int[] newDisplayList = new int[displayStages];
		innerMap.put(numBlocks, newDisplayList);
		
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
			block.maxY = 0.95 + (numBlocks-1);
			
			RenderHelpers.renderBlock(block, world, 0, 0, 0, false, true);
			
			GL11.glEndList();
		}
		
		return newDisplayList;
	}	
	
	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y,
			double z, float f) {
		TileEntityReactorControlRod controlRod = ((TileEntityReactorControlRod)tileEntity);
		
		if(!controlRod.isAssembled()) {
			return;
		}
		
		if(controlRod.getSizeOfFuelTank() <= 0) {
			return;
		}
		
		// Render rod itself
		renderControlRod(controlRod, x, y, z);

		if(controlRod.isEmpty()) {
			return;
		}
		
		// Now render fluid column
		int columnHeight = controlRod.getColumnHeight();
		
		double columnBottom = y - columnHeight;
		
		int fuelAmt = controlRod.getFuelAmount();
		int wasteAmt = controlRod.getWasteAmount();
		int totalAmt = fuelAmt + wasteAmt;
		float liquidProportion = (float)totalAmt / (float)controlRod.getSizeOfFuelTank();
		
		int fuelColor = getRegisteredFuelColor(controlRod.getFuelType());
		int wasteColor = getRegisteredWasteColor(controlRod.getWasteType());
        float fuelR = unpackR(fuelColor);
        float fuelG = unpackG(fuelColor);
        float fuelB = unpackB(fuelColor);
		
        float wasteR = unpackR(wasteColor);
        float wasteG = unpackG(wasteColor);
        float wasteB = unpackB(wasteColor);
		
        float wasteProportion = (float)wasteAmt / (float)totalAmt;

		int[] displayList = getDisplayListsForLiquid(BigReactors.liquidFuelColumn, columnHeight, controlRod.worldObj);
		int displayListIndex = (int) (liquidProportion * (displayStages-1));
		renderLiquidColumn(displayList[displayListIndex], BigReactors.liquidFuelColumn.getTextureSheet(),
				lerp(fuelR, wasteR, wasteProportion), lerp(fuelR, wasteG, wasteProportion), lerp(fuelB, wasteB, wasteProportion),
				 x, columnBottom, z);
	}
	
	// Render Helpers
	
	private void renderControlRod(TileEntityReactorControlRod controlRod, double x, double y, double z) {
		double rodLength = ((double)controlRod.getControlRodInsertion()/100.0) * 
								(double)controlRod.getColumnHeight();
		
		double rodBottom = y - rodLength;
		int displayList = getDisplayListForControlRod(rodLength, controlRod.worldObj);

		GL11.glPushMatrix();
		GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glTranslatef((float)x, (float)rodBottom, (float)z);
		
		// Why is there no constant for this? :(
		bindTextureByName("/terrain.png");
		GL11.glCallList(displayList);
		
		GL11.glPopAttrib();
		GL11.glPopMatrix();		
	}

	protected void renderLiquidColumn(int displayListItem, String textureSheet, float r, float g, float b,double x, double y, double z) {
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

	// Liquid/Color Helpers
	
	// Returns the registered liquid color if there is one; 0 otherwise.
	protected int getRegisteredFuelColor(ItemStack itemStack) {
		IReactorFuel fuelData = BRRegistry.getDataForFuel(itemStack);
		if(fuelData != null) {
			return fuelData.getFuelColor();
		}
		
		return BigReactors.defaultLiquidColorFuel;
	}
	
	protected int getRegisteredWasteColor(ItemStack itemStack) {
		IReactorFuel fuelData = BRRegistry.getDataForWaste(itemStack);
		if(fuelData != null) {
			return fuelData.getFuelColor();
		}

		return BigReactors.defaultLiquidColorWaste;
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

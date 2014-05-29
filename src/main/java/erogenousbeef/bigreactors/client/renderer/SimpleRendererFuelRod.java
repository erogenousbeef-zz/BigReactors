package erogenousbeef.bigreactors.client.renderer;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fluids.Fluid;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import erogenousbeef.bigreactors.common.BRRegistry;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.multiblock.MultiblockReactor;
import erogenousbeef.bigreactors.common.multiblock.block.BlockFuelRod;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorFuelRod;

public class SimpleRendererFuelRod implements ISimpleBlockRenderingHandler {

	private static final float FLUID_RENDER_OFFSET_MAX = 0.05f;
	private static final float FLUID_RENDER_OFFSET_MIN = 0.45f;
	
	public SimpleRendererFuelRod() {
	}

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelID,
			RenderBlocks renderer) {
		Tessellator tessellator = Tessellator.instance;
        GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, -1.0F, 0.0F);
        renderer.renderFaceYNeg(block, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSideAndMetadata(block, 0, metadata));
        tessellator.draw();
        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, 1.0F, 0.0F);
        renderer.renderFaceYPos(block, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSideAndMetadata(block, 1, metadata));
        tessellator.draw();
        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, 0.0F, -1.0F);
        renderer.renderFaceZNeg(block, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSideAndMetadata(block, 2, metadata));
        tessellator.draw();
        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, 0.0F, 1.0F);
        renderer.renderFaceZPos(block, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSideAndMetadata(block, 3, metadata));
        tessellator.draw();
        tessellator.startDrawingQuads();
        tessellator.setNormal(-1.0F, 0.0F, 0.0F);
        renderer.renderFaceXNeg(block, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSideAndMetadata(block, 4, metadata));
        tessellator.draw();
        tessellator.startDrawingQuads();
        tessellator.setNormal(1.0F, 0.0F, 0.0F);
        renderer.renderFaceXPos(block, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSideAndMetadata(block, 5, metadata));
        tessellator.draw();
        GL11.glTranslatef(0.5F, 0.5F, 0.5F);
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z,
			Block block, int modelId, RenderBlocks renderer) {
		if(!(block instanceof BlockFuelRod)) { return false; }

        Tessellator tessellator = Tessellator.instance;
		BlockFuelRod blockFuelRod = (BlockFuelRod)block;

        boolean renderTop = block.shouldSideBeRendered(world, x, y + 1, z, 0);
        boolean renderBottom = block.shouldSideBeRendered(world, x, y - 1, z, 0);
        boolean renderedFuelOnTop = false;
        boolean renderedFuelOnBottom = false;
        
        boolean[] renderSides = new boolean[]
        {
            block.shouldSideBeRendered(world, x, y, z - 1, 2), 
            block.shouldSideBeRendered(world, x, y, z + 1, 3),
            block.shouldSideBeRendered(world, x - 1, y, z, 4), 
            block.shouldSideBeRendered(world, x + 1, y, z, 5)
        };

        if (!renderTop && !renderBottom && !renderSides[0] && !renderSides[1] && !renderSides[2] && !renderSides[3])
        {
            return false;
        }

        boolean rendered = false;
        int blockMetadata = world.getBlockMetadata(x, y, z);
        
        // Render internal bits, if we can
        TileEntity te;
        te = world.getTileEntity(x, y, z);
        if(te instanceof TileEntityReactorFuelRod) {
        	TileEntityReactorFuelRod fuelRod = (TileEntityReactorFuelRod)te;
        	if(fuelRod.isConnected()) {
        		MultiblockReactor reactor = (MultiblockReactor)fuelRod.getMultiblockController();
        		int fuelAmount = reactor.getFuelAmount();
        		int wasteAmount = reactor.getWasteAmount();
        		int totalFluid = fuelAmount + wasteAmount;
        		int capacity = reactor.getCapacity();
        		if(capacity > 0 && totalFluid > 0) {
        			// Okay, we're connected and have some kind of fluid inside. Let's do this.
        	        float fluidColumnOffsetFromCenter = -1f;
        	        float red, green, blue;
        	        IIcon iconSide, iconBottom;
        	        iconSide = iconBottom = null;
        	        red = green = blue = 1f;

    	    		Fluid rodFuel = reactor.getFuelType();
    	    		Fluid rodWaste = reactor.getWasteType();
        	        
        	    	if(rodFuel != null && rodWaste != null) {
        	    		iconSide = BigReactors.fluidFuelColumn.getFlowingIcon();
        	    		iconBottom = BigReactors.fluidFuelColumn.getStillIcon();
        	        	
        	        	// Blend the colors
        	        	int fuelColor = BRRegistry.getReactorFluidInfo(rodFuel.getName()).getFuelColor();
        	        	int wasteColor = BRRegistry.getReactorFluidInfo(rodWaste.getName()).getFuelColor();
        	        	float proportion = (float)fuelAmount / (float)totalFluid;
        	        	float fuelR, fuelG, fuelB;
        	        	float wasteR, wasteG, wasteB;

        	        	fuelR = unpackR(fuelColor);
        	        	fuelG = unpackG(fuelColor);
        	        	fuelB = unpackB(fuelColor);
        	        	wasteR = unpackR(wasteColor);
        	        	wasteG = unpackG(wasteColor);
        	        	wasteB = unpackB(wasteColor);

        	        	red = lerp(wasteR, fuelR, proportion);
        	        	green = lerp(wasteG, fuelG, proportion);
        	        	blue = lerp(wasteB, fuelB, proportion);
        	    	}
        	    	else if(rodFuel != null) {
        	    		iconSide = rodFuel.getFlowingIcon();
        	    		iconBottom = rodFuel.getStillIcon();
        	    	}
        	    	else if(rodWaste != null) {
        	    		iconSide = rodWaste.getFlowingIcon();
        	    		iconBottom = rodWaste.getStillIcon();
        	    	}

    	    		float pctFilled = Math.min(1f, Math.max(0f, (float)totalFluid / (float)capacity));
    	    		fluidColumnOffsetFromCenter = lerp(FLUID_RENDER_OFFSET_MIN, FLUID_RENDER_OFFSET_MAX, pctFilled);
        	    	
    	    		if(iconSide != null && iconBottom != null) {
    	    			// We've got fuel data! Let's do this thang.
    	    			
	    	            tessellator.setColorRGBA_F(red, green, blue, 0.75f);
	    	            tessellator.setBrightness(world.getLightBrightnessForSkyBlocks(x, y, z, 15));

    	    			renderer.setOverrideBlockTexture(iconBottom);
	    	            float xzMin = fluidColumnOffsetFromCenter;
	    	            float xzMax = 1f - fluidColumnOffsetFromCenter;
	    				renderer.setRenderBounds(xzMin, 0.01f, xzMin, xzMax, 0.99f, xzMax);
	    	            
    	    			if(renderer.renderAllFaces || renderBottom) {
    	    				rendered = true;
    	    				renderer.renderFaceYNeg(block, x, y, z, iconBottom);
    	    			}
    	    			
    	    			if(renderer.renderAllFaces || renderTop) {
    	    				rendered = true;
    	    				renderer.renderFaceYPos(block, x, y, z, iconBottom);
    	    			}
    	    			
    	    			renderer.setOverrideBlockTexture(iconSide);
	    				renderer.setRenderBounds(xzMin, 0f, xzMin, xzMax, 1f, xzMax);
	    				
	    				if(renderer.renderAllFaces || renderSides[0]) {
    	    				rendered = true;
	    		            renderer.renderFaceZNeg(block, x, y, z, iconSide);
	    				}
	    				if(renderer.renderAllFaces || renderSides[1]) {
    	    				rendered = true;
	    		            renderer.renderFaceZPos(block, x, y, z, iconSide);
	    				}
	    				if(renderer.renderAllFaces || renderSides[2]) {
    	    				rendered = true;
	    		            renderer.renderFaceXNeg(block, x, y, z, iconSide);
	    				}
	    				if(renderer.renderAllFaces || renderSides[3]) {
    	    				rendered = true;
	    		            renderer.renderFaceXPos(block, x, y, z, iconSide);
	    				}
    	    		}
        		}
        	}
        }
        
        // Then render external housing
        tessellator.setColorRGBA(255, 255, 255, 255);
        renderer.setRenderBoundsFromBlock(block);
        renderer.setOverrideBlockTexture(null);

        // First, render the regular block
        if(!renderedFuelOnBottom && (renderer.renderAllFaces || renderBottom)) {
        	rendered = true;
            tessellator.setBrightness(block.getMixedBrightnessForBlock(world, x, y, z));
            renderer.renderFaceYNeg(block, x, y, z, block.getIcon(0, blockMetadata));
        }
        if(renderer.renderAllFaces || renderTop) {
        	rendered = true;
            tessellator.setBrightness(block.getMixedBrightnessForBlock(world, x, y, z));
            renderer.renderFaceYPos(block, x, y, z, block.getIcon(1, blockMetadata));
        }
        if(renderer.renderAllFaces || renderSides[0]) {
        	rendered = true;
            tessellator.setBrightness(block.getMixedBrightnessForBlock(world, x, y, z));
            renderer.renderFaceZNeg(block, x, y, z, block.getIcon(2, blockMetadata));
        }
        if(renderer.renderAllFaces || renderSides[1]) {
        	rendered = true;
            tessellator.setBrightness(block.getMixedBrightnessForBlock(world, x, y, z));
            renderer.renderFaceZPos(block, x, y, z, block.getIcon(3, blockMetadata));
        }
        if(renderer.renderAllFaces || renderSides[2]) {
        	rendered = true;
            tessellator.setBrightness(block.getMixedBrightnessForBlock(world, x, y, z));
            renderer.renderFaceXNeg(block, x, y, z, block.getIcon(4, blockMetadata));
        }
        if(renderer.renderAllFaces || renderSides[3]) {
        	rendered = true;
            tessellator.setBrightness(block.getMixedBrightnessForBlock(world, x, y, z));
            renderer.renderFaceXPos(block, x, y, z, block.getIcon(5, blockMetadata));
        }
        
		return rendered;
	}

	@Override
	public boolean shouldRender3DInInventory(int var1) {
		return true;
	}

	@Override
	public int getRenderId() {
		return BlockFuelRod.renderId;
	}

	/// HELPERS ///
	protected static float unpackR(int rgb) {
		return (float)(rgb >> 16 & 255) / 255.0F;
	}
	
	protected static float unpackG(int rgb) {
		return (float)(rgb >> 8 & 255) / 255.0F;
	}
	
	protected static float unpackB(int rgb) {
		return (float)(rgb & 255) / 255.0F;
	}	
	protected static float lerp(float min, float max, float value) {
		return min + (max-min)*value;
	}
}

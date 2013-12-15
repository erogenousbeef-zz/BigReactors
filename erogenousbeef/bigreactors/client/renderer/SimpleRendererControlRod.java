package erogenousbeef.bigreactors.client.renderer;

import org.lwjgl.opengl.GL11;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.common.FMLLog;
import erogenousbeef.bigreactors.api.IReactorFuel;
import erogenousbeef.bigreactors.common.BRRegistry;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.block.BlockReactorControlRod;
import erogenousbeef.bigreactors.common.tileentity.TileEntityReactorControlRod;

public class SimpleRendererControlRod implements ISimpleBlockRenderingHandler {

	private static final float ROD_RENDER_OFFSET = 0.4f;
	private static final float FLUID_RENDER_OFFSET_MAX = 0.05f;
	private static final float FLUID_RENDER_OFFSET_MIN = 0.45f;
	
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
		if(!(block instanceof BlockReactorControlRod)) { return false; }

        Tessellator tessellator = Tessellator.instance;
        BlockReactorControlRod blockControlRod = (BlockReactorControlRod)block;
        
        boolean renderTop = block.shouldSideBeRendered(world, x, y + 1, z, 0);
        boolean renderBottom = block.shouldSideBeRendered(world, x, y - 1, z, 0);
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

        int blockMetadata = world.getBlockMetadata(x, y, z);
        boolean rendered = false;
        // First, render the regular block
        if(renderer.renderAllFaces || renderBottom) {
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

        // Now deal with the fuel column or control rod.
        TileEntity te = world.getBlockTileEntity(x, y, z);
        TileEntityReactorControlRod controlRod = null;
        if(te instanceof TileEntityReactorControlRod) {
        	controlRod = (TileEntityReactorControlRod)te;
        }

        float rodHeight = -1f;
        Icon iconSide, iconBottom;
        float renderOffset = -1f;
        float red, green, blue;

        iconSide = iconBottom = null;
        red = green = blue = 1f;

        if(controlRod == null) {
        	return rendered;
        }

    	Fluid rodFuel = controlRod.getFuelType();
    	Fluid rodWaste = controlRod.getWasteType();
    	rodHeight = controlRod.getColumnHeight();

    	if(rodFuel != null && rodWaste != null) {
    		iconSide = BigReactors.fluidFuelColumn.getFlowingIcon();
    		iconBottom = BigReactors.fluidFuelColumn.getStillIcon();
    		float pctFilled = (float)(controlRod.getFuelAmount() + controlRod.getWasteAmount()) / (float)controlRod.getSizeOfFuelTank();
        	renderOffset = lerp(FLUID_RENDER_OFFSET_MIN, FLUID_RENDER_OFFSET_MAX, pctFilled);
        	
        	// Blend the colors
        	int fuelColor = BRRegistry.getDataForFluid(rodFuel).getFuelColor();
        	int wasteColor = BRRegistry.getDataForFluid(rodWaste).getFuelColor();
        	float proportion = (float)controlRod.getFuelAmount() / (float)(controlRod.getFuelAmount() + controlRod.getWasteAmount());
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
    		float pctFilled = (float)controlRod.getFuelAmount() / (float)controlRod.getSizeOfFuelTank();
        	renderOffset = lerp(FLUID_RENDER_OFFSET_MIN, FLUID_RENDER_OFFSET_MAX, pctFilled);
    	}
    	else if(rodWaste != null) {
    		iconSide = rodWaste.getFlowingIcon();
    		iconBottom = rodWaste.getStillIcon();
    		float pctFilled = (float)controlRod.getWasteAmount() / (float)controlRod.getSizeOfFuelTank();
        	renderOffset = lerp(FLUID_RENDER_OFFSET_MIN, FLUID_RENDER_OFFSET_MAX, pctFilled);
    	}
    	else if(controlRod.getControlRodInsertion() > 0) {
        	rodHeight = (float)controlRod.getColumnHeight() * ((float)controlRod.getControlRodInsertion() / 100f);
    		iconSide = Block.obsidian.getIcon(2, 0);	// TODO: My own icons?
    		iconBottom = Block.obsidian.getIcon(0, 0);
        	renderOffset = ROD_RENDER_OFFSET;
		}

        double u1, u2, u3, u4, v1, v2, v3, v4;

        // Render bottom face of rod
        if(iconSide != null && iconBottom != null) {
            rendered = true;
            u2 = iconBottom.getInterpolatedU(0.0D);
            v2 = iconBottom.getInterpolatedV(0.0D);
            u1 = u2;
            v1 = iconBottom.getInterpolatedV(16.0D);
            u4 = iconBottom.getInterpolatedU(16.0D);
            v4 = v1;
            u3 = u4;
            v3 = v2;

            tessellator.setColorOpaque_F(red, green, blue);
            tessellator.setBrightness(world.getLightBrightnessForSkyBlocks(x, y, z, 15));
            
            // Render bottom of column
            tessellator.addVertexWithUV(x + renderOffset, y - rodHeight, z + 1 - renderOffset, u2, v2);
            tessellator.addVertexWithUV(x + renderOffset, y - rodHeight, z + renderOffset, u1, v1);
            tessellator.addVertexWithUV(x + 1 - renderOffset, y - rodHeight, z + renderOffset, u4, v4);
            tessellator.addVertexWithUV(x + 1 - renderOffset, y - rodHeight, z + 1 - renderOffset, u3, v3);
            
            // Render column sides
            for (int side = 0; side < 4; ++side)
            {
                int x2 = x;
                int z2 = z;
                float drawnHeight = 0f;

                switch (side)
                {
                    case 0: --z2; break;
                    case 1: ++z2; break;
                    case 2: --x2; break;
                    case 3: ++x2; break;
                }

                // Force fuel columns to be fullbright

                double tx1;
                double tx2;
                double tz1;
                double tz2;

                if (side == 0)
                {
                    tx1 = x + renderOffset;
                    tx2 = x + 1 - renderOffset;
                    tz1 = z + renderOffset;
                    tz2 = z + renderOffset;
                }
                else if (side == 1)
                {
                    tx1 = x + 1 - renderOffset;
                    tx2 = x + renderOffset;
                    tz1 = z + 1 - renderOffset;
                    tz2 = z + 1 - renderOffset;
                }
                else if (side == 2)
                {
                    tx1 = x + renderOffset;
                    tx2 = x + renderOffset;
                    tz1 = z + 1 - renderOffset;
                    tz2 = z + renderOffset;
                }
                else
                {
                    tx1 = x + 1 - renderOffset;
                    tx2 = x + 1 - renderOffset;
                    tz1 = z + renderOffset;
                    tz2 = z + 1 - renderOffset;
                }

                // Draw column in segments so we don't stretch textures.
                float currentY = y - rodHeight;
                while(currentY < y) {
                    float heightToDraw = Math.max(Math.min(1.0f, rodHeight - drawnHeight), 0.05f);
                    
                    u1 = iconSide.getInterpolatedU(0.0D);
                    u2 = iconSide.getInterpolatedU(16.0D);
                    v1 = iconSide.getInterpolatedV(0.0D);
                    v2 = iconSide.getInterpolatedV(0.0D);
                    v3 = iconSide.getInterpolatedV(16.0D * heightToDraw);

                    tessellator.addVertexWithUV(tx1, currentY + heightToDraw, tz1, u1, v1);
                    tessellator.addVertexWithUV(tx2, currentY + heightToDraw, tz2, u2, v2);
                    tessellator.addVertexWithUV(tx2, currentY, tz2, u2, v3);
                    tessellator.addVertexWithUV(tx1, currentY, tz1, u1, v3);
                    
                	drawnHeight += heightToDraw;
                	currentY += heightToDraw;
                }
            }
        }
        
		return true;
	}

	@Override
	public boolean shouldRender3DInInventory() {
		return true;
	}

	@Override
	public int getRenderId() {
		return BlockReactorControlRod.renderId;
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

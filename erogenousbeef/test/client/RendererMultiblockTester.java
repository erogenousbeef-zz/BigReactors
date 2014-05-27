package erogenousbeef.test.client;

import org.lwjgl.opengl.GL11;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import erogenousbeef.core.multiblock.IMultiblockPart;
import erogenousbeef.test.TestMod;
import erogenousbeef.test.common.BlockMultiblockTester;
import erogenousbeef.test.common.TileEntityMultiblockTester;
import erogenousbeef.test.common.TestMultiblockController;

public class RendererMultiblockTester extends TileEntitySpecialRenderer {

	RenderBlocks renderBlocks = new RenderBlocks();
	
	@Override
	public void renderTileEntityAt(TileEntity tileentity, double x, double y,
			double z, float f) {
		GL11.glPushMatrix();
        float f1 = 0.6666667F;
        float f2 = 0.16f * f1;
		
        FontRenderer fontrenderer = this.getFontRenderer();
        int color = 0;
        int identifier = -1;
        int numConnected = -1;
        if(tileentity instanceof TileEntityMultiblockTester) {
        	TileEntityMultiblockTester mbt = (TileEntityMultiblockTester)tileentity;
        	color = mbt.getColor();
        	if(mbt.getMultiblockController() != null) {
            	numConnected = mbt.getMultiblockController().getNumConnectedBlocks();
            	identifier = ((TestMultiblockController)mbt.getMultiblockController()).ordinal;
        	}
        	else {
        		numConnected = -1;
        		identifier = -1;
        	}
        }

        GL11.glPushAttrib(GL11.GL_COLOR_BUFFER_BIT);
        GL11.glPushAttrib(GL11.GL_LIGHTING_BIT);

        GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_CULL_FACE);
        
        int xmod, zmod;
        for(int i = 0; i < 1; i++) {
        	GL11.glPushMatrix();

        	if(i>=2) {zmod=1;} else {zmod=0;}
        	if(i%2!=0) {xmod=1;} else {xmod=0;}
            //GL11.glTranslatef((float)x + 0.5F, (float)y + 0.75F * f1, (float)z + 0.5F);
            //GL11.glTranslatef(0.0F, 0.5F * f1, 0.07F * f1);
            
        	GL11.glTranslatef((float)x+0.5f, (float)y+0.9f, (float)z+0.5f);
            GL11.glRotatef(90*i, 0.0F, 1.0F, 0.0F);
            GL11.glTranslatef(-0.2f, 0f, 0.51f);
            
            GL11.glPushMatrix();
            GL11.glTranslatef(-0.1F, -0.2F, 0.0f);
            GL11.glScalef(f2*0.5f, -f2*0.5f, f2*0.5f);
            GL11.glNormal3f(0.0F, 0.0F, -1.0F * f2);
            if(identifier == IMultiblockPart.INVALID_DISTANCE) {
                fontrenderer.drawString("XX", 0, 0, color);
            }
            else {
                fontrenderer.drawString(Integer.toString(identifier), 0, 0, color);
            }

        	GL11.glPopMatrix();
        	
        	GL11.glTranslatef(-0.2f, 0.0f, 0.0f);
        	GL11.glScalef(f2*0.15f, -f2*0.15f, f2*0.15f);
            GL11.glNormal3f(0.0F, 0.0F, -1.0F * f2);
            fontrenderer.drawString(String.format("%d", numConnected), 0, 0, color);
        	
        	GL11.glPopMatrix();
        }
		
        GL11.glPopAttrib();
        GL11.glPopAttrib();
        GL11.glPopAttrib();
		GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		GL11.glPopMatrix();
	}
}

package erogenousbeef.bigreactors.client.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;

import erogenousbeef.bigreactors.client.ClientProxy;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.multiblock.MultiblockTurbine;
import erogenousbeef.bigreactors.common.multiblock.block.BlockTurbineRotorPart;
import erogenousbeef.bigreactors.common.multiblock.helpers.RotorInfo;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityTurbineRotorBearing;
import erogenousbeef.bigreactors.utils.StaticUtils;
import erogenousbeef.core.common.CoordTriplet;

public class RotorSpecialRenderer extends TileEntitySpecialRenderer {

	RenderBlocks renderBlocks = new RenderBlocks();
	
	@Override
	public void renderTileEntityAt(TileEntity tileentity, double x, double y,
			double z, float f) {
		TileEntityTurbineRotorBearing bearing = (TileEntityTurbineRotorBearing)tileentity;
		
		if(bearing == null || !bearing.isConnected()) { return; }
		
		MultiblockTurbine turbine = bearing.getTurbine();
		
		if(!turbine.isAssembled() || !turbine.isActive() || !turbine.hasGlass()) { return; }
		
		Integer displayList = bearing.getDisplayList();
		ForgeDirection rotorDir = bearing.getOutwardsDir().getOpposite();
		
		if(displayList == null) {
			RotorInfo info = bearing.getRotorInfo();
			displayList = generateRotor(info);
			bearing.setDisplayList(displayList);
		}
		
		float angle = bearing.getAngle();
		long elapsedTime = Minecraft.getSystemTime() - ClientProxy.lastRenderTime;
		
		float speed = turbine.getRotorSpeed();
		if(speed > 0.001f) {
			angle += speed * ((float)elapsedTime / 60000f) * 360f; // RPM * time in minutes * 360 degrees per rotation
			angle = angle % 360f;
			bearing.setAngle(angle);
		}

		GL11.glPushMatrix();
		GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glDisable(GL11.GL_LIGHTING);

		bindTexture(net.minecraft.client.renderer.texture.TextureMap.locationBlocksTexture);

		GL11.glTranslated(x + rotorDir.offsetX, y + rotorDir.offsetY, z + rotorDir.offsetZ);
		if(rotorDir.offsetX != 0) {
			GL11.glTranslated(0, 0.5, 0.5);
		}
		else if(rotorDir.offsetY != 0) {
			GL11.glTranslated(0.5, 0, 0.5);
		}
		else if(rotorDir.offsetZ != 0) {
			GL11.glTranslated(0.5, 0.5, 0);
		}

		GL11.glRotatef(angle, rotorDir.offsetX, rotorDir.offsetY, rotorDir.offsetZ);
		GL11.glColor3f(1f, 1f, 1f);
		GL11.glCallList(displayList);

		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glPopAttrib();
		GL11.glPopMatrix();
	}

	int generateRotor(RotorInfo rotorInfo) {
		int list = GLAllocation.generateDisplayLists(1);
		GL11.glNewList(list,  GL11.GL_COMPILE);

		ForgeDirection rotorDir = rotorInfo.rotorDirection;
		int rotorLen = rotorInfo.rotorLength;
		CoordTriplet currentRotorCoord = new CoordTriplet(0,0,0);

		Tessellator tessellator = Tessellator.instance;
		if(rotorDir.offsetX != 0) {
			tessellator.setTranslation(0, -0.5, -0.5);
		}
		else if(rotorDir.offsetY != 0) {
			tessellator.setTranslation(-0.5, 0, -0.5);
		}
		else {
			tessellator.setTranslation(-0.5, -0.5, 0);
		}

		tessellator.startDrawingQuads();
		tessellator.setBrightness(256);
		tessellator.setColorOpaque(255, 255, 255);
		
		CoordTriplet bladeCoord = new CoordTriplet(0,0,0);
		int rotorIdx = 0;
		boolean[] hasBlades = new boolean[4];
		ForgeDirection[] bladeDirs = StaticUtils.neighborsBySide[rotorInfo.rotorDirection.ordinal()];

		while(rotorIdx < rotorInfo.rotorLength) {
			
			for(int i = 0; i < hasBlades.length; i++) {
				hasBlades[i] = rotorInfo.bladeLengths[rotorIdx][i] > 0;
			}

			RotorSimpleRenderer.renderRotorShaft(BigReactors.blockTurbineRotorPart, renderBlocks, BlockTurbineRotorPart.METADATA_SHAFT, rotorDir, hasBlades, currentRotorCoord.x, currentRotorCoord.y, currentRotorCoord.z, false);
			for(int bladeIdx = 0; bladeIdx < bladeDirs.length; bladeIdx++) {
				bladeCoord.copy(currentRotorCoord);
				int bladeLen = 0;
				bladeCoord.translate(bladeDirs[bladeIdx]);
				while(bladeLen < rotorInfo.bladeLengths[rotorIdx][bladeIdx]) {
					RotorSimpleRenderer.renderBlade(renderBlocks, bladeCoord.x, bladeCoord.y, bladeCoord.z, BigReactors.blockTurbineRotorPart, BlockTurbineRotorPart.METADATA_BLADE, rotorInfo.rotorDirection);
					bladeLen++;
					bladeCoord.translate(bladeDirs[bladeIdx]);
				}
			}
			rotorIdx++;
			currentRotorCoord.translate(rotorDir);
		}
		tessellator.setTranslation(0, 0, 0);
		tessellator.draw();
		
		GL11.glEndList();
		return list;
	}
	
}

package erogenousbeef.bigreactors.client.renderer;

import java.util.Map;

import org.lwjgl.opengl.GL11;

import cofh.lib.render.RenderHelper;
import cofh.repack.codechicken.lib.lighting.LightModel;
import cofh.repack.codechicken.lib.render.CCModel;
import cofh.repack.codechicken.lib.render.CCRenderState;
import cofh.repack.codechicken.lib.render.uv.IconTransformation;
import cofh.repack.codechicken.lib.vec.Rotation;
import cofh.repack.codechicken.lib.vec.Scale;
import cofh.repack.codechicken.lib.vec.Translation;
import cofh.repack.codechicken.lib.vec.Vector3;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import erogenousbeef.bigreactors.common.BRLoader;
import erogenousbeef.bigreactors.common.BRLog;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.multiblock.block.BlockExchangerInteriorPart;

public class ExchangerPipeSimpleRenderer implements
		ISimpleBlockRenderingHandler {

	protected static CCModel base;
	protected static CCModel[] connector = new CCModel[6];
	protected static CCModel[] pipe = new CCModel[6];
	
	public static IconTransformation uvt[] = new IconTransformation[BlockExchangerInteriorPart.NUM_BLOCK_TYPES];
	
	static {
		try {
			Map<String, CCModel> pipeModels = CCModel.parseObjModels(BRLoader.class.getResourceAsStream("/assets/bigreactors/models/ExchangerPipe.obj"),
												7, new Scale(1/16f));
			BRLog.info("PipeRenderer loaded %d models, with names: %s", pipeModels.size(), pipeModels.keySet().toString());
			base = pipeModels.get("base").backfacedCopy();
			compute(base);
			
			connector[5] = pipeModels.get("connector").backfacedCopy();
			calculateSidedModels(connector);
			
			pipe[5] = pipeModels.get("pipe").backfacedCopy();
			calculateSidedModels(pipe);
		}
		catch(Throwable _) { _.printStackTrace(); }
	}
	
	private static void calculateSidedModels(CCModel[] m) {
		compute(m[4] = m[5].copy().apply(new Rotation(Math.PI * 1.0, 0, 1, 0)));
		compute(m[3] = m[5].copy().apply(new Rotation(Math.PI * -.5, 0, 1, 0)));
		compute(m[2] = m[5].copy().apply(new Rotation(Math.PI * 0.5, 0, 1, 0)));
		compute(m[1] = m[5].copy().apply(new Rotation(Math.PI * 0.5, 0, 0, 1).with(new Rotation(Math.PI, 0, 1, 0))));
		compute(m[0] = m[5].copy().apply(new Rotation(Math.PI * -.5, 0, 0, 1)));
		compute(m[5]);
	}

	private static void compute(CCModel m) {
		m.computeNormals();
		m.apply(new Translation(0.5, 0.5, 0.5));
		m.computeLighting(LightModel.standardLightModel);
		m.shrinkUVs(RenderHelper.RENDER_OFFSET);
	}
	
	public static void updateUVT(int metadata, IIcon icon) {
		uvt[metadata] = new IconTransformation(icon);
	}
	
	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelId,
			RenderBlocks renderer) {
		CCRenderState.reset();
		CCRenderState.useNormals = true;
		
		GL11.glTranslatef(-0.5f, -0.5f, -0.5f);
		Tessellator.instance.startDrawingQuads();
		base.render(uvt[metadata]);
		pipe[2].render(uvt[metadata]);
		pipe[3].render(uvt[metadata]);
		Tessellator.instance.draw();
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z,
			Block block, int modelId, RenderBlocks renderer) {
		CCRenderState.reset();
		CCRenderState.useNormals = true;
		CCRenderState.alphaOverride = 0xff;
		
		int localMetadata = world.getBlockMetadata(x, y, z);
		int brightness = block.getMixedBrightnessForBlock(world, x, y, z);
		
		Tessellator t = Tessellator.instance;
		t.setColorOpaque_F(1, 1, 1);
		t.setBrightness(brightness);
		t.addTranslation(x, y, z);
		
		base.render(uvt[localMetadata]);
		for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			Block remoteBlock = world.getBlock(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ);
			int remoteMetadata = world.getBlockMetadata(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ);
			if(remoteBlock == block && remoteMetadata == localMetadata) {
				pipe[dir.ordinal()].render(uvt[localMetadata]);
			}
			else if(remoteBlock == BigReactors.blockExchangerPart && BigReactors.blockExchangerPart.isFluidPort(remoteMetadata)) {
				connector[dir.ordinal()].render(uvt[localMetadata]);
			}
		}
		
		t.addTranslation(-x, -y, -z);
		return true;
	}

	@Override
	public boolean shouldRender3DInInventory(int modelId) {
		return true;
	}

	@Override
	public int getRenderId() {
		return BlockExchangerInteriorPart.renderId;
	}

}

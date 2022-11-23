package am2.client.blocks.render;

import am2.client.texture.SpellIconManager;
import am2.common.blocks.tileentity.TileEntityCraftingAltar;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

import static net.minecraft.client.renderer.texture.TextureMap.LOCATION_BLOCKS_TEXTURE;

public class TileCraftingAltarRenderer extends TileEntitySpecialRenderer<TileEntityCraftingAltar> {
	
	private IBakedModel model;
	private IBlockState prevState;
	private TextureAtlasSprite def;
	private TextureAtlasSprite runeStone;

	@Override
	public void render(TileEntityCraftingAltar te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		Minecraft.getMinecraft().profiler.startSection("crafting-altar");
		Minecraft.getMinecraft().profiler.startSection("definitions");
		if (def == null)
			def = SpellIconManager.INSTANCE.getSprite("CasterRuneSide");
		if (runeStone == null)
			runeStone = SpellIconManager.INSTANCE.getSprite("RuneStone");
		if (te.getMimicState() != prevState || model == null) {
			model = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(te.getMimicState());
		}
		Minecraft.getMinecraft().profiler.endSection();
		BlockPos pos = te.getPos();
		GL11.glPushMatrix();
		Tessellator t = Tessellator.getInstance();
		GL11.glTranslated(x, y, z);
		RenderHelper.disableStandardItemLighting();
		Minecraft.getMinecraft().profiler.startSection("block-render");
		if (te.isStructureValid() && te.getMimicState() != null) {
			Minecraft.getMinecraft().profiler.startSection("pre-check");
			GlStateManager.pushMatrix();
			t.getBuffer().begin(7, DefaultVertexFormats.BLOCK);
			GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			GlStateManager.translate(-pos.getX(), -pos.getY(), -pos.getZ());
			Minecraft.getMinecraft().renderEngine.bindTexture(LOCATION_BLOCKS_TEXTURE);
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			Minecraft.getMinecraft().profiler.endStartSection("buffering");
			Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer().renderModel(Minecraft.getMinecraft().world, model, te.getMimicState(), pos, t.getBuffer(), false);
			Minecraft.getMinecraft().profiler.endStartSection("drawing");
			t.draw();
			GlStateManager.popMatrix();
			Minecraft.getMinecraft().profiler.endSection();
		} else {
			Minecraft.getMinecraft().profiler.startSection("raw-render");
			render(te, def);
			Minecraft.getMinecraft().profiler.endSection();
		}
		Minecraft.getMinecraft().profiler.endSection();
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		GL11.glTranslated(-0.005, -0.005, -0.005);
		GL11.glScaled(1.01, 1.01, 1.01);
		render(te, runeStone);
		GlStateManager.disableBlend();
		RenderHelper.enableStandardItemLighting();
		GL11.glPopMatrix();
		Minecraft.getMinecraft().profiler.endSection();
	}
	
	public void render(TileEntityCraftingAltar te, TextureAtlasSprite sprite) {
		if (sprite != null)
			Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		float minU = (sprite != null ? sprite.getMinU() : 0F);
		float maxU = (sprite != null ? sprite.getMaxU() : 1F);
		float minV = (sprite != null ? sprite.getMinV() : 0F);
		float maxV = (sprite != null ? sprite.getMaxV() : 1F);
		//LogHelper.info(sprite);
		GL11.glPushMatrix();
		RenderHelper.disableStandardItemLighting();
		//GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
		buffer.pos(0, 1, 0).tex(maxU, minV).endVertex();
		buffer.pos(1, 1, 0).tex(minU, minV).endVertex();
		buffer.pos(1, 0, 0).tex(minU, maxV).endVertex();
		buffer.pos(0, 0, 0).tex(maxU, maxV).endVertex();
		
		buffer.pos(1, 0, 1).tex(minU, maxV).endVertex();
		buffer.pos(1, 1, 1).tex(minU, minV).endVertex();
		buffer.pos(0, 1, 1).tex(maxU, minV).endVertex();
		buffer.pos(0, 0, 1).tex(maxU, maxV).endVertex();
		tessellator.draw();
		
		buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
		buffer.pos(1, 0, 0).tex(maxU, maxV).endVertex();
		buffer.pos(1, 0, 1).tex(maxU, minV).endVertex();
		buffer.pos(0, 0, 1).tex(minU, minV).endVertex();
		buffer.pos(0, 0, 0).tex(minU, maxV).endVertex();
		
		buffer.pos(0, 1, 1).tex(minU, maxV).endVertex();
		buffer.pos(1, 1, 1).tex(maxU, maxV).endVertex();
		buffer.pos(1, 1, 0).tex(maxU, minV).endVertex();
		buffer.pos(0, 1, 0).tex(minU, minV).endVertex();
		tessellator.draw();
				
		buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
		buffer.pos(1, 1, 0).tex(maxU, minV).endVertex();
		buffer.pos(1, 1, 1).tex(minU, minV).endVertex();
		buffer.pos(1, 0, 1).tex(minU, maxV).endVertex();
		buffer.pos(1, 0, 0).tex(maxU, maxV).endVertex();
		
		buffer.pos(0, 0, 1).tex(minU, maxV).endVertex();
		buffer.pos(0, 1, 1).tex(minU, minV).endVertex();
		buffer.pos(0, 1, 0).tex(maxU, minV).endVertex();
		buffer.pos(0, 0, 0).tex(maxU, maxV).endVertex();
		tessellator.draw();
		
		RenderHelper.enableStandardItemLighting();
		GL11.glPopMatrix();
	}

}

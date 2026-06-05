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
    private TextureAtlasSprite def;
    private TextureAtlasSprite runeStone;

    @Override
    public void render(TileEntityCraftingAltar te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        Minecraft.getMinecraft().profiler.startSection("crafting-altar");
        Minecraft.getMinecraft().profiler.startSection("definitions");
        if (def == null)
            def = SpellIconManager.INSTANCE.getSprite("caster_rune_side");
        if (runeStone == null)
            runeStone = SpellIconManager.INSTANCE.getSprite("rune_stone");
        if (model == null) {
            model = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(te.getMimicState());
        }
        Minecraft.getMinecraft().profiler.endSection();
        BlockPos pos = te.getPos();

        GL11.glPushMatrix();
        GlStateManager.disableLighting();

        Tessellator t = Tessellator.getInstance();
        GL11.glTranslated(x, y, z);

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
        }
        else {
            Minecraft.getMinecraft().profiler.startSection("raw-render");
            render(te, def);
            Minecraft.getMinecraft().profiler.endSection();
        }
        Minecraft.getMinecraft().profiler.endSection();
        GL11.glEnable(GL11.GL_BLEND);
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL);
        GL11.glPolygonOffset(-1.0f, -1.0f);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.85F);
        render(te, runeStone);
        GL11.glPolygonOffset(0.0f, 0.0f);
        GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);

        GL11.glDisable(GL11.GL_BLEND);
        GlStateManager.enableLighting();

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

        GL11.glPushMatrix();
        RenderHelper.disableStandardItemLighting();

        Tessellator t = Tessellator.getInstance();
        BufferBuilder b = t.getBuffer();
        b.begin(7, DefaultVertexFormats.POSITION_TEX);
        b.pos(0, 1, 0).tex(maxU, minV).endVertex();
        b.pos(1, 1, 0).tex(minU, minV).endVertex();
        b.pos(1, 0, 0).tex(minU, maxV).endVertex();
        b.pos(0, 0, 0).tex(maxU, maxV).endVertex();

        b.pos(1, 0, 1).tex(minU, maxV).endVertex();
        b.pos(1, 1, 1).tex(minU, minV).endVertex();
        b.pos(0, 1, 1).tex(maxU, minV).endVertex();
        b.pos(0, 0, 1).tex(maxU, maxV).endVertex();
        t.draw();

        b.begin(7, DefaultVertexFormats.POSITION_TEX);
        b.pos(1, 0, 0).tex(maxU, maxV).endVertex();
        b.pos(1, 0, 1).tex(maxU, minV).endVertex();
        b.pos(0, 0, 1).tex(minU, minV).endVertex();
        b.pos(0, 0, 0).tex(minU, maxV).endVertex();

        b.pos(0, 1, 1).tex(minU, maxV).endVertex();
        b.pos(1, 1, 1).tex(maxU, maxV).endVertex();
        b.pos(1, 1, 0).tex(maxU, minV).endVertex();
        b.pos(0, 1, 0).tex(minU, minV).endVertex();
        t.draw();

        b.begin(7, DefaultVertexFormats.POSITION_TEX);
        b.pos(1, 1, 0).tex(maxU, minV).endVertex();
        b.pos(1, 1, 1).tex(minU, minV).endVertex();
        b.pos(1, 0, 1).tex(minU, maxV).endVertex();
        b.pos(1, 0, 0).tex(maxU, maxV).endVertex();

        b.pos(0, 0, 1).tex(minU, maxV).endVertex();
        b.pos(0, 1, 1).tex(minU, minV).endVertex();
        b.pos(0, 1, 0).tex(maxU, minV).endVertex();
        b.pos(0, 0, 0).tex(maxU, maxV).endVertex();
        t.draw();

        RenderHelper.enableStandardItemLighting();
        GL11.glPopMatrix();
    }

}

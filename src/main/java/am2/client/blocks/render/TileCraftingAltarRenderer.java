package am2.client.blocks.render;

import am2.client.texture.SpellIconManager;
import am2.common.blocks.tileentity.TileEntityCraftingAltar;
import am2.common.blocks.tileentity.TileEntityEverstone;
import am2.common.registry.AMBlocks;
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
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

import static net.minecraft.client.renderer.texture.TextureMap.LOCATION_BLOCKS_TEXTURE;

public class TileCraftingAltarRenderer extends TileEntitySpecialRenderer<TileEntityCraftingAltar> {

    private TextureAtlasSprite runeStone;

    @Override
    public void render(TileEntityCraftingAltar te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        Minecraft.getMinecraft().profiler.startSection("crafting-altar");
        Minecraft.getMinecraft().profiler.startSection("definitions");
        if (runeStone == null)
            runeStone = SpellIconManager.INSTANCE.getSprite("rune_stone");
        Minecraft.getMinecraft().profiler.endSection();

        BlockPos pos = te.getPos();
        IBlockState state = te.isStructureValid() && te.getMimicState() != null ? te.getMimicState() : AMBlocks.crafting_altar.getDefaultState();
        IBakedModel model = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(state);

        Minecraft.getMinecraft().profiler.startSection("block-render");
        Minecraft.getMinecraft().renderEngine.bindTexture(LOCATION_BLOCKS_TEXTURE);

        GlStateManager.pushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.translate(x, y, z);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.pushMatrix();
        GlStateManager.translate(-pos.getX(), -pos.getY(), -pos.getZ());

        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        Tessellator t = Tessellator.getInstance();
        t.getBuffer().begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
        Minecraft.getMinecraft().profiler.endStartSection("buffering");
        model = damageModel(model, te.getMimicState(), pos, destroyStage);
        Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer().renderModel(Minecraft.getMinecraft().world, model, state, pos, t.getBuffer(), false);
        Minecraft.getMinecraft().profiler.endStartSection("drawing");
        t.draw();

        GlStateManager.popMatrix();

        renderRune(te);

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
        Minecraft.getMinecraft().profiler.endSection();
    }

    private static IBakedModel damageModel(IBakedModel model, IBlockState state, BlockPos pos, int destroyStage) {
        if (state == null || destroyStage < 0)
            return model;
        TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("minecraft:blocks/destroy_stage_" + destroyStage);
        return net.minecraftforge.client.ForgeHooksClient.getDamageModel(model, sprite, state, Minecraft.getMinecraft().world, pos);
    }

    public void renderRune(TileEntityCraftingAltar te) {
        TextureAtlasSprite sprite = runeStone;
        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        Minecraft.getMinecraft().profiler.endStartSection("rune-render");

        GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL);
        GL11.glPolygonOffset(-1.0F, -1.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        float minU = (sprite != null ? sprite.getMinU() : 0F);
        float maxU = (sprite != null ? sprite.getMaxU() : 1F);
        float minV = (sprite != null ? sprite.getMinV() : 0F);
        float maxV = (sprite != null ? sprite.getMaxV() : 1F);

        GlStateManager.pushMatrix();
        RenderHelper.disableStandardItemLighting();

        Tessellator t = Tessellator.getInstance();
        final VertexFormat fmt = DefaultVertexFormats.POSITION_TEX;

        BufferBuilder b = t.getBuffer();
        b.begin(GL11.GL_QUADS, fmt);
        vertex(b,0, 1, 0, maxU, minV);
        vertex(b,1, 1, 0, minU, minV);
        vertex(b,1, 0, 0, minU, maxV);
        vertex(b,0, 0, 0, maxU, maxV);

        vertex(b,1, 0, 1, minU, maxV);
        vertex(b,1, 1, 1, minU, minV);
        vertex(b,0, 1, 1, maxU, minV);
        vertex(b,0, 0, 1, maxU, maxV);
        t.draw();

        b.begin(GL11.GL_QUADS, fmt);
        vertex(b,1, 0, 0, maxU, maxV);
        vertex(b,1, 0, 1, maxU, minV);
        vertex(b,0, 0, 1, minU, minV);
        vertex(b,0, 0, 0, minU, maxV);

        vertex(b,0, 1, 1, minU, maxV);
        vertex(b,1, 1, 1, maxU, maxV);
        vertex(b,1, 1, 0, maxU, minV);
        vertex(b,0, 1, 0, minU, minV);
        t.draw();

        b.begin(GL11.GL_QUADS, fmt);
        vertex(b,1, 1, 0, maxU, minV);
        vertex(b,1, 1, 1, minU, minV);
        vertex(b,1, 0, 1, minU, maxV);
        vertex(b,1, 0, 0, maxU, maxV);

        vertex(b,0, 0, 1, minU, maxV);
        vertex(b,0, 1, 1, minU, minV);
        vertex(b,0, 1, 0, maxU, minV);
        vertex(b,0, 0, 0, maxU, maxV);
        t.draw();

        RenderHelper.enableStandardItemLighting();
        GL11.glPolygonOffset(0.0f, 0.0f);
        GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);
        GL11.glDisable(GL11.GL_BLEND);

        GlStateManager.popMatrix();
    }

    private void vertex(BufferBuilder b, double x, double y, double z, double u, double v) {
        b.pos(x, y, z).tex(u, v).endVertex();
    }

}

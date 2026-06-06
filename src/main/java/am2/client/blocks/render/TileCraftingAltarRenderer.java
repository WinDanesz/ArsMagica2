package am2.client.blocks.render;

import am2.client.texture.SpellIconManager;
import am2.common.blocks.tileentity.TileEntityCraftingAltar;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import org.lwjgl.opengl.GL11;

public class TileCraftingAltarRenderer extends TileEntitySpecialRenderer<TileEntityCraftingAltar> {

    private TextureAtlasSprite runeStone;

    @Override
    public void render(TileEntityCraftingAltar te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (runeStone == null)
            runeStone = SpellIconManager.INSTANCE.getSprite("rune_stone");

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        TextureAtlasSprite sprite = runeStone;
        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL);
        GL11.glPolygonOffset(-1.0F, -1.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glEnable(GL11.GL_BLEND);
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        float minU = (sprite != null ? sprite.getMinU() : 0F);
        float maxU = (sprite != null ? sprite.getMaxU() : 1F);
        float minV = (sprite != null ? sprite.getMinV() : 0F);
        float maxV = (sprite != null ? sprite.getMaxV() : 1F);

        RenderHelper.disableStandardItemLighting();

        Tessellator t = Tessellator.getInstance();
        final VertexFormat fmt = DefaultVertexFormats.POSITION_TEX;

        Minecraft.getMinecraft().profiler.startSection("rune-render");
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
        Minecraft.getMinecraft().profiler.endSection();

        RenderHelper.enableStandardItemLighting();
        GL11.glPolygonOffset(0.0f, 0.0f);
        GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);
        GL11.glDisable(GL11.GL_BLEND);

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }

    private void vertex(BufferBuilder b, double x, double y, double z, double u, double v) {
        b.pos(x, y, z).tex(u, v).endVertex();
    }

}

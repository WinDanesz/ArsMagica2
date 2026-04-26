package am2.client.blocks.render;

import am2.api.affinity.Affinity;
import am2.common.blocks.tileentity.TileEntityGroundRuneSpell;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

public class TileRuneRenderer extends TileEntitySpecialRenderer<TileEntityGroundRuneSpell> {

    @Override
    public void render(TileEntityGroundRuneSpell te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (te.getSpell() != null) {
            Affinity mainAff = te.getSpell().getMainShift();
            TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(new ResourceLocation(mainAff.getRegistryName().getNamespace(), "blocks/runes/rune_" + mainAff.getRegistryName().getPath()).toString());
            Tessellator tesselator = Tessellator.getInstance();
            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            GlStateManager.enableBlend();
            Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            RenderHelper.disableStandardItemLighting();
            int i = 0xF00F0;
            int j = i >> 16 & 65535;
            int k = i & 65535;
            float height = 0.03125F;
            BufferBuilder builder = tesselator.getBuffer();
            builder.begin(7, DefaultVertexFormats.POSITION_TEX_LMAP_COLOR);

            float minU = sprite.getMinU();
            float minV = sprite.getMinV();
            float maxU = sprite.getMaxU();
            float maxV = sprite.getMaxV();

            builder.pos(1, 0, 0).tex(maxU, maxV).lightmap(j, k).color(1.0f, 1.0f, 1.0f, 1.0f).endVertex();
            builder.pos(1, 0, 1).tex(maxU, minV).lightmap(j, k).color(1.0f, 1.0f, 1.0f, 1.0f).endVertex();
            builder.pos(0, 0, 1).tex(minU, minV).lightmap(j, k).color(1.0f, 1.0f, 1.0f, 1.0f).endVertex();
            builder.pos(0, 0, 0).tex(minU, maxV).lightmap(j, k).color(1.0f, 1.0f, 1.0f, 1.0f).endVertex();

            builder.pos(0, height, 1).tex(minU, maxV).lightmap(j, k).color(1.0f, 1.0f, 1.0f, 1.0f).endVertex();
            builder.pos(1, height, 1).tex(maxU, maxV).lightmap(j, k).color(1.0f, 1.0f, 1.0f, 1.0f).endVertex();
            builder.pos(1, height, 0).tex(maxU, minV).lightmap(j, k).color(1.0f, 1.0f, 1.0f, 1.0f).endVertex();
            builder.pos(0, height, 0).tex(minU, minV).lightmap(j, k).color(1.0f, 1.0f, 1.0f, 1.0f).endVertex();
            tesselator.draw();
            GlStateManager.disableBlend();
            RenderHelper.enableStandardItemLighting();
            GlStateManager.popMatrix();
        }
    }

}

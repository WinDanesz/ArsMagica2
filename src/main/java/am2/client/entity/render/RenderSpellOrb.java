package am2.client.entity.render;

import am2.client.particles.AMParticleIcons;
import am2.common.entity.EntitySpellOrb;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class RenderSpellOrb extends Render<EntitySpellOrb> {

    public RenderSpellOrb(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(EntitySpellOrb entity, double d0, double d1, double d2, float f, float f1) {
        doRenderSpellOrb((EntitySpellOrb) entity, d0, d1, d2, f, f1);
    }

    private void doRenderSpellOrb(EntitySpellOrb entity, double d, double d1, double d2, float f, float f1) {
        TextureAtlasSprite sprite = AMParticleIcons.instance.getIconByName(entity.getIcon());
        if (sprite == null) {
            // Fallback to a default sprite if icon lookup fails
            sprite = Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite();
        }

        GlStateManager.pushMatrix();
        GlStateManager.pushAttrib();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableLighting();
        GlStateManager.disableCull();

        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        int renderColor = entity.getColor();

        GlStateManager.translate(d, d1, d2);
        GlStateManager.scale(0.5f, 0.5f, 0.5f);
        GlStateManager.rotate(180F - renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-renderManager.playerViewX, Minecraft.getMinecraft().gameSettings.thirdPersonView == 2 ? -1.0F : 1.0F, 0.0F, 0.0F);

        renderIcon(sprite, renderColor);

        GlStateManager.popAttrib();
        GlStateManager.popMatrix();
    }

    private void renderIcon(TextureAtlasSprite sprite, int renderColor) {
        Tessellator tessellator = Tessellator.getInstance();
        float size = 1.0F;
        float halfSize = 0.5F;

        // Extract color components
        int red = (renderColor >> 16) & 0xFF;
        int green = (renderColor >> 8) & 0xFF;
        int blue = renderColor & 0xFF;

        tessellator.getBuffer().begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        tessellator.getBuffer().pos(-halfSize, -halfSize, 0.0D).tex(sprite.getMinU(), sprite.getMaxV()).color(red, green, blue, 255).endVertex();
        tessellator.getBuffer().pos(halfSize, -halfSize, 0.0D).tex(sprite.getMaxU(), sprite.getMaxV()).color(red, green, blue, 255).endVertex();
        tessellator.getBuffer().pos(halfSize, halfSize, 0.0D).tex(sprite.getMaxU(), sprite.getMinV()).color(red, green, blue, 255).endVertex();
        tessellator.getBuffer().pos(-halfSize, halfSize, 0.0D).tex(sprite.getMinU(), sprite.getMinV()).color(red, green, blue, 255).endVertex();
        tessellator.draw();
    }

    @Override
    protected ResourceLocation getEntityTexture(EntitySpellOrb entity) {
        return TextureMap.LOCATION_BLOCKS_TEXTURE;
    }

}

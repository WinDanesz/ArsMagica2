package am2.client.entity.render;

import am2.client.particles.AMParticleIcons;
import am2.common.entity.EntitySpellProjectile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class RenderSpellProjectile extends Render<EntitySpellProjectile> {

    public RenderSpellProjectile(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(EntitySpellProjectile entity, double d0, double d1, double d2, float f, float f1) {
        doRenderSpellProjectile((EntitySpellProjectile) entity, d0, d1, d2, f, f1);
    }

    private void doRenderSpellProjectile(EntitySpellProjectile entity, double d, double d1, double d2, float f, float f1) {
        TextureAtlasSprite sprite = AMParticleIcons.instance.getIconByName(entity.getIcon());
        if (sprite == null) {
            // Fallback to a default sprite if icon lookup fails
            sprite = Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite();
        }

        GL11.glPushMatrix();
        GL11.glPushAttrib(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_CULL_FACE);

        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        int renderColor = entity.getColor();

        GL11.glTranslated(d, d1, d2);
        GL11.glScalef(0.5f, 0.5f, 0.5f);
        GL11.glRotatef(180F - renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-renderManager.playerViewX, 1.0F, 0.0F, 0.0F);

        renderIcon(sprite, renderColor);

        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glPopAttrib();
        GL11.glPopMatrix();
    }

    private void renderIcon(TextureAtlasSprite sprite, int renderColor) {
        Tessellator tessellator = Tessellator.getInstance();
        float size = 1.0F;
        float halfSize = 0.5F;

        // Fix projectile texture warping: Extract color components safely
        int red = Math.max(0, Math.min(255, (renderColor & 0xFF0000) >> 16));
        int green = Math.max(0, Math.min(255, (renderColor & 0x00FF00) >> 8));
        int blue = Math.max(0, Math.min(255, renderColor & 0x0000FF));

        tessellator.getBuffer().begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        tessellator.getBuffer().pos(-halfSize, -halfSize, 0.0D).tex(sprite.getMinU(), sprite.getMaxV()).color(red, green, blue, 255).endVertex();
        tessellator.getBuffer().pos(halfSize, -halfSize, 0.0D).tex(sprite.getMaxU(), sprite.getMaxV()).color(red, green, blue, 255).endVertex();
        tessellator.getBuffer().pos(halfSize, halfSize, 0.0D).tex(sprite.getMaxU(), sprite.getMinV()).color(red, green, blue, 255).endVertex();
        tessellator.getBuffer().pos(-halfSize, halfSize, 0.0D).tex(sprite.getMinU(), sprite.getMinV()).color(red, green, blue, 255).endVertex();
        tessellator.draw();
    }

    @Override
    protected ResourceLocation getEntityTexture(EntitySpellProjectile entity) {
        return TextureMap.LOCATION_BLOCKS_TEXTURE;
    }

}

package am2.client.blocks.render;

import am2.common.blocks.tileentity.TileEntityFlickerHabitat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class TileFlickerHabitatRenderer extends TileEntitySpecialRenderer<TileEntityFlickerHabitat> {

    private static final ResourceLocation ORB_TEXTURE =
            new ResourceLocation("arsmagica2", "textures/items/particles/ember.png");

    /** Full orbit period in milliseconds */
    private static final float ORBIT_PERIOD_MS = 3000f;
    /** Orbit radius in block units from center (0.5, 0.5) */
    private static final float ORBIT_RADIUS = 0.22f;
    /** Vertical bob amplitude */
    private static final float BOB_AMPLITUDE = 0.05f;
    /** Y center of orbit */
    private static final float ORBIT_Y = 0.52f;

    private static final float GLOW_RADIUS = 0.32f;
    private static final float MID_RADIUS   = 0.20f;
    private static final float CORE_RADIUS  = 0.10f;

    @Override
    public void render(TileEntityFlickerHabitat te, double x, double y, double z,
                       float partialTicks, int destroyStage, float alpha) {
        if (!te.hasFlicker()) return;

        int[] colors = te.getHabitatColors();
        if (colors.length == 0) return;

        Entity viewer = Minecraft.getMinecraft().getRenderViewEntity();
        if (viewer == null) return;

        // Compute orbit angle from wall clock — smooth, independent of tick rate
        float t = (System.currentTimeMillis() % (long) ORBIT_PERIOD_MS) / ORBIT_PERIOD_MS;
        float angle = (float) (t * 2.0 * Math.PI);

        int count = colors.length;
        // Camera angles for billboard facing
        float yaw   = viewer.prevRotationYaw   + (viewer.rotationYaw   - viewer.prevRotationYaw)   * partialTicks;
        float pitch = viewer.prevRotationPitch + (viewer.rotationPitch - viewer.prevRotationPitch) * partialTicks;

        Minecraft.getMinecraft().renderEngine.bindTexture(ORB_TEXTURE);

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);

        GlStateManager.disableLighting();
        // Force full-brightness lightmap so the orbs are not dimmed by world light level
        float savedLightX = OpenGlHelper.lastBrightnessX;
        float savedLightY = OpenGlHelper.lastBrightnessY;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.depthMask(false);
        GlStateManager.disableCull();
        GlStateManager.color(1f, 1f, 1f, 1f); // clear any inherited tint

        Tessellator tess = Tessellator.getInstance();

        for (int i = 0; i < count; ++i) {
            float phase = angle + (float) (2.0 * Math.PI * i / count);
            float ox = 0.5f + ORBIT_RADIUS * (float) Math.cos(phase);
            float oz = 0.5f + ORBIT_RADIUS * (float) Math.sin(phase);
            float oy = ORBIT_Y + BOB_AMPLITUDE * (float) Math.sin(angle * 2 + phase);

            int color = colors[i];
            float r = ((color >> 16) & 0xFF) / 255.0f;
            float g = ((color >> 8)  & 0xFF) / 255.0f;
            float b = (color         & 0xFF) / 255.0f;

            renderOrb(tess, ox, oy, oz, yaw, pitch, r, g, b);
        }

        // Restore GL state
        GlStateManager.enableCull();
        GlStateManager.depthMask(true);
        GlStateManager.disableBlend();
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit,
                savedLightX, savedLightY);
        GlStateManager.enableLighting();

        GlStateManager.popMatrix();
    }

    private void renderOrb(Tessellator tess, float bx, float by, float bz,
                           float yaw, float pitch, float r, float g, float b) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(bx, by, bz);

        GL11.glRotatef(-yaw,  0f, 1f, 0f);
        GL11.glRotatef(pitch, 1f, 0f, 0f);

        // Three layers: soft outer glow, mid glow, bright core — additive so they stack
        drawQuad(tess, GLOW_RADIUS, r, g, b, 0.55f);
        drawQuad(tess, MID_RADIUS,  r, g, b, 0.80f);
        drawQuad(tess, CORE_RADIUS, r, g, b, 1.00f);

        GlStateManager.popMatrix();
    }

    private void drawQuad(Tessellator tess, float size, float r, float g, float b, float a) {
        BufferBuilder bb = tess.getBuffer();
        bb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_LMAP_COLOR);
        bb.pos(-size, -size, 0).tex(0, 1).lightmap(240, 240).color(r, g, b, a).endVertex();
        bb.pos( size, -size, 0).tex(1, 1).lightmap(240, 240).color(r, g, b, a).endVertex();
        bb.pos( size,  size, 0).tex(1, 0).lightmap(240, 240).color(r, g, b, a).endVertex();
        bb.pos(-size,  size, 0).tex(0, 0).lightmap(240, 240).color(r, g, b, a).endVertex();
        tess.draw();
    }
}

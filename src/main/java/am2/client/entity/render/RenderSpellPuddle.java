package am2.client.entity.render;

import am2.common.entity.EntitySpellPuddle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.Random;

/**
 * Renders the {@link EntitySpellPuddle} as a flat, coloured disc lying on the ground.
 * Visual design is inspired by Electroblob's Wizardry RenderDecay.
 *
 * Rendering technique:
 *  - Translate to the entity position.
 *  - Rotate -90° around X so the XY-plane becomes horizontal (matching the XZ world plane).
 *  - Draw a GL_TRIANGLE_FAN circle in the XY plane, coloured using the puddle's stored affinity colour.
 *  - Alpha fades in during the first FADE_TICKS and out during the last FADE_TICKS.
 */
@SideOnly(Side.CLIENT)
public class RenderSpellPuddle extends Render<EntitySpellPuddle> {

    private static final int CIRCLE_SEGMENTS = 40;
    private static final int FADE_TICKS = 10;
    /** Ticks over which the puddle grows from nothing to its full radius on spawn. */
    private static final int GROW_TICKS = 20;

    public RenderSpellPuddle(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(EntitySpellPuddle entity, double x, double y, double z,
                         float entityYaw, float partialTicks) {

        int lifetime = entity.getLifetime();
        if (lifetime <= 0) return;

        // Compute alpha:  0→1 during first FADE_TICKS,  1→0 during last FADE_TICKS.
        float te = entity.ticksExisted + partialTicks;
        float alpha;
        if (te < FADE_TICKS) {
            alpha = te / FADE_TICKS;
        } else if (lifetime - te < FADE_TICKS) {
            alpha = (lifetime - te) / FADE_TICKS;
        } else {
            alpha = 1.0f;
        }
        alpha = Math.max(0f, Math.min(1f, alpha));

        // Decode colour components
        int packed = entity.getColor();
        int r = (packed >> 16) & 0xFF;
        int g = (packed >>  8) & 0xFF;
        int b =  packed        & 0xFF;

        float radius = entity.getRadius();

        // Grow animation: cubic ease-out from 0 → full radius over GROW_TICKS.
        if (te < GROW_TICKS) {
            float t = te / GROW_TICKS;
            float growScale = 1f - (1f - t) * (1f - t) * (1f - t); // ease-out cubic
            radius *= growScale;
        }

        // Generate irregular edge radii seeded by entity ID so the shape is
        // consistent across frames but unique per puddle.
        Random rng = new Random(entity.getEntityId() * 6364136223846793005L);
        float phase1 = rng.nextFloat() * (float)(2 * Math.PI);
        float phase2 = rng.nextFloat() * (float)(2 * Math.PI);
        float phase3 = rng.nextFloat() * (float)(2 * Math.PI);
        float phase4 = rng.nextFloat() * (float)(2 * Math.PI);
        // Base shape: lopsided ellipse-ish; higher harmonics add fine bumpiness
        float squishX = 0.85f + rng.nextFloat() * 0.3f; // 0.85–1.15
        float squishZ = 0.85f + rng.nextFloat() * 0.3f;
        float[] segRadii = new float[CIRCLE_SEGMENTS + 1];
        for (int i = 0; i <= CIRCLE_SEGMENTS; i++) {
            double angle = i * 2.0 * Math.PI / CIRCLE_SEGMENTS;
            // Elliptical base
            double ex = Math.cos(angle) * squishX;
            double ey = Math.sin(angle) * squishZ;
            float baseR = (float) Math.sqrt(ex * ex + ey * ey);
            // Organic bumps via multiple harmonics
            float bumps = (float)(
                0.08 * Math.sin(3 * angle + phase1) +
                0.05 * Math.sin(5 * angle + phase2) +
                0.03 * Math.sin(7 * angle + phase3) +
                0.02 * Math.cos(11 * angle + phase4)
            );
            segRadii[i] = radius * baseR * (1.0f + bumps);
        }
        // Close the loop
        segRadii[CIRCLE_SEGMENTS] = segRadii[0];

        GlStateManager.pushMatrix();
        GlStateManager.pushAttrib();

        GlStateManager.translate(x, y, z);

        // Lay disc flat on the ground – rotate the XY-plane to become horizontal
        GlStateManager.rotate(-90f, 1f, 0f, 0f);

        // After the -90° X rotation, local +Z points world-up, so +0.01 lifts
        // the disc just above the surface to prevent z-fighting with the block.
        GlStateManager.translate(0f, 0f, 0.01f);

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableLighting();
        GlStateManager.disableTexture2D();
        GlStateManager.disableCull();
        GlStateManager.depthMask(false);
  
        // Use the actual light level at the puddle's position rather than forcing max brightness
        int combinedLight = entity.getBrightnessForRender();
        int lightU = combinedLight & 0xFFFF;
        int lightV = (combinedLight >> 16) & 0xFFFF;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lightU, lightV);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        // In the rotated coordinate system local +Z = world up.
        // The puddle bottom sits at z=0 (ground surface); the top face and side walls
        // rise to z=+WALL_HEIGHT so they are fully above the block surface.
        final float WALL_HEIGHT = 0.06f;

        // Top face: concentric rings from dark centre to full colour at edge.
        // Each ring has a uniform colour – no reliance on GPU interpolation.
        final int N_RINGS = 32;
        int fullAlpha = (int)(alpha * 255);

        // Reset any global tint Minecraft's pipeline may have left so per-vertex colours are pure.
        GlStateManager.color(1f, 1f, 1f, 1f);

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        for (int ring = 0; ring < N_RINGS; ring++) {
            float t0 = (float)  ring      / N_RINGS;
            float t1 = (float)(ring + 1)  / N_RINGS;
            // Cubic curve: stays near 1.0 for most of the disc, drops sharply near the edge
            float b0 = 1.0f - 0.65f * t0 * t0 * t0;
            float b1 = 1.0f - 0.65f * t1 * t1 * t1;
            int ri0 = (int)(r * b0), gi0 = (int)(g * b0), bi0 = (int)(b * b0);
            int ri1 = (int)(r * b1), gi1 = (int)(g * b1), bi1 = (int)(b * b1);
            for (int i = 0; i < CIRCLE_SEGMENTS; i++) {
                double a0 = i       * 2.0 * Math.PI / CIRCLE_SEGMENTS;
                double a1 = (i + 1) * 2.0 * Math.PI / CIRCLE_SEGMENTS;
                float iR0 = segRadii[i]     * t0, oR0 = segRadii[i]     * t1;
                float iR1 = segRadii[i + 1] * t0, oR1 = segRadii[i + 1] * t1;
                double cos0 = Math.cos(a0), sin0 = Math.sin(a0);
                double cos1 = Math.cos(a1), sin1 = Math.sin(a1);
                buffer.pos(cos0 * iR0, sin0 * iR0, WALL_HEIGHT).color(ri0, gi0, bi0, fullAlpha).endVertex();
                buffer.pos(cos1 * iR1, sin1 * iR1, WALL_HEIGHT).color(ri0, gi0, bi0, fullAlpha).endVertex();
                buffer.pos(cos1 * oR1, sin1 * oR1, WALL_HEIGHT).color(ri1, gi1, bi1, fullAlpha).endVertex();
                buffer.pos(cos0 * oR0, sin0 * oR0, WALL_HEIGHT).color(ri1, gi1, bi1, fullAlpha).endVertex();
            }
        }
        tessellator.draw();

        // Side walls: match the dark edge colour (1.0 - 0.65 at t=1)
        int re = (int)(r * 0.35f), ge = (int)(g * 0.35f), be = (int)(b * 0.35f);
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        for (int i = 0; i < CIRCLE_SEGMENTS; i++) {
            double angle0 = i       * 2.0 * Math.PI / CIRCLE_SEGMENTS;
            double angle1 = (i + 1) * 2.0 * Math.PI / CIRCLE_SEGMENTS;
            double cx0 = Math.cos(angle0) * segRadii[i];
            double cy0 = Math.sin(angle0) * segRadii[i];
            double cx1 = Math.cos(angle1) * segRadii[i + 1];
            double cy1 = Math.sin(angle1) * segRadii[i + 1];
            buffer.pos(cx0, cy0, 0.0        ).color(re, ge, be, fullAlpha).endVertex();
            buffer.pos(cx1, cy1, 0.0        ).color(re, ge, be, fullAlpha).endVertex();
            buffer.pos(cx1, cy1, WALL_HEIGHT).color(re, ge, be, fullAlpha).endVertex();
            buffer.pos(cx0, cy0, WALL_HEIGHT).color(re, ge, be, fullAlpha).endVertex();
        }
        tessellator.draw();

        GlStateManager.depthMask(true);
        GlStateManager.enableCull();
        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();

        GlStateManager.popAttrib();
        GlStateManager.popMatrix();
    }

    @Override
    protected ResourceLocation getEntityTexture(EntitySpellPuddle entity) {
        return null;
    }
}

package am2.client.entity.render;

import am2.common.entity.EntityAirElemental;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

/** Feathered wind ribbons wrapping the head in horizontal and rising currents. */
public class LayerAirElementalWind implements LayerRenderer<EntityAirElemental> {

    private static final int SEGMENTS = 28;
    private static final float PI = (float) Math.PI;
    private static final float ORBIT_SPEED_MULTIPLIER = 1.75F;
    private static final float RIBBON_WIDTH_MULTIPLIER = 1.20F;
    // Model Y runs downward. The broad middle is framed by smaller end swirls;
    // two finer currents sweep diagonally and almost vertically around the head.
    private static final WindOrbit[] ORBITS = {
            // Radius, height, speed, width, tilt, heading, ribbon count.
            new WindOrbit(3.8F, 8.0F, 0.085F, 0.65F, -10.0F, 0.0F, 2),
            new WindOrbit(8.6F, 0.5F, 0.055F, 1.15F, 7.0F, 50.0F, 2),
            new WindOrbit(3.4F, -7.5F, 0.11F, 0.60F, 8.0F, 100.0F, 2),
            new WindOrbit(7.1F, 0.5F, 0.045F, 0.60F, 52.0F, 35.0F, 1),
            new WindOrbit(7.4F, 0.5F, 0.060F, 0.50F, 82.0F, 110.0F, 1)
    };
    // Sample the pale center of the existing wind texture; geometry supplies the fade.
    private static final float WIND_U = 4.5F / 32.0F;
    private static final float WIND_V = 22.5F / 32.0F;

    private final RenderAirElemental renderer;
    private final ResourceLocation texture;

    public LayerAirElementalWind(RenderAirElemental renderer, ResourceLocation texture) {
        this.renderer = renderer;
        this.texture = texture;
    }

    @Override
    public void doRenderLayer(EntityAirElemental entity, float limbSwing, float limbSwingAmount,
                              float partialTicks, float ageInTicks, float netHeadYaw,
                              float headPitch, float scale) {
        if (entity.isInvisible()) return;

        renderer.bindTexture(texture);
        boolean wasLit = GL11.glIsEnabled(GL11.GL_LIGHTING);
        int shadeModel = GL11.glGetInteger(GL11.GL_SHADE_MODEL);
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        // Alpha testing would cut off the soft edges. Keep depth testing against
        // the head/world, but don't let one translucent ribbon hide another.
        GlStateManager.disableAlpha();
        GlStateManager.depthMask(false);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        try {
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
            appendWind(buffer, ageInTicks, scale);
            tessellator.draw();
        } finally {
            GlStateManager.shadeModel(shadeModel);
            GlStateManager.depthMask(true);
            GlStateManager.enableAlpha();
            GlStateManager.disableBlend();
            if (wasLit) GlStateManager.enableLighting();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    private static void appendWind(BufferBuilder buffer, float ageInTicks, float scale) {
        for (int ring = 0; ring < ORBITS.length; ring++) {
            WindOrbit orbit = ORBITS[ring];
            float tilt = orbit.tilt + MathHelper.sin(ageInTicks * 0.023F + ring) * 0.07F;
            float heading = orbit.heading + ageInTicks * 0.008F;
            float sinTilt = MathHelper.sin(tilt), cosTilt = MathHelper.cos(tilt);
            float sinHeading = MathHelper.sin(heading), cosHeading = MathHelper.cos(heading);
            for (int ribbon = 0; ribbon < orbit.ribbonCount; ribbon++) {
                float phase = ageInTicks * orbit.speed * ORBIT_SPEED_MULTIPLIER + ring * 1.7F + ribbon * PI;
                float drift = MathHelper.sin(ageInTicks * 0.065F + ring * 2.1F + ribbon) * 0.45F;
                for (int segment = 0; segment < SEGMENTS; segment++) {
                    float t0 = segment / (float) SEGMENTS;
                    float t1 = (segment + 1) / (float) SEGMENTS;
                    // Two strips meet at a brighter center and fade to transparent
                    // edges. RenderLiving already disables culling for both sides.
                    for (int side = -1; side < 1; side++) {
                        windVertex(buffer, orbit, ribbon, phase, drift, sinTilt, cosTilt, sinHeading, cosHeading, t0, side, scale);
                        windVertex(buffer, orbit, ribbon, phase, drift, sinTilt, cosTilt, sinHeading, cosHeading, t1, side, scale);
                        windVertex(buffer, orbit, ribbon, phase, drift, sinTilt, cosTilt, sinHeading, cosHeading, t1, side + 1, scale);
                        windVertex(buffer, orbit, ribbon, phase, drift, sinTilt, cosTilt, sinHeading, cosHeading, t0, side + 1, scale);
                    }
                }
            }
        }
    }

    private static void windVertex(BufferBuilder buffer, WindOrbit orbit, int ribbon, float phase,
                                   float drift, float sinTilt, float cosTilt, float sinHeading,
                                   float cosHeading, float t, int side, float scale) {
        float taper = MathHelper.sin(PI * t);
        float angle = phase + (t - 0.5F) * (2.8F + ribbon * 0.3F);
        float width = orbit.width * RIBBON_WIDTH_MULTIPLIER * taper * (0.55F + t * 0.45F);
        float bank = 0.45F + MathHelper.sin(angle * 1.4F + phase * 0.3F) * 0.25F;
        float radius = orbit.radius + (t - 0.5F) * 0.7F
                + MathHelper.sin(angle * 2.0F - phase) * 0.2F;
        // Bank the ribbon gently in 3D so it has a visible curve from above as well.
        radius += side * width * MathHelper.sin(bank);
        float y = -(t - 0.5F) * 3.0F
                + MathHelper.sin(angle - phase * 0.4F) * 0.35F
                + side * width * MathHelper.cos(bank);
        float alpha = side == 0 ? taper * 0.62F : 0.0F;
        float x = MathHelper.cos(angle) * radius;
        float z = MathHelper.sin(angle) * radius;
        // Tilt the entire orbit, then turn its heading around the model. Apply
        // the height last so the small top and bottom swirls stay at the ends.
        float tiltedY = y * cosTilt - z * sinTilt;
        float tiltedZ = y * sinTilt + z * cosTilt;
        buffer.pos((x * cosHeading + tiltedZ * sinHeading) * scale,
                        (orbit.height + drift + tiltedY) * scale,
                        (tiltedZ * cosHeading - x * sinHeading) * scale)
                .tex(WIND_U, WIND_V).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
    }

    private static final class WindOrbit {
        private final float radius, height, speed, width, tilt, heading;
        private final int ribbonCount;

        private WindOrbit(float radius, float height, float speed, float width,
                          float tiltDegrees, float headingDegrees, int ribbonCount) {
            this.radius = radius;
            this.height = height;
            this.speed = speed;
            this.width = width;
            this.tilt = tiltDegrees * PI / 180.0F;
            this.heading = headingDegrees * PI / 180.0F;
            this.ribbonCount = ribbonCount;
        }
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
}

package am2.client.entity.render;

import am2.common.entity.EntitySpellSlice;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/**
 * Renders the Slice spell as a flat textured quad flying through the air.
 * The quad lies in the horizontal plane (forward × right), oriented along
 * the direction of travel, and is tinted by the entity's colour.
 */
public class RenderSpellSlice extends Render<EntitySpellSlice> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("arsmagica2", "textures/entities/slice.png");

    public RenderSpellSlice(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(EntitySpellSlice entity, double x, double y, double z,
                         float entityYaw, float partialTicks) {

        // Colour tint from the entity (affinity colour or player-chosen colour modifier)
        int rawColor = entity.getColor();
        int red   = Math.max(0, Math.min(255, (rawColor >> 16) & 0xFF));
        int green = Math.max(0, Math.min(255, (rawColor >>  8) & 0xFF));
        int blue  = Math.max(0, Math.min(255,  rawColor        & 0xFF));

        // ---- Forward axis (direction of travel) --------------------------------
        double mx = entity.motionX, my = entity.motionY, mz = entity.motionZ;
        double len = Math.sqrt(mx * mx + my * my + mz * mz);
        if (len < 1e-6) {
            double yawRad = Math.toRadians(entity.rotationYaw);
            mx = -Math.sin(yawRad); my = 0; mz = Math.cos(yawRad); len = 1.0;
        }
        double fx = mx / len, fy = my / len, fz = mz / len;

        // ---- Right axis: f × worldUp -------------------------------------------
        double rx, ry, rz;
        if (Math.abs(fy) > 0.99) {
            rx = 1; ry = 0; rz = 0;
        } else {
            rx = fz; ry = 0; rz = -fx;
            double rLen = Math.sqrt(rx * rx + rz * rz);
            rx /= rLen; rz /= rLen;
        }

        // ---- Quad corners ------------------------------------------------------
        // The texture fills a square hw×hw centred at the entity, lying flat in
        // the (forward, right) plane.
        double hw = EntitySpellSlice.SLICE_HALF_WIDTH;

        double cx = x;
        double cy = y + entity.height * 0.5;
        double cz = z;

        double flx = fx * hw, fly = fy * hw, flz = fz * hw;  // forward half-extent
        double rlx = rx * hw, rly = ry * hw, rlz = rz * hw;  // right   half-extent

        double[] fr = { cx + flx + rlx,  cy + fly + rly,  cz + flz + rlz };
        double[] fl = { cx + flx - rlx,  cy + fly - rly,  cz + flz - rlz };
        double[] bl = { cx - flx - rlx,  cy - fly - rly,  cz - flz - rlz };
        double[] br = { cx - flx + rlx,  cy - fly + rly,  cz - flz + rlz };

        // ---- GL state ----------------------------------------------------------
        GL11.glPushMatrix();
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDepthMask(false);

        Minecraft.getMinecraft().renderEngine.bindTexture(TEXTURE);

        Tessellator tess = Tessellator.getInstance();
        tess.getBuffer().begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);

        // Front face
        tess.getBuffer().pos(fr[0], fr[1], fr[2]).tex(1, 0).color(red, green, blue, 255).endVertex();
        tess.getBuffer().pos(fl[0], fl[1], fl[2]).tex(0, 0).color(red, green, blue, 255).endVertex();
        tess.getBuffer().pos(bl[0], bl[1], bl[2]).tex(0, 1).color(red, green, blue, 255).endVertex();
        tess.getBuffer().pos(br[0], br[1], br[2]).tex(1, 1).color(red, green, blue, 255).endVertex();

        // Back face (reversed winding so it's visible from both sides)
        tess.getBuffer().pos(br[0], br[1], br[2]).tex(1, 1).color(red, green, blue, 255).endVertex();
        tess.getBuffer().pos(bl[0], bl[1], bl[2]).tex(0, 1).color(red, green, blue, 255).endVertex();
        tess.getBuffer().pos(fl[0], fl[1], fl[2]).tex(0, 0).color(red, green, blue, 255).endVertex();
        tess.getBuffer().pos(fr[0], fr[1], fr[2]).tex(1, 0).color(red, green, blue, 255).endVertex();

        tess.draw();

        GL11.glDepthMask(true);
        GL11.glPopAttrib();
        GL11.glPopMatrix();
    }

    @Override
    protected ResourceLocation getEntityTexture(EntitySpellSlice entity) {
        return TEXTURE;
    }
}

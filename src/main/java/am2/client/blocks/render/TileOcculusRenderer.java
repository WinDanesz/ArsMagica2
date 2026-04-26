package am2.client.blocks.render;

import am2.common.blocks.tileentity.TileEntityOcculus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class TileOcculusRenderer extends TileEntitySpecialRenderer<TileEntityOcculus> {

    private static final ResourceLocation EYE_TEXTURE = new ResourceLocation("arsmagica2", "textures/blocks/occulus_eye.png");

    // Eye dimensions in pixels on the 16x16 texture
    private static final float EYE_WIDTH_PIXELS = 9.0f;
    private static final float EYE_HEIGHT_PIXELS = 6.0f;
    private static final float TEXTURE_SIZE = 16.0f;

    // UV coordinates for the eye (top-left part of texture)
    private static final float U_MIN = 0.0f;
    private static final float U_MAX = EYE_WIDTH_PIXELS / TEXTURE_SIZE;
    private static final float V_MIN = 0.0f;
    private static final float V_MAX = EYE_HEIGHT_PIXELS / TEXTURE_SIZE;

    // Size of the eye in world units (relative to block size) - made larger for visibility
    private static final float EYE_WIDTH = (EYE_WIDTH_PIXELS / TEXTURE_SIZE) * 1.0f; // Scale up by 1.0x
    private static final float EYE_HEIGHT = (EYE_HEIGHT_PIXELS / TEXTURE_SIZE) * 1.0f;

    @Override
    public void render(TileEntityOcculus te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (te == null) return;

        EntityPlayer player = Minecraft.getMinecraft().player;
        if (player == null) return;

        GlStateManager.pushMatrix();

        // Calculate subtle floating animation
        float floatOffset = (float) Math.sin((te.getWorld().getTotalWorldTime() + partialTicks) * 0.05) * 0.04f;

        // Position at the block center, at the eye height (around y=14/16 blocks based on model)
        GlStateManager.translate(x + 0.5, y + 0.875 + floatOffset, z + 0.5); // 0.875 = 14/16

        // Calculate the vector from the block to the player's eyes
        double dx = player.posX - (te.getPos().getX() + 0.5);
        double dy = player.posY + player.getEyeHeight() - (te.getPos().getY() + 0.875 + floatOffset);
        double dz = player.posZ - (te.getPos().getZ() + 0.5);

        // Calculate yaw and pitch to face the player
        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0f;
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, horizontalDistance));

        // Apply rotations to face the player (billboard effect)
        GlStateManager.rotate(-yaw, 0.0f, 1.0f, 0.0f);
        GlStateManager.rotate(pitch, 1.0f, 0.0f, 0.0f);

        // Bind the eye texture
        bindTexture(EYE_TEXTURE);

        // Setup rendering state
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableLighting();
        GlStateManager.enableAlpha();
        GlStateManager.disableCull();
        GlStateManager.enableTexture2D();

        // Render the eye as a textured quad
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

        float halfWidth = EYE_WIDTH / 2.0f;
        float halfHeight = EYE_HEIGHT / 2.0f;

        // Draw a quad centered at the origin, facing towards the camera
        buffer.pos(-halfWidth, -halfHeight, 0.0).tex(U_MIN, V_MAX).endVertex();
        buffer.pos(-halfWidth, halfHeight, 0.0).tex(U_MIN, V_MIN).endVertex();
        buffer.pos(halfWidth, halfHeight, 0.0).tex(U_MAX, V_MIN).endVertex();
        buffer.pos(halfWidth, -halfHeight, 0.0).tex(U_MAX, V_MAX).endVertex();

        tessellator.draw();

        // Restore rendering state
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
        GlStateManager.enableCull();

        GlStateManager.popMatrix();
    }
}

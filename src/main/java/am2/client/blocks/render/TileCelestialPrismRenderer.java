package am2.client.blocks.render;

import am2.client.utils.DirectOBJModel;
import am2.common.blocks.tileentity.TileEntityCelestialPrism;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/**
 * TESR for Celestial Prism - renders the OBJ crystal model.
 * The block uses the scaledown invisible model in the blockstate,
 * and this TESR renders the actual visual using the 1.7.10-era OBJ.
 * Uses a direct OBJ parser via Minecraft's resource manager to avoid
 * depending on Forge's internal OBJModel API.
 */
public class TileCelestialPrismRenderer extends TileEntitySpecialRenderer<TileEntityCelestialPrism> {

    private static final ResourceLocation TEXTURE = new ResourceLocation("arsmagica2", "textures/blocks/celestial_prism.png");
    private static final ResourceLocation OBJ_RESOURCE = new ResourceLocation("arsmagica2", "models/block/celestial_prism.obj");

    private static final DirectOBJModel model = new DirectOBJModel(OBJ_RESOURCE);

    @Override
    public void render(TileEntityCelestialPrism te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (!model.isLoaded()) {
            model.load(false, true);
        }
        if (model.isEmpty()) return;

        // Use world time for animation; render statically in inventory (no world)
        float brightness;
        float time;
        if (te.getWorld() != null) {
            time = (te.getWorld().getTotalWorldTime() + partialTicks) / 20f;
            brightness = 0.90f + 0.10f * (float) Math.sin(time * Math.PI * 0.6);
        } else {
            brightness = 1.0f;
        }

        boolean wasBlendEnabled = GL11.glIsEnabled(GL11.GL_BLEND);
        GlStateManager.pushMatrix();

        GlStateManager.translate(x + 0.5, y, z + 0.5);

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.enableTexture2D();
        GlStateManager.color(brightness, brightness, brightness, 0.95f);

        Minecraft.getMinecraft().renderEngine.bindTexture(TEXTURE);

        model.render();

        GlStateManager.enableCull();
        GlStateManager.enableLighting();
        if (wasBlendEnabled) GlStateManager.enableBlend(); else GlStateManager.disableBlend();
        GlStateManager.color(1f, 1f, 1f, 1f);

        GlStateManager.popMatrix();
    }
}

package am2.client.entity.render;

import am2.common.entity.EntityLightningElemental;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerLightningElementalEyes implements LayerRenderer<EntityLightningElemental> {

    private static final ResourceLocation EYES_TEXTURE =
            new ResourceLocation("arsmagica2", "textures/entities/lightning_elemental_eyes.png");

    private final RenderLightningElemental renderer;

    public LayerLightningElementalEyes(RenderLightningElemental renderer) {
        this.renderer = renderer;
    }

    @Override
    public void doRenderLayer(EntityLightningElemental entity, float limbSwing, float limbSwingAmount,
                              float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        this.renderer.bindTexture(EYES_TEXTURE);
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
        GlStateManager.depthMask(!entity.isInvisible());

        // Force full-bright lightmap so eyes are unaffected by world lighting
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 61680f % 65536, 61680f / 65536);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        Minecraft.getMinecraft().entityRenderer.setupFogColor(true);
        this.renderer.getMainModel().render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        Minecraft.getMinecraft().entityRenderer.setupFogColor(false);

        // Restore normal lightmap
        int brightness = entity.getBrightnessForRender();
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit,
                brightness % 65536, brightness / 65536);
        this.renderer.setLightmap(entity);

        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.depthMask(true);
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
}

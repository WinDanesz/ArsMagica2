package am2.client.entity.render;

import am2.client.entity.models.ModelAirElemental;
import am2.common.entity.EntityAirElemental;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

/**
 * A slightly translucent head wrapped in soft ribbons of spiraling wind.
 */
public class RenderAirElemental extends RenderLiving<EntityAirElemental> {

    private static final ResourceLocation TEXTURE = new ResourceLocation("arsmagica2", "textures/entities/air_elemental.png");

    public RenderAirElemental(RenderManager renderManager) {
        super(renderManager, new ModelAirElemental(), 0.4f);
        addLayer(new LayerAirElementalWind(this, TEXTURE));
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityAirElemental entity) {
        return TEXTURE;
    }

    @Override
    protected void preRenderCallback(EntityAirElemental entity, float partialTicks) {
        float size = entity.getAirBlastScale(partialTicks);
        // Expand around the floating head, keeping the face and breath at the
        // same height. RenderLiving translates the model by -1.501 afterwards.
        GlStateManager.translate(0.0F, -1.501F, 0.0F);
        GlStateManager.scale(size, size, size);
        GlStateManager.translate(0.0F, 1.501F, 0.0F);
    }

    @Override
    protected void renderModel(EntityAirElemental entity, float limbSwing, float limbSwingAmount,
                               float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        if (renderOutlines || entity.isInvisible()) {
            super.renderModel(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            return;
        }
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        // Cull the rear faces so overlapping cube faces don't compound the opacity.
        GlStateManager.enableCull();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 0.82F);
        try {
            super.renderModel(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        } finally {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.disableCull();
            GlStateManager.disableBlend();
        }
    }
}

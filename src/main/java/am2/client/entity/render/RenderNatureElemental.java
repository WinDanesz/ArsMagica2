package am2.client.entity.render;

import am2.client.entity.models.ModelNatureElemental;
import am2.common.entity.EntityNatureElemental;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class RenderNatureElemental extends RenderLiving<EntityNatureElemental> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("arsmagica2", "textures/entities/nature_elemental.png");

    public RenderNatureElemental(RenderManager manager) {
        super(manager, new ModelNatureElemental(), 0.7F);
    }

    @Override
    protected void renderModel(EntityNatureElemental entity, float limbSwing, float limbSwingAmount,
                               float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        // Flat ModelBoxes have coincident, oppositely wound faces. RenderLiving disables
        // culling, making both faces draw and fight; culling keeps either side visible
        // from its own direction without changing the foliage's box UVs.
        boolean wasCulling = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        GlStateManager.enableCull();
        try {
            super.renderModel(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        } finally {
            if (!wasCulling) {
                GlStateManager.disableCull();
            }
        }
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityNatureElemental entity) {
        return TEXTURE;
    }
}

package am2.client.entity.render;

import am2.client.entity.models.ModelLightningElemental;
import am2.common.entity.EntityLightningElemental;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderLightningElemental extends RenderLiving<EntityLightningElemental> {

    private static final ResourceLocation TEXTURE = new ResourceLocation("arsmagica2", "textures/entities/lightning_elemental.png");

    public RenderLightningElemental(RenderManager renderManager) {
        super(renderManager, new ModelLightningElemental(), 0.5f);
        this.addLayer(new LayerLightningElementalCharge(this));
        this.addLayer(new LayerLightningElementalEyes(this));
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityLightningElemental entity) {
        return TEXTURE;
    }
}

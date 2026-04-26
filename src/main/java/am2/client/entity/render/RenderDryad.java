package am2.client.entity.render;

import am2.common.entity.EntityDryad;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderDryad extends RenderBiped<EntityDryad> {

    private static final ResourceLocation[] SKIN_VARIANTS = {
        new ResourceLocation("arsmagica2", "textures/entities/dryad_0.png"),
        new ResourceLocation("arsmagica2", "textures/entities/dryad_1.png"),
        new ResourceLocation("arsmagica2", "textures/entities/dryad_2.png")
    };

    public RenderDryad(RenderManager manager) {
        super(manager, new ModelBiped(), 0.5f);
    }

    @Override
    protected void preRenderCallback(EntityDryad entity, float partialTickTime) {
        GlStateManager.scale(0.9375F, 0.9375F, 0.9375F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityDryad par1Entity) {
        int variant = par1Entity.getSkinVariant();
        if (variant < 0 || variant >= SKIN_VARIANTS.length) variant = 0;
        return SKIN_VARIANTS[variant];
    }

}

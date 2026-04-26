package am2.client.entity.render;

import am2.client.entity.models.ModelIceElemental;
import am2.common.entity.EntityIceElemental;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderIceElemental extends RenderBiped<EntityIceElemental> {

    private static final ResourceLocation[] TEXTURES = new ResourceLocation[4];

    static {
        for (int i = 0; i < TEXTURES.length; i++) {
            TEXTURES[i] = new ResourceLocation("arsmagica2", "textures/entities/ice_elemental_" + i + ".png");
        }
    }

    public RenderIceElemental(RenderManager renderManager) {
        super(renderManager, new ModelIceElemental(), 0.5f);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityIceElemental entity) {
        int variant = entity.getVariant();
        if (variant < 0 || variant >= TEXTURES.length) variant = 0;
        return TEXTURES[variant];
    }

}

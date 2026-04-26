package am2.client.entity.render;

import am2.client.entity.models.ModelEarthElemental;
import am2.common.entity.EntityEarthElemental;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderEarthElemental extends RenderBiped<EntityEarthElemental> {

    private static final ResourceLocation[] TEXTURES = new ResourceLocation[8];

    static {
        for (int i = 0; i < TEXTURES.length; i++) {
            TEXTURES[i] = new ResourceLocation("arsmagica2", "textures/entities/earth_elemental_" + i + ".png");
        }
    }

    public RenderEarthElemental(RenderManager renderManager) {
        super(renderManager, new ModelEarthElemental(), 0.5f);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityEarthElemental entity) {
        int variant = entity.getVariant();
        if (variant < 0 || variant >= TEXTURES.length) variant = 0;
        return TEXTURES[variant];
    }

}

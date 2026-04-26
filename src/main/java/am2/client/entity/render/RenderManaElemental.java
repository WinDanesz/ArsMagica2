package am2.client.entity.render;

import am2.client.entity.models.ModelManaElemental;
import am2.common.entity.EntityManaElemental;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderManaElemental extends RenderBiped<EntityManaElemental> {

    private static final ResourceLocation rLoc = new ResourceLocation("arsmagica2", "textures/entities/mana_elemental.png");

    public RenderManaElemental(RenderManager renderManager) {
        super(renderManager, new ModelManaElemental(), 0.5f);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityManaElemental par1Entity) {
        return rLoc;
    }

}

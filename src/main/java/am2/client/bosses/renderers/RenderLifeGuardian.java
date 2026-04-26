package am2.client.bosses.renderers;

import am2.client.bosses.models.ModelLifeGuardian;
import am2.common.bosses.EntityLifeGuardian;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderLifeGuardian extends RenderBoss<EntityLifeGuardian> {

    private static final ResourceLocation rLoc = new ResourceLocation("arsmagica2", "textures/entities/bosses/life_guardian.png");

    public RenderLifeGuardian(RenderManager manager) {
        super(manager, new ModelLifeGuardian());
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityLifeGuardian entity) {
        return rLoc;
    }

}

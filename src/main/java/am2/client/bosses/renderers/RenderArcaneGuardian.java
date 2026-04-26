package am2.client.bosses.renderers;

import am2.client.bosses.models.ModelArcaneGuardian;
import am2.common.bosses.EntityArcaneGuardian;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderArcaneGuardian extends RenderBoss<EntityArcaneGuardian> {

    private static final ResourceLocation rLoc = new ResourceLocation("arsmagica2", "textures/entities/bosses/arcane_guardian.png");

    public RenderArcaneGuardian(RenderManager manager) {
        super(manager, new ModelArcaneGuardian());
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityArcaneGuardian entity) {
        return rLoc;
    }

}

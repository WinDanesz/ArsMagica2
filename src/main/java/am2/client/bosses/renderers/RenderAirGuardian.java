package am2.client.bosses.renderers;

import am2.client.bosses.models.ModelAirGuardian;
import am2.common.bosses.EntityAirGuardian;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderAirGuardian extends RenderBoss<EntityAirGuardian> {

    private static final ResourceLocation rLoc = new ResourceLocation("arsmagica2", "textures/entities/bosses/air_guardian.png");

    public RenderAirGuardian(RenderManager manager) {
        super(manager, new ModelAirGuardian());
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityAirGuardian entity) {
        return rLoc;
    }
}

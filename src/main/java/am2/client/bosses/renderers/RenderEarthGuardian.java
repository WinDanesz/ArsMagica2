package am2.client.bosses.renderers;

import am2.client.bosses.models.ModelEarthGuardian;
import am2.common.bosses.EntityEarthGuardian;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderEarthGuardian extends RenderBoss<EntityEarthGuardian> {

    private static final ResourceLocation rLoc = new ResourceLocation("arsmagica2", "textures/entities/bosses/earth_guardian.png");

    public RenderEarthGuardian(RenderManager manager) {
        super(manager, new ModelEarthGuardian());
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityEarthGuardian entity) {
        return rLoc;
    }
}

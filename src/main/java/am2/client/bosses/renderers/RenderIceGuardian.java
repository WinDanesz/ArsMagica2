package am2.client.bosses.renderers;

import am2.client.bosses.models.ModelWinterGuardian;
import am2.common.bosses.EntityWinterGuardian;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderIceGuardian extends RenderBoss<EntityWinterGuardian> {

    private static final ResourceLocation rLoc = new ResourceLocation("arsmagica2", "textures/entities/bosses/ice_guardian.png");

    public RenderIceGuardian(RenderManager manager) {
        super(manager, new ModelWinterGuardian());
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityWinterGuardian entity) {
        return rLoc;
    }
}

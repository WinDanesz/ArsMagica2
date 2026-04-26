package am2.client.bosses.renderers;

import am2.client.bosses.models.ModelEnderGuardian;
import am2.common.bosses.EntityEnderGuardian;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderEnderGuardian extends RenderBoss<EntityEnderGuardian> {

    private static final ResourceLocation rLoc = new ResourceLocation("arsmagica2", "textures/entities/bosses/ender_guardian.png");

    public RenderEnderGuardian(RenderManager manager) {
        super(manager, new ModelEnderGuardian());
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityEnderGuardian entity) {
        return rLoc;
    }
}

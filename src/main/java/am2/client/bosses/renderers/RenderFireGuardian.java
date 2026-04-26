package am2.client.bosses.renderers;

import am2.client.bosses.models.ModelFireGuardian;
import am2.common.bosses.EntityFireGuardian;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderFireGuardian extends RenderBoss<EntityFireGuardian> {
    private static final ResourceLocation rLoc = new ResourceLocation("arsmagica2", "textures/entities/bosses/fire_guardian.png");

    public RenderFireGuardian(RenderManager manager) {
        super(manager, new ModelFireGuardian());
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityFireGuardian entity) {
        return rLoc;
    }
}

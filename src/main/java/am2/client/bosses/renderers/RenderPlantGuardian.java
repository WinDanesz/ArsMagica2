package am2.client.bosses.renderers;

import am2.client.bosses.models.ModelPlantGuardian;
import am2.common.bosses.EntityNatureGuardian;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderPlantGuardian extends RenderBoss<EntityNatureGuardian> {

    private static final ResourceLocation rLoc = new ResourceLocation("arsmagica2", "textures/entities/bosses/plant_guardian.png");

    public RenderPlantGuardian(RenderManager manager) {
        super(manager, new ModelPlantGuardian());
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityNatureGuardian entity) {
        return rLoc;
    }

}

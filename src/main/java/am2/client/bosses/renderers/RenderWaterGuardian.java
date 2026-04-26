package am2.client.bosses.renderers;

import am2.client.bosses.models.ModelWaterGuardian;
import am2.common.bosses.EntityWaterGuardian;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderWaterGuardian extends RenderBoss<EntityWaterGuardian> {

    private static final ResourceLocation rLoc = new ResourceLocation("arsmagica2", "textures/entities/bosses/water_guardian.png");

    public RenderWaterGuardian(RenderManager manager) {
        super(manager, new ModelWaterGuardian());
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityWaterGuardian entity) {
        return rLoc;
    }
}

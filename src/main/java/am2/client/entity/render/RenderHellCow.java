package am2.client.entity.render;

import am2.client.models.ModelHellCow;
import am2.common.entity.EntityHellCow;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderHellCow extends RenderBiped<EntityHellCow> {

    private static final ResourceLocation rLoc = new ResourceLocation("arsmagica2", "textures/entities/hell_cow.png");

    public RenderHellCow(RenderManager manager) {
        super(manager, new ModelHellCow(), 0.5f);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityHellCow entity) {
        return rLoc;
    }
}

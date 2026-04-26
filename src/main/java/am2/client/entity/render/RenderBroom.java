package am2.client.entity.render;

import am2.client.entity.models.ModelBroom;
import am2.common.entity.EntityBroom;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderBroom extends RenderLiving<EntityBroom> {

    private static final ResourceLocation rLoc = new ResourceLocation("arsmagica2", "textures/entities/broom.png");

    public RenderBroom(RenderManager rendermanagerIn) {
        super(rendermanagerIn, new ModelBroom(), 0.5f);
    }

    @Override
    public void doRender(EntityBroom entity, double x, double y, double z, float entityYaw, float partialTicks) {
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityBroom entity) {
        return rLoc;
    }
}

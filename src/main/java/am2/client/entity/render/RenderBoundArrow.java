package am2.client.entity.render;

import am2.common.entity.EntityBoundArrow;
import net.minecraft.client.renderer.entity.RenderArrow;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderBoundArrow extends RenderArrow<EntityBoundArrow> {

    public RenderBoundArrow(RenderManager renderManagerIn) {
        super(renderManagerIn);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityBoundArrow entity) {
        return new ResourceLocation("textures/entity/projectiles/arrow.png");
    }

}

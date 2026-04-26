package am2.client.entity.render;

import am2.client.models.ModelDarkling;
import am2.common.entity.EntityDarkling;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderDarkling extends RenderLiving<EntityDarkling> {

    private static final ResourceLocation rLoc = new ResourceLocation("arsmagica2", "textures/entities/darkling.png");

    public RenderDarkling(RenderManager manager) {
        super(manager, new ModelDarkling(), 0.5f);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityDarkling par1Entity) {
        return rLoc;
    }
}

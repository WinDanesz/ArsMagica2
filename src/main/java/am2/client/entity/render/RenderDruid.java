package am2.client.entity.render;

import am2.client.models.ModelDruid;
import am2.common.entity.EntityDruid;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderDruid extends RenderBiped<EntityDruid> {

    private static final ResourceLocation TEXTURE = new ResourceLocation("arsmagica2", "textures/entities/druid.png");

    public RenderDruid(RenderManager manager) {
        super(manager, new ModelDruid(), 0.5f);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityDruid entity) {
        return TEXTURE;
    }
}

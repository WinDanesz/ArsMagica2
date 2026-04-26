package am2.client.entity.render;

import am2.common.entity.EntityFireElemental;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderFireElemental extends RenderBiped<EntityFireElemental> {

    private static final ResourceLocation rLoc = new ResourceLocation("arsmagica2", "textures/entities/fire_elemental.png");

    public RenderFireElemental(RenderManager renderManager) {
        super(renderManager, new ModelBiped(), 0.5f);
        this.addLayer(new LayerFireElementalFlame(this));
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityFireElemental par1Entity) {
        return rLoc;
    }

    @Override
    public void doRender(EntityFireElemental entity, double x, double y, double z, float entityYaw, float partialTicks) {
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }
}
package am2.client.entity.render;

import am2.common.entity.EntityShadowHelper;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class RenderShadowHelper extends RenderBiped<EntityShadowHelper> {
    private final ModelPlayer defaultModel;
    private final ModelPlayer slimModel;

    public RenderShadowHelper(RenderManager manager) {
        super(manager, new ModelPlayer(0.0F, false), 0.5f);
        this.defaultModel = (ModelPlayer) this.mainModel;
        this.slimModel = new ModelPlayer(0.0F, true);
    }

    @Override
    public void doRender(EntityShadowHelper entity, double x, double y, double z, float entityYaw, float partialTicks) {
        this.mainModel = "slim".equals(entity.getSkinType()) ? this.slimModel : this.defaultModel;
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityShadowHelper par1Entity) {
        return par1Entity.getLocationSkin();
    }

    @Override
    protected void renderModel(EntityShadowHelper par1EntityLivingBase, float par2, float par3, float par4, float par5, float par6, float par7) {
        GL11.glPushAttrib(GL11.GL_COLOR_BUFFER_BIT);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1, 1, 1, 0.35f);
        super.renderModel(par1EntityLivingBase, par2, par3, par4, par5, par6, par7);
        GL11.glPopAttrib();
    }
}

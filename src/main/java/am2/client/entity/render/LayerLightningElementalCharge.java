package am2.client.entity.render;

import am2.client.entity.models.ModelLightningElemental;
import am2.common.entity.EntityLightningElemental;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Renders a scrolling electric-charge overlay on the Lightning Elemental,
 * identical in technique to vanilla's LayerCreepersCharge.
 */
@SideOnly(Side.CLIENT)
public class LayerLightningElementalCharge implements LayerRenderer<EntityLightningElemental> {

    // Vanilla charged-creeper overlay texture – a tileable lightning-crackle pattern
    private static final ResourceLocation CHARGE_TEXTURE =
            new ResourceLocation("textures/entity/creeper/creeper_armor.png");

    private final RenderLightningElemental renderer;
    // Separate model instance with every box inflated by 0.5 units outward from its surface
    private final ModelLightningElemental overlayModel = new ModelLightningElemental(0.5F);

    public LayerLightningElementalCharge(RenderLightningElemental renderer) {
        this.renderer = renderer;
    }

    @Override
    public void doRenderLayer(EntityLightningElemental entity, float limbSwing, float limbSwingAmount,
                              float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {

        boolean wasInvisible = entity.isInvisible();
        entity.setInvisible(false);

        this.renderer.bindTexture(CHARGE_TEXTURE);

        // Scroll the texture matrix for an animated crackling effect
        GlStateManager.matrixMode(5890); // GL_TEXTURE
        GlStateManager.loadIdentity();
        float time = entity.ticksExisted + partialTicks;
        GlStateManager.translate(time * 0.01F, time * 0.01F, 0.0F);
        GlStateManager.matrixMode(5888); // GL_MODELVIEW

        GlStateManager.enableBlend();
        GlStateManager.color(0.5F, 0.5F, 0.5F, 1.0F);
        GlStateManager.disableAlpha();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);

        // Copy animation state (head yaw, limb swing, etc.) from the live model,
        // then re-run setLivingAnimations so per-part rotation angles also match.
        overlayModel.setModelAttributes(this.renderer.getMainModel());
        overlayModel.setLivingAnimations(entity, limbSwing, limbSwingAmount, partialTicks);
        overlayModel.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);

        entity.setInvisible(wasInvisible);

        // Reset texture matrix
        GlStateManager.matrixMode(5890);
        GlStateManager.loadIdentity();
        GlStateManager.matrixMode(5888);

        GlStateManager.enableAlpha();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
}


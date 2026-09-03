package am2.client.entity.render;

import am2.common.affinity.abilities.AbilityRimeguard;
import am2.common.extensions.AffinityData;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Renders a translucent icy crust over the player's body while their Rimeguard (Ice affinity)
 * shield is charged. Re-renders the player's own already-posed model at a slightly enlarged
 * scale with a flat icy tint instead of its skin texture, so it always matches the current
 * animation (walking, swinging, sneaking, ...) exactly without needing its own geometry or
 * texture — the same general approach {@link LayerFireElementalFlame} and
 * {@link LayerLightningElementalCharge} use for their own elementals' glow layers.
 */
@SideOnly(Side.CLIENT)
public class LayerRimeguardFrost implements LayerRenderer<AbstractClientPlayer> {

    private static final float ENLARGE = 1.03f;
    private static final float TINT_R = 0.85f;
    private static final float TINT_G = 0.96f;
    private static final float TINT_B = 1.0f;
    private static final float MAX_ALPHA = 0.35f;

    private final RenderPlayer renderer;

    public LayerRimeguardFrost(RenderPlayer renderer) {
        this.renderer = renderer;
    }

    @Override
    public void doRenderLayer(AbstractClientPlayer player, float limbSwing, float limbSwingAmount,
                               float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        AffinityData data = AffinityData.For(player);
        if (data == null) return;
        float shield = data.getAbilityFloat(AbilityRimeguard.SHIELD_KEY);
        if (shield <= 0f || player.isInvisible()) return;

        // Fade the crust in with how much shield is actually banked, rather than snapping
        // straight to full opacity the instant the first heart finishes building.
        float alpha = Math.min(MAX_ALPHA, MAX_ALPHA * (shield / (AbilityRimeguard.HP_PER_HEART * 2f)));

        ModelBase model = renderer.getMainModel();

        GlStateManager.pushMatrix();
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);
        GlStateManager.color(TINT_R, TINT_G, TINT_B, alpha);

        GlStateManager.scale(ENLARGE, ENLARGE, ENLARGE);
        model.render(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);

        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.popMatrix();
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
}

package am2.client.render;

import am2.ArsMagica;
import am2.client.models.ModelFireGuardianEars;
import am2.client.utils.ModelLibrary;
import am2.common.registry.AMItems;
import baubles.api.BaublesApi;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(modid = ArsMagica.MODID, value = Side.CLIENT)
public final class FireEarsBaubleRenderer {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("arsmagica2:textures/entities/bosses/fire_guardian.png");

    @SubscribeEvent
    public static void onPlayerRenderPost(RenderPlayerEvent.Post event) {
        if (!Loader.isModLoaded("baubles")) return;
        checkAndRender(event);
    }

    @Optional.Method(modid = "baubles")
    private static void checkAndRender(RenderPlayerEvent.Post event) {
        EntityPlayer player = event.getEntityPlayer();
        if (BaublesApi.isBaubleEquipped(player, AMItems.fire_ears) < 0) return;
        renderEars(event, player);
    }

    private static void renderEars(RenderPlayerEvent.Post event, EntityPlayer player) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);

        GlStateManager.pushMatrix();
        GlStateManager.pushAttrib();
        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        float partialTick = event.getPartialRenderTick();
        float ageInTicks = player.ticksExisted + partialTick;
        float headYaw = player.rotationYawHead - player.renderYawOffset;
        float headPitch = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * partialTick;

        // RenderPlayerEvent.Post fires after the player's own pushMatrix/popMatrix scope,
        // so the player-specific GL transforms are gone. Reconstruct them to match what
        // LayerArmorBase (and RenderLivingBase) would see:
        GlStateManager.translate((float) event.getX(), (float) event.getY(), (float) event.getZ());
        float bodyYaw = player.prevRenderYawOffset + (player.renderYawOffset - player.prevRenderYawOffset) * partialTick;
        GlStateManager.rotate(180.0f - bodyYaw, 0.0f, 1.0f, 0.0f); // applyRotations
        GlStateManager.scale(-1.0f, -1.0f, 1.0f);                   // prepareScale Y-flip
        GlStateManager.scale(0.9375f, 0.9375f, 0.9375f);            // player model scale
        GlStateManager.translate(0.0f, -1.501f, 0.0f);              // neck/head-base offset

        ModelFireGuardianEars model = ModelLibrary.instance.fireEars;
        model.render(player, player.limbSwing, player.limbSwingAmount, ageInTicks, headYaw, headPitch, 0.0625f);

        GlStateManager.disableBlend();
        GlStateManager.popAttrib();
        GlStateManager.popMatrix();
    }
}

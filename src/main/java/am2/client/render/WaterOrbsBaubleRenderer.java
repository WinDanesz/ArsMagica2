package am2.client.render;

import am2.ArsMagica;
import am2.client.models.ModelWaterGuardianOrbs;
import am2.client.utils.ModelLibrary;
import am2.common.compat.baubles.BaublesCompat;
import am2.common.registry.AMItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(modid = ArsMagica.MODID, value = Side.CLIENT)
public final class WaterOrbsBaubleRenderer {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("arsmagica2:textures/entities/bosses/water_guardian.png");

    @SubscribeEvent
    public static void onPlayerRenderPost(RenderPlayerEvent.Post event) {
        checkAndRender(event);
    }

    private static void checkAndRender(RenderPlayerEvent.Post event) {
        EntityPlayer player = event.getEntityPlayer();
        if (!BaublesCompat.isEquipped(player, AMItems.water_orbs)) return;
        renderOrbs(event, player);
    }

    private static void renderOrbs(RenderPlayerEvent.Post event, EntityPlayer player) {
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

        ModelWaterGuardianOrbs model = ModelLibrary.instance.waterOrbs;
        model.render(player, player.limbSwing, player.limbSwingAmount, ageInTicks, headYaw, headPitch, 0.0625f);

        GlStateManager.disableBlend();
        GlStateManager.popAttrib();
        GlStateManager.popMatrix();
    }
}

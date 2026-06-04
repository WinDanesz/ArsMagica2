package am2.client.blocks.render;

import am2.client.bosses.renderers.RenderItemNoBob;
import am2.client.gui.AMGuiHelper;
import am2.common.blocks.tileentity.TileEntityArcaneDeconstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderEntityItem;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

import java.util.Random;

public class TileArcaneDeconstructorRenderer extends TileEntitySpecialRenderer<TileEntityArcaneDeconstructor> {

    RenderEntityItem renderItem;

    @Override
    public void render(TileEntityArcaneDeconstructor te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (renderItem == null)
            renderItem = new RenderItemNoBob(Minecraft.getMinecraft().getRenderManager());

        renderParticle(te, x, y, z, partialTicks);

        if (te.isActive()) {
            ItemStack stack = te.getInputItem();
            if (!stack.isEmpty()) {
                AMGuiHelper.instance.dummyItem.setItem(stack);
                GlStateManager.pushMatrix();
                GlStateManager.translate(x + 0.5, y + 0.3, z + 0.5);
                final float s1 = 0.8F;
                GlStateManager.scale(s1, s1, s1);
                renderItem.doRender(AMGuiHelper.instance.dummyItem, 0, 0, 0, AMGuiHelper.instance.dummyItem.rotationYaw, partialTicks);
                GlStateManager.popMatrix();
            }
        }
    }

    protected void renderParticle(TileEntityArcaneDeconstructor te, double x, double y, double z, float partialTicks) {
        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GL11.glPushAttrib(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        // gl states/settings for drawing
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glDepthMask(false);

        Tessellator t = Tessellator.getInstance();
        RenderHelper.disableStandardItemLighting();
        int alive = te.getWorld() == null ? 0 : (int)(te.getWorld().getTotalWorldTime() % 1000L);
        float time = (partialTicks + alive) / 1000.0F;
        float rescale = MathHelper.sin((float) Math.PI * time);

        final float s0 = 0.1F;
        final float particleRed = 0.72F,  particleGreen = 0.051F, particleBlue = 0.106F;
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5, y  + 0.5, z  + 0.5);
        GlStateManager.scale(s0, s0, s0);
        Random rand = new Random(432L); // Mithion can do wierd stuff sometimes
        // particle spam
        for (int i = 0; i < 47; ++i) {

            GlStateManager.rotate(rand.nextFloat() * 360.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(rand.nextFloat() * 360.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(rand.nextFloat() * 360.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(rand.nextFloat() * 360.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(rand.nextFloat() * 360.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(time * 360.0F, 0.0F, 0.0F, 1.0F);
            t.getBuffer().begin(6, DefaultVertexFormats.POSITION_COLOR);
            float w = rand.nextFloat() * 2.0F + 1.0F + rescale;
            float h = rand.nextFloat() * 2.0F + 2.0F + rescale * 0.25F;
            t.getBuffer().pos(0.0D, 0.0D, 0.0D).color(particleRed, particleGreen, particleBlue, 1.0F).endVertex();
            t.getBuffer().pos(-0.866D * w, h, -0.5F * w).color(particleRed, particleGreen, particleBlue, 0.0F).endVertex();
            t.getBuffer().pos(0.866D * w, h, -0.5F * w).color(particleRed, particleGreen, particleBlue, 0.0F).endVertex();
            t.getBuffer().pos(0.0D, h, w).color(particleRed, particleGreen, particleBlue, 0.0F).endVertex();
            t.getBuffer().pos(-0.866D * w, h, -0.5F * w).color(particleRed, particleGreen, particleBlue, 0.0F).endVertex();
            t.draw();
        }

        // restore previous gl state
        GL11.glPopAttrib();

        GL11.glDepthMask(true);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GlStateManager.popMatrix();
    }
}

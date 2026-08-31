package am2.api.compendium.pages;

import am2.ArsMagica;
import am2.common.bosses.AM2Boss;
import am2.common.entity.EntityFlicker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class PageEntity extends CompendiumPage<Entity> {

    private float curRotationH = 0;
    private int lastMouseX = 0;
    private boolean isDragging;
    private boolean loggedRenderError = false;

    public PageEntity(Entity element) throws Throwable {
        super(element);
    }

    @Override
    protected void renderPage(int posX, int posY, int mouseX, int mouseY) {
        World world = Minecraft.getMinecraft().world;
        if (world == null)
            return;
        int cx = posX + 60;
        int cy = posY + 92;
        NBTTagCompound compound = element.writeToNBT(new NBTTagCompound());

        GlStateManager.enableColorMaterial();
        GlStateManager.pushMatrix();
        try {
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            GlStateManager.translate((float) (cx - 2), (float) (cy + 20), 50.0F);
            GlStateManager.scale(10.0F, 10.0F, 10.0F);
            GlStateManager.translate(1.0F, 6.5F, 1.0F);
            GlStateManager.scale(6.0F, 6.0F, -1.0F);
            GlStateManager.rotate(210.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);

            RenderHelper.enableStandardItemLighting();

            GlStateManager.pushMatrix();
            GlStateManager.pushAttrib();
            try {
                if (element instanceof AM2Boss) {
                    float scaleFactorX = (1 / element.width);
                    float scaleFactorY = (2 / element.height);
                    float scaleFactor = Math.min(scaleFactorX, scaleFactorY);
                    GlStateManager.scale(scaleFactor, scaleFactor, scaleFactor);
                } else if (element instanceof EntityFlicker) {
                    GlStateManager.translate(0, 1.3f, 0);
                }
                GlStateManager.rotate(curRotationH, 0, 1, 0);

                Entity ent = element.getClass().getConstructor(World.class).newInstance(world);
                ent.readFromNBT(compound);
                if (mc.player != null) {
                    // Keep the preview entity at the player's position so it picks up loaded, lit
                    // chunk data - otherwise it sits at world origin, which is usually unloaded on
                    // the client and renders pitch black (i.e. invisible).
                    ent.setPosition(mc.player.posX, mc.player.posY, mc.player.posZ);
                }

                RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
                renderManager.setPlayerViewY(180.0F);
                renderManager.setRenderShadow(false);
                renderManager.renderEntity(ent, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, false);
                renderManager.setRenderShadow(true);

                GlStateManager.disableRescaleNormal();
                GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
                GlStateManager.disableTexture2D();
                GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
            } catch (Exception e) {
                if (!loggedRenderError) {
                    loggedRenderError = true;
                    ArsMagica.LOGGER.error("PageEntity failed to render entity preview for {}", element.getClass().getName(), e);
                }
            }
            GlStateManager.popMatrix();
            GlStateManager.popAttrib();

            RenderHelper.disableStandardItemLighting();
        } catch (Exception e) {
            if (!loggedRenderError) {
                loggedRenderError = true;
                ArsMagica.LOGGER.error("PageEntity failed to render entity preview for {}", element.getClass().getName(), e);
            }
        }
        GlStateManager.popMatrix();

        String renderString = "Click and drag to rotate";
        mc.fontRenderer.drawString(renderString, posX + 72 - (mc.fontRenderer.getStringWidth(renderString) / 2), posY + 200, 0x000000);
    }

    @Override
    public void dragMouse(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (isDragging) {
            curRotationH -= (lastMouseX - mouseX);
            lastMouseX = mouseX;
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        isDragging = true;
        lastMouseX = mouseX;
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        if (state == 1)
            isDragging = false;
    }
}

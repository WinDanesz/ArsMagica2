package am2.client.blocks.render;

import am2.client.bosses.renderers.RenderItemNoBob;
import am2.client.gui.AMGuiHelper;
import am2.common.blocks.tileentity.TileEntityArcaneDeconstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderEntityItem;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;

public class TileArcaneDeconstructorRenderer extends TileEntitySpecialRenderer<TileEntityArcaneDeconstructor> {

    RenderEntityItem renderItem;

    @Override
    public void render(TileEntityArcaneDeconstructor te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (renderItem == null)
            renderItem = new RenderItemNoBob(Minecraft.getMinecraft().getRenderManager());

        if (te.isActive()) {
            ItemStack stack = te.getInputItem();
            if (!stack.isEmpty()) {
                AMGuiHelper.instance.dummyItem.setItem(stack);
                GlStateManager.pushMatrix();
                GlStateManager.translate(x + 0.5, y + 0.3, z + 0.5);
                GlStateManager.scale(0.8f, 0.8f, 0.8f);
                renderItem.doRender(AMGuiHelper.instance.dummyItem, 0, 0, 0, AMGuiHelper.instance.dummyItem.rotationYaw, partialTicks);
                GlStateManager.popMatrix();
            }
        }
    }
}

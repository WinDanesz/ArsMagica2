package am2.client.blocks.render;

import am2.client.gui.AMGuiHelper;
import am2.common.blocks.tileentity.TileEntityCalefactor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderEntityItem;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;

public class TileCalefactorRenderer extends TileEntitySpecialRenderer<TileEntityCalefactor> {

    RenderEntityItem renderItem;

    public TileCalefactorRenderer() {
    }

    @Override
    public void render(TileEntityCalefactor tile, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (renderItem == null) {
            renderItem = new RenderEntityItem(Minecraft.getMinecraft().getRenderManager(), Minecraft.getMinecraft().getRenderItem());
        }

        //render item being cooked, if any
        ItemStack item = tile.getItemBeingCooked();
        if (!item.isEmpty()) {
            int meta = 0;
            if (tile.getWorld() != null) {
                meta = tile.getBlockMetadata();
            }

            double offsetX = 0.5;
            double offsetY = 0.0;
            double offsetZ = 0.5;

            switch (meta) {
                case 1:
                    offsetY = -1.5;
                    break;
                case 3:
                    offsetY = -0.8;
                    offsetZ = -0.2;
                    break;
                case 4:
                    offsetY = -0.8;
                    offsetZ = 1.2;
                    break;
                case 5:
                    offsetX = -0.2;
                    offsetY = -0.8;
                    break;
                case 6:
                    offsetX = 1.2;
                    offsetY = -0.8;
                    break;
            }

            GlStateManager.pushMatrix();
            GlStateManager.translate(0, 0.9f, 0);
            RenderItemAtCoords(item, x + offsetX, y + offsetY, z + offsetZ, partialTicks, 0.7f);
            GlStateManager.popMatrix();
        }
    }

    private void RenderItemAtCoords(ItemStack item, double x, double y, double z, float partialTicks) {
        RenderItemAtCoords(item, x, y, z, partialTicks, 1.0f);
    }

    private void RenderItemAtCoords(ItemStack item, double x, double y, double z, float partialTicks, float scale) {
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, scale);
        AMGuiHelper.instance.dummyItem.setItem(item);
        renderItem.doRender(AMGuiHelper.instance.dummyItem, x / scale, y / scale, z / scale, AMGuiHelper.instance.dummyItem.rotationYaw, partialTicks);
        GlStateManager.popMatrix();
    }
}

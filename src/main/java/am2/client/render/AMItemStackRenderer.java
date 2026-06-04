package am2.client.render;

import am2.common.blocks.tileentity.*;
import am2.common.registry.AMBlocks;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import java.util.HashMap;
import java.util.Map;

public class AMItemStackRenderer extends TileEntityItemStackRenderer {

    private final Map<Item, RenderEntry> renderEntries = new HashMap<>();

    public AMItemStackRenderer() {
        register(AMBlocks.crafting_altar, new TileEntityCraftingAltar(), 1.7F);
        register(AMBlocks.celestial_prism, new TileEntityCelestialPrism(), 1.4F, 0.0D, -0.25D, 0.0D);
        register(AMBlocks.obelisk, new TileEntityObelisk(), 1.4F, 0.0D, -0.25D, 0.0D);
        register(AMBlocks.keystone_chest, new TileEntityKeystoneChest(), 1.7F);
        register(AMBlocks.seer_stone, new TileEntitySeerStone());
        register(AMBlocks.crystal_marker, new TileEntityCrystalMarker(), 3.1F);
        register(AMBlocks.keystone_receptacle, new TileEntityKeystoneReceptacle(), 1.0F);
        register(AMBlocks.arcane_reconstructor, new TileEntityArcaneReconstructor());
        register(AMBlocks.summoner, new TileEntitySummoner(), 1.7F);
        register(AMBlocks.astral_barrier, new TileEntityAstralBarrier(), 1.7F);
    }

    private void register(Block block, TileEntity tileEntity) {
        register(block, tileEntity, 1.0F);
    }

    private void register(Block block, TileEntity tileEntity, float scale) {
        register(block, tileEntity, scale, 0.0D, 0.0D, 0.0D);
    }

    private void register(Block block, TileEntity tileEntity, float scale, double x, double y, double z) {
        renderEntries.put(Item.getItemFromBlock(block), new RenderEntry(tileEntity, scale, x, y, z));
    }

    @Override
    public void renderByItem(ItemStack itemStackIn, float partialTicks) {
        RenderEntry entry = renderEntries.get(itemStackIn.getItem());
        if (entry == null) return;
        GlStateManager.pushMatrix();
        GlStateManager.pushAttrib();
        GlStateManager.translate(0.5F, 0.5F, 0.5F);
        if(entry.scale != 1.0F) {
            GlStateManager.scale(entry.scale, entry.scale, entry.scale);
        }
        TileEntityRendererDispatcher.instance.render(entry.tileEntity, entry.x - 0.5D, entry.y - 0.5D, entry.z - 0.5D, partialTicks);
        GlStateManager.popAttrib();
        GlStateManager.popMatrix();
    }

    private static class RenderEntry {
        final TileEntity tileEntity;
        final float scale;
        final double x, y, z;

        RenderEntry(TileEntity tileEntity, float scale, double x, double y, double z) {
            this.tileEntity = tileEntity;
            this.scale = scale;
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }
}

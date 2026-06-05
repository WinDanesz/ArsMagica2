package am2.client.render;

import am2.common.blocks.BlockCrystalMarker;
import am2.common.blocks.tileentity.*;
import am2.common.registry.AMBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.datafix.walkers.ItemStackData;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AMItemStackRenderer extends TileEntityItemStackRenderer {

    private final Map<ItemData, RenderEntry> renderEntries = new HashMap<>();

    public AMItemStackRenderer() {
        register(data(AMBlocks.crafting_altar), new TileEntityCraftingAltar(), 1.7F);
        register(data(AMBlocks.celestial_prism), new TileEntityCelestialPrism(), 1.4F, 0.0D, -0.25D, 0.0D);
        register(data(AMBlocks.obelisk), new TileEntityObelisk(), 1.4F, 0.0D, -0.25D, 0.0D);
        register(data(AMBlocks.keystone_chest), new TileEntityKeystoneChest(), 1.7F);
        register(data(AMBlocks.seer_stone), new TileEntitySeerStone());
        register(data(AMBlocks.keystone_receptacle), new TileEntityKeystoneReceptacle(), 1.0F);
        register(data(AMBlocks.arcane_reconstructor), new TileEntityArcaneReconstructor());
        register(data(AMBlocks.summoner), new TileEntitySummoner(), 1.7F);
        register(data(AMBlocks.astral_barrier), new TileEntityAstralBarrier(), 1.7F);
        for (int i = 0; i < BlockCrystalMarker.crystalMarkerTypes.length; ++i)
            register(data(AMBlocks.crystal_marker, i), new TileEntityCrystalMarker(i), 3.1F);
    }

    private ItemData data(Block block) {
        return new ItemData(block);
    }

    private ItemData data(Block block, int metadata) {
        return new ItemData(block, metadata);
    }

    private void register(ItemData data, TileEntity tileEntity) {
        register(data, tileEntity, 1.0F);
    }

    private void register(ItemData data, TileEntity tileEntity, float scale) {
        register(data, tileEntity, scale, 0.0D, 0.0D, 0.0D);
    }

    private void register(ItemData data, TileEntity tileEntity, float scale, double x, double y, double z) {
        renderEntries.put(data, new RenderEntry(tileEntity, scale, x, y, z));
    }

    @Override
    public void renderByItem(ItemStack stack, float partialTicks) {
        RenderEntry entry = renderEntries.get(new ItemData(stack.getItem(), stack.getMetadata()));
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

        private RenderEntry(TileEntity tileEntity, float scale, double x, double y, double z) {
            this.tileEntity = tileEntity;
            this.scale = scale;
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    private static class ItemData {
        final Item item;
        final int metadata;

        private ItemData(Item item, int metadata) {
            this.item = item;
            this.metadata = metadata;
        }

        private ItemData(Block block, int metadata) {
            this(Item.getItemFromBlock(block), metadata);
        }

        private ItemData(Block block) {
            this(Item.getItemFromBlock(block), 0);
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            ItemData itemData = (ItemData) o;
            return metadata == itemData.metadata && Objects.equals(item, itemData.item);
        }

        @Override
        public int hashCode() {
            return Objects.hash(item, metadata);
        }

    }
}

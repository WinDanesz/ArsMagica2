package am2.common.trackers;

import am2.common.bosses.BossSpawnHelper;
import am2.common.registry.AMBlocks;
import am2.common.registry.AMItems;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;

public class EntityItemWatcher {
    private final ArrayList<EntityItem> watchedItems;
    private final ArrayList<EntityItem> toRemove;

    private final ArrayList<Block> inlayBlocks;
    private final ArrayList<Item> itemsToWatch;

    public static EntityItemWatcher instance = new EntityItemWatcher();

    private EntityItemWatcher() {
        watchedItems = new ArrayList<EntityItem>();
        toRemove = new ArrayList<EntityItem>();
        inlayBlocks = new ArrayList<Block>();
        itemsToWatch = new ArrayList<Item>();
        init();
    }

    public void init() {
        registerInlayBlock(AMBlocks.redstone_inlay);
        registerInlayBlock(AMBlocks.iron_inlay);
        registerInlayBlock(AMBlocks.gold_inlay);

        registerWatchableItem(Items.BOAT);
        registerWatchableItem(Items.WATER_BUCKET);
        //registerWatchableItem(AMItems.essence_none);
        registerWatchableItem(AMItems.essence_ender);
        registerWatchableItem(AMItems.essence_earth);
        registerWatchableItem(AMItems.essence_fire);
        registerWatchableItem(AMItems.essence_life);
        registerWatchableItem(AMItems.essence_nature);
        registerWatchableItem(AMItems.essence_ice);
        registerWatchableItem(AMItems.essence_lightning);
        registerWatchableItem(AMItems.essence_air);
        registerWatchableItem(AMItems.essence_water);
        registerWatchableItem(AMItems.essence_arcane);
        registerWatchableItem(AMItems.chimerite);
        registerWatchableItem(AMItems.blue_topaz);
        registerWatchableItem(AMItems.moonstone);
        registerWatchableItem(AMItems.sunstone);
        registerWatchableItem(AMItems.vinteum_dust);
        registerWatchableItem(AMItems.purified_vinteum_dust);
        registerWatchableItem(Items.EMERALD);
        registerWatchableItem(Items.ENDER_EYE);
    }

    public void tick() {
        watchedItems.removeAll(toRemove);
        toRemove.clear();

        ArrayList<EntityItem> tempItemList = new ArrayList<EntityItem>(watchedItems);
        for (EntityItem item : tempItemList) {
            if (item.isDead) {
                toRemove.add(item);
                continue;
            }
            if (!item.isBurning() && (Math.abs(item.motionX) > 0.01 || Math.abs(item.motionY) > 0.01 || Math.abs(item.motionZ) > 0.01))
                continue;
            BlockPos pos = item.getPosition();

            if (item.isBurning()) pos = pos.up();

            boolean insideRing = true;
            Block ringType = null;

            for (int i = -1; i <= 1 && insideRing; i++) {
                for (int j = -1; j <= 1 && insideRing; ++j) {
                    if (i == 0 && j == 0) continue;
                    Block blockID1 = item.world.getBlockState(pos.add(i, 0, j)).getBlock();
                    Block blockID2 = item.world.getBlockState(pos.add(i, -1, j)).getBlock();
                    Block blockID3 = item.world.getBlockState(pos.add(i, 1, j)).getBlock();
                    if (inlayBlocks.contains(blockID1) || inlayBlocks.contains(blockID2) || inlayBlocks.contains(blockID3)) {
                        if (ringType == null) {
                            ringType = inlayBlocks.contains(blockID1) ? blockID1 : inlayBlocks.contains(blockID2) ? blockID2 : blockID3;
                        } else if (ringType != blockID1 && ringType != blockID2 && ringType != blockID3) {
                            insideRing = false;
                        }
                    } else {
                        insideRing = false;
                    }
                }
            }

            if (insideRing) {
                BossSpawnHelper.instance.onItemInRing(item, ringType);
            }
            removeWatchedItem(item);
        }
        tempItemList.clear();
    }

    public void addWatchedItem(EntityItem item) {
        if (this.itemsToWatch.contains(item.getItem().getItem())) {
            watchedItems.add(item);
        }
    }

    public void registerInlayBlock(Block inlayBlock) {
        if (!this.inlayBlocks.contains(inlayBlock))
            this.inlayBlocks.add(inlayBlock);
    }

    public void registerWatchableItem(Item item) {
        if (!this.itemsToWatch.contains(item))
            this.itemsToWatch.add(item);
    }

    private void removeWatchedItem(EntityItem item) {
        toRemove.add(item);
    }
}

package am2.common.registry;

import am2.ArsMagica;
import am2.common.enchantments.AMEnchantmentHelper;
import com.google.common.collect.Sets;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import java.util.Set;

public class AMTabs {

    public static final CreativeTabs AMBLOCKS = new CreativeTabs(ArsMagica.MODID + ".blocks") {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(AMBlocks.mana_battery);
        }

    };

    public static final CreativeTabs AMITEMS = new CreativeTabs(ArsMagica.MODID + ".items") {
        public ItemStack createIcon() {
            return new ItemStack(AMItems.vinteum_dust);
        }

        /** Boss drops are soulbound when looted, so the creative copies are too. */
        @Override
        public void displayAllRelevantItems(NonNullList<ItemStack> items) {
            super.displayAllRelevantItems(items);
            Set<Item> soulboundDrops = Sets.newHashSet(AMItems.arcane_spellbook, AMItems.nature_scythe,
                    AMItems.winter_arm, AMItems.air_sled, AMItems.earth_armor, AMItems.ender_boots,
                    AMItems.fire_ears, AMItems.life_ward, AMItems.lightning_charm, AMItems.water_orbs);
            for (ItemStack stack : items) {
                if (soulboundDrops.contains(stack.getItem())
                        && EnchantmentHelper.getEnchantmentLevel(AMEnchantments.soulbound, stack) <= 0) {
                    AMEnchantmentHelper.soulbindStack(stack);
                }
            }
        }
    };
}

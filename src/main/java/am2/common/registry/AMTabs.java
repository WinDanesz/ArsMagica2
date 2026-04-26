package am2.common.registry;

import am2.ArsMagica;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

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
    };
}

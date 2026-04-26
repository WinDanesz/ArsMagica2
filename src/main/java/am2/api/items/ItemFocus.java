package am2.api.items;

import am2.common.registry.AMTabs;
import net.minecraft.item.Item;

public abstract class ItemFocus extends Item {

    public ItemFocus() {
        setCreativeTab(AMTabs.AMITEMS);
        setMaxDamage(0);
    }

    public abstract Object[] getRecipeItems();

    public abstract String getInGameName();

}

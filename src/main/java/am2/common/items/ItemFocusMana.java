package am2.common.items;

import am2.api.items.ItemFocus;
import am2.common.registry.AMItems;
import net.minecraft.item.ItemStack;

public class ItemFocusMana extends ItemFocus {

    public ItemFocusMana() {
        super();
    }

    @Override
    public Object[] getRecipeItems() {
        return new Object[]{
                "P", "F", "P",
                'P', new ItemStack(AMItems.vinteum_dust),
                'F', AMItems.standard_focus
        };
    }

    @Override
    public String getInGameName() {
        return "Mana Focus";
    }
}

package am2.common.items;

import am2.api.items.ISpellFocus;
import am2.api.items.ItemFocus;
import am2.common.registry.AMItems;
import net.minecraft.item.ItemStack;

public class ItemFocusGreater extends ItemFocus implements ISpellFocus {

    public ItemFocusGreater() {
        super();
    }

    @Override
    public Object[] getRecipeItems() {
        return new Object[]{
                "A A", "PFP", "A A",
                'A', new ItemStack(AMItems.arcane_ash),
                'F', AMItems.standard_focus,
                'P', new ItemStack(AMItems.purified_vinteum_dust)
        };
    }

    @Override
    public String getInGameName() {
        return "Greater Focus";
    }

    @Override
    public int getFocusLevel() {
        return 2;
    }
}

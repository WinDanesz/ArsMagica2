package am2.common.items;

import am2.api.items.ISpellFocus;
import am2.api.items.ItemFocus;
import am2.common.registry.AMItems;
import net.minecraft.init.Items;

public class ItemFocusStandard extends ItemFocus implements ISpellFocus {

    public ItemFocusStandard() {
        super();
    }

    @Override
    public Object[] getRecipeItems() {
        return new Object[]{
                " R ", "RFR", " R ",
                'R', Items.REDSTONE,
                'F', AMItems.lesser_focus
        };
    }

    @Override
    public String getInGameName() {
        return "Focus";
    }

    @Override
    public int getFocusLevel() {
        return 1;
    }
}

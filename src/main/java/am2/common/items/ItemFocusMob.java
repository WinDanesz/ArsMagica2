package am2.common.items;

import am2.api.items.ItemFilterFocus;
import am2.common.registry.AMItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.init.Items;

public class ItemFocusMob extends ItemFilterFocus {

    public ItemFocusMob() {
        super();
    }

    @Override
    public Class<? extends Entity> getFilterClass() {
        return EntityMob.class;
    }

    @Override
    public Object[] getRecipeItems() {
        return new Object[]{
                "S",
                "F",
                "S",
                Character.valueOf('S'), Items.IRON_SWORD,
                Character.valueOf('F'), AMItems.standard_focus
        };
    }

    @Override
    public String getInGameName() {
        return "Monster Focus";
    }

}

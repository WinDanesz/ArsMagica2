package am2.common.items;

import am2.api.items.ItemFilterFocus;
import am2.common.registry.AMItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;

public class ItemFocusItem extends ItemFilterFocus {

    public ItemFocusItem() {
        super();
    }

    @Override
    public Class<? extends Entity> getFilterClass() {
        return EntityItem.class;
    }

    @Override
    public Object[] getRecipeItems() {
        return new Object[]{
                " C ",
                "PFS",
                " W ",
                Character.valueOf('C'), Blocks.COBBLESTONE,
                Character.valueOf('B'), Items.STONE_PICKAXE,
                Character.valueOf('F'), AMItems.standard_focus,
                Character.valueOf('T'), Items.IRON_SHOVEL,
                Character.valueOf('W'), Blocks.CRAFTING_TABLE
        };
    }

    @Override
    public String getInGameName() {
        return "Item Focus";
    }
}

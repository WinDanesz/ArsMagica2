package am2.common.items;

import am2.api.items.ItemFilterFocus;
import am2.common.registry.AMItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

public class ItemFocusPlayer extends ItemFilterFocus {

    public ItemFocusPlayer() {
        super();
    }

    @Override
    public Class<? extends Entity> getFilterClass() {
        return EntityPlayer.class;
    }

    @Override
    public Object[] getRecipeItems() {
        return new Object[]{
                "L",
                "F",
                // TODO registry
                //	Character.valueOf('L'), new ItemStack(ItemDefs.essence, 1, ArsMagicaAPI.getAffinityRegistry().getId(Affinity.LIFE)),
                Character.valueOf('F'), AMItems.standard_focus
        };
    }

    @Override
    public String getInGameName() {
        return "Player Focus";
    }
}

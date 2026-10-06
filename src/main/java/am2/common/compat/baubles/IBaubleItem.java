package am2.common.compat.baubles;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

/**
 * Marks an AM2 item as wearable in a Baubles slot.
 * <p>
 * This deliberately does not extend Baubles' {@code IBauble}: item classes must never reference a
 * Baubles type, otherwise they fail to load when Baubles is missing or is loaded late. When Baubles
 * is present, {@link BaublesCompat#register()} exposes these items to it through its item capability.
 */
public interface IBaubleItem {

    /**
     * The Baubles slot this item goes in.
     */
    BaubleSlot getBaubleSlot(ItemStack stack);

    /**
     * Called every tick while the item is worn in a Baubles slot.
     */
    default void onWornTick(ItemStack stack, EntityLivingBase entity) {
    }

    /**
     * Mirrors Baubles' slot types by name, so this package-level API has no Baubles dependency.
     */
    enum BaubleSlot {
        AMULET, RING, BELT, TRINKET, HEAD, BODY, CHARM
    }
}

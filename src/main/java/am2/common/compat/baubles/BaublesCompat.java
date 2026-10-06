package am2.common.compat.baubles;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;

/**
 * Entry point for the optional Baubles integration.
 * <p>
 * This class never references a Baubles type, so it is always safe to load. Everything that touches
 * the Baubles API lives in {@link BaublesCompatImpl}, which is only ever loaded after
 * {@link #isLoaded()} has returned true.
 */
public final class BaublesCompat {

    public static final String MODID = "baubles";

    private BaublesCompat() {
    }

    public static boolean isLoaded() {
        return Loader.isModLoaded(MODID);
    }

    /**
     * Makes every {@link IBaubleItem} wearable in Baubles slots. Must run in pre-initialisation,
     * before any item stack of ours is created. Does nothing when Baubles is not installed.
     */
    public static void register() {
        if (!isLoaded()) return;
        MinecraftForge.EVENT_BUS.register(new BaublesCompatImpl());
    }

    /**
     * Finds a spell book in the player's Baubles slots, or {@link ItemStack#EMPTY}.
     */
    public static ItemStack findSpellBook(EntityPlayer player) {
        if (!isLoaded()) return ItemStack.EMPTY;
        return BaublesCompatImpl.findSpellBook(player);
    }

    /**
     * Writes the spell book back to the Baubles slot it came from, marking that slot dirty so it syncs.
     */
    public static void writeSpellBook(EntityPlayer player, ItemStack bookStack) {
        if (!isLoaded()) return;
        BaublesCompatImpl.writeSpellBook(player, bookStack);
    }

    /**
     * Whether the player has the given item equipped in any Baubles slot.
     */
    public static boolean isEquipped(EntityPlayer player, Item item) {
        if (!isLoaded()) return false;
        return BaublesCompatImpl.isEquipped(player, item);
    }
}

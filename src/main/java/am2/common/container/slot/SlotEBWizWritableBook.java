package am2.common.container.slot;

import am2.common.blocks.tileentity.TileEntityInscriptionTable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemWritableBook;
import net.minecraft.util.text.translation.I18n;

/**
 * The secondary book slot that appears in the Inscription Table when the
 * {@code EBWiz_Preserve_Spell_Book} config option is enabled.
 *
 * <p>In this mode the player places their EBWiz spell book in the primary slot
 * and a writable book here.  Pressing "Make Spell" writes the binding recipe
 * written book into <em>this</em> slot while leaving the EBWiz book untouched.
 */
public class SlotEBWizWritableBook extends Slot {

    public SlotEBWizWritableBook(TileEntityInscriptionTable inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    /** Only active when an EBWiz spell book is in the book slot (slot 0). */
    @Override
    public boolean isEnabled() {
        return ((TileEntityInscriptionTable) this.inventory).isEBWizMode();
    }

    /** Only writable books may be placed in this slot. */
    @Override
    public boolean isItemValid(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return stack.getItem() instanceof ItemWritableBook;
    }

    /**
     * Converts a freshly-placed writable book to a written-book placeholder,
     * matching the behaviour of {@link SlotInscriptionTable#putStack(ItemStack)}.
     */
    @SuppressWarnings("deprecation")
    @Override
    public void putStack(ItemStack stack) {
        if (!stack.isEmpty() && stack.getItem() instanceof ItemWritableBook) {
            ItemStack written = new ItemStack(Items.WRITTEN_BOOK);
            if (stack.hasTagCompound()) written.setTagCompound(stack.getTagCompound().copy());
            written.setStackDisplayName(I18n.translateToLocal("am2.tooltip.unfinishedSpellRecipe"));
            super.putStack(written);
        } else {
            super.putStack(stack);
        }
    }

    /**
     * When the player takes the book from this slot it is already a finalized
     * EBWiz binding written book – nothing more to do.
     */
    @Override
    public ItemStack onTake(EntityPlayer player, ItemStack stack) {
        super.onTake(player, stack);
        return stack;
    }
}

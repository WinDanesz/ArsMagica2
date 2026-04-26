package am2.common.compat.electroblob.item;

import am2.common.items.ItemSpellBook;
import electroblob.wizardry.item.ISpellCastingItem;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

import javax.annotation.Nonnull;

/**
 * EBWiz-aware variant of {@link ItemSpellBook} that implements
 * {@link ISpellCastingItem}.
 *
 * <p>Registering this under the {@code "spellbook"} name when EBWiz is
 * present allows {@link electroblob.wizardry.util.EntityUtils#isCasting} to
 * recognise the spell book as a spell-casting item.  This enables EBWiz
 * rendering features that query {@code isCasting} to work correctly when an
 * {@link ItemEBWizSpellBinding} is active in the spell book:
 * <ul>
 *   <li>Wing particle rendering ({@code RenderWings}) while Flight is active</li>
 *   <li>Looping spell sounds ({@code SoundLoopSpell})</li>
 *   <li>Shadow-ward damage-reduction while that spell is active</li>
 *   <li>Any other EBWiz feature keyed off {@code isCasting}</li>
 * </ul>
 *
 * <p>This class is safe as a soft-dependency: it lives in the compat package
 * and is only instantiated (in {@code AMItems.register()}) after
 * {@code Loader.isModLoaded("ebwizardry")} has been confirmed.
 */
public final class ItemSpellBookEBWiz extends ItemSpellBook implements ISpellCastingItem {

    // -------------------------------------------------------------------------
    // ISpellCastingItem implementation
    // -------------------------------------------------------------------------

    /**
     * Returns the EBWiz spell in the spell book's currently active slot, so
     * that {@code EntityUtils.isCasting(player, Spells.flight)} (and similar
     * queries) returns {@code true} while the player is channelling the spell.
     * Returns {@code Spells.none} when the active slot holds a native AM2
     * spell or is empty.
     */
    @Override
    @Nonnull
    public Spell getCurrentSpell(ItemStack stack) {
        ItemStack activeStack = getActiveItemStack(stack);
        if (!activeStack.isEmpty() && activeStack.getItem() instanceof ItemEBWizSpellBinding) {
            Spell spell = ItemEBWizSpellBinding.getSpell(activeStack);
            if (spell != null) return spell;
        }
        return Spells.none;
    }

    /**
     * AM2 provides its own HUD; inform EBWiz that it should not render its
     * own spell overlay when the player holds a spell book.
     */
    @Override
    public boolean showSpellHUD(EntityPlayer player, ItemStack stack) {
        return false;
    }

    /**
     * Not called by EBWiz for spell books (EBWiz uses this for wands).
     * Casting through the spell book is driven by {@link #onUsingTick} which
     * delegates to the active scroll's item ({@link ItemEBWizSpellBinding}).
     * Returns {@code true} so that any speculative call does not block casting.
     */
    @Override
    public boolean canCast(ItemStack stack, Spell spell, EntityPlayer caster,
                           EnumHand hand, int castingTick, SpellModifiers modifiers) {
        return true;
    }

    /**
     * Not called by EBWiz for spell books.  Returns {@code false}; actual
     * casting is dispatched from {@link #onUsingTick}.
     */
    @Override
    public boolean cast(ItemStack stack, Spell spell, EntityPlayer caster,
                        EnumHand hand, int castingTick, SpellModifiers modifiers) {
        return false;
    }
}

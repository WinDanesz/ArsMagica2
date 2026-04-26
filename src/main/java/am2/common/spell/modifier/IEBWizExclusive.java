package am2.common.spell.modifier;

import am2.common.compat.electroblob.item.ItemEBWizSpellBinding;

/**
 * Marker interface for {@link am2.api.spell.SpellModifier} implementations
 * that are exclusive to Electroblob's Wizardry integration.
 *
 * <p>The Scribing Desk GUI uses this marker to:
 * <ul>
 *   <li>Hide these modifiers when no EBWiz spell book is in the desk slot.</li>
 *   <li>Allow them to be applied when an {@link ItemEBWizSpellBinding}
 *       is in the desk slot (EBWiz mode).</li>
 * </ul>
 *
 * <p>Non-EBWiz-exclusive modifiers (Range, Duration) are also available in
 * EBWiz mode, but are already shown for regular spells.
 */
public interface IEBWizExclusive {
}

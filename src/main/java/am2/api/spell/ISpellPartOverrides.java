package am2.api.spell;

import net.minecraft.util.ResourceLocation;

/**
 * Interface through which {@link SpellPart} and {@link SpellComponent} query
 * config-file overrides at runtime.
 *
 * <p>Implement this interface in a {@link net.minecraftforge.common.config.Configuration}
 * subclass and register the instance once (post-init) via
 * {@link SpellPart#setOverrides(ISpellPartOverrides)}.
 *
 * <p>All methods must be safe to call frequently and from any thread that the
 * spell system runs on; implementations should return from an in-memory cache.
 */
public interface ISpellPartOverrides {

    /**
     * Returns override ingredient strings for the given spell part's crafting-altar
     * recipe, or {@code null} / empty array to fall back to the built-in
     * {@link SpellPart#getRecipe()}.
     *
     * <p>String semantics match {@link SpellIngredientParser}.
     */
    String[] getRecipeOverride(ResourceLocation name);

    /**
     * Returns an override base mana cost for the given {@link SpellComponent},
     * or {@code null} to fall back to the built-in {@link SpellComponent#manaCost()}.
     */
    Float getManaCostOverride(ResourceLocation name);

    /**
     * Returns override reagent strings for the given {@link SpellComponent},
     * or {@code null} / empty array to fall back to the built-in
     * {@link SpellComponent#reagents(net.minecraft.entity.EntityLivingBase)}.
     *
     * <p>String semantics: same {@code modid:item[:meta]} format as
     * {@link SpellIngredientParser}; ore-dict and essence entries are silently ignored.
     */
    String[] getReagentsOverride(ResourceLocation name);

    /**
     * Returns an override mana cost multiplier for the given {@link SpellShape}
     * or {@link SpellModifier}, or {@code null} to fall back to the built-in
     * {@link SpellShape#manaCostMultiplier()} / {@link SpellModifier#getManaCostMultiplier()}.
     */
    default Float getManaCostMultiplierOverride(ResourceLocation name) { return null; }
}

package am2.api.spell;

import am2.api.skill.Skill;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.EnumSet;


public abstract class SpellPart extends IForgeRegistryEntry.Impl<SpellPart> implements Comparable<SpellPart> {

    /**
     * Forge registry-based replacement for the internal affinity list.
     */
    public static IForgeRegistry<SpellPart> registry;

    /**
     * The single config-backed override source for recipes, mana costs, and reagents.
     * Set once during mod post-init via {@link #setOverrides(ISpellPartOverrides)}.
     */
    private static ISpellPartOverrides overrides;

    /**
     * Installs the config-backed override provider.
     * Called once during mod post-init after the spell registry is fully populated.
     *
     * @param provider implementation that reads {@code spell_parts.cfg}
     */
    public static void setOverrides(ISpellPartOverrides provider) {
        overrides = provider;
    }

    /**
     * Returns the active override provider, or {@code null} if not yet installed.
     * Package-visible so that {@link SpellComponent} can call through it.
     */
    static ISpellPartOverrides getOverrides() {
        return overrides;
    }

    public SpellPart() {
        this.id = nextSpellPartId++;
    }

    /**
     * Returns the built-in crafting-altar ingredient list for this spell part.
     * Supports:
     * <ul>
     *   <li>{@link net.minecraft.item.ItemStack} instances</li>
     *   <li>{@link net.minecraft.item.Item} instances (meta 0)</li>
     *   <li>{@link net.minecraft.block.Block} instances (meta 0)</li>
     *   <li>OreDict {@code String}s</li>
     *   <li>Essence strings ({@code "E:mask1|mask2"}) followed by an {@code Integer} amount</li>
     * </ul>
     *
     * <p><strong>Prefer calling {@link #getEffectiveRecipe()} in consumer code</strong>
     * so that config-file overrides are respected.
     */
    public Object[] getRecipe() {
        return new Object[0];
    }
    /**
     * Returns the effective crafting-altar ingredient list for this spell part.
     *
     * <p>If a config override has been registered via
     * {@link #setRecipeOverrideProvider} and the config file contains a non-empty
     * entry for this part, the override is parsed and returned.  Otherwise the
     * built-in {@link #getRecipe()} result is returned.
     *
     * <p><strong>Always prefer this method over {@link #getRecipe()} in consumer code.</strong>
     *
     * @return recipe {@code Object[]} suitable for the crafting altar; may be {@code null}
     *         if {@link #getRecipe()} returns {@code null}
     */
    public final Object[] getEffectiveRecipe() {
        ResourceLocation name = getRegistryName();
        if (overrides != null && name != null) {
            String[] overrideStrings = overrides.getRecipeOverride(name);
            if (overrideStrings != null && overrideStrings.length > 0) {
                return SpellIngredientParser.parseIngredients(overrideStrings);
            }
        }
        return getRecipe();
    }

    public void encodeBasicData(NBTTagCompound tag, Object[] recipe) {
    }

    private int id;

    /**
     * Gets an spell part instance from its network ID, or null if no such spell part exists.
     */
    public static SpellPart byNetworkID(int id) {
        if (id < 0 || id >= registry.getValuesCollection().size()) {
            return null;
        }
        return registry.getValuesCollection().stream().filter(s -> s.id == id).findAny().orElse(null);
    }

    public final int networkID() {
        return id;
    }

    public Skill getSkill() {
        return Skill.registry.getValue(getRegistryName());
    }

    private static int nextSpellPartId = 0;

    /**
     * What modifier affect this spell part?
     *
     * @return
     */
    public abstract EnumSet<SpellModifiers> getModifiers();

    @Override
    public int compareTo(SpellPart o) {
        if (this instanceof SpellShape && o instanceof SpellShape)
            return 0;
        if (this instanceof SpellShape)
            return -1;
        if (o instanceof SpellShape)
            return 1;
        return 0;
    }
}

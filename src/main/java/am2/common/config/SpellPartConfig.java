package am2.common.config;

import am2.api.ArsMagicaAPI;
import am2.api.spell.*;
import am2.common.compat.electroblob.EBWizardryCompatBootstrap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Config file ({@code spell_parts.cfg}) that exposes three per-spell-part knobs to
 * server operators and modpack authors:
 *
 * <ul>
 *   <li><b>{@code [crafting_recipes]}</b> – override the crafting-altar ingredient list</li>
 *   <li><b>{@code [mana_costs]}</b>       – override the base mana cost per cast</li>
 *   <li><b>{@code [reagents]}</b>         – override the inventory reagents required per cast</li>
 * </ul>
 *
 * <p>All sections are pre-populated with the built-in defaults on the first run.
 * Restoring an entry to its default silently falls through to the built-in value.
 *
 * <h3>Lifecycle</h3>
 * <ol>
 *   <li>Instantiate once in mod pre-init.</li>
 *   <li>Call {@link #reload()} after the spell registry is fully populated (post-init).</li>
 *   <li>Register the instance via {@link SpellPart#setOverrides(ISpellPartOverrides)}.</li>
 * </ol>
 */
public class SpellPartConfig extends Configuration implements ISpellPartOverrides {

    // ---- section names ------------------------------------------------------
    public static final String CATEGORY_CRAFTING_RECIPES = "crafting_recipes";
    public static final String CATEGORY_MANA_COSTS       = "mana_costs";
    public static final String CATEGORY_REAGENTS         = "reagents";
    public static final String CATEGORY_MANA_COST_MULTIPLIERS = "mana_cost_multipliers";

    private static final org.apache.logging.log4j.Logger LOGGER = am2.ArsMagica.LOGGER;

    // ---- in-memory caches (populated by reload()) ---------------------------
    private final Map<String, String[]> recipeOverrides   = new HashMap<>();
    private final Map<String, Float>    manaCostOverrides = new HashMap<>();
    private final Map<String, String[]> reagentOverrides  = new HashMap<>();
    private final Map<String, Float>    manaCostMultiplierOverrides = new HashMap<>();

    /** EBWiz-specific recipe defaults; populated once on first reload(). Null until then. */
    private Map<String, String[]> ebwizRecipeDefaults = null;

    public SpellPartConfig(File file) {
        super(file);
    }

    // =========================================================================
    // ISpellPartOverrides
    // =========================================================================

    @Override
    public String[] getRecipeOverride(ResourceLocation name) {
        return name != null ? recipeOverrides.get(name.toString()) : null;
    }

    @Override
    public Float getManaCostOverride(ResourceLocation name) {
        return name != null ? manaCostOverrides.get(name.toString()) : null;
    }

    @Override
    public String[] getReagentsOverride(ResourceLocation name) {
        return name != null ? reagentOverrides.get(name.toString()) : null;
    }

    @Override
    public Float getManaCostMultiplierOverride(ResourceLocation name) {
        return name != null ? manaCostMultiplierOverrides.get(name.toString()) : null;
    }

    // =========================================================================
    // Reload
    // =========================================================================

    /**
     * (Re-)reads the config file and rebuilds all three override caches.
     *
     * <p>Safe to call multiple times (e.g., on {@code ConfigChangedEvent}).
     * Must be called <em>after</em> the spell registry is populated so that
     * built-in defaults can be written for every spell part.
     */
    public void reload() {
        load();
        recipeOverrides.clear();
        manaCostOverrides.clear();
        reagentOverrides.clear();
        manaCostMultiplierOverrides.clear();

        // Fetch EBWiz defaults once (they are static, but we defer until reload() so that
        // Loader.isModLoaded() returns a stable value).
        if (ebwizRecipeDefaults == null)
            ebwizRecipeDefaults = EBWizardryCompatBootstrap.getEBWizRecipeDefaults();

        addCategoryComments();

        for (SpellPart part : ArsMagicaAPI.getSpellRegistry().getValuesCollection()) {
            if (part == null || part.getRegistryName() == null) continue;
            String key = part.getRegistryName().toString();

            loadRecipeEntry(part, key);

            if (part instanceof SpellComponent) {
                SpellComponent comp = (SpellComponent) part;
                loadManaEntry(comp, key);
                loadReagentEntry(comp, key);
            }

            if (part instanceof SpellShape) {
                loadManaCostMultiplierEntry(key, ((SpellShape) part).manaCostMultiplier());
            } else if (part instanceof SpellModifier) {
                loadManaCostMultiplierEntry(key, ((SpellModifier) part).getManaCostMultiplier());
            }
        }

        save();
        LOGGER.info("SpellPartConfig loaded – {} recipe, {} mana, {} reagent, {} mana-multiplier override(s) active.",
                recipeOverrides.size(), manaCostOverrides.size(), reagentOverrides.size(), manaCostMultiplierOverrides.size());
    }

    // =========================================================================
    // Private – per-section loaders
    // =========================================================================

    private void loadRecipeEntry(SpellPart part, String key) {
        // Compute serialised AM2 built-in default.
        String[] am2Defaults = new String[0];
        try {
            Object[] builtIn = part.getRecipe();
            if (builtIn != null && builtIn.length > 0)
                am2Defaults = SpellIngredientParser.serializeRecipe(builtIn);
        } catch (Exception e) {
            LOGGER.warn("Could not serialize built-in recipe for '{}': {}", key, e.getMessage());
        }

        // When EBWiz is loaded, prefer elemental magic_crystal substitutions over the
        // AM2 rune-based defaults.  The EBWiz strings are written to the config file so
        // server operators see them as the starting point.
        String[] ebwizDefaults = (ebwizRecipeDefaults != null) ? ebwizRecipeDefaults.get(key) : null;
        String[] writtenDefaults = (ebwizDefaults != null) ? ebwizDefaults : am2Defaults;

        String comment = "Crafting-altar ingredients for " + key + ".\n"
                + "Leave unchanged (or remove) to use the built-in default.";
        if (ebwizDefaults != null)
            comment += "\nEBWiz is loaded – these are the EBWiz-compatible defaults (magic_crystal substitutions).";

        Property prop = this.get(CATEGORY_CRAFTING_RECIPES, key, writtenDefaults, comment);
        prop.setRequiresWorldRestart(true);

        String[] value = prop.getStringList();
        boolean matchesAM2    = arraysEqualTrimmed(value, am2Defaults);
        boolean matchesEBWiz  = ebwizDefaults != null && arraysEqualTrimmed(value, ebwizDefaults);
        boolean isEmpty        = !hasNonEmpty(value);

        if (!isEmpty && !matchesAM2 && !matchesEBWiz) {
            // Genuinely custom value – respect it regardless of EBWiz state.
            recipeOverrides.put(key, value);
            LOGGER.debug("Loaded recipe override for '{}'", key);
        } else if (ebwizDefaults != null) {
            // EBWiz is loaded and no custom override: inject EBWiz defaults so that
            // getRecipeOverride() returns them instead of falling through to the AM2 built-in.
            recipeOverrides.put(key, ebwizDefaults);
            LOGGER.debug("Applied EBWiz recipe defaults for '{}'", key);
        }
        // else: no EBWiz defaults and value matches AM2 built-in (or is empty) → fall through.
    }

    private void loadManaEntry(SpellComponent comp, String key) {
        float defaultMana = comp.manaCost();
        Property prop = this.get(CATEGORY_MANA_COSTS, key, (double) defaultMana,
                "Base mana cost per cast for " + key + " (built-in: " + defaultMana + ").\n"
                + "Leave at the built-in value (or remove) to use the default.");
        prop.setRequiresWorldRestart(false);

        float configMana = (float) prop.getDouble(defaultMana);
        if (Math.abs(configMana - defaultMana) > 0.0001f) {
            manaCostOverrides.put(key, configMana);
            LOGGER.debug("Loaded mana cost override for '{}': {}", key, configMana);
        }
    }

    private void loadReagentEntry(SpellComponent comp, String key) {
        String[] defaults = new String[0];
        try {
            ItemStack[] builtIn = comp.reagents(null);
            if (builtIn != null && builtIn.length > 0)
                defaults = SpellIngredientParser.serializeReagents(builtIn);
        } catch (Exception e) {
            LOGGER.warn("Could not serialize built-in reagents for '{}': {}", key, e.getMessage());
        }

        Property prop = this.get(CATEGORY_REAGENTS, key, defaults,
                "Inventory reagents required per cast for " + key + ".\n"
                + "Same item format as crafting_recipes; OreDict and Essence entries are ignored.\n"
                + "Empty list = no reagents required.");
        prop.setRequiresWorldRestart(false);

        String[] value = prop.getStringList();
        if (!arraysEqualTrimmed(value, defaults)) {
            reagentOverrides.put(key, value);
            LOGGER.debug("Loaded reagent override for '{}'", key);
        }
    }

    private void loadManaCostMultiplierEntry(String key, float defaultMultiplier) {
        Property prop = this.get(CATEGORY_MANA_COST_MULTIPLIERS, key, (double) defaultMultiplier,
                "Mana cost multiplier for " + key + " (built-in: " + defaultMultiplier + ").\n"
                + "Shapes/modifiers multiply the base mana cost of a spell.\n"
                + "Leave at the built-in value (or remove) to use the default.");
        prop.setRequiresWorldRestart(false);

        float configVal = (float) prop.getDouble(defaultMultiplier);
        if (Math.abs(configVal - defaultMultiplier) > 0.0001f) {
            manaCostMultiplierOverrides.put(key, configVal);
            LOGGER.debug("Loaded mana cost multiplier override for '{}': {}", key, configVal);
        }
    }

    // =========================================================================
    // Private – utility
    // =========================================================================

    private void addCategoryComments() {
        String resetNote = "\n"
                + "Resetting to defaults:\n"
                + "  - Delete a single entry (including the key, not just the value) to restore just that spell part's built-in default into this config file.\n"
                + "  - Leaving an entry empty will result in using the built-in defaults.\n"
                + "  - Delete this entire file to regenerate all defaults from scratch.\n"
                + "  Changes take effect on the next game startup.";

        this.addCustomCategoryComment(CATEGORY_CRAFTING_RECIPES,
                "Override crafting-altar ingredient lists for any registered spell part.\n"
                + "Key: full registry name (e.g. arsmagica2:fire_damage).\n"
                + "Built-in defaults are pre-populated; restoring to default re-enables the built-in.\n"
                + "\n"
                + "Ingredient formats:\n"
                + "  modid:item               -- item, metadata 0\n"
                + "  modid:item:meta          -- item with specific metadata\n"
                + "  modid:item:*             -- item with wildcard metadata\n"
                + "  modid:item:meta:{nbt}    -- item with metadata and raw JSON NBT\n"
                + "  ore:oreDictName          -- OreDict entry\n"
                + "  E:type:amount            -- Essence (type: * any, 1 neutral, 2 light, 4 dark,\n"
                + "                             combinable with | e.g. E:1|2:1000)"
                + resetNote);

        this.addCustomCategoryComment(CATEGORY_MANA_COSTS,
                "Override the base mana cost per cast for any registered SpellComponent.\n"
                + "Key: full registry name. Value: float.\n"
                + "Built-in defaults are pre-populated."
                + resetNote);

        this.addCustomCategoryComment(CATEGORY_REAGENTS,
                "Override inventory reagents required per cast for any registered SpellComponent.\n"
                + "Key: full registry name. Values: modid:item[:meta] strings (same format as\n"
                + "crafting_recipes minus OreDict and Essence entries).\n"
                + "Empty list means no reagents are required.\n"
                + "Built-in defaults are pre-populated."
                + resetNote);

        this.addCustomCategoryComment(CATEGORY_MANA_COST_MULTIPLIERS,
                "Override the mana cost multiplier for any registered SpellShape or SpellModifier.\n"
                + "Key: full registry name. Value: float.\n"
                + "Shapes and modifiers multiply the base mana cost of a spell.\n"
                + "Built-in defaults are pre-populated."
                + resetNote);
    }

    private static boolean hasNonEmpty(String[] arr) {
        if (arr == null) return false;
        for (String s : arr)
            if (s != null && !s.trim().isEmpty()) return true;
        return false;
    }

    private static boolean arraysEqualTrimmed(String[] a, String[] b) {
        if (a == null) a = new String[0];
        if (b == null) b = new String[0];
        if (a.length != b.length) return false;
        for (int i = 0; i < a.length; i++) {
            String sa = a[i] == null ? "" : a[i].trim();
            String sb = b[i] == null ? "" : b[i].trim();
            if (!sa.equalsIgnoreCase(sb)) return false;
        }
        return true;
    }
}

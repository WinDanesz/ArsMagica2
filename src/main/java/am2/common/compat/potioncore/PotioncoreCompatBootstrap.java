package am2.common.compat.potioncore;

import am2.api.compendium.CompendiumCategory;
import am2.api.compendium.CompendiumEntry;
import am2.api.spell.SpellPart;
import am2.common.LogHelper;
import am2.common.registry.CompendiumRegistry;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.registries.IForgeRegistry;

/**
 * Bootstrap for PotionCore compatibility.
 *
 * <p>Features provided when PotionCore is loaded:
 * <ul>
 *   <li>{@code corrosion} spell component – applies the {@code potioncore:corrosion} potion effect,
 *       which damages the target's armor and held items (except gold).</li>
 * </ul>
 * All public methods are safe to call unconditionally; the mod-presence check is performed inside each one.
 */
public final class PotioncoreCompatBootstrap {

    /** Mod ID for PotionCore. */
    public static final String MODID = "potioncore";

    private PotioncoreCompatBootstrap() {}

    /** Returns {@code true} only when PotionCore is loaded. */
    public static boolean isLoaded() {
        return Loader.isModLoaded(MODID);
    }

    /**
     * Performs any runtime registration needed. Call during post-init.
     * Safe to call unconditionally.
     */
    public static void register() {
        if (isLoaded()) {
            registerCompendiumEntries();
            LogHelper.info("PotionCore detected – AM2 compatibility module loaded.");
        }
    }

    private static void registerCompendiumEntries() {
        CompendiumEntry corrosion = new CompendiumEntry(null, "corrosion");
        corrosion.setCategory(CompendiumCategory.SPELL_COMPONENT);
        corrosion.setName("Corrosion");
        corrosion.addObject(
            "Metal corrodes. Stone crumbles. Even the finest plate cannot hold forever against the right kind of magic. "
            + "Corrosion eats away at the target's armor and held items, degrading durability with every passing moment. "
            + "Gold, by some quirk of its nature, is left untouched. !d"
            + "#5Duration#0 modifiers extend the corrosive effect, and #5Buff Power#0 accelerates the rate of decay. !d"
            + "Requires PotionCore to be installed."
        );
        corrosion.setUnlocked();
        CompendiumRegistry.registerEntry(corrosion);
    }

    /**
     * Registers all PotionCore-exclusive spell parts.
     * Safe to call unconditionally – does nothing when PotionCore is absent.
     */
    public static void registerSpellParts(IForgeRegistry<SpellPart> registry) {
        if (!isLoaded()) return;
        PotioncoreCompatHandler.registerSpellParts(registry);
    }
}

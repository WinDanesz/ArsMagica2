package am2.common.compat.ancientspellcraft;

import am2.api.compendium.CompendiumCategory;
import am2.api.compendium.CompendiumEntry;
import am2.api.spell.SpellPart;
import am2.common.registry.CompendiumRegistry;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.registries.IForgeRegistry;

/**
 * Bootstrap for AncientSpellcraft + Artemislib compatibility.
 *
 * <p>Features provided when both mods are loaded:
 * <ul>
 *   <li>{@code shrinkage} spell component – applies the {@code ancientspellcraft:shrinkage} potion effect.</li>
 *   <li>{@code growth} spell component – applies the {@code ancientspellcraft:growth} potion effect.</li>
 * </ul>
 * All public methods are safe to call unconditionally; the mod-presence check is performed inside each one.
 */
public final class AncientSpellcraftCompatBootstrap {

    /** Mod ID for AncientSpellcraft. */
    public static final String ANCIENTSPELLCRAFT_MODID = "ancientspellcraft";
    /** Mod ID for the required Artemislib library. */
    public static final String ARTEMISLIB_MODID = "artemislib";

    private AncientSpellcraftCompatBootstrap() {}

    /** Returns {@code true} only when both AncientSpellcraft and Artemislib are loaded. */
    public static boolean isLoaded() {
        return Loader.isModLoaded(ANCIENTSPELLCRAFT_MODID) && Loader.isModLoaded(ARTEMISLIB_MODID);
    }

    /**
     * Performs any runtime registration needed. Call during post-init.
     * Safe to call unconditionally.
     */
    public static void register() {
        if (isLoaded()) {
            registerCompendiumEntries();
            //LogHelper.info("AncientSpellcraft + Artemislib detected – AM2 compatibility module loaded.");
        }
    }

    private static void registerCompendiumEntries() {
        CompendiumEntry shrinkage = new CompendiumEntry(null, "shrinkage");
        shrinkage.setCategory(CompendiumCategory.SPELL_COMPONENT);
        shrinkage.setName("Shrinkage");
        shrinkage.addObject(
            "A flick of the wrist and the target collapses inward, their form dwindling until they are a fraction of what they were. "
            + "Shrinkage physically reduces the target's size, making them smaller and easier to handle. !d"
            + "#5Duration#0 modifiers extend how long they stay small, and #5Buff Power#0 increases the degree of shrinkage."
        );
        shrinkage.setUnlocked();
        CompendiumRegistry.registerEntry(shrinkage);

        CompendiumEntry growth = new CompendiumEntry(null, "growth");
        growth.setCategory(CompendiumCategory.SPELL_COMPONENT);
        growth.setName("Growth");
        growth.addObject(
            "The magic swells outward and so does the target — their body expanding, growing taller and broader until they loom over what surrounded them before. "
            + "Growth physically enlarges the target, causing them to take up considerably more space. !d"
            + "#5Duration#0 modifiers extend how long they remain enlarged, and #5Buff Power#0 increases the degree of growth."
        );
        growth.setUnlocked();
        CompendiumRegistry.registerEntry(growth);
    }

    /**
     * Registers the AncientSpellcraft-exclusive spell parts.
     * Safe to call unconditionally – does nothing when either mod is absent.
     */
    public static void registerSpellParts(IForgeRegistry<SpellPart> registry) {
        if (!isLoaded()) return;
        AncientSpellcraftCompatHandler.registerSpellParts(registry);
    }
}

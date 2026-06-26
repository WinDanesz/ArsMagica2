package am2.common.registry;

import am2.api.compendium.CompendiumCategory;
import am2.api.compendium.CompendiumEntry;
import am2.common.LogHelper;
import am2.common.lore.CompendiumXMLLoader;

public class CompendiumRegistry {

    public static void postInit() {
        // Load all compendium entries from XML
        CompendiumXMLLoader.loadFromXML();
    }

    /**
     * Register a compendium entry directly. Used by XML loader and addon mods.
     */
    public static void registerEntry(CompendiumEntry entry) {
        // The entry already has its category set, just add it
        CompendiumCategory category = entry.getCategory();
        if (category != null) {
            category.addEntry(entry);
        } else {
            LogHelper.warn("Attempted to register entry with no category: " + entry.getFullID());
        }
    }
}

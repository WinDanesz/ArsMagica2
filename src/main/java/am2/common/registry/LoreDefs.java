package am2.common.registry;

import am2.api.affinity.Affinity;
import am2.api.compendium.CompendiumCategory;
import am2.api.compendium.CompendiumEntry;
import am2.api.rituals.IRitualInteraction;
import am2.api.spell.SpellPart;
import am2.common.LogHelper;
import am2.common.entity.EntityFlicker;
import am2.common.lore.CompendiumXMLLoader;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;

import static am2.api.compendium.CompendiumCategory.*;

public class LoreDefs {

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
            LogHelper.warn("Attempted to register entry with no category: " + entry.getID());
        }
    }

    private static void createItemEntry(String name, ItemStack item, int textPages) {
        CompendiumEntry entry = new CompendiumEntry(null, name);
        for (int i = 1; i <= textPages; i++) {
            entry = entry.addObject("compendium." + name + ".page" + i);
        }
        if (item == null)
            LogHelper.debug("Missing item for : %s", name);
        entry.addObject(item);
        ITEM.addEntry(entry);
    }

    private static void createComponentEntry(String name, SpellPart component, int textPages) {
        CompendiumEntry entry = new CompendiumEntry(component, name);
        for (int i = 1; i <= textPages; i++) {
            entry = entry.addObject("compendium." + name + ".page" + i);
        }
        if (component == null)
            LogHelper.debug("Missing component for : %s", name);
        entry.addObject(component);
        if (component instanceof IRitualInteraction)
            entry.addObject(new IRitualInteraction.Wrapper((IRitualInteraction) component));
        SPELL_COMPONENT.addEntry(entry);
    }

    private static void createModifierEntry(String name, SpellPart mod, int textPages) {
        CompendiumEntry entry = new CompendiumEntry(mod, name);
        for (int i = 1; i <= textPages; i++) {
            entry = entry.addObject("compendium." + name + ".page" + i);
        }
        if (mod == null)
            LogHelper.debug("Missing modifier for : %s", name);
        entry.addObject(mod);
        SPELL_MODIFIER.addEntry(entry);
    }

    private static void createEntry(CompendiumCategory category, String name, int pages) {
        CompendiumEntry entry = new CompendiumEntry(null, name);
        for (int i = 1; i <= pages; i++) {
            entry = entry.addObject("compendium." + name + ".page" + i);
        }
        category.addEntry(entry);
    }

    private static void createMobEntry(String name, Entity entity, int pages) {
        CompendiumEntry entry = new CompendiumEntry(null, name);
        for (int i = 1; i <= pages; i++) {
            entry = entry.addObject("compendium." + name + ".page" + i);
        }
        entry.addObject(entity);
        MOB.addEntry(entry);
    }

    private static void createBossEntry(String name, Entity entity, int pages) {
        CompendiumEntry entry = new CompendiumEntry(null, name);
        for (int i = 1; i <= pages; i++) {
            entry = entry.addObject("compendium." + name + ".page" + i);
        }
        entry.addObject(entity);
        BOSS.addEntry(entry);
    }

    private static void createFlickerEntry(String name, Affinity aff, int pages) {
        CompendiumEntry entry = new CompendiumEntry(null, name);
        for (int i = 1; i <= pages; i++) {
            entry = entry.addObject("compendium." + name + ".page" + i);
        }
        EntityFlicker flicker = new EntityFlicker(null);
        flicker.setFlickerType(aff.getID());
        entry.addObject(flicker);
        MOB_FLICKER.addEntry(entry);
    }

}

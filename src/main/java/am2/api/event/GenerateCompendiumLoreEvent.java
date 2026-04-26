package am2.api.event;

import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Fired when the Arcane Compendium is generating the lore text for a given entry.
 *
 * <p>Listeners can read {@link #entry} to identify which compendium entry is being
 * processed and modify {@link #lore} to append or replace the displayed text.</p>
 *
 * <p>Posted on {@link net.minecraftforge.common.MinecraftForge#EVENT_BUS}.</p>
 */
public class GenerateCompendiumLoreEvent extends Event {

    /** The lore/description text that will be displayed. Modify this to change the displayed text. */
    public String lore;
    /** The registry key / identifier of the compendium entry being generated. */
    public String entry;

    /**
     * @param lore  the initial lore text for the entry
     * @param entry the identifier of the compendium entry
     */
    public GenerateCompendiumLoreEvent(String lore, String entry) {
        this.lore = lore;
        this.entry = entry;
    }

}

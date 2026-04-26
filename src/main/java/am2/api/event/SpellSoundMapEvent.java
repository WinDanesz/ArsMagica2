package am2.api.event;

import am2.api.affinity.Affinity;
import am2.common.LogHelper;
import am2.common.registry.Affinities;
import com.google.common.collect.ImmutableMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.Map;

/**
 * Fired when a spell sound map is being assembled for a named sound group.
 *
 * <p>AM2 uses sound maps to select the appropriate casting sound per affinity.
 * Listeners can call {@link #put(Affinity, SoundEvent)} to register additional
 * affinity-to-sound mappings, or override existing ones for the named map.
 * The map name is used to distinguish between different sound contexts (e.g.,
 * spell cast start vs. spell impact sounds).</p>
 *
 * <p>Posted on {@link net.minecraftforge.common.MinecraftForge#EVENT_BUS}.</p>
 */
public class SpellSoundMapEvent extends Event {

    /** Identifies which sound map is being built (e.g., cast start, impact). */
    private final ResourceLocation mapName;
    private final ImmutableMap.Builder<Affinity, SoundEvent> map;

    /**
     * @param mapName the identifier of the sound map being assembled
     */
    public SpellSoundMapEvent(ResourceLocation mapName) {
        this.mapName = mapName;
        this.map = ImmutableMap.builder();
    }

    /**
     * Returns the fully built affinity-to-sound map after all listeners have run.
     * Call this only <em>after</em> the event has been posted.
     *
     * @return an immutable map of {@link Affinity} to {@link SoundEvent}
     */
    public Map<Affinity, SoundEvent> getMap() {
        return this.map.build();
    }

    /** @return the resource location identifying this sound map */
    public ResourceLocation getMapName() {
        return this.mapName;
    }

    /**
     * Registers a sound for a given affinity in this map.
     *
     * <p>If {@code aff} is {@code null} it defaults to {@link Affinities#none}.
     * A {@code null} sound is rejected and an error is logged.</p>
     *
     * @param aff   the affinity to associate the sound with
     * @param sound the sound event to play for that affinity
     */
    public void put(Affinity aff, SoundEvent sound) {
        if (aff == null)
            aff = Affinities.none;
        if (sound == null) {
            LogHelper.error("A mod tried to add a null sound to {0}", aff);
            return;
        }
        map.put(aff, sound);
    }
}

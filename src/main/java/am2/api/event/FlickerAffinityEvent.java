package am2.api.event;

import am2.common.entity.EntityFlicker;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.ArrayList;

/**
 * Fired when the valid affinities for a {@link EntityFlicker} are being determined
 * based on the biome it spawned in.
 *
 * <p>Listeners may call {@link #getValidAffinity()} to inspect or mutate the list
 * of affinity IDs that are considered valid for this flicker. Affinity IDs correspond
 * to the integer identifiers used internally by AM2.</p>
 *
 * <p>Posted on {@link net.minecraftforge.common.MinecraftForge#EVENT_BUS}.</p>
 */
public class FlickerAffinityEvent extends Event {

    private final ArrayList<Integer> validAffinity;
    private final EntityFlicker flicker;
    private final Biome biome;

    //How to go back to 1.7.10. INTEGER ID FOR EVERYTHING

    /**
     * @param validAffinity mutable list of valid affinity IDs for this flicker
     * @param flicker       the flicker entity being checked
     * @param biome         the biome the flicker is in
     */
    public FlickerAffinityEvent(ArrayList<Integer> validAffinity, EntityFlicker flicker, Biome biome) {
        this.validAffinity = validAffinity;
        this.flicker = flicker;
        this.biome = biome;
    }

    /** @return the biome the flicker is located in */
    public Biome getBiome() {
        return biome;
    }

    /** @return the flicker entity whose affinity is being resolved */
    public EntityFlicker getFlicker() {
        return flicker;
    }

    /**
     * Returns the mutable list of valid affinity IDs. Add or remove IDs to
     * influence which affinity the flicker may receive.
     *
     * @return mutable list of valid affinity IDs
     */
    public ArrayList<Integer> getValidAffinity() {
        return validAffinity;
    }
}

package am2.api.flickers;

import am2.ArsMagica;
import am2.api.affinity.Affinity;
import am2.common.LogHelper;
import am2.common.registry.Affinities;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;

/**
 * This class is used in the flicker lure, it manages the different flicker generation weights
 *
 * @author Edwin
 *
 */
public class FlickerGenerationPool {

    public static FlickerGenerationPool INSTANCE = new FlickerGenerationPool();

    private HashMap<Affinity, Integer> map = new HashMap<>();
    private boolean initialized = false;

    private FlickerGenerationPool() {
        // Defaults are applied lazily in ensureDefaults() because Affinities registry
        // entries are not yet populated when this static initializer runs during preInit.
    }

    /**
     * Ensures default weights are populated if loadFromConfig() hasn't been called yet.
     * Safe to call after registry events have fired.
     */
    private void ensureDefaults() {
        if (!initialized) {
            initialized = true;
            addWeightedAffinity(Affinities.air, 50);
            addWeightedAffinity(Affinities.arcane, 25);
            addWeightedAffinity(Affinities.earth, 50);
            addWeightedAffinity(Affinities.ender, 5);
            addWeightedAffinity(Affinities.fire, 50);
            addWeightedAffinity(Affinities.ice, 25);
            addWeightedAffinity(Affinities.life, 5);
            addWeightedAffinity(Affinities.lightning, 25);
            addWeightedAffinity(Affinities.nature, 25);
            addWeightedAffinity(Affinities.water, 50);
        }
    }

    /**
     * Reloads flicker generation weights from the config.
     * Called after config init is complete.
     */
    public void loadFromConfig() {
        initialized = true;
        map.clear();
        addWeightedAffinity(Affinities.air, ArsMagica.config.getFlickerWeightAir());
        addWeightedAffinity(Affinities.arcane, ArsMagica.config.getFlickerWeightArcane());
        addWeightedAffinity(Affinities.earth, ArsMagica.config.getFlickerWeightEarth());
        addWeightedAffinity(Affinities.ender, ArsMagica.config.getFlickerWeightEnder());
        addWeightedAffinity(Affinities.fire, ArsMagica.config.getFlickerWeightFire());
        addWeightedAffinity(Affinities.ice, ArsMagica.config.getFlickerWeightIce());
        addWeightedAffinity(Affinities.life, ArsMagica.config.getFlickerWeightLife());
        addWeightedAffinity(Affinities.lightning, ArsMagica.config.getFlickerWeightLightning());
        addWeightedAffinity(Affinities.nature, ArsMagica.config.getFlickerWeightNature());
        addWeightedAffinity(Affinities.water, ArsMagica.config.getFlickerWeightWater());
    }

    public void addWeightedAffinity(Affinity aff, int weight) {
        if (map.containsKey(aff))
            LogHelper.warn("Override : %s is already registered, report to the mod author", aff.getRegistryName().toString());
        map.put(aff, weight);
    }

    public int getTotalWeight() {
        ensureDefaults();
        int total = 0;
        for (Integer integer : map.values())
            if (integer != null) total += integer.intValue();
        return total;
    }

    public Affinity getWeightedAffinity() {
        ensureDefaults();
        int chosen = new Random().nextInt(getTotalWeight());
        int total = 0;
        for (Entry<Affinity, Integer> entry : map.entrySet()) {
            if (entry.getValue() != null) {
                total += entry.getValue().intValue();
                if (total > chosen)
                    return entry.getKey();
            }
        }
        return Affinities.none;
    }
}

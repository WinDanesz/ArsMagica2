package am2.common.lore;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Declarative description of how gated compendium entries are unlocked, shared by the unlock
 * handler, the progression report and the harness audit. Pure data: no Minecraft classes.
 * <p>
 * An entry is gated when it has a render object (spell parts, talents, structures, ritual shapes).
 * Every other entry is open from the start. Every gated entry must be covered here or by a skill unlock.
 */
public final class CompendiumProgression {
    private CompendiumProgression() {
    }

    /** Magic level at which all ritual entries unlock. */
    public static final int RITUAL_LEVEL = 15;

    /** Magic level at which structure entries unlock even if the player never held the controller block. */
    public static final int STRUCTURE_FALLBACK_LEVEL = 15;

    /** Structure entry id (in category "structure") -> registry path of the block that unlocks it on pickup/craft. */
    public static final Map<String, String> STRUCTURE_CONTROLLERS;

    /** Magic level -> bare entry ids unlocked when reaching that level. */
    public static final TreeMap<Integer, String[]> LEVEL_UNLOCKS = new TreeMap<>();

    static {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("obelisk_structure", "obelisk");
        m.put("mana_drain_block", "draining_well");
        m.put("blackaurem_structure", "black_aurem");
        m.put("celestialprism_structure", "celestial_prism");
        m.put("gateways", "keystone_receptacle");
        m.put("crafting_altar", "crafting_altar");
        STRUCTURE_CONTROLLERS = Collections.unmodifiableMap(m);

        LEVEL_UNLOCKS.put(5, new String[]{"enchantments"});
        LEVEL_UNLOCKS.put(10, new String[]{"armorMage", "playerjournal"});
        LEVEL_UNLOCKS.put(15, new String[]{"BossWaterGuardian", "BossEarthGuardian", "rituals", "inlays", "inlays_structure"});
        LEVEL_UNLOCKS.put(20, new String[]{"armorBattlemage"});
        LEVEL_UNLOCKS.put(25, new String[]{"BossAirGuardian", "BossArcaneGuardian", "BossLifeGuardian"});
        LEVEL_UNLOCKS.put(35, new String[]{"BossNatureGuardian", "BossWinterGuardian", "BossFireGuardian", "BossLightningGuardian", "BossEnderGuardian"});
    }
}

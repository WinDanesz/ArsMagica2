package am2.common.advancement;

import am2.ArsMagica;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.util.ResourceLocation;

public final class AMAdvancementTriggers {

    public static final AllSkillsUnlockedTrigger ALL_SKILLS_UNLOCKED =
            new AllSkillsUnlockedTrigger(new ResourceLocation(ArsMagica.MODID, "all_skills_unlocked"));

    public static final MagicLevelReachedTrigger MAGIC_LEVEL_REACHED =
            new MagicLevelReachedTrigger(new ResourceLocation(ArsMagica.MODID, "magic_level_reached"));

    private AMAdvancementTriggers() {}

    public static void register() {
        CriteriaTriggers.register(ALL_SKILLS_UNLOCKED);
        CriteriaTriggers.register(MAGIC_LEVEL_REACHED);
    }
}

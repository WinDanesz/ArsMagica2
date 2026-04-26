package am2.common.compat.potioncore;

import am2.api.skill.SkillPoint;
import am2.api.spell.SpellPart;
import am2.common.compat.potioncore.spells.Corrosion;
import am2.common.registry.SkillTrees;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;

/**
 * Registers PotionCore-exclusive AM2 spell components.
 *
 * <p>Only called from {@link PotioncoreCompatBootstrap#registerSpellParts} after
 * confirming PotionCore is loaded.
 */
public final class PotioncoreCompatHandler {

    private PotioncoreCompatHandler() {}

    public static void registerSpellParts(IForgeRegistry<SpellPart> registry) {
        am2.api.SpellRegistryHelper.registerSpellComponent(registry, "corrosion",
                new ResourceLocation(am2.ArsMagica.MODID, "items/spells/components/corrosion"),
                SkillPoint.SILVER_POINT,
                new Corrosion(),
                SkillTrees.TREE_OFFENSE, 75, 450);
    }
}

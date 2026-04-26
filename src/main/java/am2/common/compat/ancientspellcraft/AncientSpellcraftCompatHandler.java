package am2.common.compat.ancientspellcraft;

import am2.api.skill.SkillPoint;
import am2.api.spell.SpellPart;
import am2.common.compat.ancientspellcraft.spells.Growth;
import am2.common.compat.ancientspellcraft.spells.Shrinkage;
import am2.common.registry.SkillTrees;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;

/**
 * Registers AncientSpellcraft-exclusive AM2 spell components.
 *
 * <p>Only called from {@link AncientSpellcraftCompatBootstrap#registerSpellParts} after
 * confirming both AncientSpellcraft and Artemislib are loaded.
 */
public final class AncientSpellcraftCompatHandler {

    private AncientSpellcraftCompatHandler() {}

    public static void registerSpellParts(IForgeRegistry<SpellPart> registry) {
        am2.api.SpellRegistryHelper.registerSpellComponent(registry, "shrinkage",
                new ResourceLocation(am2.ArsMagica.MODID, "items/spells/components/shrinkage"),
                SkillPoint.SILVER_POINT,
                new Shrinkage(),
                SkillTrees.TREE_OFFENSE, 75, 360);
        am2.api.SpellRegistryHelper.registerSpellComponent(registry, "growth",
                new ResourceLocation(am2.ArsMagica.MODID, "items/spells/components/growth"),
                SkillPoint.SILVER_POINT,
                new Growth(),
                SkillTrees.TREE_OFFENSE, 75, 405);
    }
}

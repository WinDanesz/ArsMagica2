package am2.common.registry;

import am2.ArsMagica;
import am2.api.ArsMagicaAPI;
import am2.api.SkillPointRegistry;
import am2.api.SkillTreeRegistry;
import am2.api.skill.SkillPoint;
import am2.api.skill.SkillTree;
import am2.common.compat.electroblob.EBWizardryCompatBootstrap;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;

public class SkillTrees {

    public static final SkillTree TREE_OFFENSE = new SkillTree("offense", new ResourceLocation(ArsMagica.MODID, "textures/occulus/offense.png"), new ResourceLocation(ArsMagica.MODID, "textures/icons/offense.png"));
    public static final SkillTree TREE_DEFENSE = new SkillTree("defense", new ResourceLocation(ArsMagica.MODID, "textures/occulus/defense.png"), new ResourceLocation(ArsMagica.MODID, "textures/icons/defense.png"));
    public static final SkillTree TREE_UTILITY = new SkillTree("utility", new ResourceLocation(ArsMagica.MODID, "textures/occulus/utility.png"), new ResourceLocation(ArsMagica.MODID, "textures/icons/utility.png"));
    public static final SkillTree TREE_AFFINITY = new SkillTree("affinity", new ResourceLocation(ArsMagica.MODID, "textures/occulus/affinity.png"), new ResourceLocation(ArsMagica.MODID, "textures/icons/affinity.png")).disableRender("affinity");
    public static final SkillTree TREE_TALENT = new SkillTree("talent", new ResourceLocation(ArsMagica.MODID, "textures/occulus/talent.png"), new ResourceLocation(ArsMagica.MODID, "textures/icons/talent.png"));
    public static final SkillTree TREE_DISCIPLINE = new SkillTree("discipline", new ResourceLocation(ArsMagica.MODID, "textures/occulus/disciplines.png"), new ResourceLocation(ArsMagica.MODID, "textures/icons/discipline.png"));

    private SkillTrees() {
    } // no instances

    public static void init() {
        SkillTreeRegistry.registerSkillTree(TREE_OFFENSE);
        SkillTreeRegistry.registerSkillTree(TREE_DEFENSE);
        SkillTreeRegistry.registerSkillTree(TREE_UTILITY);
        SkillTreeRegistry.registerSkillTree(TREE_TALENT);
        if (Loader.isModLoaded(EBWizardryCompatBootstrap.MODID)) {
            SkillTreeRegistry.registerSkillTree(TREE_DISCIPLINE);
        }
        SkillTreeRegistry.registerSkillTree(TREE_AFFINITY);

        SkillPointRegistry.registerSkillPoint(-1, SkillPoint.SILVER_POINT);
        SkillPointRegistry.registerSkillPoint(0, SkillPoint.BLUE_SKILL_POINT);
        SkillPointRegistry.registerSkillPoint(1, SkillPoint.GREEN_SKILL_POINT);
        SkillPointRegistry.registerSkillPoint(2, SkillPoint.RED_SKILL_POINT);
        if (ArsMagicaAPI.hasTier4() || ArsMagicaAPI.hasTier5() || ArsMagicaAPI.hasTier6()) {
            SkillPointRegistry.registerSkillPoint(3, SkillPoint.YELLOW_SKILL_POINT);
        }
        if (ArsMagicaAPI.hasTier5() || ArsMagicaAPI.hasTier6()) {
            SkillPointRegistry.registerSkillPoint(4, SkillPoint.MAGENTA_SKILL_POINT);
        }
        if (ArsMagicaAPI.hasTier6()) {
            SkillPointRegistry.registerSkillPoint(5, SkillPoint.CYAN_SKILL_POINT);
        }

        //		SkillRegistry.registerSkill(mana_regen_1);
        //		SkillRegistry.registerSkill(mana_regen_2);
        //		SkillRegistry.registerSkill(mana_regen_3);
        //		SkillRegistry.registerSkill(mage_posse_1);
        //		SkillRegistry.registerSkill(mage_posse_2);
        //		SkillRegistry.registerSkill(spell_motion);
        //		SkillRegistry.registerSkill(augmented_casting);
        //		SkillRegistry.registerSkill(affinity_gains);
        //		SkillRegistry.registerSkill(extra_summons);
        //		SkillRegistry.registerSkill(shield_overload);
    }
}

package am2.api;

import am2.api.affinity.AbstractAffinityAbility;
import am2.api.affinity.Affinity;
import am2.api.flickers.AbstractFlickerFunctionality;
import am2.api.items.armor.ArmorImbuement;
import am2.api.skill.Skill;
import am2.api.spell.SpellPart;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.registries.*;

import javax.annotation.Nullable;

public class ArsMagicaAPI {

    private static final IForgeRegistry<AbstractAffinityAbility> ABILITY_REGISTRY;
    private static final IForgeRegistry<ArmorImbuement> IMBUEMENTS_REGISTRY;
    private static final IForgeRegistry<AbstractFlickerFunctionality> FLICKER_FOCUS_REGISTRY;

    private static boolean enableTier4 = false;
    private static boolean enableTier5 = false;
    private static boolean enableTier6 = false;


    static {
        ABILITY_REGISTRY = new RegistryBuilder<AbstractAffinityAbility>().setName(new ResourceLocation("arsmagica2", "affinityabilities")).setType(AbstractAffinityAbility.class).setIDRange(0, Short.MAX_VALUE).addCallback(ObjectCallbacks.AFFINITY).create();
        IMBUEMENTS_REGISTRY = new RegistryBuilder<ArmorImbuement>().setName(new ResourceLocation("arsmagica2", "armorimbuments")).setType(ArmorImbuement.class).setIDRange(0, Short.MAX_VALUE).addCallback(ObjectCallbacks.IMBUEMENT).create();
        FLICKER_FOCUS_REGISTRY = new RegistryBuilder<AbstractFlickerFunctionality>().setName(new ResourceLocation("arsmagica2", "flicker_focus")).setType(AbstractFlickerFunctionality.class).setIDRange(0, Short.MAX_VALUE).addCallback(ObjectCallbacks.FLICKER_FOCUS).create();
    }

    //Bonus to max mana.  Applied additively.
    public static final IAttribute maxManaBonus = new RangedAttribute(null, "am2.maxManaBonus", 0.0f, 0.0f, Double.MAX_VALUE).setDescription("Mana Bonus").setShouldWatch(true);
    //Bonus to max burnout.  Applied additively.
    public static final IAttribute maxBurnoutBonus = new RangedAttribute(null, "am2.maxBurnoutBonus", 0.0f, 0.0f, Double.MAX_VALUE).setDescription("Burnout Bonus").setShouldWatch(true);
    //Bonus to XP gained.  Applied multiplicatively.
    public static final IAttribute xpGainModifier = new RangedAttribute(null, "am2.xpMultiplier", 1.0f, 0.0f, Double.MAX_VALUE).setDescription("XP Mutiplier").setShouldWatch(true);
    //Bonus to mana regen rate.  Applied multiplicatively.
    public static final IAttribute manaRegenTimeModifier = new RangedAttribute(null, "am2.manaRegenModifier", 1.0f, 0.5f, 2.0f).setDescription("Mana Regen Rate Multiplier").setShouldWatch(true);
    //Bonus to burnout reduction rate.  Applied multiplicatively.
    public static final IAttribute burnoutReductionRate = new RangedAttribute(null, "am2.burnoutReduction", 1.0f, 0.1f, 2.0f).setDescription("Burnout Reduction Rate").setShouldWatch(true);


    public static IForgeRegistry<Affinity> getAffinityRegistry() {
        return Affinity.registry;
    }

    public static IForgeRegistry<AbstractAffinityAbility> getAffinityAbilityRegistry() {
        return (IForgeRegistry<AbstractAffinityAbility>) ABILITY_REGISTRY;
    }

    public static IForgeRegistry<ArmorImbuement> getArmorImbuementRegistry() {
        return (IForgeRegistry<ArmorImbuement>) IMBUEMENTS_REGISTRY;
    }

    public static IForgeRegistry<SpellPart> getSpellRegistry() {
        return SpellPart.registry;
    }

    public static IForgeRegistry<Skill> getSkillRegistry() {
        return Skill.registry;
    }

    public static IForgeRegistry<AbstractFlickerFunctionality> getFlickerFocusRegistry() {
        return (IForgeRegistry<AbstractFlickerFunctionality>) FLICKER_FOCUS_REGISTRY;
    }

    /**
     * Enable Tier 4, call in static{} for change to take effect.
     */
    public static void enableTier4() {
        enableTier4 = true;
    }

    public static boolean hasTier4() {
        return enableTier4 || hasTier5();
    }

    /**
     * Enable Tier 5, call in static{} for change to take effect.
     */
    public static void enableTier5() {
        enableTier5 = true;
    }

    public static boolean hasTier5() {
        return enableTier5 || hasTier6();
    }

    /**
     * Enable Tier 6, call in static{} for change to take effect.
     */
    public static void enableTier6() {
        enableTier6 = true;
    }

    public static boolean hasTier6() {
        return enableTier6;
    }

    public static String getCurrentModId() {
        ModContainer current = Loader.instance().activeModContainer();
        String modid = "arsmagica2";
        if (current != null)
            modid = current.getModId();
        return modid;
    }

    public static class ObjectCallbacks<T extends IForgeRegistryEntry<T>> implements IForgeRegistry.AddCallback<T>, IForgeRegistry.ClearCallback<T>, IForgeRegistry.CreateCallback<T> {
        static final ObjectCallbacks<SpellPart> SPELL = new SpellCallbacks();
        static final ObjectCallbacks<AbstractAffinityAbility> ABILITY = new ObjectCallbacks<>();
        static final ObjectCallbacks<Affinity> AFFINITY = new ObjectCallbacks<>();
        static final ObjectCallbacks<ArmorImbuement> IMBUEMENT = new ObjectCallbacks<>();
        static final ObjectCallbacks<Skill> SKILL = new ObjectCallbacks<>();
        static final ObjectCallbacks<AbstractFlickerFunctionality> FLICKER_FOCUS = new ObjectCallbacks<>();


        @Override
        public void onAdd(IForgeRegistryInternal<T> owner, RegistryManager stage, int id, T obj, @Nullable T oldObj) {

        }

        @Override
        public void onClear(IForgeRegistryInternal<T> owner, RegistryManager stage) {

        }

        @Override
        public void onCreate(IForgeRegistryInternal<T> owner, RegistryManager stage) {

        }
    }

    public static class SpellCallbacks extends ObjectCallbacks<SpellPart> {
    }
}
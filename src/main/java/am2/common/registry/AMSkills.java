package am2.common.registry;

import am2.ArsMagica;
import am2.api.skill.Skill;
import am2.api.skill.SkillPoint;
import am2.common.compat.ancientspellcraft.AncientSpellcraftCompatBootstrap;
import am2.common.compat.electroblob.EBWizardryCompatBootstrap;
import am2.common.compat.potioncore.PotioncoreCompatBootstrap;
import am2.common.items.ItemSpellComponent;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

@ObjectHolder(ArsMagica.MODID)
@Mod.EventBusSubscriber(modid = ArsMagica.MODID)
public class AMSkills {

    public static final Skill mana_regen_i = placeholder();
    public static final Skill mana_regen_ii = placeholder();
    public static final Skill mana_regen_iii = placeholder();
    public static final Skill mage_posse_i = placeholder();
    public static final Skill mage_posse_ii = placeholder();
    public static final Skill spell_motion = placeholder();
    public static final Skill augmented_casting = placeholder();
    public static final Skill affinity_gains = placeholder();
    public static final Skill extra_summons = placeholder();
    public static final Skill shield_overload = placeholder();
    public static final Skill colour = placeholder();
    public static final Skill meditation = placeholder();

    public static final Skill projectile = placeholder();
    public static final Skill orbs = placeholder();
    public static final Skill cone = placeholder();
    public static final Skill physical_damage = placeholder();
    public static final Skill gravity = placeholder();
    public static final Skill bounce = placeholder();
    public static final Skill fire_damage = placeholder();
    public static final Skill lightning_damage = placeholder();
    public static final Skill ignition = placeholder();
    public static final Skill forge = placeholder();
    public static final Skill magic_damage = placeholder();
    public static final Skill frost_damage = placeholder();
    public static final Skill drown = placeholder();
    public static final Skill blind = placeholder();
    public static final Skill poison = placeholder();
    public static final Skill wither = placeholder();
    public static final Skill aoe = placeholder();
    public static final Skill freeze = placeholder();
    public static final Skill ice_block = placeholder();
    public static final Skill knockback = placeholder();
    public static final Skill contingency_fire = placeholder();
    public static final Skill solar = placeholder();
    public static final Skill storm = placeholder();
    public static final Skill astral_distortion = placeholder();
    public static final Skill silence = placeholder();
    public static final Skill fling = placeholder();
    public static final Skill velocity_added = placeholder();
    public static final Skill watery_grave = placeholder();
    public static final Skill piercing = placeholder();
    public static final Skill beam = placeholder();
    public static final Skill damage = placeholder();
    public static final Skill fury = placeholder();
    public static final Skill wave = placeholder();
 //   public static final Skill slice = placeholder();
    public static final Skill blizzard = placeholder();
    public static final Skill falling_star = placeholder();
    public static final Skill fire_rain = placeholder();
    public static final Skill mana_blast = placeholder();
    public static final Skill dismembering = placeholder();
    public static final Skill self = placeholder();
    public static final Skill leap = placeholder();
    public static final Skill regeneration = placeholder();
    public static final Skill shrink = placeholder();
    public static final Skill slowfall = placeholder();
    public static final Skill heal = placeholder();
    public static final Skill life_tap = placeholder();
    public static final Skill healing = placeholder();
    public static final Skill summon = placeholder();
    public static final Skill contingency_damage = placeholder();
    public static final Skill haste = placeholder();
    public static final Skill slow = placeholder();
    public static final Skill gravity_well = placeholder();
    public static final Skill life_drain = placeholder();
    public static final Skill dispel = placeholder();
    public static final Skill contingency_fall = placeholder();
    public static final Skill swift_swim = placeholder();
    public static final Skill repel = placeholder();
    public static final Skill levitate = placeholder();
    public static final Skill mana_drain = placeholder();
    public static final Skill zone = placeholder();
    public static final Skill puddle = placeholder();
    public static final Skill gravitate = placeholder();
    public static final Skill wall = placeholder();
    public static final Skill accelerate = placeholder();
    public static final Skill entangle = placeholder();
    public static final Skill appropriation = placeholder();
    public static final Skill flight = placeholder();
    public static final Skill shield = placeholder();
    public static final Skill contingency_health = placeholder();
    public static final Skill rune = placeholder();
    public static final Skill rune_procs = placeholder();
    public static final Skill glyph = placeholder();
    public static final Skill speed = placeholder();
    public static final Skill reflect = placeholder();
    public static final Skill chrono_anchor = placeholder();
    public static final Skill duration = placeholder();
    public static final Skill absorption = placeholder();
    public static final Skill resistance = placeholder();
    public static final Skill mana_link = placeholder();
    public static final Skill mana_shield = placeholder();
    public static final Skill buff_power = placeholder();
    public static final Skill touch = placeholder();
    public static final Skill dig = placeholder();
    public static final Skill wizards_autumn = placeholder();
    public static final Skill target_non_solid = placeholder();
    public static final Skill place_block = placeholder();
    public static final Skill conjure_dirt = placeholder();
    public static final Skill feather_touch = placeholder();
    public static final Skill mining_power = placeholder();
    public static final Skill light = placeholder();
    public static final Skill night_vision = placeholder();
    public static final Skill binding = placeholder();
    public static final Skill disarm = placeholder();
    public static final Skill charm = placeholder();
    public static final Skill alchemical_infusion = placeholder();
    public static final Skill true_sight = placeholder();
    public static final Skill reveal = placeholder();
    public static final Skill lunar = placeholder();
    public static final Skill harvest_plants = placeholder();
    public static final Skill plow = placeholder();
    public static final Skill plant = placeholder();
    public static final Skill create_water = placeholder();
    public static final Skill extinguish = placeholder();
    public static final Skill drought = placeholder();
    public static final Skill banish_rain = placeholder();
    public static final Skill water_breathing = placeholder();
    public static final Skill grow = placeholder();
    public static final Skill chain = placeholder();
    public static final Skill rift = placeholder();
    public static final Skill invisibility = placeholder();
    public static final Skill random_teleport = placeholder();
    public static final Skill attract = placeholder();
    public static final Skill telekinesis = placeholder();
    public static final Skill blink = placeholder();
    public static final Skill phase_shift = placeholder();
    public static final Skill range = placeholder();
    public static final Skill channel = placeholder();
    public static final Skill toggle = placeholder();
    public static final Skill radius = placeholder();
    public static final Skill transplace = placeholder();
    public static final Skill mark = placeholder();
    public static final Skill recall = placeholder();
    public static final Skill divine_intervention = placeholder();
    public static final Skill ender_intervention = placeholder();
    public static final Skill contingency_death = placeholder();
    public static final Skill daylight = placeholder();
    public static final Skill moonrise = placeholder();
    public static final Skill prosperity = placeholder();
    // EBWiz-exclusive modifier
    public static final Skill ebwiz_blast = placeholder();
    // EBWiz-exclusive component (only registered when Electroblob's Wizardry is present)
    public static final Skill ice_statue = placeholder();
    public static final Skill metamorphosis = placeholder();
    // AncientSpellcraft-exclusive components (only registered when AncientSpellcraft + Artemislib are present)
    public static final Skill shrinkage = placeholder();
    public static final Skill growth = placeholder();
    // PotionCore-exclusive skills (only registered when PotionCore is present)
    public static final Skill corrosion = placeholder();
    public static final Skill conjure_block = placeholder();
    public static final Skill crystallize = placeholder();

    // power system
    public static final Skill channel_etherium = placeholder();

    private AMSkills() {
    } // no instances!

    // This is here because this class is already an event handler.
    @SubscribeEvent
    public static void createRegistry(RegistryEvent.NewRegistry event) {
        RegistryBuilder<Skill> builder = new RegistryBuilder<>();
        builder.setType(Skill.class);
        builder.setName(new ResourceLocation(ArsMagica.MODID, "skills"));
        builder.setIDRange(0, 5000); // Is there any penalty for using a larger number?

        Skill.registry = builder.create();
    }

    @Nonnull
    @SuppressWarnings("ConstantConditions")
    private static <T> T placeholder() {
        return null;
    }

    @SubscribeEvent
    public static void register(RegistryEvent.Register<Skill> event) {
        IForgeRegistry<Skill> registry = event.getRegistry();

        // Dev
        registry.register(new Skill("none", null, null, 0, 0, null));
        registry.register(new Skill("melt_armor", null, null, 0, 0, null));
        registry.register(new Skill("nauseate", null, null, 0, 0, null));
        registry.register(new Skill("scramble_synapses", null, null, 0, 0, null));

        // Talent
        registry.register(new Skill("mana_regen_i", getTalentTexture("mana_regen_i"), SkillPoint.BLUE_SKILL_POINT, 275, 75, SkillTrees.TREE_TALENT));
        registry.register(new Skill("mana_regen_ii", getTalentTexture("mana_regen_ii"), SkillPoint.GREEN_SKILL_POINT, 275, 120, SkillTrees.TREE_TALENT, "arsmagica2:mana_regen_i"));
        registry.register(new Skill("mana_regen_iii", getTalentTexture("mana_regen_iii"), SkillPoint.RED_SKILL_POINT, 275, 165, SkillTrees.TREE_TALENT, "arsmagica2:mana_regen_ii"));
        registry.register(new Skill("mage_posse_i", getTalentTexture("mage_band_i"), SkillPoint.GREEN_SKILL_POINT, 320, 120, SkillTrees.TREE_TALENT, "arsmagica2:mana_regen_ii"));
        registry.register(new Skill("mage_posse_ii", getTalentTexture("mage_band_ii"), SkillPoint.RED_SKILL_POINT, 320, 165, SkillTrees.TREE_TALENT, "arsmagica2:mage_posse_i"));
        registry.register(new Skill("spell_motion", getTalentTexture("spell_motion"), SkillPoint.GREEN_SKILL_POINT, 230, 120, SkillTrees.TREE_TALENT, "arsmagica2:mana_regen_ii"));
        registry.register(new Skill("augmented_casting", getTalentTexture("augmented_casting"), SkillPoint.RED_SKILL_POINT, 230, 165, SkillTrees.TREE_TALENT, "arsmagica2:spell_motion"));
        registry.register(new Skill("affinity_gains", getTalentTexture("affinity_gains"), SkillPoint.BLUE_SKILL_POINT, 365, 120, SkillTrees.TREE_TALENT, "arsmagica2:mana_regen_i"));
        registry.register(new Skill("extra_summons", getTalentTexture("extra_summon"), SkillPoint.RED_SKILL_POINT, 230, 210, SkillTrees.TREE_TALENT, ArsMagica.config.getExtraSummonsMaxLevel(), "arsmagica2:augmented_casting"));
        registry.register(new Skill("shield_overload", getTalentTexture("shield_overload"), SkillPoint.SILVER_POINT, 275, 210, SkillTrees.TREE_TALENT));
        registry.register(new Skill("colour", getModifierTexture("Colour"), SkillPoint.BLUE_SKILL_POINT, 230, 75, SkillTrees.TREE_TALENT));
        registry.register(new Skill("meditation", getTalentTexture("meditation"), SkillPoint.RED_SKILL_POINT, 365, 165, SkillTrees.TREE_TALENT, "arsmagica2:mana_regen_ii"));

        // Offense
        registry.register(new Skill("projectile", getShapeTexture("projectile"), SkillPoint.BLUE_SKILL_POINT, 300, 45, SkillTrees.TREE_OFFENSE));
        registry.register(new Skill("orbs", getShapeTexture("orbs"), SkillPoint.RED_SKILL_POINT, 402, 225, SkillTrees.TREE_DEFENSE, "arsmagica2:zone"));
        registry.register(new Skill("cone", getShapeTexture("cone"), SkillPoint.RED_SKILL_POINT, 300, 360, SkillTrees.TREE_OFFENSE, "arsmagica2:damage"));
        registry.register(new Skill("physical_damage", getComponentTexture("physical_damage"), SkillPoint.BLUE_SKILL_POINT, 300, 90, SkillTrees.TREE_OFFENSE, "arsmagica2:projectile"));
        registry.register(new Skill("gravity", getModifierTexture("gravity"), SkillPoint.BLUE_SKILL_POINT, 255, 70, SkillTrees.TREE_OFFENSE, "arsmagica2:projectile"));
        registry.register(new Skill("bounce", getModifierTexture("bounce"), SkillPoint.BLUE_SKILL_POINT, 345, 70, SkillTrees.TREE_OFFENSE, "arsmagica2:projectile"));
        registry.register(new Skill("fire_damage", getComponentTexture("fire_damage"), SkillPoint.BLUE_SKILL_POINT, 210, 135, SkillTrees.TREE_OFFENSE, "arsmagica2:physical_damage"));
        registry.register(new Skill("lightning_damage", getComponentTexture("lightning_damage"), SkillPoint.BLUE_SKILL_POINT, 255, 135, SkillTrees.TREE_OFFENSE, "arsmagica2:fire_damage"));
        registry.register(new Skill("ignition", getComponentTexture("ignition"), SkillPoint.GREEN_SKILL_POINT, 165, 135, SkillTrees.TREE_OFFENSE, "arsmagica2:fire_damage"));
        registry.register(new Skill("forge", getComponentTexture("forge"), SkillPoint.GREEN_SKILL_POINT, 120, 135, SkillTrees.TREE_OFFENSE, "arsmagica2:ignition"));
        registry.register(new Skill("magic_damage", getComponentTexture("magic_damage"), SkillPoint.BLUE_SKILL_POINT, 435, 135, SkillTrees.TREE_OFFENSE, "arsmagica2:physical_damage"));
        registry.register(new Skill("frost_damage", getComponentTexture("frost_damage"), SkillPoint.BLUE_SKILL_POINT, 345, 135, SkillTrees.TREE_OFFENSE, "arsmagica2:magic_damage"));
        registry.register(new Skill("drown", getComponentTexture("drown"), SkillPoint.BLUE_SKILL_POINT, 480, 135, SkillTrees.TREE_OFFENSE, "arsmagica2:magic_damage"));
        registry.register(new Skill("blind", getComponentTexture("blind"), SkillPoint.GREEN_SKILL_POINT, 233, 180, SkillTrees.TREE_OFFENSE, "arsmagica2:fire_damage", "arsmagica2:lightning_damage"));
        registry.register(new Skill("poison", getComponentTexture("poison"), SkillPoint.GREEN_SKILL_POINT, 233, 225, SkillTrees.TREE_OFFENSE, "arsmagica2:blind"));
        registry.register(new Skill("wither", getComponentTexture("wither"), SkillPoint.GREEN_SKILL_POINT, 233, 270, SkillTrees.TREE_OFFENSE, "arsmagica2:poison"));
        registry.register(new Skill("aoe", getShapeTexture("aoe"), SkillPoint.GREEN_SKILL_POINT, 300, 180, SkillTrees.TREE_OFFENSE, "arsmagica2:frost_damage", "arsmagica2:physical_damage", "arsmagica2:fire_damage", "arsmagica2:lightning_damage", "arsmagica2:magic_damage"));
        registry.register(new Skill("freeze", getComponentTexture("freeze"), SkillPoint.GREEN_SKILL_POINT, 345, 180, SkillTrees.TREE_OFFENSE, "arsmagica2:frost_damage"));
        registry.register(new Skill("knockback", getComponentTexture("knockback"), SkillPoint.GREEN_SKILL_POINT, 435, 180, SkillTrees.TREE_OFFENSE, "arsmagica2:magic_damage"));
        registry.register(new Skill("contingency_fire", getShapeTexture("contingency_fire"), SkillPoint.GREEN_SKILL_POINT, 165, 190, SkillTrees.TREE_OFFENSE, "arsmagica2:ignition"));
        registry.register(new Skill("solar", getModifierTexture("solar"), SkillPoint.RED_SKILL_POINT, 185, 255, SkillTrees.TREE_OFFENSE, "arsmagica2:blind"));
        registry.register(new Skill("storm", getComponentTexture("storm"), SkillPoint.RED_SKILL_POINT, 278, 225, SkillTrees.TREE_OFFENSE, "arsmagica2:lightning_damage"));
        registry.register(new Skill("astral_distortion", getComponentTexture("astral_distortion"), SkillPoint.GREEN_SKILL_POINT, 390, 180, SkillTrees.TREE_OFFENSE, "arsmagica2:magic_damage", "arsmagica2:frost_damage"));
        registry.register(new Skill("silence", getComponentTexture("silence"), SkillPoint.RED_SKILL_POINT, 390, 290, SkillTrees.TREE_OFFENSE, "arsmagica2:astral_distortion"));
        registry.register(new Skill("fling", getComponentTexture("fling"), SkillPoint.GREEN_SKILL_POINT, 435, 245, SkillTrees.TREE_OFFENSE, "arsmagica2:knockback"));
        registry.register(new Skill("velocity_added", getModifierTexture("velocity_added"), SkillPoint.RED_SKILL_POINT, 435, 290, SkillTrees.TREE_OFFENSE, "arsmagica2:fling"));
        registry.register(new Skill("watery_grave", getComponentTexture("watery_grave"), SkillPoint.GREEN_SKILL_POINT, 480, 245, SkillTrees.TREE_OFFENSE, "arsmagica2:drown"));
        registry.register(new Skill("piercing", getModifierTexture("piercing"), SkillPoint.RED_SKILL_POINT, 323, 215, SkillTrees.TREE_OFFENSE, "arsmagica2:freeze"));
        registry.register(new Skill("beam", getShapeTexture("beam"), SkillPoint.RED_SKILL_POINT, 300, 270, SkillTrees.TREE_OFFENSE, "arsmagica2:aoe"));
        registry.register(new Skill("damage", getModifierTexture("damage"), SkillPoint.RED_SKILL_POINT, 300, 315, SkillTrees.TREE_OFFENSE, "arsmagica2:beam"));
        registry.register(new Skill("fury", getComponentTexture("fury"), SkillPoint.RED_SKILL_POINT, 255, 315, SkillTrees.TREE_OFFENSE, "arsmagica2:beam", "arsmagica2:storm"));
        registry.register(new Skill("wave", getShapeTexture("wave"), SkillPoint.RED_SKILL_POINT, 480, 315, SkillTrees.TREE_OFFENSE, "arsmagica2:beam", "arsmagica2:fling"));
//        registry.register(new Skill("slice", getShapeTexture("slice"), SkillPoint.RED_SKILL_POINT, 413, 315, SkillTrees.TREE_OFFENSE, "arsmagica2:wave"));
        registry.register(new Skill("blizzard", getComponentTexture("blizzard"), SkillPoint.SILVER_POINT, 75, 45, SkillTrees.TREE_OFFENSE));
        registry.register(new Skill("falling_star", getComponentTexture("falling_star"), SkillPoint.SILVER_POINT, 75, 90, SkillTrees.TREE_OFFENSE));
        registry.register(new Skill("fire_rain", getComponentTexture("fire_rain"), SkillPoint.SILVER_POINT, 75, 135, SkillTrees.TREE_OFFENSE));
        registry.register(new Skill("mana_blast", getComponentTexture("mana_blast"), SkillPoint.SILVER_POINT, 75, 180, SkillTrees.TREE_OFFENSE));
        registry.register(new Skill("dismembering", getModifierTexture("dismembering"), SkillPoint.SILVER_POINT, 75, 225, SkillTrees.TREE_OFFENSE));

        // Defense
        registry.register(new Skill("self", getShapeTexture("self"), SkillPoint.BLUE_SKILL_POINT, 267, 45, SkillTrees.TREE_DEFENSE));
        registry.register(new Skill("leap", getComponentTexture("leap"), SkillPoint.BLUE_SKILL_POINT, 222, 90, SkillTrees.TREE_DEFENSE, "arsmagica2:self"));
        registry.register(new Skill("regeneration", getComponentTexture("regeneration"), SkillPoint.BLUE_SKILL_POINT, 357, 90, SkillTrees.TREE_DEFENSE, "arsmagica2:self"));
        registry.register(new Skill("shrink", getComponentTexture("shrink"), SkillPoint.BLUE_SKILL_POINT, 402, 90, SkillTrees.TREE_DEFENSE, "arsmagica2:regeneration"));
        registry.register(new Skill("slowfall", getComponentTexture("slowfall"), SkillPoint.BLUE_SKILL_POINT, 222, 135, SkillTrees.TREE_DEFENSE, "arsmagica2:leap"));
        registry.register(new Skill("heal", getComponentTexture("heal"), SkillPoint.BLUE_SKILL_POINT, 357, 135, SkillTrees.TREE_DEFENSE, "arsmagica2:regeneration"));
        registry.register(new Skill("life_tap", getComponentTexture("life_tap"), SkillPoint.GREEN_SKILL_POINT, 312, 135, SkillTrees.TREE_DEFENSE, "arsmagica2:heal"));
        registry.register(new Skill("healing", getModifierTexture("healing"), SkillPoint.RED_SKILL_POINT, 402, 135, SkillTrees.TREE_DEFENSE, "arsmagica2:heal"));
        registry.register(new Skill("summon", getComponentTexture("summon"), SkillPoint.GREEN_SKILL_POINT, 267, 135, SkillTrees.TREE_DEFENSE, "arsmagica2:life_tap"));
        registry.register(new Skill("contingency_damage", getShapeTexture("contingency_damage"), SkillPoint.GREEN_SKILL_POINT, 447, 180, SkillTrees.TREE_DEFENSE, "arsmagica2:healing"));
        registry.register(new Skill("haste", getComponentTexture("haste"), SkillPoint.BLUE_SKILL_POINT, 177, 155, SkillTrees.TREE_DEFENSE, "arsmagica2:slowfall"));
        registry.register(new Skill("slow", getComponentTexture("slow"), SkillPoint.BLUE_SKILL_POINT, 132, 155, SkillTrees.TREE_DEFENSE, "arsmagica2:slowfall"));
        registry.register(new Skill("gravity_well", getComponentTexture("gravity_well"), SkillPoint.GREEN_SKILL_POINT, 222, 180, SkillTrees.TREE_DEFENSE, "arsmagica2:slowfall"));
        registry.register(new Skill("life_drain", getComponentTexture("life_drain"), SkillPoint.GREEN_SKILL_POINT, 312, 180, SkillTrees.TREE_DEFENSE, "arsmagica2:life_tap"));
        registry.register(new Skill("dispel", getComponentTexture("dispel"), SkillPoint.GREEN_SKILL_POINT, 357, 180, SkillTrees.TREE_DEFENSE, "arsmagica2:heal"));
        registry.register(new Skill("contingency_fall", getShapeTexture("contingency_fall"), SkillPoint.GREEN_SKILL_POINT, 267, 180, SkillTrees.TREE_DEFENSE, "arsmagica2:gravity_well"));
        registry.register(new Skill("swift_swim", getComponentTexture("swift_swim"), SkillPoint.BLUE_SKILL_POINT, 177, 200, SkillTrees.TREE_DEFENSE, "arsmagica2:haste"));
        registry.register(new Skill("repel", getComponentTexture("repel"), SkillPoint.GREEN_SKILL_POINT, 132, 200, SkillTrees.TREE_DEFENSE, "arsmagica2:slow"));
        registry.register(new Skill("levitate", getComponentTexture("levitate"), SkillPoint.GREEN_SKILL_POINT, 222, 225, SkillTrees.TREE_DEFENSE, "arsmagica2:gravity_well"));
        registry.register(new Skill("mana_drain", getComponentTexture("mana_drain"), SkillPoint.GREEN_SKILL_POINT, 312, 225, SkillTrees.TREE_DEFENSE, "arsmagica2:life_drain"));
        registry.register(new Skill("zone", getShapeTexture("zone"), SkillPoint.RED_SKILL_POINT, 357, 225, SkillTrees.TREE_DEFENSE, "arsmagica2:dispel"));
        registry.register(new Skill("puddle", getShapeTexture("puddle"), SkillPoint.RED_SKILL_POINT, 447, 270, SkillTrees.TREE_DEFENSE, "arsmagica2:zone"));
        registry.register(new Skill("gravitate", getModifierTexture("gravitate"), SkillPoint.GREEN_SKILL_POINT, 447, 315, SkillTrees.TREE_DEFENSE, "arsmagica2:puddle", "arsmagica2:zone"));
        registry.register(new Skill("wall", getShapeTexture("wall"), SkillPoint.GREEN_SKILL_POINT, 87, 200, SkillTrees.TREE_DEFENSE, "arsmagica2:repel"));
        registry.register(new Skill("accelerate", getComponentTexture("accelerate"), SkillPoint.GREEN_SKILL_POINT, 177, 245, SkillTrees.TREE_DEFENSE, "arsmagica2:swift_swim"));
        registry.register(new Skill("entangle", getComponentTexture("entangle"), SkillPoint.GREEN_SKILL_POINT, 132, 245, SkillTrees.TREE_DEFENSE, "arsmagica2:repel"));
        registry.register(new Skill("appropriation", getComponentTexture("appropriation"), SkillPoint.RED_SKILL_POINT, 87, 245, SkillTrees.TREE_DEFENSE, "arsmagica2:entangle"));
        registry.register(new Skill("flight", getComponentTexture("flight"), SkillPoint.RED_SKILL_POINT, 222, 270, SkillTrees.TREE_DEFENSE, "arsmagica2:levitate"));
        registry.register(new Skill("shield", getComponentTexture("shield"), SkillPoint.BLUE_SKILL_POINT, 357, 270, SkillTrees.TREE_DEFENSE, "arsmagica2:zone"));
        registry.register(new Skill("contingency_health", getShapeTexture("contingency_health"), SkillPoint.RED_SKILL_POINT, 402, 270, SkillTrees.TREE_DEFENSE, "arsmagica2:shield"));
        registry.register(new Skill("rune", getShapeTexture("rune"), SkillPoint.GREEN_SKILL_POINT, 157, 315, SkillTrees.TREE_DEFENSE, "arsmagica2:accelerate", "arsmagica2:entangle"));
        registry.register(new Skill("rune_procs", getModifierTexture("rune_procs"), SkillPoint.GREEN_SKILL_POINT, 157, 360, SkillTrees.TREE_DEFENSE, "arsmagica2:rune"));
        registry.register(new Skill("glyph", getShapeTexture("glyph"), SkillPoint.GREEN_SKILL_POINT, 112, 360, SkillTrees.TREE_DEFENSE, "arsmagica2:rune"));
        registry.register(new Skill("speed", getModifierTexture("speed"), SkillPoint.RED_SKILL_POINT, 202, 315, SkillTrees.TREE_DEFENSE, "arsmagica2:accelerate", "arsmagica2:flight"));
        registry.register(new Skill("reflect", getComponentTexture("reflect"), SkillPoint.RED_SKILL_POINT, 357, 315, SkillTrees.TREE_DEFENSE, "arsmagica2:shield"));
        registry.register(new Skill("chrono_anchor", getComponentTexture("chrono_anchor"), SkillPoint.RED_SKILL_POINT, 312, 315, SkillTrees.TREE_DEFENSE, "arsmagica2:reflect"));
        registry.register(new Skill("duration", getModifierTexture("duration"), SkillPoint.RED_SKILL_POINT, 312, 360, SkillTrees.TREE_DEFENSE, "arsmagica2:chrono_anchor"));
        registry.register(new Skill("absorption", getComponentTexture("absorption"), SkillPoint.RED_SKILL_POINT, 312, 270, SkillTrees.TREE_DEFENSE, "arsmagica2:shield"));
        registry.register(new Skill("resistance", getComponentTexture("resistance"), SkillPoint.GREEN_SKILL_POINT, 402, 315, SkillTrees.TREE_DEFENSE, "arsmagica2:zone"));
        registry.register(new Skill("mana_link", getComponentTexture("mana_link"), SkillPoint.SILVER_POINT, 30, 45, SkillTrees.TREE_DEFENSE));
        registry.register(new Skill("mana_shield", getComponentTexture("mana_shield"), SkillPoint.SILVER_POINT, 30, 90, SkillTrees.TREE_DEFENSE));
        registry.register(new Skill("buff_power", getModifierTexture("buff_power"), SkillPoint.SILVER_POINT, 30, 135, SkillTrees.TREE_DEFENSE));

        // Utility
        registry.register(new Skill("touch", getShapeTexture("touch"), SkillPoint.BLUE_SKILL_POINT, 275, 75, SkillTrees.TREE_UTILITY));
        registry.register(new Skill("dig", getComponentTexture("dig"), SkillPoint.BLUE_SKILL_POINT, 275, 120, SkillTrees.TREE_UTILITY, "arsmagica2:touch"));
        registry.register(new Skill("wizards_autumn", getComponentTexture("wizards_autumn"), SkillPoint.BLUE_SKILL_POINT, 315, 120, SkillTrees.TREE_UTILITY, "arsmagica2:dig"));
        registry.register(new Skill("target_non_solid", getModifierTexture("target_non_solid"), SkillPoint.BLUE_SKILL_POINT, 230, 40, SkillTrees.TREE_UTILITY, "arsmagica2:touch"));
        registry.register(new Skill("place_block", getComponentTexture("place_block"), SkillPoint.BLUE_SKILL_POINT, 185, 93, SkillTrees.TREE_UTILITY, "arsmagica2:dig"));
        registry.register(new Skill("conjure_dirt", getComponentTexture("conjure_dirt"), SkillPoint.GREEN_SKILL_POINT, 230, 93, SkillTrees.TREE_UTILITY, "arsmagica2:place_block"));
        registry.register(new Skill("feather_touch", getModifierTexture("feather_touch"), SkillPoint.BLUE_SKILL_POINT, 230, 137, SkillTrees.TREE_UTILITY, "arsmagica2:dig"));
        registry.register(new Skill("mining_power", getModifierTexture("mining_power"), SkillPoint.GREEN_SKILL_POINT, 140, 137, SkillTrees.TREE_UTILITY, "arsmagica2:feather_touch"));
        registry.register(new Skill("light", getComponentTexture("light"), SkillPoint.BLUE_SKILL_POINT, 275, 165, SkillTrees.TREE_UTILITY, "arsmagica2:dig"));
        registry.register(new Skill("night_vision", getComponentTexture("night_vision"), SkillPoint.BLUE_SKILL_POINT, 185, 165, SkillTrees.TREE_UTILITY, "arsmagica2:light"));
        registry.register(new Skill("binding", getShapeTexture("binding"), SkillPoint.BLUE_SKILL_POINT, 275, 210, SkillTrees.TREE_UTILITY, "arsmagica2:light"));
        registry.register(new Skill("disarm", getComponentTexture("disarm"), SkillPoint.BLUE_SKILL_POINT, 230, 210, SkillTrees.TREE_UTILITY, "arsmagica2:binding"));
        registry.register(new Skill("charm", getComponentTexture("charm"), SkillPoint.BLUE_SKILL_POINT, 315, 235, SkillTrees.TREE_UTILITY, "arsmagica2:binding"));
        registry.register(new Skill("alchemical_infusion", getComponentTexture("alchemical_infusion"), SkillPoint.GREEN_SKILL_POINT, 315, 280, SkillTrees.TREE_UTILITY, "arsmagica2:charm"));
        registry.register(new Skill("crystallize", getComponentTexture("crystallize"), SkillPoint.RED_SKILL_POINT, 315, 312, SkillTrees.TREE_UTILITY, "arsmagica2:alchemical_infusion"));
        registry.register(new Skill("true_sight", getComponentTexture("true_sight"), SkillPoint.BLUE_SKILL_POINT, 185, 210, SkillTrees.TREE_UTILITY, "arsmagica2:night_vision"));
        registry.register(new Skill("reveal", getComponentTexture("reveal"), SkillPoint.BLUE_SKILL_POINT, 230, 255, SkillTrees.TREE_UTILITY, "arsmagica2:true_sight"));
        registry.register(new Skill("lunar", getModifierTexture("lunar"), SkillPoint.RED_SKILL_POINT, 145, 210, SkillTrees.TREE_UTILITY, "arsmagica2:true_sight"));
        registry.register(new Skill("harvest_plants", getComponentTexture("harvest_plants"), SkillPoint.GREEN_SKILL_POINT, 365, 120, SkillTrees.TREE_UTILITY, "arsmagica2:binding"));
        registry.register(new Skill("plow", getComponentTexture("plow"), SkillPoint.BLUE_SKILL_POINT, 365, 165, SkillTrees.TREE_UTILITY, "arsmagica2:binding"));
        registry.register(new Skill("plant", getComponentTexture("plant"), SkillPoint.BLUE_SKILL_POINT, 365, 210, SkillTrees.TREE_UTILITY, "arsmagica2:binding"));
        registry.register(new Skill("create_water", getComponentTexture("create_water"), SkillPoint.GREEN_SKILL_POINT, 365, 255, SkillTrees.TREE_UTILITY, "arsmagica2:binding"));
        registry.register(new Skill("extinguish", getComponentTexture("extinguish"), SkillPoint.GREEN_SKILL_POINT, 410, 255, SkillTrees.TREE_UTILITY, "arsmagica2:create_water"));
        registry.register(new Skill("drought", getComponentTexture("drought"), SkillPoint.GREEN_SKILL_POINT, 365, 300, SkillTrees.TREE_UTILITY, "arsmagica2:binding"));
        registry.register(new Skill("banish_rain", getComponentTexture("banish_rain"), SkillPoint.GREEN_SKILL_POINT, 365, 345, SkillTrees.TREE_UTILITY, "arsmagica2:drought"));
        registry.register(new Skill("water_breathing", getComponentTexture("water_breathing"), SkillPoint.BLUE_SKILL_POINT, 410, 345, SkillTrees.TREE_UTILITY, "arsmagica2:drought"));
        registry.register(new Skill("grow", getComponentTexture("grow"), SkillPoint.RED_SKILL_POINT, 410, 210, SkillTrees.TREE_UTILITY, "arsmagica2:drought", "arsmagica2:create_water", "arsmagica2:plant", "arsmagica2:plow", "arsmagica2:harvest_plants"));
        registry.register(new Skill("chain", getShapeTexture("chain"), SkillPoint.RED_SKILL_POINT, 455, 210, SkillTrees.TREE_UTILITY, "arsmagica2:grow"));
        registry.register(new Skill("rift", getComponentTexture("rift"), SkillPoint.GREEN_SKILL_POINT, 275, 255, SkillTrees.TREE_UTILITY, "arsmagica2:binding"));
        registry.register(new Skill("invisibility", getComponentTexture("invisibility"), SkillPoint.GREEN_SKILL_POINT, 185, 255, SkillTrees.TREE_UTILITY, "arsmagica2:true_sight"));
        registry.register(new Skill("random_teleport", getComponentTexture("random_teleport"), SkillPoint.BLUE_SKILL_POINT, 185, 300, SkillTrees.TREE_UTILITY, "arsmagica2:invisibility"));
        registry.register(new Skill("attract", getComponentTexture("attract"), SkillPoint.GREEN_SKILL_POINT, 245, 300, SkillTrees.TREE_UTILITY, "arsmagica2:rift"));
        registry.register(new Skill("telekinesis", getComponentTexture("telekinesis"), SkillPoint.GREEN_SKILL_POINT, 410, 300, SkillTrees.TREE_UTILITY, "arsmagica2:rift"));
        registry.register(new Skill("blink", getComponentTexture("blink"), SkillPoint.GREEN_SKILL_POINT, 185, 345, SkillTrees.TREE_UTILITY, "arsmagica2:random_teleport"));
        registry.register(new Skill("range", getModifierTexture("range"), SkillPoint.RED_SKILL_POINT, 140, 345, SkillTrees.TREE_UTILITY, "arsmagica2:blink"));
        registry.register(new Skill("channel", getShapeTexture("channel"), SkillPoint.GREEN_SKILL_POINT, 275, 345, SkillTrees.TREE_UTILITY, "arsmagica2:attract", "arsmagica2:telekinesis"));
        registry.register(new Skill("toggle", getShapeTexture("toggle"), SkillPoint.RED_SKILL_POINT, 315, 345, SkillTrees.TREE_UTILITY, "arsmagica2:channel"));
        registry.register(new Skill("radius", getModifierTexture("radius"), SkillPoint.RED_SKILL_POINT, 275, 390, SkillTrees.TREE_UTILITY, "arsmagica2:channel"));
        registry.register(new Skill("channel_etherium", getComponentTexture("etherium_channel"), SkillPoint.GREEN_SKILL_POINT, 355, 390, SkillTrees.TREE_UTILITY, "arsmagica2:channel"));
        registry.register(new Skill("transplace", getComponentTexture("transplace"), SkillPoint.BLUE_SKILL_POINT, 185, 390, SkillTrees.TREE_UTILITY, "arsmagica2:blink"));
        registry.register(new Skill("phase_shift", getComponentTexture("phase_shift"), SkillPoint.RED_SKILL_POINT, 140, 390, SkillTrees.TREE_UTILITY, "arsmagica2:blink"));
        registry.register(new Skill("mark", getComponentTexture("mark"), SkillPoint.GREEN_SKILL_POINT, 155, 435, SkillTrees.TREE_UTILITY, "arsmagica2:transplace"));
        registry.register(new Skill("recall", getComponentTexture("recall"), SkillPoint.GREEN_SKILL_POINT, 215, 435, SkillTrees.TREE_UTILITY, "arsmagica2:transplace"));
        registry.register(new Skill("divine_intervention", getComponentTexture("divine_intervention"), SkillPoint.RED_SKILL_POINT, 140, 480, SkillTrees.TREE_UTILITY, "arsmagica2:recall", "arsmagica2:mark"));
        registry.register(new Skill("ender_intervention", getComponentTexture("ender_intervention"), SkillPoint.RED_SKILL_POINT, 198, 480, SkillTrees.TREE_UTILITY, "arsmagica2:recall", "arsmagica2:mark"));
        registry.register(new Skill("contingency_death", getShapeTexture("contingency_death"), SkillPoint.RED_SKILL_POINT, 198, 524, SkillTrees.TREE_UTILITY, "arsmagica2:ender_intervention"));
        registry.register(new Skill("daylight", getComponentTexture("daylight"), SkillPoint.SILVER_POINT, 75, 45, SkillTrees.TREE_UTILITY));
        registry.register(new Skill("moonrise", getComponentTexture("moonrise"), SkillPoint.SILVER_POINT, 75, 90, SkillTrees.TREE_UTILITY));
        registry.register(new Skill("prosperity", getModifierTexture("prosperity"), SkillPoint.SILVER_POINT, 75, 135, SkillTrees.TREE_UTILITY));

        // EBWiz-exclusive component – only registered when Electroblob's Wizardry is present.
        if (Loader.isModLoaded(EBWizardryCompatBootstrap.MODID)) {
            registry.register(new Skill("ebwiz_blast", getModifierTexture("ebwiz_blast"), SkillPoint.SILVER_POINT, 75, 270, SkillTrees.TREE_OFFENSE));
            registry.register(new Skill("ice_statue", getComponentTexture("ice_statue"), SkillPoint.RED_SKILL_POINT, 368, 245, SkillTrees.TREE_OFFENSE, "arsmagica2:freeze"));
            registry.register(new Skill("cobweb_spell", getComponentTexture("cobweb_spell"), SkillPoint.GREEN_SKILL_POINT, 87, 290, SkillTrees.TREE_DEFENSE, "arsmagica2:entangle"));
            registry.register(new Skill("conjure_block", getComponentTexture("conjure_block"), SkillPoint.GREEN_SKILL_POINT, 140, 93, SkillTrees.TREE_UTILITY, "arsmagica2:place_block"));
            registry.register(new Skill("metamorphosis", getComponentTexture("metamorphosis"), SkillPoint.GREEN_SKILL_POINT, 75, 315, SkillTrees.TREE_OFFENSE));
        }

        // AncientSpellcraft-exclusive components – only registered when both mods are present.
        if (AncientSpellcraftCompatBootstrap.isLoaded()) {
            registry.register(new Skill("shrinkage", getComponentTexture("shrinkage"), SkillPoint.SILVER_POINT, 75, 360, SkillTrees.TREE_OFFENSE));
            registry.register(new Skill("growth", getComponentTexture("growth"), SkillPoint.SILVER_POINT, 75, 405, SkillTrees.TREE_OFFENSE));
        }

        // PotionCore-exclusive skills – only registered when the mod is present.
        if (PotioncoreCompatBootstrap.isLoaded()) {
            registry.register(new Skill("corrosion", getComponentTexture("corrosion"), SkillPoint.SILVER_POINT, 75, 450, SkillTrees.TREE_OFFENSE));
        }

        Item spell_part = new ItemSpellComponent().setRegistryName(ArsMagica.MODID, "spell_part");
        ForgeRegistries.ITEMS.register(spell_part);

        // No two skills in the same tree may have overlapping icons
        List<Skill> skillsWithTree = new ArrayList<>();
        for (Skill s : registry.getValuesCollection()) {
            if (s.getTree() != null) skillsWithTree.add(s);
        }
        List<String> overlapping = getOverlapping(skillsWithTree);
        if (!overlapping.isEmpty()) {
           throw new IllegalStateException("[ArsMagica2] Occulus skill position overlap detected:\n  "
                    + String.join("\n  ", overlapping));
        }
    }

    private static List<String> getOverlapping(List<Skill> skillsWithTree) {
        final int ICON_SIZE = 32;
        List<String> conflicts = new ArrayList<>();

        for (int i = 0; i < skillsWithTree.size(); i++) {
            Skill a = skillsWithTree.get(i);
            for (int j = i + 1; j < skillsWithTree.size(); j++) {
                Skill b = skillsWithTree.get(j);
                if (!a.getTree().equals(b.getTree())) continue;
                int dx = Math.abs(a.getPosX() - b.getPosX());
                int dy = Math.abs(a.getPosY() - b.getPosY());
                if (dx < ICON_SIZE && dy < ICON_SIZE) {
                    conflicts.add(a.getID() + " overlaps with " + b.getID()
                            + " (positions (" + a.getPosX() + "," + a.getPosY() + ") and ("
                            + b.getPosX() + "," + b.getPosY() + ")) in tree '" + a.getTree().getName() + "'");
                }
            }
        }
        return conflicts;
    }

    private static ResourceLocation getComponentTexture(String name) {
        return new ResourceLocation(ArsMagica.MODID, "items/spells/components/" + name);
    }

    private static ResourceLocation getShapeTexture(String name) {
        return new ResourceLocation(ArsMagica.MODID, "items/spells/shapes/" + name);
    }

    private static ResourceLocation getModifierTexture(String name) {
        return new ResourceLocation(ArsMagica.MODID, "items/spells/modifiers/" + name);
    }

    private static ResourceLocation getTalentTexture(String iconName) { // not sure why it called Skill if it's clearly stated in-game that it is indeed Talent
        return new ResourceLocation(ArsMagica.MODID, "items/spells/skills/" + iconName);
    }

}

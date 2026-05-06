package am2.common.registry;

import am2.ArsMagica;
import am2.api.SpellRegistryHelper;
import am2.api.skill.SkillPoint;
import am2.api.spell.SpellManager;
import am2.api.spell.SpellPart;
import am2.common.compat.ancientspellcraft.AncientSpellcraftCompatBootstrap;
import am2.common.compat.electroblob.EBWizardryCompatBootstrap;
import am2.common.compat.potioncore.PotioncoreCompatBootstrap;
import am2.common.spell.component.*;
import am2.common.spell.modifier.*;
import am2.common.spell.shape.*;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import javax.annotation.Nonnull;

@ObjectHolder(ArsMagica.MODID)
@Mod.EventBusSubscriber(modid = ArsMagica.MODID)
public class AMSpells {

    private AMSpells() {
    } // no instances

    // This is here because this class is already an event handler.
    @SubscribeEvent
    public static void createRegistry(RegistryEvent.NewRegistry event) {

        RegistryBuilder<SpellPart> builder = new RegistryBuilder<>();
        builder.setType(SpellPart.class);
        builder.setName(new ResourceLocation(ArsMagica.MODID, "spells"));
        builder.setIDRange(0, 5000); // Is there any penalty for using a larger number?

        SpellPart.registry = builder.create();
    }

    @Nonnull
    @SuppressWarnings("ConstantConditions")
    private static <T> T placeholder() {
        return null;
    }

    public static final SpellPart missing_shape = placeholder();

    // offense
    public static final SpellPart melt_armor = placeholder();
    public static final SpellPart nauseate = placeholder();
    public static final SpellPart scramble_synapses = placeholder();
    public static final SpellPart colour = placeholder();
    public static final SpellPart projectile = placeholder();
    public static final SpellPart physical_damage = placeholder();
    public static final SpellPart gravity = placeholder();
    public static final SpellPart bounce = placeholder();
    public static final SpellPart fire_damage = placeholder();
    public static final SpellPart lightning_damage = placeholder();
    public static final SpellPart ignition = placeholder();
    public static final SpellPart forge = placeholder();
    public static final SpellPart magic_damage = placeholder();
    public static final SpellPart frost_damage = placeholder();
    public static final SpellPart drown = placeholder();
    public static final SpellPart blind = placeholder();
    public static final SpellPart poison = placeholder();
    public static final SpellPart wither = placeholder();
    public static final SpellPart aoe = placeholder();
    public static final SpellPart orbs = placeholder();
    public static final SpellPart freeze = placeholder();
    public static final SpellPart knockback = placeholder();
    public static final SpellPart contingency_fire = placeholder();
    public static final SpellPart solar = placeholder();
    public static final SpellPart storm = placeholder();
    public static final SpellPart astral_distortion = placeholder();
    public static final SpellPart silence = placeholder();
    public static final SpellPart fling = placeholder();
    public static final SpellPart velocity_added = placeholder();
    public static final SpellPart watery_grave = placeholder();
    public static final SpellPart piercing = placeholder();
    public static final SpellPart beam = placeholder();
    public static final SpellPart cone = placeholder();
    public static final SpellPart damage = placeholder();
    public static final SpellPart fury = placeholder();
    public static final SpellPart wave = placeholder();
 //   public static final SpellPart slice = placeholder();
    public static final SpellPart blizzard = placeholder();
    public static final SpellPart falling_star = placeholder();
    public static final SpellPart fire_rain = placeholder();
    public static final SpellPart mana_blast = placeholder();
    public static final SpellPart dismembering = placeholder();

    // defense
    public static final SpellPart self = placeholder();
    public static final SpellPart leap = placeholder();
    public static final SpellPart regeneration = placeholder();
    public static final SpellPart shrink = placeholder();
    public static final SpellPart slowfall = placeholder();
    public static final SpellPart heal = placeholder();
    public static final SpellPart life_tap = placeholder();
    public static final SpellPart healing = placeholder();
    public static final SpellPart summon = placeholder();
    public static final SpellPart contingency_damage = placeholder();
    public static final SpellPart haste = placeholder();
    public static final SpellPart slow = placeholder();
    public static final SpellPart gravity_well = placeholder();
    public static final SpellPart life_drain = placeholder();
    public static final SpellPart dispel = placeholder();
    public static final SpellPart contingency_fall = placeholder();
    public static final SpellPart swift_swim = placeholder();
    public static final SpellPart repel = placeholder();
    public static final SpellPart levitate = placeholder();
    public static final SpellPart mana_drain = placeholder();
    public static final SpellPart zone = placeholder();
    public static final SpellPart puddle = placeholder();
    public static final SpellPart gravitate = placeholder();
    public static final SpellPart wall = placeholder();
    public static final SpellPart accelerate = placeholder();
    public static final SpellPart entangle = placeholder();
    public static final SpellPart appropriation = placeholder();
    public static final SpellPart flight = placeholder();
    public static final SpellPart shield = placeholder();
    public static final SpellPart contingency_health = placeholder();
    public static final SpellPart rune = placeholder();
    public static final SpellPart rune_procs = placeholder();
    public static final SpellPart glyph = placeholder();
    public static final SpellPart speed = placeholder();
    public static final SpellPart reflect = placeholder();
    public static final SpellPart chrono_anchor = placeholder();
    public static final SpellPart duration = placeholder();
    public static final SpellPart absorption = placeholder();
    public static final SpellPart resistance = placeholder();
    public static final SpellPart mana_link = placeholder();
    public static final SpellPart mana_shield = placeholder();
    public static final SpellPart buff_power = placeholder();


    // utility
    public static final SpellPart touch = placeholder();
    public static final SpellPart dig = placeholder();
    public static final SpellPart wizards_autumn = placeholder();
    public static final SpellPart target_non_solid = placeholder();
    public static final SpellPart place_block = placeholder();
    public static final SpellPart feather_touch = placeholder();
    public static final SpellPart mining_power = placeholder();
    public static final SpellPart light = placeholder();
    public static final SpellPart night_vision = placeholder();
    public static final SpellPart binding = placeholder();
    public static final SpellPart disarm = placeholder();
    public static final SpellPart charm = placeholder();
    public static final SpellPart alchemical_infusion = placeholder();
    public static final SpellPart true_sight = placeholder();
    public static final SpellPart reveal = placeholder();
    public static final SpellPart lunar = placeholder();
    public static final SpellPart harvest_plants = placeholder();
    public static final SpellPart plow = placeholder();
    public static final SpellPart plant = placeholder();
    public static final SpellPart create_water = placeholder();
    public static final SpellPart extinguish = placeholder();
    public static final SpellPart drought = placeholder();
    public static final SpellPart banish_rain = placeholder();
    public static final SpellPart water_breathing = placeholder();
    public static final SpellPart grow = placeholder();
    public static final SpellPart chain = placeholder();
    public static final SpellPart rift = placeholder();
    public static final SpellPart invisibility = placeholder();
    public static final SpellPart random_teleport = placeholder();
    public static final SpellPart attract = placeholder();
    public static final SpellPart telekinesis = placeholder();
    public static final SpellPart blink = placeholder();
    public static final SpellPart phase_shift = placeholder();
    public static final SpellPart range = placeholder();
    public static final SpellPart channel = placeholder();
    public static final SpellPart toggle = placeholder();
    public static final SpellPart radius = placeholder();
    public static final SpellPart transplace = placeholder();
    public static final SpellPart mark = placeholder();
    public static final SpellPart recall = placeholder();
    public static final SpellPart divine_intervention = placeholder();
    public static final SpellPart ender_intervention = placeholder();
    public static final SpellPart contingency_death = placeholder();
    public static final SpellPart daylight = placeholder();
    public static final SpellPart moonrise = placeholder();
    public static final SpellPart prosperity = placeholder();
    // EBWiz-exclusive modifier (only usable with Electroblob's Wizardry spells at the Scribing Desk)
    public static final SpellPart ebwiz_blast = placeholder();
    // EBWiz-exclusive component (only registered when Electroblob's Wizardry is present)
    public static final SpellPart ice_statue = placeholder();
    public static final SpellPart metamorphosis = placeholder();
    // AncientSpellcraft-exclusive components (only registered when AncientSpellcraft + Artemislib are present)
    public static final SpellPart shrinkage = placeholder();
    public static final SpellPart growth = placeholder();
    // PotionCore-exclusive components (only registered when PotionCore is present)
    public static final SpellPart corrosion = placeholder();
    public static final SpellPart conjure_block = placeholder();
    public static final SpellPart conjure_dirt = placeholder();

    // power system
    public static final SpellPart channel_etherium = placeholder();

    @SubscribeEvent
    public static void register(RegistryEvent.Register<SpellPart> event) {

        IForgeRegistry<SpellPart> registry = event.getRegistry();

        SpellRegistryHelper.registerSpellShape(registry, "none", null, null, new MissingShape(), null, 0, 0);
        SpellRegistryHelper.registerSpellComponent(registry, "melt_armor", null, null, new MeltArmor(), null, 0, 0);
        SpellRegistryHelper.registerSpellComponent(registry, "nauseate", null, null, new Nauseate(), null, 0, 0);
        SpellRegistryHelper.registerSpellComponent(registry, "scramble_synapses", null, null, new ScrambleSynapses(), null, 0, 0);

        //offense tree
        SpellRegistryHelper.registerSpellShape(registry, "projectile", getShapeTexture("Projectile"), SkillPoint.BLUE_SKILL_POINT, new Projectile(), SkillTrees.TREE_OFFENSE, 300, 45);
        SpellRegistryHelper.registerSpellComponent(registry, "physical_damage", getComponentTexture("PhysicalDamage"), SkillPoint.BLUE_SKILL_POINT, new PhysicalDamage(), SkillTrees.TREE_OFFENSE, 300, 90, "arsmagica2:projectile");
        SpellRegistryHelper.registerSpellModifier(registry, "gravity", getModifierTexture("Gravity"), SkillPoint.BLUE_SKILL_POINT, new Gravity(), SkillTrees.TREE_OFFENSE, 255, 70, "arsmagica2:projectile");
        SpellRegistryHelper.registerSpellModifier(registry, "bounce", getModifierTexture("Bounce"), SkillPoint.BLUE_SKILL_POINT, new Bounce(), SkillTrees.TREE_OFFENSE, 345, 70, "arsmagica2:projectile");
        SpellRegistryHelper.registerSpellComponent(registry, "fire_damage", getComponentTexture("FireDamage"), SkillPoint.BLUE_SKILL_POINT, new FireDamage(), SkillTrees.TREE_OFFENSE, 210, 135, "arsmagica2:physical_damage");
        SpellRegistryHelper.registerSpellComponent(registry, "lightning_damage", getComponentTexture("LightningDamage"), SkillPoint.BLUE_SKILL_POINT, new LightningDamage(), SkillTrees.TREE_OFFENSE, 255, 135, "arsmagica2:fire_damage");
        SpellRegistryHelper.registerSpellComponent(registry, "ignition", getComponentTexture("Ignition"), SkillPoint.GREEN_SKILL_POINT, new Ignition(), SkillTrees.TREE_OFFENSE, 165, 135, "arsmagica2:fire_damage");
        SpellRegistryHelper.registerSpellComponent(registry, "forge", getComponentTexture("Forge"), SkillPoint.GREEN_SKILL_POINT, new Forge(), SkillTrees.TREE_OFFENSE, 120, 135, "arsmagica2:ignition");
        SpellRegistryHelper.registerSpellComponent(registry, "magic_damage", getComponentTexture("MagicDamage"), SkillPoint.BLUE_SKILL_POINT, new MagicDamage(), SkillTrees.TREE_OFFENSE, 390, 135, "arsmagica2:physical_damage");
        SpellRegistryHelper.registerSpellComponent(registry, "frost_damage", getComponentTexture("FrostDamage"), SkillPoint.BLUE_SKILL_POINT, new FrostDamage(), SkillTrees.TREE_OFFENSE, 345, 135, "arsmagica2:magic_damage");
        SpellRegistryHelper.registerSpellComponent(registry, "drown", getComponentTexture("Drown"), SkillPoint.BLUE_SKILL_POINT, new Drown(), SkillTrees.TREE_OFFENSE, 435, 135, "arsmagica2:magic_damage");
        SpellRegistryHelper.registerSpellComponent(registry, "blind", getComponentTexture("Blind"), SkillPoint.GREEN_SKILL_POINT, new Blind(), SkillTrees.TREE_OFFENSE, 233, 180, "arsmagica2:fire_damage", "arsmagica2:lightning_damage");
        SpellRegistryHelper.registerSpellComponent(registry, "poison", getComponentTexture("Poison"), SkillPoint.GREEN_SKILL_POINT, new Poison(), SkillTrees.TREE_OFFENSE, 233, 225, "arsmagica2:blind");
        SpellRegistryHelper.registerSpellComponent(registry, "wither", getComponentTexture("Wither"), SkillPoint.GREEN_SKILL_POINT, new Wither(), SkillTrees.TREE_OFFENSE, 233, 270, "arsmagica2:poison");
        SpellRegistryHelper.registerSpellShape(registry, "aoe", getShapeTexture("AoE"), SkillPoint.GREEN_SKILL_POINT, new AoE(), SkillTrees.TREE_OFFENSE, 300, 180, "arsmagica2:frost_damage", "arsmagica2:physical_damage", "arsmagica2:fire_damage", "arsmagica2:lightning_damage", "arsmagica2:magic_damage");
        SpellRegistryHelper.registerSpellShape(registry, "orbs", getShapeTexture("Orbs"), SkillPoint.GREEN_SKILL_POINT, new Orbs(), SkillTrees.TREE_OFFENSE, 300, 225, "arsmagica2:aoe");
        SpellRegistryHelper.registerSpellComponent(registry, "freeze", getComponentTexture("Freeze"), SkillPoint.GREEN_SKILL_POINT, new Freeze(), SkillTrees.TREE_OFFENSE, 345, 180, "arsmagica2:frost_damage");
        SpellRegistryHelper.registerSpellComponent(registry, "knockback", getComponentTexture("Knockback"), SkillPoint.GREEN_SKILL_POINT, new Knockback(), SkillTrees.TREE_OFFENSE, 390, 180, "arsmagica2:magic_damage");
        SpellRegistryHelper.registerSpellShape(registry, "contingency_fire", getShapeTexture("Contingency_Fire"), SkillPoint.GREEN_SKILL_POINT, new Contingency_Fire(), SkillTrees.TREE_OFFENSE, 165, 190, "arsmagica2:ignition");
        SpellRegistryHelper.registerSpellModifier(registry, "solar", getModifierTexture("Solar"), SkillPoint.RED_SKILL_POINT, new Solar(), SkillTrees.TREE_OFFENSE, 210, 255, "arsmagica2:blind");
        SpellRegistryHelper.registerSpellComponent(registry, "storm", getComponentTexture("Storm"), SkillPoint.RED_SKILL_POINT, new Storm(), SkillTrees.TREE_OFFENSE, 255, 225, "arsmagica2:lightning_damage");
        SpellRegistryHelper.registerSpellComponent(registry, "astral_distortion", getComponentTexture("AstralDistortion"), SkillPoint.GREEN_SKILL_POINT, new AstralDistortion(), SkillTrees.TREE_OFFENSE, 367, 215, "arsmagica2:magic_damage", "arsmagica2:frost_damage");
        SpellRegistryHelper.registerSpellComponent(registry, "silence", getComponentTexture("Silence"), SkillPoint.RED_SKILL_POINT, new Silence(), SkillTrees.TREE_OFFENSE, 345, 245, "arsmagica2:astral_distortion");
        SpellRegistryHelper.registerSpellComponent(registry, "fling", getComponentTexture("Fling"), SkillPoint.GREEN_SKILL_POINT, new Fling(), SkillTrees.TREE_OFFENSE, 390, 245, "arsmagica2:knockback");
        SpellRegistryHelper.registerSpellModifier(registry, "velocity_added", getModifierTexture("VelocityAdded"), SkillPoint.RED_SKILL_POINT, new VelocityAdded(), SkillTrees.TREE_OFFENSE, 390, 290, "arsmagica2:fling");
        SpellRegistryHelper.registerSpellComponent(registry, "watery_grave", getComponentTexture("WateryGrave"), SkillPoint.GREEN_SKILL_POINT, new WateryGrave(), SkillTrees.TREE_OFFENSE, 435, 245, "arsmagica2:drown");
        SpellRegistryHelper.registerSpellModifier(registry, "piercing", getModifierTexture("Piercing"), SkillPoint.RED_SKILL_POINT, new Piercing(), SkillTrees.TREE_OFFENSE, 323, 215, "arsmagica2:freeze");
        SpellRegistryHelper.registerSpellShape(registry, "beam", getShapeTexture("Beam"), SkillPoint.RED_SKILL_POINT, new Beam(), SkillTrees.TREE_OFFENSE, 300, 270, "arsmagica2:aoe");
        SpellRegistryHelper.registerSpellShape(registry, "cone", getShapeTexture("Cone"), SkillPoint.RED_SKILL_POINT, new Cone(), SkillTrees.TREE_OFFENSE, 345, 270, "arsmagica2:beam");
        SpellRegistryHelper.registerSpellModifier(registry, "damage", getModifierTexture("Damage"), SkillPoint.RED_SKILL_POINT, new Damage(), SkillTrees.TREE_OFFENSE, 300, 315, "arsmagica2:beam");
        SpellRegistryHelper.registerSpellComponent(registry, "fury", getComponentTexture("Fury"), SkillPoint.RED_SKILL_POINT, new Fury(), SkillTrees.TREE_OFFENSE, 255, 315, "arsmagica2:beam", "arsmagica2:storm");
        SpellRegistryHelper.registerSpellShape(registry, "wave", getShapeTexture("Wave"), SkillPoint.RED_SKILL_POINT, new Wave(), SkillTrees.TREE_OFFENSE, 367, 315, "arsmagica2:beam", "arsmagica2:fling");
  //      SpellRegistryHelper.registerSpellShape(registry, "slice", getShapeTexture("Slice"), SkillPoint.RED_SKILL_POINT, new Slice(), SkillTrees.TREE_OFFENSE, 413, 315, "arsmagica2:wave");
        SpellRegistryHelper.registerSpellComponent(registry, "blizzard", getComponentTexture("Blizzard"), SkillPoint.SILVER_POINT, new Blizzard(), SkillTrees.TREE_OFFENSE, 75, 45);
        SpellRegistryHelper.registerSpellComponent(registry, "falling_star", getComponentTexture("FallingStar"), SkillPoint.SILVER_POINT, new FallingStar(), SkillTrees.TREE_OFFENSE, 75, 90);
        SpellRegistryHelper.registerSpellComponent(registry, "fire_rain", getComponentTexture("FireRain"), SkillPoint.SILVER_POINT, new FireRain(), SkillTrees.TREE_OFFENSE, 75, 135);
        SpellRegistryHelper.registerSpellComponent(registry, "mana_blast", getComponentTexture("ManaBlast"), SkillPoint.SILVER_POINT, new ManaBlast(), SkillTrees.TREE_OFFENSE, 75, 180);
        SpellRegistryHelper.registerSpellModifier(registry, "dismembering", getModifierTexture("Dismembering"), SkillPoint.SILVER_POINT, new Dismembering(), SkillTrees.TREE_OFFENSE, 75, 225);
        //defense tree
        SpellRegistryHelper.registerSpellShape(registry, "self", getShapeTexture("Self"), SkillPoint.BLUE_SKILL_POINT, new Self(), SkillTrees.TREE_DEFENSE, 267, 45);
        SpellRegistryHelper.registerSpellComponent(registry, "leap", getComponentTexture("Leap"), SkillPoint.BLUE_SKILL_POINT, new Leap(), SkillTrees.TREE_DEFENSE, 222, 90, "arsmagica2:self");
        SpellRegistryHelper.registerSpellComponent(registry, "regeneration", getComponentTexture("Regeneration"), SkillPoint.BLUE_SKILL_POINT, new Regeneration(), SkillTrees.TREE_DEFENSE, 357, 90, "arsmagica2:self");
        SpellRegistryHelper.registerSpellComponent(registry, "shrink", getComponentTexture("Shrink"), SkillPoint.BLUE_SKILL_POINT, new Shrink(), SkillTrees.TREE_DEFENSE, 402, 90, "arsmagica2:regeneration");
        SpellRegistryHelper.registerSpellComponent(registry, "slowfall", getComponentTexture("Slowfall"), SkillPoint.BLUE_SKILL_POINT, new Slowfall(), SkillTrees.TREE_DEFENSE, 222, 135, "arsmagica2:leap");
        SpellRegistryHelper.registerSpellComponent(registry, "heal", getComponentTexture("Heal"), SkillPoint.BLUE_SKILL_POINT, new Heal(), SkillTrees.TREE_DEFENSE, 357, 135, "arsmagica2:regeneration");
        SpellRegistryHelper.registerSpellComponent(registry, "life_tap", getComponentTexture("LifeTap"), SkillPoint.GREEN_SKILL_POINT, new LifeTap(), SkillTrees.TREE_DEFENSE, 312, 135, "arsmagica2:heal");
        SpellRegistryHelper.registerSpellModifier(registry, "healing", getModifierTexture("Healing"), SkillPoint.RED_SKILL_POINT, new Healing(), SkillTrees.TREE_DEFENSE, 402, 135, "arsmagica2:heal");
        SpellRegistryHelper.registerSpellComponent(registry, "summon", getComponentTexture("Summon"), SkillPoint.GREEN_SKILL_POINT, new Summon(), SkillTrees.TREE_DEFENSE, 267, 135, "arsmagica2:life_tap");
        SpellRegistryHelper.registerSpellShape(registry, "contingency_damage", getShapeTexture("Contingency_Damage"), SkillPoint.GREEN_SKILL_POINT, new Contingency_Hit(), SkillTrees.TREE_DEFENSE, 447, 180, "arsmagica2:healing");
        SpellRegistryHelper.registerSpellComponent(registry, "haste", getComponentTexture("Haste"), SkillPoint.BLUE_SKILL_POINT, new Haste(), SkillTrees.TREE_DEFENSE, 177, 155, "arsmagica2:slowfall");
        SpellRegistryHelper.registerSpellComponent(registry, "slow", getComponentTexture("Slow"), SkillPoint.BLUE_SKILL_POINT, new Slow(), SkillTrees.TREE_DEFENSE, 132, 155, "arsmagica2:slowfall");
        SpellRegistryHelper.registerSpellComponent(registry, "gravity_well", getComponentTexture("GravityWell"), SkillPoint.GREEN_SKILL_POINT, new GravityWell(), SkillTrees.TREE_DEFENSE, 222, 180, "arsmagica2:slowfall");
        SpellRegistryHelper.registerSpellComponent(registry, "life_drain", getComponentTexture("LifeDrain"), SkillPoint.GREEN_SKILL_POINT, new LifeDrain(), SkillTrees.TREE_DEFENSE, 312, 180, "arsmagica2:life_tap");
        SpellRegistryHelper.registerSpellComponent(registry, "dispel", getComponentTexture("Dispel"), SkillPoint.GREEN_SKILL_POINT, new Dispel(), SkillTrees.TREE_DEFENSE, 357, 180, "arsmagica2:heal");
        SpellRegistryHelper.registerSpellShape(registry, "contingency_fall", getShapeTexture("Contingency_Fall"), SkillPoint.GREEN_SKILL_POINT, new Contingency_Fall(), SkillTrees.TREE_DEFENSE, 267, 180, "arsmagica2:gravity_well");
        SpellRegistryHelper.registerSpellComponent(registry, "swift_swim", getComponentTexture("SwiftSwim"), SkillPoint.BLUE_SKILL_POINT, new SwiftSwim(), SkillTrees.TREE_DEFENSE, 177, 200, "arsmagica2:haste");
        SpellRegistryHelper.registerSpellComponent(registry, "repel", getComponentTexture("Repel"), SkillPoint.GREEN_SKILL_POINT, new Repel(), SkillTrees.TREE_DEFENSE, 132, 200, "arsmagica2:slow");
        SpellRegistryHelper.registerSpellComponent(registry, "levitate", getComponentTexture("Levitate"), SkillPoint.GREEN_SKILL_POINT, new Levitation(), SkillTrees.TREE_DEFENSE, 222, 225, "arsmagica2:gravity_well");
        SpellRegistryHelper.registerSpellComponent(registry, "mana_drain", getComponentTexture("ManaDrain"), SkillPoint.GREEN_SKILL_POINT, new ManaDrain(), SkillTrees.TREE_DEFENSE, 312, 225, "arsmagica2:life_drain");
        SpellRegistryHelper.registerSpellShape(registry, "zone", getShapeTexture("Zone"), SkillPoint.RED_SKILL_POINT, new Zone(), SkillTrees.TREE_DEFENSE, 357, 225, "arsmagica2:dispel");
        SpellRegistryHelper.registerSpellShape(registry, "puddle", getShapeTexture("Puddle"), SkillPoint.RED_SKILL_POINT, new Puddle(), SkillTrees.TREE_DEFENSE, 447, 270, "arsmagica2:zone");
        SpellRegistryHelper.registerSpellModifier(registry, "gravitate", getModifierTexture("gravitate"), SkillPoint.GREEN_SKILL_POINT, new Gravitate(), SkillTrees.TREE_DEFENSE, 447, 315, "arsmagica2:puddle", "arsmagica2:zone");
        SpellRegistryHelper.registerSpellShape(registry, "wall", getShapeTexture("Wall"), SkillPoint.GREEN_SKILL_POINT, new Wall(), SkillTrees.TREE_DEFENSE, 87, 200, "arsmagica2:repel");
        SpellRegistryHelper.registerSpellComponent(registry, "accelerate", getComponentTexture("Accelerate"), SkillPoint.GREEN_SKILL_POINT, new Accelerate(), SkillTrees.TREE_DEFENSE, 177, 245, "arsmagica2:swift_swim");
        SpellRegistryHelper.registerSpellComponent(registry, "entangle", getComponentTexture("Entangle"), SkillPoint.GREEN_SKILL_POINT, new Entangle(), SkillTrees.TREE_DEFENSE, 132, 245, "arsmagica2:repel");
        SpellRegistryHelper.registerSpellComponent(registry, "appropriation", getComponentTexture("Appropriation"), SkillPoint.RED_SKILL_POINT, new Appropriation(), SkillTrees.TREE_DEFENSE, 87, 245, "arsmagica2:entangle");
        SpellRegistryHelper.registerSpellComponent(registry, "flight", getComponentTexture("Flight"), SkillPoint.RED_SKILL_POINT, new Flight(), SkillTrees.TREE_DEFENSE, 222, 270, "arsmagica2:levitate");
        SpellRegistryHelper.registerSpellComponent(registry, "shield", getComponentTexture("Shield"), SkillPoint.BLUE_SKILL_POINT, new Shield(), SkillTrees.TREE_DEFENSE, 357, 270, "arsmagica2:zone");
        SpellRegistryHelper.registerSpellShape(registry, "contingency_health", getShapeTexture("Contingency_Health"), SkillPoint.RED_SKILL_POINT, new Contingency_Health(), SkillTrees.TREE_DEFENSE, 402, 270, "arsmagica2:shield");
        SpellRegistryHelper.registerSpellShape(registry, "rune", getShapeTexture("Rune"), SkillPoint.GREEN_SKILL_POINT, new Rune(), SkillTrees.TREE_DEFENSE, 157, 315, "arsmagica2:accelerate", "arsmagica2:entangle");
        SpellRegistryHelper.registerSpellModifier(registry, "rune_procs", getModifierTexture("RuneProcs"), SkillPoint.GREEN_SKILL_POINT, new RuneProcs(), SkillTrees.TREE_DEFENSE, 157, 360, "arsmagica2:rune");
        SpellRegistryHelper.registerSpellShape(registry, "glyph", getShapeTexture("Glyph"), SkillPoint.GREEN_SKILL_POINT, new Glyph(), SkillTrees.TREE_DEFENSE, 112, 360, "arsmagica2:rune");
        SpellRegistryHelper.registerSpellModifier(registry, "speed", getModifierTexture("Speed"), SkillPoint.RED_SKILL_POINT, new Speed(), SkillTrees.TREE_DEFENSE, 202, 315, "arsmagica2:accelerate", "arsmagica2:flight");
        SpellRegistryHelper.registerSpellComponent(registry, "reflect", getComponentTexture("Reflect"), SkillPoint.RED_SKILL_POINT, new Reflect(), SkillTrees.TREE_DEFENSE, 357, 315, "arsmagica2:shield");
        SpellRegistryHelper.registerSpellComponent(registry, "chrono_anchor", getComponentTexture("ChronoAnchor"), SkillPoint.RED_SKILL_POINT, new ChronoAnchor(), SkillTrees.TREE_DEFENSE, 312, 315, "arsmagica2:reflect");
        SpellRegistryHelper.registerSpellModifier(registry, "duration", getModifierTexture("Duration"), SkillPoint.RED_SKILL_POINT, new Duration(), SkillTrees.TREE_DEFENSE, 312, 360, "arsmagica2:chrono_anchor");
        SpellRegistryHelper.registerSpellComponent(registry, "absorption", getComponentTexture("Absorption"), SkillPoint.RED_SKILL_POINT, new Absorption(), SkillTrees.TREE_DEFENSE, 312, 270, "arsmagica2:shield");
        SpellRegistryHelper.registerSpellComponent(registry, "resistance", getComponentTexture("Resistance"), SkillPoint.GREEN_SKILL_POINT, new Resistance(), SkillTrees.TREE_DEFENSE, 357, 270, "arsmagica2:zone");
        SpellRegistryHelper.registerSpellComponent(registry, "mana_link", getComponentTexture("ManaLink"), SkillPoint.SILVER_POINT, new ManaLink(), SkillTrees.TREE_DEFENSE, 30, 45);
        SpellRegistryHelper.registerSpellComponent(registry, "mana_shield", getComponentTexture("ManaShield"), SkillPoint.SILVER_POINT, new ManaShield(), SkillTrees.TREE_DEFENSE, 30, 90);
        SpellRegistryHelper.registerSpellModifier(registry, "buff_power", getModifierTexture("BuffPower"), SkillPoint.SILVER_POINT, new BuffPower(), SkillTrees.TREE_DEFENSE, 30, 135);
        //utility tree
        SpellRegistryHelper.registerSpellShape(registry, "touch", getShapeTexture("Touch"), SkillPoint.BLUE_SKILL_POINT, new Touch(), SkillTrees.TREE_UTILITY, 275, 75);
        SpellRegistryHelper.registerSpellComponent(registry, "dig", getComponentTexture("Dig"), SkillPoint.BLUE_SKILL_POINT, new Dig(), SkillTrees.TREE_UTILITY, 275, 120, "arsmagica2:touch");
        SpellRegistryHelper.registerSpellComponent(registry, "wizards_autumn", getComponentTexture("WizardsAutumn"), SkillPoint.BLUE_SKILL_POINT, new WizardsAutumn(), SkillTrees.TREE_UTILITY, 315, 120, "arsmagica2:dig");
        SpellRegistryHelper.registerSpellModifier(registry, "target_non_solid", getModifierTexture("TargetNonSolid"), SkillPoint.BLUE_SKILL_POINT, new TargetNonSolidBlocks(), SkillTrees.TREE_UTILITY, 230, 75, "arsmagica2:touch");
        SpellRegistryHelper.registerSpellComponent(registry, "place_block", getComponentTexture("PlaceBlock"), SkillPoint.BLUE_SKILL_POINT, new PlaceBlock(), SkillTrees.TREE_UTILITY, 185, 93, "arsmagica2:dig");
        SpellRegistryHelper.registerSpellComponent(registry, "conjure_dirt", getComponentTexture("conjure_dirt"), SkillPoint.GREEN_SKILL_POINT, new ConjureDirt(), SkillTrees.TREE_UTILITY, 230, 93, "arsmagica2:place_block");
        SpellRegistryHelper.registerSpellModifier(registry, "feather_touch", getModifierTexture("FeatherTouch"), SkillPoint.BLUE_SKILL_POINT, new FeatherTouch(), SkillTrees.TREE_UTILITY, 230, 137, "arsmagica2:dig");
        SpellRegistryHelper.registerSpellModifier(registry, "mining_power", getModifierTexture("MiningPower"), SkillPoint.GREEN_SKILL_POINT, new MiningPower(), SkillTrees.TREE_UTILITY, 185, 137, "arsmagica2:feather_touch");
        SpellRegistryHelper.registerSpellComponent(registry, "light", getComponentTexture("Light"), SkillPoint.BLUE_SKILL_POINT, new Light(), SkillTrees.TREE_UTILITY, 275, 165, "arsmagica2:dig");
        SpellRegistryHelper.registerSpellComponent(registry, "night_vision", getComponentTexture("NightVision"), SkillPoint.BLUE_SKILL_POINT, new NightVision(), SkillTrees.TREE_UTILITY, 185, 165, "arsmagica2:light");
        SpellRegistryHelper.registerSpellShape(registry, "binding", getShapeTexture("Binding"), SkillPoint.BLUE_SKILL_POINT, new Binding(), SkillTrees.TREE_UTILITY, 275, 210, "arsmagica2:light");
        SpellRegistryHelper.registerSpellComponent(registry, "disarm", getComponentTexture("Disarm"), SkillPoint.BLUE_SKILL_POINT, new Disarm(), SkillTrees.TREE_UTILITY, 230, 210, "arsmagica2:binding");
        SpellRegistryHelper.registerSpellComponent(registry, "charm", getComponentTexture("Charm"), SkillPoint.BLUE_SKILL_POINT, new Charm(), SkillTrees.TREE_UTILITY, 315, 235, "arsmagica2:binding");
        SpellRegistryHelper.registerSpellComponent(registry, "alchemical_infusion", getComponentTexture("alchemical_infusion"), SkillPoint.GREEN_SKILL_POINT, new ApplyPotion(), SkillTrees.TREE_UTILITY, 315, 280, "arsmagica2:charm");
        SpellRegistryHelper.registerSpellComponent(registry, "crystallize", getComponentTexture("crystallize"), SkillPoint.RED_SKILL_POINT, new Crystallize(), SkillTrees.TREE_UTILITY, 315, 312, "arsmagica2:alchemical_infusion");
        SpellRegistryHelper.registerSpellComponent(registry, "true_sight", getComponentTexture("TrueSight"), SkillPoint.BLUE_SKILL_POINT, new TrueSight(), SkillTrees.TREE_UTILITY, 185, 210, "arsmagica2:night_vision");
        SpellRegistryHelper.registerSpellComponent(registry, "reveal", getComponentTexture("Reveal"), SkillPoint.BLUE_SKILL_POINT, new Reveal(), SkillTrees.TREE_UTILITY, 230, 210, "arsmagica2:true_sight");
        SpellRegistryHelper.registerSpellModifier(registry, "lunar", getModifierTexture("Lunar"), SkillPoint.RED_SKILL_POINT, new Lunar(), SkillTrees.TREE_UTILITY, 145, 210, "arsmagica2:true_sight");
        SpellRegistryHelper.registerSpellComponent(registry, "harvest_plants", getComponentTexture("HarvestPlants"), SkillPoint.GREEN_SKILL_POINT, new HarvestPlants(), SkillTrees.TREE_UTILITY, 365, 120, "arsmagica2:binding");
        SpellRegistryHelper.registerSpellComponent(registry, "plow", getComponentTexture("Plow"), SkillPoint.BLUE_SKILL_POINT, new Plow(), SkillTrees.TREE_UTILITY, 365, 165, "arsmagica2:binding");
        SpellRegistryHelper.registerSpellComponent(registry, "plant", getComponentTexture("Plant"), SkillPoint.BLUE_SKILL_POINT, new Plant(), SkillTrees.TREE_UTILITY, 365, 210, "arsmagica2:binding");
        SpellRegistryHelper.registerSpellComponent(registry, "create_water", getComponentTexture("CreateWater"), SkillPoint.GREEN_SKILL_POINT, new CreateWater(), SkillTrees.TREE_UTILITY, 365, 255, "arsmagica2:binding");
        SpellRegistryHelper.registerSpellComponent(registry, "extinguish", getComponentTexture("Extinguish"), SkillPoint.GREEN_SKILL_POINT, new Extinguish(), SkillTrees.TREE_UTILITY, 410, 255, "arsmagica2:create_water");
        SpellRegistryHelper.registerSpellComponent(registry, "drought", getComponentTexture("Drought"), SkillPoint.GREEN_SKILL_POINT, new Drought(), SkillTrees.TREE_UTILITY, 365, 300, "arsmagica2:binding");
        SpellRegistryHelper.registerSpellComponent(registry, "banish_rain", getComponentTexture("BanishRain"), SkillPoint.GREEN_SKILL_POINT, new BanishRain(), SkillTrees.TREE_UTILITY, 365, 345, "arsmagica2:drought");
        SpellRegistryHelper.registerSpellComponent(registry, "water_breathing", getComponentTexture("WaterBreathing"), SkillPoint.BLUE_SKILL_POINT, new WaterBreathing(), SkillTrees.TREE_UTILITY, 410, 345, "arsmagica2:drought");
        SpellRegistryHelper.registerSpellComponent(registry, "grow", getComponentTexture("Grow"), SkillPoint.RED_SKILL_POINT, new Grow(), SkillTrees.TREE_UTILITY, 410, 210, "arsmagica2:drought", "arsmagica2:create_water", "arsmagica2:plant", "arsmagica2:plow", "arsmagica2:harvest_plants");
        SpellRegistryHelper.registerSpellShape(registry, "chain", getShapeTexture("Chain"), SkillPoint.RED_SKILL_POINT, new Chain(), SkillTrees.TREE_UTILITY, 455, 210, "arsmagica2:grow");
        SpellRegistryHelper.registerSpellComponent(registry, "rift", getComponentTexture("Rift"), SkillPoint.GREEN_SKILL_POINT, new Rift(), SkillTrees.TREE_UTILITY, 275, 255, "arsmagica2:binding");
        SpellRegistryHelper.registerSpellComponent(registry, "invisibility", getComponentTexture("Invisibility"), SkillPoint.GREEN_SKILL_POINT, new Invisiblity(), SkillTrees.TREE_UTILITY, 185, 255, "arsmagica2:true_sight");
        SpellRegistryHelper.registerSpellComponent(registry, "random_teleport", getComponentTexture("RandomTeleport"), SkillPoint.BLUE_SKILL_POINT, new RandomTeleport(), SkillTrees.TREE_UTILITY, 185, 300, "arsmagica2:invisibility");
        SpellRegistryHelper.registerSpellComponent(registry, "attract", getComponentTexture("Attract"), SkillPoint.GREEN_SKILL_POINT, new Attract(), SkillTrees.TREE_UTILITY, 245, 300, "arsmagica2:rift");
        SpellRegistryHelper.registerSpellComponent(registry, "telekinesis", getComponentTexture("Telekinesis"), SkillPoint.GREEN_SKILL_POINT, new Telekinesis(), SkillTrees.TREE_UTILITY, 305, 300, "arsmagica2:rift");
        SpellRegistryHelper.registerSpellComponent(registry, "blink", getComponentTexture("Blink"), SkillPoint.GREEN_SKILL_POINT, new Blink(), SkillTrees.TREE_UTILITY, 185, 345, "arsmagica2:random_teleport");
        SpellRegistryHelper.registerSpellModifier(registry, "range", getModifierTexture("Range"), SkillPoint.RED_SKILL_POINT, new Range(), SkillTrees.TREE_UTILITY, 140, 345, "arsmagica2:blink");
        SpellRegistryHelper.registerSpellShape(registry, "channel", getShapeTexture("Channel"), SkillPoint.GREEN_SKILL_POINT, new Channel(), SkillTrees.TREE_UTILITY, 275, 345, "arsmagica2:attract", "arsmagica2:telekinesis");
        SpellRegistryHelper.registerSpellShape(registry, "toggle", getShapeTexture("Toggle"), SkillPoint.RED_SKILL_POINT, new Toggle(), SkillTrees.TREE_UTILITY, 315, 345, "arsmagica2:channel");
        SpellRegistryHelper.registerSpellModifier(registry, "radius", getModifierTexture("Radius"), SkillPoint.RED_SKILL_POINT, new Radius(), SkillTrees.TREE_UTILITY, 275, 390, "arsmagica2:channel");
        SpellRegistryHelper.registerSpellComponent(registry, "transplace", getComponentTexture("Transplace"), SkillPoint.BLUE_SKILL_POINT, new Transplace(), SkillTrees.TREE_UTILITY, 185, 390, "arsmagica2:blink");
        SpellRegistryHelper.registerSpellComponent(registry, "phase_shift", getComponentTexture("PhaseShift"), SkillPoint.RED_SKILL_POINT, new PhaseShift(), SkillTrees.TREE_UTILITY, 140, 390, "arsmagica2:blink");
        SpellRegistryHelper.registerSpellComponent(registry, "mark", getComponentTexture("Mark"), SkillPoint.GREEN_SKILL_POINT, new Mark(), SkillTrees.TREE_UTILITY, 155, 435, "arsmagica2:transplace");
        SpellRegistryHelper.registerSpellComponent(registry, "recall", getComponentTexture("Recall"), SkillPoint.GREEN_SKILL_POINT, new Recall(), SkillTrees.TREE_UTILITY, 215, 435, "arsmagica2:transplace");
        SpellRegistryHelper.registerSpellComponent(registry, "divine_intervention", getComponentTexture("DivineIntervention"), SkillPoint.RED_SKILL_POINT, new DivineIntervention(), SkillTrees.TREE_UTILITY, 172, 480, "arsmagica2:recall", "arsmagica2:mark");
        SpellRegistryHelper.registerSpellComponent(registry, "ender_intervention", getComponentTexture("EnderIntervention"), SkillPoint.RED_SKILL_POINT, new EnderIntervention(), SkillTrees.TREE_UTILITY, 198, 480, "arsmagica2:recall", "arsmagica2:mark");
        SpellRegistryHelper.registerSpellShape(registry, "contingency_death", getShapeTexture("Contingency_Death"), SkillPoint.RED_SKILL_POINT, new Contingency_Death(), SkillTrees.TREE_UTILITY, 198, 524, "arsmagica2:ender_intervention");
        SpellRegistryHelper.registerSpellComponent(registry, "daylight", getComponentTexture("Daylight"), SkillPoint.SILVER_POINT, new Daylight(), SkillTrees.TREE_UTILITY, 75, 45);
        SpellRegistryHelper.registerSpellComponent(registry, "moonrise", getComponentTexture("Moonrise"), SkillPoint.SILVER_POINT, new Moonrise(), SkillTrees.TREE_UTILITY, 75, 90);
        SpellRegistryHelper.registerSpellModifier(registry, "prosperity", getModifierTexture("Prosperity"), SkillPoint.SILVER_POINT, new Prosperity(), SkillTrees.TREE_UTILITY, 75, 135);
        SpellRegistryHelper.registerSpellComponent(registry, "channel_etherium", getComponentTexture("EtheriumChannel"), SkillPoint.GREEN_SKILL_POINT, new EtheriumChannel(), SkillTrees.TREE_UTILITY, 355, 390, "arsmagica2:channel");
        EBWizardryCompatBootstrap.registerSpellParts(registry);
        AncientSpellcraftCompatBootstrap.registerSpellParts(registry);
        PotioncoreCompatBootstrap.registerSpellParts(registry);

        SpellRegistryHelper.registerSpellModifier(registry, "colour", getModifierTexture("Colour"), SkillPoint.BLUE_SKILL_POINT, new Colour(), SkillTrees.TREE_TALENT, 230, 75);

        // Register components that should apply every tick when channeled
        SpellManager.registerChannelComponent((Class) Telekinesis.class);
        SpellManager.registerChannelComponent((Class) Attract.class);
        SpellManager.registerChannelComponent((Class) Repel.class);

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
}

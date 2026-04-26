package am2.common.registry;

import am2.ArsMagica;
import am2.api.affinity.Affinity;
import am2.api.event.SpellSoundMapEvent;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Mod.EventBusSubscriber(modid = ArsMagica.MODID)
public class AMSounds {

    private static final List<SoundEvent> sounds = new ArrayList<>();

    public static final SoundEvent AIR_GUARDIAN_HIT = createSound("mob.airguardian.hit");
    public static final SoundEvent AIR_GUARDIAN_DEATH = createSound("mob.airguardian.death");
    public static final SoundEvent AIR_GUARDIAN_IDLE = createSound("mob.airguardian.idle");

    public static final SoundEvent ARCANE_GUARDIAN_HIT = createSound("mob.arcaneguardian.hit");
    public static final SoundEvent ARCANE_GUARDIAN_DEATH = createSound("mob.arcaneguardian.death");
    public static final SoundEvent ARCANE_GUARDIAN_IDLE = createSound("mob.arcaneguardian.idle");
    public static final SoundEvent ARCANE_GUARDIAN_SPELL = createSound("mob.arcaneguardian.spell");

    public static final SoundEvent LIGHTNING_GUARDIAN_IDLE = createSound("mob.lightningguardian.idle");
    public static final SoundEvent LIGHTNING_GUARDIAN_ATTACK = createSound("mob.lightningguardian.attack");
    public static final SoundEvent LIGHTNING_GUARDIAN_ATTACK_STATIC = createSound("mob.lightningguardian.attack_static");
    public static final SoundEvent LIGHTNING_GUARDIAN_LIGHTNING_ROD_1 = createSound("mob.lightningguardian.lightning_rod_1");
    public static final SoundEvent LIGHTNING_GUARDIAN_LIGHTNING_ROD_START = createSound("mob.lightningguardian.lightning_rod_start");
    public static final SoundEvent LIGHTNING_GUARDIAN_STATIC = createSound("mob.lightningguardian.static");
    public static final SoundEvent LIGHTNING_GUARDIAN_HIT = createSound("mob.lightningguardian.hit");
    public static final SoundEvent LIGHTNING_GUARDIAN_DEATH = createSound("mob.lightningguardian.death");

    public static final SoundEvent NATURE_GUARDIAN_WHIRL_LOOP = createSound("mob.natureguardian.whirlloop");
    public static final SoundEvent NATURE_GUARDIAN_HIT = createSound("mob.natureguardian.hit");
    public static final SoundEvent NATURE_GUARDIAN_IDLE = createSound("mob.natureguardian.idle");
    public static final SoundEvent NATURE_GUARDIAN_DEATH = createSound("mob.natureguardian.death");
    public static final SoundEvent NATURE_GUARDIAN_ATTACK = createSound("mob.natureguardian.attack");

    public static final SoundEvent LIFE_GUARDIAN_SUMMON = createSound("mob.lifeguardian.summon");
    public static final SoundEvent LIFE_GUARDIAN_HIT = createSound("mob.lifeguardian.hit");
    public static final SoundEvent LIFE_GUARDIAN_DEATH = createSound("mob.lifeguardian.death");
    public static final SoundEvent LIFE_GUARDIAN_IDLE = createSound("mob.lifeguardian.idle");
    public static final SoundEvent LIFE_GUARDIAN_HEAL = createSound("mob.lifeguardian.heal");

    public static final SoundEvent WINTER_GUARDIAN_LAUNCH_ARM = createSound("mob.winterguardian.launcharm");
    public static final SoundEvent WINTER_GUARDIAN_IDLE = createSound("mob.winterguardian.idle");
    public static final SoundEvent WINTER_GUARDIAN_HIT = createSound("mob.winterguardian.hit");
    public static final SoundEvent WINTER_GUARDIAN_DEATH = createSound("mob.winterguardian.death");
    public static final SoundEvent WINTER_GUARDIAN_ATTACK = createSound("mob.winterguardian.attack");

    public static final SoundEvent EARTH_GUARDIAN_HIT = createSound("mob.earthguardian.hit");
    public static final SoundEvent EARTH_GUARDIAN_DEATH = createSound("mob.earthguardian.death");
    public static final SoundEvent EARTH_GUARDIAN_IDLE = createSound("mob.earthguardian.idle");
    public static final SoundEvent EARTH_GUARDIAN_ATTACK = createSound("mob.earthguardian.attack");

    public static final SoundEvent ENDER_GUARDIAN_ROAR = createSound("mob.enderguardian.roar");
    public static final SoundEvent ENDER_GUARDIAN_FLAP = createSound("mob.enderguardian.flap");
    public static final SoundEvent ENDER_GUARDIAN_HIT = createSound("mob.enderguardian.hit");
    public static final SoundEvent ENDER_GUARDIAN_DEATH = createSound("mob.enderguardian.death");
    public static final SoundEvent ENDER_GUARDIAN_IDLE = createSound("mob.enderguardian.idle");
    public static final SoundEvent ENDER_GUARDIAN_ATTACK = createSound("mob.enderguardian.attack");

    public static final SoundEvent FIRE_GUARDIAN_HIT = createSound("mob.fireguardian.hit");
    public static final SoundEvent FIRE_GUARDIAN_DEATH = createSound("mob.fireguardian.death");
    public static final SoundEvent FIRE_GUARDIAN_IDLE = createSound("mob.fireguardian.idle");
    public static final SoundEvent FIRE_GUARDIAN_ATTACK = createSound("mob.fireguardian.attack");

    public static final SoundEvent WATER_GUARDIAN_HIT = createSound("mob.waterguardian.hit");
    public static final SoundEvent WATER_GUARDIAN_IDLE = createSound("mob.waterguardian.idle");
    public static final SoundEvent WATER_GUARDIAN_DEATH = createSound("mob.waterguardian.death");
    public static final SoundEvent WATER_GUARDIAN_ATTACK = createSound("mob.waterguardian.attack");

    public static final SoundEvent MANA_ELEMENTAL_HIT = createSound("mob.manaelemental.hit");
    public static final SoundEvent MANA_ELEMENTAL_IDLE = createSound("mob.manaelemental.living");
    public static final SoundEvent MANA_ELEMENTAL_DEATH = createSound("mob.manaelemental.death");

    public static final SoundEvent HECATE_IDLE = createSound("mob.hecate.idle");
    public static final SoundEvent HECATE_DEATH = createSound("mob.hecate.death");
    public static final SoundEvent HECATE_HIT = createSound("mob.hecate.hit");

    public static final SoundEvent GATEWAY_OPEN = createSound("misc.gateway.open");
    public static final SoundEvent RECONSTRUCTOR_COMPLETE = createSound("misc.reconstructor.complete");
    public static final SoundEvent CALEFACTOR_BURN = createSound("misc.calefactor.burn");
    public static final SoundEvent CRAFTING_ALTAR_CREATE_SPELL = createSound("misc.craftingaltar.create_spell");
    public static final SoundEvent CRAFTING_ALTAR_COMPONENT_ADDED = createSound("misc.craftingaltar.component_added");

    public static final SoundEvent MOO_IDLE = createSound("mob.moo.idle");
    public static final SoundEvent MOO_DEATH = createSound("mob.moo.death");
    public static final SoundEvent MOO_HIT = createSound("mob.moo.hit");
    public static Map<Affinity, SoundEvent> LOOP_MAP;
    public static Map<Affinity, SoundEvent> CAST_MAP;
    public static SoundEvent LOOP_AIR = getLoopSound("air");
    public static SoundEvent LOOP_ARCANE = getLoopSound("arcane");
    public static SoundEvent LOOP_EARTH = getLoopSound("earth");
    public static SoundEvent LOOP_ENDER = getLoopSound("ender");
    public static SoundEvent LOOP_FIRE = getLoopSound("fire");
    public static SoundEvent LOOP_ICE = getLoopSound("ice");
    public static SoundEvent LOOP_LIFE = getLoopSound("life");
    public static SoundEvent LOOP_LIGHTNING = getLoopSound("lightning");
    public static SoundEvent LOOP_NATURE = getLoopSound("nature");
    public static SoundEvent LOOP_NONE = getLoopSound("none");
    public static SoundEvent LOOP_WATER = getLoopSound("water");
    public static SoundEvent CAST_AIR = getCastSound("air");
    public static SoundEvent CAST_ARCANE = getCastSound("arcane");
    public static SoundEvent CAST_EARTH = getCastSound("earth");
    public static SoundEvent CAST_ENDER = getCastSound("ender");
    public static SoundEvent CAST_FIRE = getCastSound("fire");
    public static SoundEvent CAST_ICE = getCastSound("ice");
    public static SoundEvent CAST_LIFE = getCastSound("life");
    public static SoundEvent CAST_LIGHTNING = getCastSound("lightning");
    public static SoundEvent CAST_NATURE = getCastSound("nature");
    public static SoundEvent CAST_NONE = getCastSound("none");
    public static SoundEvent CAST_WATER = getCastSound("water");
    public static SoundEvent RUNE_CAST = new SoundEvent(new ResourceLocation(ArsMagica.MODID, "spell.rune.cast"));
    public static SoundEvent CONTINGENCY = new SoundEvent(new ResourceLocation(ArsMagica.MODID, "spell.contingency.cast"));
    public static SoundEvent BINDING_CAST = new SoundEvent(new ResourceLocation(ArsMagica.MODID, "spell.binding.cast"));

    public static SoundEvent createSound(String name) {
        return createSound(ArsMagica.MODID, name);
    }

    public static SoundEvent createSound(String modID, String name) {
        // All the setRegistryName methods delegate to this one, it doesn't matter which you use.
        SoundEvent sound = new SoundEvent(new ResourceLocation(modID, name)).setRegistryName(name);
        sounds.add(sound);
        return sound;
    }

    @SubscribeEvent
    public static void register(RegistryEvent.Register<SoundEvent> event) {
        event.getRegistry().registerAll(sounds.toArray(new SoundEvent[0]));
    }

    private static SoundEvent getLoopSound(String aff) {
        ResourceLocation rl = new ResourceLocation(ArsMagica.MODID, "spell.loop." + aff);
        return new SoundEvent(rl).setRegistryName(rl);
    }

    private static SoundEvent getCastSound(String aff) {
        ResourceLocation rl = new ResourceLocation(ArsMagica.MODID, "spell.cast." + aff);
        return new SoundEvent(rl).setRegistryName(rl);
    }

    public static void registerSounds() {
        register(LOOP_AIR);
        register(LOOP_ARCANE);
        register(LOOP_EARTH);
        register(LOOP_ENDER);
        register(LOOP_FIRE);
        register(LOOP_ICE);
        register(LOOP_LIFE);
        register(LOOP_LIGHTNING);
        register(LOOP_NATURE);
        register(LOOP_NONE);
        register(LOOP_WATER);

        register(CAST_AIR);
        register(CAST_ARCANE);
        register(CAST_EARTH);
        register(CAST_ENDER);
        register(CAST_FIRE);
        register(CAST_ICE);
        register(CAST_LIFE);
        register(CAST_LIGHTNING);
        register(CAST_NATURE);
        register(CAST_NONE);
        register(CAST_WATER);

        // TODO: registry GameRegistry.register(RUNE_CAST, new ResourceLocation(ArsMagica2.MODID, "spell.rune.cast"));
        // TODO: registry GameRegistry.register(CONTINGENCY, new ResourceLocation(ArsMagica2.MODID, "spell.contingency.contingency"));
        // TODO: registry GameRegistry.register(BINDING_CAST, new ResourceLocation(ArsMagica2.MODID, "spell.binding.cast"));
    }

    private static void register(SoundEvent event) {
        // TODO: registry GameRegistry.register(event);
    }

    public static void createSoundMaps() {
        SpellSoundMapEvent event = new SpellSoundMapEvent(new ResourceLocation(ArsMagica.MODID, "loop"));
        event.put(Affinities.air, LOOP_AIR);
        event.put(Affinities.arcane, LOOP_ARCANE);
        event.put(Affinities.earth, LOOP_EARTH);
        event.put(Affinities.ender, LOOP_ENDER);
        event.put(Affinities.fire, LOOP_FIRE);
        event.put(Affinities.ice, LOOP_ICE);
        event.put(Affinities.life, LOOP_LIFE);
        event.put(Affinities.lightning, LOOP_LIGHTNING);
        event.put(Affinities.nature, LOOP_NATURE);
        event.put(Affinities.none, LOOP_NONE);
        event.put(Affinities.water, LOOP_WATER);
        MinecraftForge.EVENT_BUS.post(event);
        LOOP_MAP = event.getMap();

        event = new SpellSoundMapEvent(new ResourceLocation(ArsMagica.MODID, "cast"));
        event.put(Affinities.air, CAST_AIR);
        event.put(Affinities.arcane, CAST_ARCANE);
        event.put(Affinities.earth, CAST_EARTH);
        event.put(Affinities.ender, CAST_ENDER);
        event.put(Affinities.fire, CAST_FIRE);
        event.put(Affinities.ice, CAST_ICE);
        event.put(Affinities.life, CAST_LIFE);
        event.put(Affinities.lightning, CAST_LIGHTNING);
        event.put(Affinities.nature, CAST_NATURE);
        event.put(Affinities.none, CAST_NONE);
        event.put(Affinities.water, CAST_WATER);
        MinecraftForge.EVENT_BUS.post(event);
        CAST_MAP = event.getMap();

    }
}

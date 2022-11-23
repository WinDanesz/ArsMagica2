package am2.common.defs;

import am2.ArsMagica2;
import am2.api.ArsMagicaAPI;
import am2.api.affinity.Affinity;
import am2.api.event.SpellSoundMapEvent;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.Map;

public class SoundDefs {

	public static Map<Affinity, SoundEvent> LOOP_MAP;
	public static Map<Affinity, SoundEvent> CAST_MAP;

	private static SoundEvent getLoopSound(String aff) {
		ResourceLocation rl = new ResourceLocation(ArsMagica2.MODID, "spell.loop." + aff);
		return new SoundEvent(rl).setRegistryName(rl);
	}

	private static SoundEvent getCastSound(String aff) {
		ResourceLocation rl = new ResourceLocation(ArsMagica2.MODID, "spell.cast." + aff);
		return new SoundEvent(rl).setRegistryName(rl);
	}

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

	public static SoundEvent RUNE_CAST = new SoundEvent(new ResourceLocation(ArsMagica2.MODID, "spell.rune.cast"));
	public static SoundEvent CONTINGENCY = new SoundEvent(new ResourceLocation(ArsMagica2.MODID, "spell.contingency.cast"));
	public static SoundEvent BINDING_CAST = new SoundEvent(new ResourceLocation(ArsMagica2.MODID, "spell.binding.cast"));

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
		SpellSoundMapEvent event = new SpellSoundMapEvent(new ResourceLocation(ArsMagica2.MODID, "loop"));
		event.put(Affinity.AIR, LOOP_AIR);
		event.put(Affinity.ARCANE, LOOP_ARCANE);
		event.put(Affinity.EARTH, LOOP_EARTH);
		event.put(Affinity.ENDER, LOOP_ENDER);
		event.put(Affinity.FIRE, LOOP_FIRE);
		event.put(Affinity.ICE, LOOP_ICE);
		event.put(Affinity.LIFE, LOOP_LIFE);
		event.put(Affinity.LIGHTNING, LOOP_LIGHTNING);
		event.put(Affinity.NATURE, LOOP_NATURE);
		event.put(Affinity.NONE, LOOP_NONE);
		event.put(Affinity.WATER, LOOP_WATER);
		MinecraftForge.EVENT_BUS.post(event);
		LOOP_MAP = event.getMap();

		event = new SpellSoundMapEvent(new ResourceLocation(ArsMagica2.MODID, "cast"));
		event.put(Affinity.AIR, CAST_AIR);
		event.put(Affinity.ARCANE, CAST_ARCANE);
		event.put(Affinity.EARTH, CAST_EARTH);
		event.put(Affinity.ENDER, CAST_ENDER);
		event.put(Affinity.FIRE, CAST_FIRE);
		event.put(Affinity.ICE, CAST_ICE);
		event.put(Affinity.LIFE, CAST_LIFE);
		event.put(Affinity.LIGHTNING, CAST_LIGHTNING);
		event.put(Affinity.NATURE, CAST_NATURE);
		event.put(Affinity.NONE, CAST_NONE);
		event.put(Affinity.WATER, CAST_WATER);
		MinecraftForge.EVENT_BUS.post(event);
		CAST_MAP = event.getMap();

	}
}

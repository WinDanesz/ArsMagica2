package am2.common.defs;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import am2.common.LogHelper;
import am2.common.buffs.BuffEffect;
import am2.common.buffs.BuffEffectAgility;
import am2.common.buffs.BuffEffectAstralDistortion;
import am2.common.buffs.BuffEffectBurnoutReduction;
import am2.common.buffs.BuffEffectCharmed;
import am2.common.buffs.BuffEffectClarity;
import am2.common.buffs.BuffEffectEntangled;
import am2.common.buffs.BuffEffectFlight;
import am2.common.buffs.BuffEffectFrostSlowed;
import am2.common.buffs.BuffEffectFury;
import am2.common.buffs.BuffEffectGravityWell;
import am2.common.buffs.BuffEffectHaste;
import am2.common.buffs.BuffEffectIllumination;
import am2.common.buffs.BuffEffectInstantMana;
import am2.common.buffs.BuffEffectLeap;
import am2.common.buffs.BuffEffectLevitation;
import am2.common.buffs.BuffEffectMagicShield;
import am2.common.buffs.BuffEffectManaRegen;
import am2.common.buffs.BuffEffectRegeneration;
import am2.common.buffs.BuffEffectScrambleSynapses;
import am2.common.buffs.BuffEffectShield;
import am2.common.buffs.BuffEffectShrink;
import am2.common.buffs.BuffEffectSilence;
import am2.common.buffs.BuffEffectSlowfall;
import am2.common.buffs.BuffEffectSpellReflect;
import am2.common.buffs.BuffEffectSwiftSwim;
import am2.common.buffs.BuffEffectTemporalAnchor;
import am2.common.buffs.BuffEffectTrueSight;
import am2.common.buffs.BuffEffectWaterBreathing;
import am2.common.buffs.BuffEffectWateryGrave;
import am2.common.buffs.BuffMaxManaIncrease;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class PotionEffectsDefs {
	
	public static final HashMap<Potion, Class<? extends BuffEffect>> classForId = new HashMap<Potion, Class<? extends BuffEffect>>();
	
	public static Potion AGILITY;
	public static Potion ASTRAL_DISTORTION;
	public static Potion BURNOUT_REDUCTION;
	public static Potion CHARME;
	public static Potion CLARITY;
	public static Potion ENTANGLE;
	public static Potion FLIGHT;
	public static Potion FROST_SLOW;
	public static Potion FURY;
	public static Potion GRQVITY_WELL;
	public static Potion HASTE;
	public static Potion ILLUMINATION;
	public static Potion INSTANT_MANA;
	public static Potion LEAP;
	public static Potion LEVITATION;
	public static Potion MAGIC_SHIELD;
	public static Potion MANA_REGEN;
	public static Potion REGENERATION;
	public static Potion SCRAMBLE_SYNAPSES;
	public static Potion SHIELD;
	public static Potion SHRINK;
	public static Potion SILENCE;
	public static Potion SLOWFALL;
	public static Potion SPELL_REFLECT;
	public static Potion SWIFT_SWIM;
	public static Potion TEMPORAL_ANCHOR;
	public static Potion TRUE_SIGHT;
	public static Potion WATER_BREATHING;
	public static Potion WATERY_GRAVE;
	public static Potion MANA_BOOST;
	
	public static final int DEFAULT_BUFF_DURATION = 600;
	
	public static void init () {
		AGILITY = createPotion(new ResourceLocation("arsmagica2:agility"), false, 0xade000, 0, 0, BuffEffectAgility.class);
		ASTRAL_DISTORTION = createPotion(new ResourceLocation("arsmagica2:astral_distortion"), true, 0x6c0000, 0, 4, BuffEffectAstralDistortion.class);
		BURNOUT_REDUCTION = createPotion(new ResourceLocation("arsmagica2:burnout_reduction"), false, 0xcc0000, 1, 1, BuffEffectBurnoutReduction.class);
		CHARME = createPotion(new ResourceLocation("arsmagica2:charme"), true, 0xff3ca2, 3, 2, BuffEffectCharmed.class);
		CLARITY = createPotion(new ResourceLocation("arsmagica2:clarity"), false, 0xbbffff, 0, 5, BuffEffectClarity.class);
		ENTANGLE = createPotion(new ResourceLocation("arsmagica2:entangle"), false, 0x009300, 3, 7, BuffEffectEntangled.class);
		FLIGHT = createPotion(new ResourceLocation("arsmagica2:flight"), false, 0xc6dada, 2, 1, BuffEffectFlight.class);
		FROST_SLOW = createPotion(new ResourceLocation("arsmagica2:frost_slow"), true, 0x1fffdd, 3, 3, BuffEffectFrostSlowed.class);
		FURY = createPotion(new ResourceLocation("arsmagica2:fury"), true, 0xff8033, 3, 6, BuffEffectFury.class);
		GRQVITY_WELL = createPotion(new ResourceLocation("arsmagica2:gravity_well"), true, 0xa400ff, 0, 6, BuffEffectGravityWell.class);
		HASTE = createPotion(new ResourceLocation("arsmagica2:haste"), false, 0xf1f1f1, 2, 3, BuffEffectHaste.class);
		ILLUMINATION = createPotion(new ResourceLocation("arsmagica2:illumination"), false, 0xffffbe, 1, 0, BuffEffectIllumination.class);
		INSTANT_MANA = createPotion(new ResourceLocation("arsmagica2:instant_mana"), false, 0x00ffff, 0, 0, BuffEffectInstantMana.class);
		LEAP = createPotion(new ResourceLocation("arsmagica2:leap"), false, 0x00ff00, 0, 2, BuffEffectLeap.class);
		LEVITATION = createPotion(new ResourceLocation("arsmagica2:levitation"), false, 0xd780ff, 0, 7, BuffEffectLevitation.class);
		MAGIC_SHIELD = createPotion(new ResourceLocation("arsmagica2:magic_shield"), false, 0xd780ff, 3, 1, BuffEffectMagicShield.class);
		MANA_REGEN = createPotion(new ResourceLocation("arsmagica2:mana_regen"), false, 0x8bffff, 3, 5, BuffEffectManaRegen.class);
		REGENERATION = createPotion(new ResourceLocation("arsmagica2:regeneration"), false, 0xff00ff, 2, 6, BuffEffectRegeneration.class);
		SCRAMBLE_SYNAPSES = createPotion(new ResourceLocation("arsmagica2:scramble_synapses"), true, 0x306600, 3, 7, BuffEffectScrambleSynapses.class);
		SHIELD = createPotion(new ResourceLocation("arsmagica2:shield"), false, 0xc4c4c4, 0, 0, BuffEffectShield.class);
		SHRINK = createPotion(new ResourceLocation("arsmagica2:shrink"), false, 0x0000dd, 0, 5, BuffEffectShrink.class);
		SILENCE = createPotion(new ResourceLocation("arsmagica2:silence"), true, 0xc1c1ff, 4, 6, BuffEffectSilence.class);
		SLOWFALL = createPotion(new ResourceLocation("arsmagica2:slowfall"), false, 0xe3ffe3, 2, 2, BuffEffectSlowfall.class);
		SPELL_REFLECT = createPotion(new ResourceLocation("arsmagica2:spell_reflect"), false, 0xadffff, 4, 3, BuffEffectSpellReflect.class);
		SWIFT_SWIM = createPotion(new ResourceLocation("arsmagica2:swift_swim"), false, 0x3b3bff, 4, 7, BuffEffectSwiftSwim.class);
		TEMPORAL_ANCHOR = createPotion(new ResourceLocation("arsmagica2:temporal_anchor"), false, 0xa2a2a2, 3, 4, BuffEffectTemporalAnchor.class);
		TRUE_SIGHT = createPotion(new ResourceLocation("arsmagica2:true_sight"), false, 0xc400ff, 2, 4, BuffEffectTrueSight.class);
		WATER_BREATHING = createPotion(new ResourceLocation("arsmagica2:water_breathing"), false, 0x0000ff, 2, 0, BuffEffectWaterBreathing.class);
		WATERY_GRAVE = createPotion(new ResourceLocation("arsmagica2:watery_grave"), true, 0x0000a2, 4, 0, BuffEffectWateryGrave.class);
		MANA_BOOST = createPotion(new ResourceLocation("arsmagica2:mana_boost"), false, 0x0093ff, 3, 0, BuffMaxManaIncrease.class);
	}
	
	public static Potion createPotion(ResourceLocation loc, boolean isBad, int color, int posX, int posY, Class<? extends BuffEffect> clazz) {
		Potion potion = new AMPotion(isBad, color).setIconIndex(posX, posY).setPotionName(loc.toString());
		// TODO: registry GameRegistry.register(potion, loc);
		classForId.put(potion, clazz);
		return potion;
	}
	
	public static PotionEffect getEffect(PotionEffect effect) {
		Class<? extends BuffEffect> clazz = classForId.get(effect.getPotion());
		if (clazz == null) return null;
		if (effect instanceof BuffEffect) return null;
		try {
			Constructor<? extends BuffEffect> constr;
			try {
				constr = clazz.getDeclaredConstructor(int.class, int.class);
			} catch (NoSuchMethodException e) {
				constr = clazz.getDeclaredConstructor(Potion.class, int.class, int.class);
				return constr.newInstance(effect.getPotion(), effect.getDuration(), effect.getAmplifier());
			}
			return constr.newInstance(effect.getDuration(), effect.getAmplifier());
		} catch (NoSuchMethodException e) {
			LogHelper.warn("Wrong definition for : " + clazz.getName());
		} catch (SecurityException e) {
			LogHelper.warn("Is this even a thing ? (SecurityException:" + clazz.getName() + ")");
		} catch (InstantiationException e) {
			LogHelper.warn("Could not create : " + clazz.getName());
		} catch (IllegalAccessException e) {
			LogHelper.warn("Could not access : " + clazz.getName());
		} catch (IllegalArgumentException e) {
			LogHelper.warn("Could not create : " + clazz.getName());
		} catch (InvocationTargetException e) {
			LogHelper.warn("InvocationTargetException" + clazz.getName());
		} catch (NullPointerException e) {
			LogHelper.warn("This should never be a thing : NullPointerExecption at " + clazz.getName());
		}
		
		return null;
	}
}

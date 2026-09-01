package am2.common.registry;

import am2.ArsMagica;
import am2.common.potions.*;
import net.minecraft.potion.Potion;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nonnull;

@ObjectHolder(ArsMagica.MODID)
@Mod.EventBusSubscriber(modid = ArsMagica.MODID)
public class AMPotions {

    public static final Potion agility = placeholder();
    public static final Potion astral_distortion = placeholder();
    public static final Potion burnout_reduction = placeholder();
    public static final Potion charm = placeholder();
    public static final Potion clarity = placeholder();
    public static final Potion entangle = placeholder();
    public static final Potion flight = placeholder();
    public static final Potion frost_slow = placeholder();
    public static final Potion fury = placeholder();
    public static final Potion gravity_well = placeholder();
    public static final Potion haste = placeholder();
    public static final Potion illumination = placeholder();
    public static final Potion instant_mana = placeholder();
    public static final Potion leap = placeholder();
    public static final Potion levitation = placeholder();
    public static final Potion magic_shield = placeholder();
    public static final Potion mana_regeneration = placeholder();
    public static final Potion regeneration = placeholder();
    public static final Potion scramble_synapses = placeholder();
    public static final Potion shield = placeholder();
    public static final Potion shrink = placeholder();
    public static final Potion silence = placeholder();
    public static final Potion slowfall = placeholder();
    public static final Potion spell_reflect = placeholder();
    public static final Potion swift_swim = placeholder();
    public static final Potion temporal_anchor = placeholder();
    public static final Potion true_sight = placeholder();
    public static final Potion water_breathing = placeholder();
    public static final Potion watery_grave = placeholder();
    public static final Potion mana_boost = placeholder();

    @Nonnull
    @SuppressWarnings("ConstantConditions")
    public static <T> T placeholder() {
        return null;
    }

    @SubscribeEvent
    public static void register(RegistryEvent.Register<Potion> event) {
        IForgeRegistry<Potion> registry = event.getRegistry();

        registerPotion(registry, "agility", new BuffEffectAgility(false, 0xade000).setIconIndex(0, 0));
        registerPotion(registry, "astral_distortion", new BuffEffectAstralDistortion(true, 0x6c0000).setIconIndex(0, 4));
        registerPotion(registry, "burnout_reduction", new BuffEffectBurnoutReduction(false, 0xcc0000).setIconIndex(1, 1));
        registerPotion(registry, "charm", new BuffEffectCharmed(true, 0xff3ca2).setIconIndex(3, 2));
        registerPotion(registry, "clarity", new BuffEffectClarity(false, 0xbbffff).setIconIndex(0, 1));
        registerPotion(registry, "entangle", new BuffEffectEntangled(false, 0x009300).setIconIndex(3, 7));
        registerPotion(registry, "flight", new BuffEffectFlight(false, 0xc6dada).setIconIndex(2, 1));
        registerPotion(registry, "frost_slow", new BuffEffectFrostSlowed(true, 0x1fffdd).setIconIndex(3, 3));
        registerPotion(registry, "fury", new BuffEffectFury(true, 0xff8033).setIconIndex(3, 6));
        registerPotion(registry, "gravity_well", new BuffEffectGravityWell(true, 0xa400ff).setIconIndex(0, 6));
        registerPotion(registry, "haste", new BuffEffectHaste(false, 0xf1f1f1).setIconIndex(2, 3));
        registerPotion(registry, "illumination", new BuffEffectIllumination(false, 0xffffbe).setIconIndex(1, 0));
        registerPotion(registry, "instant_mana", new BuffEffectInstantMana(false, 0x00ffff).setIconIndex(0, 0));
        registerPotion(registry, "leap", new BuffEffectLeap(false, 0x00ff00).setIconIndex(0, 2));
        registerPotion(registry, "levitation", new BuffEffectLevitation(false, 0xd780ff).setIconIndex(0, 7));
        registerPotion(registry, "magic_shield", new BuffEffectMagicShield(false, 0xd780ff).setIconIndex(3, 1));
        registerPotion(registry, "mana_regeneration", new BuffEffectManaRegeneration(false, 0x8bffff).setIconIndex(3, 5));
        registerPotion(registry, "regeneration", new BuffEffectRegeneration(false, 0xff00ff).setIconIndex(2, 6));
        registerPotion(registry, "scramble_synapses", new BuffEffectScrambleSynapses(true, 0x306600).setIconIndex(3, 7));
        registerPotion(registry, "shield", new BuffEffectShield(false, 0xc4c4c4).setIconIndex(0, 0));
        registerPotion(registry, "shrink", new BuffEffectShrink(false, 0x0000dd).setIconIndex(0, 5));
        registerPotion(registry, "silence", new BuffEffectSilence(true, 0xc1c1ff).setIconIndex(4, 6));
        registerPotion(registry, "slowfall", new BuffEffectSlowfall(false, 0xe3ffe3).setIconIndex(2, 2));
        registerPotion(registry, "spell_reflect", new BuffEffectSpellReflect(false, 0xadffff).setIconIndex(4, 3));
        registerPotion(registry, "swift_swim", new BuffEffectSwiftSwim(false, 0x3b3bff).setIconIndex(4, 7));
        registerPotion(registry, "temporal_anchor", new BuffEffectTemporalAnchor(false, 0xa2a2a2).setIconIndex(3, 4));
        registerPotion(registry, "true_sight", new BuffEffectTrueSight(false, 0xc400ff).setIconIndex(2, 4));
        registerPotion(registry, "water_breathing", new BuffEffectWaterBreathing(false, 0x0000ff).setIconIndex(2, 0));
        registerPotion(registry, "watery_grave", new PotionWateryGrave(true, 0x0000a2).setIconIndex(4, 0));
        registerPotion(registry, "mana_boost", new BuffMaxManaIncrease(false, 0x0093ff).setIconIndex(3, 0));
    }

    public static void registerPotion(IForgeRegistry<Potion> registry, String name, Potion potion) {
        potion.setRegistryName(ArsMagica.MODID, name);
        potion.setPotionName("potion." + potion.getRegistryName().toString());
        registry.register(potion);
    }
}

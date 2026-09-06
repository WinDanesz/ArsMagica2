package am2.common.registry;

import am2.ArsMagica;
import am2.api.affinity.AbstractAffinityAbility;
import am2.common.affinity.abilities.*;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber(modid = ArsMagica.MODID)
public class AffinityAbilities {

    private AffinityAbilities() {
    } // no instances

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void register(RegistryEvent.Register<AbstractAffinityAbility> event) {
        IForgeRegistry<AbstractAffinityAbility> registry = event.getRegistry();

        //AIR
        registry.register(new AbilityAgile());
        registry.register(new AbilityWindswept());
        registry.register(new AbilityGaleFist());
        registry.register(new AbilityTailwind());
        registry.register(new AbilityEyeOfTheStorm());

        //ARCANE
        registry.register(new AbilityClearCaster());
        registry.register(new AbilityMagicWeakness());
        registry.register(new AbilityOneWithMagic());

        //EARTH
        registry.register(new AbilitySolidBones());
        registry.register(new AbilityImmovable());
        registry.register(new AbilityHardening());

        //ENDER
        registry.register(new AbilityRelocation());
        registry.register(new AbilityNightVision());
        registry.register(new AbilityWaterWeakness("ender"));
        registry.register(new AbilityPoisonResistance());
        registry.register(new AbilitySunlightWeakness());

        //FIRE
        registry.register(new AbilityFireResistance());
        registry.register(new AbilityFirePunch());
        registry.register(new AbilityWaterWeakness("fire"));

        //ICE
        registry.register(new AbilityLavaFreeze());
        registry.register(new AbilityWaterFreeze());
        registry.register(new AbilityColdBlooded());
        registry.register(new AbilityRimeguard());

        //LIFE
        registry.register(new AbilityFastHealing());
        registry.register(new AbilityPacifist());
        registry.register(new AbilityHealingTouch());

        //WATER
        registry.register(new AbilityExpandedLungs());
        registry.register(new AbilityFluidity());
        registry.register(new AbilitySwiftSwim());
        registry.register(new AbilityFireWeakness());
        registry.register(new AbilityAntiEndermen());

        //NATURE
        registry.register(new AbilityRooted());
        registry.register(new AbilityThorns());
        registry.register(new AbilityPhotosynthesis());
        registry.register(new AbilityWallClimb());

        //LIGHTNING
        registry.register(new AbilityLightningStep());
        registry.register(new AbilityReflexes());
        registry.register(new AbilityFulmination());
        registry.register(new AbilityShortCircuit());
        registry.register(new AbilityThunderPunch());
        registry.register(new AbilityWaterWeakness("lightning"));
    }
}

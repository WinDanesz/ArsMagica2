package am2.common.registry;

import am2.ArsMagica;
import am2.api.flickers.AbstractFlickerFunctionality;
import am2.common.blocks.tileentity.flickers.*;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

@GameRegistry.ObjectHolder(ArsMagica.MODID)
@Mod.EventBusSubscriber(modid = ArsMagica.MODID)
public class Flickers {
    @SubscribeEvent
    public static void register(RegistryEvent.Register<AbstractFlickerFunctionality> event) {
        IForgeRegistry<AbstractFlickerFunctionality> registry = event.getRegistry();
        registry.register(FlickerOperatorItemTransport.instance.setRegistryName(ArsMagica.MODID, "flicker_itemtransport"));
        registry.register(FlickerOperatorButchery.instance.setRegistryName(ArsMagica.MODID, "flicker_butchery"));
        registry.register(FlickerOperatorContainment.instance.setRegistryName(ArsMagica.MODID, "flicker_containment"));
        registry.register(FlickerOperatorFelledOak.instance.setRegistryName(ArsMagica.MODID, "flicker_felledoak"));
        registry.register(FlickerOperatorFlatLands.instance.setRegistryName(ArsMagica.MODID, "flicker_flatlands"));
        registry.register(FlickerOperatorGentleRains.instance.setRegistryName(ArsMagica.MODID, "flicker_gentlerains"));
        registry.register(FlickerOperatorInterdiction.instance.setRegistryName(ArsMagica.MODID, "flicker_interdiction"));
        registry.register(FlickerOperatorLight.instance.setRegistryName(ArsMagica.MODID, "flicker_light"));
        registry.register(FlickerOperatorMoonstoneAttractor.instance.setRegistryName(ArsMagica.MODID, "flicker_moonstoneattractor"));
        registry.register(FlickerOperatorNaturesBounty.instance.setRegistryName(ArsMagica.MODID, "flicker_naturesbounty"));
        registry.register(FlickerOperatorPackedEarth.instance.setRegistryName(ArsMagica.MODID, "flicker_packedearth"));
        registry.register(FlickerOperatorProgeny.instance.setRegistryName(ArsMagica.MODID, "flicker_progeny"));
        registry.register(FlickerOperatorFishing.instance.setRegistryName(ArsMagica.MODID, "flicker_fishing"));
    }

}

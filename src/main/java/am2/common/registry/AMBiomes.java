package am2.common.registry;

import am2.ArsMagica;
import am2.common.world.BiomeWitchwoodForest;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Mod.EventBusSubscriber(modid = ArsMagica.MODID)
public class AMBiomes {

    private static List<Biome> biomesToRegister;

    public static List<Biome> GetBiomesToRegister() {
        if (biomesToRegister == null) biomesToRegister = new ArrayList<>();
        return biomesToRegister;
    }

    @SubscribeEvent
    public static void registerBiomes(RegistryEvent.Register<Biome> event) {
        if (ArsMagica.config.getEnableWitchwoodForest()) {
            BiomeWitchwoodForest.instance.setRegistryName(ArsMagica.MODID, "witchwood_forest");
            event.getRegistry().register(BiomeWitchwoodForest.instance);
        }
        for (Biome biome : GetBiomesToRegister()) event.getRegistry().register(biome);
        RegisterCustomBiomes();
    }

    private static void RegisterCustomBiomes() {
        if (ArsMagica.config.getEnableWitchwoodForest()) {
            Biome registeredWitchwood = Objects.requireNonNull(GameRegistry.findRegistry(Biome.class).getValue(BiomeWitchwoodForest.instance.getRegistryName()));
            BiomeDictionary.addTypes(registeredWitchwood, BiomeDictionary.Type.FOREST, BiomeDictionary.Type.MAGICAL);
            BiomeManager.addBiome(BiomeManager.BiomeType.WARM, new BiomeManager.BiomeEntry(registeredWitchwood, ArsMagica.config.getWitchwoodForestRarity()));
            BiomeManager.addSpawnBiome(registeredWitchwood);
        }
    }
}

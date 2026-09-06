package am2.common.registry;

import am2.ArsMagica;
import am2.client.bosses.renderers.*;
import am2.client.entity.render.*;
import am2.common.LogHelper;
import am2.common.bosses.*;
import am2.common.entity.*;
import am2.common.utils.RenderFactory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.Set;

@Mod.EventBusSubscriber(modid = ArsMagica.MODID)
public class AMEntities {
    public static final AMEntities instance = new AMEntities();

    private static int id = 0;

    @SubscribeEvent
    public static void registerEntities(RegistryEvent.Register<EntityEntry> event) {
        IForgeRegistry<EntityEntry> registry = event.getRegistry();

        registry.register(create(EntitySpellProjectile.class,   "spell_projectile")   .tracker(64, 2, true).build());
        registry.register(create(EntityRiftStorage.class,        "rift_storage")        .tracker(64, 2, false).build());
        registry.register(create(EntitySpellEffect.class,        "spell_effect")        .tracker(64, 2, true).build());
        registry.register(create(EntityThrownRock.class,         "thrown_rock")         .tracker(64, 2, true).build());
        registry.register(create(EntityBoundArrow.class,         "bound_arrow")         .tracker(64, 2, true).build());
        registry.register(create(EntityDarkling.class,           "darkling")            .tracker(64, 2, true).build());
        registry.register(create(EntityDarkMage.class,           "dark_mage")           .tracker(64, 2, true).egg(0x110011, 0xAA00FF).build());
        registry.register(create(EntityDryad.class,              "dryad")               .tracker(64, 2, true).egg(0x00ff00, 0x34e122).build());
        registry.register(create(EntityDruid.class,              "druid")               .tracker(64, 2, true).egg(0x4a7023, 0x6b4423).build());
        registry.register(create(EntityAirElemental.class,       "air_elemental")       .tracker(64, 2, true).egg(0xeaf6ff, 0xffd84d).build());
        registry.register(create(EntityEarthElemental.class,     "earth_elemental")     .tracker(64, 2, true).egg(0x61330b, 0x00ff00).build());
        registry.register(create(EntityFireElemental.class,      "fire_elemental")      .tracker(64, 2, true).egg(0xef260b, 0xff0000).build());
        registry.register(create(EntityLightMage.class,          "light_mage")          .tracker(64, 2, true).egg(0xEEEEFF, 0xAA00FF).build());
        registry.register(create(EntityManaElemental.class,      "mana_elemental")      .tracker(64, 2, true).egg(0xcccccc, 0xb935cd).build());
        registry.register(create(EntityManaVortex.class,         "mana_vortex")         .tracker(64, 2, true).build());
        registry.register(create(EntityShockwave.class,          "shockwave")           .tracker(64, 2, true).build());
        registry.register(create(EntityThrownSickle.class,       "thrown_sickle")       .tracker(64, 2, true).build());
        registry.register(create(EntityWhirlwind.class,          "whirlwind")           .tracker(64, 2, true).build());
        registry.register(create(EntityWinterGuardianArm.class,  "winter_guardian_arm") .tracker(64, 2, true).build());

        registry.register(create(EntityAirGuardian.class,        "air_guardian")        .tracker(64, 2, true).egg(0xFFFFFF, 0xFFCC00).build());
        registry.register(create(EntityArcaneGuardian.class,     "arcane_guardian")     .tracker(64, 2, true).egg(0x999999, 0xcc00cc).build());
        registry.register(create(EntityEarthGuardian.class,      "earth_guardian")      .tracker(64, 2, true).egg(0x663300, 0x339900).build());
        registry.register(create(EntityEnderGuardian.class,      "ender_guardian")      .tracker(64, 2, true).egg(0x000000, 0x6633).build());
        registry.register(create(EntityFireGuardian.class,       "fire_guardian")       .tracker(64, 2, true).egg(0xFFFFFF, 0xFF0000).build());
        registry.register(create(EntityLifeGuardian.class,       "life_guardian")       .tracker(64, 2, true).egg(0x00E6FF, 0xFFE600).build());
        registry.register(create(EntityLightningGuardian.class,  "lightning_guardian")  .tracker(64, 2, true).egg(0xFFE600, 0x00C4FF).build());
        registry.register(create(EntityNatureGuardian.class,     "nature_guardian")     .tracker(64, 2, true).egg(0x44FF00, 0x307D0F).build());
        registry.register(create(EntityWaterGuardian.class,      "water_guardian")      .tracker(64, 2, true).egg(0x0F387D, 0x0097CE).build());
        registry.register(create(EntityWinterGuardian.class,     "winter_guardian")     .tracker(64, 2, true).egg(0x00CEBA, 0x104742).build());

        registry.register(create(EntityAirSled.class,            "air_sled")            .tracker(64, 2, true).build());
        registry.register(create(EntityBroom.class,              "broom")               .tracker(64, 2, true).build());
        registry.register(create(EntityWaterElemental.class,     "water_elemental")     .tracker(64, 2, true).egg(0x0b5cef, 0x0000ff).build());
        registry.register(create(EntityManaCreeper.class,        "mana_creeper")        .tracker(64, 2, true).egg(0x0b5cef, 0xb935cd).build());
        registry.register(create(EntityHecate.class,             "hecate")              .tracker(64, 2, true).egg(0xef260b, 0x3f043d).build());
        registry.register(create(EntityFlicker.class,            "flicker")             .tracker(64, 2, true).build());
        registry.register(create(EntityHellCow.class,            "hell_cow")            .tracker(64, 2, true).build());
        registry.register(create(EntityShadowHelper.class,       "shadow_helper")       .tracker(64, 2, true).build());
        registry.register(create(EntitySpellOrb.class,           "spell_orb")           .tracker(64, 2, true).build());
        registry.register(create(EntitySpellPuddle.class,        "spell_puddle")        .tracker(64, 2, true).build());
        registry.register(create(EntityLightningElemental.class, "lightning_elemental") .tracker(64, 2, true).egg(0xFFFF44, 0xFFAA00).build());
        registry.register(create(EntityIceElemental.class,       "ice_elemental")       .tracker(64, 2, true).egg(0xaaddff, 0xffffff).build());
        registry.register(create(EntityFlyingBook.class,         "flying_book")         .tracker(64, 2, true).build());
        registry.register(create(EntityNatureElemental.class,   "nature_elemental")   .tracker(64, 2, true).egg(0x49652a, 0x785332).build());
    }

    private static <T extends Entity> EntityEntryBuilder<T> create(Class<T> entityClass, String name) {
        ResourceLocation registryName = new ResourceLocation(ArsMagica.MODID, name);
        return EntityEntryBuilder.<T>create()
                .entity(entityClass)
                .id(registryName, id++)
                .name(ArsMagica.MODID + "." + name);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void registerRenderers() {
        RenderingRegistry.registerEntityRenderingHandler(EntityRiftStorage.class, new RenderFactory(RenderRiftStorage.class));
        RenderingRegistry.registerEntityRenderingHandler(EntitySpellProjectile.class, new RenderFactory(RenderSpellProjectile.class));
        RenderingRegistry.registerEntityRenderingHandler(EntitySpellOrb.class, new RenderFactory(RenderSpellOrb.class));
        RenderingRegistry.registerEntityRenderingHandler(EntitySpellPuddle.class, new RenderFactory(RenderSpellPuddle.class));
        RenderingRegistry.registerEntityRenderingHandler(EntitySpellEffect.class, new RenderFactory(RenderHidden.class));
        RenderingRegistry.registerEntityRenderingHandler(EntityThrownRock.class, new RenderFactory(RenderThrownRock.class));
        RenderingRegistry.registerEntityRenderingHandler(EntityBoundArrow.class, new RenderFactory(RenderBoundArrow.class));

        RenderingRegistry.registerEntityRenderingHandler(EntityThrownSickle.class, new RenderFactory(RenderThrownSickle.class));
        RenderingRegistry.registerEntityRenderingHandler(EntityWinterGuardianArm.class, new RenderFactory(RenderWinterGuardianArm.class));
        RenderingRegistry.registerEntityRenderingHandler(EntityAirSled.class, new RenderFactory(RenderAirSled.class));
        //Bosses
        RenderingRegistry.registerEntityRenderingHandler(EntityAirGuardian.class, new RenderFactory(RenderAirGuardian.class));
        RenderingRegistry.registerEntityRenderingHandler(EntityArcaneGuardian.class, new RenderFactory(RenderArcaneGuardian.class));
        RenderingRegistry.registerEntityRenderingHandler(EntityEarthGuardian.class, new RenderFactory(RenderEarthGuardian.class));
        RenderingRegistry.registerEntityRenderingHandler(EntityFireGuardian.class, new RenderFactory(RenderFireGuardian.class));
        RenderingRegistry.registerEntityRenderingHandler(EntityEnderGuardian.class, new RenderFactory(RenderEnderGuardian.class));
        RenderingRegistry.registerEntityRenderingHandler(EntityFireGuardian.class, new RenderFactory(RenderFireGuardian.class));
        RenderingRegistry.registerEntityRenderingHandler(EntityLifeGuardian.class, new RenderFactory(RenderLifeGuardian.class));
        RenderingRegistry.registerEntityRenderingHandler(EntityLightningGuardian.class, new RenderFactory(RenderLightningGuardian.class));
        RenderingRegistry.registerEntityRenderingHandler(EntityNatureGuardian.class, new RenderFactory(RenderPlantGuardian.class));
        RenderingRegistry.registerEntityRenderingHandler(EntityWaterGuardian.class, new RenderFactory(RenderWaterGuardian.class));
        RenderingRegistry.registerEntityRenderingHandler(EntityWinterGuardian.class, new RenderFactory(RenderIceGuardian.class));

        RenderingRegistry.registerEntityRenderingHandler(EntityManaElemental.class, new RenderFactory(RenderManaElemental.class));
        RenderingRegistry.registerEntityRenderingHandler(EntityWaterElemental.class, new RenderFactory(RenderWaterElemental.class));
        RenderingRegistry.registerEntityRenderingHandler(EntityFireElemental.class, new RenderFactory(RenderFireElemental.class));
        RenderingRegistry.registerEntityRenderingHandler(EntityEarthElemental.class, new RenderFactory(RenderEarthElemental.class));
        RenderingRegistry.registerEntityRenderingHandler(EntityLightningElemental.class, new RenderFactory(RenderLightningElemental.class));
        RenderingRegistry.registerEntityRenderingHandler(EntityIceElemental.class, new RenderFactory(RenderIceElemental.class));
        RenderingRegistry.registerEntityRenderingHandler(EntityAirElemental.class, new RenderFactory(RenderAirElemental.class));
        RenderingRegistry.registerEntityRenderingHandler(EntityNatureElemental.class, new RenderFactory(RenderNatureElemental.class));
        RenderingRegistry.registerEntityRenderingHandler(EntityManaCreeper.class, new RenderFactory(RenderManaCreeper.class));
        RenderingRegistry.registerEntityRenderingHandler(EntityLightMage.class, new RenderFactory(RenderLightMage.class));
        RenderingRegistry.registerEntityRenderingHandler(EntityDarkMage.class, new RenderFactory(RenderDarkMage.class));
        RenderingRegistry.registerEntityRenderingHandler(EntityManaVortex.class, new RenderFactory(RenderManaVortex.class));
        RenderingRegistry.registerEntityRenderingHandler(EntityHecate.class, new RenderFactory(RenderHecate.class));
        RenderingRegistry.registerEntityRenderingHandler(EntityDryad.class, new RenderFactory(RenderDryad.class));
        RenderingRegistry.registerEntityRenderingHandler(EntityDruid.class, new RenderFactory(RenderDruid.class));
        RenderingRegistry.registerEntityRenderingHandler(EntityFlicker.class, new RenderFactory(RenderFlicker.class));
        RenderingRegistry.registerEntityRenderingHandler(EntityHellCow.class, new RenderFactory(RenderHellCow.class));
        RenderingRegistry.registerEntityRenderingHandler(EntityDarkling.class, new RenderFactory(RenderDarkling.class));
        RenderingRegistry.registerEntityRenderingHandler(EntityShadowHelper.class, new RenderFactory(RenderShadowHelper.class));
        RenderingRegistry.registerEntityRenderingHandler(EntityBroom.class, new RenderFactory(RenderBroom.class));
        RenderingRegistry.registerEntityRenderingHandler(EntityShockwave.class, new RenderFactory(RenderShockwave.class));
        RenderingRegistry.registerEntityRenderingHandler(EntityFlyingBook.class, new RenderFactory(RenderFlyingBook.class));
    //    RenderingRegistry.registerEntityRenderingHandler(EntitySpellSlice.class, new RenderFactory(RenderSpellSlice.class));
    }

    public void initializeSpawns() {

        //SpawnListEntry wisps = new SpawnListEntry(EntityWisp.class, 1, 1, 1);
        Biome.SpawnListEntry manaElementals = new Biome.SpawnListEntry(EntityManaElemental.class, ArsMagica.config.GetManaElementalSpawnRate(), 1, 1);
        Biome.SpawnListEntry dryads = new Biome.SpawnListEntry(EntityDryad.class, ArsMagica.config.GetDryadSpawnRate(), 1, 2);
        Biome.SpawnListEntry hecates_nonHell = new Biome.SpawnListEntry(EntityHecate.class, ArsMagica.config.GetHecateSpawnRate(), 1, 1);
        Biome.SpawnListEntry hecates_hell = new Biome.SpawnListEntry(EntityHecate.class, ArsMagica.config.GetHecateSpawnRate() * 2, 1, 2);
        Biome.SpawnListEntry manaCreepers = new Biome.SpawnListEntry(EntityManaCreeper.class, ArsMagica.config.GetManaCreeperSpawnRate(), 1, 1);
        Biome.SpawnListEntry lightMages = new Biome.SpawnListEntry(EntityLightMage.class, ArsMagica.config.GetMageSpawnRate(), 1, 3);
        Biome.SpawnListEntry darkMages = new Biome.SpawnListEntry(EntityDarkMage.class, ArsMagica.config.GetMageSpawnRate(), 1, 3);
        Biome.SpawnListEntry waterElementals = new Biome.SpawnListEntry(EntityWaterElemental.class, ArsMagica.config.GetWaterElementalSpawnRate(), 1, 3);
        Biome.SpawnListEntry darklings = new Biome.SpawnListEntry(EntityDarkling.class, ArsMagica.config.GetDarklingSpawnRate(), 4, 8);
        Biome.SpawnListEntry earthElementals = new Biome.SpawnListEntry(EntityEarthElemental.class, ArsMagica.config.GetEarthElementalSpawnRate(), 1, 2);
        Biome.SpawnListEntry fireElementals = new Biome.SpawnListEntry(EntityFireElemental.class, ArsMagica.config.GetFireElementalSpawnRate(), 1, 1);
        Biome.SpawnListEntry lightningElementals = new Biome.SpawnListEntry(EntityLightningElemental.class, ArsMagica.config.GetLightningElementalSpawnRate(), 1, 1);
        Biome.SpawnListEntry airElementals = new Biome.SpawnListEntry(EntityAirElemental.class, ArsMagica.config.GetAirElementalSpawnRate(), 1, 2);
        Biome.SpawnListEntry natureElementals = new Biome.SpawnListEntry(EntityNatureElemental.class, ArsMagica.config.GetNatureElementalSpawnRate(), 1, 2);
        Biome.SpawnListEntry flickers = new Biome.SpawnListEntry(EntityFlicker.class, ArsMagica.config.GetFlickerSpawnRate(), 1, 1);

        initSpawnsForBiomeTypes(manaElementals, EnumCreatureType.MONSTER, new BiomeDictionary.Type[]{BiomeDictionary.Type.BEACH, BiomeDictionary.Type.DRY, BiomeDictionary.Type.FOREST, BiomeDictionary.Type.COLD, BiomeDictionary.Type.HILLS, BiomeDictionary.Type.JUNGLE, BiomeDictionary.Type.MAGICAL, BiomeDictionary.Type.MOUNTAIN, BiomeDictionary.Type.PLAINS, BiomeDictionary.Type.SWAMP, BiomeDictionary.Type.WASTELAND}, new BiomeDictionary.Type[]{BiomeDictionary.Type.END, BiomeDictionary.Type.NETHER, BiomeDictionary.Type.MUSHROOM});

        initSpawnsForBiomeTypes(dryads, EnumCreatureType.CREATURE, new BiomeDictionary.Type[]{BiomeDictionary.Type.BEACH, BiomeDictionary.Type.FOREST, BiomeDictionary.Type.MAGICAL, BiomeDictionary.Type.HILLS, BiomeDictionary.Type.JUNGLE, BiomeDictionary.Type.MOUNTAIN, BiomeDictionary.Type.PLAINS}, new BiomeDictionary.Type[]{BiomeDictionary.Type.END, BiomeDictionary.Type.COLD, BiomeDictionary.Type.MUSHROOM, BiomeDictionary.Type.NETHER, BiomeDictionary.Type.WASTELAND, BiomeDictionary.Type.SWAMP, BiomeDictionary.Type.DRY});

        initSpawnsForBiomeTypes(hecates_nonHell, EnumCreatureType.MONSTER, new BiomeDictionary.Type[]{BiomeDictionary.Type.BEACH, BiomeDictionary.Type.DRY, BiomeDictionary.Type.FOREST, BiomeDictionary.Type.COLD, BiomeDictionary.Type.HILLS, BiomeDictionary.Type.JUNGLE, BiomeDictionary.Type.MAGICAL, BiomeDictionary.Type.MOUNTAIN, BiomeDictionary.Type.PLAINS, BiomeDictionary.Type.SWAMP, BiomeDictionary.Type.WASTELAND}, new BiomeDictionary.Type[]{BiomeDictionary.Type.END, BiomeDictionary.Type.NETHER, BiomeDictionary.Type.MUSHROOM});

        initSpawnsForBiomeTypes(hecates_hell, EnumCreatureType.MONSTER, new BiomeDictionary.Type[]{BiomeDictionary.Type.NETHER}, new BiomeDictionary.Type[]{BiomeDictionary.Type.MUSHROOM});

        initSpawnsForBiomeTypes(darklings, EnumCreatureType.MONSTER, new BiomeDictionary.Type[]{BiomeDictionary.Type.NETHER}, new BiomeDictionary.Type[]{BiomeDictionary.Type.MUSHROOM});

        initSpawnsForBiomeTypes(manaCreepers, EnumCreatureType.MONSTER, new BiomeDictionary.Type[]{BiomeDictionary.Type.BEACH, BiomeDictionary.Type.DRY, BiomeDictionary.Type.FOREST, BiomeDictionary.Type.COLD, BiomeDictionary.Type.HILLS, BiomeDictionary.Type.JUNGLE, BiomeDictionary.Type.MAGICAL, BiomeDictionary.Type.MOUNTAIN, BiomeDictionary.Type.PLAINS, BiomeDictionary.Type.SWAMP, BiomeDictionary.Type.WASTELAND}, new BiomeDictionary.Type[]{BiomeDictionary.Type.END, BiomeDictionary.Type.NETHER, BiomeDictionary.Type.MUSHROOM});

        initSpawnsForBiomeTypes(lightMages, EnumCreatureType.MONSTER, new BiomeDictionary.Type[]{BiomeDictionary.Type.BEACH, BiomeDictionary.Type.DRY, BiomeDictionary.Type.FOREST, BiomeDictionary.Type.COLD, BiomeDictionary.Type.HILLS, BiomeDictionary.Type.JUNGLE, BiomeDictionary.Type.MAGICAL, BiomeDictionary.Type.MOUNTAIN, BiomeDictionary.Type.PLAINS, BiomeDictionary.Type.SWAMP, BiomeDictionary.Type.WASTELAND}, new BiomeDictionary.Type[]{BiomeDictionary.Type.END, BiomeDictionary.Type.NETHER, BiomeDictionary.Type.MUSHROOM});

        initSpawnsForBiomeTypes(darkMages, EnumCreatureType.MONSTER, new BiomeDictionary.Type[]{BiomeDictionary.Type.BEACH, BiomeDictionary.Type.DRY, BiomeDictionary.Type.FOREST, BiomeDictionary.Type.COLD, BiomeDictionary.Type.HILLS, BiomeDictionary.Type.JUNGLE, BiomeDictionary.Type.MAGICAL, BiomeDictionary.Type.MOUNTAIN, BiomeDictionary.Type.PLAINS, BiomeDictionary.Type.SWAMP, BiomeDictionary.Type.WASTELAND}, new BiomeDictionary.Type[]{BiomeDictionary.Type.END, BiomeDictionary.Type.NETHER, BiomeDictionary.Type.MUSHROOM});

        initSpawnsForBiomeTypes(waterElementals, EnumCreatureType.MONSTER, new BiomeDictionary.Type[]{BiomeDictionary.Type.WATER}, new BiomeDictionary.Type[]{BiomeDictionary.Type.END, BiomeDictionary.Type.NETHER, BiomeDictionary.Type.MUSHROOM});
        initSpawnsForBiomeTypes(waterElementals, EnumCreatureType.WATER_CREATURE, new BiomeDictionary.Type[]{BiomeDictionary.Type.WATER}, new BiomeDictionary.Type[]{BiomeDictionary.Type.END, BiomeDictionary.Type.NETHER, BiomeDictionary.Type.MUSHROOM});

        initSpawnsForBiomeTypes(earthElementals, EnumCreatureType.MONSTER, new BiomeDictionary.Type[]{BiomeDictionary.Type.HILLS, BiomeDictionary.Type.MOUNTAIN}, new BiomeDictionary.Type[]{BiomeDictionary.Type.END, BiomeDictionary.Type.NETHER, BiomeDictionary.Type.MUSHROOM});
        if (ArsMagica.config.GetEarthElementalSpawnUnderground()) {
            initSpawnsForBiomeTypes(earthElementals, EnumCreatureType.MONSTER, new BiomeDictionary.Type[]{BiomeDictionary.Type.BEACH, BiomeDictionary.Type.DRY, BiomeDictionary.Type.FOREST, BiomeDictionary.Type.COLD, BiomeDictionary.Type.JUNGLE, BiomeDictionary.Type.MAGICAL, BiomeDictionary.Type.PLAINS, BiomeDictionary.Type.SWAMP, BiomeDictionary.Type.WASTELAND}, new BiomeDictionary.Type[]{BiomeDictionary.Type.END, BiomeDictionary.Type.NETHER, BiomeDictionary.Type.MUSHROOM});
        }
        initSpawnsForBiomeTypes(fireElementals, EnumCreatureType.MONSTER, new BiomeDictionary.Type[]{BiomeDictionary.Type.NETHER}, new BiomeDictionary.Type[]{BiomeDictionary.Type.MUSHROOM});
        initSpawnsForBiomeTypes(fireElementals, EnumCreatureType.MONSTER, new BiomeDictionary.Type[]{BiomeDictionary.Type.HOT, BiomeDictionary.Type.DRY}, new BiomeDictionary.Type[]{BiomeDictionary.Type.END, BiomeDictionary.Type.NETHER, BiomeDictionary.Type.MUSHROOM});

        Biome.SpawnListEntry iceElementals = new Biome.SpawnListEntry(EntityIceElemental.class, ArsMagica.config.GetIceElementalSpawnRate(), 1, 2);
        initSpawnsForBiomeTypes(iceElementals, EnumCreatureType.MONSTER, new BiomeDictionary.Type[]{BiomeDictionary.Type.COLD, BiomeDictionary.Type.SNOWY}, new BiomeDictionary.Type[]{BiomeDictionary.Type.END, BiomeDictionary.Type.NETHER, BiomeDictionary.Type.MUSHROOM});

        initSpawnsForBiomeTypes(lightningElementals, EnumCreatureType.MONSTER, new BiomeDictionary.Type[]{BiomeDictionary.Type.MOUNTAIN, BiomeDictionary.Type.HILLS, BiomeDictionary.Type.BEACH}, new BiomeDictionary.Type[]{BiomeDictionary.Type.END, BiomeDictionary.Type.NETHER, BiomeDictionary.Type.MUSHROOM});

        initSpawnsForBiomeTypes(airElementals, EnumCreatureType.MONSTER, new BiomeDictionary.Type[]{BiomeDictionary.Type.MOUNTAIN, BiomeDictionary.Type.HILLS, BiomeDictionary.Type.PLAINS, BiomeDictionary.Type.BEACH}, new BiomeDictionary.Type[]{BiomeDictionary.Type.END, BiomeDictionary.Type.NETHER, BiomeDictionary.Type.MUSHROOM});

        initSpawnsForBiomeTypes(flickers, EnumCreatureType.AMBIENT, BiomeDictionary.Type.getAll().toArray(new BiomeDictionary.Type[0]), new BiomeDictionary.Type[0]);
        // FOREST includes vanilla roofed forests; the dictionary also supports modded forests/jungles.
        initSpawnsForBiomeTypes(natureElementals, EnumCreatureType.MONSTER, new BiomeDictionary.Type[]{BiomeDictionary.Type.FOREST, BiomeDictionary.Type.JUNGLE}, new BiomeDictionary.Type[]{BiomeDictionary.Type.END, BiomeDictionary.Type.NETHER, BiomeDictionary.Type.MUSHROOM});

    }

    private void initSpawnsForBiomeTypes(Biome.SpawnListEntry spawnListEntry, EnumCreatureType creatureType, BiomeDictionary.Type[] types, BiomeDictionary.Type[] exclusions) {
        if (spawnListEntry.itemWeight == 0) {
            LogHelper.info("Skipping spawn list entry for %s (as type %s), as the weight is set to 0.  This can be changed in config.", spawnListEntry.entityClass.getName(), creatureType.toString());
            return;
        }
        for (BiomeDictionary.Type type : types) {
            Set<Biome> biomes = BiomeDictionary.getBiomes(type);
            initSpawnsForBiomes(biomes.toArray(new Biome[biomes.size()]), spawnListEntry, creatureType, exclusions);
        }
    }

    private void initSpawnsForBiomes(Biome[] biomes, Biome.SpawnListEntry spawnListEntry, EnumCreatureType creatureType, BiomeDictionary.Type[] exclusions) {
        if (biomes == null) return;
        for (Biome biome : biomes) {
            if (biomeIsExcluded(biome, exclusions)) continue;
            if (!biome.getSpawnableList(creatureType).contains(spawnListEntry))
                biome.getSpawnableList(creatureType).add(spawnListEntry);
        }
    }

    private boolean biomeIsExcluded(Biome biome, BiomeDictionary.Type[] exclusions) {

        BiomeDictionary.Type[] biomeTypes = BiomeDictionary.getTypes(biome).toArray(new BiomeDictionary.Type[0]);

        for (BiomeDictionary.Type exclusion : exclusions) {
            for (BiomeDictionary.Type biomeType : biomeTypes) {
                if (biomeType == exclusion) return true;
            }
        }
        return false;
    }
}

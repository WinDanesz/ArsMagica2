package am2.common.registry;

import am2.ArsMagica;
import am2.client.bosses.renderers.*;
import am2.client.entity.render.*;
import am2.common.LogHelper;
import am2.common.bosses.*;
import am2.common.entity.*;
import am2.common.utils.RenderFactory;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry;

import java.util.Set;

public class AMEntities {
    public static final AMEntities instance = new AMEntities();

    public void registerEntities() {
        EntityRegistry.registerModEntity(new ResourceLocation(ArsMagica.MODID, "spell_projectile"), EntitySpellProjectile.class, "spell_projectile", 0, ArsMagica.instance, 64, 2, true);
        EntityRegistry.registerModEntity(new ResourceLocation(ArsMagica.MODID, "rift_storage"), EntityRiftStorage.class, "rift_storage", 1, ArsMagica.instance, 64, 2, false);
        EntityRegistry.registerModEntity(new ResourceLocation(ArsMagica.MODID, "spell_effect"), EntitySpellEffect.class, "spell_effect", 2, ArsMagica.instance, 64, 2, true);
        EntityRegistry.registerModEntity(new ResourceLocation(ArsMagica.MODID, "thrown_rock"), EntityThrownRock.class, "thrown_rock", 3, ArsMagica.instance, 64, 2, true);
        EntityRegistry.registerModEntity(new ResourceLocation(ArsMagica.MODID, "bound_arrow"), EntityBoundArrow.class, "bound_arrow", 4, ArsMagica.instance, 64, 2, true);
        EntityRegistry.registerModEntity(new ResourceLocation(ArsMagica.MODID, "darkling"), EntityDarkling.class, "darkling", 5, ArsMagica.instance, 64, 2, true);
        EntityRegistry.registerModEntity(new ResourceLocation(ArsMagica.MODID, "dark_mage"), EntityDarkMage.class, "dark_mage", 6, ArsMagica.instance, 64, 2, true, 0x110011, 0xAA00FF);
        EntityRegistry.registerModEntity(new ResourceLocation(ArsMagica.MODID, "dryad"), EntityDryad.class, "dryad", 7, ArsMagica.instance, 64, 2, true, 0x00ff00, 0x34e122);
        EntityRegistry.registerModEntity(new ResourceLocation(ArsMagica.MODID, "earth_elemental"), EntityEarthElemental.class, "earth_elemental", 8, ArsMagica.instance, 64, 2, true, 0x61330b, 0x00ff00);
        EntityRegistry.registerModEntity(new ResourceLocation(ArsMagica.MODID, "fire_elemental"), EntityFireElemental.class, "fire_elemental", 9, ArsMagica.instance, 64, 2, true, 0xef260b, 0xff0000);
        EntityRegistry.registerModEntity(new ResourceLocation(ArsMagica.MODID, "light_mage"), EntityLightMage.class, "light_mage", 10, ArsMagica.instance, 64, 2, true, 0xEEEEFF, 0xAA00FF);
        EntityRegistry.registerModEntity(new ResourceLocation(ArsMagica.MODID, "mana_elemental"), EntityManaElemental.class, "mana_elemental", 11, ArsMagica.instance, 64, 2, true, 0xcccccc, 0xb935cd);
        EntityRegistry.registerModEntity(new ResourceLocation(ArsMagica.MODID, "mana_vortex"), EntityManaVortex.class, "mana_vortex", 12, ArsMagica.instance, 64, 2, true);
        EntityRegistry.registerModEntity(new ResourceLocation(ArsMagica.MODID, "shockwave"), EntityShockwave.class, "shockwave", 13, ArsMagica.instance, 64, 2, true);
        EntityRegistry.registerModEntity(new ResourceLocation(ArsMagica.MODID, "thrown_sickle"), EntityThrownSickle.class, "thrown_sickle", 14, ArsMagica.instance, 64, 2, true);
        EntityRegistry.registerModEntity(new ResourceLocation(ArsMagica.MODID, "whirlwind"), EntityWhirlwind.class, "whirlwind", 15, ArsMagica.instance, 64, 2, true);
        EntityRegistry.registerModEntity(new ResourceLocation(ArsMagica.MODID, "winter_guardian_arm"), EntityWinterGuardianArm.class, "winter_guardian_arm", 16, ArsMagica.instance, 64, 2, true);

        EntityRegistry.registerModEntity(new ResourceLocation(ArsMagica.MODID, "air_guardian"), EntityAirGuardian.class, "air_guardian", 17, ArsMagica.instance, 64, 2, true, 0xFFFFFF, 0xFFCC00);
        EntityRegistry.registerModEntity(new ResourceLocation(ArsMagica.MODID, "arcane_guardian"), EntityArcaneGuardian.class, "arcane_guardian", 18, ArsMagica.instance, 64, 2, true, 0x999999, 0xcc00cc);
        EntityRegistry.registerModEntity(new ResourceLocation(ArsMagica.MODID, "earth_guardian"), EntityEarthGuardian.class, "earth_guardian", 19, ArsMagica.instance, 64, 2, true, 0x663300, 0x339900);
        EntityRegistry.registerModEntity(new ResourceLocation(ArsMagica.MODID, "ender_guardian"), EntityEnderGuardian.class, "ender_guardian", 20, ArsMagica.instance, 64, 2, true, 0x000000, 0x6633);
        EntityRegistry.registerModEntity(new ResourceLocation(ArsMagica.MODID, "fire_guardian"), EntityFireGuardian.class, "fire_guardian", 21, ArsMagica.instance, 64, 2, true, 0xFFFFFF, 0xFF0000);
        EntityRegistry.registerModEntity(new ResourceLocation(ArsMagica.MODID, "life_guardian"), EntityLifeGuardian.class, "life_guardian", 22, ArsMagica.instance, 64, 2, true, 0x00E6FF, 0xFFE600);
        EntityRegistry.registerModEntity(new ResourceLocation(ArsMagica.MODID, "lightning_guardian"), EntityLightningGuardian.class, "lightning_guardian", 23, ArsMagica.instance, 64, 2, true, 0xFFE600, 0x00C4FF);
        EntityRegistry.registerModEntity(new ResourceLocation(ArsMagica.MODID, "nature_guardian"), EntityNatureGuardian.class, "nature_guardian", 24, ArsMagica.instance, 64, 2, true, 0x44FF00, 0x307D0F);
        EntityRegistry.registerModEntity(new ResourceLocation(ArsMagica.MODID, "water_guardian"), EntityWaterGuardian.class, "water_guardian", 25, ArsMagica.instance, 64, 2, true, 0x0F387D, 0x0097CE);
        EntityRegistry.registerModEntity(new ResourceLocation(ArsMagica.MODID, "winter_guardian"), EntityWinterGuardian.class, "winter_guardian", 26, ArsMagica.instance, 64, 2, true, 0x00CEBA, 0x104742);

        EntityRegistry.registerModEntity(new ResourceLocation(ArsMagica.MODID, "air_sled"), EntityAirSled.class, "air_sled", 27, ArsMagica.instance, 64, 2, true);
        EntityRegistry.registerModEntity(new ResourceLocation(ArsMagica.MODID, "broom"), EntityBroom.class, "broom", 28, ArsMagica.instance, 64, 2, true);
        EntityRegistry.registerModEntity(new ResourceLocation(ArsMagica.MODID, "water_elemental"), EntityWaterElemental.class, "water_elemental", 29, ArsMagica.instance, 64, 2, true, 0x0b5cef, 0x0000ff);
        EntityRegistry.registerModEntity(new ResourceLocation(ArsMagica.MODID, "mana_creeper"), EntityManaCreeper.class, "mana_creeper", 30, ArsMagica.instance, 64, 2, true, 0x0b5cef, 0xb935cd);
        EntityRegistry.registerModEntity(new ResourceLocation(ArsMagica.MODID, "lightning_elemental"), EntityLightningElemental.class, "lightning_elemental", 37, ArsMagica.instance, 64, 2, true, 0xFFFF44, 0xFFAA00);
        EntityRegistry.registerModEntity(new ResourceLocation(ArsMagica.MODID, "ice_elemental"), EntityIceElemental.class, "ice_elemental", 38, ArsMagica.instance, 64, 2, true, 0xaaddff, 0xffffff);
        EntityRegistry.registerModEntity(new ResourceLocation(ArsMagica.MODID, "hecate"), EntityHecate.class, "hecate", 31, ArsMagica.instance, 64, 2, true, 0xef260b, 0x3f043d);
        EntityRegistry.registerModEntity(new ResourceLocation(ArsMagica.MODID, "flicker"), EntityFlicker.class, "flicker", 32, ArsMagica.instance, 64, 2, true);
        EntityRegistry.registerModEntity(new ResourceLocation(ArsMagica.MODID, "hell_cow"), EntityHellCow.class, "hell_cow", 33, ArsMagica.instance, 64, 2, true);
        EntityRegistry.registerModEntity(new ResourceLocation(ArsMagica.MODID, "shadow_helper"), EntityShadowHelper.class, "shadow_helper", 34, ArsMagica.instance, 64, 2, true);
        EntityRegistry.registerModEntity(new ResourceLocation(ArsMagica.MODID, "spell_orb"), EntitySpellOrb.class, "spell_orb", 35, ArsMagica.instance, 64, 2, true);
        EntityRegistry.registerModEntity(new ResourceLocation(ArsMagica.MODID, "spell_puddle"), EntitySpellPuddle.class, "spell_puddle", 36, ArsMagica.instance, 64, 2, true);
        EntityRegistry.registerModEntity(new ResourceLocation(ArsMagica.MODID, "flying_book"), EntityFlyingBook.class, "flying_book", 39, ArsMagica.instance, 64, 2, true);
       // EntityRegistry.registerModEntity(new ResourceLocation(ArsMagica.MODID, "spell_slice"), EntitySpellSlice.class, "spell_slice", 37, ArsMagica.instance, 64, 2, true);
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
        RenderingRegistry.registerEntityRenderingHandler(EntityManaCreeper.class, new RenderFactory(RenderManaCreeper.class));
        RenderingRegistry.registerEntityRenderingHandler(EntityLightMage.class, new RenderFactory(RenderLightMage.class));
        RenderingRegistry.registerEntityRenderingHandler(EntityDarkMage.class, new RenderFactory(RenderDarkMage.class));
        RenderingRegistry.registerEntityRenderingHandler(EntityManaVortex.class, new RenderFactory(RenderManaVortex.class));
        RenderingRegistry.registerEntityRenderingHandler(EntityHecate.class, new RenderFactory(RenderHecate.class));
        RenderingRegistry.registerEntityRenderingHandler(EntityDryad.class, new RenderFactory(RenderDryad.class));
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

        initSpawnsForBiomeTypes(flickers, EnumCreatureType.AMBIENT, BiomeDictionary.Type.getAll().toArray(new BiomeDictionary.Type[0]), new BiomeDictionary.Type[0]);

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

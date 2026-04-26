package am2.common.world;

import am2.ArsMagica;
import am2.common.entity.SpawnBlacklists;
import am2.common.registry.AMBlocks;
import net.minecraft.block.state.pattern.BlockMatcher;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.event.terraingen.TerrainGen;
import net.minecraftforge.fml.common.IWorldGenerator;

import java.util.ArrayList;
import java.util.Random;

import static net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType.LAKE;

public class AM2WorldDecorator implements IWorldGenerator {

    //ores
    private final WorldGenMinable vinteum;
    private final WorldGenMinable blueTopaz;
    private final WorldGenMinable chimerite;
    private final WorldGenMinable sunstone;

    //flowers
    private final AM2FlowerGen blueOrchid;
    private final AM2FlowerGen desertNova;
    private final AM2FlowerGen wakebloom;
    private final AM2FlowerGen aum;
    private final AM2FlowerGen tarmaRoot;

    private final ArrayList<Integer> dimensionBlacklist = new ArrayList<Integer>();

    //trees
    private final WitchwoodTreeHuge witchwoodTree;

    //pools
    private final AM2PoolGen pools;

    public AM2WorldDecorator() {


        for (int i : ArsMagica.config.getWorldgenBlacklist()) {
            if (i == -1) continue;
            dimensionBlacklist.add(i);
        }

        vinteum = new WorldGenMinable(AMBlocks.vinteum_ore.getDefaultState(), ArsMagica.config.getVinteumVeinSize(), BlockMatcher.forBlock(Blocks.STONE));
        chimerite = new WorldGenMinable(AMBlocks.chimerite_ore.getDefaultState(), ArsMagica.config.getChimeriteVeinSize(), BlockMatcher.forBlock(Blocks.STONE));
        blueTopaz = new WorldGenMinable(AMBlocks.blue_topaz_ore.getDefaultState(), ArsMagica.config.getBlueTopazVeinSize(), BlockMatcher.forBlock(Blocks.STONE));
        sunstone = new WorldGenMinable(AMBlocks.sunstone_ore.getDefaultState(), ArsMagica.config.getSunstoneVeinSize(), BlockMatcher.forBlock(Blocks.LAVA));

        blueOrchid = new AM2FlowerGen(AMBlocks.cerublossom);
        desertNova = new AM2FlowerGen(AMBlocks.desert_nova);
        wakebloom = new AM2FlowerGen(AMBlocks.wakebloom, 24);
        aum = new AM2FlowerGen(AMBlocks.aum);
        tarmaRoot = new AM2FlowerGen(AMBlocks.tarma_root);

        witchwoodTree = new WitchwoodTreeHuge(true);

        pools = new AM2PoolGen();

        new WorldGenEssenceLakes(AMBlocks.liquid_essence.getBlock());
    }


    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {

        if (!SpawnBlacklists.worldgenCanHappenInDimension(world.provider.getDimension()))
            return;

        if (world.getWorldInfo().getTerrainType() == WorldType.FLAT) return;
        if (dimensionBlacklist.contains(world.provider.getDimension())) return;
        switch (world.provider.getDimension()) {
            case -1:
                generateNether(random, chunkX, chunkZ, world, chunkGenerator, chunkProvider);
                break;
            case 1:
                break;
            default:
                generateOverworld(random, chunkX, chunkZ, world, chunkGenerator, chunkProvider);
        }
    }

    public void generateNether(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator,
                               IChunkProvider chunkProvider) {
        generateOre(sunstone, ArsMagica.config.getSunstoneFrequency(), world, random, ArsMagica.config.getSunstoneMinHeight(), ArsMagica.config.getSunstoneMaxHeight(), chunkX, chunkZ);
    }

    public void generateOverworld(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        generateOre(vinteum, ArsMagica.config.getVinteumFrequency(), world, random, ArsMagica.config.getVinteumMinHeight(), ArsMagica.config.getVinteumMaxHeight(), chunkX, chunkZ);
        generateOre(chimerite, ArsMagica.config.getChimeriteFrequency(), world, random, ArsMagica.config.getChimeriteMinHeight(), ArsMagica.config.getChimeriteMaxHeight(), chunkX, chunkZ);
        generateOre(blueTopaz, ArsMagica.config.getBlueTopazFrequency(), world, random, ArsMagica.config.getBlueTopazMinHeight(), ArsMagica.config.getBlueTopazMaxHeight(), chunkX, chunkZ);
        generateOre(sunstone, ArsMagica.config.getSunstoneFrequency(), world, random, ArsMagica.config.getSunstoneMinHeight(), ArsMagica.config.getSunstoneMaxHeight(), chunkX, chunkZ);

        if (random.nextInt(ArsMagica.config.getCeruBlossomFrequency()) == 0) {
            generateFlowers(blueOrchid, world, random, chunkX, chunkZ);
        }
        if (random.nextInt(ArsMagica.config.getTarmaRootFrequency()) == 0) {
            generateFlowers(tarmaRoot, world, random, chunkX, chunkZ);
        }

        Biome biome = world.getBiome(new BlockPos(chunkX << 4, 0, chunkZ << 4));
        Type[] biomeTypes = BiomeDictionary.getTypes(biome).toArray(new Type[0]);
        boolean typeValid = false;
        for (Type type : biomeTypes) {
            if (type == Type.BEACH || type == Type.SWAMP || type == Type.JUNGLE || type == Type.PLAINS || type == Type.WATER) {
                typeValid = true;
            } else if (type == Type.SNOWY) {
                typeValid = false;
                break;
            }
        }

        // Desert Nova only spawns in desert biomes
        if ((BiomeDictionary.hasType(biome, Type.SANDY) || BiomeDictionary.hasType(biome, Type.DRY)) && random.nextInt(ArsMagica.config.getDesertNovaFrequency()) == 0) {
            generateFlowers(desertNova, world, random, chunkX, chunkZ);
        }

        if (biome != Biome.REGISTRY.getObject(new ResourceLocation("minecraft:ocean")) && typeValid && random.nextInt(ArsMagica.config.getWakebloomFrequency()) == 0) {
            generateFlowers(wakebloom, world, random, chunkX, chunkZ);
        }

        if (random.nextInt(ArsMagica.config.getWitchwoodFrequency()) == 0) {
            generateTree(witchwoodTree, world, random, chunkX, chunkZ);
        }

        if (random.nextInt(ArsMagica.config.getEssencePuddleFrequency()) == 0) {
            generatePools(world, random, chunkX, chunkZ);
        }

        // Extra pool rolls for the Witchwood biome.
        if (biome == BiomeWitchwoodForest.instance) {
            for (int i = 0; i < 3; i++) {
                if (random.nextInt(Math.max(1, ArsMagica.config.getEssencePuddleFrequency() / 4)) == 0) {
                    generatePools(world, random, chunkX, chunkZ);
                }
            }
        }

        // Witchwood gets a dedicated Cerublossom roll so it reliably appears in this biome.
        if (biome == BiomeWitchwoodForest.instance && random.nextInt(3) == 0) {
            generateFlowers(blueOrchid, world, random, chunkX, chunkZ);
        }

        // Keep Aum present in Witchwood, but slightly rarer.
        if (biome == BiomeWitchwoodForest.instance && random.nextInt(6) == 0) {
            int ax = (chunkX << 4) + random.nextInt(16) + 8;
            int az = (chunkZ << 4) + random.nextInt(16) + 8;
            aum.generate(world, random, world.getHeight(new BlockPos(ax, 0, az)));
        }

        if ((BiomeDictionary.hasType(biome, Type.MAGICAL) || BiomeDictionary.hasType(biome, Type.FOREST)) && random.nextInt(ArsMagica.config.getEssenceLakeFrequency()) == 0 && TerrainGen.populate(chunkGenerator, world, random, chunkX, chunkZ, true, LAKE)) {
            int lakeGenX = (chunkX * 16) + random.nextInt(16) + 8;
            int lakeGenY = random.nextInt(128);
            int lakeGenZ = (chunkZ * 16) + random.nextInt(16) + 8;
            (new WorldGenEssenceLakes(AMBlocks.liquid_essence.getBlock())).generate(world, random, new BlockPos(lakeGenX, lakeGenY, lakeGenZ));
        }

        generateGatewayRuins(world, random, chunkX, chunkZ);
        generateLostArchives(world, random, chunkX, chunkZ);
    }

    private void generateGatewayRuins(World world, Random random, int chunkX, int chunkZ) {
        if (!ArsMagica.config.getGatewayRuinEnabled()) return;
        if (random.nextInt(ArsMagica.config.getGatewayRuinFrequency()) != 0) return;

        Biome biome = world.getBiome(new BlockPos(chunkX << 4, 0, chunkZ << 4));

        // Blacklist check
        String[] blacklist = ArsMagica.config.getGatewayRuinBiomeBlacklist();
        if (blacklist != null) {
            for (String typeName : blacklist) {
                try {
                    Type type = Type.getType(typeName.trim());
                    if (BiomeDictionary.hasType(biome, type)) return;
                } catch (Exception e) {
                    ArsMagica.LOGGER.error("Invalid biome type in config: {}", typeName, e);
                }
            }
        }

        // Whitelist check (empty = all allowed)
        String[] whitelist = ArsMagica.config.getGatewayRuinBiomes();
        if (whitelist != null && whitelist.length > 0) {
            boolean found = false;
            for (String typeName : whitelist) {
                try {
                    Type type = Type.getType(typeName.trim());
                    if (BiomeDictionary.hasType(biome, type)) {
                        found = true;
                        break;
                    }
                } catch (Exception e) {
                    ArsMagica.LOGGER.error("Invalid biome type in config: {}", typeName, e);
                }
            }
            if (!found) return;
        }

        int x = (chunkX * 16) + random.nextInt(16) + 8;
        int z = (chunkZ * 16) + random.nextInt(16) + 8;
        new WorldGenGateway().generate(world, random, new BlockPos(x, 0, z));
    }

    private void generateLostArchives(World world, Random random, int chunkX, int chunkZ) {
        if (!ArsMagica.config.getLostArchiveEnabled()) return;
        if (random.nextInt(ArsMagica.config.getLostArchiveFrequency()) != 0) return;

        Biome biome = world.getBiome(new BlockPos(chunkX << 4, 0, chunkZ << 4));

        // Blacklist check
        String[] blacklist = ArsMagica.config.getLostArchiveBiomeBlacklist();
        if (blacklist != null) {
            for (String typeName : blacklist) {
                try {
                    Type type = Type.getType(typeName.trim());
                    if (BiomeDictionary.hasType(biome, type)) return;
                } catch (Exception e) {
                    ArsMagica.LOGGER.error("Invalid biome type in config: {}", typeName, e);
                }
            }
        }

        int x = (chunkX * 16) + random.nextInt(16) + 8;
        int z = (chunkZ * 16) + random.nextInt(16) + 8;
        new WorldGenLostArchive().generate(world, random, new BlockPos(x, 0, z));
    }

    private void generateFlowers(AM2FlowerGen flowers, World world, Random random, int chunkX, int chunkZ) {
        if (random.nextDouble() > 0.75)
            return;
        int x = (chunkX << 4) + random.nextInt(16) + 8;
        int y = random.nextInt(128);
        int z = (chunkZ << 4) + random.nextInt(16) + 8;

        flowers.generate(world, random, new BlockPos(x, y, z));
    }

    private void generateOre(WorldGenMinable mineable, int amount, World world, Random random, int minY, int maxY, int chunkX, int chunkZ) {
        for (int i = 0; i < amount; ++i) {
            int x = (chunkX << 4) + random.nextInt(16);
            int y = random.nextInt(maxY - minY) + minY;
            int z = (chunkZ << 4) + random.nextInt(16);

            mineable.generate(world, random, new BlockPos(x, y, z));
        }
    }

    private void generateTree(WorldGenerator trees, World world, Random random, int chunkX, int chunkZ) {
        int x = (chunkX * 16) + random.nextInt(16) + 8;
        int z = (chunkZ * 16) + random.nextInt(16) + 8;
        BlockPos y = world.getHeight(new BlockPos(x, 0, z));

        if (new WitchwoodTreeHuge(true).generate(world, random, y)) {
            aum.generate(world, random, y);
        }
    }

    private void generatePools(World world, Random random, int chunkX, int chunkZ) {
        int x = (chunkX * 16) + random.nextInt(16) + 8;
        int z = (chunkZ * 16) + random.nextInt(16) + 8;
        BlockPos y = world.getHeight(new BlockPos(x, 0, z));

        pools.generate(world, random, y);
    }
}

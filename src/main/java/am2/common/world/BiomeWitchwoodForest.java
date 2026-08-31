package am2.common.world;

import am2.ArsMagica;
import am2.common.entity.EntityDryad;
import am2.common.entity.EntityManaCreeper;
import am2.common.entity.EntityManaElemental;
import am2.common.registry.AMBlocks;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeDecorator;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import net.minecraft.world.gen.feature.WorldGenBlockBlob;
import net.minecraft.world.gen.feature.WorldGenTallGrass;
import net.minecraft.world.gen.feature.WorldGenerator;

import java.util.Random;

public class BiomeWitchwoodForest extends Biome {

    public static final Biome instance = new BiomeWitchwoodForest(new BiomeProperties("WitchwoodForest")
            .setBaseHeight(0.0f)
            .setHeightVariation(0.08f));
    private static final WitchwoodTreeHuge hugeTree = new WitchwoodTreeHuge(true);
    private static final WitchwoodTreeSmall smallTree = new WitchwoodTreeSmall(true);
    private static final WorldGenFairyRing fairyRingGen = new WorldGenFairyRing();
    private static final WorldGenSurfaceEssencePond surfaceEssencePondGen = new WorldGenSurfaceEssencePond();
    private static final WorldGenRuins ruinsGen = new WorldGenRuins();
    private static final WorldGenFallenLog fallenLogGen = new WorldGenFallenLog();
    private static final WorldGenBlockBlob mossyBoulderGenSmall = new WorldGenBlockBlob(Blocks.MOSSY_COBBLESTONE, 1);
    private static final WorldGenBlockBlob mossyBoulderGenMedium = new WorldGenBlockBlob(Blocks.MOSSY_COBBLESTONE, 2);
    private static int biomeId;
    private static BiomeDecorator decorator;
    
    private static Block crystalFlowerBlock = null;
    private static boolean crystalFlowerSearched = false;

    private Block getCrystalFlower() {
        if (!crystalFlowerSearched) {
            crystalFlowerBlock = Block.REGISTRY.getObject(new ResourceLocation("ebwizardry", "crystal_flower"));
            if (crystalFlowerBlock == Blocks.AIR) {
                crystalFlowerBlock = null;
            }
            crystalFlowerSearched = true;
        }
        return crystalFlowerBlock;
    }

    public BiomeWitchwoodForest(BiomeProperties par1) {
        super(par1);
        this.spawnableCreatureList.add(new SpawnListEntry(EntityWolf.class, 5, 4, 4));
        this.spawnableCreatureList.add(new SpawnListEntry(EntityDryad.class, 5, 4, 4));
        this.spawnableCreatureList.add(new SpawnListEntry(EntityManaElemental.class, 4, 1, 2));
        this.spawnableCreatureList.add(new SpawnListEntry(EntityManaCreeper.class, 2, 1, 1));
        decorator = this.createBiomeDecorator();
        decorator.treesPerChunk = 10;
        decorator.grassPerChunk = 4;
        decorator.flowersPerChunk = 2;
        decorator.deadBushPerChunk = 1;
    }

    @Override
    public int getWaterColorMultiplier() {
        return 0x0a2a72;
    }

    @Override
    public int getFoliageColorAtPos(BlockPos pos) {
        return 0xdbe6e5;
    }

    @Override
    public int getGrassColorAtPos(BlockPos pos) {
        return 0xc2b8a8;
    }

    @Override
    public int getSkyColorByTemp(float par1) {
        return 0x381078;
    }

    @Override
    public WorldGenerator getRandomWorldGenForGrass(Random rand) {
        return rand.nextInt(4) == 0 ? new WorldGenTallGrass(BlockTallGrass.EnumType.FERN) : new WorldGenTallGrass(BlockTallGrass.EnumType.GRASS);
    }

    @Override
    public void decorate(World worldIn, Random rand, BlockPos pos) {
        DOUBLE_PLANT_GENERATOR.setPlantType(BlockDoublePlant.EnumPlantType.FERN);
        for (int i = 0; i < rand.nextInt(5); ++i) {
            int x = rand.nextInt(16) + 8;
            int z = rand.nextInt(16) + 8;
            BlockPos blockpos = worldIn.getHeight(pos.add(x, 0, z));
            DOUBLE_PLANT_GENERATOR.generate(worldIn, rand, blockpos);
        }

        this.decorator.decorate(worldIn, rand, this, pos);

        // Electroblob's Wizardry Crystal Flowers
        Block crystalFlower = getCrystalFlower();
        if (crystalFlower != null && crystalFlower instanceof BlockBush && rand.nextInt(4) == 0) {
            int x = rand.nextInt(16) + 8;
            int z = rand.nextInt(16) + 8;
            BlockPos clusterPos = worldIn.getHeight(pos.add(x, 0, z));

            for (int j = 0; j < 6; ++j) {
                BlockPos p = clusterPos.add(rand.nextInt(8) - rand.nextInt(8), rand.nextInt(4) - rand.nextInt(4), rand.nextInt(8) - rand.nextInt(8));
                if (worldIn.isAirBlock(p) && p.getY() < 255 && ((BlockBush)crystalFlower).canBlockStay(worldIn, p, crystalFlower.getDefaultState())) {
                    worldIn.setBlockState(p, crystalFlower.getDefaultState(), 2);
                }
            }
        }

        // ~1-in-3 chance to generate a fairy ring in each chunk
        if (rand.nextInt(3) == 0) {
            int x = pos.getX() + rand.nextInt(16) + 8;
            int z = pos.getZ() + rand.nextInt(16) + 8;
            fairyRingGen.generate(worldIn, rand, new BlockPos(x, 0, z));
        }

        // ~1-in-5 chance to generate a larger surface essence pond in each chunk
        if (rand.nextInt(5) == 0) {
            int x = pos.getX() + rand.nextInt(16) + 8;
            int z = pos.getZ() + rand.nextInt(16) + 8;
            surfaceEssencePondGen.generate(worldIn, rand, new BlockPos(x, 0, z));
        }

        // ~1-in-4 chance to generate ruins in each chunk
        if (rand.nextInt(4) == 0) {
            int x = pos.getX() + rand.nextInt(16) + 8;
            int z = pos.getZ() + rand.nextInt(16) + 8;
            ruinsGen.generate(worldIn, rand, new BlockPos(x, 0, z));
        }

        // Mossy boulders: small, sparse clusters similar to Mega Taiga stones.
        if (rand.nextInt(3) == 0) {
            int x = pos.getX() + rand.nextInt(16) + 8;
            int z = pos.getZ() + rand.nextInt(16) + 8;
            BlockPos boulderPos = worldIn.getHeight(new BlockPos(x, 0, z));
            (rand.nextInt(8) == 0 ? mossyBoulderGenMedium : mossyBoulderGenSmall).generate(worldIn, rand, boulderPos);
        }

        // Fallen Logs (~1-in-2 chance per chunk)
        if (rand.nextInt(2) == 0) {
            int x = pos.getX() + rand.nextInt(16) + 8;
            int z = pos.getZ() + rand.nextInt(16) + 8;
            fallenLogGen.generate(worldIn, rand, new BlockPos(x, 0, z));
        }

        // A small number of podzol patches on top of the coarse-dirt-heavy surface.
        int podzolPatchCount = 1 + rand.nextInt(2);
        for (int i = 0; i < podzolPatchCount; i++) {
            int x = pos.getX() + rand.nextInt(16) + 8;
            int z = pos.getZ() + rand.nextInt(16) + 8;
            generatePodzolPatch(worldIn, rand, new BlockPos(x, 0, z));
        }

        // Canopy ambience: hanging vines plus occasional cobwebs.
        decorateCanopyAmbience(worldIn, rand, pos);
    }

    private void generatePodzolPatch(World world, Random rand, BlockPos origin) {
        int radius = 1 + rand.nextInt(2);
        int radiusSq = radius * radius;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx * dx + dz * dz > radiusSq) continue;
                if (rand.nextInt(3) == 0) continue;

                BlockPos top = world.getHeight(origin.add(dx, 0, dz)).down();
                IBlockState state = world.getBlockState(top);

                if (state.getBlock() == Blocks.GRASS) {
                    world.setBlockState(top, Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.PODZOL), 2);
                    continue;
                }

                if (state.getBlock() == Blocks.DIRT) {
                    BlockDirt.DirtType dirtType = state.getValue(BlockDirt.VARIANT);
                    if (dirtType == BlockDirt.DirtType.DIRT || dirtType == BlockDirt.DirtType.COARSE_DIRT) {
                        world.setBlockState(top, Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.PODZOL), 2);
                    }
                }
            }
        }
    }

    private void decorateCanopyAmbience(World world, Random rand, BlockPos chunkPos) {
        int attempts = 14;
        for (int i = 0; i < attempts; i++) {
            int x = chunkPos.getX() + rand.nextInt(16) + 8;
            int z = chunkPos.getZ() + rand.nextInt(16) + 8;
            BlockPos canopy = findCanopyLeaf(world, new BlockPos(x, 0, z));
            if (canopy == null) continue;

            // Cobwebs are rare and only appear on selected "spider trees".
            if (rand.nextInt(12) == 0) {
                tryPlaceCanopyWeb(world, rand, canopy);
            } else {
                tryPlaceHangingVine(world, rand, canopy);
            }
        }
    }

    private BlockPos findCanopyLeaf(World world, BlockPos base) {
        BlockPos top = world.getHeight(base);
        int maxY = Math.min(255, top.getY() + 18);
        int minY = Math.max(40, top.getY() - 2);

        for (int y = maxY; y >= minY; y--) {
            BlockPos check = new BlockPos(base.getX(), y, base.getZ());
            Material material = world.getBlockState(check).getMaterial();
            if (material == Material.LEAVES) {
                return check;
            }
        }
        return null;
    }

    private void tryPlaceCanopyWeb(World world, Random rand, BlockPos canopyLeaf) {
        BlockPos trunkPos = findWebTreeTrunk(world, canopyLeaf);
        if (trunkPos == null) return;
        if (canopyLeaf.getY() - trunkPos.getY() < 10) return;
        if (!isSpiderTree(trunkPos)) return;
        if (rand.nextInt(3) != 0) return;

        BlockPos webPos = canopyLeaf.down();
        if (world.isAirBlock(webPos) && world.isAirBlock(webPos.down())
                && world.getBlockState(webPos.down()).getBlock() != AMBlocks.witchwood_log) {
            world.setBlockState(webPos, Blocks.WEB.getDefaultState(), 2);
        }
    }

    private BlockPos findWebTreeTrunk(World world, BlockPos canopyLeaf) {
        int minY = Math.max(1, canopyLeaf.getY() - 18);
        for (int y = canopyLeaf.getY(); y >= minY; y--) {
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    BlockPos check = new BlockPos(canopyLeaf.getX() + dx, y, canopyLeaf.getZ() + dz);
                    if (world.getBlockState(check).getBlock() == AMBlocks.witchwood_log) {
                        return check;
                    }
                }
            }
        }
        return null;
    }

    private boolean isSpiderTree(BlockPos trunkPos) {
        int hash = (trunkPos.getX() * 73428767) ^ (trunkPos.getZ() * 912931);
        return (hash & 7) == 0;
    }

    private void tryPlaceHangingVine(World world, Random rand, BlockPos canopyLeaf) {
        EnumFacing side = EnumFacing.Plane.HORIZONTAL.random(rand);
        BlockPos vineStart = canopyLeaf.offset(side);
        if (!world.isAirBlock(vineStart)) return;

        IBlockState vineState = vineStateAttachedTo(side.getOpposite());
        world.setBlockState(vineStart, vineState, 2);

        int length = 1 + rand.nextInt(4);
        BlockPos cursor = vineStart.down();
        for (int i = 0; i < length && cursor.getY() > 1; i++) {
            if (!world.isAirBlock(cursor)) break;
            world.setBlockState(cursor, vineState, 2);
            cursor = cursor.down();
        }
    }

    private IBlockState vineStateAttachedTo(EnumFacing supportSide) {
        IBlockState state = Blocks.VINE.getDefaultState();
        switch (supportSide) {
            case NORTH:
                return state.withProperty(BlockVine.NORTH, true);
            case SOUTH:
                return state.withProperty(BlockVine.SOUTH, true);
            case EAST:
                return state.withProperty(BlockVine.EAST, true);
            case WEST:
                return state.withProperty(BlockVine.WEST, true);
            default:
                return state;
        }
    }


    public static int getBiomeId() {
        if (biomeId == 0 && ArsMagica.config != null) {
            biomeId = ArsMagica.config.getWitchwoodForestID();
        }
        return biomeId;
    }

    @Override
    public void genTerrainBlocks(World worldIn, Random rand, ChunkPrimer chunkPrimerIn, int x, int z, double noiseVal) {
        float f = rand.nextFloat();
        if (f < 0.3f) {
            this.topBlock = Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.COARSE_DIRT);
        } else if (f < 0.38f) { // Light podzol baseline; additional podzol comes from decorate patches.
            this.topBlock = Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.PODZOL);
        } else {
            this.topBlock = Blocks.GRASS.getDefaultState();
        }
        super.genTerrainBlocks(worldIn, rand, chunkPrimerIn, x, z, noiseVal);
    }

    public static int getNextFreeBiomeId() {
        for (int i = 0; i < 256; i++) {
            if (Biome.getBiome(i) != null) {
                if (i == 255) throw new IllegalArgumentException("No more biome ids are avaliable");
                continue;
            }
            return i;
        }
        return -1;
    }

    @Override
    public WorldGenAbstractTree getRandomTreeFeature(Random rand) {
        return rand.nextInt(10) == 0 ? hugeTree : smallTree;
    }

}

package am2.common.world;

import am2.ArsMagica;
import am2.common.LogHelper;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockStoneBrick;
import net.minecraft.block.BlockVine;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntityLockableLoot;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.common.BiomeDictionary;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class WorldGenGateway extends WorldGenerator {

    private static final ResourceLocation LOOT_TABLE = new ResourceLocation("arsmagica2", "chests/gateway_ruin");

    private static final IBlockState STONE_BRICK = Blocks.STONEBRICK.getDefaultState();
    private static final IBlockState CRACKED = Blocks.STONEBRICK.getDefaultState().withProperty(BlockStoneBrick.VARIANT, BlockStoneBrick.EnumType.CRACKED);
    private static final IBlockState MOSSY = Blocks.STONEBRICK.getDefaultState().withProperty(BlockStoneBrick.VARIANT, BlockStoneBrick.EnumType.MOSSY);
    private static final IBlockState CHISELED = Blocks.STONEBRICK.getDefaultState().withProperty(BlockStoneBrick.VARIANT, BlockStoneBrick.EnumType.CHISELED);

    @Override
    public boolean generate(World world, Random rand, BlockPos pos) {
        boolean alongZ = rand.nextBoolean();

        BlockPos surfacePos = world.getTopSolidOrLiquidBlock(pos);

        if (surfacePos.getY() < 10 || surfacePos.getY() > 240) return false;
        IBlockState groundState = world.getBlockState(surfacePos.down());
        if (!groundState.getMaterial().isSolid() || groundState.getMaterial().isLiquid()) return false;

        // Check flatness at the structure's extent
        int centerY = surfacePos.getY();
        for (int d = -2; d <= 2; d += 4) {
            BlockPos corner;
            if (alongZ) {
                corner = world.getTopSolidOrLiquidBlock(pos.add(0, 0, d));
            } else {
                corner = world.getTopSolidOrLiquidBlock(pos.add(d, 0, 0));
            }
            if (Math.abs(corner.getY() - centerY) > 2) return false;
        }

        // Base sits on the ground surface
        int baseY = surfacePos.getY() - 1;

        Biome biome = world.getBiome(surfacePos);
        boolean isDesert = BiomeDictionary.hasType(biome, BiomeDictionary.Type.SANDY);
        boolean hasVegetation = !isDesert && !BiomeDictionary.hasType(biome, BiomeDictionary.Type.DEAD)
                && !BiomeDictionary.hasType(biome, BiomeDictionary.Type.SNOWY);

        List<BlockPlacement> placements = new ArrayList<>();
        int cx = pos.getX();
        int cz = pos.getZ();

        if (alongZ) {
            buildPrimaryStructure(placements, baseY, cx, cz);
        } else {
            buildSecondaryStructure(placements, baseY, cx, cz);
        }

        // Capture original structure positions before ruining
        HashSet<BlockPos> originalPositions = new HashSet<>();
        for (BlockPlacement bp : placements) {
            originalPositions.add(bp.pos);
        }

        // Apply weathering
        for (BlockPlacement bp : placements) {
            if (bp.state.getBlock() == Blocks.STONEBRICK
                    && bp.state.getValue(BlockStoneBrick.VARIANT) == BlockStoneBrick.EnumType.DEFAULT) {
                float r = rand.nextFloat();
                if (r < 0.25f) {
                    bp.state = CRACKED;
                } else if (r < 0.45f && !isDesert) {
                    bp.state = MOSSY;
                }
            }
        }

        // Ruin: remove blocks top-down so upper blocks are removed before lower ones,
        // preventing floating parts. Reduced degradation rates.
        List<BlockPlacement> debris = new ArrayList<>();
        HashSet<BlockPos> removed = new HashSet<>();
        placements.sort((a, b) -> Integer.compare(b.pos.getY(), a.pos.getY()));

        for (int i = placements.size() - 1; i >= 0; i--) {
            BlockPlacement bp = placements.get(i);

            // If the block above was part of the structure and still exists, don't remove this support
            BlockPos above = bp.pos.up();
            if (originalPositions.contains(above) && !removed.contains(above)) {
                continue;
            }

            float removalChance = bp.isBase ? 0.10f : 0.30f;
            if (rand.nextFloat() < removalChance) {
                if (!bp.isBase && rand.nextFloat() < 0.5f) {
                    int ox = (rand.nextInt(3) + 1) * (rand.nextBoolean() ? 1 : -1);
                    int oz = (rand.nextInt(3) + 1) * (rand.nextBoolean() ? 1 : -1);
                    BlockPos debrisTarget = new BlockPos(bp.pos.getX() + ox, 0, bp.pos.getZ() + oz);
                    if (!world.isBlockLoaded(debrisTarget)) continue;
                    BlockPos debrisCheck = world.getTopSolidOrLiquidBlock(debrisTarget);
                    if (debrisCheck.getY() > 5) {
                        IBlockState below = world.getBlockState(debrisCheck.down());
                        if (below.getMaterial().isSolid() && !below.getMaterial().isLiquid()
                                && world.isAirBlock(debrisCheck)) {
                            IBlockState ds = STONE_BRICK;
                            float dw = rand.nextFloat();
                            if (dw < 0.3f) ds = CRACKED;
                            else if (dw < 0.5f && !isDesert) ds = MOSSY;
                            debris.add(new BlockPlacement(debrisCheck, ds, false));
                        }
                    }
                }
                removed.add(bp.pos);
                placements.remove(i);
            }
        }

        // Place structure blocks
        for (BlockPlacement bp : placements) {
            world.setBlockState(bp.pos, bp.state, 2);
        }

        // Place debris
        for (BlockPlacement bp : debris) {
            world.setBlockState(bp.pos, bp.state, 2);
        }

        // Place chest near the base, randomized offset and optionally buried
        if (ArsMagica.config.getGatewayRuinChest()) {
            int offX = rand.nextInt(3) - 1; // -1, 0, or 1
            int offZ = rand.nextInt(3) - 1;
            // Avoid placing exactly at the chiseled center
            if (offX == 0 && offZ == 0) offX = rand.nextBoolean() ? 1 : -1;
            int yOffset = rand.nextBoolean() ? 0 : -1; // surface or 1 block buried
            BlockPos chestPos = new BlockPos(cx + offX, baseY + yOffset, cz + offZ);
            world.setBlockState(chestPos, Blocks.CHEST.getDefaultState(), 2);
            // When buried, cover with dirt so the chest is hidden
            if (yOffset == -1) {
                world.setBlockState(chestPos.up(), Blocks.DIRT.getDefaultState(), 2);
            }
            TileEntityLockableLoot te = (TileEntityLockableLoot) world.getTileEntity(chestPos);
            if (te != null) {
                te.setLootTable(LOOT_TABLE, rand.nextLong());
            }
        }

        // Vines in vegetated biomes
        if (hasVegetation) {
            for (BlockPlacement bp : placements) {
                if (rand.nextFloat() < 0.3f) {
                    tryPlaceVine(world, bp.pos, rand);
                }
            }
        }

        LogHelper.info("Gateway ruin generated at [%d, %d, %d]", cx, baseY, cz);
        return true;
    }

    /**
     * Build the primary (Z-axis) gateway arch.
     * Derived from TileEntityKeystoneReceptacle multiblock definition.
     * Receptacle position (cx, baseY+4, cz) is left empty.
     */
    private void buildPrimaryStructure(List<BlockPlacement> list, int baseY, int cx, int cz) {
        // Base row (multiblock Y=-4)
        list.add(new BlockPlacement(new BlockPos(cx, baseY, cz - 2), STONE_BRICK, true));
        list.add(new BlockPlacement(new BlockPos(cx, baseY, cz - 1), STONE_BRICK, true));
        list.add(new BlockPlacement(new BlockPos(cx, baseY, cz),     CHISELED,    true));
        list.add(new BlockPlacement(new BlockPos(cx, baseY, cz + 1), STONE_BRICK, true));
        list.add(new BlockPlacement(new BlockPos(cx, baseY, cz + 2), STONE_BRICK, true));

        // Y=-3: pillars + inner stairs
        list.add(new BlockPlacement(new BlockPos(cx, baseY + 1, cz - 2), STONE_BRICK, false));
        list.add(new BlockPlacement(new BlockPos(cx, baseY + 1, cz + 2), STONE_BRICK, false));
        list.add(new BlockPlacement(new BlockPos(cx, baseY + 1, cz - 1), stairs(EnumFacing.NORTH, false), false));
        list.add(new BlockPlacement(new BlockPos(cx, baseY + 1, cz + 1), stairs(EnumFacing.SOUTH, false), false));

        // Y=-2: pillars only
        list.add(new BlockPlacement(new BlockPos(cx, baseY + 2, cz - 2), STONE_BRICK, false));
        list.add(new BlockPlacement(new BlockPos(cx, baseY + 2, cz + 2), STONE_BRICK, false));

        // Y=-1: top-half stairs inner + bottom stairs outer
        list.add(new BlockPlacement(new BlockPos(cx, baseY + 3, cz - 1), stairs(EnumFacing.NORTH, true),  false));
        list.add(new BlockPlacement(new BlockPos(cx, baseY + 3, cz - 2), stairs(EnumFacing.SOUTH, false), false));
        list.add(new BlockPlacement(new BlockPos(cx, baseY + 3, cz + 1), stairs(EnumFacing.SOUTH, true),  false));
        list.add(new BlockPlacement(new BlockPos(cx, baseY + 3, cz + 2), stairs(EnumFacing.NORTH, false), false));

        // Y=0: arch top - stairs flanking center
        list.add(new BlockPlacement(new BlockPos(cx, baseY + 4, cz - 1), stairs(EnumFacing.SOUTH, false), false));
        list.add(new BlockPlacement(new BlockPos(cx, baseY + 4, cz + 1), stairs(EnumFacing.NORTH, false), false));
        // Keystone Receptacle at (cx, baseY+4, cz) intentionally LEFT EMPTY
    }

    /**
     * Build the secondary (X-axis) gateway arch.
     * Same layout as primary but rotated 90 degrees.
     */
    private void buildSecondaryStructure(List<BlockPlacement> list, int baseY, int cx, int cz) {
        // Base row
        list.add(new BlockPlacement(new BlockPos(cx - 2, baseY, cz), STONE_BRICK, true));
        list.add(new BlockPlacement(new BlockPos(cx - 1, baseY, cz), STONE_BRICK, true));
        list.add(new BlockPlacement(new BlockPos(cx,     baseY, cz), CHISELED,    true));
        list.add(new BlockPlacement(new BlockPos(cx + 1, baseY, cz), STONE_BRICK, true));
        list.add(new BlockPlacement(new BlockPos(cx + 2, baseY, cz), STONE_BRICK, true));

        // Y=-3: pillars + inner stairs
        list.add(new BlockPlacement(new BlockPos(cx - 2, baseY + 1, cz), STONE_BRICK, false));
        list.add(new BlockPlacement(new BlockPos(cx + 2, baseY + 1, cz), STONE_BRICK, false));
        list.add(new BlockPlacement(new BlockPos(cx - 1, baseY + 1, cz), stairs(EnumFacing.EAST, false), false));
        list.add(new BlockPlacement(new BlockPos(cx + 1, baseY + 1, cz), stairs(EnumFacing.WEST, false), false));

        // Y=-2: pillars only
        list.add(new BlockPlacement(new BlockPos(cx - 2, baseY + 2, cz), STONE_BRICK, false));
        list.add(new BlockPlacement(new BlockPos(cx + 2, baseY + 2, cz), STONE_BRICK, false));

        // Y=-1: top-half stairs inner + bottom stairs outer
        list.add(new BlockPlacement(new BlockPos(cx - 1, baseY + 3, cz), stairs(EnumFacing.WEST, true),  false));
        list.add(new BlockPlacement(new BlockPos(cx - 2, baseY + 3, cz), stairs(EnumFacing.EAST, false), false));
        list.add(new BlockPlacement(new BlockPos(cx + 1, baseY + 3, cz), stairs(EnumFacing.EAST, true),  false));
        list.add(new BlockPlacement(new BlockPos(cx + 2, baseY + 3, cz), stairs(EnumFacing.WEST, false), false));

        // Y=0: arch top
        list.add(new BlockPlacement(new BlockPos(cx - 1, baseY + 4, cz), stairs(EnumFacing.EAST, false), false));
        list.add(new BlockPlacement(new BlockPos(cx + 1, baseY + 4, cz), stairs(EnumFacing.WEST, false), false));
        // Keystone Receptacle at (cx, baseY+4, cz) intentionally LEFT EMPTY
    }

    private static IBlockState stairs(EnumFacing facing, boolean top) {
        IBlockState state = Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, facing);
        if (top) {
            state = state.withProperty(BlockStairs.HALF, BlockStairs.EnumHalf.TOP);
        }
        return state;
    }

    private void tryPlaceVine(World world, BlockPos structureBlock, Random rand) {
        EnumFacing[] horizontals = {EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.EAST, EnumFacing.WEST};
        EnumFacing face = horizontals[rand.nextInt(4)];
        BlockPos vinePos = structureBlock.offset(face);
        if (world.isAirBlock(vinePos)) {
            IBlockState vineState = Blocks.VINE.getDefaultState()
                    .withProperty(BlockVine.getPropertyFor(face.getOpposite()), true);
            world.setBlockState(vinePos, vineState, 2);
        }
    }

    private static class BlockPlacement {
        BlockPos pos;
        IBlockState state;
        boolean isBase;

        BlockPlacement(BlockPos pos, IBlockState state, boolean isBase) {
            this.pos = pos;
            this.state = state;
            this.isBase = isBase;
        }
    }
}

package am2.common.world;

import am2.common.registry.AMBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

import java.util.Random;

/**
 * Generates an irregular surface essence pond in the Witchwood Forest.
 * Carves a shallow bowl into the terrain (1–2 blocks below the rim) and fills it
 * with liquid essence, producing a natural-looking pond larger than the 2×2 AM2PoolGen pools.
 */
public class WorldGenSurfaceEssencePond extends WorldGenerator {

    @Override
    public boolean generate(World world, Random rand, BlockPos origin) {
        BlockPos centerSurface = world.getHeight(origin);
        // pondY = Y of the top solid block at the center position
        int pondY = centerSurface.getY() - 1;

        int radius = 3 + rand.nextInt(3); // 3–5 block radius
        int depth  = 1 + rand.nextInt(2); // 1–2 blocks deep below the removed rim block

        // Flatness check: reject if any point within (radius+1) differs by more than 3 blocks
        for (int dx = -(radius + 1); dx <= radius + 1; dx++) {
            for (int dz = -(radius + 1); dz <= radius + 1; dz++) {
                if (dx * dx + dz * dz > (radius + 1) * (radius + 1)) continue;
                int surfY = world.getHeight(origin.add(dx, 0, dz)).getY() - 1;
                if (Math.abs(surfY - pondY) > 3) return false;
            }
        }

        IBlockState essence = AMBlocks.liquid_essence.getBlock().getDefaultState();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                // Jitter the effective radius per point for a natural irregular edge
                double pointRadius = radius - 0.5 + rand.nextDouble();
                if (dx * dx + dz * dz > pointRadius * pointRadius) continue;

                BlockPos column   = origin.add(dx, 0, dz);
                BlockPos solidTop = world.getHeight(column).down(); // top solid block

                // Only carve into full solid ground blocks
                if (!world.getBlockState(solidTop).isFullBlock()) continue;

                // Remove the top solid block to form the bowl rim (pond surface sits 1 block
                // below the surrounding terrain, creating a visible depression)
                world.setBlockState(solidTop, Blocks.AIR.getDefaultState(), 2);

                // Fill downward with essence
                for (int dy = 1; dy <= depth; dy++) {
                    BlockPos target = solidTop.add(0, -dy, 0);
                    if (!world.getBlockState(target).isFullBlock()) break;
                    world.setBlockState(target, essence, 2);
                }
            }
        }

        return true;
    }
}

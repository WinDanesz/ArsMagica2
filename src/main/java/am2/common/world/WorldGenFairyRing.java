package am2.common.world;

import am2.ArsMagica;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Generates a fairy ring: two concentric circles of mushrooms on the surface.
 * The mushroom blocks are driven by the config key {@code fairy_ring_mushroom_blocks},
 * each entry formatted as {@code modid:registryname:meta}.
 */
public class WorldGenFairyRing extends WorldGenerator {

    /** Step angle in degrees between mushroom positions around each ring (30° = up to 12 per ring). */
    private static final int STEP_DEG = 30;

    /** Lazily-resolved list of blockstates read from config, built once on first use. */
    private List<IBlockState> mushroomStates = null;

    private List<IBlockState> getMushroomStates() {
        if (mushroomStates != null) return mushroomStates;

        mushroomStates = new ArrayList<>();
        for (String entry : ArsMagica.config.getFairyRingMushroomBlocks()) {
            String[] parts = entry.trim().split(":");
            // Expect format  modid:registryname:meta  (3 colon-separated parts)
            if (parts.length < 3) continue;
            // Reassemble registry name (parts[0]:parts[1]) and parse meta from parts[2]
            String registryName = parts[0] + ":" + parts[1];
            int meta;
            try {
                meta = Integer.parseInt(parts[2]);
            } catch (NumberFormatException e) {
                continue;
            }
            Block block = Block.REGISTRY.getObject(new ResourceLocation(registryName));
            if (block == null) continue;
            //noinspection deprecation
            mushroomStates.add(block.getStateFromMeta(meta));
        }
        return mushroomStates;
    }

    @Override
    public boolean generate(World world, Random rand, BlockPos center) {
        List<IBlockState> states = getMushroomStates();
        if (states.isEmpty()) return false;

        BlockPos surfaceCenter = world.getHeight(center);

        int innerRadius = 1 + rand.nextInt(2); // 1–2 blocks
        int outerRadius = innerRadius + 1 + rand.nextInt(2); // 2–3 blocks (max 3 from inner=1+outer=2)

        placeRing(world, rand, surfaceCenter, innerRadius, states);
        placeRing(world, rand, surfaceCenter, outerRadius, states);

        return true;
    }

    private void placeRing(World world, Random rand, BlockPos center, int radius, List<IBlockState> states) {
        for (int angle = 0; angle < 360; angle += STEP_DEG) {
            // Skip a random portion of positions for a natural, gap-filled ring
            if (rand.nextInt(4) == 0) continue;

            double rad = Math.toRadians(angle);
            int dx = (int) Math.round(Math.sin(rad) * radius);
            int dz = (int) Math.round(Math.cos(rad) * radius);

            BlockPos surface = world.getHeight(center.add(dx, 0, dz));
            BlockPos ground = surface.down();

            // world.getHeight() may land on top of a tree; scan downward past any non-soil
            // blocks (leaves, logs, vines, etc.) until we find actual terrain or give up.
            while (ground.getY() > 0) {
                IBlockState gs = world.getBlockState(ground);
                Material mat = gs.getMaterial();
                if (mat == Material.GROUND || mat == Material.GRASS || mat == Material.ROCK
                        || mat == Material.SAND || mat == Material.CLAY) {
                    // ground is real terrain, surface is the air block above it
                    surface = ground.up();
                    break;
                }
                // Non-terrain block (leaf, log, vine, air gap) — step down
                ground = ground.down();
                surface = ground.up();
            }
            if (ground.getY() <= 0) continue;

            // Only place on solid terrain blocks with air above
            IBlockState groundState = world.getBlockState(ground);
            if (!groundState.isOpaqueCube()) continue;
            if (!world.isAirBlock(surface)) continue;

            IBlockState mushroom = states.get(rand.nextInt(states.size()));
            world.setBlockState(surface, mushroom, 2);
        }
    }
}

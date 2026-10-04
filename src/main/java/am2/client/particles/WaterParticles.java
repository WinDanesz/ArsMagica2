package am2.client.particles;

import am2.ArsMagica;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

/**
 * The look of water-affinity spell effects: translucent blue bubbles mixed with tumbling cubes of the vanilla water block.
 * <p>
 * These only create and style the particle. Callers add their own motion, age and controllers, which keeps each shape's
 * behaviour unchanged. A {@code color} of -1 means the spell has no Color modifier, so the water tints are used.
 */
public final class WaterParticles {

    public static final String BUBBLE = "water_drop";
    public static final String CUBE = "water_cube";

    /** Tint of the bubbles, and the paler blue of the foam bubbles. */
    public static final int BUBBLE_COLOR = 0x4FA0FF;
    public static final int FOAM_COLOR = 0xBFE0FF;

    /** Chance that {@link #bubbleOrCube} makes a cube. */
    private static final float CUBE_CHANCE = 0.4f;

    private WaterParticles() {
    }

    /** A translucent water bubble. About a quarter are a paler blue, like foam. {@code scale <= 0} picks a small random size. */
    public static AMParticle bubble(World world, double x, double y, double z, int color, float scale) {
        AMParticle bubble = (AMParticle) ArsMagica.proxy.particleManager.spawn(world, BUBBLE, x, y, z);
        if (bubble == null) return null;

        Random rand = world.rand;
        bubble.setParticleScale(scale > 0 ? scale : 0.05f + rand.nextFloat() * 0.1f);
        bubble.setRGBColorI(color != -1 ? color & 0xFFFFFF : rand.nextInt(4) == 0 ? FOAM_COLOR : BUBBLE_COLOR);
        bubble.SetParticleAlpha(0.9f);
        bubble.setIgnoreMaxAge(false);
        bubble.setMaxAge(14 + rand.nextInt(10));
        return bubble;
    }

    /**
     * A tumbling cube of the animated water block texture, tinted like water in the local biome. {@code scale} is the size a
     * bubble would get; the cube is made smaller so it comes out as a small chip.
     */
    public static AMParticle cube(World world, double x, double y, double z, int color, float scale) {
        AMParticle cube = (AMParticle) ArsMagica.proxy.particleManager.spawn(world, CUBE, x, y, z);
        if (cube == null) return null;

        Random rand = world.rand;
        int tint = color != -1 ? color & 0xFFFFFF : world.getBiome(new BlockPos(x, y, z)).getWaterColorMultiplier() & 0xFFFFFF;
        cube.setTumblingCube(0.35f);
        cube.setParticleScale(scale > 0 ? Math.min(scale * 0.45f, 0.1f) : 0.05f + rand.nextFloat() * 0.05f);
        // A random 4x4 pixel patch of the 16x16 water texture, like vanilla block-break particles
        cube.setTextureSubRegion(rand.nextInt(4) * 0.25f, rand.nextInt(4) * 0.25f, 0.25f);
        cube.setRGBColorI(tint);
        cube.SetParticleAlpha(1.0f);
        cube.setIgnoreMaxAge(false);
        cube.setMaxAge(16 + rand.nextInt(10));
        return cube;
    }

    /** Either a bubble or a cube, for shapes that spawn a single stream of particles. */
    public static AMParticle bubbleOrCube(World world, double x, double y, double z, int color, float scale) {
        return world.rand.nextFloat() < CUBE_CHANCE ? cube(world, x, y, z, color, scale) : bubble(world, x, y, z, color, scale);
    }

    /** The tint of a bubble trail left behind a water particle. */
    public static int trailColor(int color) {
        return color != -1 ? color & 0xFFFFFF : BUBBLE_COLOR;
    }
}

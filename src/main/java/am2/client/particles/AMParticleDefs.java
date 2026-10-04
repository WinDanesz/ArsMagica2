package am2.client.particles;

import am2.ArsMagica;
import am2.api.affinity.Affinity;
import am2.common.registry.Affinities;
import net.minecraft.world.World;

public class AMParticleDefs {

    /**
     * Spawns the particle for an affinity, sized and coloured for the effect that uses it. Water gets a mix of bubbles
     * and water cubes, every other affinity its usual sprite.
     *
     * @param scale particle size, or 0 or less to keep the default
     * @param color spell colour, or -1 for the affinity's default look
     */
    public static AMParticle spawnForAffinity(World world, Affinity aff, double x, double y, double z, float scale, int color) {
        if (Affinities.water.equals(aff)) {
            return WaterParticles.bubbleOrCube(world, x, y, z, color, scale);
        }
        AMParticle particle = (AMParticle) ArsMagica.proxy.particleManager.spawn(world, getParticleForAffinity(aff), x, y, z);
        if (particle != null) {
            if (scale > 0) particle.setParticleScale(scale);
            if (color != -1) particle.setRGBColorI(color & 0xFFFFFF);
        }
        return particle;
    }

    public static String getParticleForAffinity(Affinity aff) {
        if (aff.equals(Affinities.air)) return "wind";
        if (aff.equals(Affinities.arcane)) return "arcane";
        if (aff.equals(Affinities.earth)) return "rock";
        if (aff.equals(Affinities.ender)) return "pulse";
        if (aff.equals(Affinities.fire)) return "explosion_2";
        if (aff.equals(Affinities.ice)) return "ice_hand";
        if (aff.equals(Affinities.life)) return "sparkle";
        if (aff.equals(Affinities.lightning)) return "lightning_hand";
        if (aff.equals(Affinities.nature)) return "plant";
        if (aff.equals(Affinities.water)) return "water_ball";
        if (aff.equals(Affinities.none)) return "lens_flare";
        return "lens_flare";
    }

    public static String getSecondaryParticleForAffinity(Affinity aff) {
        if (aff.equals(Affinities.air)) return "air_hand";
        if (aff.equals(Affinities.arcane)) return "symbols";
        if (aff.equals(Affinities.earth)) return "earth_hand";
        if (aff.equals(Affinities.ender)) return "ghost";
        if (aff.equals(Affinities.fire)) return "smoke";
        if (aff.equals(Affinities.ice)) return "snowflakes";
        if (aff.equals(Affinities.life)) return "sparkle2";
        if (aff.equals(Affinities.lightning)) return "lightning_hand";
        if (aff.equals(Affinities.nature)) return "leaf";
        if (aff.equals(Affinities.water)) return "water_hand";
        if (aff.equals(Affinities.none)) return "lights";
        return "lights";

    }
}

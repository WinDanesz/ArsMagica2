package am2.common.affinity.abilities;

import am2.ArsMagica;
import am2.api.affinity.AbstractAffinityAbility;
import am2.api.affinity.Affinity;
import am2.common.extensions.AffinityData;
import am2.common.extensions.EntityExtension;
import am2.common.registry.Affinities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

public class AbilityShortCircuit extends AbstractAffinityAbility {

    private static final String COOLDOWN_KEY = "ShortCircuit";
    private static final int PARTICLE_BOLTS = 3;
    private static final double PARTICLE_SPREAD = 1.5;

    public AbilityShortCircuit() {
        super(new ResourceLocation("arsmagica2", "shortcircuit"));
    }

    @Override
    public float getMinimumDepth() {
        return 0.25f;
    }

    @Override
    public Affinity getAffinity() {
        return Affinities.lightning;
    }

    @Override
    public void applyTick(EntityPlayer player) {
        if (!player.isWet()) return;
        if (AffinityData.For(player).getCooldown(COOLDOWN_KEY) > 0) return;
        if (player.getRNG().nextFloat() < ArsMagica.config.getAffinityShortCircuitChance()) {
            EntityExtension.For(player).deductMana(ArsMagica.config.getAffinityShortCircuitManaDrain());
            AffinityData.For(player).addCooldown(COOLDOWN_KEY, ArsMagica.config.getAffinityShortCircuitCooldown());
            spawnSparkParticles(player);
        }
    }

    // Called from the server-side tick loop; ParticleManagerServer#BoltFromEntityToPoint already
    // relays the effect to nearby clients via a packet (see AbilityFulmination's creeper-supercharge
    // bolt for the same pattern), so no isRemote check is needed here.
    private void spawnSparkParticles(EntityPlayer player) {
        for (int i = 0; i < PARTICLE_BOLTS; i++) {
            double px = player.posX + (player.getRNG().nextDouble() - 0.5) * PARTICLE_SPREAD * 2;
            double py = player.posY + player.getRNG().nextDouble() * player.height;
            double pz = player.posZ + (player.getRNG().nextDouble() - 0.5) * PARTICLE_SPREAD * 2;
            ArsMagica.proxy.particleManager.BoltFromEntityToPoint(player.world, player, px, py, pz);
        }
    }
}

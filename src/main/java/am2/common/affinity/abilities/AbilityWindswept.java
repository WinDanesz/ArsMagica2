package am2.common.affinity.abilities;

import am2.ArsMagica;
import am2.api.affinity.AbstractAffinityAbility;
import am2.api.affinity.Affinity;
import am2.common.extensions.AffinityData;
import am2.common.packet.AMNetHandler;
import am2.common.registry.Affinities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

/**
 * Before you learn to control the wind, it still throws you around: taking a combat hit adds a
 * bonus shove away from the attacker, on top of whatever knockback the hit already applies.
 * Strongest right at the minimum qualifying depth, tapering linearly down to nothing as depth
 * approaches the maximum — a liability that fades with mastery rather than a flat, constant one.
 */
public class AbilityWindswept extends AbstractAffinityAbility {

    public AbilityWindswept() {
        super(new ResourceLocation("arsmagica2", "windswept"));
    }

    @Override
    public float getMinimumDepth() {
        return ArsMagica.config.getAffinityWindsweptMinDepth();
    }

    @Override
    public float getMaximumDepth() {
        return ArsMagica.config.getAffinityWindsweptMaxDepth();
    }

    @Override
    public Affinity getAffinity() {
        return Affinities.air;
    }

    @Override
    public void applyHurt(EntityPlayer player, LivingHurtEvent event, boolean isAttacker) {
        if (isAttacker || player.world.isRemote || event.getAmount() <= 0) return;

        Entity source = event.getSource().getImmediateSource();
        if (source == null || source == player) return;

        double dx = player.posX - source.posX;
        double dz = player.posZ - source.posZ;
        double dist = Math.sqrt(dx * dx + dz * dz);
        if (dist < 1.0e-4) return;

        double strength = ArsMagica.config.getAffinityWindsweptKnockback() * knockbackFalloff(player);
        double x = (dx / dist) * strength;
        double z = (dz / dist) * strength;
        double y = 0.15;

        player.addVelocity(x, y, z);
        AMNetHandler.INSTANCE.sendVelocityAddPacket(player.world, player, x, y, z);
    }

    // 1.0 (full configured strength) right at the minimum depth, ramping linearly down to 0.0 at
    // the maximum depth — canApply() already guarantees depth is within [min, max] whenever this runs.
    private float knockbackFalloff(EntityPlayer player) {
        float minDepth = ArsMagica.config.getAffinityWindsweptMinDepth();
        float maxDepth = ArsMagica.config.getAffinityWindsweptMaxDepth();
        float depth = (float) AffinityData.For(player).getAffinityDepth(getAffinity());
        float span = Math.max(1e-4f, maxDepth - minDepth);
        float progress = Math.min(1f, Math.max(0f, (depth - minDepth) / span));
        return 1f - progress;
    }
}

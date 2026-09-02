package am2.common.affinity.abilities;

import am2.api.affinity.AbstractAffinityAbility;
import am2.api.affinity.Affinity;
import am2.common.extensions.AffinityData;
import am2.common.registry.Affinities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class AbilityExpandedLungs extends AbstractAffinityAbility {

    /** Effective underwater breath duration at 100% Water affinity depth, relative to normal. */
    private static final double MAX_DURATION_MULTIPLIER = 3.0D;
    /** Vanilla's air supply cap; EntityLivingBase exposes no getter for it. */
    private static final int MAX_AIR = 300;

    public AbilityExpandedLungs() {
        super(new ResourceLocation("arsmagica2", "expandedlungs"));
    }

    @Override
    public float getMinimumDepth() {
        return 0.4f;
    }

    @Override
    public Affinity getAffinity() {
        return Affinities.water;
    }

    @Override
    public void applyTick(EntityPlayer player) {
        if (!player.isInWater())
            return;

        double depth = AffinityData.For(player).getAffinityDepth(getAffinity());
        // 0 at the activation threshold, 1 at full (100%) depth
        double progress = MathHelper.clamp((depth - getMinimumDepth()) / (1.0 - getMinimumDepth()), 0, 1);
        double durationMultiplier = 1.0 + progress * (MAX_DURATION_MULTIPLIER - 1.0);
        // vanilla drains 1 air/tick underwater; this cancels that drain often enough
        // to stretch total breath-holding time out to durationMultiplier x normal
        double regenChance = 1.0 - 1.0 / durationMultiplier;

        if (player.world.rand.nextDouble() < regenChance) {
            player.setAir(Math.min(player.getAir() + 1, MAX_AIR));
        }
    }

}

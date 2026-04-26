package am2.common.affinity.abilities;

import am2.api.affinity.AbstractAffinityAbility;
import am2.api.affinity.Affinity;
import am2.common.extensions.AffinityData;
import am2.common.registry.Affinities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public class AbilitySolidBones extends AbstractAffinityAbility {

    public AbilitySolidBones() {
        super(new ResourceLocation("arsmagica2", "solidbones"));
    }

    @Override
    public float getMinimumDepth() {
        return 0.5f;
    }

    @Override
    public Affinity getAffinity() {
        return Affinities.earth;
    }

    @Override
    public void applyTick(EntityPlayer player) {
        if (player.isInWater()) {
            double earthDepth = AffinityData.For(player).getAffinityDepth(Affinities.earth);
            if (player.motionY > -0.3f) {
                player.addVelocity(0, -0.01f * earthDepth, 0);
            }
        }
    }

    @Override
    public void applyFall(EntityPlayer player, LivingFallEvent event) {
        double earthDepth = AffinityData.For(player).getAffinityDepth(Affinities.earth);
        event.setDistance((float) (event.getDistance() + (1.25 * (earthDepth))));
    }

    @Override
    public void applyHurt(EntityPlayer player, LivingHurtEvent event, boolean isAttacker) {
        if (!isAttacker) {
            double earthDepth = AffinityData.For(player).getAffinityDepth(Affinities.earth);
            double reduction = 0.1f * earthDepth;
            event.setAmount((float) (event.getAmount() - (event.getAmount() * reduction)));
        }
    }
}

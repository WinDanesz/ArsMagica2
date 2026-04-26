package am2.common.affinity.abilities;

import am2.ArsMagica;
import am2.api.affinity.AbstractAffinityAbility;
import am2.api.affinity.Affinity;
import am2.common.extensions.AffinityData;
import am2.common.extensions.EntityExtension;
import am2.common.registry.Affinities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;

public class AbilityAgile extends AbstractAffinityAbility {

    public AbilityAgile() {
        super(new ResourceLocation("arsmagica2", "agile"));
    }

    @Override
    public float getMinimumDepth() {
        return ArsMagica.config.getAffinityAgileMinDepth();
    }

    @Override
    public Affinity getAffinity() {
        return Affinities.air;
    }

    @Override
    public void applyJump(EntityPlayer player, LivingJumpEvent event) {
        double airDepth = AffinityData.For(player).getAffinityDepth(Affinities.air);
        double velocity = airDepth * ArsMagica.config.getAffinityAgileJumpBoost();
        if (EntityExtension.For(player).getIsFlipped())
            velocity *= -1;
        player.addVelocity(0, velocity, 0);
    }

    @Override
    public void applyFall(EntityPlayer player, LivingFallEvent event) {
        double airDepth = AffinityData.For(player).getAffinityDepth(Affinities.air);
        event.setDistance((float) (event.getDistance() - (2 * airDepth)));
        if (event.getDistance() < 0) event.setDistance(0);
    }

}

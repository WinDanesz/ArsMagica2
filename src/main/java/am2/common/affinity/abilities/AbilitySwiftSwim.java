package am2.common.affinity.abilities;

import am2.api.affinity.AbstractAffinityAbility;
import am2.api.affinity.Affinity;
import am2.common.extensions.AffinityData;
import am2.common.registry.AMPotions;
import am2.common.registry.Affinities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;

public class AbilitySwiftSwim extends AbstractAffinityAbility {

    public AbilitySwiftSwim() {
        super(new ResourceLocation("arsmagica2", "swiftswim"));
    }

    @Override
    public float getMinimumDepth() {
        return 0.5f;
    }

    @Override
    public Affinity getAffinity() {
        return Affinities.water;
    }

    @Override
    public void applyTick(EntityPlayer player) {
        if (player.isInWater()) {
            if (!player.world.isRemote && (!player.isPotionActive(AMPotions.swift_swim) || player.getActivePotionEffect(AMPotions.swift_swim).getDuration() < 10)) {
                player.addPotionEffect(new PotionEffect(AMPotions.swift_swim, 100, AffinityData.For(player).getAffinityDepth(getAffinity()) > 0.75f ? 1 : 0));
            }
        }
    }

}

package am2.common.affinity.abilities;

import am2.api.affinity.AbstractAffinityAbility;
import am2.api.affinity.Affinity;
import am2.api.event.SpellCastEvent;
import am2.common.extensions.AffinityData;
import am2.common.registry.AMPotions;
import am2.common.registry.Affinities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;

public class AbilityClearCaster extends AbstractAffinityAbility {

    private static final double BASE_CHANCE_PERCENT = 5.0;
    // Above this depth, each 1% of extra depth adds 1% more chance, up to +10% at full depth.
    private static final double BONUS_CHANCE_DEPTH_THRESHOLD = 0.9;

    public AbilityClearCaster() {
        super(new ResourceLocation("arsmagica2", "clearcaster"));
    }

    @Override
    public float getMinimumDepth() {
        return 0.4f;
    }

    @Override
    public Affinity getAffinity() {
        return Affinities.arcane;
    }

    @Override
    public void applySpellCast(EntityPlayer player, SpellCastEvent.Post event) {
        if (event.entityLiving.world.isRemote) return;

        double arcaneDepth = AffinityData.For(player).getAffinityDepth(getAffinity());
        double bonusChance = Math.max(0.0, arcaneDepth - BONUS_CHANCE_DEPTH_THRESHOLD) * 100.0;
        double chance = BASE_CHANCE_PERCENT + bonusChance;

        if (event.entityLiving.world.rand.nextDouble() * 100.0 < chance) {
            event.entityLiving.addPotionEffect(new PotionEffect(AMPotions.clarity, 140, 0));
        }
    }
}

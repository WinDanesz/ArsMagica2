package am2.common.affinity.abilities;

import am2.ArsMagica;
import am2.api.affinity.AbstractAffinityAbility;
import am2.api.affinity.Affinity;
import am2.api.event.SpellCastEvent;
import am2.api.event.SpellCastEvent.Pre;
import am2.common.extensions.AffinityData;
import am2.common.registry.AMPotions;
import am2.common.registry.Affinities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;

public class AbilityClearCaster extends AbstractAffinityAbility {

    private static final String COOLDOWN_KEY = "ClearCasterClarity";
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
        if (AffinityData.For(player).getCooldown(COOLDOWN_KEY) > 0) return;

        double arcaneDepth = AffinityData.For(player).getAffinityDepth(getAffinity());
        double bonusChance = Math.max(0.0, arcaneDepth - BONUS_CHANCE_DEPTH_THRESHOLD) * 100.0;
        double chance = BASE_CHANCE_PERCENT + bonusChance;

        if (event.entityLiving.world.rand.nextDouble() * 100.0 < chance) {
            event.entityLiving.addPotionEffect(new PotionEffect(AMPotions.clarity, 140, 0));
            AffinityData.For(player).addCooldown(COOLDOWN_KEY, ArsMagica.config.getAffinityClearCasterCooldown());
        }
    }

    // Clear Caster grants Clarity, so Clear Caster is what spends it — previously this lived on
    // Magical Focus (AbilityOneWithMagic) instead, which meant a player with only Clear Caster's own
    // 40% depth (below Magical Focus's separate 50% threshold) could proc Clarity but had no ability
    // active that would ever actually consume it for the free cast.
    @Override
    public void applyPreSpellCast(EntityPlayer player, Pre event) {
        if (event.entityLiving.isPotionActive(AMPotions.clarity)) {
            event.manaCost = 0f;
            event.burnout = 0f;
            event.entityLiving.removePotionEffect(AMPotions.clarity);
        }
    }
}

package am2.common.affinity.abilities;

import am2.ArsMagica;
import am2.api.affinity.AbstractAffinityAbility;
import am2.api.affinity.Affinity;
import am2.common.extensions.AffinityData;
import am2.common.registry.Affinities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public class AbilityRimeguard extends AbstractAffinityAbility {

    /** Ability-float key holding the player's current Rimeguard shield amount, in HP. Synced to the owning client for the health bar and player-layer rendering. */
    public static final String SHIELD_KEY = "rimeguard_shield";
    private static final String CALM_TICKS_KEY = "rimeguard_calm_ticks";
    public static final float HP_PER_HEART = 2.0f;

    public AbilityRimeguard() {
        super(new ResourceLocation("arsmagica2", "rimeguard"));
    }

    @Override
    public float getMinimumDepth() {
        return ArsMagica.config.getAffinityRimeguardMinDepth();
    }

    @Override
    public Affinity getAffinity() {
        return Affinities.ice;
    }

    @Override
    public void applyTick(EntityPlayer player) {
        AffinityData data = AffinityData.For(player);
        float cap = computeCap(player, data);

        float shield = data.getAbilityFloat(SHIELD_KEY);
        if (shield > cap) {
            shield = cap;
            data.addAbilityFloat(SHIELD_KEY, shield);
        }

        float calmTicks = data.getAbilityFloat(CALM_TICKS_KEY) + 1f;
        data.addAbilityFloat(CALM_TICKS_KEY, calmTicks);

        int grace = ArsMagica.config.getAffinityRimeguardCalmTicks();
        if (calmTicks >= grace) {
            int ticksPerHeart = Math.max(1, ArsMagica.config.getAffinityRimeguardTicksPerHeart());
            int heartsEarned = (int) ((calmTicks - grace) / ticksPerHeart);
            float target = Math.min(cap, heartsEarned * HP_PER_HEART);
            if (target > shield) {
                data.addAbilityFloat(SHIELD_KEY, target);
            }
        }
    }

    // Absorbs damage directly out of LivingHurtEvent's amount, entirely separate from vanilla's
    // own absorptionAmount/Absorption potion mechanic — a golden apple or other absorption source
    // stacks normally instead of being overwritten by (or fighting with) this shield.
    @Override
    public void applyHurt(EntityPlayer player, LivingHurtEvent event, boolean isAttacker) {
        if (isAttacker) return;

        AffinityData data = AffinityData.For(player);
        float shield = data.getAbilityFloat(SHIELD_KEY);
        if (shield > 0f && event.getAmount() > 0f) {
            float absorbed = Math.min(shield, event.getAmount());
            event.setAmount(event.getAmount() - absorbed);
            shield -= absorbed;
            data.addAbilityFloat(SHIELD_KEY, shield);
            if (shield <= 0f) {
                spawnBreakParticles(player);
            }
        }
        data.addAbilityFloat(CALM_TICKS_KEY, 0f);
    }

    @Override
    public void removeEffects(EntityPlayer player) {
        AffinityData data = AffinityData.For(player);
        data.addAbilityFloat(SHIELD_KEY, 0f);
        data.addAbilityFloat(CALM_TICKS_KEY, 0f);
    }

    private float computeCap(EntityPlayer player, AffinityData data) {
        float minDepth = ArsMagica.config.getAffinityRimeguardMinDepth();
        float maxFraction = ArsMagica.config.getAffinityRimeguardMaxShieldFraction();
        float depth = (float) data.getAffinityDepth(getAffinity());
        float span = Math.max(1e-4f, 1f - minDepth);
        float progress = Math.min(1f, Math.max(0f, (depth - minDepth) / span));
        return progress * maxFraction * player.getMaxHealth();
    }

    // Called from the server-side tick loop; WorldServer's numberOfParticles overload builds and
    // broadcasts the particle packet to nearby clients itself (see AbilityHealingTouch/AbilityRelocation
    // for the same pattern), so no isRemote check or client-side particle manager call is needed here.
    private void spawnBreakParticles(EntityPlayer player) {
        if (!(player.world instanceof WorldServer)) return;
        WorldServer world = (WorldServer) player.world;
        world.spawnParticle(EnumParticleTypes.SNOWBALL,
                player.posX, player.posY + player.height * 0.5D, player.posZ,
                20, player.width * 0.6D, player.height * 0.5D, player.width * 0.6D, 0.05D);
    }
}

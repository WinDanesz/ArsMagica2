package am2.common.affinity.abilities;

import am2.ArsMagica;
import am2.api.affinity.AbstractToggledAffinityAbility;
import am2.api.affinity.Affinity;
import am2.common.extensions.AffinityData;
import am2.common.registry.Affinities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldServer;

/**
 * Toggled with a key bind (see Keybindings.TAILWIND). While on, double-jumping — vanilla's own
 * creative-flight gesture — burns from a depth-scaled "air" reserve for short bursts of real flight.
 * <p>
 * The flight grant reuses the exact technique {@code BuffEffectFlight} (the Flight spell's potion
 * effect) already relies on — {@code capabilities.allowFlying} plus {@code sendPlayerAbilities()} —
 * just driven by the fuel gauge instead of a potion duration, so it stays server-authoritative and
 * doesn't need any client-side capability prediction.
 */
public class AbilityTailwind extends AbstractToggledAffinityAbility {

    public static final String FUEL_KEY = "tailwind_fuel";
    private static final String REGEN_COOLDOWN_KEY = "TailwindRegen";
    private static final float VANILLA_FLY_SPEED = 0.05f;

    public AbilityTailwind() {
        super(new ResourceLocation("arsmagica2", "tailwind"));
    }

    @Override
    public float getMinimumDepth() {
        return ArsMagica.config.getAffinityTailwindMinDepth();
    }

    @Override
    public Affinity getAffinity() {
        return Affinities.air;
    }

    @Override
    protected boolean isEnabled(EntityPlayer player) {
        return AffinityData.For(player).getAbilityBoolean(AffinityData.TAILWIND);
    }

    // hasClientTick() is deliberately left at its default (false): everything here only touches
    // capabilities.allowFlying/isFlying, which are server-authoritative and synced to the client via
    // sendPlayerAbilities() rather than needing client-side motion prediction.
    @Override
    public void applyTick(EntityPlayer player) {
        if (!managesFlightFor(player)) return;

        AffinityData data = AffinityData.For(player);
        float maxFuel = computeMaxFuel(data);
        float fuel = Math.min(maxFuel, data.getAbilityFloat(FUEL_KEY));

        if (player.capabilities.isFlying) {
            fuel -= ArsMagica.config.getAffinityTailwindFlightDrainPerTick();
            if (fuel <= 0f) {
                fuel = 0f;
                revokeFlight(player);
                data.addCooldown(REGEN_COOLDOWN_KEY, ArsMagica.config.getAffinityTailwindFullDepletionRegenDelay());
            } else {
                // Primes the shorter delay in case this turns out to be the last tick of flight (the
                // player lands, or double-taps jump again to stop) — refreshed every tick while still
                // flying, so it's a no-op until flight actually ends, at which point whatever this was
                // last set to (this branch's short delay, or the depletion branch's longer one above)
                // is what governs the pause before regen resumes.
                data.addCooldown(REGEN_COOLDOWN_KEY, ArsMagica.config.getAffinityTailwindPartialRegenDelay());
                spawnTrailParticles(player);
            }
        } else if (data.getCooldown(REGEN_COOLDOWN_KEY) <= 0) {
            fuel = Math.min(maxFuel, fuel + ArsMagica.config.getAffinityTailwindFuelRegenPerTick());
        }

        // Any reserve above zero is immediately flyable — no separate "banked enough to resume" gate.
        // The cooldown above only controls when regen begins, never how much is needed once it does;
        // gating on a banked fraction on top of that was what made a partial refill look available
        // (fuel ticking up, the crosshair ring shrinking back) while flight still silently refused to
        // re-engage.
        boolean shouldAllowFlying = fuel > 0f;
        if (shouldAllowFlying != player.capabilities.allowFlying) {
            player.capabilities.allowFlying = shouldAllowFlying;
            // Vanilla creative flight's own speed (flySpeed) otherwise applies unmodified; set here
            // (and reset back on revoke, including in revokeFlight() below) so Tailwind flight has
            // its own configurable pace instead of just inheriting creative's.
            player.capabilities.setFlySpeed(shouldAllowFlying ? ArsMagica.config.getAffinityTailwindFlightSpeed() : VANILLA_FLY_SPEED);
            player.sendPlayerAbilities();
        }

        data.addAbilityFloat(FUEL_KEY, fuel);
    }

    // The base reserve is what you get right at the minimum qualifying depth; it then scales
    // linearly up to maxDurationMultiplier times that amount at 100% depth. Never bottoms out at
    // zero — barely qualifying still gets you the full base duration.
    //
    // Public/static so the crosshair HUD ring (AMIngameGUI) can compute the same fraction the fuel
    // gauge itself uses, without duplicating the ramp math or needing a live ability instance.
    public static float computeMaxFuel(AffinityData data) {
        float minDepth = ArsMagica.config.getAffinityTailwindMinDepth();
        float baseFuel = ArsMagica.config.getAffinityTailwindBaseFuelTicks();
        float maxMultiplier = ArsMagica.config.getAffinityTailwindMaxDurationMultiplier();
        float depth = (float) data.getAffinityDepth(Affinities.air);
        float span = Math.max(1e-4f, 1f - minDepth);
        float progress = Math.min(1f, Math.max(0f, (depth - minDepth) / span));
        float multiplier = 1f + (maxMultiplier - 1f) * progress;
        return baseFuel * multiplier;
    }

    private void revokeFlight(EntityPlayer player) {
        player.capabilities.allowFlying = false;
        player.capabilities.isFlying = false;
        player.capabilities.setFlySpeed(VANILLA_FLY_SPEED);
        player.fallDistance = 0f;
        player.sendPlayerAbilities();
    }

    private void spawnTrailParticles(EntityPlayer player) {
        if (!(player.world instanceof WorldServer)) return;
        ((WorldServer) player.world).spawnParticle(EnumParticleTypes.CLOUD,
                player.posX, player.posY + 0.2D, player.posZ,
                3, player.width * 0.35D, 0.1D, player.width * 0.35D, 0.01D);
    }

    // Deliberately leaves the banked fuel alone: this only fires when the ability stops applying
    // (toggled off, or depth dropped below the threshold), and the reserve needs to persist across
    // that rather than reset. The regen-delay cooldown doesn't need any handling here either — like
    // every other ability's cooldowns, AffinityAbilityHelper ticks it down unconditionally regardless
    // of whether this ability currently applies.
    @Override
    public void removeEffects(EntityPlayer player) {
        if (managesFlightFor(player) && (player.capabilities.allowFlying || player.capabilities.isFlying)) {
            revokeFlight(player);
        }
    }

    // Spectators get allowFlying=true, isFlying=true forced by GameType.configurePlayerCapabilities
    // and have no other way to regain them (only creative mode grants allowFlying on its own outside
    // this ability) — isCreativeMode is false for spectators too, so checking only that let this
    // ability treat a spectator as "actively flying" every tick, immediately drain to empty, and
    // permanently revoke allowFlying. With isFlying then forced false but noclip still on, the result
    // was an uncontrollable fall through the world instead of the spectator's normal free-float.
    private static boolean managesFlightFor(EntityPlayer player) {
        return !player.capabilities.isCreativeMode && !player.isSpectator();
    }
}

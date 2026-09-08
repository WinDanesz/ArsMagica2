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
import net.minecraftforge.event.entity.player.PlayerFlyableFallEvent;

/**
 * Toggled with a key bind (see Keybindings.TAILWIND). While on, double-jumping — vanilla's own
 * creative-flight gesture — burns from a depth-scaled "air" reserve for short bursts of real flight,
 * via {@code capabilities.allowFlying}/{@code sendPlayerAbilities()}.
 */
public class AbilityTailwind extends AbstractToggledAffinityAbility {

    public static final String FUEL_KEY = "tailwind_fuel";
    private static final String REGEN_COOLDOWN_KEY = "TailwindRegen";
    // Tracks whether Tailwind itself granted the current allowFlying/isFlying state, as opposed to
    // another mod's item (Thaumcraft's Thaumostatic Harness, Botania's Flugel Tiara, ...) that grants
    // survival flight through those same vanilla fields — without this, Tailwind would revoke flight
    // it never granted every time it doesn't apply, i.e. for every player who's never touched it.
    private static final String GRANTED_FLIGHT_KEY = "tailwind_granted_flight";
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

    @Override
    public void applyTick(EntityPlayer player) {
        if (!managesFlightFor(player)) return;

        AffinityData data = AffinityData.For(player);
        float maxFuel = computeMaxFuel(data);
        float fuel = Math.min(maxFuel, data.getAbilityFloat(FUEL_KEY));
        boolean grantedByUs = data.getAbilityBoolean(GRANTED_FLIGHT_KEY);

        if (player.capabilities.isFlying && grantedByUs) {
            fuel -= ArsMagica.config.getAffinityTailwindFlightDrainPerTick();
            if (fuel <= 0f) {
                fuel = 0f;
                revokeFlight(player, data);
                data.addCooldown(REGEN_COOLDOWN_KEY, ArsMagica.config.getAffinityTailwindFullDepletionRegenDelay());
            } else {
                data.addCooldown(REGEN_COOLDOWN_KEY, ArsMagica.config.getAffinityTailwindPartialRegenDelay());
                spawnTrailParticles(player);
            }
        } else if (!player.capabilities.isFlying && data.getCooldown(REGEN_COOLDOWN_KEY) <= 0) {
            fuel = Math.min(maxFuel, fuel + ArsMagica.config.getAffinityTailwindFuelRegenPerTick());
        }

        boolean shouldAllowFlying = fuel > 0f;
        grantedByUs = data.getAbilityBoolean(GRANTED_FLIGHT_KEY);
        // Only claim allowFlying when it's currently off — if another mod's item already has it on,
        // leave it to that mod so a later fuel depletion doesn't revoke a grant that wasn't ours.
        if (shouldAllowFlying && !grantedByUs && !player.capabilities.allowFlying) {
            player.capabilities.allowFlying = true;
            player.capabilities.setFlySpeed(ArsMagica.config.getAffinityTailwindFlightSpeed());
            player.sendPlayerAbilities();
            data.addAbilityBoolean(GRANTED_FLIGHT_KEY, true);
        } else if (!shouldAllowFlying && grantedByUs) {
            revokeFlight(player, data);
        }

        data.addAbilityFloat(FUEL_KEY, fuel);
    }

    @Override
    public void applyFlyableFall(EntityPlayer player, PlayerFlyableFallEvent event) {
        // Vanilla skips EntityLivingBase.fall() whenever allowFlying is true, even if isFlying is
        // false. Run the normal fall path for Tailwind freefalls specifically — but only when
        // Tailwind granted the flight, so this doesn't force fall damage on another mod's flight item.
        if (player.capabilities.isFlying) return;
        if (!AffinityData.For(player).getAbilityBoolean(GRANTED_FLIGHT_KEY)) return;

        boolean allowFlying = player.capabilities.allowFlying;
        player.capabilities.allowFlying = false;
        player.fall(event.getDistance(), event.getMultiplier());
        player.capabilities.allowFlying = allowFlying;
    }

    // Public/static so the crosshair HUD ring (AMIngameGUI) can compute the same fraction.
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

    private void revokeFlight(EntityPlayer player, AffinityData data) {
        player.capabilities.allowFlying = false;
        player.capabilities.isFlying = false;
        player.capabilities.setFlySpeed(VANILLA_FLY_SPEED);
        player.sendPlayerAbilities();
        data.addAbilityBoolean(GRANTED_FLIGHT_KEY, false);
    }

    private void spawnTrailParticles(EntityPlayer player) {
        if (!(player.world instanceof WorldServer)) return;
        ((WorldServer) player.world).spawnParticle(EnumParticleTypes.CLOUD,
                player.posX, player.posY + 0.2D, player.posZ,
                3, player.width * 0.35D, 0.1D, player.width * 0.35D, 0.01D);
    }

    @Override
    public void removeEffects(EntityPlayer player) {
        AffinityData data = AffinityData.For(player);
        if (managesFlightFor(player) && data.getAbilityBoolean(GRANTED_FLIGHT_KEY)) {
            revokeFlight(player, data);
        }
    }

    // Spectators get allowFlying=true/isFlying=true forced by vanilla with no other way to regain
    // them, so treating them like a normal player here would drain to empty and permanently revoke
    // flight, leaving them falling through the world with noclip still on.
    private static boolean managesFlightFor(EntityPlayer player) {
        return !player.capabilities.isCreativeMode && !player.isSpectator();
    }
}

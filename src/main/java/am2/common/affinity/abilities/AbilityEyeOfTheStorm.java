package am2.common.affinity.abilities;

import am2.ArsMagica;
import am2.api.affinity.AbstractAffinityAbility;
import am2.api.affinity.Affinity;
import am2.common.extensions.AffinityData;
import am2.common.registry.Affinities;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.List;

/**
 * Capstone panic button: when a hit would drop the player to a critical fraction of max health and
 * the ability is off cooldown, unleash an instant 360-degree gust that knocks back nearby mobs
 * (players are deliberately left untouched, so this can't be used to shove people off ledges in
 * PvP or fling teammates around in co-op) and clears fire off the player and nearby blocks. Long
 * cooldown by design — this is an emergency valve, not a repeatable free disengage.
 */
public class AbilityEyeOfTheStorm extends AbstractAffinityAbility {

    private static final String COOLDOWN_KEY = "EyeOfTheStorm";
    private static final int FIRE_CLEAR_RADIUS = 3;

    public AbilityEyeOfTheStorm() {
        super(new ResourceLocation("arsmagica2", "eyeofthestorm"));
    }

    @Override
    public float getMinimumDepth() {
        return ArsMagica.config.getAffinityEyeOfTheStormMinDepth();
    }

    @Override
    public Affinity getAffinity() {
        return Affinities.air;
    }

    @Override
    public void applyHurt(EntityPlayer player, LivingHurtEvent event, boolean isAttacker) {
        if (isAttacker || player.world.isRemote) return;
        if (AffinityData.For(player).getCooldown(COOLDOWN_KEY) > 0) return;

        float remainingHealth = player.getHealth() - event.getAmount();
        float threshold = player.getMaxHealth() * ArsMagica.config.getAffinityEyeOfTheStormHealthThreshold();
        if (remainingHealth <= 0f || remainingHealth > threshold) return;

        trigger(player);
        AffinityData.For(player).addCooldown(COOLDOWN_KEY, ArsMagica.config.getAffinityEyeOfTheStormCooldown());
    }

    private void trigger(EntityPlayer player) {
        WorldServer world = (WorldServer) player.world;
        double radius = ArsMagica.config.getAffinityEyeOfTheStormRadius();
        double strength = ArsMagica.config.getAffinityEyeOfTheStormKnockback();

        List<EntityLivingBase> nearby = world.getEntitiesWithinAABB(EntityLivingBase.class,
                player.getEntityBoundingBox().grow(radius, radius * 0.5, radius));
        for (EntityLivingBase ent : nearby) {
            if (ent == player || ent instanceof EntityPlayer) continue;

            double dx = ent.posX - player.posX;
            double dz = ent.posZ - player.posZ;
            double dist = Math.sqrt(dx * dx + dz * dz);
            if (dist < 1.0e-4) continue;

            double x = (dx / dist) * strength;
            double z = (dz / dist) * strength;
            ent.addVelocity(x, 0.4, z);
            ent.fallDistance = 0f;
        }

        player.extinguish();
        clearNearbyFire(world, player.getPosition());

        world.spawnParticle(EnumParticleTypes.CLOUD,
                player.posX, player.posY + player.height * 0.5D, player.posZ,
                40, player.width * 1.5D, player.height * 0.5D, player.width * 1.5D, 0.15D);
    }

    private void clearNearbyFire(WorldServer world, BlockPos center) {
        BlockPos min = center.add(-FIRE_CLEAR_RADIUS, -FIRE_CLEAR_RADIUS, -FIRE_CLEAR_RADIUS);
        BlockPos max = center.add(FIRE_CLEAR_RADIUS, FIRE_CLEAR_RADIUS, FIRE_CLEAR_RADIUS);
        for (BlockPos pos : BlockPos.getAllInBox(min, max)) {
            if (world.getBlockState(pos).getBlock() == Blocks.FIRE) {
                world.setBlockToAir(pos);
            }
        }
    }
}

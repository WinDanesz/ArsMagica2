package am2.common.affinity.abilities;

import am2.ArsMagica;
import am2.api.affinity.AbstractAffinityAbility;
import am2.api.affinity.Affinity;
import am2.common.extensions.AffinityData;
import am2.common.packet.AMNetHandler;
import am2.common.registry.Affinities;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

/**
 * Melee hits wreathe the target in a visible gust and shove it back, scaling with depth. Unlike
 * {@link AbilityFirePunch}/{@link AbilityThunderPunch} this isn't fist-only — it's ambient wind
 * around the caster, not a punch effect, so it works with any weapon. The gust particle shows on
 * every hit for feedback, but the actual knockback is on a short cooldown so it doesn't juggle the
 * target out of melee range on every single swing.
 */
public class AbilityGaleFist extends AbstractAffinityAbility {

    private static final String COOLDOWN_KEY = "GaleFist";

    public AbilityGaleFist() {
        super(new ResourceLocation("arsmagica2", "galefist"));
    }

    @Override
    public float getMinimumDepth() {
        return ArsMagica.config.getAffinityGaleFistMinDepth();
    }

    @Override
    public Affinity getAffinity() {
        return Affinities.air;
    }

    @Override
    public void applyHurt(EntityPlayer player, LivingHurtEvent event, boolean isAttacker) {
        if (!isAttacker || player.world.isRemote) return;

        EntityLivingBase target = event.getEntityLiving();
        spawnGustParticles(player.world, target);

        if (AffinityData.For(player).getCooldown(COOLDOWN_KEY) > 0) return;

        double dx = target.posX - player.posX;
        double dz = target.posZ - player.posZ;
        double dist = Math.sqrt(dx * dx + dz * dz);
        if (dist >= 1.0e-4) {
            double depth = AffinityData.For(player).getAffinityDepth(getAffinity());
            double strength = ArsMagica.config.getAffinityGaleFistKnockback() * depth;
            double x = (dx / dist) * strength;
            double z = (dz / dist) * strength;
            double y = 0.25;

            target.addVelocity(x, y, z);
            if (target instanceof EntityPlayer) {
                AMNetHandler.INSTANCE.sendVelocityAddPacket(player.world, target, x, y, z);
            }
            target.fallDistance = 0f;
        }

        AffinityData.For(player).addCooldown(COOLDOWN_KEY, ArsMagica.config.getAffinityGaleFistCooldown());
    }

    private void spawnGustParticles(World world, EntityLivingBase target) {
        if (!(world instanceof WorldServer)) return;
        ((WorldServer) world).spawnParticle(EnumParticleTypes.CLOUD,
                target.posX, target.posY + target.height * 0.5D, target.posZ,
                12, target.width * 0.5D, target.height * 0.3D, target.width * 0.5D, 0.05D);
    }
}

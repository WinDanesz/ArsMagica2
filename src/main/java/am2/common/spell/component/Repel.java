package am2.common.spell.component;

import am2.ArsMagica;
import am2.api.affinity.Affinity;
import am2.api.math.AMVector3;
import am2.api.spell.SpellComponent;
import am2.api.spell.SpellData;
import am2.api.spell.SpellModifiers;
import am2.client.particles.AMParticle;
import am2.client.particles.ParticleFadeOut;
import am2.client.particles.ParticleFleePoint;
import am2.common.packet.AMNetHandler;
import am2.common.registry.AMItems;
import am2.common.registry.Affinities;
import com.google.common.collect.Sets;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class Repel extends SpellComponent {

    @Override
    public boolean applyEffectEntity(SpellData spell, World world, EntityLivingBase caster, Entity target) {
        if (target == null)
            return false;
        if (target == caster) {
            EntityLivingBase source = caster;

            if (target instanceof EntityLivingBase)
                source = (EntityLivingBase) target;

            List<Entity> ents = world.getEntitiesWithinAABB(Entity.class, source.getEntityBoundingBox().grow(2, 2, 2));

            for (Entity e : ents) {
                performRepel(world, caster, e);
            }
            return true;
        }

        performRepel(world, caster, target);

        return true;
    }

    /**
     * Checks if a projectile is stuck in the ground or embedded in a block.
     * Projectiles on the ground should not be processed to save performance.
     */
    private boolean isProjectileOnGround(Entity projectile) {
        return projectile.world.collidesWithAnyBlock(projectile.getEntityBoundingBox().grow(0.05D));
    }

    /**
     * Deflects a projectile away from the caster using a stronger force vector.
     * Based on the Forcefend implementation from AncientSpellcraft.
     *
     * @param caster     The entity casting the repel spell
     * @param projectile The projectile to deflect
     */
    private void deflectProjectile(EntityLivingBase caster, Entity projectile) {
        Vec3d centre = caster.getPositionEyes(0).subtract(0, 0.1, 0);
        Vec3d vec = projectile.getPositionVector().subtract(centre).normalize().scale(0.6);

        // For arrows, reset the shooting entity so they can hit their original shooter
        if (projectile instanceof EntityArrow) {
            ((EntityArrow) projectile).shootingEntity = null;
        }

        projectile.addVelocity(vec.x, vec.y, vec.z);
    }

    /**
     * Aims and shoots a projectile at a target entity using proper projectile physics.
     * Uses the IProjectile.shoot() method for accurate trajectory calculation.
     * Based on the Forcefend implementation from AncientSpellcraft.
     *
     * @param target      The target to aim at
     * @param projectile  The projectile entity
     * @param iProjectile The IProjectile interface of the entity
     */
    private void aimArrow(EntityLivingBase target, Entity projectile, IProjectile iProjectile) {
        double d0 = target.posX - projectile.posX;
        double d1 = target.getEntityBoundingBox().minY + (double) (target.height / 3.0F) - projectile.posY;
        double d2 = target.posZ - projectile.posZ;
        double d3 = (double) MathHelper.sqrt(d0 * d0 + d2 * d2);
        // Arc compensation factor of 0.2 for gravity
        iProjectile.shoot(d0, d1 + d3 * 0.20000000298023224D, d2, 1.3F, (float) (14 - target.world.getDifficulty().getId() * 4));

        // For arrows, reset the shooting entity so they can hit their original shooter
        if (projectile instanceof EntityArrow) {
            ((EntityArrow) projectile).shootingEntity = null;
        }
    }

    private void performRepel(World world, EntityLivingBase caster, Entity target) {
        // Special handling for projectiles
        if (target instanceof IProjectile && !isProjectileOnGround(target)) {
            deflectProjectile(caster, target);
            return;
        }

        // Original behavior for non-projectile entities
        Vec3d casterPos = new Vec3d(caster.posX, caster.posY, caster.posZ);
        Vec3d targetPos = new Vec3d(target.posX, target.posY, target.posZ);
        double distance = casterPos.distanceTo(targetPos) + 0.1D;

        Vec3d delta = new Vec3d(targetPos.x - casterPos.x, targetPos.y - casterPos.y, targetPos.z - casterPos.z);

        double dX = delta.x / 2.5D / distance;
        double dY = delta.y / 2.5D / distance;
        double dZ = delta.z / 2.5D / distance;
        if (target instanceof EntityPlayer) {
            AMNetHandler.INSTANCE.sendVelocityAddPacket(world, (EntityPlayer) target, dX, dY, dZ);
        }
        target.motionX += dX;
        target.motionY += dY;
        target.motionZ += dZ;
    }

    @Override
    public EnumSet<SpellModifiers> getModifiers() {
        return EnumSet.noneOf(SpellModifiers.class);
    }

    @Override
    public float manaCost() {
        return 5.0f;
    }


    @Override
    public void spawnParticles(World world, double x, double y, double z, EntityLivingBase caster, Entity target, Random rand, int colorModifier) {
        for (int i = 0; i < ArsMagica.config.getGFXLevel() * 2; i++) {
            AMParticle effect = (AMParticle) ArsMagica.proxy.particleManager.spawn(world, "sparkle", x, y, z);
            if (effect != null) {
                effect.addRandomOffset(1, 2, 1);
                //double dx = caster.posX - target.posX;
                //double dz = caster.posZ - target.posZ;
                //double angle = Math.toDegrees(Math.atan2(-dz, -dx));
                //effect.AddParticleController(new ParticleMoveOnHeading(effect, angle, 0, 0.1 + rand.nextDouble() * 0.5, 1, false));
                effect.AddParticleController(new ParticleFleePoint(effect, new AMVector3(caster).add(new AMVector3(0, caster.getEyeHeight(), 0)).toVec3D(), 0.075f, 3f, 1, true));
                effect.AddParticleController(new ParticleFadeOut(effect, 1, false).setFadeSpeed(0.05f));
                effect.setMaxAge(20);
                if (colorModifier > -1) {
                    effect.setRGBColorF(((colorModifier >> 16) & 0xFF) / 255.0f, ((colorModifier >> 8) & 0xFF) / 255.0f, (colorModifier & 0xFF) / 255.0f);
                }
            }
        }
    }

    @Override
    public Set<Affinity> getAffinity() {
        return Sets.newHashSet(Affinities.none);
    }

    @Override
    public Object[] getRecipe() {
        return new Object[]{
                new ItemStack(AMItems.rune, 1, EnumDyeColor.WHITE.getDyeDamage()),
                Items.WATER_BUCKET
        };
    }

    @Override
    public float getAffinityShift(Affinity affinity) {
        return 0;
    }
}

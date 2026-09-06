package am2.common.entity.ai;

import am2.common.entity.EntityAirElemental;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Approaches from a shared side, then channels a short, forceful air torrent. */
public class EntityAIAirBlast extends EntityAIBase implements AirBlastVolley.Member {
    private final EntityAirElemental host;
    private EntityLivingBase target;
    private AirBlastVolley volley;
    private boolean running;
    private boolean participating;
    private boolean settled;

    public EntityAIAirBlast(EntityAirElemental host) {
        this.host = host;
        setMutexBits(3);
    }

    @Override
    public boolean shouldExecute() {
        EntityLivingBase current = host.getAttackTarget();
        if (!validTarget(current)) return false;
        if (participating && volley != null && !volley.isFinished() && target == current) return true;
        if (host.getAirBlastCooldown() != 0 || host.getDistanceSq(current) > 256.0D) return false;
        // A late arrival waits for an existing torrent rather than countering it
        // with an independent attack from the opposite side.
        for (EntityAirElemental ally : nearbyAllies()) {
            EntityAIAirBlast ai = ally.getAirBlastAI();
            if (ai != null && ai != this && ai.target == current && ai.participating
                    && ai.volley != null && ai.volley.isFiring()) return false;
        }
        return true;
    }

    @Override
    public void startExecuting() {
        running = true;
        host.getNavigator().clearPath();
        if (participating && volley != null && !volley.isFinished() && target == host.getAttackTarget()) return;
        if (volley != null) volley.withdraw(this);

        settled = false;
        target = host.getAttackTarget();
        List<EntityAirElemental> allies = nearbyAllies();
        // Target AI runs separately for each mob. Allow allies that acquire the
        // target later in this tick to join the same gathering volley.
        for (EntityAirElemental ally : allies) {
            EntityAIAirBlast ai = ally.getAirBlastAI();
            if (ai != null && ai != this && ai.target == target && ai.participating
                    && ai.volley != null && ai.volley.recruit(this, host.world.getTotalWorldTime())) {
                volley = ai.volley;
                participating = true;
                return;
            }
        }
        List<EntityAIAirBlast> group = new ArrayList<>();
        group.add(this);
        for (EntityAirElemental ally : allies) {
            if (ally == host || !ally.isEntityAlive() || ally.getAttackTarget() != target
                    || ally.getAirBlastCooldown() > 40) continue;
            EntityAIAirBlast ai = ally.getAirBlastAI();
            if (ai != null && (ai.volley == null || ai.volley.isFinished())) group.add(ai);
        }
        group.sort(Comparator.comparingInt(ai -> ai.host.getEntityId()));
        double sideX = host.posX - target.posX, sideZ = host.posZ - target.posZ;
        double length = Math.sqrt(sideX * sideX + sideZ * sideZ);
        if (length < 0.001D) {
            double yaw = Math.toRadians(host.rotationYawHead);
            sideX = Math.sin(yaw);
            sideZ = -Math.cos(yaw);
        } else {
            sideX /= length;
            sideZ /= length;
        }
        AirBlastVolley shared = new AirBlastVolley(group, sideX, sideZ, host.world.getTotalWorldTime());
        for (int i = 0; i < group.size(); i++) {
            EntityAIAirBlast member = group.get(i);
            member.target = target;
            member.volley = shared;
            member.participating = true;
            member.settled = false;
        }
    }

    @Override
    public boolean shouldContinueExecuting() {
        return volley != null && !volley.isFinished() && isValid();
    }

    @Override
    public void resetTask() {
        if (volley != null) volley.withdraw(this);
        running = false;
        participating = false;
        settled = false;
        host.setAirBlastTarget(null);
        host.setAirBlastInflation(0.0F);
        volley = null;
        target = null;
        host.getNavigator().clearPath();
        host.getMoveHelper().setMoveTo(host.posX, host.posY, host.posZ, 0.0D);
    }

    @Override
    public void updateTask() {
        if (volley == null || host.world.isRemote) return;
        volley.update(host.world.getTotalWorldTime());
        if (volley.isFinished() || !isValid()) return;
        host.getLookHelper().setLookPositionWithEntity(target, 45.0F, 45.0F);
        if (volley.isFiring() || (volley.isWindingUp() && canHit()) || isInPosition()) {
            // Keep the fly helper active at the current point to hover while blowing.
            host.getMoveHelper().setMoveTo(host.posX, host.posY, host.posZ, 0.0D);
            host.motionX *= 0.5D;
            host.motionY *= 0.5D;
            host.motionZ *= 0.5D;
        } else {
            Vec3d position = formationPosition();
            host.getMoveHelper().setMoveTo(position.x, position.y, position.z, 1.0D);
        }
    }

    private Vec3d formationPosition() {
        double angle = AirBlastVolley.formationAngle(volley.slotOf(this), volley.formationSize());
        double x = volley.sideX * Math.cos(angle) - volley.sideZ * Math.sin(angle);
        double z = volley.sideX * Math.sin(angle) + volley.sideZ * Math.cos(angle);
        return new Vec3d(target.posX + x * AirBlastVolley.FORMATION_RADIUS,
                target.posY + 0.25D, target.posZ + z * AirBlastVolley.FORMATION_RADIUS);
    }

    private List<EntityAirElemental> nearbyAllies() {
        return host.world.getEntitiesWithinAABB(EntityAirElemental.class, host.getEntityBoundingBox().grow(12.0D));
    }

    private boolean validTarget(EntityLivingBase entity) {
        return entity != null && !(entity instanceof EntityAirElemental) && entity.isEntityAlive() && entity.world == host.world
                && !(entity instanceof EntityPlayer && (((EntityPlayer) entity).isSpectator()
                || ((EntityPlayer) entity).capabilities.disableDamage));
    }

    @Override
    public boolean isValid() {
        return participating && host.isEntityAlive() && validTarget(target) && host.getAttackTarget() == target
                && volley != null && host.world.getEntityByID(host.getEntityId()) == host;
    }

    @Override
    public boolean isReady() {
        return running && host.getAirBlastCooldown() == 0;
    }

    @Override
    public boolean isInPosition() {
        Vec3d position = formationPosition();
        double dx = host.posX - target.posX, dz = host.posZ - target.posZ;
        double distanceSq = dx * dx + dz * dz;
        settled = AirBlastVolley.inFormation(distanceSq,
                host.getDistanceSq(position.x, position.y, position.z), settled) && canHit();
        return settled;
    }

    @Override
    public boolean canHit() {
        double dx = target.posX - host.posX, dz = target.posZ - host.posZ;
        double length = Math.sqrt(dx * dx + dz * dz);
        return length > 0.001D && host.getAirBlastOrigin().squareDistanceTo(targetPoint()) <= 100.0D
                && (-volley.sideX * dx - volley.sideZ * dz) / length >= 0.7D && hasClearTorrent();
    }

    private Vec3d targetPoint() {
        return new Vec3d(target.posX, target.posY + target.height * 0.6D, target.posZ);
    }

    private boolean hasClearTorrent() {
        return host.world.rayTraceBlocks(host.getAirBlastOrigin(), targetPoint(), false, true, false) == null;
    }

    @Override
    public void setInflation(float inflation) {
        host.setAirBlastInflation(inflation);
    }

    @Override
    public void begin() {
        // Eight seconds between starts, including the channel itself.
        host.setAirBlastCooldown(AirBlastVolley.COOLDOWN_TICKS);
        host.playSound(SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, 0.9F, 0.65F);
    }

    @Override
    public void showTorrent(boolean visible) {
        host.setAirBlastTarget(visible ? target : null);
    }

    @Override
    public void applyBlast(int contributors, double pushX, double pushZ, boolean damage) {
        DamageSource source = DamageSource.causeMobDamage(host);
        if (target.isEntityInvulnerable(source)) return;
        if (damage) target.attackEntityFrom(source, 1.0F);
        double resistance = target.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).getAttributeValue();
        double strength = AirBlastVolley.pushStrength(contributors, resistance, target.motionX * pushX + target.motionZ * pushZ);
        if (strength <= 0.0D) return;
        target.addVelocity(pushX * strength, Math.max(0.0D, Math.min(0.08D, 0.2D - target.motionY)), pushZ * strength);
        // Vanilla tracking synchronizes the resulting velocity, including to the hit player.
        target.velocityChanged = true;
    }

    @Override
    public void finish(boolean fired) {
        participating = false;
        host.setAirBlastTarget(null);
        host.setAirBlastInflation(0.0F);
        if (!fired) host.setAirBlastCooldown(Math.max(20, host.getAirBlastCooldown()));
    }
}

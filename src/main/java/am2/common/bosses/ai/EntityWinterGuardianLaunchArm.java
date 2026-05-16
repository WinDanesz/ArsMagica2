package am2.common.bosses.ai;

import am2.common.bosses.BossActions;
import am2.common.bosses.EntityWinterGuardian;
import am2.common.entity.EntityWinterGuardianArm;
import am2.common.registry.AMSounds;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;

import java.util.List;

public class EntityWinterGuardianLaunchArm extends EntityAIBase {
    private final EntityWinterGuardian host;
    private final float moveSpeed;
    private EntityLivingBase target;
    private int cooldownTicks = 0;

    public EntityWinterGuardianLaunchArm(EntityWinterGuardian host, float moveSpeed) {
        this.host = host;
        this.moveSpeed = moveSpeed;
        this.setMutexBits(1);
    }

    @Override
    public boolean shouldExecute() {
        if (cooldownTicks-- > 0 || host.getAttackTarget() == null) return false;
        if (host.getAttackTarget().getDistanceSq(host) > 16D) {
            target = host.getAttackTarget();
            return true;
        }
        List<EntityLivingBase> entities = host.world.getEntitiesWithinAABB(EntityLivingBase.class, host.getEntityBoundingBox().grow(20, 20, 20));
        if (!entities.isEmpty()) {
            for (EntityLivingBase entity : entities) {
                if (entity instanceof EntityPlayer && ((EntityPlayer) entity).capabilities.isCreativeMode) continue;
                if (entity.getDistanceSq(host) > 49D) {
                    target = entity;
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean shouldContinueExecuting() {
        if (target == null || target.isDead || host.getDistanceSq(target) < 49D || host.getDistanceSq(target) > 225D || (host.getCurrentAction() == BossActions.LAUNCHING && host.getTicksInCurrentAction() > host.getCurrentAction().getMaxActionTime())) {
            host.setCurrentAction(BossActions.IDLE);
            target = null;
            if (!host.hasLeftArm() && !host.hasRightArm())
                cooldownTicks = 20;
            return false;
        }
        return true;
    }

    @Override
    public void updateTask() {
        host.getLookHelper().setLookPositionWithEntity(target, 30, 30);
        if (host.getDistanceSq(target) > 144 && host.getCurrentAction() == BossActions.IDLE) {
            host.getNavigator().tryMoveToEntityLiving(target, moveSpeed);
        } else {
            host.getNavigator().clearPath();
            if (host.getCurrentAction() != BossActions.LAUNCHING)
                host.setCurrentAction(BossActions.LAUNCHING);

            if (host.getTicksInCurrentAction() == 14) {
                host.faceEntity(target, 180, 180);
                if (!host.world.isRemote) {

                    if (!host.world.isRemote)
                        host.world.playSound(host.posX, host.posY, host.posZ, AMSounds.WINTER_GUARDIAN_LAUNCH_ARM, SoundCategory.HOSTILE, 1.0f, 1.0f, false);

                    EntityWinterGuardianArm projectile = new EntityWinterGuardianArm(host.world, host, 1.25f);
                    projectile.setThrowingEntity(host);
                    projectile.setProjectileSpeed(2.0);
                    host.world.spawnEntity(projectile);
                }
            }
        }
    }
}

package am2.common.entity.ai;

import am2.common.entity.EntityNatureElemental;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;

public class EntityAINatureElementalCharge extends EntityAIBase {
    private final EntityNatureElemental host;
    private EntityLivingBase target;

    public EntityAINatureElementalCharge(EntityNatureElemental host) {
        this.host = host;
        setMutexBits(3);
    }

    @Override
    public boolean shouldExecute() {
        target = host.getAttackTarget();
        if (!validTarget() || host.getChargeCooldown() > 0 || !host.onGround
                || host.isInWater() || host.isInLava()) return false;
        double distance = host.getDistanceSq(target);
        return distance >= 16.0D && distance <= 144.0D
                && Math.abs(target.posY - host.posY) <= 1.5D
                && host.getEntitySenses().canSee(target);
    }

    private boolean validTarget() {
        return target != null && target.isEntityAlive() && target.world == host.world
                && target == host.getAttackTarget()
                && (!(target instanceof EntityPlayer)
                || !((EntityPlayer) target).isCreative() && !((EntityPlayer) target).isSpectator());
    }

    @Override
    public void startExecuting() {
        host.beginCharge(target);
        host.playSound(SoundEvents.ENTITY_PIG_AMBIENT, 1.0F, 0.6F);
    }

    @Override
    public boolean shouldContinueExecuting() {
        return validTarget() && host.isEntityAlive() && !host.isInWater() && !host.isInLava()
                && host.getCharge() != null && !host.getCharge().isFinished(host.world.getTotalWorldTime());
    }

    @Override
    public void updateTask() {
        host.getNavigator().clearPath();
        host.getMoveHelper().setMoveTo(host.posX, host.posY, host.posZ, 0.0D);
        NatureElementalCharge charge = host.getCharge();
        if (charge.isPreparing(host.world.getTotalWorldTime())) {
            host.getLookHelper().setLookPositionWithEntity(target, 180.0F, 180.0F);
        } else {
            host.getLookHelper().setLookPosition(host.posX + charge.getDirectionX() * 8.0D,
                    host.posY + host.getEyeHeight(), host.posZ + charge.getDirectionZ() * 8.0D, 180.0F, 180.0F);
        }
    }

    @Override
    public void resetTask() {
        host.endCharge();
        target = null;
    }
}

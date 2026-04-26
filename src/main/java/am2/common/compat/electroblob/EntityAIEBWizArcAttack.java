package am2.common.compat.electroblob;

import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.EnumHand;

/**
 * AI task that makes an {@link EntityLiving} cast the EBWiz {@code Arc} spell
 * at its attack target. Used by the Lightning Elemental when Electroblob's
 * Wizardry is present.
 *
 * <p>This class directly references EBWiz classes and must only be loaded when
 * EBWiz is confirmed present (via the compat bootstrap guard).
 */
public class EntityAIEBWizArcAttack extends EntityAIBase {

    private final EntityLiving host;
    private final int cooldown;
    /** Tick count (based on {@code host.ticksExisted}) when Arc was last cast. */
    private int lastCastTick = -10000;
    /** Set to {@code true} after the spell fires so the task yields control. */
    private boolean hasCast;

    public EntityAIEBWizArcAttack(EntityLiving host, int cooldown) {
        this.host = host;
        this.cooldown = cooldown;
        setMutexBits(3);
    }

    @Override
    public boolean shouldExecute() {
        EntityLivingBase target = host.getAttackTarget();
        if (target == null || !target.isEntityAlive()) return false;
        if ((host.ticksExisted - lastCastTick) < cooldown) return false;
        // Only activate when already in range and with line of sight so this
        // task does not steal movement control from the normal ranged AI.
        double distSq = host.getDistanceSq(target.posX, target.getEntityBoundingBox().minY, target.posZ);
        return distSq <= 225D && host.getEntitySenses().canSee(target);
    }

    @Override
    public boolean shouldContinueExecuting() {
        if (hasCast) return false;
        EntityLivingBase target = host.getAttackTarget();
        return target != null && target.isEntityAlive();
    }

    @Override
    public void startExecuting() {
        hasCast = false;
    }

    @Override
    public void resetTask() {
        hasCast = false;
    }

    @Override
    public void updateTask() {
        EntityLivingBase target = host.getAttackTarget();
        if (target == null) return;

        host.getLookHelper().setLookPositionWithEntity(target, 30F, 30F);
        host.faceEntity(target, 180, 180);

        double distSq = host.getDistanceSq(target.posX, target.getEntityBoundingBox().minY, target.posZ);
        boolean canSee = host.getEntitySenses().canSee(target);

        if (distSq <= 225D && canSee) {
            Spells.lightning_arrow.cast(host.world, host, EnumHand.MAIN_HAND, 0, target, new SpellModifiers());
            host.swingArm(EnumHand.MAIN_HAND);
            lastCastTick = host.ticksExisted;
            hasCast = true;
        }
    }
}

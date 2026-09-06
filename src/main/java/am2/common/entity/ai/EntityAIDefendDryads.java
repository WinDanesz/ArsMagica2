package am2.common.entity.ai;

import am2.common.entity.EntityDryad;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAITarget;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.List;

/**
 * Makes a druid retaliate against whatever last attacked a nearby dryad - the same shape as
 * {@link EntityAIDefendAffinityAlly} (which elementals use to defend friendly players), but
 * watching dryads instead. {@link EntityLivingBase#getRevengeTarget()} is generic to any living
 * entity, not player-only, so this works even though dryads have no attack AI of their own.
 */
public class EntityAIDefendDryads extends EntityAITarget {

    private static final double ALERT_PURSUIT_MULTIPLIER = 3.0D;

    private final double range;
    private EntityDryad defendedDryad;
    private EntityLivingBase attacker;
    private int timestamp;

    public EntityAIDefendDryads(EntityCreature creature, double range) {
        super(creature, false);
        this.range = range;
        this.setMutexBits(1);
    }

    @Override
    protected double getTargetDistance() {
        // After seeing a dryad attacked, continue the chase well beyond the normal follow range.
        return Math.max(super.getTargetDistance(), range * ALERT_PURSUIT_MULTIPLIER);
    }

    @Override
    public boolean shouldExecute() {
        AxisAlignedBB area = this.taskOwner.getEntityBoundingBox().grow(range);
        List<EntityDryad> nearby = this.taskOwner.world.getEntitiesWithinAABB(EntityDryad.class, area);
        for (EntityDryad dryad : nearby) {
            EntityLivingBase target = dryad.getRevengeTarget();
            if (target == null || target == this.taskOwner) continue;
            int revengeTimer = dryad.getRevengeTimer();
            if (dryad == this.defendedDryad && revengeTimer == this.timestamp) continue;
            if (!isSuitableTarget(target, false)) continue;
            this.defendedDryad = dryad;
            this.attacker = target;
            return true;
        }
        return false;
    }

    @Override
    public void startExecuting() {
        this.taskOwner.setAttackTarget(this.attacker);
        if (this.defendedDryad != null) {
            this.timestamp = this.defendedDryad.getRevengeTimer();
        }
        super.startExecuting();
    }
}

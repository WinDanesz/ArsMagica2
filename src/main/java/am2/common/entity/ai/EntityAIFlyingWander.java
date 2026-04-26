package am2.common.entity.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityFlyHelper;

/**
 * A wander AI for flying entities that use {@link EntityFlyHelper}.
 * Picks a random 3D destination, feeds it to the fly helper every tick until
 * reached, then idles briefly before picking the next one.
 * This avoids the jitter caused by picking a new destination every tick
 * (EntityFlyHelper resets isUpdating to false immediately on each tick).
 */
public class EntityAIFlyingWander extends EntityAIBase {

    private final EntityCreature entity;
    private final double speed;
    private final float wanderRadius;

    private double targetX, targetY, targetZ;
    private boolean hasTarget;
    private int idleTimer;

    /** Ticks to wait at a destination before picking the next one. */
    private static final int IDLE_TICKS = 40;
    /** Squared distance at which a destination is considered "reached". */
    private static final double ARRIVED_SQ = 9.0;

    public EntityAIFlyingWander(EntityCreature entity, double speed, float wanderRadius) {
        this.entity = entity;
        this.speed = speed;
        this.wanderRadius = wanderRadius;
        this.setMutexBits(1);
    }

    @Override
    public boolean shouldExecute() {
        return getFlyHelper() != null;
    }

    @Override
    public boolean shouldContinueExecuting() {
        return getFlyHelper() != null;
    }

    @Override
    public void updateTask() {
        if (entity.world.isRemote) return;
        EntityFlyHelper mh = getFlyHelper();
        if (mh == null) return;

        if (hasTarget) {
            double distSq = entity.getDistanceSq(targetX, targetY, targetZ);
            if (distSq < ARRIVED_SQ) {
                // Reached — start idle countdown
                hasTarget = false;
                idleTimer = IDLE_TICKS;
            } else {
                // Keep feeding the destination every tick so the helper stays on course
                mh.setMoveTo(targetX, targetY, targetZ, speed);
            }
        } else {
            if (idleTimer > 0) {
                idleTimer--;
            } else {
                pickNewDestination();
            }
        }
    }

    private void pickNewDestination() {
        double x = entity.posX + (entity.getRNG().nextFloat() * 2.0F - 1.0F) * wanderRadius;
        double z = entity.posZ + (entity.getRNG().nextFloat() * 2.0F - 1.0F) * wanderRadius;
        double groundY = entity.world.getHeight((int) x, (int) z);
        double minY = groundY + 2.0;
        double maxY = groundY + 10.0;
        double y = minY + entity.getRNG().nextFloat() * (maxY - minY);
        y = Math.max(2.0, Math.min(entity.world.getActualHeight() - 4, y));

        targetX = x;
        targetY = y;
        targetZ = z;
        hasTarget = true;
    }

    private EntityFlyHelper getFlyHelper() {
        if (entity.getMoveHelper() instanceof EntityFlyHelper)
            return (EntityFlyHelper) entity.getMoveHelper();
        return null;
    }
}


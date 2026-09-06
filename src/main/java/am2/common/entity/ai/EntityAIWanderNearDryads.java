package am2.common.entity.ai;

import am2.common.entity.EntityDryad;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;

/** Keeps ordinary wandering loosely centered on nearby dryads. */
public class EntityAIWanderNearDryads extends EntityAIWander {

    private static final double SEARCH_RADIUS = 24.0D;
    private static final double CLOSE_DISTANCE_SQ = 16.0D;

    public EntityAIWanderNearDryads(EntityCreature creature, double speed) {
        super(creature, speed);
    }

    @Nullable
    @Override
    protected Vec3d getPosition() {
        // Retain occasional independent strolls, using vanilla's normal wander cadence.
        if (this.entity.getRNG().nextInt(4) == 0) {
            return super.getPosition();
        }

        EntityDryad nearest = null;
        double nearestDistanceSq = SEARCH_RADIUS * SEARCH_RADIUS;
        for (EntityDryad dryad : this.entity.world.getEntitiesWithinAABB(EntityDryad.class,
                this.entity.getEntityBoundingBox().grow(SEARCH_RADIUS))) {
            double distanceSq = this.entity.getDistanceSq(dryad);
            if (dryad.isEntityAlive() && distanceSq < nearestDistanceSq) {
                nearest = dryad;
                nearestDistanceSq = distanceSq;
            }
        }

        if (nearest != null) {
            if (nearestDistanceSq <= CLOSE_DISTANCE_SQ) {
                // Leave some room around the dryad instead of walking into its position.
                Vec3d position = RandomPositionGenerator.findRandomTarget(this.entity, 3, 2);
                if (position != null) {
                    return position;
                }
            } else {
                for (int attempt = 0; attempt < 3; attempt++) {
                    Vec3d position = RandomPositionGenerator.findRandomTargetBlockTowards(
                            this.entity, 10, 7, nearest.getPositionVector());
                    if (position != null && position.squareDistanceTo(nearest.getPositionVector()) < nearestDistanceSq) {
                        return position;
                    }
                }
            }
        }

        // No nearby dryad or suitable approach: allow normal wandering around obstacles.
        return super.getPosition();
    }
}

package am2.common.entity.ai;

import am2.common.extensions.EntityExtension;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAITarget;
import net.minecraft.pathfinding.Path;

import java.util.List;

public class EntityAITargetNearbyInanimate extends EntityAITarget {

    private float targetDistance;

    @SafeVarargs
    public EntityAITargetNearbyInanimate(EntityCreature taskOwner, float targetDistance, boolean needsLineofSight, Class<? extends Entity>... classes) {
        super(taskOwner, needsLineofSight);
        targetTypes = classes;
        this.targetDistance = targetDistance;
    }

    private Entity target;
    private int timeSinceLastSight;
    private Class<? extends Entity>[] targetTypes;

    @Override
    public boolean shouldExecute() {
        boolean attackTargetNull = taskOwner.getAttackTarget() == null;
        boolean inanimateTargetNull = EntityExtension.For(taskOwner).getInanimateTarget() == null;
        boolean result = attackTargetNull && inanimateTargetNull;
        return result;
    }

    @Override
    public boolean shouldContinueExecuting() {
        // Target tasks should only run once to find and set a target,
        // then let action tasks (like EntityAIPickup) take over
        return false;
    }

    @Override
    public void resetTask() {
        EntityExtension.For(taskOwner).setInanimateTarget(null);
        this.target = null;
    }

    protected boolean isSuitableTarget(Entity target) {
        if (target.isDead) return false;
        for (Class<? extends Entity> c : targetTypes)
            if (target.getClass() == c) return true;
        return false;
    }

    @Override
    public void startExecuting() {
        double dist = 10000;
        for (Class<? extends Entity> c : targetTypes) {
            // Use targetDistance for vertical search too - items can be above/below the broom
            List<Entity> potentialTargets = taskOwner.world.getEntitiesWithinAABB(c, taskOwner.getEntityBoundingBox().grow(targetDistance, targetDistance, targetDistance));

            for (Entity e : potentialTargets) {
                if (isSuitableTarget(e)) { //sanity check
                    Path pe = taskOwner.getNavigator().getPathToXYZ(e.posX, e.posY, e.posZ); //can we get to the item?
                    if (pe != null) {
                        double eDist = taskOwner.getDistanceSq(e);
                        if (eDist < dist) {
                            this.target = e;
                            dist = eDist;
                        }
                    }
                }
            }
        }
        if (this.target != null) {
            EntityExtension.For(taskOwner).setInanimateTarget(this.target);
        } else {
        }
        super.startExecuting();
    }

}

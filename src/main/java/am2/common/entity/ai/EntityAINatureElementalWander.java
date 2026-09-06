package am2.common.entity.ai;

import am2.common.entity.EntityNatureElemental;
import net.minecraft.entity.ai.EntityAIWanderAvoidWater;

/** Short walks separated by rests, during which the boar can sniff its surroundings. */
public class EntityAINatureElementalWander extends EntityAIWanderAvoidWater {
    private final EntityNatureElemental host;
    private long nextWalkTick;

    public EntityAINatureElementalWander(EntityNatureElemental host) {
        super(host, 0.8D);
        this.host = host;
        scheduleNextWalk();
    }

    private void scheduleNextWalk() {
        nextWalkTick = host.world.getTotalWorldTime() + 40 + host.getRNG().nextInt(61);
    }

    @Override
    public boolean shouldExecute() {
        if (!host.canPerformIdleActions() || host.isSniffing()
                || host.world.getTotalWorldTime() < nextWalkTick) return false;
        // This task supplies its own rest timer instead of vanilla's random/idle-age checks.
        makeUpdate();
        if (super.shouldExecute()) return true;
        scheduleNextWalk();
        return false;
    }

    @Override
    public boolean shouldContinueExecuting() {
        return host.canPerformIdleActions() && !host.isSniffing() && super.shouldContinueExecuting();
    }

    @Override
    public void resetTask() {
        super.resetTask();
        host.getNavigator().clearPath();
        scheduleNextWalk();
    }
}

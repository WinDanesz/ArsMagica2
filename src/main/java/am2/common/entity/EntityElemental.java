package am2.common.entity;

import am2.api.affinity.Affinity;
import am2.common.entity.ai.EntityAIDefendAffinityAlly;
import am2.common.entity.ai.selectors.AffinityFriendSelector;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

/**
 * Common base for the classic elementals.
 */
public abstract class EntityElemental extends EntityMob {

    /** Affinity depth (0-1) at which the elemental stops treating the player as prey. */
    public static final double NEUTRAL_AFFINITY_DEPTH = 0.75D;
    /** Affinity depth (0-1) at which the elemental actively defends the player in combat. */
    public static final double FRIENDLY_AFFINITY_DEPTH = 1.0D;

    private final Affinity affinity;

    protected EntityElemental(World world, Affinity affinity) {
        super(world);
        this.affinity = affinity;
    }

    public Affinity getElementalAffinity() {
        return affinity;
    }

    /**
     * Adds the shared target tasks: self-defense (priority 1), defending a nearby player who
     * is fully friendly ({@link #FRIENDLY_AFFINITY_DEPTH}) and just got hurt (priority 2), and
     * hunting down any other nearby player (priority 3, excluding players at or above
     * {@link #NEUTRAL_AFFINITY_DEPTH}, who are left alone but not actively defended).
     *
     * @param defendRange radius, in blocks, to look for a friendly player under attack
     */
    protected void initAffinityTargeting(double defendRange) {
        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
        this.targetTasks.addTask(2, new EntityAIDefendAffinityAlly(this, affinity, defendRange));
        this.targetTasks.addTask(3, new EntityAINearestAttackableTarget<>(this, EntityPlayer.class, 0, true, false, new AffinityFriendSelector(affinity)));
    }
}

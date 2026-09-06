package am2.common.entity.ai;

import am2.api.affinity.Affinity;
import am2.common.entity.EntityElemental;
import am2.common.extensions.AffinityData;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAITarget;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.List;

/**
 * Makes a friendly elemental retaliate against whatever just attacked a nearby player who is
 * fully friendly ({@link EntityElemental#FRIENDLY_AFFINITY_DEPTH} in the elemental's matching
 * affinity) - the same shape as vanilla's {@code EntityAIOwnerHurtByTarget} (which wolves use
 * to defend their tamed owner), but checking every nearby player against the affinity depth
 * instead of a single owner. A merely neutral player (at or above
 * {@link EntityElemental#NEUTRAL_AFFINITY_DEPTH} but below full) is left alone but not
 * actively defended.
 */
public class EntityAIDefendAffinityAlly extends EntityAITarget {

    private final Affinity affinity;
    private final double range;
    private EntityPlayer defendedPlayer;
    private EntityLivingBase attacker;
    private int timestamp;

    public EntityAIDefendAffinityAlly(EntityCreature creature, Affinity affinity, double range) {
        super(creature, false);
        this.affinity = affinity;
        this.range = range;
        this.setMutexBits(1);
    }

    @Override
    public boolean shouldExecute() {
        AxisAlignedBB area = this.taskOwner.getEntityBoundingBox().grow(range);
        List<EntityPlayer> nearby = this.taskOwner.world.getEntitiesWithinAABB(EntityPlayer.class, area);
        for (EntityPlayer player : nearby) {
            if (AffinityData.For(player).getAffinityDepth(affinity) < EntityElemental.FRIENDLY_AFFINITY_DEPTH) continue;
            EntityLivingBase target = player.getRevengeTarget();
            if (target == this.taskOwner) continue;
            int revengeTimer = player.getRevengeTimer();
            if (player == this.defendedPlayer && revengeTimer == this.timestamp) continue;
            if (!isSuitableTarget(target, false)) continue;
            this.defendedPlayer = player;
            this.attacker = target;
            return true;
        }
        return false;
    }

    @Override
    public void startExecuting() {
        this.taskOwner.setAttackTarget(this.attacker);
        if (this.defendedPlayer != null) {
            this.timestamp = this.defendedPlayer.getRevengeTimer();
        }
        super.startExecuting();
    }
}

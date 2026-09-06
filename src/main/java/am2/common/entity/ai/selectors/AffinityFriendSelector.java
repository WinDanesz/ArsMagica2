package am2.common.entity.ai.selectors;

import am2.api.affinity.Affinity;
import am2.common.entity.EntityElemental;
import am2.common.extensions.AffinityData;
import com.google.common.base.Predicate;
import net.minecraft.entity.player.EntityPlayer;

/**
 * Target selector for elementals: excludes players at or above
 * {@link EntityElemental#NEUTRAL_AFFINITY_DEPTH} in the elemental's matching affinity, so a
 * neutral or fully-friendly player is no longer picked as an initial target. Does not affect
 * retaliation - a player who attacks the elemental is still fought back via the elemental's
 * existing {@code EntityAIHurtByTarget} task.
 */
public class AffinityFriendSelector implements Predicate<EntityPlayer> {

    private final Affinity affinity;

    public AffinityFriendSelector(Affinity affinity) {
        this.affinity = affinity;
    }

    @Override
    public boolean apply(EntityPlayer player) {
        return player == null || AffinityData.For(player).getAffinityDepth(affinity) < EntityElemental.NEUTRAL_AFFINITY_DEPTH;
    }
}

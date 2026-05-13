package am2.common.entity.ai.selectors;

import am2.common.utils.EntityUtils;
import com.google.common.base.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.monster.IMob;

/**
 * Target selector for player-faction AM2 summons.
 *
 * <p>When Electroblob's Wizardry is present, {@link #ebwizValidator} is set by
 * {@code EBWizardryCompatBootstrap} and delegates target validity to
 * {@code AllyDesignationSystem.isValidTarget}, giving summons full awareness of
 * the EBWiz ally system. Without EBWiz the fallback restricts targets to
 * {@link IMob} implementors and excludes the owner and other AM2 summons.
 */
public class SummonEntitySelector implements Predicate<EntityLivingBase> {

    /**
     * Set by {@code EBWizardryCompatBootstrap.register()} when EBWiz is loaded.
     * Signature mirrors {@code AllyDesignationSystem.isValidTarget(attacker, target)}.
     */
    public static java.util.function.BiPredicate<Entity, Entity> ebwizValidator = null;

    private final EntityLivingBase owner;

    public SummonEntitySelector(EntityLivingBase owner) {
        this.owner = owner;
    }

    @Override
    public boolean apply(EntityLivingBase entity) {
        if (entity == null || owner == null || entity == owner) return false;

        // Exclude other summons of the same owner
        if (EntityUtils.isSummon(entity) && EntityUtils.getOwner(entity) == owner.getEntityId()) return false;

        if (entity.isInvisible()) return false;

        if (entity instanceof IEntityOwnable && owner.equals(((IEntityOwnable) entity).getOwner())) return false;

        // Base filter: only consider hostile mobs or summons as attack candidates.
        // This must run before the EBWiz validator so passive animals (chickens, cows, etc.)
        // are never targeted — AllyDesignationSystem.isValidTarget only tracks ally
        // relationships and returns true for any non-ally, including passive mobs.
        boolean isHostile = entity instanceof IMob;
        boolean isAnySummon = EntityUtils.isSummon(entity);
        if (!isHostile && !isAnySummon) return false;

        // Use the AllyDesignation System from EBWiz if available, to allow summons to properly target allies and enemies
        if (ebwizValidator != null) {
            return ebwizValidator.test(owner, entity);
        }

        // Fallback without EBWiz: only attack hostile mobs; skip AM2 summons
        return isHostile && !isAnySummon;
    }
}

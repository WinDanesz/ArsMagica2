package am2.common.entity.ai;

import am2.common.utils.EntityUtils;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIHurtByTarget;

public class EntityAIHurtByTargetExcludeOwner extends EntityAIHurtByTarget {

    private final EntityCreature creature;

    public EntityAIHurtByTargetExcludeOwner(EntityCreature creature, boolean callForHelp) {
        super(creature, callForHelp);
        this.creature = creature;
    }

    @Override
    public boolean shouldExecute() {
        if (!super.shouldExecute()) return false;
        EntityLivingBase attacker = creature.getRevengeTarget();
        if (attacker == null) return true;
        int ownerId = EntityUtils.getOwner(creature);
        return ownerId < 0 || attacker.getEntityId() != ownerId;
    }
}

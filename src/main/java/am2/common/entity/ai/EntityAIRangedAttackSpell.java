package am2.common.entity.ai;

import am2.api.extensions.IEntityExtension;
import am2.api.extensions.ISpellCaster;
import am2.common.extensions.EntityExtension;
import am2.common.spell.SpellCaster;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityFlyHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EntityAIRangedAttackSpell extends EntityAIBase {
    private static final Logger LOGGER = LogManager.getLogger("AM2-SpellAI");
    World world;

    /**
     * The entity the AI instance has been applied to
     */
    EntityCreature entityHost;
    EntityLivingBase attackTarget;

    /**
     * A decrementing tick that spawns a ranged attack once this value reaches 0. It is then set back to the
     * maxRangedAttackTime.
     */
    int rangedAttackTime;
    float field_48370_e;
    int field_48367_f;

    /**
     * The maximum time the AI has to wait before peforming another ranged attack.
     */
    int maxRangedAttackTime;

    private final ItemStack[] spellStacks;

    public EntityAIRangedAttackSpell(EntityCreature host, float moveSpeed, int cooldown, ItemStack... spellStacks) {
        if (spellStacks.length == 0) {
            throw new IllegalArgumentException("An attack spell is required");
        }
        rangedAttackTime = 0;
        field_48367_f = 0;
        entityHost = host;
        world = host.world;
        field_48370_e = moveSpeed;
        maxRangedAttackTime = cooldown;
        this.spellStacks = spellStacks.clone();
        setMutexBits(3);
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    @Override
    public boolean shouldExecute() {
        if (entityHost == null)
            return false;
        EntityLivingBase entityliving = entityHost.getAttackTarget();

        if (entityliving == null) {
            return false;
        } else if (!canAffordAnySpell()) {
            // Release the movement/look mutex so a lower-priority melee AI can take over
            // instead of the host standing there failing to cast every cooldown forever..
            return false;
        } else {
            attackTarget = entityliving;
            return true;
        }
    }

    private boolean canAfford(IEntityExtension ext, ItemStack spellStack) {
        ISpellCaster caster = spellStack.getCapability(SpellCaster.INSTANCE, null);
        return caster != null && ext.hasEnoughMana(caster.getManaCost(world, entityHost));
    }

    private boolean canAffordAnySpell() {
        IEntityExtension ext = EntityExtension.For(entityHost);
        for (ItemStack spellStack : spellStacks) {
            if (canAfford(ext, spellStack)) return true;
        }
        return false;
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    @Override
    public boolean shouldContinueExecuting() {
        return shouldExecute();
    }

    /**
     * Resets the task
     */
    @Override
    public void resetTask() {
        attackTarget = null;
    }

    /**
     * Updates the task
     */
    @Override
    public void updateTask() {
        double d = 225D;
        double d1 = entityHost.getDistanceSq(attackTarget.posX, attackTarget.getEntityBoundingBox().minY, attackTarget.posZ);
        boolean flag = entityHost.getEntitySenses().canSee(attackTarget);

        if (flag) {
            field_48367_f++;
        } else {
            field_48367_f = 0;
        }

        if (d1 > d || field_48367_f > 20) {
            double deltaZ = attackTarget.posZ - entityHost.posZ;
            double deltaX = attackTarget.posX - entityHost.posX;

            // Normalize direction from entity to target, then stop 6 blocks short of the target
            double dist = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
            double nx = dist > 0.001 ? deltaX / dist : 0;
            double nz = dist > 0.001 ? deltaZ / dist : 0;
            double newX = attackTarget.posX - nx * 6;
            double newZ = attackTarget.posZ - nz * 6;

            // Use fly helper if available, otherwise fall back to navigator
            if (entityHost.getMoveHelper() instanceof EntityFlyHelper) {
                // Hover 4 blocks above the target so the elemental stays airborne
                double hoverY = attackTarget.posY + 2.0;
                ((EntityFlyHelper) entityHost.getMoveHelper()).setMoveTo(newX, hoverY, newZ, field_48370_e);
            } else {
                entityHost.getNavigator().tryMoveToXYZ(newX, attackTarget.posY, newZ, field_48370_e);
            }
        } else {
            // In range — stop moving. For fly helper, hold position by zeroing motion;
            // for navigator, clear the path.
            if (entityHost.getMoveHelper() instanceof EntityFlyHelper) {
                entityHost.motionX *= 0.5;
                entityHost.motionZ *= 0.5;
            } else {
                entityHost.getNavigator().clearPath();
            }
        }

        entityHost.getLookHelper().setLookPositionWithEntity(attackTarget, 30F, 30F);

        rangedAttackTime = Math.max(rangedAttackTime - 1, 0);

        if (rangedAttackTime > 0) {
            return;
        }

        if (d1 > d || !flag) {
            return;
        } else {
            doRangedAttack();
            rangedAttackTime = maxRangedAttackTime;
            return;
        }
    }

    /**
     * Performs a ranged attack according to the AI's rangedAttackID.
     */
    private void doRangedAttack() {
        if (entityHost.getAttackTarget() == null)
            return;
        entityHost.faceEntity(entityHost.getAttackTarget(), 180, 180);

        IEntityExtension ext = EntityExtension.For(entityHost);
        float manaBefore = ext.getCurrentMana();
        float manaMax = ext.getMaxMana();
        LOGGER.info("[{}] PRE-CAST  mana: {}/{}", entityHost.getName(), manaBefore, manaMax);

        ItemStack spellStack = chooseSpell();
        ISpellCaster caster = spellStack.getCapability(SpellCaster.INSTANCE, null);
        boolean success = caster != null && caster.cast(spellStack, world, entityHost);

        float manaAfter = ext.getCurrentMana();
        LOGGER.info("[{}] POST-CAST mana: {}/{}  (cast={})", entityHost.getName(), manaAfter, manaMax, success);

        onSpellCast(spellStack, success);

        if (success) {
            entityHost.swingArm(EnumHand.MAIN_HAND);
        }
    }

    protected ItemStack chooseSpell() {
        IEntityExtension ext = EntityExtension.For(entityHost);
        int affordableCount = 0;
        for (ItemStack spellStack : spellStacks) {
            if (canAfford(ext, spellStack)) affordableCount++;
        }
        if (affordableCount == 0) {
        return spellStacks[entityHost.getRNG().nextInt(spellStacks.length)];
    }
        int choice = entityHost.getRNG().nextInt(affordableCount);
        for (ItemStack spellStack : spellStacks) {
            if (canAfford(ext, spellStack)) {
                if (choice == 0) return spellStack;
                choice--;
            }
        }
        return spellStacks[0];
    }

    /** Called after the selected spell has been resolved. */
    protected void onSpellCast(ItemStack spellStack, boolean success) {
    }
}

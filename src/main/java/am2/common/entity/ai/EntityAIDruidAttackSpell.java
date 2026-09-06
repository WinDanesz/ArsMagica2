package am2.common.entity.ai;

import am2.common.entity.EntityDruid;
import am2.common.registry.AMPotions;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;

/** Selects a support summon only while the druid has no nearby wolf companion. */
public class EntityAIDruidAttackSpell extends EntityAIRangedAttackSpell {

    private static final double WOLF_SEARCH_RADIUS = 24.0D;

    private final EntityDruid druid;
    private final ItemStack rockThrow;
    private final ItemStack rootWave;
    private final ItemStack wolfSummon;

    public EntityAIDruidAttackSpell(EntityDruid druid, float moveSpeed, int cooldown,
                                    ItemStack rockThrow, ItemStack rootWave, ItemStack wolfSummon) {
        super(druid, moveSpeed, cooldown, rockThrow, rootWave, wolfSummon);
        this.druid = druid;
        this.rockThrow = rockThrow;
        this.rootWave = rootWave;
        this.wolfSummon = wolfSummon;
    }

    @Override
    protected ItemStack chooseSpell() {
        if (!hasNearbyWolf() && druid.getRNG().nextInt(4) == 0) {
            return wolfSummon;
        }
        return druid.getRNG().nextBoolean() ? rockThrow : rootWave;
    }

    @Override
    protected void onSpellCast(ItemStack spellStack, boolean success) {
        if (success && spellStack == rootWave && druid.getAttackTarget() != null) {
            // Wave supplies the travelling roots, but only applies its components to ground.
            // Root the intended target here as the wave is released.
            druid.getAttackTarget().addPotionEffect(new PotionEffect(AMPotions.entangle, 100, 0));
        }
    }

    private boolean hasNearbyWolf() {
        return !druid.world.getEntitiesWithinAABB(EntityWolf.class,
                druid.getEntityBoundingBox().grow(WOLF_SEARCH_RADIUS)).isEmpty();
    }
}

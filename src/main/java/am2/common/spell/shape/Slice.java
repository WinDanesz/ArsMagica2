package am2.common.spell.shape;

import am2.api.spell.Operation;
import am2.api.spell.SpellData;
import am2.api.spell.SpellModifiers;
import am2.api.spell.SpellShape;
import am2.common.entity.EntitySpellSlice;
import am2.common.registry.AMItems;
import am2.common.spell.SpellCastResult;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import java.util.EnumSet;

/**
 * Slice – a wide, flat spell shape that hurls a spinning disc toward where
 * the caster is looking.  The disc passes through every entity it touches
 * (applying the spell to each one) and only despawns when it strikes solid
 * terrain or its maximum lifetime is reached.
 */
public class Slice extends SpellShape {

    private static final double DEFAULT_SPEED    = 0.4;
    private static final int    DEFAULT_LIFETIME = 60;   // ticks (= 3 seconds)

    @Override
    public Object[] getRecipe() {
        return new Object[]{
                new ItemStack(AMItems.vinteum_dust),
                new ItemStack(AMItems.arcane_ash),
                "E:*", 20000
        };
    }



    @Override
    public float manaCostMultiplier() {
        return 2.0F;
    }

    @Override
    public boolean isTerminusShape() {
        return false;
    }

    @Override
    public boolean isPrincipumShape() {
        return true;
    }

    @Override
    public SpellCastResult beginStackStage(SpellData spell, EntityLivingBase caster,
                                           EntityLivingBase target, World world,
                                           double x, double y, double z,
                                           EnumFacing side, boolean giveXP, int useCount) {
        if (!world.isRemote) {
            double speed    = spell.getModifiedValue(DEFAULT_SPEED,    SpellModifiers.SPEED,    Operation.ADD,      world, caster, target);
            int    lifetime = (int) spell.getModifiedValue(DEFAULT_LIFETIME, SpellModifiers.DURATION, Operation.MULTIPLY, world, caster, target);
            int    color    = spell.getColor(world, caster, target);

            EntitySpellSlice slice = new EntitySpellSlice(world);

            // Spawn slightly in front of the caster's eyes so it doesn't clip through them
            double eyeX = caster.posX + caster.getLookVec().x * 0.8;
            double eyeY = caster.posY + caster.getEyeHeight() + caster.getLookVec().y * 0.8 - 0.125;
            double eyeZ = caster.posZ + caster.getLookVec().z * 0.8;
            slice.setPosition(eyeX, eyeY, eyeZ);

            slice.motionX = caster.getLookVec().x * speed;
            slice.motionY = caster.getLookVec().y * speed;
            slice.motionZ = caster.getLookVec().z * speed;

            slice.setShooter(caster);
            slice.setSpell(spell);
            slice.setMaxTicks(lifetime);

            if (color != -1) {
                slice.setColor(color);
            } else {
                // Fall back to the spell's primary affinity colour so it's never plain white
                slice.setColor(spell.getMainShift().getColor());
            }

            world.spawnEntity(slice);
        }

        return SpellCastResult.SUCCESS;
    }

    @Override
    public EnumSet<SpellModifiers> getModifiers() {
        return EnumSet.of(SpellModifiers.SPEED, SpellModifiers.DURATION, SpellModifiers.COLOR);
    }
}

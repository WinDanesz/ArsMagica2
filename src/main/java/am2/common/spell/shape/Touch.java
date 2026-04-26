package am2.common.spell.shape;

import am2.api.spell.SpellData;
import am2.api.spell.SpellModifiers;
import am2.api.spell.SpellShape;
import am2.common.registry.AMItems;
import am2.common.spell.SpellCastResult;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import java.util.EnumSet;

public class Touch extends SpellShape {

    @Override
    public SpellCastResult beginStackStage(SpellData spell, EntityLivingBase caster, EntityLivingBase target, World world, double x, double y, double z, EnumFacing side, boolean giveXP, int useCount) {
        if (target != null) {
            SpellCastResult result = spell.applyComponentsToEntity(world, caster, target);
            if (result != SpellCastResult.SUCCESS) {
                return result;
            }
            return spell.execute(world, caster, target, target.posX, target.posY, target.posZ, null);
        }

        boolean targetWater = spell.isModifierPresent(SpellModifiers.TARGET_NONSOLID_BLOCKS);
        RayTraceResult mop = spell.raytrace(caster, world, 2.5f, true, targetWater);
        if (mop == null) {
            return SpellCastResult.EFFECT_FAILED;
        } else {
            if (mop.typeOfHit == RayTraceResult.Type.ENTITY) {
                Entity e = mop.entityHit;
                SpellCastResult result = spell.applyComponentsToEntity(world, caster, e);
                if (result != SpellCastResult.SUCCESS) {
                    return result;
                }
                return spell.execute(world, caster, target, mop.hitVec.x, mop.hitVec.y, mop.hitVec.z, null);
            } else {
                SpellCastResult result = spell.applyComponentsToGround(world, caster, mop.getBlockPos(), mop.sideHit, mop.hitVec.x, mop.hitVec.y, mop.hitVec.z);
                if (result != SpellCastResult.SUCCESS) {
                    return result;
                }
                return spell.execute(world, caster, target, mop.getBlockPos().getX(), mop.getBlockPos().getY(), mop.getBlockPos().getZ(), mop.sideHit);
            }
        }
    }

    @Override
    public EnumSet<SpellModifiers> getModifiers() {
        return EnumSet.of(SpellModifiers.TARGET_NONSOLID_BLOCKS);
    }




    @Override
    public Object[] getRecipe() {
        return new Object[]{
                new ItemStack(AMItems.vinteum_dust),
                Items.FEATHER,
                Items.FISH,
                Items.CLAY_BALL
        };
    }

    @Override
    public float manaCostMultiplier() {
        return 1;
    }

    @Override
    public boolean isTerminusShape() {
        return false;
    }

    @Override
    public boolean isPrincipumShape() {
        return false;
    }
}

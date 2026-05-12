package am2.common.spell.shape;

import am2.ArsMagica;
import am2.api.affinity.Affinity;
import am2.api.spell.Operation;
import am2.api.spell.SpellData;
import am2.api.spell.SpellModifiers;
import am2.api.spell.SpellShape;
import am2.common.registry.AMItems;
import am2.common.registry.Affinities;
import am2.common.spell.SpellCastResult;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class Chain extends SpellShape {

    @Override
    public SpellCastResult beginStackStage(SpellData spell, EntityLivingBase caster, EntityLivingBase target, World world, double x, double y, double z, EnumFacing side, boolean giveXP, int useCount) {

        RayTraceResult mop = spell.raytrace(caster, world, 8.0f, true, false);
        double range = spell.getModifiedValue(8, SpellModifiers.RANGE, Operation.MULTIPLY, world, caster, target);//SpellUtils.getModifiedDouble_Mul(8, stack, caster, target, world, SpellModifiers.RANGE);
        int num_targets = (int) spell.getModifiedValue(3, SpellModifiers.PROCS, Operation.ADD, world, caster, target);//SpellUtils.getModifiedInt_Add(3, stack, caster, target, world, SpellModifiers.PROCS);

        ArrayList<EntityLivingBase> targets = new ArrayList<EntityLivingBase>();

        if (target != null) {
            mop = new RayTraceResult(target);
        }

        if (mop != null && mop.typeOfHit == RayTraceResult.Type.ENTITY && mop.entityHit != null) {
            Entity e = mop.entityHit;
            if (e instanceof EntityLivingBase) {
                do {
                    targets.add((EntityLivingBase) e);

                    List<EntityLivingBase> nearby = world.getEntitiesWithinAABB(EntityLivingBase.class, e.getEntityBoundingBox().expand(range, range, range));
                    EntityLivingBase closest = null;
                    for (EntityLivingBase near : nearby) {
                        if (targets.contains(near) || near == caster) continue;

                        if (closest == null || closest.getDistanceSq(e) > near.getDistanceSq(e)) {
                            closest = near;
                        }
                    }

                    e = closest;

                } while (e != null && targets.size() < num_targets);
            }
        }

        boolean atLeastOneApplication = false;
        SpellCastResult result = SpellCastResult.SUCCESS;

        EntityLivingBase prevEntity = null;

        for (EntityLivingBase e : targets) {
            if (e == caster)
                continue;
            result = spell.applyComponentsToEntity(world, caster, e);

            if (world.isRemote) {
                if (prevEntity == null)
                    spawnChainParticles(world, x, y, z, e.posX, e.posY + e.getEyeHeight(), e.posZ, spell);
                else
                    spawnChainParticles(world, prevEntity.posX, prevEntity.posY + e.getEyeHeight(), prevEntity.posZ, e.posX, e.posY + e.getEyeHeight(), e.posZ, spell);
            }
            prevEntity = e;

            if (result == SpellCastResult.SUCCESS) {
                atLeastOneApplication = true;
            }
        }

        if (atLeastOneApplication) {
            return SpellCastResult.SUCCESS;
        }
        return result;
    }

    @Override
    public EnumSet<SpellModifiers> getModifiers() {
        return EnumSet.of(SpellModifiers.RANGE, SpellModifiers.PROCS);
    }


    private void spawnChainParticles(World world, double startX, double startY, double startZ, double endX, double endY, double endZ, SpellData spell) {
        int color = spell.getColor(world, null, null);

        Affinity aff = spell.getMainShift();

        if (aff.equals(Affinities.lightning)) {
            ArsMagica.proxy.particleManager.BoltFromPointToPoint(world, startX, startY, startZ, endX, endY, endZ, 1, color);
        } else {
            if (color == -1)
                color = aff.getColor();
            ArsMagica.proxy.particleManager.BeamFromPointToPoint(world, startX, startY, startZ, endX, endY, endZ, color);
        }
    }



    @Override
    public Object[] getRecipe() {
        return new Object[]{
                new ItemStack(AMItems.sunstone),
                Items.LEAD,
                Items.IRON_INGOT,
                Blocks.TRIPWIRE_HOOK,
                Items.STRING
        };
    }

    @Override
    public float manaCostMultiplier() {
        return 1.5f;
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

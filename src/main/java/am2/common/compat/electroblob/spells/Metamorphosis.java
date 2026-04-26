package am2.common.compat.electroblob.spells;

import am2.api.affinity.Affinity;
import am2.api.spell.SpellComponent;
import am2.api.spell.SpellData;
import am2.api.spell.SpellModifiers;
import am2.common.LogHelper;
import am2.common.registry.Affinities;
import com.google.common.collect.Sets;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

/**
 * Spell component that transforms a target mob into another form using
 * Electroblob's Wizardry {@link electroblob.wizardry.spell.Metamorphosis#TRANSFORMATIONS}.
 *
 * <p>The caster's sneak state determines direction: normal cast transforms forward
 * (e.g. Pig → Zombie Pigman), sneaking reverses the transformation (Zombie Pigman → Pig).
 *
 * <p>Only registered when Electroblob's Wizardry is present.
 */
public class Metamorphosis extends SpellComponent {

    @Override
    public boolean applyEffectBlock(SpellData spell, World world, BlockPos blockPos, EnumFacing blockFace,
                                    double impactX, double impactY, double impactZ, EntityLivingBase caster) {
        return false;
    }

    @Override
    public boolean applyEffectEntity(SpellData spell, World world, EntityLivingBase caster, Entity target) {
        if (world.isRemote) return false;
        if (!(target instanceof EntityLivingBase)) return false;
        EntityLivingBase living = (EntityLivingBase) target;

        // Determine transformation direction based on caster sneak state
        Class<? extends EntityLivingBase> targetClass;
        if (caster != null && caster.isSneaking()) {
            targetClass = electroblob.wizardry.spell.Metamorphosis.TRANSFORMATIONS.inverse().get(living.getClass());
        } else {
            targetClass = electroblob.wizardry.spell.Metamorphosis.TRANSFORMATIONS.get(living.getClass());
        }

        if (targetClass == null) return false; // Entity class is not in the transformation map

        double x = living.posX;
        double y = living.posY;
        double z = living.posZ;

        EntityLivingBase newEntity;
        try {
            newEntity = targetClass.getConstructor(World.class).newInstance(world);
        } catch (Exception e) {
            LogHelper.warn("AM2 Metamorphosis component: Failed to instantiate %s: %s", targetClass.getName(), e.getMessage());
            return false;
        }

        // Copy health from the original entity
        newEntity.setHealth(living.getHealth());

        // Copy NBT state but strip the UUID so the new entity gets a fresh identity
        NBTTagCompound nbt = new NBTTagCompound();
        living.writeToNBT(nbt);
        electroblob.wizardry.util.NBTExtras.removeUniqueId(nbt, "UUID");
        newEntity.readFromNBT(nbt);

        // Kill the original entity and spawn the transformed one
        living.setDead();
        newEntity.setPosition(x, y, z);
        world.spawnEntity(newEntity);
        return true;
    }

    @Override
    public EnumSet<SpellModifiers> getModifiers() {
        return EnumSet.noneOf(SpellModifiers.class);
    }

    @Override
    public float manaCost() {
        return 60;
    }


    @Override
    public void spawnParticles(World world, double x, double y, double z, EntityLivingBase caster,
                               Entity target, Random rand, int colorModifier) {
        // Dark-magic burst centred on the target — safe to call directly since this class
        // is only loaded when EBWizardry is present.
        for (int i = 0; i < 20; i++) {
            electroblob.wizardry.util.ParticleBuilder.create(electroblob.wizardry.util.ParticleBuilder.Type.DARK_MAGIC,
                            rand, x, y + 1.0, z, 1.0, false)
                    .clr(0.4f, 0.0f, 0.8f)
                    .spawn(world);
        }
        electroblob.wizardry.util.ParticleBuilder.create(electroblob.wizardry.util.ParticleBuilder.Type.BUFF)
                .pos(x, y + 1.0, z)
                .clr(0x6A0DAD)
                .spawn(world);
    }

    @Override
    public Set<Affinity> getAffinity() {
        return Sets.newHashSet(Affinities.arcane);
    }

    @Override
    public float getAffinityShift(Affinity affinity) {
        return 0.05f;
    }

    @Override
    public Object[] getRecipe() {
        return new Object[]{Blocks.BROWN_MUSHROOM, Blocks.RED_MUSHROOM};
    }
}

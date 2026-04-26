package am2.common.compat.electroblob.spells;

import am2.ArsMagica;
import am2.api.affinity.Affinity;
import am2.api.spell.Operation;
import am2.api.spell.SpellComponent;
import am2.api.spell.SpellData;
import am2.api.spell.SpellModifiers;
import am2.common.compat.electroblob.EBWizardryCompatHandler;
import am2.common.registry.Affinities;
import com.google.common.collect.Sets;
import electroblob.wizardry.block.BlockStatue;
import electroblob.wizardry.registry.WizardryBlocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

/**
 * Spell component that freezes a target mob into an ice statue using
 * Electroblob's Wizardry {@link BlockStatue#convertToStatue(EntityLiving, EntityLivingBase, int)}.
 *
 * <p>Only registered when Electroblob's Wizardry is present.
 */
public class IceStatue extends SpellComponent {

    @Override
    public boolean applyEffectBlock(SpellData spell, World world, BlockPos blockPos, EnumFacing blockFace,
                                    double impactX, double impactY, double impactZ, EntityLivingBase caster) {
        return false;
    }

    @Override
    public boolean applyEffectEntity(SpellData spell, World world, EntityLivingBase caster, Entity target) {
        if (world.isRemote) return false;
        if (!(target instanceof EntityLiving)) return false;
        EntityLiving living = (EntityLiving) target;
        if (living.deathTime > 0) return false;

        int duration = (int) spell.getModifiedValue(ArsMagica.config.getDefaultBuffDuration(), SpellModifiers.DURATION,
                Operation.MULTIPLY, world, caster, target);
        return ((BlockStatue) WizardryBlocks.ice_statue).convertToStatue(living, caster, duration);
    }

    @Override
    public EnumSet<SpellModifiers> getModifiers() {
        return EnumSet.of(SpellModifiers.DURATION);
    }

    @Override
    public float manaCost() {
        return 50;
    }


    @Override
    public void spawnParticles(World world, double x, double y, double z, EntityLivingBase caster,
                               Entity target, Random rand, int colorModifier) {
        // IceStatue is only registered when EBWizardry is loaded, so we can use its particles directly.
        EBWizardryCompatHandler.spawnFrostParticles(world, x + 0.5, y + 0.5, z + 0.5, 5, rand);
    }

    @Override
    public Set<Affinity> getAffinity() {
        return Sets.newHashSet(Affinities.ice);
    }

    @Override
    public float getAffinityShift(Affinity affinity) {
        return 0.05f;
    }

    @Override
    public Object[] getRecipe() {
        return new Object[]{Blocks.ICE, Blocks.SNOW};
    }
}

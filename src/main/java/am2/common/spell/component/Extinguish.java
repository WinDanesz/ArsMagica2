package am2.common.spell.component;

import am2.api.affinity.Affinity;
import am2.api.spell.SpellComponent;
import am2.api.spell.SpellData;
import am2.api.spell.SpellModifiers;
import am2.common.registry.AMItems;
import am2.common.registry.Affinities;
import com.google.common.collect.Sets;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class Extinguish extends SpellComponent {

    @Override
    public boolean applyEffectBlock(SpellData spell, World world, BlockPos pos, EnumFacing blockFace, double impactX, double impactY, double impactZ, EntityLivingBase caster) {
        boolean did = false;
        // Remove fire blocks directly hit
        if (world.getBlockState(pos).getBlock() == Blocks.FIRE) {
            if (!world.isRemote) world.setBlockToAir(pos);
            did = true;
        }
        // Also check the face offset (e.g. if the spell hit a burning block's surface)
        BlockPos offsetPos = pos.offset(blockFace);
        if (world.getBlockState(offsetPos).getBlock() == Blocks.FIRE) {
            if (!world.isRemote) world.setBlockToAir(offsetPos);
            did = true;
        }
        // Extinguish any burning entities nearby the impact point
        if (!world.isRemote) {
            AxisAlignedBB area = new AxisAlignedBB(impactX - 1.5, impactY - 1.5, impactZ - 1.5,
                    impactX + 1.5, impactY + 1.5, impactZ + 1.5);
            List<Entity> nearby = world.getEntitiesWithinAABBExcludingEntity(caster, area);
            for (Entity e : nearby) {
                if (e.isBurning()) {
                    e.extinguish();
                    did = true;
                }
            }
        }
        return did;
    }

    @Override
    public boolean applyEffectEntity(SpellData spell, World world, EntityLivingBase caster, Entity target) {
        if (!target.isBurning()) {
            return false;
        }
        target.extinguish();
        return true;
    }

    @Override
    public float manaCost() {
        return 18;
    }


    @Override
    public void spawnParticles(World world, double x, double y, double z, EntityLivingBase caster, Entity target, Random rand, int colorModifier) {
        for (int i = 0; i < 12; ++i) {
            world.spawnParticle(EnumParticleTypes.WATER_SPLASH,
                    x - 0.5 + rand.nextDouble(),
                    y + rand.nextDouble() * 0.5,
                    z - 0.5 + rand.nextDouble(),
                    0.4 - rand.nextDouble() * 0.8,
                    0.15,
                    0.4 - rand.nextDouble() * 0.8);
        }
        world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, x, y + 0.5, z, 0, 0.05, 0);
    }

    @Override
    public Set<Affinity> getAffinity() {
        return Sets.newHashSet(Affinities.water);
    }

    @Override
    public Object[] getRecipe() {
        return new Object[]{
                new ItemStack(AMItems.rune, 1, EnumDyeColor.BLUE.getDyeDamage()),
                Items.SNOWBALL
        };
    }

    @Override
    public EnumSet<SpellModifiers> getModifiers() {
        return EnumSet.noneOf(SpellModifiers.class);
    }

    @Override
    public float getAffinityShift(Affinity affinity) {
        return 0.01f;
    }
}

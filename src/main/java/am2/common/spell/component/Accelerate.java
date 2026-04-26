package am2.common.spell.component;

import am2.ArsMagica;
import am2.api.affinity.Affinity;
import am2.api.spell.SpellComponent;
import am2.api.spell.SpellData;
import am2.api.spell.SpellModifiers;
import am2.client.particles.AMParticle;
import am2.client.particles.ParticleOrbitEntity;
import am2.common.registry.AMItems;
import am2.common.registry.Affinities;
import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

public class Accelerate extends SpellComponent {

    @Override
    public boolean applyEffectEntity(SpellData spell, World world, EntityLivingBase caster, Entity target) {
        caster.setAIMoveSpeed(caster.getAIMoveSpeed() * 1.6f);
        return true;
    }

    @Override
    public float manaCost() {
        return 6;
    }


    @Override
    public void spawnParticles(World world, double x, double y, double z, EntityLivingBase caster, Entity target, Random rand, int colorModifier) {
        AMParticle particle = (AMParticle) ArsMagica.proxy.particleManager.spawn(world, "sparkle", x, y, z);
        if (particle != null) {
            particle.AddParticleController(new ParticleOrbitEntity(particle, caster, 0.1f, 1, false).SetTargetDistance(rand.nextDouble() + 0.5));
            particle.setMaxAge(25 + rand.nextInt(10));
            if (colorModifier > -1) {
                particle.setRGBColorF(((colorModifier >> 16) & 0xFF) / 255.0f, ((colorModifier >> 8) & 0xFF) / 255.0f, (colorModifier & 0xFF) / 255.0f);
            }
        }
    }

    @Override
    public Object[] getRecipe() {
        return new Object[]{
                new ItemStack(AMItems.rune, 1, EnumDyeColor.YELLOW.getDyeDamage()),
                Items.LEATHER_BOOTS,
                Items.REDSTONE
        };
    }

    @Override
    public float getAffinityShift(Affinity affinity) {
        if (affinity.equals(Affinities.air))
            return 1F;
        return 0;
    }

    @Override
    public boolean applyEffectBlock(SpellData spell, World world, BlockPos blockPos, EnumFacing blockFace, double impactX, double impactY, double impactZ, EntityLivingBase caster) {
        if (world.rand.nextDouble() < 0.5) {
            Block block = world.getBlockState(blockPos).getBlock();
            if (block != Blocks.AIR) {
                block.updateTick(world, blockPos, world.getBlockState(blockPos), world.rand);
            }
        }
        return true;
    }

    @Override
    public EnumSet<SpellModifiers> getModifiers() {
        return EnumSet.noneOf(SpellModifiers.class);
    }


    @Override
    public Set<Affinity> getAffinity() {
        return Sets.newHashSet(Affinities.air);
    }

}

package am2.common.spell.component;

import am2.ArsMagica;
import am2.api.affinity.Affinity;
import am2.api.blocks.IMultiblock;
import am2.api.rituals.IRitualInteraction;
import am2.api.rituals.RitualShapeHelper;
import am2.api.spell.SpellComponent;
import am2.api.spell.SpellData;
import am2.api.spell.SpellModifiers;
import am2.client.particles.AMParticle;
import am2.client.particles.ParticleFloatUpward;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

public class BanishRain extends SpellComponent implements IRitualInteraction {

    @Override
    public boolean applyEffectBlock(SpellData spell, World world, BlockPos pos, EnumFacing blockFace, double impactX, double impactY, double impactZ, EntityLivingBase caster) {
        boolean hasMatch = RitualShapeHelper.instance.matchesRitual(this, world, pos);
        if (hasMatch) {
            RitualShapeHelper.instance.consumeReagents(this, world, pos);
            world.getWorldInfo().setRainTime(0);
            world.getWorldInfo().setRaining(true);
            return true;
        }
        if (!world.isRaining()) return false;
        world.getWorldInfo().setRainTime(24000);
        world.getWorldInfo().setRaining(false);
        return true;
    }

    @Override
    public boolean applyEffectEntity(SpellData spell, World world, EntityLivingBase caster, Entity target) {
        boolean hasMatch = RitualShapeHelper.instance.matchesRitual(this, world, caster.getPosition());
        if (hasMatch) {
            RitualShapeHelper.instance.consumeReagents(this, world, target.getPosition());
            world.getWorldInfo().setRainTime(0);
            world.getWorldInfo().setRaining(true);
            return true;
        }
        if (!world.isRaining()) return false;
        world.getWorldInfo().setRainTime(24000);
        world.getWorldInfo().setRaining(false);
        return true;
    }

    @Override
    public float manaCost() {
        return 750;
    }

    @Override
    public EnumSet<SpellModifiers> getModifiers() {
        return EnumSet.noneOf(SpellModifiers.class);
    }

    @Override
    public ItemStack[] reagents(EntityLivingBase caster) {
        return new ItemStack[]{new ItemStack(AMItems.essence_water)};
    }

    @Override
    public void spawnParticles(World world, double x, double y, double z, EntityLivingBase caster, Entity target, Random rand, int colorModifier) {
        for (int i = 0; i < 25; ++i) {
            AMParticle particle = (AMParticle) ArsMagica.proxy.particleManager.spawn(world, "water_ball", x, y, z);
            if (particle != null) {
                particle.addRandomOffset(5, 4, 5);
                particle.AddParticleController(new ParticleFloatUpward(particle, 0f, 0.5f, 1, false));
                particle.setMaxAge(25 + rand.nextInt(10));
                if (colorModifier > -1) {
                    particle.setRGBColorF(((colorModifier >> 16) & 0xFF) / 255.0f, ((colorModifier >> 8) & 0xFF) / 255.0f, (colorModifier & 0xFF) / 255.0f);
                }
            }
        }
    }

    @Override
    public Set<Affinity> getAffinity() {
        return Sets.newHashSet(Affinities.water);
    }

    @Override
    public Object[] getRecipe() {
        return new Object[]{
                new ItemStack(AMItems.rune, 1, EnumDyeColor.BLUE.getDyeDamage()),
                Items.GOLD_INGOT
        };
    }

    @Override
    public float getAffinityShift(Affinity affinity) {
        return 0.3f;
    }

    @Override
    public IMultiblock getRitualShape() {
        return RitualShapeHelper.instance.hourglass;
    }

    @Override
    public ItemStack[] getRitualReagents() {
        return new ItemStack[]{
                new ItemStack(Items.WATER_BUCKET),
                new ItemStack(Blocks.SNOW)
        };
    }
}

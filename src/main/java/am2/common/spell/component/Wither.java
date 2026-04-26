package am2.common.spell.component;

import am2.ArsMagica;
import am2.api.affinity.Affinity;
import am2.api.spell.Operation;
import am2.api.spell.SpellComponent;
import am2.api.spell.SpellData;
import am2.api.spell.SpellModifiers;
import am2.client.particles.AMParticle;
import am2.client.particles.ParticleFloatUpward;
import am2.client.particles.ParticleOrbitEntity;
import am2.common.registry.Affinities;
import com.google.common.collect.Sets;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

public class Wither extends SpellComponent {

    /** Default duration: 5 seconds = 100 ticks. */
    private static final int DEFAULT_DURATION = 100;

    @Override
    public boolean applyEffectEntity(SpellData spell, World world, EntityLivingBase caster, Entity target) {
        if (target instanceof EntityLivingBase) {
            int duration = (int) spell.getModifiedValue(DEFAULT_DURATION, SpellModifiers.DURATION, Operation.MULTIPLY, world, caster, target);
            int amplifier = spell.getModifierCount(SpellModifiers.BUFF_POWER);

            if (!world.isRemote)
                ((EntityLivingBase) target).addPotionEffect(new PotionEffect(MobEffects.WITHER, duration, amplifier));

            return true;
        }
        return false;
    }

    @Override
    public EnumSet<SpellModifiers> getModifiers() {
        return EnumSet.of(SpellModifiers.DURATION, SpellModifiers.BUFF_POWER);
    }

    @Override
    public float manaCost() {
        return 100;
    }


    @Override
    public Object[] getRecipe() {
        return new Object[]{
                new ItemStack(Items.NETHER_STAR),
                new ItemStack(Items.BONE)
        };
    }
    @Override
    public Set<Affinity> getAffinity() {
        return Sets.newHashSet(Affinities.ender);
    }

    @Override
    public float getAffinityShift(Affinity affinity) {
        return 0.05f;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void spawnParticles(World world, double x, double y, double z, EntityLivingBase caster,
                               Entity target, Random rand, int colorModifier) {
        for (int i = 0; i < 10; i++) {
            AMParticle particle = (AMParticle) ArsMagica.proxy.particleManager.spawn(world, "sparkle", x, y + 1, z);
            if (particle != null) {
                particle.AddParticleController(new ParticleOrbitEntity(particle, target, 0.08f, 1, false)
                        .SetTargetDistance(rand.nextDouble() * 0.5 + 0.3));
                particle.setMaxAge(20 + rand.nextInt(15));
                // Wither dark grey/black
                particle.setRGBColorF(0.15f, 0.1f, 0.15f);
                particle.AddParticleController(new ParticleFloatUpward(particle, 0, 0.03f, 1, false));
            }
        }
    }
}

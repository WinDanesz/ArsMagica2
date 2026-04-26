package am2.common.spell.component;

import am2.ArsMagica;
import am2.api.affinity.Affinity;
import am2.api.spell.Operation;
import am2.api.spell.SpellComponent;
import am2.api.spell.SpellData;
import am2.api.spell.SpellModifiers;
import am2.client.particles.AMParticle;
import am2.client.particles.ParticleOrbitEntity;
import am2.common.registry.Affinities;
import com.google.common.collect.Sets;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

public class Reveal extends SpellComponent {

    @Override
    public boolean applyEffectEntity(SpellData spell, World world, EntityLivingBase caster, Entity target) {
        if (target instanceof EntityLivingBase) {
            int duration = (int) spell.getModifiedValue(ArsMagica.config.getDefaultBuffDuration(), SpellModifiers.DURATION, Operation.MULTIPLY, world, caster, target);
            if (!world.isRemote)
                ((EntityLivingBase) target).addPotionEffect(new PotionEffect(MobEffects.GLOWING, duration, 0));
            return true;
        }
        return false;
    }

    @Override
    public float manaCost() {
        return 120;
    }

    @Override
    public EnumSet<SpellModifiers> getModifiers() {
        return EnumSet.of(SpellModifiers.DURATION);
    }

    @Override
    public void spawnParticles(World world, double x, double y, double z, EntityLivingBase caster, Entity target, Random rand, int colorModifier) {
        for (int i = 0; i < 12; ++i) {
            AMParticle particle = (AMParticle) ArsMagica.proxy.particleManager.spawn(world, "sparkle", x, y, z);
            if (particle != null) {
                particle.addRandomOffset(1, 1, 1);
                if (target != null) {
                    particle.AddParticleController(new ParticleOrbitEntity(particle, target, 0.1f, 2, false).setOrbitY(0.5).SetTargetDistance(1f));
                }
                particle.setMaxAge(35);
                particle.setParticleScale(0.15f);
                if (colorModifier > -1) {
                    particle.setRGBColorF(((colorModifier >> 16) & 0xFF) / 255.0f, ((colorModifier >> 8) & 0xFF) / 255.0f, (colorModifier & 0xFF) / 255.0f);
                } else {
                    particle.setRGBColorF(1.0f, 0.9f, 0.2f); // golden yellow
                }
            }
        }
    }

    @Override
    public Set<Affinity> getAffinity() {
        return Sets.newHashSet(Affinities.arcane);
    }

    @Override
    public float getAffinityShift(Affinity affinity) {
        return 0.01f;
    }
}

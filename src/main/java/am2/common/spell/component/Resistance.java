package am2.common.spell.component;

import am2.ArsMagica;
import am2.api.affinity.Affinity;
import am2.api.spell.Operation;
import am2.api.spell.SpellComponent;
import am2.api.spell.SpellData;
import am2.api.spell.SpellModifiers;
import am2.client.particles.AMParticle;
import am2.client.particles.ParticleFadeOut;
import am2.client.particles.ParticleOrbitEntity;
import am2.common.registry.AMItems;
import am2.common.registry.Affinities;
import com.google.common.collect.Sets;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

public class Resistance extends SpellComponent {

    // Vanilla Resistance caps out at level 2 (amplifier 1), so we clamp BUFF_POWER to 1
    private static final int MAX_AMPLIFIER = 1;

    @Override
    public boolean applyEffectEntity(SpellData spell, World world, EntityLivingBase caster, Entity target) {
        if (target instanceof EntityLivingBase) {
            int duration = (int) spell.getModifiedValue(ArsMagica.config.getDefaultBuffDuration(), SpellModifiers.DURATION, Operation.MULTIPLY, world, caster, target);
            int amplifier = Math.min(spell.getModifierCount(SpellModifiers.BUFF_POWER), MAX_AMPLIFIER);

            if (!world.isRemote)
                ((EntityLivingBase) target).addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, duration, amplifier));

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
        return EnumSet.of(SpellModifiers.BUFF_POWER, SpellModifiers.DURATION);
    }

    @Override
    public void spawnParticles(World world, double x, double y, double z, EntityLivingBase caster, Entity target, Random rand, int colorModifier) {
        for (int i = 0; i < 20; ++i) {
            AMParticle particle = (AMParticle) ArsMagica.proxy.particleManager.spawn(world, "lens_flare", x, y, z);
            if (particle != null) {
                particle.AddParticleController(new ParticleOrbitEntity(particle, target, 0.1f, 1, false)
                        .SetTargetDistance(rand.nextDouble() * 0.6 + 0.4));
                particle.AddParticleController(new ParticleFadeOut(particle, 1, false).setFadeSpeed(0.04f).setKillParticleOnFinish(true));
                particle.setMaxAge(20 + rand.nextInt(10));
                // Blue-grey tint for a defensive/armor feel
                if (colorModifier > -1) {
                    particle.setRGBColorF(((colorModifier >> 16) & 0xFF) / 255.0f, ((colorModifier >> 8) & 0xFF) / 255.0f, (colorModifier & 0xFF) / 255.0f);
                } else {
                    particle.setRGBColorF(0.5f, 0.7f, 1.0f);
                }
            }
        }
    }

    @Override
    public Set<Affinity> getAffinity() {
        return Sets.newHashSet(Affinities.life, Affinities.earth);
    }

    @Override
    public float getAffinityShift(Affinity affinity) {
        return 0.05f;
    }

    @Override
    public Object[] getRecipe() {
        return new Object[]{
                new ItemStack(AMItems.rune, 1, EnumDyeColor.GRAY.getDyeDamage()),
                Items.DIAMOND_CHESTPLATE
        };
    }
}

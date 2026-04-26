package am2.common.spell.component;

import am2.ArsMagica;
import am2.api.DamageSources;
import am2.api.affinity.Affinity;
import am2.api.spell.Operation;
import am2.api.spell.SpellComponent;
import am2.api.spell.SpellData;
import am2.api.spell.SpellModifiers;
import am2.client.particles.*;
import am2.common.bosses.AM2Boss;
import am2.common.extensions.AffinityData;
import am2.common.extensions.EntityExtension;
import am2.common.registry.AMBlocks;
import am2.common.registry.AMItems;
import am2.common.registry.Affinities;
import am2.common.utils.SpellUtils;
import com.google.common.collect.Sets;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

public class Heal extends SpellComponent {

    @Override
    public boolean applyEffectEntity(SpellData spell, World world, EntityLivingBase caster, Entity target) {
        if (target instanceof EntityLivingBase) {
            if (((EntityLivingBase) target).getCreatureAttribute() == EnumCreatureAttribute.UNDEAD) {
                double healing = spell.getModifiedValue(10, SpellModifiers.HEALING, Operation.MULTIPLY, world, caster, target);
                target.setFire(2);
                return SpellUtils.attackTargetSpecial(spell, target, DamageSources.causeHolyDamage(caster), (float) (healing * (0.5f + 2 * AffinityData.For(caster).getAffinityDepth(Affinities.life))));
            } else {
                // For channeled spells, use 0.1 base healing (applies every tick)
                // For non-channeled spells, use 2.0 base healing (applies once with cooldown)
                boolean isChanneled = spell.isChanneled();
                double baseHealing = isChanneled ? 0.1 : 2.0;
                double healing = spell.getModifiedValue(baseHealing, SpellModifiers.HEALING, Operation.MULTIPLY, world, caster, target);
                if (!(caster instanceof AM2Boss))
                    healing *= 1F + AffinityData.For(caster).getAffinityDepth(Affinities.life);

                // For channeled spells, heal every time without cooldown
                // For non-channeled spells, use the cooldown system
                if (isChanneled || EntityExtension.For((EntityLivingBase) target).getHealCooldown() == 0) {
                    ((EntityLivingBase) target).heal((float) healing);
                    if (!isChanneled) {
                        EntityExtension.For((EntityLivingBase) target).setHealCooldown(ArsMagica.config.getHealCooldown());
                    }
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public EnumSet<SpellModifiers> getModifiers() {
        return EnumSet.of(SpellModifiers.HEALING);
    }


    @Override
    public float manaCost() {
        return 225f;
    }


    @Override
    public void spawnParticles(World world, double x, double y, double z, EntityLivingBase caster, Entity target, Random rand, int colorModifier) {
        if (target instanceof EntityLivingBase && ((EntityLivingBase) target).getCreatureAttribute() == EnumCreatureAttribute.UNDEAD) {
            for (int i = 0; i < 25; ++i) {
                AMParticle particle = (AMParticle) ArsMagica.proxy.particleManager.spawn(world, "symbols", x, y - 1, z);
                if (particle != null) {
                    particle.addRandomOffset(1, 1, 1);
                    particle.AddParticleController(new ParticleHoldPosition(particle, 20, 1, true));
                    particle.AddParticleController(new ParticleFloatUpward(particle, 0, -0.01f, 2, false));
                    particle.AddParticleController(new ParticleFadeOut(particle, 2, false).setFadeSpeed(0.02f));
                    particle.setParticleScale(0.1f);
                    particle.setRGBColorF(1f, 0.2f, 0.2f);
                }
            }
        } else {
            for (int i = 0; i < 25; ++i) {
                AMParticle particle = (AMParticle) ArsMagica.proxy.particleManager.spawn(world, "sparkle", x, y - 1, z);
                if (particle != null) {
                    particle.addRandomOffset(1, 1, 1);
                    particle.AddParticleController(new ParticleFloatUpward(particle, 0, 0.1f, 1, false));
                    particle.AddParticleController(new ParticleOrbitEntity(particle, target, 0.5f, 2, false).setIgnoreYCoordinate(true).SetTargetDistance(0.3f + rand.nextDouble() * 0.3));
                    particle.setMaxAge(20);
                    particle.setParticleScale(0.2f);
                    particle.setRGBColorF(0.1f, 1f, 0.1f);
                    if (colorModifier > -1) {
                        particle.setRGBColorF(((colorModifier >> 16) & 0xFF) / 255.0f, ((colorModifier >> 8) & 0xFF) / 255.0f, (colorModifier & 0xFF) / 255.0f);
                    }
                }
            }
        }
    }

    @Override
    public Set<Affinity> getAffinity() {
        return Sets.newHashSet(Affinities.life);
    }

    @Override
    public Object[] getRecipe() {
        return new Object[]{
                new ItemStack(AMItems.rune, 1, EnumDyeColor.GREEN.getDyeDamage()),
                AMBlocks.aum
        };
    }

    @Override
    public float getAffinityShift(Affinity affinity) {
        return 0.05f;
    }
}

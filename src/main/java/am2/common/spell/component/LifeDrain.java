package am2.common.spell.component;

import am2.ArsMagica;
import am2.api.affinity.Affinity;
import am2.api.spell.Operation;
import am2.api.spell.SpellComponent;
import am2.api.spell.SpellData;
import am2.api.spell.SpellModifiers;
import am2.client.particles.AMParticle;
import am2.client.particles.ParticleArcToEntity;
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
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

public class LifeDrain extends SpellComponent {

    @Override
    public boolean applyEffectEntity(SpellData spell, World world, EntityLivingBase caster, Entity target) {
        if (world.isRemote || !(target instanceof EntityLivingBase) || ((EntityLivingBase) target).getCreatureAttribute() == EnumCreatureAttribute.UNDEAD) {
            return true;
        }
        float magnitude = (float) spell.getModifiedValue(ArsMagica.config.getLifeDrainBase(), SpellModifiers.DAMAGE, Operation.ADD, world, caster, target);

        boolean success = SpellUtils.attackTargetSpecial(spell, target, DamageSource.causeIndirectMagicDamage(caster, caster), SpellUtils.modifyDamage(caster, magnitude));

        if (success) {
            caster.heal((int) Math.ceil(magnitude / 4));
            return true;
        }

        return false;
    }

    @Override
    public float manaCost() {
        return 300;
    }


    @Override
    public void spawnParticles(World world, double x, double y, double z, EntityLivingBase caster, Entity target, Random rand, int colorModifier) {
        for (int i = 0; i < 15; ++i) {
            AMParticle particle = (AMParticle) ArsMagica.proxy.particleManager.spawn(world, "ember", x, y, z);
            if (particle != null) {
                particle.addRandomOffset(1, 1, 1);
                particle.setIgnoreMaxAge(true);
                particle.AddParticleController(new ParticleArcToEntity(particle, 1, caster, false).SetSpeed(0.03f).generateControlPoints());
                particle.setRGBColorF(1, 0.2f, 0.2f);
                particle.SetParticleAlpha(0.5f);
                if (colorModifier > -1) {
                    particle.setRGBColorF(((colorModifier >> 16) & 0xFF) / 255.0f, ((colorModifier >> 8) & 0xFF) / 255.0f, (colorModifier & 0xFF) / 255.0f);
                }
            }
        }
    }

    @Override
    public EnumSet<SpellModifiers> getModifiers() {
        return EnumSet.of(SpellModifiers.DAMAGE);
    }


    @Override
    public Set<Affinity> getAffinity() {
        return Sets.newHashSet(Affinities.life);
    }

    @Override
    public Object[] getRecipe() {
        return new Object[]{
                new ItemStack(AMItems.rune, 1, EnumDyeColor.BLACK.getDyeDamage()),
                new ItemStack(AMItems.sunstone),
                AMBlocks.aum
        };
    }

    @Override
    public float getAffinityShift(Affinity affinity) {
        return 0.01f;
    }
}

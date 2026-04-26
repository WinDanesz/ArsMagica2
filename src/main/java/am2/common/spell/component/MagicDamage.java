package am2.common.spell.component;

import am2.ArsMagica;
import am2.api.DamageSources;
import am2.api.affinity.Affinity;
import am2.api.spell.Operation;
import am2.api.spell.SpellComponent;
import am2.api.spell.SpellData;
import am2.api.spell.SpellModifiers;
import am2.client.particles.AMParticle;
import am2.common.registry.AMItems;
import am2.common.registry.Affinities;
import am2.common.utils.SpellUtils;
import com.google.common.collect.Sets;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

public class MagicDamage extends SpellComponent {

    @Override
    public boolean applyEffectEntity(SpellData spell, World world, EntityLivingBase caster, Entity target) {
        if (!(target instanceof EntityLivingBase)) return false;
        double damage = spell.getModifiedValue(ArsMagica.config.getMagicDamageBase(), SpellModifiers.DAMAGE, Operation.ADD, world, caster, target);
        return SpellUtils.attackTargetSpecial(spell, target, DamageSources.causeMagicDamage(caster), SpellUtils.modifyDamage(caster, (float) damage));
    }

    @Override
    public float manaCost() {
        return 80;
    }

    @Override
    public void spawnParticles(World world, double x, double y, double z, EntityLivingBase caster, Entity target, Random rand, int colorModifier) {
        for (int i = 0; i < 5; ++i) {
            AMParticle particle = (AMParticle) ArsMagica.proxy.particleManager.spawn(world, "arcane", x, y, z);
            if (particle != null) {
                particle.addRandomOffset(1, 0.5, 1);
                particle.addVelocity(rand.nextDouble() * 0.2 - 0.1, rand.nextDouble() * 0.2, rand.nextDouble() * 0.2 - 0.1);
                particle.setAffectedByGravity();
                particle.setDontRequireControllers();
                particle.setMaxAge(5);
                particle.setParticleScale(0.1f);
                if (colorModifier > -1) {
                    particle.setRGBColorI(colorModifier);
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
        return Sets.newHashSet(Affinities.arcane, Affinities.ender);
    }

    @Override
    public Object[] getRecipe() {
        return new Object[]{
                new ItemStack(AMItems.rune, 1, EnumDyeColor.PURPLE.getDyeDamage()),
                new ItemStack(Items.DYE, 1, 4),
                Items.BOOK,
                Items.STONE_SWORD
        };
    }

    @Override
    public float getAffinityShift(Affinity affinity) {
        if (affinity == Affinities.ender)
            return 0.005f;
        return 0.01f;
    }
}

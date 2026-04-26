package am2.common.spell.component;

import am2.ArsMagica;
import am2.api.affinity.Affinity;
import am2.api.extensions.IEntityExtension;
import am2.api.spell.Operation;
import am2.api.spell.SpellComponent;
import am2.api.spell.SpellData;
import am2.api.spell.SpellModifiers;
import am2.client.particles.AMParticle;
import am2.client.particles.ParticleArcToEntity;
import am2.common.extensions.EntityExtension;
import am2.common.registry.AMItems;
import am2.common.registry.Affinities;
import com.google.common.collect.Sets;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

public class ManaDrain extends SpellComponent {

    @Override
    public boolean applyEffectEntity(SpellData spell, World world, EntityLivingBase caster, Entity target) {
        if (!(target instanceof EntityLivingBase)) return false;

        double manaStolen = spell.getModifiedValue(ArsMagica.config.getManaDrainBase(), SpellModifiers.DAMAGE, Operation.ADD, world, caster, target);
        IEntityExtension targetProperties = EntityExtension.For((EntityLivingBase) target);
        if (manaStolen > targetProperties.getCurrentMana()) {
            manaStolen = targetProperties.getCurrentMana();
        }
        targetProperties.setCurrentMana((float) (targetProperties.getCurrentMana() - manaStolen));
        IEntityExtension casterProperties = EntityExtension.For(caster);
        casterProperties.setCurrentMana((float) (casterProperties.getCurrentMana() + manaStolen));
        return true;
    }

    @Override
    public EnumSet<SpellModifiers> getModifiers() {
        return EnumSet.of(SpellModifiers.DAMAGE);
    }


    @Override
    public float manaCost() {
        return 20;
    }


    @Override
    public void spawnParticles(World world, double x, double y, double z, EntityLivingBase caster, Entity target, Random rand, int colorModifier) {
        for (int i = 0; i < 15; ++i) {
            AMParticle particle = (AMParticle) ArsMagica.proxy.particleManager.spawn(world, "sparkle2", x, y, z);
            if (particle != null) {
                particle.addRandomOffset(1, 1, 1);
                particle.setIgnoreMaxAge(true);
                particle.AddParticleController(new ParticleArcToEntity(particle, 1, caster, false).SetSpeed(0.03f).generateControlPoints());
                particle.setRGBColorF(0, 0.4f, 1);
                if (colorModifier > -1) {
                    particle.setRGBColorF(((colorModifier >> 16) & 0xFF) / 255.0f, ((colorModifier >> 8) & 0xFF) / 255.0f, (colorModifier & 0xFF) / 255.0f);
                }
            }
        }

        double tx = target != null ? target.posX : x;
        double ty = target instanceof EntityLivingBase ? target.posY + ((EntityLivingBase) target).getEyeHeight() * 0.5 : y;
        double tz = target != null ? target.posZ : z;
        for (int i = 0; i < 15; ++i) {
            AMParticle particle = (AMParticle) ArsMagica.proxy.particleManager.spawn(world, "sparkle2", tx, ty, tz);
            if (particle != null) {
                particle.addRandomOffset(0.5, 0.5, 0.5);
                particle.setMaxAge(15);
                particle.addVelocity(rand.nextGaussian() * 0.05, rand.nextDouble() * 0.05 + 0.02, rand.nextGaussian() * 0.05);
                particle.setRGBColorF(1f, 0.1f, 0.1f);
            }
        }
    }

    @Override
    public Set<Affinity> getAffinity() {
        return Sets.newHashSet(Affinities.arcane);
    }

    @Override
    public Object[] getRecipe() {
        return new Object[]{
                new ItemStack(AMItems.rune, 1, EnumDyeColor.BLACK.getDyeDamage()),
                new ItemStack(AMItems.moonstone),
                new ItemStack(AMItems.vinteum_dust),
        };
    }

    @Override
    public float getAffinityShift(Affinity affinity) {
        return 0.01f;
    }

}

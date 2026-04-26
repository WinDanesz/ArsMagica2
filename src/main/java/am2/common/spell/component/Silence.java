package am2.common.spell.component;

import am2.ArsMagica;
import am2.api.affinity.Affinity;
import am2.api.spell.SpellComponent;
import am2.api.spell.SpellData;
import am2.api.spell.SpellModifiers;
import am2.common.registry.AMItems;
import am2.common.registry.AMPotions;
import am2.common.registry.Affinities;
import com.google.common.collect.Sets;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

public class Silence extends SpellComponent {

    @Override
    public Object[] getRecipe() {
        return new Object[]{
                Blocks.WOOL,
                new ItemStack(AMItems.arcane_ash),
                Blocks.JUKEBOX,
                Blocks.WOOL
        };
    }

    @Override
    public boolean applyEffectEntity(SpellData spell, World world, EntityLivingBase caster, Entity target) {
        if (target instanceof EntityLivingBase && target.isNonBoss()) {
            if (!world.isRemote)
                ((EntityLivingBase) target).addPotionEffect(new PotionEffect(AMPotions.silence, ArsMagica.config.getDefaultBuffDuration(), spell.getModifierCount(SpellModifiers.BUFF_POWER)));
            return true;
        }
        return false;
    }

    @Override
    public EnumSet<SpellModifiers> getModifiers() {
        return EnumSet.of(SpellModifiers.BUFF_POWER);
    }

    @Override
    public float manaCost() {
        return 800;
    }

    @Override
    public ItemStack[] reagents(EntityLivingBase caster) {
        return new ItemStack[]{
                new ItemStack(AMItems.purified_vinteum_dust)
        };
    }

    @Override
    public void spawnParticles(World world, double x, double y, double z,
                               EntityLivingBase caster, Entity target, Random rand,
                               int colorModifier) {

    }

    @Override
    public Set<Affinity> getAffinity() {
        return Sets.newHashSet(Affinities.water, Affinities.ender);
    }

    @Override
    public float getAffinityShift(Affinity affinity) {
        return 0.01f;
    }
}

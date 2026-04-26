package am2.common.spell.component;

import am2.ArsMagica;
import am2.api.affinity.Affinity;
import am2.api.blocks.IMultiblock;
import am2.api.rituals.IRitualInteraction;
import am2.api.rituals.RitualShapeHelper;
import am2.api.spell.Operation;
import am2.api.spell.SpellComponent;
import am2.api.spell.SpellData;
import am2.api.spell.SpellModifiers;
import am2.client.particles.*;
import am2.common.registry.AMItems;
import am2.common.registry.AMPotions;
import am2.common.registry.Affinities;
import am2.common.utils.SpellUtils;
import com.google.common.collect.Sets;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

public class WateryGrave extends SpellComponent implements IRitualInteraction {

    @Override
    public boolean applyEffectEntity(SpellData spell, World world, EntityLivingBase caster, Entity target) {
        if (target instanceof EntityLivingBase) {
            int duration = (int) spell.getModifiedValue(ArsMagica.config.getDefaultBuffDuration(), SpellModifiers.DURATION, Operation.MULTIPLY, world, caster, target);
            duration = SpellUtils.modifyDurationBasedOnArmor(caster, duration);
            if (RitualShapeHelper.instance.matchesRitual(this, world, target.getPosition())) {
                duration += (ArsMagica.config.getBuffPowerDurationBonus() * (spell.getModifierCount(SpellModifiers.BUFF_POWER) + 1));
                RitualShapeHelper.instance.consumeReagents(this, world, target.getPosition());
            }

            if (!world.isRemote)
                ((EntityLivingBase) target).addPotionEffect(new PotionEffect(AMPotions.watery_grave, duration, spell.getModifierCount(SpellModifiers.BUFF_POWER)));
            return true;
        }
        return false;
    }

    @Override
    public float manaCost() {
        return 80;
    }


    @Override
    public void spawnParticles(World world, double x, double y, double z, EntityLivingBase caster, Entity target, Random rand, int colorModifier) {
        for (int i = 0; i < 25; ++i) {
            AMParticle particle = (AMParticle) ArsMagica.proxy.particleManager.spawn(world, "water_ball", x, y, z);
            if (particle != null) {
                particle.addRandomOffset(1, 2, 1);
                particle.AddParticleController(new ParticleOrbitEntity(particle, target, 0.2f, 1, false));
                particle.AddParticleController(new ParticleFadeOut(particle, 1, false).setFadeSpeed(0.05f).setKillParticleOnFinish(true));
                particle.AddParticleController(new ParticleLeaveParticleTrail(particle, "water_ball", false, 5, 2, false)
                        .addControllerToParticleList(new ParticleFloatUpward(particle, 0, -0.3f, 1, false))
                        .setParticleRGB_F(1, 1, 1)
                        .setTicksBetweenSpawns(2));
                particle.setMaxAge(20);
                particle.setParticleScale(0.01f);
                if (colorModifier > -1) {
                    particle.setRGBColorI(colorModifier);
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
                Blocks.STONE,
                Items.LEATHER_BOOTS
        };
    }

    @Override
    public float getAffinityShift(Affinity affinity) {
        return 0.05f;
    }

    @Override
    public IMultiblock getRitualShape() {
        return RitualShapeHelper.instance.hourglass;
    }

    @Override
    public ItemStack[] getRitualReagents() {
        return new ItemStack[]{
                new ItemStack(Blocks.STONE)
        };
    }

    @Override
    public EnumSet<SpellModifiers> getModifiers() {
        return EnumSet.of(SpellModifiers.BUFF_POWER, SpellModifiers.DURATION);
    }

}

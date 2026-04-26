package am2.common.spell.modifier;

import am2.ArsMagica;
import am2.api.spell.SpellModifier;
import am2.api.spell.SpellModifiers;
import am2.common.registry.AMItems;
import am2.common.registry.Affinities;
import am2.common.utils.AffinityShiftUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import java.util.EnumSet;

public class Solar extends SpellModifier {

    @Override
    public EnumSet<SpellModifiers> getAspectsModified() {
        return EnumSet.of(SpellModifiers.RANGE, SpellModifiers.RADIUS, SpellModifiers.DAMAGE, SpellModifiers.DURATION, SpellModifiers.HEALING);
    }

    @Override
    public float getModifier(SpellModifiers type, EntityLivingBase caster, Entity target, World world, NBTTagCompound metadata) {
        switch (type) {
            case RANGE:
                return modifyValueOnInverseLunarCycle(world, ArsMagica.config.getSolarRangeBase());
            case RADIUS:
                return modifyValueOnInverseLunarCycle(world, ArsMagica.config.getSolarRangeBase());
            case DAMAGE:
                return modifyValueOnTime(world, ArsMagica.config.getSolarDamageBase());
            case DURATION:
                return modifyValueOnTime(world, ArsMagica.config.getSolarDurationBase());
            case HEALING:
                return modifyValueOnTime(world, ArsMagica.config.getSolarHealingBase());
            default:
                return 1.0f;
        }
    }

    private float modifyValueOnTime(World world, float value) {
        long x = world.provider.getWorldTime() % 24000;
        float multiplierFromTime = (float) (Math.cos(((x / 3800f) * (x / 24000f) - 13000f) * (180f / Math.PI)) * 1.5f) + 1;
        if (multiplierFromTime < 0)
            multiplierFromTime *= -0.5f;
        return value * multiplierFromTime;
    }

    private float modifyValueOnInverseLunarCycle(World world, float value) {
        long boundedTime = world.provider.getWorldTime() % 24000;
        int phase = 8 - (8 - world.provider.getMoonPhase(world.getWorldInfo().getWorldTime()));
        if (boundedTime > 23500 && boundedTime < 12500) {
            return value + (phase / 2);
        }
        return Math.abs(value - 1);
    }

    @Override
    public Object[] getRecipe() {
        return new Object[]{
                AffinityShiftUtils.getEssenceForAffinity(Affinities.nature),
                new ItemStack(AMItems.sunstone),
                Items.CLOCK
        };
    }

    @Override
    public float getManaCostMultiplier() {
        return 4.0f;
    }
}

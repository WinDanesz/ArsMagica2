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

public class Lunar extends SpellModifier {

    @Override
    public EnumSet<SpellModifiers> getAspectsModified() {
        return EnumSet.of(SpellModifiers.RANGE, SpellModifiers.RADIUS, SpellModifiers.DAMAGE, SpellModifiers.DURATION, SpellModifiers.HEALING);
    }

    @Override
    public float getModifier(SpellModifiers type, EntityLivingBase caster, Entity target, World world, NBTTagCompound metadata) {
        switch (type) {
            case DAMAGE:
                return modifyValueOnTime(world, ArsMagica.config.getLunarDamageBase(), type);
            case HEALING:
                return modifyValueOnTime(world, ArsMagica.config.getLunarHealingBase(), type);
            case RANGE:
            case RADIUS:
                return modifyValueOnLunarCycle(world, ArsMagica.config.getLunarRangeBase(), type);
            case DURATION:
                return modifyValueOnLunarCycle(world, ArsMagica.config.getLunarDurationBase(), type);
            default:
                return 1.0f;
        }
    }

    private float modifyValueOnTime(World world, float baseBonus, SpellModifiers type) {
        long t = world.provider.getWorldTime() % 24000;
        float neutral = (type == SpellModifiers.HEALING || type == SpellModifiers.DURATION) ? 1.0f : 0.0f;
        if (t < 12000) {
            return neutral;
        }
        float curve = (float) (Math.cos(((t - 18000L) / 6000.0) * Math.PI) + 1.0) * 0.5f;
        if (type == SpellModifiers.HEALING || type == SpellModifiers.DURATION) {
            return 1.0f + (baseBonus - 1.0f) * curve;
        }
        return baseBonus * curve;
    }

    private float modifyValueOnLunarCycle(World world, float baseBonus, SpellModifiers type) {
        long t = world.provider.getWorldTime() % 24000;
        float neutral = (type == SpellModifiers.HEALING || type == SpellModifiers.DURATION) ? 1.0f : 0.0f;
        if (t < 12000) {
            return neutral;
        }
        int p = world.provider.getMoonPhase(world.getWorldInfo().getWorldTime());
        int distFromFull = (p <= 4) ? p : (8 - p);
        float phaseFactor = 1.0f - (distFromFull / 4.0f);
        if (type == SpellModifiers.HEALING || type == SpellModifiers.DURATION) {
            return 1.0f + (baseBonus - 1.0f) * phaseFactor;
        }
        return baseBonus * phaseFactor;
    }

    @Override
    public Object[] getRecipe() {
        return new Object[]{
                AffinityShiftUtils.getEssenceForAffinity(Affinities.nature),
                new ItemStack(AMItems.moonstone),
                Items.CLOCK
        };
    }

    @Override
    public float getManaCostMultiplier() {
        return ArsMagica.config.getLunarManaCostMultiplier();
    }
}

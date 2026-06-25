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
            case DAMAGE:
                return modifyValueOnTime(world, ArsMagica.config.getSolarDamageBase(), type);
            case HEALING:
                return modifyValueOnTime(world, ArsMagica.config.getSolarHealingBase(), type);
            case RANGE:
            case RADIUS:
                return modifyValueOnInverseLunarCycle(world, ArsMagica.config.getSolarRangeBase(), type);
            case DURATION:
                return modifyValueOnInverseLunarCycle(world, ArsMagica.config.getSolarDurationBase(), type);
            default:
                return 1.0f;
        }
    }

    private float modifyValueOnTime(World world, float baseBonus, SpellModifiers type) {
        long t = world.provider.getWorldTime() % 24000;
        float neutral = (type == SpellModifiers.HEALING || type == SpellModifiers.DURATION) ? 1.0f : 0.0f;
        if (t >= 12000) {
            return neutral;
        }
        float curve = (float) (Math.cos(((t - 6000L) / 6000.0) * Math.PI) + 1.0) * 0.5f;
        if (type == SpellModifiers.HEALING || type == SpellModifiers.DURATION) {
            return 1.0f + (baseBonus - 1.0f) * curve;
        }
        return baseBonus * curve;
    }

    private float modifyValueOnInverseLunarCycle(World world, float baseBonus, SpellModifiers type) {
        long t = world.provider.getWorldTime() % 24000;
        float neutral = (type == SpellModifiers.HEALING || type == SpellModifiers.DURATION) ? 1.0f : 0.0f;
        if (t >= 12000) {
            return neutral;
        }
        int p = world.provider.getMoonPhase(world.getWorldInfo().getWorldTime());
        int distFromNew = Math.abs(p - 4);
        float phaseFactor = 1.0f - (distFromNew / 4.0f);
        if (type == SpellModifiers.HEALING || type == SpellModifiers.DURATION) {
            return 1.0f + (baseBonus - 1.0f) * phaseFactor;
        }
        return baseBonus * phaseFactor;
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
        return ArsMagica.config.getSolarManaCostMultiplier();
    }
}

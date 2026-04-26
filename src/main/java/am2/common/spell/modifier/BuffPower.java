package am2.common.spell.modifier;

import am2.api.spell.SpellModifier;
import am2.api.spell.SpellModifiers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import java.util.EnumSet;

public class BuffPower extends SpellModifier {
    @Override
    public EnumSet<SpellModifiers> getAspectsModified() {
        return EnumSet.of(SpellModifiers.BUFF_POWER);
    }

    @Override
    public float getModifier(SpellModifiers type, EntityLivingBase caster, Entity target, World world, NBTTagCompound metadata) {
        return 1;
    }

    @Override
    public Object[] getRecipe() {
        return new Object[]{
                Items.GLASS_BOTTLE,
                Items.REDSTONE,
                Items.GLOWSTONE_DUST,
                Items.GHAST_TEAR,
                Items.SPIDER_EYE,
                Items.NETHER_WART,
                Items.GUNPOWDER,
                Items.EMERALD
        };
    }

    @Override
    public float getManaCostMultiplier() {
        return 1.25f;
    }
}

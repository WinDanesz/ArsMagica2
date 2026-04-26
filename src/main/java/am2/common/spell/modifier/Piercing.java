package am2.common.spell.modifier;

import am2.ArsMagica;
import am2.api.spell.SpellModifier;
import am2.api.spell.SpellModifiers;
import am2.common.registry.Affinities;
import am2.common.utils.AffinityShiftUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import java.util.EnumSet;

public class Piercing extends SpellModifier {

    @Override
    public Object[] getRecipe() {
        return new Object[]{
                AffinityShiftUtils.getEssenceForAffinity(Affinities.ice),
                Items.ARROW,
                Items.EMERALD
        };
    }

    @Override
    public EnumSet<SpellModifiers> getAspectsModified() {
        return EnumSet.of(SpellModifiers.PIERCING);
    }

    @Override
    public float getModifier(SpellModifiers type, EntityLivingBase caster, Entity target, World world, NBTTagCompound metadata) {
        return ArsMagica.config.getPiercingModifierValue();
    }

    @Override
    public float getManaCostMultiplier() {
        return 1.5f;
    }
}

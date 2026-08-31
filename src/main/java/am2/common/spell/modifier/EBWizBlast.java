package am2.common.spell.modifier;

import am2.api.affinity.Affinity;
import am2.api.spell.SpellModifier;
import am2.api.spell.SpellModifiers;
import am2.common.registry.Affinities;
import am2.common.utils.AffinityShiftUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Scribing Desk modifier exclusive to Electroblob's Wizardry spells.
 *
 * <p>When applied to an EBWiz spell binding at the Scribing Desk it increases
 * the EBWiz {@code blast_upgrade} modifier, enlarging explosion / area radii
 * for spells that support them (e.g. Firebomb, Force Orb, Detonate).
 *
 * <p>This modifier has no effect in regular AM2 spells – shapes and components
 * cannot coexist with it in the Scribing Desk when an EBWiz book is present.
 */
public class EBWizBlast extends SpellModifier implements IEBWizExclusive {

    @Override
    public EnumSet<SpellModifiers> getAspectsModified() {
        return EnumSet.of(SpellModifiers.RADIUS);
    }

    /** Provides the AM2 modifier value; unused at runtime but required by the registry. */
    @Override
    public float getModifier(SpellModifiers type, EntityLivingBase caster, Entity target, World world, NBTTagCompound metadata) {
        return 1.0f;
    }

    @Override
    public Object[] getRecipe() {
        return new Object[]{
                AffinityShiftUtils.getEssenceForAffinity(Affinities.fire),
                Items.GUNPOWDER,
                Items.GUNPOWDER
        };
    }

    @Override
    public float getManaCostMultiplier() {
        return 1.15f;
    }


    public Set<Affinity> getAffinity() {
        return Collections.singleton(Affinities.fire);
    }
}

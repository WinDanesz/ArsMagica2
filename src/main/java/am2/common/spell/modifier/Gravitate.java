package am2.common.spell.modifier;

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

/**
 * Gravitate modifier – when applied to a zone or puddle shape, causes the spell
 * entity to pull nearby hostile entities toward its centre every second, even
 * outside the zone's effect radius.
 *
 * Each stack of this modifier increases the pull strength by 0.35 blocks/second
 * and extends the pull radius by 2 extra blocks.
 */
public class Gravitate extends SpellModifier {

    @Override
    public EnumSet<SpellModifiers> getAspectsModified() {
        return EnumSet.of(SpellModifiers.GRAVITATE);
    }

    @Override
    public float getModifier(SpellModifiers type, EntityLivingBase caster, Entity target, World world, NBTTagCompound metadata) {
        // Each modifier adds 0.5 to the total pull strength.
        return 0.5f;
    }

    @Override
    public Object[] getRecipe() {
        return new Object[]{
                AffinityShiftUtils.getEssenceForAffinity(Affinities.earth),
                AffinityShiftUtils.getEssenceForAffinity(Affinities.water),
                Items.COMPASS
        };
    }

    @Override
    public float getManaCostMultiplier() {
        return 1.2f;
    }
}

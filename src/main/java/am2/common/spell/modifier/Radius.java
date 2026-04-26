package am2.common.spell.modifier;

import am2.api.spell.SpellModifier;
import am2.api.spell.SpellModifiers;
import am2.common.registry.Affinities;
import am2.common.utils.AffinityShiftUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import java.util.EnumSet;

public class Radius extends SpellModifier {
    @Override
    public EnumSet<SpellModifiers> getAspectsModified() {
        return EnumSet.of(SpellModifiers.RADIUS);
    }

    @Override
    public float getModifier(SpellModifiers type, EntityLivingBase caster, Entity target, World world, NBTTagCompound metadata) {
        // Fix spell radius bug: return a proper multiplier value that works with both ADD and MULTIPLY operations
        return 2.0f; // Doubles the radius
    }

    @Override
    public Object[] getRecipe() {
        return new Object[]{
                AffinityShiftUtils.getEssenceForAffinity(Affinities.fire),
                Items.GLOWSTONE_DUST,
                Blocks.TNT
        };
    }

    @Override
    public float getManaCostMultiplier() {
        return 2.5f;
    }
}

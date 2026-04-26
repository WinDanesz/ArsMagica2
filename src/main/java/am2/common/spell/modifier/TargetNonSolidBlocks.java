package am2.common.spell.modifier;

import am2.api.spell.SpellModifier;
import am2.api.spell.SpellModifiers;
import am2.common.registry.AMBlocks;
import am2.common.registry.Affinities;
import am2.common.utils.AffinityShiftUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import java.util.EnumSet;

public class TargetNonSolidBlocks extends SpellModifier {

    @Override
    public EnumSet<SpellModifiers> getAspectsModified() {
        return EnumSet.of(SpellModifiers.TARGET_NONSOLID_BLOCKS);
    }

    @Override
    public float getModifier(SpellModifiers type, EntityLivingBase caster, Entity target, World world, NBTTagCompound metadata) {
        return 1.0f;
    }

    @Override
    public Object[] getRecipe() {
        return new Object[]{
                AffinityShiftUtils.getEssenceForAffinity(Affinities.water),
                Items.POTIONITEM,
                Blocks.RED_FLOWER,
                Blocks.BROWN_MUSHROOM,
                AMBlocks.cerublossom
        };
    }

    @Override
    public float getManaCostMultiplier() {
        return 1;
    }
}

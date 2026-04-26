package am2.common.spell.shape;

import am2.api.affinity.Affinity;
import am2.api.spell.SpellData;
import am2.api.spell.SpellModifiers;
import am2.api.spell.SpellShape;
import am2.common.spell.SpellCastResult;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

import java.util.EnumSet;

public class MissingShape extends SpellShape {

    @Override
    public Object[] getRecipe() {
        return null;
    }

    @Override
    public boolean isChanneled() {
        return false;
    }

    @Override
    public float manaCostMultiplier() {
        return 0F;
    }

    @Override
    public boolean isTerminusShape() {
        return false;
    }

    @Override
    public boolean isPrincipumShape() {
        return false;
    }

    @Override
    public SpellCastResult beginStackStage(SpellData spell, EntityLivingBase caster, EntityLivingBase target, World world, double x, double y, double z, EnumFacing side, boolean giveXP, int useCount) {
        return SpellCastResult.MALFORMED_SPELL_STACK;
    }

    @Override
    public EnumSet<SpellModifiers> getModifiers() {
        return EnumSet.noneOf(SpellModifiers.class);
    }

    @Override
    public SoundEvent getSoundForAffinity(Affinity affinity, SpellData stack, World world) {
        return null;
    }
}

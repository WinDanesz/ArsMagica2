package am2.common.spell.shape;

import am2.api.affinity.Affinity;
import am2.api.spell.SpellData;
import am2.api.spell.SpellModifiers;
import am2.api.spell.SpellShape;
import am2.common.extensions.EntityExtension;
import am2.common.power.PowerTypes;
import am2.common.registry.AMBlocks;
import am2.common.registry.AMItems;
import am2.common.registry.AMSounds;
import am2.common.registry.Affinities;
import am2.common.spell.ContingencyType;
import am2.common.spell.SpellCastResult;
import am2.common.utils.AffinityShiftUtils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

import java.util.EnumSet;

public class Contingency_Death extends SpellShape {

    @Override
    public Object[] getRecipe() {
        return new Object[]{
                Items.CLOCK,
                AffinityShiftUtils.getEssenceForAffinity(Affinities.ender),
                Blocks.STONE,
                Blocks.STONE_SLAB,
                Blocks.STONE_SLAB,
                Blocks.STONE_SLAB,
                Items.BLAZE_POWDER,
                AMBlocks.tarma_root,
                new ItemStack(AMItems.arcane_ash),
                "E:" + PowerTypes.DARK.ID(), 5000
        };
    }

    @Override
    public SpellCastResult beginStackStage(SpellData spell, EntityLivingBase caster, EntityLivingBase target, World world, double x, double y, double z, EnumFacing side, boolean giveXP, int useCount) {
        EntityExtension.For(target != null ? target : caster).setContingency(ContingencyType.DEATH, spell);
        return SpellCastResult.SUCCESS;
    }



    @Override
    public float manaCostMultiplier() {
        return 10;
    }

    @Override
    public boolean isTerminusShape() {
        return false;
    }

    @Override
    public boolean isPrincipumShape() {
        return true;
    }

    @Override
    public EnumSet<SpellModifiers> getModifiers() {
        return EnumSet.noneOf(SpellModifiers.class);
    }

    @Override
    public SoundEvent getSoundForAffinity(Affinity affinity, SpellData stack, World world) {
        return AMSounds.CONTINGENCY;
    }
}
package am2.common.spell.shape;

import am2.api.affinity.Affinity;
import am2.api.spell.Operation;
import am2.api.spell.SpellData;
import am2.api.spell.SpellModifiers;
import am2.api.spell.SpellShape;
import am2.common.blocks.BlockGroundRuneSpell;
import am2.common.registry.AMBlocks;
import am2.common.registry.AMItems;
import am2.common.registry.AMSounds;
import am2.common.spell.SpellCastResult;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import java.util.EnumSet;

public class Rune extends SpellShape {

    @Override
    public SpellCastResult beginStackStage(SpellData spell, EntityLivingBase caster, EntityLivingBase target, World world, double x, double y, double z, EnumFacing side, boolean giveXP, int useCount) {
        int procs = (int) spell.getModifiedValue(1, SpellModifiers.PROCS, Operation.ADD, world, caster, target);//SpellUtils.getModifiedInt_Add(1, stack, caster, target, world, SpellModifiers.PROCS);
        boolean targetWater = spell.isModifierPresent(SpellModifiers.TARGET_NONSOLID_BLOCKS);
        RayTraceResult mop = spell.raytrace(caster, world, 8.0f, true, targetWater);
        if (mop == null || mop.typeOfHit == RayTraceResult.Type.ENTITY) return SpellCastResult.EFFECT_FAILED;

        if (!((BlockGroundRuneSpell) AMBlocks.spell_rune).placeAt(world, mop.getBlockPos().up(), AMBlocks.spell_rune.getDefaultState())) {
            return SpellCastResult.EFFECT_FAILED;
        }
        if (!world.isRemote) {
            //world.setTileEntity(mop.getBlockPos().up(), BlockDefs.spellRune.createNewTileEntity(world, 0));
            BlockGroundRuneSpell.setSpellStack(world, mop.getBlockPos().up(), spell);
            BlockGroundRuneSpell.setPlacedBy(world, mop.getBlockPos().up(), caster);
            ((BlockGroundRuneSpell) AMBlocks.spell_rune).setNumTriggers(world, mop.getBlockPos().up(), world.getBlockState(mop.getBlockPos().up()), procs);
        }

        return SpellCastResult.SUCCESS;
    }



    @Override
    public Object[] getRecipe() {
        //Blue Rune, Red Rune, White Rune, Black Rune, Orange Rune, Purple Rune, Yellow Rune
        return new Object[]{
                new ItemStack(AMItems.rune, 1, EnumDyeColor.BLUE.getDyeDamage()),
                new ItemStack(AMItems.rune, 1, EnumDyeColor.RED.getDyeDamage()),
                new ItemStack(AMItems.rune, 1, EnumDyeColor.WHITE.getDyeDamage()),
                new ItemStack(AMItems.rune, 1, EnumDyeColor.BLACK.getDyeDamage()),
                new ItemStack(AMItems.rune, 1, EnumDyeColor.ORANGE.getDyeDamage()),
                new ItemStack(AMItems.rune, 1, EnumDyeColor.PURPLE.getDyeDamage()),
                new ItemStack(AMItems.rune, 1, EnumDyeColor.YELLOW.getDyeDamage())
        };
    }

    @Override
    public EnumSet<SpellModifiers> getModifiers() {
        return EnumSet.of(SpellModifiers.TARGET_NONSOLID_BLOCKS, SpellModifiers.PROCS);
    }


    @Override
    public float manaCostMultiplier() {
        return 1.8f;
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
    public SoundEvent getSoundForAffinity(Affinity affinity, SpellData stack, World world) {
        return AMSounds.RUNE_CAST;
    }
}

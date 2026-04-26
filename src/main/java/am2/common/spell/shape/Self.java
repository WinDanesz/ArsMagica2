package am2.common.spell.shape;

import am2.api.spell.SpellData;
import am2.api.spell.SpellModifiers;
import am2.api.spell.SpellShape;
import am2.common.power.PowerTypes;
import am2.common.registry.AMBlocks;
import am2.common.registry.AMItems;
import am2.common.spell.SpellCastResult;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import java.util.EnumSet;

public class Self extends SpellShape {

    @Override
    public SpellCastResult beginStackStage(SpellData spell, EntityLivingBase caster, EntityLivingBase target, World world, double x, double y, double z, EnumFacing side, boolean giveXP, int useCount) {
        SpellCastResult result = spell.applyComponentsToEntity(world, caster, caster);
        if (result != SpellCastResult.SUCCESS) {
            return result;
        }

        return spell.execute(world, caster, target, x, y, z, null);
    }



    @Override
    public Object[] getRecipe() {
        return new Object[]{
                AMBlocks.aum,
                new ItemStack(AMItems.vinteum_dust),
                AMItems.lesser_focus,
                "E:" + PowerTypes.NEUTRAL.ID(), 500
        };
    }

    @Override
    public EnumSet<SpellModifiers> getModifiers() {
        return EnumSet.noneOf(SpellModifiers.class);
    }

    @Override
    public float manaCostMultiplier() {
        return 0.5f;
    }

    @Override
    public boolean isTerminusShape() {
        return false;
    }

    @Override
    public boolean isPrincipumShape() {
        return false;
    }
}

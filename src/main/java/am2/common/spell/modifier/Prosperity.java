package am2.common.spell.modifier;

import am2.api.spell.SpellModifier;
import am2.api.spell.SpellModifiers;
import am2.common.items.ItemCore;
import am2.common.registry.AMItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import java.util.EnumSet;

public class Prosperity extends SpellModifier {
    @Override
    public EnumSet<SpellModifiers> getAspectsModified() {
        return EnumSet.of(SpellModifiers.FORTUNE_LEVEL);
    }

    @Override
    public float getModifier(SpellModifiers type, EntityLivingBase caster, Entity target, World world, NBTTagCompound metadata) {
        return 1;
    }

    @Override
    public Object[] getRecipe() {
        return new Object[]{
                Items.GOLD_INGOT,
                new ItemStack(AMItems.core, 1, ItemCore.META_BASE_CORE),
                Items.GOLD_INGOT
        };
    }

    @Override
    public float getManaCostMultiplier() {
        return 1.25f;
    }
}

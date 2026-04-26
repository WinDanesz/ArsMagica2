package am2.common.spell.modifier;

import am2.api.spell.SpellModifier;
import am2.api.spell.SpellModifiers;
import am2.common.registry.AMItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

import java.util.EnumSet;

public class Colour extends SpellModifier {

    @Override
    public Object[] getRecipe() {
        return new Object[]{
                new ItemStack(AMItems.chimerite),
                new ItemStack(Items.DYE, 1, OreDictionary.WILDCARD_VALUE)
        };
    }

    @Override
    public void encodeBasicData(NBTTagCompound tag, Object[] recipe) {
        for (Object obj : recipe) {
            if (obj instanceof ItemStack) {
                ItemStack is = (ItemStack) obj;
                if (is.getItem().equals(Items.DYE)) {
                    tag.setInteger("Color", is.getMetadata());
                }
            }
        }
    }

    @Override
    public EnumSet<SpellModifiers> getAspectsModified() {
        return EnumSet.of(SpellModifiers.COLOR);
    }

    @Override
    public float getModifier(SpellModifiers type, EntityLivingBase caster, Entity target, World world, NBTTagCompound nbt) {
        if (type == SpellModifiers.COLOR) {
            return nbt.getInteger("Color");
        }
        return 0;
    }

    @Override
    public float getManaCostMultiplier() {
        return 1F;
    }

}

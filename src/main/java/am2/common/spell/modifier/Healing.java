package am2.common.spell.modifier;

import am2.ArsMagica;
import am2.api.spell.SpellModifier;
import am2.api.spell.SpellModifiers;
import am2.common.registry.Affinities;
import am2.common.utils.AffinityShiftUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionUtils;
import net.minecraft.world.World;

import java.util.EnumSet;

public class Healing extends SpellModifier {
    @Override
    public EnumSet<SpellModifiers> getAspectsModified() {
        return EnumSet.of(SpellModifiers.HEALING);
    }

    @Override
    public float getModifier(SpellModifiers type, EntityLivingBase caster, Entity target, World world, NBTTagCompound metadata) {
        return ArsMagica.config.getHealingModifierValue();
    }

    @Override
    public Object[] getRecipe() {
        return new Object[]{
                AffinityShiftUtils.getEssenceForAffinity(Affinities.life),
                Items.EGG,
                PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM), PotionTypes.HEALING)
        };
    }

    @Override
    public float getManaCostMultiplier() {
        return 1.2F;
    }
}

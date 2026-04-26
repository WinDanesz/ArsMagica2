package am2.common.spell.shape;

import am2.api.affinity.Affinity;
import am2.api.extensions.ISpellCaster;
import am2.api.spell.SpellData;
import am2.api.spell.SpellModifiers;
import am2.api.spell.SpellShape;
import am2.common.items.ItemBindingCatalyst;
import am2.common.registry.AMItems;
import am2.common.registry.AMSounds;
import am2.common.spell.SpellCastResult;
import am2.common.spell.SpellCaster;
import am2.common.utils.InventoryUtilities;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

import java.util.EnumSet;

public class Binding extends SpellShape {

    @Override
    public SpellCastResult beginStackStage(SpellData spell, EntityLivingBase caster, EntityLivingBase target, World world, double x, double y, double z, EnumFacing side, boolean giveXP, int useCount) {
        if (!(caster instanceof EntityPlayer)) {
            return SpellCastResult.EFFECT_FAILED;
        }

        EntityPlayer player = (EntityPlayer) caster;
        ItemStack heldStack = player.getActiveItemStack();
        if (heldStack.isEmpty() || heldStack.getItem() != AMItems.spell) {
            return SpellCastResult.EFFECT_FAILED;
        }

        // Serialize the SpellCaster capability data into the tag compound BEFORE replaceItem,
        // so it travels with the bound item's NBT and can be restored when the item is dropped.
        ISpellCaster srcCaster = SpellCaster.of(heldStack);
        NBTBase spellCasterNBT = (srcCaster instanceof SpellCaster) ? ((SpellCaster) srcCaster).serializeNBT() : null;

        int bindingType = getBindingType(spell);
        switch (bindingType) {
            case ItemBindingCatalyst.META_AXE:
                heldStack = InventoryUtilities.replaceItem(heldStack, AMItems.bound_axe);
                break;
            case ItemBindingCatalyst.META_PICK:
                heldStack = InventoryUtilities.replaceItem(heldStack, AMItems.bound_pickaxe);
                break;
            case ItemBindingCatalyst.META_SWORD:
                heldStack = InventoryUtilities.replaceItem(heldStack, AMItems.bound_sword);
                break;
            case ItemBindingCatalyst.META_SHOVEL:
                heldStack = InventoryUtilities.replaceItem(heldStack, AMItems.bound_shovel);
                break;
            case ItemBindingCatalyst.META_HOE:
                heldStack = InventoryUtilities.replaceItem(heldStack, AMItems.bound_hoe);
                break;
            case ItemBindingCatalyst.META_BOW:
                heldStack = InventoryUtilities.replaceItem(heldStack, AMItems.bound_bow);
                break;
            case ItemBindingCatalyst.META_SHIELD:
                heldStack = InventoryUtilities.replaceItem(heldStack, AMItems.bound_shield);
                break;
            default:
                return SpellCastResult.EFFECT_FAILED;
        }
        if (spellCasterNBT != null) {
            if (!heldStack.hasTagCompound()) heldStack.setTagCompound(new NBTTagCompound());
            heldStack.getTagCompound().setTag(InventoryUtilities.KEY_BOUND_SPELL_DATA, spellCasterNBT);
        }
        player.inventory.setInventorySlotContents(player.inventory.currentItem, heldStack);
        return SpellCastResult.SUCCESS;
    }



    @Override
    public EnumSet<SpellModifiers> getModifiers() {
        return EnumSet.noneOf(SpellModifiers.class);
    }

    @Override
    public Object[] getRecipe() {
        return new Object[]{
                new ItemStack(AMItems.chimerite),
                Items.WOODEN_SWORD,
                Items.STONE_SHOVEL,
                Items.IRON_HOE,
                Items.GOLDEN_AXE,
                Items.DIAMOND_PICKAXE,
                new ItemStack(AMItems.binding_catalyst, 1, OreDictionary.WILDCARD_VALUE)
        };
    }

    @Override
    public float manaCostMultiplier() {
        return 1;
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
        return AMSounds.BINDING_CAST;
    }


//	public void setBindingType(ItemStack craftStack, ItemStack addedBindingCatalyst){
//		SpellUtils.instance.setSpellMetadata(craftStack, "binding_type", "" + addedBindingCatalyst.getItemDamage());
//	}

    public int getBindingType(SpellData spell) {
        int type = 0;
        try {
            type = spell.getStoredData().getInteger("BindingType");
        } catch (Throwable t) {

        }
        return type;
    }

    public int getBindingType(ISpellCaster spell) {
        int type = 0;
        try {
            type = spell.getCommonStoredData().getInteger("BindingType");
        } catch (Throwable t) {

        }
        return type;
    }

    @Override
    public void encodeBasicData(NBTTagCompound tag, Object[] recipe) {
        for (Object obj : recipe) {
            if (obj instanceof ItemStack) {
                ItemStack is = (ItemStack) obj;
                if (is.getItem().equals(AMItems.binding_catalyst))
                    tag.setString("BindingType", "" + is.getItemDamage());
            }
        }
    }
}

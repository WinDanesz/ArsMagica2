package am2.common.items;

import am2.api.items.IBoundItem;
import am2.common.registry.AMMaterials;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;

public class ItemBoundSword extends ItemSword implements IBoundItem {

    public ItemBoundSword() {
        super(AMMaterials.BOUND);
        this.maxStackSize = 1;
        this.setMaxDamage(0);
    }

    @Override
    public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
        int hurtResist = target.hurtResistantTime;
        target.hurtResistantTime = 0;
        applySpellOnHitEntity(stack, target, attacker);
        target.hurtResistantTime = hurtResist;
        return true;
    }

    @Override
    public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
        return false;
    }

    @Override
    public boolean onDroppedByPlayer(ItemStack item, EntityPlayer player) {
        return unbindOnDrop(item, player);
    }

    @Override
    public float maintainCost(EntityPlayer player, ItemStack stack) {
        return normalMaintain;
    }
}

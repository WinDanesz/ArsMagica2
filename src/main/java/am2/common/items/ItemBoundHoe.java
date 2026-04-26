package am2.common.items;

import am2.api.items.IBoundItem;
import am2.common.registry.AMMaterials;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemBoundHoe extends ItemHoe implements IBoundItem {

    public ItemBoundHoe() {
        super(AMMaterials.BOUND);
        this.maxStackSize = 1;
        this.setMaxDamage(0);
        this.setCreativeTab(null);
    }

    @Override
    public ItemStack onItemUseFinish(ItemStack stack, World worldIn, EntityLivingBase entityLiving) {
        applySpellAtPosition(stack, worldIn, entityLiving, entityLiving.posX, entityLiving.posY, entityLiving.posZ);
        return stack;
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

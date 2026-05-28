package am2.common.items;

import am2.api.items.IBoundItem;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemShield;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SuppressWarnings("deprecation")
public class ItemBoundShield extends ItemShield implements IBoundItem {

    public ItemBoundShield() {
        super();
        this.maxStackSize = 1;
        this.setMaxDamage(0);
        this.setCreativeTab(null);
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

    @Override
    @SideOnly(Side.CLIENT)
    public String getItemStackDisplayName(ItemStack stack) {
        return I18n.translateToLocalFormatted("item." + getRegistryName().toString() + ".name");
    }
}

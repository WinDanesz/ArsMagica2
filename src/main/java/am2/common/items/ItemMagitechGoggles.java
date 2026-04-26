package am2.common.items;

import am2.common.armor.ArsMagicaArmorMaterial;
import am2.common.registry.AMMaterials;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;

public class ItemMagitechGoggles extends AMArmor {

    public ItemMagitechGoggles() {
        super(AMMaterials.MAGITECH, ArsMagicaArmorMaterial.UNIQUE, 0, EntityEquipmentSlot.HEAD);
    }

    @Override
    public boolean hasOverlay(ItemStack stack) {
        return false;
    }

    @Override
    public int getArmorDisplay(EntityPlayer player, ItemStack armor, int slot) {
        return 2;
    }

    @Override
    public int GetDamageReduction() {
        return 2;
    }
}

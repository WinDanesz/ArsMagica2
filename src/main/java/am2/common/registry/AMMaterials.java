package am2.common.registry;

import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraftforge.common.util.EnumHelper;

public class AMMaterials {

    public static final ItemArmor.ArmorMaterial MAGITECH = EnumHelper.addArmorMaterial("magitech", "arsmagica2:magitech", 5, new int[]{1, 2, 3, 1}, 15, SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, 0.0F);
    public static final ItemArmor.ArmorMaterial ENDER = EnumHelper.addArmorMaterial("ender", "arsmagica2:ender", 7, new int[]{1, 3, 5, 2}, 25, SoundEvents.ITEM_ARMOR_EQUIP_GOLD, 0.0F);
    public static final ItemArmor.ArmorMaterial MAGE = EnumHelper.addArmorMaterial("mage", "arsmagica2:mage", 7, new int[]{1, 3, 5, 2}, 25, SoundEvents.ITEM_ARMOR_EQUIP_GOLD, 0.0F);
    public static final ItemArmor.ArmorMaterial BATTLEMAGE = EnumHelper.addArmorMaterial("battlemage", "arsmagica2:battlemage", 15, new int[]{2, 5, 6, 2}, 9, SoundEvents.ITEM_ARMOR_EQUIP_IRON, 0.0F);
    public static final Item.ToolMaterial BOUND = EnumHelper.addToolMaterial("BOUND", 3, 1561, 8.0F, 3.0F, 10);

    public static void preInit() {

    }
}

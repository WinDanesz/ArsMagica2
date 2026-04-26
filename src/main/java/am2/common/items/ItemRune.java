package am2.common.items;

import am2.ArsMagica;
import am2.api.items.IMultiTexturedItem;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

public class ItemRune extends Item implements IMultiTexturedItem {

    public ItemRune() {
        super();
        setHasSubtypes(true);
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (!this.isInCreativeTab(tab)) return;

        for (int i = 0; i < EnumDyeColor.values().length; i++) {
            items.add(new ItemStack(this, 1, i));
        }
    }

    @Override
    public String getUnlocalizedNameInefficiently(ItemStack stack) {
        return "item.arsmagica2:rune." + EnumDyeColor.byDyeDamage(stack.getItemDamage());
    }

    public int getKeyIndex(ItemStack stack) {
        return getKeyIndex(stack.getItemDamage());
    }

    public int getKeyIndex(int meta) {
        if (meta == EnumDyeColor.BLACK.getDyeDamage())
            return 0x1;
        if (meta == EnumDyeColor.BLUE.getDyeDamage())
            return 0x2;
        if (meta == EnumDyeColor.BROWN.getDyeDamage())
            return 0x4;
        if (meta == EnumDyeColor.CYAN.getDyeDamage())
            return 0x8;
        if (meta == EnumDyeColor.GRAY.getDyeDamage())
            return 0x10;
        if (meta == EnumDyeColor.GREEN.getDyeDamage())
            return 0x20;
        if (meta == EnumDyeColor.LIGHT_BLUE.getDyeDamage())
            return 0x40;
        if (meta == EnumDyeColor.SILVER.getDyeDamage())
            return 0x80;
        if (meta == EnumDyeColor.LIME.getDyeDamage())
            return 0x100;
        if (meta == EnumDyeColor.MAGENTA.getDyeDamage())
            return 0x200;
        if (meta == EnumDyeColor.ORANGE.getDyeDamage())
            return 0x400;
        if (meta == EnumDyeColor.PINK.getDyeDamage())
            return 0x800;
        if (meta == EnumDyeColor.PURPLE.getDyeDamage())
            return 0x1000;
        if (meta == EnumDyeColor.RED.getDyeDamage())
            return 0x2000;
        if (meta == EnumDyeColor.WHITE.getDyeDamage())
            return 0x4000;
        if (meta == EnumDyeColor.YELLOW.getDyeDamage())
            return 0x8000;
        return 0;
    }

    @Override
    public ResourceLocation getModelName(ItemStack stack) {
        int meta = stack.getItemDamage();
        return new ResourceLocation(ArsMagica.MODID + ":rune_" + EnumDyeColor.byDyeDamage(stack.getItemDamage()).getName());
    }
}

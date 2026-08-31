package am2.common.items;

import am2.ArsMagica;
import am2.api.items.IMultiTexturedItem;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemCore extends Item implements IMultiTexturedItem {

    public static final int META_BASE_CORE = 0;
    public static final int META_HIGH_CORE = 1;
    public static final int META_PURE = 2;

    public ItemCore() {
        super();
        setHasSubtypes(true);
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (!this.isInCreativeTab(tab)) return;
        items.add(new ItemStack(this, 1, META_BASE_CORE));
        items.add(new ItemStack(this, 1, META_HIGH_CORE));
        items.add(new ItemStack(this, 1, META_PURE));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public String getItemStackDisplayName(ItemStack stack) {
        String name = I18n.translateToLocalFormatted("item.arsmagica2:core." + (stack.getItemDamage() == META_BASE_CORE ? "base" : (stack.getItemDamage() == META_HIGH_CORE ? "high" : "pure")) + ".name");
        return name;
    }

    @Override
    public ResourceLocation getModelName(ItemStack stack) {
        int meta = stack.getItemDamage();
        switch (meta) {
            case 0:
                return new ResourceLocation(ArsMagica.MODID + ":core_base");
            case 1:
                return new ResourceLocation(ArsMagica.MODID + ":core_high");
            case 2:
                return new ResourceLocation(ArsMagica.MODID + ":core_pure");

            default:
                return new ResourceLocation(ArsMagica.MODID + ":rune_" + EnumDyeColor.values()[meta]);
        }
    }
}

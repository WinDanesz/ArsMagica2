package am2.common.items;

import am2.api.ArsMagicaAPI;
import am2.api.flickers.AbstractFlickerFunctionality;
import am2.api.items.IMultiTexturedItem;
import am2.common.utils.SpellUtils;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

public class ItemFlickerFocus extends Item implements IMultiTexturedItem {

    public ItemFlickerFocus() {
        super();
        setHasSubtypes(true);
    }

    @Override
    @SideOnly(Side.CLIENT)
    @Nonnull
    public String getItemStackDisplayName(ItemStack stack) {
        int meta = stack.getItemDamage();
        AbstractFlickerFunctionality operator = SpellUtils.GetAbstractFlickerFunctionalityFromID(meta);
        if (operator == null) return "Trash";
        return I18n.translateToLocalFormatted("item.arsmagica2:FlickerFocusPrefix", I18n.translateToLocalFormatted("item.arsmagica2:" + operator.getClass().getSimpleName() + ".name"));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(@Nonnull CreativeTabs tab, @Nonnull NonNullList<ItemStack> items) {
        if (!this.isInCreativeTab(tab)) return;
        for (AbstractFlickerFunctionality func : ArsMagicaAPI.getFlickerFocusRegistry().getValuesCollection())
            items.add(new ItemStack(this, 1, func.getID()));
    }

    @Override
    public ResourceLocation getModelName(ItemStack stack) {
        for (AbstractFlickerFunctionality flicker : ArsMagicaAPI.getFlickerFocusRegistry().getValuesCollection())
            if (flicker.getID() == stack.getItemDamage()) return flicker.getTexture();
        return null;
    }
}

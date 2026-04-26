package am2.common.items;


import am2.ArsMagica;
import am2.api.ArsMagicaAPI;
import am2.api.affinity.Affinity;
import am2.api.items.IMultiTexturedItem;
import am2.common.entity.EntityFlicker;
import am2.common.registry.Affinities;
import am2.common.utils.SpellUtils;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

public class ItemFlickerJar extends Item implements IMultiTexturedItem {

    public ItemFlickerJar() {
        super();
        this.setMaxDamage(0);
        setHasSubtypes(true);
    }

    @Override
    @SideOnly(Side.CLIENT)
    @Nonnull
    public String getItemStackDisplayName(ItemStack stack) {
        int meta = stack.getItemDamage();
        String baseName;
        if (meta == (Affinities.none).getID())
            return I18n.format("item.arsmagica2:flickerJar.name", I18n.format("am2.tooltip.empty"));
        Affinity aff = SpellUtils.GetAffinityFromID(meta);
        assert aff != null;
        baseName = I18n.format("item.arsmagica2:flickerJar.name", aff.getLocalizedName());
        return baseName;
    }

    public void setFlickerJarTypeFromFlicker(ItemStack stack, EntityFlicker flick) {
        stack.setItemDamage((flick.getFlickerAffinity().getID()));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(@Nonnull CreativeTabs tab, @Nonnull NonNullList<ItemStack> items) {
        if (!this.isInCreativeTab(tab)) return;
        for (Affinity aff : ArsMagicaAPI.getAffinityRegistry()) items.add(new ItemStack(this, 1, (aff.getID())));
    }

    @Override
    public ResourceLocation getModelName(ItemStack stack) {
        if (stack.getItemDamage() == 0) return new ResourceLocation(ArsMagica.MODID, "flicker_jar_empty");
        else return new ResourceLocation(ArsMagica.MODID, "flicker_jar");
    }

}

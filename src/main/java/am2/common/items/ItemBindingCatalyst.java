package am2.common.items;

import am2.api.items.IMultiTexturedItem;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemBindingCatalyst extends Item implements IMultiTexturedItem {

    public static final int META_PICK = 0;
    public static final int META_AXE = 1;
    public static final int META_SWORD = 2;
    public static final int META_SHOVEL = 3;
    public static final int META_HOE = 4;
    public static final int META_BOW = 5;
    public static final int META_SHIELD = 6;
    public static final int META_BLANK = 7;
    public static final String[] NAMES = {"pick", "axe", "sword", "shovel", "hoe", "bow", "shield"};

    public ItemBindingCatalyst() {
        super();
        this.setMaxDamage(0);
        this.setHasSubtypes(true);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public String getItemStackDisplayName(ItemStack stack) {
        int meta = stack.getItemDamage();

        String baseName = I18n.translateToLocalFormatted("item.arsmagica2:bindingCatalyst.name");

        switch (meta) {
            case META_PICK:
                return baseName + I18n.translateToLocalFormatted("item.arsmagica2:bindingCatalystPick.name");
            case META_AXE:
                return baseName + I18n.translateToLocalFormatted("item.arsmagica2:bindingCatalystAxe.name");
            case META_SWORD:
                return baseName + I18n.translateToLocalFormatted("item.arsmagica2:bindingCatalystSword.name");
            case META_SHOVEL:
                return baseName + I18n.translateToLocalFormatted("item.arsmagica2:bindingCatalystShovel.name");
            case META_HOE:
                return baseName + I18n.translateToLocalFormatted("item.arsmagica2:bindingCatalystHoe.name");
            case META_BOW:
                return baseName + I18n.translateToLocalFormatted("item.arsmagica2:bindingCatalystBow.name");
            case META_SHIELD:
                return baseName + I18n.translateToLocalFormatted("item.arsmagica2:bindingCatalystShield.name");
        }
        return baseName;
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> par3List) {
        if (!this.isInCreativeTab(tab) && tab != CreativeTabs.SEARCH) return;

        par3List.add(new ItemStack(this, 1, META_PICK));
        par3List.add(new ItemStack(this, 1, META_AXE));
        par3List.add(new ItemStack(this, 1, META_SHOVEL));
        par3List.add(new ItemStack(this, 1, META_SWORD));
        par3List.add(new ItemStack(this, 1, META_HOE));
        par3List.add(new ItemStack(this, 1, META_BOW));
        par3List.add(new ItemStack(this, 1, META_SHIELD));
        // par3List.add(new ItemStack(this, 1, 7));
    }

    @Override
    public ResourceLocation getModelName(ItemStack stack) {
        int meta = stack.getItemDamage();

        switch (meta) {
            case META_PICK:
                return new ResourceLocation(this.getRegistryName() + "_pick");
            case META_AXE:
                return new ResourceLocation(this.getRegistryName() + "_axe");
            case META_SWORD:
                return new ResourceLocation(this.getRegistryName() + "_sword");
            case META_SHOVEL:
                return new ResourceLocation(this.getRegistryName() + "_shovel");
            case META_HOE:
                return new ResourceLocation(this.getRegistryName() + "_hoe");
            case META_BOW:
                return new ResourceLocation(this.getRegistryName() + "_bow");
            case META_SHIELD:
                return new ResourceLocation(this.getRegistryName() + "_shield");
        }

        return this.getRegistryName();
    }
}

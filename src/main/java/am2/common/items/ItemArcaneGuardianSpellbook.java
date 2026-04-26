package am2.common.items;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemArcaneGuardianSpellbook extends ItemSpellBook {

    public ItemArcaneGuardianSpellbook() {
        super();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add(I18n.format("am2.tooltip.arcanespellbook"));
        super.addInformation(stack, worldIn, tooltip, flagIn);
    }

    @Override
    public int getItemEnchantability() {
        return 0;
    }

    @Override
    public boolean isBookEnchantable(ItemStack bookStack, ItemStack enchantBook) {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean hasEffect(@Nonnull ItemStack par1ItemStack) {
        return true;
    }

    @Override
    public EnumRarity getRarity(@Nonnull ItemStack par1ItemStack) {
        return EnumRarity.EPIC;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public String getItemStackDisplayName(ItemStack par1ItemStack) {
        ItemStack activeSpell = getActiveItemStack(par1ItemStack);
        if (!activeSpell.isEmpty())
            return String.format("\2477%s \2477(" + activeSpell.getDisplayName() + "\2477)", I18n.format("item.arsmagica2:arcane_spellbook.name"));
        return I18n.format("item.arsmagica2:arcane_spellbook.name");
    }
}

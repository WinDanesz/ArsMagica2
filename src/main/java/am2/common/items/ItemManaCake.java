package am2.common.items;

import am2.ArsMagica;
import am2.common.registry.AMPotions;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class ItemManaCake extends ItemFood {

    public ItemManaCake() {
        super(3, 0.6f, false);
    }

    @Override
    public int getHealAmount(ItemStack stack) {
        return ArsMagica.config.getManaCakeFoodAmount();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add(I18n.format("am2.tooltip.mana_regen_potion", 1, ArsMagica.config.getManaCakeRegenDuration() / 20));
    }

    @Override
    protected void onFoodEaten(ItemStack stack, World worldIn, EntityPlayer player) {
        player.addPotionEffect(new PotionEffect(AMPotions.mana_regeneration, ArsMagica.config.getManaCakeRegenDuration(), 0));
        super.onFoodEaten(stack, worldIn, player);
    }

}

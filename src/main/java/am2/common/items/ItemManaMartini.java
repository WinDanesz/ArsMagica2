package am2.common.items;

import am2.ArsMagica;
import am2.common.registry.AMPotions;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class ItemManaMartini extends ItemFood {
    public ItemManaMartini() {
        super(0, 0, false);
        this.setAlwaysEdible();
    }

    @Override
    protected void onFoodEaten(ItemStack stack, World world, EntityPlayer player) {
        if (!world.isRemote) {
            player.addPotionEffect(new PotionEffect(AMPotions.burnout_reduction, ArsMagica.config.getManaMartiniDuration(), 0));
        }
    }

    @Override
    public EnumAction getItemUseAction(ItemStack p_77661_1_) {
        return EnumAction.DRINK;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add(I18n.format("am2.tooltip.shaken"));
        super.addInformation(stack, world, tooltip, flagIn);
    }
}

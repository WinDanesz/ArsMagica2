package am2.common.items;

import am2.common.armor.ArsMagicaArmorMaterial;
import am2.common.extensions.EntityExtension;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class ItemEnderBoots extends AMArmor {

    public ItemEnderBoots(ArmorMaterial inheritFrom, ArsMagicaArmorMaterial enumarmormaterial, int par3, EntityEquipmentSlot par4) {
        super(inheritFrom, enumarmormaterial, par3, par4);
    }

    @Override
    public int getArmorDisplay(EntityPlayer player, ItemStack armor, int slot) {
        return 0;
    }

    @Override
    public void onArmorTick(World world, EntityPlayer player, ItemStack itemStack) {
        if (player.posY >= world.getActualHeight() && EntityExtension.For(player).getIsFlipped())
            EntityExtension.For(player).setInverted(!EntityExtension.For(player).isInverted());
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        tooltip.add(I18n.translateToLocalFormatted("am2.tooltip.ender_boots"));
        super.addInformation(stack, world, tooltip, flag);
    }
}

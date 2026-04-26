package am2.client.items.colorizers;

import am2.common.items.AMArmor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.ItemStack;

public class ArmorColorizer implements IItemColor {

    @Override
    public int colorMultiplier(ItemStack stack, int tintIndex) {
        if (tintIndex == 0 && stack.getItem() instanceof AMArmor) {
            return ((AMArmor) stack.getItem()).getColor(stack);
        }
        return 0xFFFFFF;
    }

}

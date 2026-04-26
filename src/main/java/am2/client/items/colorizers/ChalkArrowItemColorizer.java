package am2.client.items.colorizers;

import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class ChalkArrowItemColorizer implements IItemColor {

    @Override
    public int colorMultiplier(@Nonnull ItemStack stack, int tintIndex) {
        if (tintIndex == 0) {
            float[] c = EnumDyeColor.byMetadata(stack.getItemDamage() & 0xF).getColorComponentValues();
            return ((int)(c[0] * 255) << 16) | ((int)(c[1] * 255) << 8) | (int)(c[2] * 255);
        }
        return 0xFFFFFF;
    }
}

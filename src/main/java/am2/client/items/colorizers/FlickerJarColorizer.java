package am2.client.items.colorizers;

import am2.common.utils.SpellUtils;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Objects;

public class FlickerJarColorizer implements IItemColor {

    @Override
    public int colorMultiplier(@Nonnull ItemStack stack, int tintIndex) {
        if (tintIndex == 0) return 0xffffff;
        return Objects.requireNonNull(SpellUtils.GetAffinityFromID(stack.getItemDamage())).getColor();
    }

}

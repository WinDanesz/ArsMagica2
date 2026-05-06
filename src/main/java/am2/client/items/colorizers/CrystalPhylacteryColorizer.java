package am2.client.items.colorizers;

import am2.common.items.ItemCrystalPhylactery;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class CrystalPhylacteryColorizer implements IItemColor {
    @Override
    public int colorMultiplier(@Nonnull ItemStack stack, int tintIndex) {
        if (tintIndex == 0 && stack.getItemDamage() != ItemCrystalPhylactery.META_EMPTY) {
            int color = 0x0000FF;
            if (stack.hasTagCompound()) {
                assert stack.getTagCompound() != null;
                String className = stack.getTagCompound().getString("SummonType");
                Integer storedColor = ((ItemCrystalPhylactery) stack.getItem()).spawnableEntities.get(className);
                if (storedColor != null) {
                    color = storedColor;
                }
            }
            return color;
        }
        return 0xFFFFFF;
    }
}

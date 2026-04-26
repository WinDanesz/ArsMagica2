package am2.api.items;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public interface IMultiTexturedItem {

    ResourceLocation getModelName(ItemStack stack);
}

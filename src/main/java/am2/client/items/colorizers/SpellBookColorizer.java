package am2.client.items.colorizers;

import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

public class SpellBookColorizer implements IItemColor {

	@Override
	public int colorMultiplier(ItemStack stack, int tintIndex) {
		if (tintIndex == 0) {
			int meta = MathHelper.clamp(stack.getItemDamage(), 0, 15);
			if (meta == 0) return EnumDyeColor.BROWN.getColorValue();
			if (meta == 1) return EnumDyeColor.CYAN.getColorValue();
			if (meta == 2) return EnumDyeColor.GRAY.getColorValue();
			if (meta == 3) return EnumDyeColor.LIGHT_BLUE.getColorValue();
			if (meta == 4) return EnumDyeColor.WHITE.getColorValue();
			if (meta == 5) return EnumDyeColor.BLACK.getColorValue();
			if (meta == 6) return EnumDyeColor.ORANGE.getColorValue();
			if (meta == 7) return EnumDyeColor.PURPLE.getColorValue();
			if (meta == 8) return EnumDyeColor.BLUE.getColorValue();
			if (meta == 9) return EnumDyeColor.GREEN.getColorValue();
			if (meta == 10) return EnumDyeColor.YELLOW.getColorValue();
			if (meta == 11) return EnumDyeColor.RED.getColorValue();
			if (meta == 12) return EnumDyeColor.LIME.getColorValue();
			if (meta == 13) return EnumDyeColor.PINK.getColorValue();
			if (meta == 14) return EnumDyeColor.MAGENTA.getColorValue();
			if (meta == 15) return EnumDyeColor.SILVER.getColorValue();
		}
		return 0xffffff;
	}

}

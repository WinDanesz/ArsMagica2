package am2.client.items.colorizers;

import am2.common.items.ItemCrystalPhylactery;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.ItemStack;

public class CrystalPhylacteryColorizer implements IItemColor {
	@Override
	public int colorMultiplier(ItemStack stack, int tintIndex) {
		if (tintIndex == 0 && stack.getItemDamage() != ItemCrystalPhylactery.META_EMPTY) {
			int color = 0x0000FF;
			if (stack.hasTagCompound()){
				String className = stack.getTagCompound().getString("SpawnClassName");
				if (className != null){
					Integer storedColor = ((ItemCrystalPhylactery)stack.getItem()).spawnableEntities.get(className);
					if (storedColor != null){
						color = storedColor.intValue();
					}
				}
			}
			return color;
		}
		return 0xFFFFFF;
	}
}

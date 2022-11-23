package am2.common.bosses.ai;

import net.minecraft.entity.EntityLiving;
import net.minecraft.item.ItemStack;

public interface ISpellCastCallback<T extends EntityLiving>{
	boolean shouldCast(T host, ItemStack spell);
}

package am2.client.items.colorizers;

import am2.common.defs.ItemDefs;
import am2.common.lore.Story;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.ItemStack;

public class LostJournalColorizer implements IItemColor {

	@Override
	public int colorMultiplier(ItemStack stack, int tintIndex) {

		Story s = ItemDefs.lostJournal.getStory(stack);
		if (s == null){
			return 0xffffff;
		}
		return s.getStoryPassColor(tintIndex);
	}

}

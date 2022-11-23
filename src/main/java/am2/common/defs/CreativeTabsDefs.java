package am2.common.defs;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class CreativeTabsDefs {
	
	public static final CreativeTabs tabAM2Blocks = new CreativeTabs("am2.blocks") {
		@Override
		public ItemStack createIcon() {
			return null;
		}

		public Item getTabIconItem() {return Item.getItemFromBlock(BlockDefs.occulus);}};
	public static final CreativeTabs tabAM2Items = new CreativeTabs("am2.items") {
		@Override
		public ItemStack createIcon() {
			return null;
		}

		public Item getTabIconItem() {return ItemDefs.spellParchment;}};
}

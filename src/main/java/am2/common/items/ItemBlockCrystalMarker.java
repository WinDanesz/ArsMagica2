package am2.common.items;

import am2.common.blocks.BlockCrystalMarker;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemBlockCrystalMarker extends ItemBlockSubtypes {

	public ItemBlockCrystalMarker(Block block) {
		super(block);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public String getItemStackDisplayName(ItemStack stack) {
		return I18n.format("tile.arsmagica2:" + BlockCrystalMarker.crystalMarkerTypes[MathHelper.clamp(stack.getItemDamage(), 0, BlockCrystalMarker.crystalMarkerTypes.length - 1)].toLowerCase() + ".name");
	}
}

package am2.common.items;

import am2.common.blocks.BlockIllusionBlock.EnumIllusionType;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemBlockIllusion extends ItemBlockSubtypes {

	public ItemBlockIllusion(Block block) {
		super(block);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public String getItemStackDisplayName(ItemStack stack) {
		return I18n.format("tile.arsmagica2:illusion_block_" + EnumIllusionType.values()[MathHelper.clamp(stack.getItemDamage(), 0, EnumIllusionType.values().length - 1)].getName().toLowerCase() + ".name");
	}
}

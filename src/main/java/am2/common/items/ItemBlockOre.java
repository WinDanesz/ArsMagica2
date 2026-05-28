package am2.common.items;

import am2.common.blocks.BlockAMOre.EnumOreType;
import net.minecraft.block.Block;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemBlockOre extends ItemBlockSubtypes {

    public ItemBlockOre(Block block) {
        super(block);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public String getItemStackDisplayName(ItemStack stack) {
        return I18n.translateToLocalFormatted("tile.arsmagica2:ore_" + EnumOreType.values()[MathHelper.clamp(stack.getItemDamage(), 0, EnumOreType.values().length - 1)].getName().toLowerCase() + ".name");
    }
}

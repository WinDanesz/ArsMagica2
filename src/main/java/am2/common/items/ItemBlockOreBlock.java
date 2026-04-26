package am2.common.items;

import am2.common.blocks.BlockAMOre.EnumOreType;
import am2.common.blocks.BlockArsMagicaBlock.EnumBlockType;
import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemBlockOreBlock extends ItemBlockSubtypes {

    public ItemBlockOreBlock(Block block) {
        super(block);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public String getItemStackDisplayName(ItemStack stack) {
        return I18n.format("tile.arsmagica2:block_" + EnumBlockType.values()[MathHelper.clamp(stack.getItemDamage(), 0, EnumOreType.values().length - 1)].getName().toLowerCase() + ".name");
    }
}

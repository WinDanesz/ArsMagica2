package am2.common.items;

import am2.common.blocks.BlockIllusionBlock.EnumIllusionType;
import net.minecraft.block.Block;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class ItemBlockIllusion extends ItemBlockSubtypes {

    public ItemBlockIllusion(Block block) {
        super(block);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public String getItemStackDisplayName(ItemStack stack) {
        return I18n.translateToLocalFormatted("tile.arsmagica2:illusion_block_" + EnumIllusionType.values()[MathHelper.clamp(stack.getItemDamage(), 0, EnumIllusionType.values().length - 1)].getName().toLowerCase() + ".name");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        EnumIllusionType type = EnumIllusionType.values()[MathHelper.clamp(stack.getItemDamage(), 0, EnumIllusionType.values().length - 1)];
        tooltip.add(I18n.translateToLocalFormatted("item.arsmagica2:illusion_block_" + type.getName().toLowerCase() + ".tooltip"));
    }
}

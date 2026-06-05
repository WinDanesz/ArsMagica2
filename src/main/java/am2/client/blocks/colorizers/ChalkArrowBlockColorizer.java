package am2.client.blocks.colorizers;

import am2.common.blocks.tileentity.TileEntityChalkArrow;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nullable;

public class ChalkArrowBlockColorizer implements IBlockColor {

    @Override
    public int colorMultiplier(IBlockState state, @Nullable IBlockAccess world, @Nullable BlockPos pos, int tintIndex) {
        if (tintIndex == 0 && world != null && pos != null) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileEntityChalkArrow) {
                EnumDyeColor dye = EnumDyeColor.byMetadata(((TileEntityChalkArrow) te).getColorIndex());
                float[] c = dye.getColorComponentValues();
                return ((int)(c[0] * 255) << 16) | ((int)(c[1] * 255) << 8) | (int)(c[2] * 255);
            }
        }
        return 0xFFFFFF;
    }
}
